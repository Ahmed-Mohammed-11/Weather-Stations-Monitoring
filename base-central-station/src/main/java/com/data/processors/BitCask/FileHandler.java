package com.data.processors.BitCask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileHandler {
    private Path currentDirectory;
    private Path currentFile;

    public FileHandler() {
    }

    public FileHandler(Path currentDirectory, Path currentFile) {
        this.currentDirectory = currentDirectory;
        this.currentFile = currentFile;
    }

    public void appendToActiveFile(String s) throws IOException {
        Files.writeString(currentFile, s, StandardOpenOption.APPEND);
    }

    public void appendToActiveFile(byte[] bytes) throws IOException {
        Files.write(currentFile, bytes, StandardOpenOption.APPEND);
    }

    public void appendToFile(Path file, byte[] bytes) throws IOException {
        Files.write(file, bytes, StandardOpenOption.APPEND);
    }

    public String readValue(Path fileToRead, int offset) throws IOException {
        EfficientFileReader fr = new EfficientFileReader(fileToRead.toString(), offset);
        long timestamp = fr.getNextLong();
        int keysz = fr.getNextInt();
        int valsz = fr.getNextInt();
        int key = fr.getNextInt();
        String val = new String(fr.getNext(valsz).buffer, StandardCharsets.UTF_8);
        return val;
    }

    public long getSizeOfActiveFile() throws IOException {
        return Files.size(currentFile);
    }

    public long getSizeOfFile(Path file) throws IOException {
        return Files.size(file);
    }

    public void createNewActiveFile(int fileId) throws IOException {
        setCurrentFile(createNewFile(fileId));
        //System.out.printf("Created active file with name %s\n", getCurrentFile().toString());
    }

    public Path createNewFile(int fileId) throws IOException {
        String newFile = currentDirectory.toString() + '/' + fileId + ".bitcask";
        Path newFilePath = Path.of(newFile);
        Files.createFile(newFilePath);
        //System.out.printf("Created file with name %s\n", getCurrentFile().toString());
        return newFilePath;
    }

    public Path createNewFile(String filename) throws IOException {
        String newFile = currentDirectory.toString() + '/' + filename;
        Path newFilePath = Path.of(newFile);
        Files.createFile(newFilePath);
        //System.out.printf("Created file with name %s\n", getCurrentFile().toString());
        return newFilePath;
    }

    public int getActiveFileId() {
        Path currentFile = getCurrentFile();
        String fileName = currentFile.getFileName().toString();
        return Integer.parseInt(fileName.substring(0, fileName.indexOf('.')));
    }

    public int getFileId(String fileName){
        return Integer.parseInt(fileName.substring(0, fileName.indexOf('.')));
    }

    public Path getCurrentFile() {
        return currentFile;
    }


    public void setCurrentFile(Path currentFile) {
        this.currentFile = currentFile;
    }

    public Path getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(Path currentDirectory) {
        this.currentDirectory = currentDirectory;
    }



}
