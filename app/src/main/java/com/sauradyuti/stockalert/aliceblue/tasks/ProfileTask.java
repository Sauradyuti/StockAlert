package com.sauradyuti.stockalert.aliceblue.tasks;

import static com.sauradyuti.stockalert.aliceblue.Constants.HOST;
import static com.sauradyuti.stockalert.aliceblue.Constants.PROFILE;
import static com.sauradyuti.stockalert.aliceblue.Constants.USER_AGENT;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.sauradyuti.stockalert.aliceblue.AliceBlue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProfileTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... strings) {
        HttpURLConnection urlConnection = null;
        try {
            Request.Builder requestBuilder = new Request.Builder()
                    .url(HOST+PROFILE)
                    .header("User-Agent", USER_AGENT)
                    .header("Content-Type", "application/json");

            if (strings[0].length() > 100) {
                requestBuilder.header("X-Authorization-Token", strings[0]);
                requestBuilder.header("Connection", "keep-alive");
            }
            else {
                requestBuilder.header("client_id", strings[1]);
                requestBuilder.header("authorization", "Bearer " + strings[0]);
            }

            Request request = requestBuilder.build();

            Response response = AliceBlue.getInstance().getHttpClient().newCall(request).execute();
            System.out.println("Saura:::: Part 1 response code: " + response.code());
//            System.out.println("Saura:::: Part 1 response: " + response.body().string());

            Gson gson = new Gson();
            Map<String, List> map = gson.fromJson(response.body().string(), Map.class);
            System.out.println("Saura:::: Part 1 response: " + map.get("status"));

            AliceBlue aliceBlue = AliceBlue.getInstance();
            if ("success".equals(map.get("status")))
                aliceBlue.setLoggedIn("SUCCESS");
            else
                aliceBlue.setLoggedIn("FAILED");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
