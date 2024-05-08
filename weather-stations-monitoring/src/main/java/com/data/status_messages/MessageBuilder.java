package com.data.status_messages;

public class MessageBuilder {
    int station_id;
    long s_no;
    String status;
    Weather weather;

    public void setStation_id(int station_id) { this.station_id = station_id; }

    public void setS_no(long s_no) {
        this.s_no = s_no;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
    }

    public MessageTemp build() {
        return new MessageTemp(this);
    }
}
