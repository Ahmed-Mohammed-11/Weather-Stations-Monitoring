package org.example;

import java.nio.file.Files;
import java.time.Instant;

public class ValueMetaData {
    public String fileId;
    public int valueSz;
    public int valuePos;
    public long timestamp;

    public ValueMetaData(String fileId, int valueSz, int valuePos, long timestamp) {
        this.fileId = fileId;
        this.valueSz = valueSz;
        this.valuePos = valuePos;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ValueMetaData{" +
                "fileId='" + fileId + '\'' +
                ", valueSz=" + valueSz +
                ", valuePos=" + valuePos +
                ", timestamp=" + timestamp +
                '}';
    }
}
