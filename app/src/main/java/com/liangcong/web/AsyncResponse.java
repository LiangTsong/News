package com.liangcong.web;


import java.util.List;

public interface AsyncResponse {
    public void onDataReceivedSuccess(List<TencentNewsXmlParser.NewsItem> listData);
    public void onDataReceivedFailed();
}