package com.data.processors.BitCask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// TODO Cross-cutting concerns (logging)
// TODO Configuration files
// TODO Dockerize the library
public class BitCaskImpl implements Bitcask<Integer, String> {
    private Map<Integer, ValueMetaData> keyDir;
    private final int sizeThreshold;
    private final int maxNumberOfFiles;
    private FileHandler fileHandler;
    private RecoverBitcask recoverBitcask;
    private CompactBitcask compactBitcask;

    private static final String defaultDirectoryName = "Bitcask";

    public BitCaskImpl() {
        initializeLocalVariables();
        sizeThreshold = 1024 * 1024 * 1024; // 1 GB
        maxNumberOfFiles = 5;
    }

    public BitCaskImpl(int sizeThreshold, int maxNumberOfFiles) {
        initializeLocalVariables();
        this.sizeThreshold = sizeThreshold;
        this.maxNumberOfFiles = maxNumberOfFiles;
    }

    private void initializeLocalVariables() {
        keyDir = new ConcurrentHashMap<>();
        fileHandler = new FileHandler();
        recoverBitcask = new RecoverBitcask();
        compactBitcask = new CompactBitcask(this, fileHandler);
    }

    public void open(String dirName) throws IOException {
        //System.out.println("Opening database at " + dirName);
        fileHandler.setCurrentDirectory(Paths.get(dirName, defaultDirectoryName));
        Path currentDirectory = fileHandler.getCurrentDirectory();

        if (!Files.isDirectory(currentDirectory))
            Files.createDirectories(currentDirectory);

        int fileId = getLatestFileId(currentDirectory);

        // no active file found
        if(fileId == 1){
            fileHandler.setCurrentFile(Path.of(currentDirectory.toString(), fileId + ".bitcask"));
            Files.createFile(fileHandler.getCurrentFile());
            //System.out.printf("Created file name %s\n", fileHandler.getCurrentFile().toString());
        }else {
            // recover keyDir
            keyDir = recoverBitcask.recoverKeyDir(currentDirectory);
            fileHandler.setCurrentFile(Path.of(currentDirectory.toString(), fileId-1 + ".bitcask"));
        }

    }

    private int getLatestFileId(Path currentDirectory) {
        int fileId = 1;
        while(Files.exists(Path.of(currentDirectory.toString()  + '/' + fileId + ".bitcask"))) {
            fileId++;
        }
        return fileId;
    }

    public String get(Integer key) {
        //System.out.println("Getting value for key " + key);

        if(!keyDir.containsKey(key))
            return "Key not found";

        ValueMetaData valueMetadata = keyDir.get(key);
        Path fileToReadPath = Paths.get(fileHandler.getCurrentDirectory().toString(), valueMetadata.fileId + ".bitcask");
        String value = "";
        try {
            value = fileHandler.readValue(fileToReadPath, valueMetadata.valuePos);
        } catch (IOException e) {
//            System.out.println(e);
        }
        return value;
    }

    public synchronized void put(Integer key, String value) {
        prepareDataAndPut(key, value, Instant.now().getEpochSecond());
    }

    public synchronized  void put(Integer key, String value, long timestamp) {
        if(this.keyDir.containsKey(key) && this.keyDir.get(key).timestamp > timestamp){
            System.out.println("Latest timestamp = " + this.keyDir.get(key).timestamp + " is greater than the provided timestamp = " + timestamp);
            return;
        }

        prepareDataAndPut(key, value, timestamp);
    }

    private synchronized void prepareDataAndPut(Integer key, String value, long timestamp) {
        try {
            // TODO handle timestamp when the message is sent
            ValueMetaData valueMetadata = populateValueMetadata(value, timestamp);
            BitcaskFileEntry entry = BitcaskFileEntry.populateFileEntry(key, value, timestamp);
            fileHandler.appendToActiveFile(entry.getBytes());
            keyDir.put(key, valueMetadata);
            handleActiveFileExceedThreshold();

            // System.out.printf("Updated keyDir with key %s, and value %s\n", key.toString(), keyDir.get(key).toString());
            // System.out.printf("Updated file %s with value %s\n", fileHandler.getCurrentFile().toString(), value);
        } catch (IOException e) {
//                System.out.println("ERROR: opening file...");
//                System.out.println(e);
        }
    }

    private void handleActiveFileExceedThreshold() {
        try {
            if(fileHandler.getSizeOfActiveFile() >= sizeThreshold) {
                // System.out.printf("Active file size exceeded threshold of %d bytes\n", sizeThreshold);
                int newFileId = fileHandler.getActiveFileId() + 1;
                while(Files.exists(Path.of(fileHandler.getCurrentDirectory().toString()  + '/' + newFileId + ".bitcask"))){
                    newFileId++;
                }
                fileHandler.createNewActiveFile(newFileId);

                if(fileHandler.getNumberOfFiles() > maxNumberOfFiles){
                     Thread thread = new Thread(() -> merge());
                     thread.start();
                }

                //System.out.printf("Created new file %s\n", fileHandler.getCurrentFile().toString());
            }
        } catch (IOException e) {
//            System.out.println("ERROR: Could not create new file");
        }
    }

    private ValueMetaData populateValueMetadata(String value, long timestamp) throws IOException {
        String fileId = String.valueOf(fileHandler.getActiveFileId());
        int valueSz = value.length();
        int valuePos = (int) Files.size(fileHandler.getCurrentFile()); // assume that maxSize would not exceed 4GB

        return new ValueMetaData(fileId, valueSz, valuePos, timestamp);
    }

    public synchronized void merge() {
        int activeFileId = fileHandler.getActiveFileId();
        //System.out.println("Merging data files with active id: " + activeFileId);

        Map<Integer, ValueMetaData> newKeyDir = compactBitcask.merge(this.keyDir, activeFileId);

        synchronized (keyDir){
            mergeKeyDir(newKeyDir, keyDir, activeFileId);
            int fileId = getLatestFileId(fileHandler.getCurrentDirectory());
            try{
                fileHandler.createNewActiveFile(fileId);
            } catch (IOException e) {
                System.out.println(e);
                // rollback
            }
//            fileHandler.setCurrentFile(Path.of(fileHandler.getCurrentDirectory().toString() + '/' + file + ".bitcask"));
        }
    }

    private void mergeKeyDir(Map<Integer, ValueMetaData> smallMap, Map<Integer, ValueMetaData> largeMap, int activeFileId) {
        for (Map.Entry<Integer, ValueMetaData> entry : smallMap.entrySet()) {
            if(largeMap.containsKey(entry.getKey()) && Integer.parseInt(largeMap.get(entry.getKey()).fileId) >= activeFileId) {
                continue;
            }
            //System.out.printf("Merging key %s with old value %s to new value %s\n", entry.getKey(),
                    //largeMap.get(entry.getKey()).toString(), entry.getValue().toString());
            largeMap.put(entry.getKey(), entry.getValue());
        }
    }

    public void close() {
        System.out.println("Closing database");
    }
}
