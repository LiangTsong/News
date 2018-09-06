package com.liangcong.web;

import android.util.Log;

public class NetworkActivity {
    /*public static final String WIFI = "Wi-Fi";
    public static final String ANY = "Any";*/
    //private static final String URL = "http://news.qq.com/newsgn/rss_newsgn.xml";
    private String URL;// = "http://192.168.1.8:8099/rss_newsgn.xml";

    // Whether there is a Wi-Fi connection.
    /*private static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;
    // Whether the display should be refreshed.
    public static boolean refreshDisplay = true;
    public static String sPref = null;*/
    public NetworkActivity(String URL){
        this.URL = URL;
    }

    // Uses AsyncTask to download the XML feed
    public void loadPage(AsyncResponse asyncResponse) {
        Log.i("debug001","开始loadPage");

        /*if ((sPref.equals(ANY)) && (wifiConnected || mobileConnected)) {
            DownloadXmlTask downloadXmlTask = new DownloadXmlTask();
            downloadXmlTask.setOnAsyncResponse(asyncResponse);
            downloadXmlTask.execute(URL);
        } else if ((sPref.equals(WIFI)) && (wifiConnected)) {
            DownloadXmlTask downloadXmlTask = new DownloadXmlTask();
            downloadXmlTask.setOnAsyncResponse(asyncResponse);
            downloadXmlTask.execute(URL);
        } else {
            // show error
        }*/
        DownloadXmlTask downloadXmlTask = new DownloadXmlTask();
        downloadXmlTask.setOnAsyncResponse(asyncResponse);
        downloadXmlTask.execute(URL);
    }
}