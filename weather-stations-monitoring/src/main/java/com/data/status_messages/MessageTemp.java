package com.data.status_messages;

public class MessageTemp {
    int stationId;
    long seqNo;
    String status;
    long timestamp;
    Weather weather;

    MessageTemp(MessageBuilder builder) {
        this.stationId = builder.station_id;
        this.seqNo = builder.s_no;
        this.status = builder.status;
        this.timestamp = builder.timestamp;
        this.weather = builder.weather;
    }

    @Override
    public String toString() {
        return "{" +
                "\"station_id\": " + stationId + ",\n" +
                "\"status_timestamp\": " + timestamp + ",\n" +
                "\"s_no\": " + seqNo + ",\n" +
                "\"battery_status\": \"" + status + "\",\n" +
                "\"weather\": {" + "\n" +
                "\"humidity\": " + weather.humidity + ",\n" +
                "\"temperature\": " + weather.temp + ",\n" +
                "\"wind_speed\": " + weather.wind_speed + "\n" +
                "}" +
                '}';
    }
}
