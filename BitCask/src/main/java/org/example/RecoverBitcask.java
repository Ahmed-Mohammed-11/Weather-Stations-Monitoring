package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RecoverBitcask {
    FileHandler fileHandler = new FileHandler();

    public Map<Integer, ValueMetaData> recoverKeyDir(Path currentDirectory) throws IOException {
        Map<Integer, ValueMetaData> keyDir = new ConcurrentHashMap<>();
        List<String> fileNames = getAllFileNames(currentDirectory);

        HashSet<Integer> fileIds = new HashSet<>();
        fileNames.forEach((fileName) -> {
            fileIds.add(fileHandler.getFileId(fileName));
        });

        for(Integer fileId: fileIds){
            Path hintFilePath = Path.of(currentDirectory.toString(), fileId + ".hint");
            if(Files.exists(hintFilePath)){
                recoverFromHintFile(hintFilePath, String.valueOf(fileId), keyDir);
            }else{
                recoverFromDataFile(currentDirectory, String.valueOf(fileId), keyDir);
            }
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

    //TODO You really dont need the timestamp
    private void recoverFromDataFile(Path currentDirectory, String fileId, Map<Integer, ValueMetaData> keyDir) throws IOException {
        Path dataFilePath = Path.of(currentDirectory.toString(), fileId + ".bitcask");
        EfficientFileReader fr = new EfficientFileReader(dataFilePath.toString(), 0);
        int currentPos = 0;
        while (fr.hasNext()) {
            int keySize = fr.getNextInt();
            int valueSz = fr.getNextInt();
            int key = fr.getNextInt();
            keyDir.put(key, new ValueMetaData(fileId, valueSz, currentPos, Instant.now().getEpochSecond()));
            fr.skipNumberOfSteps(valueSz);
            currentPos += 4 + 4 + 4 + valueSz;
        }
    }

    private void recoverFromHintFile(Path hintFilePath, String fileId, Map<Integer, ValueMetaData> keyDir) throws IOException {
        EfficientFileReader fr = new EfficientFileReader(hintFilePath.toString(), 0);
        while (fr.hasNext()) {
            int keySz = fr.getNextInt();
            int valueSz = fr.getNextInt();
            int valuePos = fr.getNextInt();
            long timestamp = fr.getNextLong();
            int key = fr.getNextInt();
            keyDir.put(key, new ValueMetaData(fileId, valueSz, valuePos, timestamp));
        }
    }
}
