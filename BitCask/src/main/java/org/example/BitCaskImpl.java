package org.example;

import com.sun.jdi.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// TODO Cross-cutting concerns (logging)
// TODO Configuration files
// TODO Dockerize the library
// TODO what if a reader reads the keydir while it is being written?
public class BitCaskImpl implements Bitcask<Integer, String> {
    private Map<Integer, ValueMetaData> keyDir;
    private int sizeThreshold;
    private FileHandler fileHandler;

    private static final String defaultDirectoryName = "Bitcask";
    private static final int maxNumberOfFiles = 5;

    public BitCaskImpl() {
        initializeLocalVariables();
        sizeThreshold = 1024 * 1024 * 1024; // 1 GB
    }

    public BitCaskImpl(int sizeThreshold) {
        initializeLocalVariables();
        this.sizeThreshold = sizeThreshold;
    }

    private void initializeLocalVariables() {
        keyDir = new ConcurrentHashMap<>();
        fileHandler = new FileHandler();
    }

    public void open(String dirName) throws IOException {
        System.out.println("Opening database at " + dirName);
        fileHandler.setCurrentDirectory(Paths.get(dirName, defaultDirectoryName));
        Path currentDirectory = fileHandler.getCurrentDirectory();

        if (!Files.isDirectory(currentDirectory))
            Files.createDirectories(currentDirectory);

        int fileId = 1;
        while(Files.exists(Path.of(currentDirectory.toString()  + '/' + String.valueOf(fileId) + ".bitcask"))) {
            fileId++;
        }

        // no active file found
        if(fileId == 1){
            fileHandler.setCurrentFile(Path.of(currentDirectory.toString(), String.valueOf(fileId) + ".bitcask"));
            Files.createFile(fileHandler.getCurrentFile());
            System.out.printf("Created file name %s\n", fileHandler.getCurrentFile().toString());
        }else {
            // recover keyDir
            fileHandler.setCurrentFile(Path.of(currentDirectory.toString(), fileId-1 + ".bitcask"));
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

                handleActiveFileExceedThreshold();

                System.out.printf("Updated keyDir with key %s, and value %s\n", key.toString(), valueMetadata.toString());
                System.out.printf("Updated file %s with value %s\n", fileHandler.getCurrentFile().toString(), value);
            } catch (IOException e) {
                System.out.println("ERROR: opening file...");
            }
    }

    private void handleActiveFileExceedThreshold() {
        try {
            if(fileHandler.getSizeOfActiveFile() >= sizeThreshold) {
                System.out.printf("Active file size exceeded threshold of %d bytes\n", sizeThreshold);
                fileHandler.createNewActiveFile(fileHandler.getFileId() + 1);
                System.out.printf("Created new file %s\n", fileHandler.getCurrentFile().toString());
            }
        } catch (IOException e) {
            System.out.println("ERROR: Could not create new file");
        }
    }

    private ValueMetaData populateValueMetadata(String value) throws IOException {
        String fileId = String.valueOf(fileHandler.getFileId());
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

    // TODO make sure this is a background thread
    public synchronized void merge() {
        // iterate over KeyDir and write record which are not in the active one in another file
        // generate a hint file for that merged file
        // update the keyDir with the new file

        generateMergedFiles();

        System.out.println("Merging data files");
    }

    private void generateMergedFiles() {

        // you need to write the value in a new file
        // you need to save the metadata to use it in hint file
        // you need to update keydir
    }

    public void close() {
        System.out.println("Closing database");
    }
}
