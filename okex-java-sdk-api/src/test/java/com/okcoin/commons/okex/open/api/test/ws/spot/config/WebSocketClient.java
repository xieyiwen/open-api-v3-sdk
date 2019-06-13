package com.okcoin.commons.okex.open.api.test.ws.spot.config;

import com.alibaba.fastjson.JSONArray;
import com.okcoin.commons.okex.open.api.enums.CharsetEnum;
import com.okcoin.commons.okex.open.api.utils.DateUtils;
import okhttp3.*;
import okio.ByteString;
import org.apache.commons.compress.compressors.deflate64.Deflate64CompressorInputStream;
import org.apache.commons.lang3.time.DateFormatUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * webSocket client
 *
 * @author oker
 * @create 2019-06-12 15:57
 **/
public class WebSocketClient {
    private static WebSocket webSocket = null;
    private static Boolean isLogin = false;
    private static Boolean isConnect = false;

    /**
     * 解压函数
     * Decompression function
     *
     * @param bytes
     * @return
     */
    private static String uncompress(byte[] bytes) {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
             final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
             final Deflate64CompressorInputStream zin = new Deflate64CompressorInputStream(in)) {
            byte[] buffer = new byte[1024];
            int offset;
            while (-1 != (offset = zin.read(buffer))) {
                out.write(buffer , 0 , offset);
            }
            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 与服务器建立连接，参数为服务器的URL
     * connect server
     *
     * @param url
     */
    public void connection(String url) {

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(10 , TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        webSocket = client.newWebSocket(request , new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket , Response response) {
                isConnect = true;
                //连接成功后，设置定时器，每隔25，自动向服务器发送心跳，保持与服务器连接
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        // task to run goes here
                        sendMessage("ping");
                    }
                };
                ScheduledExecutorService service = Executors
                        .newSingleThreadScheduledExecutor();
                // 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间
                service.scheduleAtFixedRate(runnable , 25 , 25 , TimeUnit.SECONDS);
                System.out.println(DateFormatUtils.format(new Date() , DateUtils.TIME_STYLE_S4) + " Connected to the server success!");
            }

            @Override
            public void onMessage(WebSocket webSocket , String s) {
                System.out.println(DateFormatUtils.format(new Date() , DateUtils.TIME_STYLE_S4) + " Receive: " + s);
                if (null != s && s.contains("login")) {
                    if (s.endsWith("true}")) {
                        isLogin = true;
                    }
                }
            }

            @Override
            public void onClosing(WebSocket webSocket , int code , String reason) {
                System.out.println(DateFormatUtils.format(new Date() , DateUtils.TIME_STYLE_S4) + " Connection is disconnected !!！");
                webSocket.close(1000 , "Long time not to send and receive messages! ");
                webSocket = null;
                isConnect = false;
            }

            @Override
            public void onClosed(WebSocket webSocket , int code , String reason) {
                System.out.println("Connection has been disconnected.");
                isConnect = false;
            }

            @Override
            public void onFailure(WebSocket webSocket , Throwable t , Response response) {
                t.printStackTrace();
                System.out.println("Connection failed!");
                isConnect = false;
            }

            @Override
            public void onMessage(WebSocket webSocket , ByteString bytes) {
                String s = WebSocketClient.uncompress(bytes.toByteArray());
                System.out.println(DateFormatUtils.format(new Date() , DateUtils.TIME_STYLE_S4) + " Receive: " + s);
                if (null != s && s.contains("login")) {
                    if (s.endsWith("true}")) {
                        isLogin = true;
                    }
                }
            }
        });
    }

    /**
     * 获得sign
     * sign
     *
     * @param message
     * @param secret
     * @return
     */
    private String sha256_HMAC(String message , String secret) {
        String hash = "";
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(CharsetEnum.UTF_8.charset()) , "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] bytes = sha256_HMAC.doFinal(message.getBytes(CharsetEnum.UTF_8.charset()));
            hash = Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            System.out.println(DateFormatUtils.format(new Date() , DateUtils.TIME_STYLE_S4) + "Error HmacSHA256 ===========" + e.getMessage());
        }
        return hash;
    }

    /**
     * @param list
     * @return
     */
    private String listToJson(List<String> list) {
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(list);
        return jsonArray.toJSONString();
    }

    /**
     * 登录
     * login
     *
     * @param apiKey
     * @param passPhrase
     * @param secretKey
     */
    public void login(String apiKey , String passPhrase , String secretKey) {
        String timestamp = (Double.parseDouble(DateUtils.getEpochTime()) + 28800) + "";
        String message = timestamp + "GET" + "/users/self/verify";
        String sign = sha256_HMAC(message , secretKey);
        String str = "{\"op\"" + ":" + "\"login\"" + "," + "\"args\"" + ":" + "[" + "\"" + apiKey + "\"" + "," + "\"" + passPhrase + "\"" + "," + "\"" + timestamp + "\"" + "," + "\"" + sign + "\"" + "]}";
        sendMessage(str);
    }


    /**
     * 订阅，参数为频道组成的集合
     * Bulk Subscription
     *
     * @param list
     */
    public void subscribe(List<String> list) {
        String s = listToJson(list);
        String str = "{\"op\": \"subscribe\", \"args\":" + s + "}";
        sendMessage(str);
    }

    /**
     * 取消订阅，参数为频道组成的集合
     * unsubscribe
     *
     * @param list
     */
    public void unsubscribe(List<String> list) {
        String s = listToJson(list);
        String str = "{\"op\": \"unsubscribe\", \"args\":" + s + "}";
        sendMessage(str);
    }

    private void sendMessage(String str) {
        if (null != webSocket) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(DateFormatUtils.format(new Date() , DateUtils.TIME_STYLE_S4) + " Send: " + str);
            webSocket.send(str);
        } else {
            System.out.println(DateFormatUtils.format(new Date() , DateUtils.TIME_STYLE_S4) + " Please establish a connection before operation !!!");
        }
    }

    /**
     * 断开连接
     * Close Connection
     */
    public void closeConnection() {
        if (null != webSocket) {
            webSocket.close(1000 , "User close connect !!!");
        } else {
            System.out.println(DateFormatUtils.format(new Date() , DateUtils.TIME_STYLE_S4) + " Please establish a connection before operation !!!");
        }
        isConnect = false;
    }

    public boolean getIsLogin() {
        return isLogin;
    }
}
