package com.sauradyuti.stockalert.aliceblue;

public final class Constants {
    // URLs
    public static final String HOST = "https://ant.aliceblueonline.com:443";
    public static final String AUTHORIZE = "/oauth2/auth";
    public static final String REDIRECT_URL = "https://ant.aliceblueonline.com/plugin/callback";
    public static final String LOGIN = "/oauth/login?login_challenge=";
    public static final String PROFILE = "/api/v2/profile";
    public static final String MASTER_CONTRACT = "/api/v2/contracts.json?exchanges=NSE";
    public static final String SOCKET_ENDPOINT = "wss://ant.aliceblueonline.com/hydrasocket/v2/websocket?access_token=";

    // Headers
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
    public static final String COOKIES_HEADER = "Set-Cookie";

    public static final int NSE_EXCHANGE_CODE = 1;
    public static final double NSE_MULTIPLIER = 100.0;

    public static final String FAILED = "FAILED";
}
