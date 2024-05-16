package com.data.processors.BitCask;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class BitcaskFileEntry {
    public long timestamp;
    public int keysz;
    public int valuesz;
    public Integer key;
    public String value;


    public BitcaskFileEntry(long timestamp, int keysz, int valuesz, Integer key, String value) {
        this.timestamp = timestamp;
        this.keysz = keysz;
        this.valuesz = valuesz;
        this.key = key;
        this.value = value;
    }

    public static BitcaskFileEntry populateFileEntry(Integer key, String value, long timestamp) {
        int keysz = 4;
        int valuesz = value.length();
        return new BitcaskFileEntry(timestamp, keysz, valuesz, key, value);
    }

    public int getNumberOfBytes(){
        return 8 + 4 * 3 + valuesz;
    }

    public byte[] getBytes() {
        return ByteBuffer.allocate(8 + 4 * 3 + valuesz)
                .putLong(timestamp)
                .putInt(keysz)
                .putInt(valuesz)
                .putInt(key)
                .put(value.getBytes(StandardCharsets.UTF_8)).array();
    }

    public static BitcaskFileEntry fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        long timestamp = buffer.getLong();
        int keySz = buffer.getInt();
        int valueSz = buffer.getInt();
        int key = buffer.getInt();
        return new BitcaskFileEntry(timestamp, keySz, valueSz, key, null);
    }
}
