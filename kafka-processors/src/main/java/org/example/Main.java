package org.example;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "rain-detector-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());


        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> humidityStream = builder.stream("quickstart-events");


        humidityStream.filter((key, value) -> getHumidityFromString(value) > 70)
                .map((key, value) ->
                        KeyValue.pair(key, "RAIN ALERT: Humidity is now " +
                                getHumidityFromString(value) + " From station " + key))
                .to("rain-alerts2");
        
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

    private static int getHumidityFromString(String value) {
        // Define a regular expression pattern to match the first number after "weather="
        Pattern pattern = Pattern.compile("weather=(\\d+)");
        Matcher matcher = pattern.matcher(value);

        // Find the first occurrence of the pattern
        if (matcher.find()) {
            // Get the captured group (the number after "weather=")
            String number = matcher.group(1);
            return Integer.parseInt(number);
        } else {
            System.out.println("No number found after 'weather='");
        }
        return 0;
    }

}