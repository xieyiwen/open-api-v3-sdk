package com.okcoin.commons.okex.open.api.test.ws.spot;

import com.okcoin.commons.okex.open.api.test.ws.spot.config.WebSocketClient;
import com.okcoin.commons.okex.open.api.test.ws.spot.config.WebSocketConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * 需要登录的频道
 * private channel
 *
 * @author oker
 * @create 2019-06-12 14:44
 **/
public class SpotPrivateChannelTest {
    private static final WebSocketClient webSocketClient = new WebSocketClient();

    @Before
    public void connect() {
        WebSocketConfig.loginConnect(webSocketClient);
        while (true) {
            if (webSocketClient.getIsLogin()) {
                return;
            } else {
                try {
                    Thread.sleep(200);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @After
    public void close() {
        webSocketClient.closeConnection();
    }

    /**
     * 币币杠杆资产 margin account
     */
    @Test
    public void marginAccountChannel() {
        //创建一个list集合，添加要订阅的频道名称
        final ArrayList<String> list = new ArrayList<>();
        list.add("spot/margin_account:LTC-USDT");
        webSocketClient.subscribe(list);
        //为保证测试方法不停，需要让线程延迟
        try {
            Thread.sleep(10000000);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 币币资产 account
     */
    @Test
    public void spotAccountChannel() {
        //创建一个list集合，添加要订阅的频道名称
        final ArrayList<String> list = new ArrayList<>();
        list.add("spot/account:LTC");
        webSocketClient.subscribe(list);
        //为保证测试方法不停，需要让线程延迟
        try {
            Thread.sleep(10000000);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 订单 order
     */
    @Test
    public void orderChannel() {
        //创建一个list集合，添加要订阅的频道名称
        final ArrayList<String> list = new ArrayList<>();
        list.add("spot/order:LTC-USDT");
        webSocketClient.subscribe(list);
        //为保证测试方法不停，需要让线程延迟
        try {
            Thread.sleep(10000000);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
