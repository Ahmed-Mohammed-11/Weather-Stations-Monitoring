package com.data.constants;

public record KafkaProps() {
    public static final String BOOTSTRAP_SERVERS = "localhost:9093";
    public static final String TOPIC = "quickstart-events";
    public static final String KEY_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
    public static final String VALUE_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
}
