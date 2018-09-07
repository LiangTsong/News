package com.liangcong.news;

import com.liangcong.web.AsyncResponse;
import com.liangcong.web.NetworkActivity;
import com.liangcong.web.TencentNewsXmlParser;

import java.util.ArrayList;

public class GetNewsList {
    String URL = null;
    ArrayList<TencentNewsXmlParser.NewsItem> displayItems = new ArrayList<>();

    GetNewsList(String URL){
        this.URL = URL;
    }

    public ArrayList<TencentNewsXmlParser.NewsItem> getNews(String type){

        NetworkActivity networkActivity = new NetworkActivity(URL, type);//"http://192.168.1.8:8099/rss_newsgn.xml"
        networkActivity.loadPage(new AsyncResponse(){
            @Override
            public void onDataReceivedSuccess(ArrayList<TencentNewsXmlParser.NewsItem> items) {
                //displayItems = items;
                displayItems.clear();

                if(items != null) {
                    displayItems.addAll(items);
                }else onDataReceivedFailed();
            }

            @Override
            public void onDataReceivedFailed() {
                //Error
                displayItems.add(new TencentNewsXmlParser.NewsItem("空","空","空","xxxx-xx-xx xx:xx:xx", "无"));
            }
        });

        return displayItems;
    }
}
