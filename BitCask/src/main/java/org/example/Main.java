package org.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Bitcask b = new BitCaskImpl(1024*1024, 5);
        try {
            b.open("src/main/resources");
        } catch (IOException e) {

        }

        String m = "{\n" +
                "\"station_id\": 1, // Long\n" +
                "\"s_no\": 1, // Long auto-incremental with each message per service\n" +
                "\"battery_status\": \"low\", // String of (low, medium, high)\n" +
                "\"status_timestamp\": 1681521224, // Long Unix timestamp\n" +
                "\"weather\": {\n" +
                "\"humidity\": 35, // Integer percentage\n" +
                "\"temperature\": 100, // Integer in fahrenheit\n" +
                "\"wind_speed\": 13, // Integer km/h\n" +
                "}\n" +
                "}";
//        int j = 1;
//        for (int i = 0; i < 1e6; i++){
//            b.put(j, m);
//            j++;
//            j%=11;
//            if(j==0)
//                j=1;
//        }
        System.out.println(b.get(1));
        System.out.println(b.get(2));
        System.out.println(b.get(3));
        System.out.println(b.get(4));


    }
}