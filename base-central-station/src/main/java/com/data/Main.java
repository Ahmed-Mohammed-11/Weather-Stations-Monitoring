package com.data;

import com.data.processors.ParquetBackup;
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
    private static final String outputPath = "/home/osama/Documents/DDIA_Project/Weather-Stations-Monitoring/base-central-station/src/main/java/com/data/Parquets/";

    private static Properties getProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.put("group.id", GROUP_ID);
        props.put("key.deserializer", KEY_DESERIALIZER);
        props.put("value.deserializer", VALUE_DESERIALIZER);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return props;
    }

    public static void main(String[] args) throws IOException {
        Properties props = getProperties();
        ParquetBackup parquetBackup = new ParquetBackup(outputPath, 10);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singleton(TOPIC));
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("Received ---> " + record.value());
                    System.out.println();
                    String jj = record.value();
                    JSONObject j = new JSONObject(jj);
                    parquetBackup.archiveRecord(j);
                }
            }
        }
    }
}