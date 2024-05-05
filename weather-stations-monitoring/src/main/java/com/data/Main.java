package com.data;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

import static com.data.constants.KafkaProps.*;

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
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            String message = "Hi osama ! it is me a Kafka !";
            int i = 0;
            while(true) {
                producer.send(new ProducerRecord<>(TOPIC, null, message + " " + i++));
                System.out.println("Done!");
            }
        }
    }
}

//  kafka-console-consumer.sh --topic quickstart-events --bootstrap-server localhost:9092 --from-beginning
//  kafka-console-producer.sh --topic quickstart-events --bootstrap-server localhost:9092