package com.liangcong.collection;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.liangcong.adapter.CollectedNewsAdapter;
import com.liangcong.adapter.TabOrderAdapter;
import com.liangcong.news.MainActivity;
import com.liangcong.news.NewsCursorWrapper;
import com.liangcong.news.R;
import com.liangcong.web.TencentNewsXmlParser;

import java.util.ArrayList;

import database.NewsDbSchema.NewsDbSchema;

public class CollectionActivity extends AppCompatActivity {

    private ListView listView;
    private Context context;
    public static ArrayList<TencentNewsXmlParser.NewsItem> collectedNews = new ArrayList<>();
    private CollectedNewsAdapter newsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("我的收藏");

        listView = (ListView)findViewById(R.id.collection_listView);
        collectedNews.clear();

        //获取收藏的新闻
        getCollectedItem();

        context = this;
        newsAdapter = new CollectedNewsAdapter(collectedNews, context);
        listView.setAdapter(newsAdapter);
    }

    public static void getCollectedItem(){
        TencentNewsXmlParser.NewsItem item = new TencentNewsXmlParser.NewsItem();
        NewsCursorWrapper cursor = queryNews(null,null);
        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                item = cursor.getNewsItem();
                if ( item.getSaved() == 1) collectedNews.add(item);
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }
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
}
