package com.data.constants;

public record KafkaProps() {
    public static final String BOOTSTRAP_SERVERS = "localhost:9092";
    public static final String TOPIC = "st";
    public static final String KEY_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
    public static final String VALUE_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
}
