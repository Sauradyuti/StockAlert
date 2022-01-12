package com.sauradyuti.stockalert.aliceblue.tasks;

import static com.sauradyuti.stockalert.aliceblue.Constants.HOST;
import static com.sauradyuti.stockalert.aliceblue.Constants.MASTER_CONTRACT;
import static com.sauradyuti.stockalert.aliceblue.Constants.PROFILE;
import static com.sauradyuti.stockalert.aliceblue.Constants.USER_AGENT;

import android.os.AsyncTask;

import com.sauradyuti.stockalert.aliceblue.AliceBlue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MasterContractTask extends AsyncTask<String, Void, String> {

    private String masterContract;

    @Override
    protected String doInBackground(String... strings) {
        try {
            Request.Builder requestBuilder = new Request.Builder()
                    .url(HOST+MASTER_CONTRACT)
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
            System.out.println("Saura:::: MasterContract response code: " + response.code());

            this.masterContract = response.body().string();

//            AliceBlue alice = AliceBlue.getInstance();
//            alice.setMasterContractBySymbol(this.masterContract);
//            alice.setMasterContractDownloaded(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

//    @Override
//    protected void onPostExecute(String s) {
//        System.out.println("Saura:::: MasterContractTask postExecute");
//        AliceBlue alice = AliceBlue.getInstance();
//        alice.setMasterContractBySymbol(this.masterContract);
//        alice.setMasterContractDownloaded(true);
//    }
}
