package com.sauradyuti.stockalert.watchlist;

import android.content.Context;
import android.os.AsyncTask;

import com.sauradyuti.stockalert.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

public class WatchListAutoSaveTask extends AsyncTask<String, Void, String> {
    private WeakReference<Context> contextRef;

    public WatchListAutoSaveTask(Context context) {
        this.contextRef = new WeakReference<>(context);
    }
    @Override
    protected String doInBackground(String... strings) {
        try {
            JSONObject watchListData = WatchListData.getInstance().getWatchlist();
            String watchListDataStr = watchListData.toString(2);
            FileWriter file = new FileWriter(this.contextRef.get().getResources().getResourceName(R.raw.watchlist));
            file.write(watchListDataStr);
            file.flush();
            file.close();
            System.out.println("Saura.... file saved");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
