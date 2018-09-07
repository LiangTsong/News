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
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.liangcong.adapter.NewsAdapter;
import com.liangcong.news.GetNewsList;
import com.liangcong.news.MainActivity;
import com.liangcong.news.NewsCursorWrapper;
import com.liangcong.news.R;
import com.liangcong.web.TencentNewsXmlParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import database.NewsDbSchema.NewsDbSchema;

import static com.liangcong.news.MainActivity.getContentValues;

public class RecyclerViewFragment extends Fragment {

    private RecyclerView newsRecyclerView;
    private RecyclerView.Adapter newsAdapter;
    private RecyclerView.LayoutManager newsLayoutManager;
    private ProgressDialog progressDialog;

    private Map<String, String> channelURLs = new HashMap<>();
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

        LinearLayout newsRecyclerViewContainer =(LinearLayout) inflater.inflate(R.layout.recyclerview, container, false);
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

        channelURLs.put("国内", "http://news.qq.com/newsgn/rss_newsgn.xml");//
        loadNews(type);

        newsAdapter = new NewsAdapter(displayNews, newsRecyclerView.getContext());
        newsRecyclerView.setAdapter(newsAdapter);

        //return newsRecyclerView;
        return newsRecyclerViewContainer;
    }

    public void showProgressDialog(Context mContext, String text) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        progressDialog.setMessage(text);	//设置内容
        progressDialog.setCancelable(false);//点击屏幕和按返回键都不能取消加载框
        progressDialog.show();

        //设置超时自动消失
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //取消加载框
                if(dismissProgressDialog()){
                    //超时处理
                }
            }
        }, 60000);//超时时间60秒
    }

    public Boolean dismissProgressDialog() {
        if (progressDialog != null){
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
                return true;//取消成功
            }
        }
        return false;//已经取消过了，不需要取消
    }

    public static ArrayList<TencentNewsXmlParser.NewsItem> getNews(String type){
        ArrayList<TencentNewsXmlParser.NewsItem> news = new ArrayList<>();

        NewsCursorWrapper cursor = queryNews(null,null);
        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                TencentNewsXmlParser.NewsItem item = cursor.getNewsItem();
                if (type != null || item.type.equals(type)) news.add(item);
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
                null
        );
        return new NewsCursorWrapper(cursor);
    }

    public void loadNews(String type){
        //newsItems.put(type, new GetNewsList(channelURLs.get(type)).getNews(type));
        final String loc_type = type;
        final ArrayList<TencentNewsXmlParser.NewsItem> newsList = new GetNewsList(channelURLs.get(type)).getNews(type);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(newsList.size() <= 0){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgressDialog(newsRecyclerView.getContext(),"加载中......");
                        }
                    });
                    SystemClock.sleep(100);
                }
                for(TencentNewsXmlParser.NewsItem item: newsList){
                    ContentValues values = getContentValues(item);
                    MainActivity.database.insertWithOnConflict(NewsDbSchema.Newstable.NAME, null,
                            values, SQLiteDatabase.CONFLICT_IGNORE);
                }

                //更新
                displayNews.addAll(getNews(loc_type));

                while(displayNews.size() == 0){
                    SystemClock.sleep(100);
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        //更新
                        newsAdapter.notifyDataSetChanged();
                    }
                });

            }
        }).start();
    }

}

