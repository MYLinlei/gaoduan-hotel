package com.sky.utils;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class CodeUtil {

    public static String randomCode() {
        int value = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(value);
    }

    public static String randomToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String defaultNickname() {
        int value = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "酒店用户_" + value;
    }

    private CodeUtil() {
    }
}
