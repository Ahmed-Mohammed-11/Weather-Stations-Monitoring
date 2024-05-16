package com.data;

public class Station {
    private int station_id;
    private long status_timestamp;
    private int s_no;
    private String battery_status;
    private Weather weather;

    public Station(){

    }

    public Station(int station_id, long status_timestamp, int s_no, String battery_status, Weather weather) {
        this.station_id = station_id;
        this.status_timestamp = status_timestamp;
        this.s_no = s_no;
        this.battery_status = battery_status;
        this.weather = weather;
    }

    public int getStation_id() {
        return station_id;
    }

    public void setStation_id(int station_id) {
        this.station_id = station_id;
    }

    public long getStatus_timestamp() {
        return status_timestamp;
    }

    public void setStatus_timestamp(long status_timestamp) {
        this.status_timestamp = status_timestamp;
    }

    public int getS_no() {
        return s_no;
    }

    public void setS_no(int s_no) {
        this.s_no = s_no;
    }

    public String getBattery_status() {
        return battery_status;
    }

    public void setBattery_status(String battery_status) {
        this.battery_status = battery_status;
    }

    public Weather getWeather() {
        return weather;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
    }
}
