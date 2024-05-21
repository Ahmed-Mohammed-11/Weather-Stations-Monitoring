package com.data.constants;

public record KafkaProps() {
//    public static final String BOOTSTRAP_SERVERS = "kafka:9092";
    // use the commented constant for local testing
    public static final String BOOTSTRAP_SERVERS = "localhost:9092";
    public static final String GROUP_ID = "central-consumers";
    public static final String TOPIC = "project-topic";
    public static final String KEY_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
    public static final String VALUE_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
}
