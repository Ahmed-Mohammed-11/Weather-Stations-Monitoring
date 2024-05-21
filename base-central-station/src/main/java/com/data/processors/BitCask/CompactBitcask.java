package com.data.processors.BitCask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class CompactBitcask {
    private final Bitcask<Integer, String> bitCask;
    private final FileHandler fileHandler;

    public CompactBitcask(Bitcask<Integer, String> bitCask, FileHandler fileHandler) {
        this.bitCask = bitCask;
        this.fileHandler = fileHandler;
    }

    public synchronized Map<Integer, ValueMetaData> merge(Map<Integer, ValueMetaData> keyDir, int activeFileId) {
        //System.out.println("Merging data files with active id: " + activeFileId);

        Map<Integer, ValueMetaData> newKeyDir;
        try{
            newKeyDir = generateMergedFile(activeFileId, keyDir);
        } catch (IOException e){
            // rollback
//            System.out.println("ERROR: could not merge files");
//            System.out.println(e);
            return new HashMap<>();
        }
        deleteUnusedFiles(keyDir);
        updateActiveFileId();
        return newKeyDir;
    }

    private synchronized Map<Integer, ValueMetaData> generateMergedFile(int activeFileId, Map<Integer, ValueMetaData> keyDir)
            throws IOException {
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
            if(valueMetaData.fileId.equals(String.valueOf(activeFileId))) {
                continue;
            }

            // generate new metadata for that record
            int newOffset = (int)fileHandler.getSizeOfFile(newFilePath);
            ValueMetaData newMetaData = new ValueMetaData(String.valueOf(newFileId), valueMetaData.valueSz,
                    newOffset, valueMetaData.timestamp);
            HintFileData hintFileData =
                    new HintFileData(4, valueMetaData.valueSz, newOffset, valueMetaData.timestamp, key);

            // write the new value in a new file
            String value = bitCask.get(key);
            BitcaskFileEntry bitcaskFileEntry = BitcaskFileEntry.populateFileEntry(key, value, valueMetaData.timestamp);
            fileHandler.appendToFile(newFilePath, bitcaskFileEntry.getBytes());

            // update the keyDir with the new metadata
            newKeyDir.put(key, newMetaData);
            byte[] b = hintFileData.getBytes();
            HintFileData h = HintFileData.fromBytes(b);
            // write that record in the hint file
            fileHandler.appendToFile(hintFilePath, hintFileData.getBytes());
        }
        return newKeyDir;
    }

    private void deleteUnusedFiles(Map<Integer, ValueMetaData> keyDir) {
        HashSet<String> CurrentFilesSet = new HashSet<>();
        for (Map.Entry<Integer, ValueMetaData> entry : keyDir.entrySet()) {
            CurrentFilesSet.add(entry.getValue().fileId);
        }

        try {
            for (Path p : fileHandler.getAllFilesInDatabase()) {
                try {
                    String fileName = p.getFileName().toString();
                    if(fileName.equals("merged.bitcask") || fileName.equals("1.hint")) {
                        continue;
                    }
                    int fileId = fileHandler.getFileId(fileName);

                    if(CurrentFilesSet.contains(String.valueOf(fileId)))
                        continue;

                    Files.delete(Path.of(fileHandler.getCurrentDirectory().toString() + '/' + fileId + ".bitcask"));
                    // System.out.println("Deleted file " + i);
//                    if(fileId == 1)
//                        continue;
//                    Files.delete(Path.of(fileHandler.getCurrentDirectory().toString() + '/' + fileId + ".bitcask.hint"));
                } catch (IOException e) {
                    // System.out.println(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateActiveFileId() {
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
    }
}
