package com.data.utils;

import java.util.Random;

public class RandomGenerator {
    private static final Random random = new Random();

    public static int generateRandomIntLowerUpper(int lowerBound, int upperBound) {
        return random.nextInt(lowerBound, upperBound);
    }

    public static int generateRandomIntUpper(int upperBound) {
        return random.nextInt(upperBound);
    }
}
