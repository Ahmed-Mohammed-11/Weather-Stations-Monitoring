package com.data.processors.BitCask;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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
}
