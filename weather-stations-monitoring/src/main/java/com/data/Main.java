package com.data;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

import static com.data.constants.KafkaProps.*;
import static com.data.constants.KafkaProps.TOPIC;

public class Main {
    private static Properties getProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.put("key.serializer", KEY_SERIALIZER);
        props.put("value.serializer", VALUE_SERIALIZER);
        return props;
    }

    public static void main(String[] args) {
        Properties props = getProperties();
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        String message = "hi ahmed this is kafka test from main";
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, message);
        producer.send(record);
        producer.close();
    }
}