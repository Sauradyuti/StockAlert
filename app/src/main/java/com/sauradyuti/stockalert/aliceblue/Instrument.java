package com.sauradyuti.stockalert.aliceblue;

public class Instrument {

    private int token;
    private String symbol;
    private String name;

    public Instrument(int token, String symbol, String name) {
        this.token = token;
        this.symbol = symbol;
        this.name = name;
    }

    public int getToken() {
        return this.token;
    }
}
