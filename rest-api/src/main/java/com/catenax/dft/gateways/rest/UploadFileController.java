package com.catenax.dft.gateways.rest;

import com.catenax.dft.gateways.file.CsvGateway;
import com.catenax.dft.usecases.MapToAspectUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
public class UploadFileController {

    private final CsvGateway csvGateway;
    @Autowired
    private MapToAspectUseCase mapToAspectUseCase;

    public UploadFileController(CsvGateway storageService) {
        this.csvGateway = storageService;
    }

    @RequestMapping(value = "/upload")
    public ResponseEntity<String> fileUpload(@RequestParam("file") MultipartFile file) {

        String processId = csvGateway.storeFile(file);

        Runnable runnable = () ->
        {
            List<String> x = csvGateway.processFile(processId);
            x.forEach(mapToAspectUseCase::run);
        };

        new Thread(runnable).start();
        return ok().body(processId);
    }
}
