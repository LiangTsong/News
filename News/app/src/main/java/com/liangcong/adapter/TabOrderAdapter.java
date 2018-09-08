package com.liangcong.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.liangcong.news.R;
import com.liangcong.taborder.CheckableLayout;

import java.util.ArrayList;

public class TabOrderAdapter extends BaseAdapter {
    private ArrayList<String> Tabs;
    private Context context;

    public TabOrderAdapter(ArrayList<String> Tabs, Context context){
        this.Tabs = Tabs;
        this.context = context;
    }

    @Override
    public int getCount() {
        return Tabs.size();
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

        convertView = LayoutInflater.from(context).inflate(R.layout.tab_order_item,parent,false);
        TextView tabName = (TextView) convertView.findViewById(R.id.tabName);
        tabName.setText(Tabs.get(position));
        return convertView;
    }
}
