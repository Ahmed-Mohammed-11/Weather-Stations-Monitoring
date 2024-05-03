package org.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Bitcask b = new BitCaskImpl(1);
        try {
            b.open("src/main/resources");
        } catch (IOException e) {

        }
//        while(true){
//            b.put(1, "made my eyes burn");
//            System.out.println(b.get(1));
//        }
        b.put(1, "test1");
        b.put(2, "test2");
        b.put(3, "test3");
        b.put(4, "test4");
        b.put(5, "test5");
        b.put(6, "test6");
        b.put(1, "test1");
        b.put(1, "test1");
        b.put(1, "test1");
        b.put(1, "test1");
        b.put(1, "test1");
        b.put(1, "test1");
        b.put(1, "test1");
        b.put(1, "test1");
        b.put(1, "test1");
        System.out.println(b.get(1));
        System.out.println(b.get(2));
        System.out.println(b.get(3));
        System.out.println(b.get(4));
        System.out.println(b.get(5));
        System.out.println(b.get(6));
    }
}