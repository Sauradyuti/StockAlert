package com.sauradyuti.stockalert.aliceblue;

import static com.sauradyuti.stockalert.aliceblue.Constants.FAILED;
import static com.sauradyuti.stockalert.aliceblue.Constants.NSE_EXCHANGE_CODE;
import static com.sauradyuti.stockalert.aliceblue.Constants.SOCKET_ENDPOINT;

import android.content.Context;
import android.os.Build;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.google.gson.Gson;
import com.sauradyuti.stockalert.R;
import com.sauradyuti.stockalert.aliceblue.tasks.HeartBeatTask;
import com.sauradyuti.stockalert.aliceblue.tasks.MasterContractTask;
import com.sauradyuti.stockalert.aliceblue.tasks.ProfileTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class AliceBlue {

    private String username;
    private String password;
    private String twoFA;
    private String secret;
    private String appId;
    private String accessToken;
    private LocalDate accessTokenCreationDate;
    private DateTimeFormatter formatter;
    private String  loginStatus;
    private Map<String, Instrument> masterContractBySymbol;
    private Map<Integer, String> masterContractByToken;
    private LocalDate masterContractCreationDate;
    private boolean isMasterContractDownloaded;

    private OkHttpClient client;
    private WebSocket ws;
    private boolean websocketConnected;
    private final Lock lock = new ReentrantLock();

    private static AliceBlue instance;
    private Context context;

    private AliceBlue(Context context) {
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        }

        loadCredentials(context);
        this.loginStatus = "SUCCESS";

        this.masterContractBySymbol = new HashMap<String, Instrument>();
        this.masterContractByToken = new HashMap<Integer, String>();
        loadMasterContract();

        this.client = new OkHttpClient.Builder()
            .addInterceptor(new HttpLoggingInterceptor())
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .cookieJar(new CookieJar() {
                private List<Cookie> cookies;
                @Override
                public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                    this.cookies =  cookies;
                }
                @Override
                public List<Cookie> loadForRequest(HttpUrl url) {
                    return cookies != null ? cookies : new ArrayList<Cookie>();
                }
            })
            .build();
    }

    public static AliceBlue getInstance(Context context) {
        if (instance == null)
            instance = new AliceBlue(context);
        return instance;
    }

    public static AliceBlue getInstance() {
        return instance;
    }

    private void loadCredentials(Context context) {
        try {
            JSONObject credentials;
            File file = context.getFileStreamPath("credentials.json");
            if (file.exists()) {
                System.out.println("Saura:::: Credentials file exists in internal storage");
                FileInputStream fin = context.openFileInput("credentials.json");
                int c;
                String temp = "";
                while( (c = fin.read()) != -1){
                    temp = temp + Character.toString((char)c);
                }
                fin.close();

                credentials = new JSONObject(temp);
            }
            else {
                System.out.println("Saura:::: Reading seeded credentials");
                InputStream inputStream = context.getResources().openRawResource(R.raw.credentials);
                String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();
                credentials = new JSONObject(jsonString);
            }

            this.username = credentials.getString("username");
            this.password = credentials.getString("password");
            this.accessToken = credentials.getString("accessToken");
            this.twoFA = credentials.getString("twoFA");
            this.secret = credentials.getString("secret");
            this.appId = credentials.getString("appId");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.accessTokenCreationDate = LocalDate.parse(credentials.getString("accessTokenCreationDate"), formatter);
                if (!this.accessTokenCreationDate.equals(LocalDate.now())) {
                    System.out.println("Saura:::: Regenerating access token");
                    this.accessToken = loginAndGetAccessToken();
                    this.accessTokenCreationDate = LocalDate.now();

                    credentials.put("accessToken", this.accessToken);
                    credentials.put("accessTokenCreationDate", this.accessTokenCreationDate.toString());
                    String credentialsStr = credentials.toString(2);
                    FileOutputStream fOut = context.openFileOutput("credentials.json",Context.MODE_PRIVATE);
                    fOut.write(credentialsStr.getBytes());
                    fOut.close();
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLoginStatus() {
        return this.loginStatus;
    }

    public void setLoggedIn(String status) {
        this.loginStatus = status;
    }

    public OkHttpClient getHttpClient() {
        return this.client;
    }

    public WebSocket getWebSocket() {
        return this.ws;
    }

    public boolean isWebSocketConnected() {
        return this.websocketConnected;
    }

    public void setWebSocketConnected(boolean value) {
        this.websocketConnected = value;
    }

    public Lock getLock() {
        return this.lock;
    }

    private String loginAndGetAccessToken() {
        Python py = Python.getInstance();
        PyObject pyObj = py.getModule("aliceblue_login");
        PyObject obj = pyObj.callAttr("main", this.username, this.password, this.twoFA, this.secret, this.appId);
        if (obj != null) {
            System.out.println("Saura:::: python script returned: " + obj.toString());
            return obj.toString();
        }
        return FAILED;
    }

    public void getProfile() {
        ProfileTask profile = new ProfileTask();
        profile.execute(new String[] { this.accessToken, this.username });
    }

//    public void loadMasterContract() {
//        MasterContractTask masterContract = new MasterContractTask();
//        masterContract.execute(new String[] { this.accessToken, this.username });
//    }

    private void loadMasterContract() {
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.master_contracts_nse);
            String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();
            JSONObject contractObj = new JSONObject(jsonString);

            Iterator<String> keys = contractObj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONArray scrips = contractObj.getJSONArray(key);
                for (int i=0; i<scrips.length(); i++) {
                    JSONObject scrip = scrips.getJSONObject(i);

                    int token = scrip.getInt("code");
                    String symbol = scrip.getString("symbol");
                    String company = scrip.getString("company");

                    Instrument instr = new Instrument(token, symbol, company);
                    this.masterContractBySymbol.put(symbol, instr);
                    this.masterContractByToken.put(token, symbol);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    public void setMasterContractBySymbol(String masterContract) {
//        Gson gson = new Gson();
//        Map<String, List> map = gson.fromJson(masterContract, Map.class);
//        for (Map.Entry<String, List> entry: map.entrySet()) {
//            List<Map> scrips = entry.getValue();
//            for (Map<String, String> scrip: scrips) {
//                int token = Integer.parseInt(scrip.get("code"));
//                Instrument instr = new Instrument(token, scrip.get("symbol"), scrip.get("company"));
//                this.masterContractBySymbol.put(scrip.get("symbol"), instr);
//                this.masterContractByToken.put(Integer.parseInt(scrip.get("code")), scrip.get("symbol"));
//            }
//        }
//    }

//    public boolean isMasterContractDownloaded() {
//        return this.isMasterContractDownloaded;
//    }

//    public void setMasterContractDownloaded(boolean value) {
//        this.isMasterContractDownloaded = value;
//    }

    public Instrument getInstrument(String stockSymbol) {
        return this.masterContractBySymbol.get(stockSymbol);
    }

    public String getStockNameByToken(int stockToken) {
        return this.masterContractByToken.get(stockToken);
    }

    public void startWebSocket() {
        String socketUri = SOCKET_ENDPOINT + this.accessToken;
        Request request = new Request.Builder()
                .url(socketUri)
                .build();
        SubscriptionListener listener = new SubscriptionListener();
        this.ws = this.client.newWebSocket(request, listener);

        HeartBeatTask heartBeat = new HeartBeatTask();
        heartBeat.execute();
    }

    public void subscribeLiveFeed(List<Instrument> stocks) {
        StringBuilder subscribeStocks = new StringBuilder();
        for (Instrument instr: stocks) {
            String data = "[" + NSE_EXCHANGE_CODE + ", " + instr.getToken() + "]";
            if (subscribeStocks.length() > 0)
                subscribeStocks.append(", ");
            subscribeStocks.append(data);
            // this.subscriptions.append(instr);
        }

        StringBuilder subscribeData = new StringBuilder();
        subscribeData.append("{\"a\": \"subscribe\", \"v\": ");
        subscribeData.append("[" + subscribeStocks.toString() + "], ");
        subscribeData.append("\"m\": \"compact_marketdata\"}");
        System.out.println("Saura:::: subscribe: " + subscribeData);

        try {
            while (!this.websocketConnected)
                Thread.sleep(50);

            this.lock.lock();
            System.out.println("Sending subscriptions...");
            this.ws.send(subscribeData.toString());
            Thread.sleep(50);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.lock.unlock();
        }
    }
}
