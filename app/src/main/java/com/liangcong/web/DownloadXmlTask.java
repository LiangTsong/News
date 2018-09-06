package com.liangcong.web;


import android.os.AsyncTask;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

// Implementation of AsyncTask used to download XML feed
class DownloadXmlTask extends AsyncTask<String, Void, List<TencentNewsXmlParser.NewsItem>> {

    public AsyncResponse asyncResponse;

    public void setOnAsyncResponse(AsyncResponse asyncResponse)
    {
        this.asyncResponse = asyncResponse;
    }

    @Override
    protected List<TencentNewsXmlParser.NewsItem> doInBackground(String... urls) {
        try {
            return loadXmlFromNetwork(urls[0]);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<TencentNewsXmlParser.NewsItem> items) {
        asyncResponse.onDataReceivedSuccess(items);
    }

    // Uploads XML, parses it, and combines it with
    // HTML markup. Returns HTML string.
    private List<TencentNewsXmlParser.NewsItem> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        // Instantiate the parser
        TencentNewsXmlParser tencentNewsXmlParser = new TencentNewsXmlParser();
        List<TencentNewsXmlParser.NewsItem> items = null;
        String title = null;
        String url = null;
        String summary = null;
        /*Calendar rightNow = Calendar.getInstance();
        DateFormat formatter = new SimpleDateFormat("MMM dd h:mmaa");*/

        // Checks whether the user set the preference to include summary text
        /*SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean pref = sharedPrefs.getBoolean("summaryPref", false);

        StringBuilder htmlString = new StringBuilder();
        htmlString.append("<h3>" + getResources().getString(R.string.page_title) + "</h3>");
        htmlString.append("<em>" + getResources().getString(R.string.updated) + " " +
                formatter.format(rightNow.getTime()) + "</em>");*/

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

        // tencentNewsXmlParser returns a List (called "newsItems") of NewsItem objects.
        // Each NewsItem object represents a single post in the XML feed.
        // This section processes the items list to combine each item with HTML markup.
        // Each entry is displayed in the UI as a link that optionally includes
        // a text summary.
        /*for (TencentNewsXmlParser.NewsItem item : items) {
            htmlString.append("<p><a href='");
            htmlString.append(item.link);
            htmlString.append("'>" + item.title + "</a></p>");
            // If the user set the preference to include summary text,
            // adds it to the display.
            if (pref) {
                htmlString.append(item.summary);
            }
        }
        return htmlString.toString();*/
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
