package com.sauradyuti.stockalert.watchlist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.sauradyuti.stockalert.MainActivity;
import com.sauradyuti.stockalert.R;

import java.util.ArrayList;
import java.util.List;

public class WatchListArrayAdapter extends ArrayAdapter<WatchListStock> {

    private static final String TAG = "WatchListArrayAdapter";
    private List<WatchListStock> watchList = new ArrayList<WatchListStock>();
    private Context mContext;

    static class WatchListViewHolder {
        TextView name;
        TextView ltp;
        TextView change;
        TextView changePercent;
    }

    public WatchListArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.mContext = context;
    }

    @Override
    public void add(@Nullable WatchListStock object) {
        watchList.add(object);
        super.add(object);
    }

    @Override
    public int getCount() {
        return this.watchList.size();
    }

    @Override
    public int getPosition(@Nullable WatchListStock item) {
        for (int i=0; i<getCount(); i++) {
            if (getItem(i).getName().equals(item.getName()))
                return i;
        }
        return -1;
    }

    @Override
    public void clear() {
        this.watchList.clear();
    }

    @Nullable
    @Override
    public WatchListStock getItem(int position) {
        return this.watchList.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        WatchListViewHolder viewHolder;
        WatchListStock wlStock = getItem(position);
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.watchlist_row, parent, false);
            viewHolder = new WatchListViewHolder();
            viewHolder.name = (TextView) row.findViewById(R.id.name);
            viewHolder.ltp = (TextView) row.findViewById(R.id.ltp);
            viewHolder.change = (TextView) row.findViewById(R.id.change);
            viewHolder.changePercent = (TextView) row.findViewById(R.id.change_percent);
            row.setTag(viewHolder);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("Saura:::: item clicked!");

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                    alertDialogBuilder.setTitle(viewHolder.name.getText());
                    alertDialogBuilder
                        .setMessage(viewHolder.ltp.getText());

                    View alertDialogView = inflater.inflate(R.layout.alert_dialog, null);
                    EditText fallBelow = (EditText) alertDialogView.findViewById(R.id.fallbelow);
                    EditText riseAbove = (EditText) alertDialogView.findViewById(R.id.riseabove);
                    Button cancelButton = (Button) alertDialogView.findViewById(R.id.close_button);
                    Button saveButton = (Button) alertDialogView.findViewById(R.id.save_button);

                    WatchListData watchListData = WatchListData.getInstance();
                    fallBelow.setText(String.valueOf(watchListData.getFallBelow(viewHolder.name.getText().toString())));
                    riseAbove.setText(String.valueOf(watchListData.getRiseAbove(viewHolder.name.getText().toString())));
                    alertDialogBuilder.setView(alertDialogView);

                    AlertDialog alertDialog = alertDialogBuilder.create();

                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });
                    saveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            double newFallBelow = Double.parseDouble(fallBelow.getText().toString());
                            double newRiseAbove = Double.parseDouble(riseAbove.getText().toString());

                            WatchListData watchListData = WatchListData.getInstance();
                            watchListData.updateData(viewHolder.name.getText().toString(), newFallBelow, newRiseAbove);

                            wlStock.setFallBelow(newFallBelow);
                            wlStock.setRiseAbove(newRiseAbove);

                            System.out.println("Saura:::: fallbelow: " + newFallBelow + " riseAbove: " + newRiseAbove);
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.show();
                }
            });
        } else {
            viewHolder = (WatchListViewHolder) row.getTag();
        }

        viewHolder.name.setText(wlStock.getName());
        viewHolder.ltp.setText(String.valueOf(wlStock.getLtp()));
        viewHolder.change.setText(String.valueOf(wlStock.getChange()));
        viewHolder.changePercent.setText(String.valueOf(wlStock.getChangePercent()));
        if (wlStock.getChange() < 0) {
            viewHolder.change.setTextColor(Color.RED);
            viewHolder.changePercent.setTextColor(Color.RED);
        }
        else if (wlStock.getChange() > 0) {
            viewHolder.change.setTextColor(Color.parseColor("#849e2d"));
            viewHolder.changePercent.setTextColor(Color.parseColor("#849e2d"));
        }
        return row;
    }
}

