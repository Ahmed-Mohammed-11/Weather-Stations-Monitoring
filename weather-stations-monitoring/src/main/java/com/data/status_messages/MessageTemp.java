package com.data.status_messages;

public class MessageTemp {
    int station_id;
    long s_no;
    String status;
    Weather weather;

    MessageTemp(MessageBuilder builder) {
        this.station_id = builder.station_id;
        this.s_no = builder.s_no;
        this.status = builder.status;
        this.weather = builder.weather;
    }

    @Override
    public String toString() {
        return "MessageTemp{" +
                "station_id=" + station_id +
                ", s_no=" + s_no +
                ", status='" + status + '\'' +
                ", weather=" + weather.humidity + ", " + weather.temp + ", " + weather.wind_speed +
                '}';
    }
}
