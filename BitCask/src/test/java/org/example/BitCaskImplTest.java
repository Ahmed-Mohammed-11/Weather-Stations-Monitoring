package org.example;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class BitCaskImplTest {

    @Test
    void open() {
    }

    @Test
    void get() {
        Bitcask<Integer, String> b = new BitCaskImpl();
        try {
            b.open("src/test/java/org/example");
        } catch (IOException e) {

        }
        b.put(1, "haha");
        b.put(3, "happy");
        assertEquals("haha", b.get(1));
        b.put(4, "You will not scare me");
        assertEquals("You will not scare me", b.get(4));
        assertEquals("happy", b.get(3));
    }

    @Test
    void testUpdate() {
        Bitcask<Integer, String> b = new BitCaskImpl();
        try {
            b.open("src/test/java/org/example");
        } catch (IOException e) {

        }
        b.put(1, "haha");
        b.put(3, "happy");
        assertEquals("haha", b.get(1));
        b.put(1, "not haha");
        assertEquals("not haha", b.get(1));
    }

    @Test
    void testMultipleFiles_ShouldCreateNewFiles() {
        BitCaskImpl b = new BitCaskImpl(1, 100);
        try {
            b.open("src/test/java/org/example/test3");
        } catch (IOException e) {

        }
        b.put(1, "haha");
        b.put(3, "happy");
        b.put(4, "You will not scare me");
        b.put(5, "Try to push through it");
        assertEquals("haha", b.get(1));
        assertEquals("happy", b.get(3));
        assertEquals("You will not scare me", b.get(4));
        assertEquals("Try to push through it", b.get(5));
        b.put(4, "You were not thinking that I will not do it");
        assertEquals("You were not thinking that I will not do it", b.get(4));
    }
    @Test
    void manyRequestTest(){
        BitCaskImpl b = new BitCaskImpl(10000, 10);
        int x = (int)1e6;
        try {
            b.open("src/test/java/org/example/testThreads");
        } catch (IOException e) {

        }
        while(x > 0){
            b.put(1, "test1");
            b.put(2, "el ta3b");
            x -= 1;
            assertEquals("test1", b.get(1));
            assertEquals("el ta3b", b.get(2));
        }
    }
}