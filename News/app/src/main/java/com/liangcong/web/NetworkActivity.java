package com.liangcong.web;

import android.util.Log;

public class NetworkActivity {

    private String URL;// = "http://192.168.1.8:8099/rss_newsgn.xml";
    private String type;

    public NetworkActivity(String URL, String type){
        this.URL = URL;
        this.type = type;
    }

    // Uses AsyncTask to download the XML feed
    public void loadPage(AsyncResponse asyncResponse) {
        Log.i("debug001","开始loadPage");

        DownloadXmlTask downloadXmlTask = new DownloadXmlTask();
        downloadXmlTask.setOnAsyncResponse(asyncResponse);
        downloadXmlTask.execute(URL,type);
    }
}