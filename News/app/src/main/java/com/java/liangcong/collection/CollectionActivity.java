package com.java.liangcong.collection;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.java.liangcong.adapter.CollectedNewsAdapter;
import com.java.liangcong.news.DisplayNewsActivity;
import com.java.liangcong.news.MainActivity;
import com.java.liangcong.news.NewsCursorWrapper;
import com.java.liangcong.web.TencentNewsXmlParser;
import com.liangcong.news.R;

import java.util.ArrayList;

import com.java.liangcong.database.NewsDbSchema.NewsDbSchema;

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
        ab.setDisplayHomeAsUpEnabled(true);

        listView = (ListView)findViewById(R.id.collection_listView);
        collectedNews.clear();

        //获取收藏的新闻
        getCollectedItem();

        context = this;
        newsAdapter = new CollectedNewsAdapter(collectedNews, context);
        listView.setAdapter(newsAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //TencentNewsXmlParser.NewsItem item = (TencentNewsXmlParser.NewsItem)listView.getItemAtPosition(i);
                TencentNewsXmlParser.NewsItem item = collectedNews.get(i);
                String url = item.link;
                Intent intent = new Intent(context, DisplayNewsActivity.class);
                intent.putExtra("NEWS_URL",url);
                startActivityForResult(intent, 30);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==30){
            //获取收藏的新闻
            collectedNews.clear();
            getCollectedItem();
            newsAdapter = new CollectedNewsAdapter(collectedNews, context);
            listView.setAdapter(newsAdapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void getCollectedItem (){

        NewsCursorWrapper cursor = queryNews(null,null);
        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                TencentNewsXmlParser.NewsItem item = cursor.getNewsItem();
                collectedNews.add(item);
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }
    }

    private static NewsCursorWrapper queryNews(String whereClause, String[] whereArgs){
        Cursor cursor = MainActivity.database.query(
                NewsDbSchema.Newstable.NAME1,
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
