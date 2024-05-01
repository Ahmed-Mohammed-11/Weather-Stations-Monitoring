package org.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Bitcask b = new BitCaskImpl();
        try {
            b.open("src/main/resources");
        } catch (IOException e) {

        }
        b.put(1, "haha");

    }
}