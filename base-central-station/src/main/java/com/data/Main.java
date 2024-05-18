package com.data;

import com.data.processors.BitCask.BitCaskImpl;
import com.data.processors.BitCask.Bitcask;
import com.data.processors.Parquet.ParquetBackup;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static com.data.constants.KafkaProps.*;

public class Main {
//    private static final String outputPath = "base-central-station/src/main/java/com/data/";
    private static final String outputPath = "";

    private static Properties getProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.put("group.id", GROUP_ID);
        props.put("key.deserializer", KEY_DESERIALIZER);
        props.put("value.deserializer", VALUE_DESERIALIZER);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return props;
    }

    public static void main(String[] args) throws IOException {
        Properties props = getProperties();

        ParquetBackup parquetBackup = new ParquetBackup(outputPath + "ParquetFiles/", 10);
        BitCaskImpl bitcask = new BitCaskImpl(200, 5);
        bitcask.open(outputPath + "BitcaskFiles");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singleton(TOPIC));
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("Received ---> " + record.value());
                    System.out.println();

                    String msg = record.value();
                    JSONObject json = new JSONObject(msg);
                    int station_id = json.getInt("station_id");
                    long timestamp = json.getLong("status_timestamp");
                    bitcask.put(station_id, msg, timestamp);
                    parquetBackup.archiveRecord(json);
                }
            }
        }
    }
}