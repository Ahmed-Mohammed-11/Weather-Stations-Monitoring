package com.data.processors.BitCask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RecoverBitcask {
    FileHandler fileHandler = new FileHandler();

    // TODO use timestamp to know latest message.
    public Map<Integer, ValueMetaData> recoverKeyDir(Path currentDirectory) throws IOException {
        Map<Integer, ValueMetaData> keyDir = new ConcurrentHashMap<>();
        List<String> fileNames = getAllFileNames(currentDirectory);

        TreeSet<Integer> fileIds = new TreeSet<>();
        fileNames.forEach((fileName) -> {
            if(!Objects.equals(fileName, "merged.bitcask"))
                fileIds.add(fileHandler.getFileId(fileName));
        });

        for(Integer fileId: fileIds){
            Path hintFilePath = Path.of(currentDirectory.toString(), fileId + ".hint");
            if(Files.exists(hintFilePath))
                recoverFromHintFile(hintFilePath, String.valueOf(fileId), keyDir);
            else
                recoverFromDataFile(currentDirectory, String.valueOf(fileId), keyDir);

        }
        return keyDir;
    }

    private List<String> getAllFileNames(Path currentDirectory) throws IOException {
        List<String> fileNames = new ArrayList<>();
        Files.list(currentDirectory).forEach(path -> {
            fileNames.add(path.getFileName().toString());
        });
        return fileNames;
    }

    private void recoverFromDataFile(Path currentDirectory, String fileId, Map<Integer, ValueMetaData> keyDir) throws IOException {
        Path dataFilePath = Path.of(currentDirectory.toString(), fileId + ".bitcask");
        EfficientFileReader fr = new EfficientFileReader(dataFilePath.toString(), 0);
        int currentPos = 0;

        while (fr.hasNext()) {
            byte[] b = fr.getNext(8 + 4*3).buffer;
            BitcaskFileEntry entry = BitcaskFileEntry.fromBytes(b);
            long timestamp = entry.timestamp;
            int keySize = entry.keysz;
            int valueSz = entry.valuesz;
            int key = entry.key;

//            long timestamp = fr.getNextLong();
//            int keySize = fr.getNextInt();
//            int valueSz = fr.getNextInt();
//            int key = fr.getNextInt();

            if (keyDir.get(key) == null || keyDir.get(key) != null && keyDir.get(key).timestamp <= timestamp) {
                keyDir.put(key, new ValueMetaData(fileId, valueSz, currentPos, timestamp));
            }
            long sk = fr.skipNumberOfSteps(valueSz);
            if(sk != valueSz)
                sk  = fr.skipNumberOfSteps(valueSz - (int)sk);

            currentPos += new BitcaskFileEntry(timestamp, keySize, valueSz, key, "").getNumberOfBytes();
        }
    }

    private void recoverFromHintFile(Path hintFilePath, String fileId, Map<Integer, ValueMetaData> keyDir) throws IOException {
        EfficientFileReader fr = new EfficientFileReader(hintFilePath.toString(), 0);
        while (fr.hasNext()) {
            byte[] b  = fr.getNext(8 + 4*4).buffer;
            HintFileData h = HintFileData.fromBytes(b);
//            long timestamp = fr.getNextLong();
//            int keySz = fr.getNextInt();
//            int valueSz = fr.getNextInt();
//            int valuePos = fr.getNextInt();
//            int key = fr.getNextInt();
            if (keyDir.get(h.key) == null || keyDir.get(h.key) != null && keyDir.get(h.key).timestamp <= h.timestamp) {
                keyDir.put(h.key, new ValueMetaData(fileId, h.valueSz, h.valuePos, h.timestamp));
            }
        }
    }
}
