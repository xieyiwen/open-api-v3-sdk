package com.okcoin.commons.okex.open.api.test.ws.spot.config;

/**
 * config
 *
 * @author oker
 * @create 2019-06-12 15:06
 **/
public class WebSocketConfig {

    private static final String SERVICE_URL = "wss://okexcomreal.bafang.com:10442/ws/v3?_compress=false";
    private static final String API_KEY = "a6ac6cbb-9090-4ce1-838e-bcc5968da916";
    private static final String SECRET_KEY = "9474B52A0A62D22774B1A913B3E28F61";
    private static final String PASSPHRASE = "123456";

    public static void publicConnect(final WebSocketClient webSocketClient) {
        webSocketClient.connection(SERVICE_URL);
    }

    public static void loginConnect(final WebSocketClient webSocketClient) {
        //与服务器建立连接
        webSocketClient.connection(SERVICE_URL);
        //登录账号,用户需提供apikey，passphrase,secretkey
        webSocketClient.login(API_KEY , PASSPHRASE , SECRET_KEY);
    }
}
