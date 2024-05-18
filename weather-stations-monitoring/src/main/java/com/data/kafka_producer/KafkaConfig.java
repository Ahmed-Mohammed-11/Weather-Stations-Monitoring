package com.data.kafka_producer;

import java.util.Properties;

import static com.data.constants.KafkaProps.*;

public class KafkaConfig {
    public static Properties getProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.put("key.serializer", KEY_SERIALIZER);
        props.put("value.serializer", VALUE_SERIALIZER);
        return props;
    }
}
