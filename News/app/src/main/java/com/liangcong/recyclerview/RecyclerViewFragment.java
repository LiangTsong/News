package com.liangcong.recyclerview;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.liangcong.adapter.NewsAdapter;
import com.liangcong.news.GetNewsList;
import com.liangcong.news.MainActivity;
import com.liangcong.news.NewsCursorWrapper;
import com.liangcong.news.R;
import com.liangcong.web.TencentNewsXmlParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import database.NewsDbSchema.NewsDbSchema;

import static com.liangcong.news.MainActivity.adapter;
import static com.liangcong.news.MainActivity.getContentValues;

public class RecyclerViewFragment extends Fragment {

    private RecyclerView newsRecyclerView;
    private RecyclerView.Adapter newsAdapter;
    private RecyclerView.LayoutManager newsLayoutManager;

    public SwipeRefreshLayout newsRecyclerViewContainer;

    public ArrayList<TencentNewsXmlParser.NewsItem> displayNews = new ArrayList<>();

    public static Fragment newInstance(String type) {
        RecyclerViewFragment fragment = new RecyclerViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type",type);
        fragment.setArguments(bundle);
        return fragment;
    }

    public String type;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        newsRecyclerViewContainer =(SwipeRefreshLayout) inflater.inflate(R.layout.recyclerview, container, false);

        //下拉刷新设置
        // 设置下拉出现小圆圈是否是缩放出现，出现的位置，最大的下拉位置
        newsRecyclerViewContainer.setProgressViewOffset(true, 50, 200);

        // 设置下拉圆圈的大小，两个值 LARGE， DEFAULT
        newsRecyclerViewContainer.setSize(SwipeRefreshLayout.LARGE);

        // 设置下拉圆圈上的颜色，蓝色、绿色、橙色、红色
        newsRecyclerViewContainer.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        newsRecyclerViewContainer.setProgressBackgroundColor(R.color.gray);
        /*
         * 设置手势下拉刷新的监听
         */
        newsRecyclerViewContainer.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        // 刷新动画开始后回调到此方法
                        loadNews(type);
                    }
                }
        );


        newsRecyclerView = newsRecyclerViewContainer.findViewById(R.id.recyclerView);
        newsRecyclerView.addItemDecoration(new DividerItemDecoration(newsRecyclerView.getContext(),DividerItemDecoration.VERTICAL));

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        newsRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        newsLayoutManager = new LinearLayoutManager(newsRecyclerView.getContext());
        newsRecyclerView.setLayoutManager(newsLayoutManager);

        if(getArguments()!=null){
            type = getArguments().getString("type");
        }

        newsRecyclerViewContainer.setRefreshing(true);
        loadNews(type);

        newsAdapter = new NewsAdapter(displayNews, newsRecyclerView.getContext());
        newsRecyclerView.setAdapter(newsAdapter);

        //return newsRecyclerView;
        return newsRecyclerViewContainer;
    }

    public static ArrayList<TencentNewsXmlParser.NewsItem> getNews(String type){
        ArrayList<TencentNewsXmlParser.NewsItem> news = new ArrayList<>();

        NewsCursorWrapper cursor = queryNews(null,null);
        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                TencentNewsXmlParser.NewsItem item = cursor.getNewsItem();
                if ( item.type.equals("全部") || item.type.equals(type)) news.add(item);
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }
        return news;
    }

    private static NewsCursorWrapper queryNews(String whereClause, String[] whereArgs){
        Cursor cursor = MainActivity.database.query(
                NewsDbSchema.Newstable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                NewsDbSchema.Newstable.Cols.PUBDATE+" DESC"
        );
        return new NewsCursorWrapper(cursor);
    }

    public void loadNews(String type){
        final String loc_type = type;

        final ArrayList<TencentNewsXmlParser.NewsItem> newsList = new GetNewsList(getUrl(loc_type)).getNews(loc_type);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(newsList.size() <= 0){
                    SystemClock.sleep(100);
                }
                for(TencentNewsXmlParser.NewsItem item: newsList){
                    ContentValues values = getContentValues(item);
                    MainActivity.database.insertWithOnConflict(NewsDbSchema.Newstable.NAME, null,
                            values, SQLiteDatabase.CONFLICT_IGNORE);
                }

                //更新
                displayNews.clear();
                displayNews.addAll(0,getNews(loc_type));

                while(displayNews.size() == 0){
                    SystemClock.sleep(100);
                }

                if(getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            newsRecyclerViewContainer.setRefreshing(false);
                            //更新
                            newsAdapter.notifyDataSetChanged();
                        }
                    });
                }

            }
        }).start();
    }

    public String getUrl(String type)  {
        InputStream is = getResources().openRawResource(R.raw.urls);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            is.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }

        String jsonString = writer.toString();

        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = jsonObj.optString(type);

        return url;
    }
}
