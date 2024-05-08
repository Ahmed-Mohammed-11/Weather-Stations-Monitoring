package com.data.constants;

public record StatusMessage() {
    public static final String LOW = "low";
    public static final String MEDIUM = "medium";
    public static final String HIGH = "high";
    public static final int HUMIDITY_BOUND = 100;
    public static final int TEMPERATURE_BOUND = 300;
    public static final int WIND_SPEED_BOUND = 50;
    public static final int GENERATE_BOUND = 100;
}
