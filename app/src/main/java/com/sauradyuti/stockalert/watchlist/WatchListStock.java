package com.sauradyuti.stockalert.watchlist;

public class WatchListStock {

    private static final String TAG = "WatchListStock";

    private String name;
    private double ltp;
    private double change;
    private double fallBelow;
    private double riseAbove;

    public WatchListStock(String name, double ltp, double change, double fallBelow, double riseAbove) {
        this.name = name;
        this.ltp = ltp;
        this.change = change;
        this.fallBelow = fallBelow;
        this.riseAbove = riseAbove;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public double getChangePercent() {
        return Math.round(change/(ltp-change)*100.0);
    }

    public double getFallBelow() {
        return fallBelow;
    }

    public void setFallBelow(double fallBelow) {
        this.fallBelow = fallBelow;
    }

    public double getRiseAbove() {
        return riseAbove;
    }

    public void setRiseAbove(double riseAbove) {
        this.riseAbove = riseAbove;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLtp() {
        return ltp;
    }

    public void setLtp(double ltp) {
        this.ltp = ltp;
    }
}
