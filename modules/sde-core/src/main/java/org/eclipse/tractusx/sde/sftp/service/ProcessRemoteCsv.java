package org.eclipse.tractusx.sde.sftp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.sde.agent.entity.SftpSchedulerReport;
import org.eclipse.tractusx.sde.agent.enums.SftpReportStatusEnum;
import org.eclipse.tractusx.sde.agent.mapper.SftpReportMapper;
import org.eclipse.tractusx.sde.agent.model.SftpReportModel;
import org.eclipse.tractusx.sde.agent.repository.SftpReportRepository;
import org.eclipse.tractusx.sde.common.entities.SubmodelFileRequest;
import org.eclipse.tractusx.sde.common.enums.ProgressStatusEnum;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.notification.manager.EmailManager;
import org.eclipse.tractusx.sde.core.processreport.entity.ProcessReportEntity;
import org.eclipse.tractusx.sde.core.processreport.repository.ProcessReportRepository;
import org.eclipse.tractusx.sde.core.service.SubmodelOrchestartorService;
import org.eclipse.tractusx.sde.sftp.RetrieverI;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.eclipse.tractusx.sde.core.utils.TryUtils.IGNORE;
import static org.eclipse.tractusx.sde.core.utils.TryUtils.tryRun;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessRemoteCsv {

    private final CsvHandlerService csvHandlerService;
    private final MetadataProvider metadataProvider;
    private final RetrieverFactory retrieverFactory;
    private final SubmodelOrchestartorService submodelOrchestartorService;
    private final SftpReportRepository sftpReportRepository;
    private final ProcessReportRepository processReportRepository;
    private final SftpReportMapper sftpReportMapper;
    private final ObjectFactory<ProcessRemoteCsv> selfFactory;

    @Autowired
    private EmailManager emailManager;

    private final ObjectMapper objectMapper = new ObjectMapper();



    @SuppressWarnings({"CallToPrintStackTrace","ResultOfMethodCallIgnored"})
    public void process(TaskScheduler taskScheduler) {
        log.info("Scheduler started");
        var schedulerId = UUID.randomUUID().toString();
        boolean loginSuccess = false;
        try (var retriever = retrieverFactory.create()) {
            loginSuccess = true;
            var submodelFileRequest = Optional.ofNullable(
                    objectMapper.convertValue(metadataProvider.getMetadata(), SubmodelFileRequest.class)
            ).orElseThrow(() -> new RuntimeException("Metadata is incorrect"));
            var inProgressIdList = StreamSupport.stream(retriever.spliterator(), false)
                    .filter(processId -> tryRun(
                            () -> retriever.setProgress(processId),
                            e -> {
                                log.info("Could not move remote file to the Progress folder {}", retriever.getFileName(processId));
                                Paths.get(csvHandlerService.getFilePath(processId)).toFile().delete();
                            })
                    ).filter(processId -> tryRun(
                            () -> submodelOrchestartorService.processSubmodelAutomationCsv(submodelFileRequest, processId),
                            e -> {
                                log.info("Could not submit CVS file for processing. {}", csvHandlerService.getFilePath(processId));
                                tryRun( () -> retriever.setFailed(processId),
                                        e1 -> log.info("Could not move file to the Failed folder {}", retriever.getFileName(processId))
                                );
                            })
                    ).peek(processId -> sftpReportRepository.save(
                            sftpReportMapper.mapFrom(
                                    SftpReportModel.builder()
                                            .schedulerId(schedulerId)
                                            .processId(processId)
                                            .fileName(retriever.getFileName(processId))
                                            .status(SftpReportStatusEnum.IN_PROGRESS)
                                            .startDate(LocalDateTime.now())
                                            .build()
                            ))
                    ).toList();
            taskScheduler.schedule(() -> checkStatusOfInprogressFiles(taskScheduler, retriever, inProgressIdList, schedulerId), Instant.now().plus(Duration.ofSeconds(5)));
        } catch (Exception e) {
            if (!loginSuccess) {
                log.info("Possible wrong credentials");
            }
            e.printStackTrace();
        }
    }

    public void checkStatusOfInprogressFiles(TaskScheduler taskScheduler, RetrieverI retriever, List<String> inProgressIdList, String schedulerId) {
        if (processReportRepository.countByProcessIdInAndStatus(inProgressIdList, ProgressStatusEnum.COMPLETED) != inProgressIdList.size()) {
            taskScheduler.schedule(() -> checkStatusOfInprogressFiles(taskScheduler, retriever, inProgressIdList, schedulerId), Instant.now().plus(Duration.ofSeconds(5)));
        } else {
            selfFactory.getObject().createDbReport(retriever, inProgressIdList).forEach(Runnable::run);
            tryRun(retriever::close, IGNORE());
            // Notification method call
            taskScheduler.schedule(() -> sendNotificationForProcessedFiles(schedulerId), Instant.now());

        }
    }

    private void sendNotificationForProcessedFiles(String schedulerId) {
        List<SftpSchedulerReport> sftpReportList = sftpReportRepository.findBySchedulerId(schedulerId);
        if(!sftpReportList.isEmpty()) {
            log.info("Send notification for scheduler: " + schedulerId);
            Map<String, Object> emailContent = new HashMap<>();
            emailContent.put("toemail", "test@email.com");
            String tableData = "";
            for (SftpSchedulerReport sftpSchedulerReport : sftpReportList) {
                tableData += "<tr>";
                emailContent.put("schedulerTime", sftpSchedulerReport.getStartDate());
                String rowData = "<td>";
                rowData += sftpSchedulerReport.getFileName() + "</td>";
                rowData += "<td>" + sftpSchedulerReport.getStatus() + "</td>";
                rowData += "<td>" + sftpSchedulerReport.getNumberOfSucceededItems() + "</td>";
                rowData += "<td>" + sftpSchedulerReport.getNumberOfFailedItems() + "</td>";
                tableData += rowData;
                tableData += "</tr>";
            }
            emailContent.put("fileTableData", tableData);
            try {
                emailManager.sendEmail(emailContent, "Scheduler status", "scheduler_status.html");
            } catch (ServiceException se) {
                log.info("Exception occurred while sending email for scheduler id: " + schedulerId + "\n" + se);
            }
        }
    }

    /***
     * Method does not close passed retriever
     * @param retriever is a RetrieverI service
     * @param completed is a List of completed ProcessId
     * @return List of actions (side effect functions) which move remote files to the appropriate locations
     */
    @Transactional
    public List<Runnable> createDbReport(RetrieverI retriever, List<String> completed) {
        List<Runnable> remoteActions = new ArrayList<>();
        if(completed.size() > 0) {
            List<SftpSchedulerReport> sftpReportList = sftpReportRepository.findByProcessIdIn(completed);
            var processReportMap = processReportRepository.findByProcessIdIn(completed).stream().collect(Collectors.toMap(ProcessReportEntity::getProcessId, Function.identity()));
            for (var sftpSchedulerReport : sftpReportList) {
                final var fileName = sftpSchedulerReport.getFileName();
                final var processId = sftpSchedulerReport.getProcessId();
                final var processReport = processReportMap.get(processId);
                final var numberOfSucceededItems = processReport.getNumberOfSucceededItems() + processReport.getNumberOfUpdatedItems();
                final var numberOfFailedItems = processReport.getNumberOfFailedItems();
                sftpSchedulerReport.setNumberOfSucceededItems(numberOfSucceededItems);
                sftpSchedulerReport.setNumberOfFailedItems(numberOfFailedItems);
                if (processReport.getNumberOfItems() == numberOfSucceededItems) {
                    sftpSchedulerReport.setStatus(SftpReportStatusEnum.SUCCESS);
                    remoteActions.add(() -> tryRun(
                            () -> retriever.setSuccess(processId),
                            e -> log.info("Could not move file {} to Success Folder", retriever.getFileName(processId))
                    ));
                } else if (numberOfSucceededItems > 0) {
                    sftpSchedulerReport.setStatus(SftpReportStatusEnum.PARTIAL_SUCCESS);
                    remoteActions.add(() -> tryRun(
                            () -> retriever.setPartial(processId),
                            e -> log.info("Could not move file {} to Partial Success Folder", retriever.getFileName(processId))
                    ));
                } else {
                    sftpSchedulerReport.setStatus(SftpReportStatusEnum.FAILED);
                    remoteActions.add(() -> tryRun(
                            () -> retriever.setFailed(processId),
                            e -> log.info("Could not move file {} to Failed Folder", retriever.getFileName(processId))
                    ));
                }
            }
        }
        return remoteActions;
    }

}
