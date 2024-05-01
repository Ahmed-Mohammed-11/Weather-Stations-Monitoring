package org.example;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
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

    public String readValue(Path fileToRead, int offset) throws IOException {
        EfficientFileReader fr = new EfficientFileReader(fileToRead.toString(), offset);
        int keysz = fr.getNextInt();
        int valsz = fr.getNextInt();
        int key = fr.getNextInt();
        String val = new String(fr.getNext(valsz).buffer, StandardCharsets.UTF_8);
        return val;
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
