package com.data;

import com.data.status_messages.MessageBuilder;
import com.data.status_messages.MessageTemp;
import com.data.status_messages.Weather;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.Random;

import static com.data.constants.KafkaProps.*;
import static com.data.constants.StatusMessage.*;
import static org.apache.kafka.common.utils.Utils.sleep;

public class Main {
    private static final Random random = new Random();

    private static Properties getProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.put("key.serializer", KEY_SERIALIZER);
        props.put("value.serializer", VALUE_SERIALIZER);
        return props;
    }

    private static String getStatus() {
        int randomInt = random.nextInt(100);
        if (randomInt <= 30) return LOW;
        else if (randomInt <= 70) return MEDIUM;
        else return HIGH;
    }

    public static void main(String[] args) {
        int stationId = Integer.parseInt(args[0]);
        int seqNo = 1;
        Properties props = getProperties();

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            long startTime = System.currentTimeMillis();
            long timeLeft;
            while (true) {
                MessageBuilder messageBuilder = new MessageBuilder();
                messageBuilder.setStation_id(stationId);
                messageBuilder.setS_no(seqNo++);
                messageBuilder.setStatus(getStatus());
                messageBuilder.setWeather(new Weather(
                        random.nextInt(HUMIDITY_BOUND),
                        random.nextInt(TEMPERATURE_BOUND),
                        random.nextInt(WIND_SPEED_BOUND)));
                MessageTemp statusMessage = messageBuilder.build();
                if (random.nextInt(GENERATE_BOUND) < 10) continue;
                timeLeft = 1000 - (System.currentTimeMillis() - startTime);
                if (timeLeft > 0) sleep(timeLeft);
                ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, statusMessage.toString());
                producer.send(record);
                startTime = System.currentTimeMillis();
            }
        }
    }
}
