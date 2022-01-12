package com.sauradyuti.stockalert.aliceblue.tasks;

import android.os.AsyncTask;

import com.sauradyuti.stockalert.aliceblue.AliceBlue;

import java.nio.ByteBuffer;

import kotlin.text.Charsets;
import okio.ByteString;

public class HeartBeatTask extends AsyncTask<String, Void, String> {
    AliceBlue alice;
    private final String message = "{\"a\": \"h\", \"v\": [], \"m\": \"\"}";

    @Override
    protected void onPreExecute() {
        this.alice = AliceBlue.getInstance();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            while (true) {
                Thread.sleep(10000);
                send();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void send() {
        try {
            while (!alice.isWebSocketConnected())
                Thread.sleep(50);

            alice.getLock().lock();
            System.out.println("Saura:::: Sending heartbeat...");
            byte[] bytes = message.getBytes(Charsets.UTF_8);
            alice.getWebSocket().send(ByteString.of(bytes, 0, bytes.length));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            alice.getLock().unlock();
        }
    }
}
