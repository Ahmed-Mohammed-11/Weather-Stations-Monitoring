package org.example;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BitcaskFileEntry {
    public int keysz;
    public int valuesz;
    public Integer key;
    public String value;


    public BitcaskFileEntry(int keysz, int valuesz, Integer key, String value) {
        this.keysz = keysz;
        this.valuesz = valuesz;
        this.key = key;
        this.value = value;
    }

    public byte[] getBytes() {
        byte[] bytes = ByteBuffer.allocate(4*3 + valuesz).putInt(keysz)
                .putInt(valuesz).putInt(key).put(value.getBytes(StandardCharsets.UTF_8)).array();
        System.out.println("writing the following bytes:");
        for (byte b : bytes) {
            System.out.printf("%x ", b);
        }

        return bytes;
    }
}
