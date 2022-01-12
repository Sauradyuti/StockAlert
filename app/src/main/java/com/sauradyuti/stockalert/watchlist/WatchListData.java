package com.sauradyuti.stockalert.watchlist;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.sauradyuti.stockalert.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class WatchListData {

    private static JSONObject watchlist;
    private static WatchListData instance;
    private static Context mContext;

    public static WatchListData getInstance(Context context) {
        if (watchlist == null) {
            mContext = context;
            readData();
            instance = new WatchListData();
        }
        return instance;
    }

    public static WatchListData getInstance() {
        return getInstance(mContext);
    }

    private static void readData() {
        try {
            File file = mContext.getFileStreamPath("watchlist.json");
            if (file.exists()) {
                FileInputStream fin = mContext.openFileInput("watchlist.json");
                int c;
                String temp = "";
                while( (c = fin.read()) != -1){
                    temp = temp + Character.toString((char)c);
                }
                watchlist = new JSONObject(temp);
                fin.close();
            }
            else {
                InputStream inputStream = mContext.getResources().openRawResource(R.raw.watchlist);
                String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();
                watchlist = new JSONObject(jsonString);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getWatchlist() {
        try {
            return watchlist.getJSONObject("watchlist");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateLiveData(String stockName, double ltp, double change) {
        try {
            JSONObject stockObject = watchlist.getJSONObject("watchlist").getJSONObject(stockName);
            stockObject.put("ltp", ltp);
            stockObject.put("change", change);
            System.out.println("Saura:::: watchlist data updated: " + watchlist.getJSONObject("watchlist").getJSONObject(stockName).toString());

            checkAlertConditions(stockName, stockObject);
//            WatchListStock updatedStock = new WatchListStock(stockName, ltp);
//            WatchListActivity.getmInstanceActivity().notifyDataUpdate(updatedStock);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateData(String stockName, double newFallBelow, double newRiseAbove) {
        try {
            JSONObject stockObject = watchlist.getJSONObject("watchlist").getJSONObject(stockName);
            stockObject.put("fallbelow", newFallBelow);
            stockObject.put("riseabove", newRiseAbove);

//            WatchListAutoSaveTask autoSaveTask = new WatchListAutoSaveTask(mContext);
//            autoSaveTask.execute();

            String watchListDataStr = watchlist.toString(2);
            FileOutputStream fOut = mContext.openFileOutput("watchlist.json",Context.MODE_PRIVATE);
            fOut.write(watchListDataStr.getBytes());
            fOut.close();
            System.out.println("Saura.... file saved");

            System.out.println("Saura:::: watchlist data updated: " + watchlist.getJSONObject("watchlist").getJSONObject(stockName).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Iterator<String> getStockNames() {
        try {
            return watchlist.getJSONObject("watchlist").keys();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public double getFallBelow(String stockName) {
        try {
            return getWatchlist().getJSONObject(stockName).getDouble("fallbelow");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public double getRiseAbove(String stockName) {
        try {
            return getWatchlist().getJSONObject(stockName).getDouble("riseabove");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void checkAlertConditions(String stockName, JSONObject stockObject) {
        try {
            double ltp = stockObject.getDouble("ltp");
            double fallBelow = stockObject.getDouble("fallbelow");
            double riseAbove = stockObject.getDouble("riseabove");
            String contentTitle = "";
            String contentText = "";

            if (ltp < fallBelow) {
                contentTitle = "Negative Alert for " + stockName;
                contentText = "Price Below " + fallBelow + " to " + ltp;
            }
            else if (ltp > riseAbove) {
                contentTitle = "Positive Alert for " + stockName;
                contentText = "Price Above " + riseAbove + " to " + ltp;
            }

            if (contentTitle.length() > 0) {
                NotificationChannel channel = null;
                NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    channel = new NotificationChannel("0", "Price Alert Channel", NotificationManager.IMPORTANCE_HIGH);
                    channel.setDescription("Channel for Price Alerts");
                    mNotificationManager.createNotificationChannel(channel);
                }
                Intent intent = new Intent(mContext, WatchListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("PriceAlertNotification", stockName);
                PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext, "0")
                        .setSmallIcon(R.drawable.alert_notification_icon)
                        .setContentTitle(contentTitle)
                        .setContentText(contentText)
                        .setOnlyAlertOnce(true)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX);

                mNotificationManager.notify(stockObject.getInt("token"), notificationBuilder.build());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
