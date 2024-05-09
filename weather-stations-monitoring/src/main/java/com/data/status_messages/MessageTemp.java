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
        return "MessageTemp{" +
                "station_id=" + stationId +
                ", timestamp=" + timestamp +
                ", seq_no=" + seqNo +
                ", status='" + status + '\'' +
                ", weather=" + weather.humidity + ", " + weather.temp + ", " + weather.wind_speed +
                '}';
    }
}
