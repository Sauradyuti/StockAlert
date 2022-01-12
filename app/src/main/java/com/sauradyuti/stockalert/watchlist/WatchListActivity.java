package com.sauradyuti.stockalert.watchlist;

import static android.widget.AdapterView.*;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sauradyuti.stockalert.R;
import com.sauradyuti.stockalert.aliceblue.AliceBlue;
import com.sauradyuti.stockalert.aliceblue.Instrument;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WatchListActivity extends Activity {

    private static final String TAG = "WatchListActivity";

    private WatchListArrayAdapter watchListArrayAdapter;
    private ListView listView;

    private static int colorIndex;
    public static WeakReference<WatchListActivity> weakActivity;

    public static WatchListActivity getmInstanceActivity() {
        return weakActivity.get();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                String stockName = extras.getString("PriceAlertNotification");
                System.out.println("Saura:::: notification clicked on " + stockName);
            }
        }

        weakActivity = new WeakReference<>(WatchListActivity.this);
        setContentView(R.layout.watchlist);

        colorIndex = 0;
        listView = (ListView) findViewById(R.id.listview);
        watchListArrayAdapter = new WatchListArrayAdapter(this, R.layout.watchlist_row);
        listView.setAdapter(watchListArrayAdapter);

        WatchListData watchListData = WatchListData.getInstance(this);
        JSONObject watchlist = watchListData.getWatchlist();
        Iterator<String> names = watchlist.keys();
        AliceBlue alice = AliceBlue.getInstance();
        List<Instrument> subscriptions = new ArrayList<Instrument>();
        while (names.hasNext()) {
            try {
                String name = names.next();
                double ltp = watchlist.getJSONObject(name).getDouble("ltp");
                double change = watchlist.getJSONObject(name).optDouble("change", 0);
                double fallBelow = watchlist.getJSONObject(name).getDouble("fallbelow");
                double riseAbove = watchlist.getJSONObject(name).getDouble("riseabove");
                boolean isActive = watchlist.getJSONObject(name).getBoolean("active");

                WatchListStock stock = new WatchListStock(name, ltp, change, fallBelow, riseAbove);
                watchListArrayAdapter.add(stock);

                if (isActive)
                    subscriptions.add(alice.getInstrument(name));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        alice.subscribeLiveFeed(subscriptions);

        final Handler handler = new Handler();
        handler.postDelayed( new Runnable() {
            @Override
            public void run() {
                System.out.println("Saura:::: Refreshing list data...");
                WatchListData watchListData = WatchListData.getInstance();
                JSONObject watchlist = watchListData.getWatchlist();
                Iterator<String> names = watchlist.keys();
                watchListArrayAdapter.clear();
                while (names.hasNext()) {
                    try {
                        String name = names.next();
                        double ltp = watchlist.getJSONObject(name).getDouble("ltp");
                        double change = watchlist.getJSONObject(name).optDouble("change", 0);
                        double fallBelow = watchlist.getJSONObject(name).getDouble("fallbelow");
                        double riseAbove = watchlist.getJSONObject(name).getDouble("riseabove");
                        WatchListStock stock = new WatchListStock(name, ltp, change, fallBelow, riseAbove);
                        watchListArrayAdapter.add(stock);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                watchListArrayAdapter.notifyDataSetChanged();
                handler.postDelayed( this, 3000 );
            }
        }, 3000 );
    }

    public void notifyDataUpdate(WatchListStock updatedStock) {
        int position = watchListArrayAdapter.getPosition(updatedStock);
        System.out.println("Saura:::: received data update at position: " + position);
        watchListArrayAdapter.remove(watchListArrayAdapter.getItem(position));
        watchListArrayAdapter.insert(updatedStock, position);
        watchListArrayAdapter.notifyDataSetChanged();
    }
}
