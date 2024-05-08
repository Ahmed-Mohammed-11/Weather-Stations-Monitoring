package org.example;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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

    public byte[] getBytes() {

        return ByteBuffer.allocate(4*2 + fileId.length() + 8)
                .put(fileId.getBytes(StandardCharsets.UTF_8))
                .putInt(valueSz)
                .putInt(valuePos)
                .putLong(timestamp).array();
    }

    public int getNumberOfBytes() {
        return 4*2 + fileId.length() + 8;
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
