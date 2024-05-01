package org.example;

import java.io.IOException;

public interface Bitcask<T, G> {
    void open(String dirName) throws IOException; // ensure that only one process opens the same database as write
    G get(T key);
    void put(T key, G value);
    void merge();
    void close();
}
