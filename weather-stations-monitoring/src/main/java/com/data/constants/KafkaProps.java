package com.data.constants;

public record KafkaProps() {
    public static final String BOOTSTRAP_SERVERS = "kafka:9092";
    // use the commented constant for local testing
//    public static final String BOOTSTRAP_SERVERS = "localhost:9092";
    public static final String TOPIC = "project-topic";
    public static final String KEY_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
    public static final String VALUE_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
    public static final int TIME_OUT = 1000;
}
