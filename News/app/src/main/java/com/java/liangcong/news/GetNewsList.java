package com.java.liangcong.news;

import android.content.Context;
import android.widget.Toast;

import com.java.liangcong.web.AsyncResponse;
import com.java.liangcong.web.NetworkActivity;
import com.java.liangcong.web.TencentNewsXmlParser;

import java.util.ArrayList;

public class GetNewsList {
    String URL = null;
    ArrayList<TencentNewsXmlParser.NewsItem> displayItems = new ArrayList<>();

    public GetNewsList(String URL){
        this.URL = URL;
    }

    public ArrayList<TencentNewsXmlParser.NewsItem> getNews(String type, final Context context){

        NetworkActivity networkActivity = new NetworkActivity(URL, type);//"http://192.168.1.8:8099/rss_newsgn.xml"
        networkActivity.loadPage(new AsyncResponse(){
            @Override
            public void onDataReceivedSuccess(ArrayList<TencentNewsXmlParser.NewsItem> items) {
                //displayItems = items;
                displayItems.clear();

                if(items != null && items.size()!=0) {
                    displayItems.addAll(items);
                }else onDataReceivedFailed();
            }

            @Override
            public void onDataReceivedFailed() {
                //Error
                //displayItems.add(new TencentNewsXmlParser.NewsItem("空","空","空","xxxx-xx-xx xx:xx:xx", "无"));
                Toast.makeText(context, "无法与服务器通讯，仅可阅览已读新闻和收藏新闻，请检查网络连接",
                        Toast.LENGTH_SHORT).show();
            }
        });

        return displayItems;
    }
}
