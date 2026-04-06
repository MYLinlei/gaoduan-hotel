package com.sky.constant;

public class RedisLoginConstant {

    public static final String LOGIN_CODE_KEY = "login:code:";

    public static final long LOGIN_CODE_TTL_MINUTES = 5L;

    public static final String LOGIN_TOKEN_KEY = "login:token:";

    public static final long LOGIN_TOKEN_TTL_MINUTES = 30L;

    private RedisLoginConstant() {
    }
}
