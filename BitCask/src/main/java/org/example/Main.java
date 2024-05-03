package org.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Bitcask b = new BitCaskImpl(1);
        try {
            b.open("src/main/resources");
        } catch (IOException e) {

        }
        b.put(1, "haha");
        b.put(3, "happy");
        b.put(3, "happy");
        b.put(3, "happy");
        b.put(3, "happy");
        b.put(3, "happy");
        b.put(3, "happy");
        b.put(3, "happy");
        b.put(3, "happy");
        b.put(3, "happy");
        b.put(3, "happy");
        b.put(3, "happy");
        b.put(3, "happy");
        b.put(3, "happy");
        b.put(3, "happy");
        System.out.println(b.get(3));
        b.merge();
    }
}