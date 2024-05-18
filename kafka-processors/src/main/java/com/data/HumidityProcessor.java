package com.data;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import static com.data.constants.KafkaProps.*;

public class HumidityProcessor {
    private static final Logger logger = LoggerFactory.getLogger(HumidityProcessor.class);

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APP_ID);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());


        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> humidityStream = builder.stream(TOPIC_TO_CONSUME);

        humidityStream
                .peek((key, value) -> logger.info("Before filter - Key: {}, Value: {}", key, value))
                .filter((key, value) -> getHumidityFromJSON(value) > 70)
                .peek((key, value) -> logger.info("After filter - Key: {}, Value: {}", key, value))
                .map((key, value) -> {
                    logger.info("Mapping - Key: {}, Value: {}", key, value);
                    return KeyValue.pair(key, "RAIN ALERT: Humidity is now " + getHumidityFromJSON(value) + " From station " + key);
                })
                .peek((key, value) -> logger.info("After map - Key: {}, Value: {}", key, value))
                .to(TOPIC_TO_PRODUCE_TO);

        
        KafkaStreams streams = new KafkaStreams(builder.build(), props);

        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }

    private static int getHumidityFromJSON(String value) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Station station = mapper.readValue(value, Station.class);
            return station.getWeather().getHumidity();
        } catch (Exception e) {
            return 0;
        }
    }

}