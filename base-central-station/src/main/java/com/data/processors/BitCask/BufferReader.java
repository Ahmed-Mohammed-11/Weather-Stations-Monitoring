package com.data.processors.BitCask;

public class BufferReader {
    public byte[] buffer;
    public int size;

    public BufferReader(byte[] buffer, int size) {
        this.buffer = buffer;
        this.size = size;
    }
}