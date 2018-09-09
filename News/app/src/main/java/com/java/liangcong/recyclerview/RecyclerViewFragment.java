package com.java.liangcong.recyclerview;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.java.liangcong.adapter.NewsAdapter;
import com.java.liangcong.web.TencentNewsXmlParser;
import com.java.liangcong.news.GetNewsList;
import com.java.liangcong.news.MainActivity;
import com.java.liangcong.news.NewsCursorWrapper;
import com.liangcong.news.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;

import database.NewsDbSchema.NewsDbSchema;

import static com.java.liangcong.news.MainActivity.getContentValues;

public class RecyclerViewFragment extends Fragment {

    private RecyclerView newsRecyclerView;
    private RecyclerView.Adapter newsAdapter;
    private RecyclerView.LayoutManager newsLayoutManager;

    private static Context context = null;

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
                        updateNews(type);
                    }
                }
        );

        context = this.getContext();

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
        boolean flag = false;


        NewsCursorWrapper cursor = queryNews(null,null);
        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                flag = true;
                TencentNewsXmlParser.NewsItem item = cursor.getNewsItem();
                if ( item.type.equals("全部") || item.type.equals(type)) {
                    news.add(item);
                }
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

        final ArrayList<TencentNewsXmlParser.NewsItem> newsList = new GetNewsList(getUrl(loc_type)).getNews(loc_type, context);

        new Thread(new Runnable() {
            @Override
            public void run() {
                int sleep_counter = 0;
                boolean over_time = false;

                while(newsList.size() <= 0){
                    SystemClock.sleep(100);
                    sleep_counter+=100;
                    if(getActivity()!=null && sleep_counter > 5000){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), loc_type +"加载超时，将仅展示过往新闻，请检查网络连接",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                        newsRecyclerViewContainer.setRefreshing(false);
                        over_time = true;
                        break;
                    }
                }
                for(TencentNewsXmlParser.NewsItem item: newsList){
                    ContentValues values = getContentValues(item);
                    MainActivity.database.insertWithOnConflict(NewsDbSchema.Newstable.NAME, null,
                            values, SQLiteDatabase.CONFLICT_IGNORE);
                }

                //更新
                final int old_size = displayNews.size();
                displayNews.clear();
                displayNews.addAll(0,getNews(loc_type));

                while(over_time == false && displayNews.size() == 0){
                    SystemClock.sleep(100);
                }
                final int new_size = displayNews.size();

                if(over_time == false && getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(new_size-old_size > 0)
                                Toast.makeText(getContext(), "更新了"+(new_size-old_size)+"条"+loc_type,
                                        Toast.LENGTH_SHORT).show();

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

    public void updateNews(String type){
        final String loc_type = type;

        final ArrayList<TencentNewsXmlParser.NewsItem> newsList = new GetNewsList(getUrl(loc_type)).getNews(loc_type, context);

        new Thread(new Runnable() {
            @Override
            public void run() {
                int sleep_counter = 0;
                boolean over_time = false;

                while(newsList.size() <= 0){
                    SystemClock.sleep(100);
                    sleep_counter+=100;
                    if(getActivity()!=null && sleep_counter > 5000){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), loc_type +"加载超时，将仅展示过往新闻，请检查网络连接",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                        newsRecyclerViewContainer.setRefreshing(false);
                        over_time = true;
                        break;
                    }
                }
                for(TencentNewsXmlParser.NewsItem item: newsList){
                    ContentValues values = getContentValues(item);
                    MainActivity.database.insertWithOnConflict(NewsDbSchema.Newstable.NAME, null,
                            values, SQLiteDatabase.CONFLICT_IGNORE);
                }

                //更新
                final int old_size = displayNews.size();
                displayNews.clear();
                displayNews.addAll(0,getNews(loc_type));

                while(over_time == false && displayNews.size() == 0){
                    SystemClock.sleep(100);
                }
                final int new_size = displayNews.size();

                if(over_time == false && getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(new_size-old_size > 0)
                                Toast.makeText(getContext(), "更新了"+(new_size-old_size)+"条"+loc_type,
                                        Toast.LENGTH_SHORT).show();
                            else Toast.makeText(getContext(), loc_type +"暂无更新",
                                    Toast.LENGTH_SHORT).show();
                            newsRecyclerViewContainer.setRefreshing(false);
                            //更新
                            newsAdapter.notifyDataSetChanged();
                        }
                    });
                }

            }
        }).start();
    }
}
