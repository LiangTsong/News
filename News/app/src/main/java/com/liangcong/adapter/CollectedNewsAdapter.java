package com.liangcong.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.liangcong.news.DisplayNewsActivity;
import com.liangcong.news.R;
import com.liangcong.taborder.CheckableLayout;
import com.liangcong.web.TencentNewsXmlParser;

import java.util.ArrayList;

public class CollectedNewsAdapter extends BaseAdapter {
    private ArrayList<TencentNewsXmlParser.NewsItem> newsItems;
    private Context context;

    public CollectedNewsAdapter(ArrayList<TencentNewsXmlParser.NewsItem> newsItems, Context context){
        this.newsItems = newsItems;
        this.context = context;
    }

    @Override
    public int getCount() {
        return newsItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = LayoutInflater.from(context).inflate(R.layout.news_textview,parent,false);

        TextView title = (TextView) convertView.findViewById(R.id.news_title);
        TextView type = (TextView) convertView.findViewById(R.id.type);
        TextView pubdate = (TextView) convertView.findViewById(R.id.date);
        TextView description = (TextView) convertView.findViewById(R.id.news_content);

        title.setText(Html.fromHtml(newsItems.get(position).getTitle()));
        type.setText(newsItems.get(position).getType());
        pubdate.setText(Html.fromHtml(newsItems.get(position).getDate()));
        description.setText(Html.fromHtml(newsItems.get(position).getDescription()));

        return convertView;
    }

}