package com.data.constants;

public record KafkaProps() {
    public static final String BOOTSTRAP_SERVERS = "localhost:9093";
    public static final String GROUP_ID = "central-consumers";
    public static final String TOPIC = "quickstart-events";
    public static final String KEY_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
    public static final String VALUE_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
}
