package com.liangcong.recyclerview;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.liangcong.news.NewsAdapter;
import com.liangcong.news.R;
import com.liangcong.web.AsyncResponse;
import com.liangcong.web.NetworkActivity;
import com.liangcong.web.TencentNewsXmlParser;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewFragment extends Fragment {

    private RecyclerView newsRecyclerView;
    private RecyclerView.Adapter newsAdapter;
    private RecyclerView.LayoutManager newsLayoutManager;

    private static String URL;

    public static Fragment newInstance(String url) {
        RecyclerViewFragment fragment = new RecyclerViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("URL", url);
        fragment.setArguments(bundle);
        return fragment;
    }

    private List<TencentNewsXmlParser.NewsItem> displayItems = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        LinearLayout newsRecyclerViewContainer =(LinearLayout) inflater.inflate(R.layout.recyclerview, container, false);

        newsRecyclerView = newsRecyclerViewContainer.findViewById(R.id.recyclerView);
        newsRecyclerView.addItemDecoration(new DividerItemDecoration(newsRecyclerView.getContext(),DividerItemDecoration.VERTICAL));

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        newsRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        newsLayoutManager = new LinearLayoutManager(newsRecyclerView.getContext());
        newsRecyclerView.setLayoutManager(newsLayoutManager);

        newsAdapter = new NewsAdapter(displayItems);
        newsRecyclerView.setAdapter(newsAdapter);

        getNews();

        //return newsRecyclerView;
        return newsRecyclerViewContainer;
    }

    private void getNews(){
        if(getArguments()!=null){
            //取出保存的值
            URL = getArguments().getString("URL");
        }
        NetworkActivity networkActivity = new NetworkActivity(URL);//"http://192.168.1.8:8099/rss_newsgn.xml"
        networkActivity.loadPage(new AsyncResponse(){
            @Override
            public void onDataReceivedSuccess(List<TencentNewsXmlParser.NewsItem> items) {
                //displayItems = items;
                displayItems.clear();
                displayItems.addAll(items);
                newsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onDataReceivedFailed() {
                //Error
                return;
            }
        });
    }

}
