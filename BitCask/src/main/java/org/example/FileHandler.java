package org.example;

import java.io.IOException;
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

    public String readValue(Path fileToRead, int offset) {
        return "not implemented";
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
