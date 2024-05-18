package com.data.status_messages;

import static com.data.constants.StatusMessage.*;
import static com.data.utils.RandomGenerator.generateRandomIntLowerUpper;
import static com.data.utils.RandomGenerator.generateRandomIntUpper;

public class Weather {
    int humidity;
    int temp;
    int wind_speed;

    public Weather() {
        this.humidity = generateRandomIntUpper(HUMIDITY_BOUND);
        this.temp = generateRandomIntLowerUpper(TEMPERATURE_LOWER_BOUND, TEMPERATURE_UPPER_BOUND);
        this.wind_speed = generateRandomIntUpper(WIND_SPEED_BOUND);
    }

    public Weather(int humidity, int temp, int wind_speed) {
        this.humidity = humidity;
        this.temp = temp;
        this.wind_speed = wind_speed;
    }
}
