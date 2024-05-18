package com.data.status_messages;

import static com.data.constants.StatusMessage.*;
import static com.data.utils.RandomGenerator.generateRandomIntUpper;

public class MessageBuilder {
    int station_id;
    long s_no;
    String status;
    long timestamp;
    Weather weather;

    private static String getStatus() {
        int randomInt = generateRandomIntUpper(100);
        if (randomInt <= 30) return LOW;
        else if (randomInt <= 70) return MEDIUM;
        else return HIGH;
    }

    public MessageBuilder setStationId(int station_id) {
        this.station_id = station_id;
        return this;
    }

    public MessageBuilder setSeqNo(long s_no) {
        this.s_no = s_no;
        return this;
    }

    public MessageBuilder setStatus() {
        this.status = getStatus();
        return this;
    }

    public MessageBuilder setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public MessageBuilder setWeather(Weather weather) {
        this.weather = weather;
        return this;
    }

    public MessageTemp build() {
        return new MessageTemp(this);
    }
}
