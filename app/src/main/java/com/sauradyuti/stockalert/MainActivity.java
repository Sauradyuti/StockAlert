package com.sauradyuti.stockalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.sauradyuti.stockalert.aliceblue.AliceBlue;
import com.sauradyuti.stockalert.aliceblue.Instrument;
import com.sauradyuti.stockalert.watchlist.WatchListActivity;
import com.sauradyuti.stockalert.watchlist.WatchListData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        AliceBlue alice = AliceBlue.getInstance(this);
//        alice.getProfile();

//        while ("INCOMPLETE".equals(alice.getLoginStatus()))
//            continue;
        TextView loginText = (TextView) findViewById(R.id.loginText);
        if ("SUCCESS".equals(alice.getLoginStatus()))
            loginText.setText("Logged in!");
        else
            loginText.setText("Login Failed!");

        alice.startWebSocket();
        while (!alice.isWebSocketConnected())
            continue;
        TextView socketText = (TextView) findViewById(R.id.socketText);
        socketText.setText("Socket Connected!");
        System.out.println("Saura:::: Socket opened!");

//        while (!alice.isMasterContractDownloaded())
//            continue;
//        System.out.println("Saura:::: Master contract downloaded!");

//        WatchListData watchListData = WatchListData.getInstance(this);
//        Iterator<String> stockNames = watchListData.getStockNames();

//        Instrument abCap = alice.getInstrument("ABCAPITAL");
//        Instrument irctc = alice.getInstrument("IRCTC");
//        List<Instrument> stocks = new ArrayList<Instrument>();
//        stocks.add(abCap);
//        stocks.add(irctc);
//        alice.subscribeLiveFeed(stocks);
//        TextView subscriptionText = (TextView) findViewById(R.id.subscriptionText);
//        subscriptionText.setText("Subscribed!");

        Intent myIntent = new Intent(this, WatchListActivity.class);
//        myIntent.putExtra("key", value); //Optional parameters
        this.startActivity(myIntent);
    }
}