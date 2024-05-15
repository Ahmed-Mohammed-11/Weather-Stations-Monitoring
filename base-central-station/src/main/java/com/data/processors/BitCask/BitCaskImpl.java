package com.data.processors.BitCask;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
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
            throw new RuntimeException("Key not found");

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
            try {
                // TODO handle timestamp when the message is sent
                ValueMetaData valueMetadata = populateValueMetadata(value);
                BitcaskFileEntry entry = populateFileEntry(key, value);
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

                if(fileHandler.getActiveFileId() > maxNumberOfFiles){
                     Thread thread = new Thread(() -> merge());
                     thread.start();
                }

                //System.out.printf("Created new file %s\n", fileHandler.getCurrentFile().toString());
            }
        } catch (IOException e) {
//            System.out.println("ERROR: Could not create new file");
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
        long timestamp = Instant.now().getEpochSecond();
        return new BitcaskFileEntry(timestamp, keysz, valuesz, key, value);
    }

    public synchronized void merge() {
        int activeFileId = fileHandler.getActiveFileId();
        //System.out.println("Merging data files with active id: " + activeFileId);

        Map<Integer, ValueMetaData> newKeyDir;
        try{
            newKeyDir = generateMergedFile();
        } catch (IOException e){
            // rollback
//            System.out.println("ERROR: could not merge files");
//            System.out.println(e);
            return;
        }
        HashSet<String> set = new HashSet<>();
        for (Map.Entry<Integer, ValueMetaData> entry : keyDir.entrySet()) {
            set.add(entry.getValue().fileId);
        }
        // merge the keydir
        for (int i = activeFileId ; i > 0; i--) {
            try {
                if(set.contains(String.valueOf(i)))
                    continue;
                Files.delete(Path.of(fileHandler.getCurrentDirectory().toString() + '/' + i + ".bitcask"));
                //System.out.println("Deleted file " + i);
                Files.delete(Path.of(fileHandler.getCurrentDirectory().toString() + '/' + i + ".bitcask.hint"));
            } catch (IOException e) {
//                System.out.println(e);
            }
        }

        // update active file id
        try {
            // Move (rename) the file
            Path mergedFilePath = Path.of(fileHandler.getCurrentDirectory().toString() + "/merged.bitcask");
            Path targetPath = Path.of(fileHandler.getCurrentDirectory().toString() + '/' + 1 + ".bitcask");
            Files.move(mergedFilePath, targetPath, StandardCopyOption.ATOMIC_MOVE);
            // System.out.println("File renamed successfully.");
        } catch (IOException e) {
//            System.err.println("Error occurred: " + e);
        }

        synchronized (keyDir){
            mergeKeyDir(newKeyDir, keyDir);
            fileHandler.setCurrentFile(Path.of(fileHandler.getCurrentDirectory().toString() + '/' + 1 + ".bitcask"));
        }
    }

    private synchronized Map<Integer, ValueMetaData> generateMergedFile() throws IOException {
        // init a new keydir
        Map<Integer, ValueMetaData> newKeyDir = new HashMap<>();

        // create a new file for writing with bigger id & hint file
        int newFileId = 1;
        if (Files.exists(Path.of(fileHandler.getCurrentDirectory().toString()  + '/' + "merged" + ".bitcask"))) {
            Files.delete(Path.of(fileHandler.getCurrentDirectory().toString()  + '/' + "merged" + ".bitcask"));
        }
        if (Files.exists(Path.of(fileHandler.getCurrentDirectory().toString()  + '/' + newFileId + ".hint"))) {
            Files.delete(Path.of(fileHandler.getCurrentDirectory().toString()  + '/' + newFileId + ".hint"));
        }
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
            int newOffset = (int)fileHandler.getSizeOfFile(newFilePath);
            ValueMetaData newMetaData = new ValueMetaData(String.valueOf(newFileId), valueMetaData.valueSz,
                    newOffset, valueMetaData.timestamp);
            HintFileData hintFileData =
                    new HintFileData(4, valueMetaData.valueSz, newOffset, valueMetaData.timestamp, key);

            // write the new value in a new file
            String value = get(key);
            BitcaskFileEntry bitcaskFileEntry = populateFileEntry(key, value);
            fileHandler.appendToFile(newFilePath, bitcaskFileEntry.getBytes());

            // update the keyDir with the new metadata
            newKeyDir.put(key, newMetaData);

            // write that record in the hint file
            fileHandler.appendToFile(hintFilePath, hintFileData.getBytes());
        }
        return newKeyDir;
    }

    private void mergeKeyDir(Map<Integer, ValueMetaData> smallMap, Map<Integer, ValueMetaData> largeMap) {
        for (Map.Entry<Integer, ValueMetaData> entry : smallMap.entrySet()) {
            if(largeMap.containsKey(entry.getKey())) {
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
