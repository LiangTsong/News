package com.liangcong.web;


import java.util.ArrayList;

public interface AsyncResponse {
    public void onDataReceivedSuccess(ArrayList<TencentNewsXmlParser.NewsItem> listData);
    public void onDataReceivedFailed();
}