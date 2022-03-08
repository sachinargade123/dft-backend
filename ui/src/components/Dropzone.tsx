import React, { SyntheticEvent, useState } from 'react';
import { FileType } from '../models/FileType';
import { File } from '../models/File';
import UploadForm from '../components/UploadForm';

const DropZone = () => {
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
  const [isDragging, setIsDragging] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState('');
  let dragCounter = 0;

  const validateFile = (file: File) => {
    const validTypes: string[] = Object.values(FileType);
    return validTypes.includes(file.type);
  };

  const handleFiles = (file: File) => {
    /*if (validateFile(file)) {
      setSelectedFiles([...selectedFiles, file]);
    } else {
      file.invalid = true;
      setErrorMessage('File not permitted');
    } */
    setSelectedFiles([...selectedFiles, file]);
  };

  const dragEnter = (e: any) => {
    e.preventDefault();
    e.stopPropagation();
    dragCounter++;
    if (e.dataTransfer.items && e.dataTransfer.items.length > 0) {
      setIsDragging(true);
    }
  };

  const dragLeave = (e: any) => {
    e.preventDefault();
    e.stopPropagation();
    dragCounter--;
    if (dragCounter > 0) return;
    setIsDragging(false);
  };

  const fileDrop = (e: any) => {
    e.preventDefault();
    const files = e.dataTransfer.files;
    if (files.length && files.length < 2 && selectedFiles.length === 0) {
      handleFiles(files[0]);
    } else {
      setErrorMessage('Only one file is permitted');
    }
    setIsDragging(false);
  };

  const removeSelectedFiles = (clearState: boolean) => {
    if (clearState) setSelectedFiles([]);
  };

  return (
    <div className=" bg-cover min-h-screen min-w-screen grid grid-cols-5 gap-19  ">
      <section
        className="col-span-12"
        onDragOver={(e: SyntheticEvent) => e.preventDefault()}
        onDragEnter={dragEnter}
        onDragLeave={dragLeave}
        onDrop={fileDrop}
      >
        {errorMessage !== '' && <div className="bg-red-400 w-full h-48">{errorMessage}</div>}
        {!isDragging && (
          <div className="flex flex-row justify-center w-auto h-full items-center">
            <div className="flex flex-col items-center">
              <UploadForm
                getSelectedFiles={(files: any) => handleFiles(files)}
                selectedFiles={selectedFiles}
                removeSelectedFiles={removeSelectedFiles}
              />
              <h1 className="text-center text-white text-5xl">Drag & Drop files here or upload via form</h1>
            </div>
          </div>
        )}
        {isDragging && (
          <div className="relative w-full h-full bg-white">
            <div className="inset-x-0 inset-y-1/2 absolute z-5 flex flex-col justify-center gap-y-2 text-center">
              <h1 className="text-4xl">Drop it like it's hot :)</h1>
              <p className="text-lg">Upload files by dropping them in this window</p>
            </div>
          </div>
        )}
      </section>
    </div>
  );
};

export default DropZone;