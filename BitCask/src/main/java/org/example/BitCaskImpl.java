package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class BitCaskImpl implements Bitcask<Integer, String>{
    private Map<Integer, ValueMetaData> keyDir;
    private int sizeThreshold;
    private FileHandler fileHandler;

    private static final String defaultDirectoryName = "Bitcask";

    public BitCaskImpl() {
        keyDir = new HashMap<Integer, ValueMetaData>();
        sizeThreshold = 1024 * 1024 * 1024; // 1 GB
        fileHandler = new FileHandler();
    }

    public void open(String dirName) throws IOException {
        System.out.println("Opening database at " + dirName);
        fileHandler.setCurrentDirectory(Paths.get(dirName, defaultDirectoryName));
        Path currentDirectory = fileHandler.getCurrentDirectory();

        if (!Files.isDirectory(currentDirectory))
            Files.createDirectory(currentDirectory);

        int fileId = 1;
        while(Files.exists(Path.of(currentDirectory.toString()  + '/' + String.valueOf(fileId) + ".bitcask"))) {
            fileId++;
        }

        if(fileId == 1){
            fileHandler.setCurrentFile(Path.of(currentDirectory.toString(), String.valueOf(fileId) + ".bitcask"));
            Files.createFile(fileHandler.getCurrentFile());
            System.out.printf("Created file name %s\n", fileHandler.getCurrentFile().toString());
        }else {
            // recover keyDir
            fileHandler.setCurrentFile(Path.of(currentDirectory.toString(), "1.bitcask"));
        }

    }

    public String get(Integer key) {
        System.out.println("Getting value for key " + key);

        if(!keyDir.containsKey(key))
            throw new RuntimeException("Key not found");

        ValueMetaData valueMetadata = keyDir.get(key);
        Path fileToReadPath = Paths.get(fileHandler.getCurrentDirectory().toString(), valueMetadata.fileId + ".bitcask");
        String value = "";
        try {
            value = fileHandler.readValue(fileToReadPath, valueMetadata.valuePos);
        } catch (IOException e) {
            System.out.println("Could not find file to read...");
        }
        return value;
    }


    // TODO make it atomic
    public synchronized void put(Integer key, String value) {
            try {
                // TODO handle timestamp when the message is sent
                ValueMetaData valueMetadata = populateValueMetadata(value);
                BitcaskFileEntry entry = populateFileEntry(key, value);
                fileHandler.appendToActiveFile(entry.getBytes());
                keyDir.put(key, valueMetadata);

                System.out.printf("Updated keyDir with key %s, and value %s\n", key.toString(), valueMetadata.toString());
                System.out.printf("Updated file %s with value %s", fileHandler.getCurrentFile().toString(), value);
            } catch (IOException e) {
                System.out.println("Error opening file...");
            }
    }

    private ValueMetaData populateValueMetadata(String value) throws IOException {
        String fileId = getFileId();
        int valueSz = value.length();
        int valuePos = (int) Files.size(fileHandler.getCurrentFile()); // assume that maxSize would not exceed 4GB
        long timestamp = Instant.now().getEpochSecond();

        return new ValueMetaData(fileId, valueSz, valuePos, timestamp);
    }

    private BitcaskFileEntry populateFileEntry(Integer key, String value) {
        int keysz = 4;
        int valuesz = value.length();
        return new BitcaskFileEntry(keysz, valuesz, key, value);
    }

    private String getFileId() {
        Path currentFile = fileHandler.getCurrentFile();
        String fileName = currentFile.getFileName().toString();
        return fileName.substring(0, fileName.indexOf('.'));
    }

    public void merge() {
        System.out.println("Merging data files");
    }

    public void close() {
        System.out.println("Closing database");
    }
}
