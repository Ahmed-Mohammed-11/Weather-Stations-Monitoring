package com.data.constants;

public record KafkaProps() {
    public static final String BOOTSTRAP_SERVERS = "localhost:9092";
    public static final String APP_ID = "rain-detector";
    public static final String TOPIC_TO_CONSUME = "quickstart-events";
    public static final String TOPIC_TO_PRODUCE_TO = "rain-alerts2";

}
