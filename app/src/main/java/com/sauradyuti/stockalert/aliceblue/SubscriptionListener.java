package com.sauradyuti.stockalert.aliceblue;

import static com.sauradyuti.stockalert.aliceblue.Constants.NSE_MULTIPLIER;

import com.sauradyuti.stockalert.watchlist.WatchListData;

import java.nio.ByteBuffer;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.Buffer;
import okio.ByteString;

public final class SubscriptionListener extends WebSocketListener {
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private AliceBlue aliceBlue;
    private WatchListData watchListData;

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        System.out.println("Saura:::: WebSocket CONNECTED");
        aliceBlue = AliceBlue.getInstance();
        aliceBlue.setWebSocketConnected(true);
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        System.out.println("Saura:::: WebSocket Receiving : " + text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        System.out.println("Saura:::: WebSocket Receiving bytes : " + bytes.hex());
        byte byteArray[] = bytes.toByteArray();
        int token = parseBytes(byteArray, 2, 6);
        double ltp = parseBytes(byteArray, 6, 10)/NSE_MULTIPLIER;
        double change = parseBytes(byteArray, 10, 14)/NSE_MULTIPLIER;
        int timestamp = parseBytes(byteArray, 14, 18);

        // if timestamp == 0 notify

        System.out.println("Saura:::: token: " + token);
        System.out.println("Saura:::: ltp: " + ltp);
        System.out.println("Saura:::: change: " + change);
        System.out.println("Saura:::: exchange timestamp: " + timestamp);

        watchListData = WatchListData.getInstance();
        watchListData.updateLiveData(aliceBlue.getStockNameByToken(token), ltp, change);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null);
        System.out.println("Saura:::: WebSocket Closing : " + code + " / " + reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        System.out.println("Saura:::: WebSocket Error : " + t.getMessage());
    }

    private int parseBytes(byte bytes[], int from, int to) {
        byte result[] = new byte[to - from];
        for (int i=from; i<to; i++)
            result[i-from] = bytes[i];
        return ByteBuffer.wrap(result).getInt();
    }
}
