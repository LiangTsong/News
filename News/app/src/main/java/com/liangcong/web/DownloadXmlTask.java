package com.liangcong.web;


import android.os.AsyncTask;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

// Implementation of AsyncTask used to download XML feed
class DownloadXmlTask extends AsyncTask<String, Void, ArrayList<TencentNewsXmlParser.NewsItem>> {

    public AsyncResponse asyncResponse;

    public void setOnAsyncResponse(AsyncResponse asyncResponse)
    {
        this.asyncResponse = asyncResponse;
    }

    @Override
    protected ArrayList<TencentNewsXmlParser.NewsItem> doInBackground(String... urls) {
        try {
            return loadXmlFromNetwork(urls[0], urls[1]);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<TencentNewsXmlParser.NewsItem> items) {
        asyncResponse.onDataReceivedSuccess(items);
    }

    // Uploads XML, parses it, and combines it with
    // HTML markup. Returns HTML string.
    private ArrayList<TencentNewsXmlParser.NewsItem> loadXmlFromNetwork(String urlString, String type) throws XmlPullParserException, IOException {
        InputStream stream = null;
        // Instantiate the parser
        TencentNewsXmlParser tencentNewsXmlParser = new TencentNewsXmlParser(type);
        ArrayList<TencentNewsXmlParser.NewsItem> items = null;
        String title = null;
        String url = null;
        String summary = null;


        try {
            stream = downloadUrl(urlString);
            items = tencentNewsXmlParser.parse(stream);
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        return items;
    }

    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }
}
