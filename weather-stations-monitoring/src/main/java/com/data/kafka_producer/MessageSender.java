package com.data.kafka_producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import static com.data.constants.KafkaProps.TOPIC;

public class MessageSender {

    public static void sendRecord(KafkaProducer producer, Object statusMessage) {
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, statusMessage.toString());
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                System.err.println("Error sending message: " + exception.getMessage());
            } else {
                System.out.println("Sent ---> " + statusMessage);
            }
        });
    }

    public static void close(KafkaProducer producer) {
        producer.close();
    }
}
