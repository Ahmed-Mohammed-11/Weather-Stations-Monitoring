package org.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Bitcask b = new BitCaskImpl(1);
        try {
            b.open("src/main/resources");
        } catch (IOException e) {

        }

//        b.put(1, "test1");
//        b.put(2, "test2");
//        b.put(3, "test3");
//        b.put(4, "test4");
//        b.put(5, "test5");
//        b.put(6, "test6");
        System.out.println(b.get(1));
        System.out.println(b.get(2));
        System.out.println(b.get(3));
        System.out.println(b.get(4));
        System.out.println(b.get(5));
        System.out.println(b.get(6));
        b.put(55, "finally");
        System.out.println(b.get(55));
        b.put(66, "ta3ban");
        System.out.println(b.get(66));

    }
}