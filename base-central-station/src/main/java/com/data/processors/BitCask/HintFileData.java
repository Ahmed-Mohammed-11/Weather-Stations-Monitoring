package com.data.processors.BitCask;
import java.nio.ByteBuffer;

public class HintFileData {
    public int keySz;
    public int valueSz;
    public int valuePos;
    public long timestamp;
    public int key;

    public HintFileData(int keySz, int valueSz, int valuePos, long timestamp, int key) {
        this.keySz = keySz;
        this.valueSz = valueSz;
        this.valuePos = valuePos;
        this.timestamp = timestamp;
        this.key = key;
    }

    public byte[] getBytes() {
        return ByteBuffer.allocate(4*4 + 8)
                .putLong(timestamp)
                .putInt(keySz)
                .putInt(valueSz)
                .putInt(valuePos)
                .putInt(key).array();
    }

    public static HintFileData fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        long timestamp = buffer.getLong();
        int keySz = buffer.getInt();
        int valueSz = buffer.getInt();
        int valuePos = buffer.getInt();
        int key = buffer.getInt();
        return new HintFileData(keySz, valueSz, valuePos, timestamp, key);
    }

    @Override
    public String toString() {
        return "hintFileData{" +
                "keySz=" + keySz +
                ", valueSz=" + valueSz +
                ", valuePos=" + valuePos +
                ", timestamp=" + timestamp +
                ", key=" + key +
                '}';
    }
}
