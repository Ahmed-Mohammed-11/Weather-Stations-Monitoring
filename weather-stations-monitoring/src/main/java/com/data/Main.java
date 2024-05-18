package com.data;

import com.data.status_messages.MessageBuilder;
import com.data.status_messages.MessageTemp;
import com.data.status_messages.Weather;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.Properties;

import static com.data.constants.KafkaProps.TIME_OUT;
import static com.data.constants.StatusMessage.STATION_SEQ_NUM_LOWER_BOUND;
import static com.data.kafka_producer.KafkaConfig.getProperties;
import static com.data.kafka_producer.MessageSender.sendRecord;
import static com.data.utils.RandomGenerator.generateRandomIntUpper;
import static org.apache.kafka.common.utils.Utils.sleep;

public class Main {

    private static MessageTemp buildMessage(int stationId, int seqNo) {
        return new MessageBuilder()
                .setStationId(stationId)
                .setSeqNo(seqNo)
                .setStatus()
                .setTimestamp(System.currentTimeMillis())
                .setWeather(new Weather())
                .build();
    }

    public static void main(String[] args) {
        int stationId = Integer.parseInt(args[0]);
        int seqNo = STATION_SEQ_NUM_LOWER_BOUND;
        Properties kafkaProps = getProperties();

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(kafkaProps)) {
            while (true) {
                MessageTemp statusMessage = buildMessage(stationId, seqNo++);
                if (generateRandomIntUpper(100) < 10) {
                    sleep(TIME_OUT);
                    continue;
                }
                sendRecord(producer, String.valueOf(stationId), statusMessage);
                sleep(TIME_OUT);
            }
        }
    }
}
