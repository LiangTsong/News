package com.java.liangcong.web;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class TencentNewsXmlParser {
    // We don't use namespaces
    private static final String ns = null;
    private String type;

    public TencentNewsXmlParser(String type){
        this.type = type;
    }

    public static class NewsItem{
        public String title;
        public String link;
        public String pubdate;
        public String description;
        public String type;

        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }
        public String getLink() {
            return link;
        }
        public void setLink(String link) {
            this.link = link;
        }
        public String getDate() {
            return pubdate;
        }
        public void setDate(String pubdate) {
            this.pubdate = pubdate;
        }
        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }

        public NewsItem(String title, String description, String link, String pubdate, String type) {
            this.title = title;
            this.description = description;
            this.link = link;
            this.pubdate = pubdate;
            this.type = type;
        }
        public NewsItem(){
        }
    }

    public ArrayList<NewsItem> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private ArrayList readFeed(XmlPullParser parser) throws IOException, XmlPullParserException {

        ArrayList items = new ArrayList();
        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, ns, "channel");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the item tag
            if (name.equals("item")) {
                items.add(readItem(parser));
            } else {
                skip(parser);
            }
        }
        return items;
    }

    private NewsItem readItem(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "item");
        String title = null;
        String description = null;
        String link = null;
        String pubDate = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("title")) {
                title = readTitle(parser);
            } else if (name.equals("description")) {
                description = readDescription(parser);
            } else if (name.equals("link")) {
                link = readLink(parser);
            } else if (name.equals("pubDate")) {
                    pubDate = readDate(parser);
            } else {
                skip(parser);
            }
        }
        return new NewsItem(title, description, link, pubDate, type);
    }

    // Processes title tags in the feed.
    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "title");
        return title;
    }

    // Processes link tags in the feed.
    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "link");
        String link = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "link");
        return link;
    }

    private String readDate(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "pubDate");
        String link = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "pubDate");
        return link;
    }

    // Processes description tags in the feed.
    private String readDescription(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "description");
        String description = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "description");
        if (description.length() > 2 && (description.charAt(0) == 12288 || description.charAt(0) == ' ')) description = description.substring(2);
        return description;
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    // Skip
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

}