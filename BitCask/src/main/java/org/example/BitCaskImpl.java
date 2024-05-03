package org.example;

import com.sun.jdi.Value;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
        while(Files.exists(Path.of(currentDirectory.toString()  + '/' + fileId + ".bitcask"))) {
            fileId++;
        }

        // no active file found
        if(fileId == 1){
            fileHandler.setCurrentFile(Path.of(currentDirectory.toString(), fileId + ".bitcask"));
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
                int newFileId = fileHandler.getActiveFileId() + 1;
                while(Files.exists(Path.of(fileHandler.getCurrentDirectory().toString()  + '/' + newFileId + ".bitcask"))){
                    newFileId++;
                }
                fileHandler.createNewActiveFile(newFileId);
//                merge();
                System.out.printf("Created new file %s\n", fileHandler.getCurrentFile().toString());
            }
        } catch (IOException e) {
            System.out.println("ERROR: Could not create new file");
        }
    }

    private ValueMetaData populateValueMetadata(String value) throws IOException {
        String fileId = String.valueOf(fileHandler.getActiveFileId());
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
        System.out.println("Merging data files");
        int activeFileId = fileHandler.getActiveFileId();
        // You need to save the whole metadata (new) including offset of value in new file
        // you basically need to generate a new keydir and then merge it in the end
        // you need to take value and write it immedietly in file because values may be very large
        // generate a hint file for that merged file
        // update the keyDir with the new file


        // what if you fail during deletion, you would need to ensure that no one reads the old files
        Map<Integer, ValueMetaData> newKeyDir;
        try{
            newKeyDir = generateMergedFile();
        } catch (IOException e){
            //rollback
            System.out.println("ERROR: could not merge files");
            return;
        }
        // delete all the old files.
        for (int i = activeFileId-1; i > 0; i--) {
            try {
                Files.delete(Path.of(fileHandler.getCurrentDirectory().toString() + '/' + i + ".bitcask"));
                Files.delete(Path.of(fileHandler.getCurrentDirectory().toString() + '/' + i + ".bitcask.hint"));
            } catch (IOException e) {
                System.out.println("ERROR: could not delete file");
            }
        }
        // TODO change above code to reflect that we may not begin from 1
        // update active file id
        try {
            // Move (rename) the file
            Path mergedFilePath = Path.of(fileHandler.getCurrentDirectory().toString() + "/merged.bitcask");
            Path targetPath = Path.of(fileHandler.getCurrentDirectory().toString() + '/' + 1 + ".bitcask");
            Files.move(mergedFilePath, targetPath, StandardCopyOption.ATOMIC_MOVE);
            System.out.println("File renamed successfully.");
        } catch (IOException e) {
            System.err.println("Error occurred: " + e);
        }
        // merge the keydir
        mergeKeyDir(newKeyDir, keyDir);
        synchronized (keyDir){
            fileHandler.setCurrentFile(Path.of(fileHandler.getCurrentDirectory().toString() + '/' + 1 + ".bitcask"));
        }
    }

    private synchronized Map<Integer, ValueMetaData> generateMergedFile() throws IOException {

        // init a new keydir
        Map<Integer, ValueMetaData> newKeyDir = new HashMap<>();

        // create a new file for writing with bigger id & hint file
        int newFileId = 1;
        Path newFilePath = fileHandler.createNewFile("merged.bitcask");
        Path hintFilePath = fileHandler.createNewFile(newFileId + ".hint");

        // iterate over KeyDir
        for (Map.Entry<Integer, ValueMetaData> entry : keyDir.entrySet()) {
            Integer key = entry.getKey();
            ValueMetaData valueMetaData = entry.getValue();
            // process records which are not in the active file
            if(valueMetaData.fileId.equals(String.valueOf(fileHandler.getActiveFileId()))) {
                continue;
            }

            // generate new metadata for that record
            valueMetaData.fileId = String.valueOf(newFileId);
            // TODO change value size to long, or merge into multiple files
            int newOffset = (int)fileHandler.getSizeOfFile(newFilePath);
            ValueMetaData newMetaData = new ValueMetaData(String.valueOf(newFileId), valueMetaData.valueSz,
                    newOffset, valueMetaData.timestamp);

            // write the new value in a new file
            String value = get(key);
            BitcaskFileEntry bitcaskFileEntry = populateFileEntry(key, value);
            fileHandler.appendToFile(newFilePath, bitcaskFileEntry.getBytes());

            // update the keyDir with the new metadata
            newKeyDir.put(key, newMetaData);

            // TODO we dont need to store the fileid in the hint file
            // write that record in the hint file
            fileHandler.appendToFile(hintFilePath, newMetaData.getBytes());
        }
        return newKeyDir;
    }

    private void mergeKeyDir(Map<Integer, ValueMetaData> smallMap, Map<Integer, ValueMetaData> largeMap) {
        for (Map.Entry<Integer, ValueMetaData> entry : smallMap.entrySet()) {
            if(!largeMap.containsKey(entry.getKey())) {
                continue;
            }
            largeMap.put(entry.getKey(), entry.getValue());
        }
    }

    public void close() {
        System.out.println("Closing database");
    }
}
