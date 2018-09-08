package com.liangcong.news;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.liangcong.web.TencentNewsXmlParser;

import java.util.ArrayList;

import database.NewsDbSchema.NewsDbSchema;

import static com.liangcong.news.MainActivity.getContentValues;

public class DisplayNewsActivity extends AppCompatActivity {
    private static final String APP_CACHE_DIRNAME = "/webcache";
    private String url;
    private String newsTitle;
    Menu readMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_news);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        url = intent.getStringExtra("NEWS_URL");

        WebView webView = (WebView) findViewById(R.id.web_view);

        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {

            //标题
            @Override
            public void onReceivedTitle(WebView view, String title) {
                newsTitle = title;
                ActionBar actionbar = getSupportActionBar();
                actionbar.setTitle(title);
            }
        });

        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        // 建议缓存策略为，判断是否有网络，有的话，使用LOAD_DEFAULT,无网络时，使用LOAD_CACHE_ELSE_NETWORK

        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        // 开启DOM storage API 功能
        webView.getSettings().setDomStorageEnabled(true);
        // 开启database storage API功能
        webView.getSettings().setDatabaseEnabled(true);
        String cacheDirPath = getFilesDir().getAbsolutePath()
                 + APP_CACHE_DIRNAME;
        //String cacheDirPath = getCacheDir()
        //        + APP_CACHE_DIRNAME;
        Log.i("CACHE", "cachePath=" + cacheDirPath);
        // 设置数据库缓存路径
        webView.getSettings().setDatabasePath(cacheDirPath);
        webView.getSettings().setAppCachePath(cacheDirPath);
        // 开启Application Cache功能
        webView.getSettings().setAppCacheEnabled(true);

        webView.loadUrl(url);

        //app内打开
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        readMenu = menu;
        if(getCollectedItem(url)!=null){//已收藏
            Log.d("NEWS", "onPrepareOptionsMenu: 已收藏");
            readMenu.findItem(R.id.add_news).setIcon(R.drawable.ic_baseline_star_24px);
        }else {
            readMenu.findItem(R.id.add_news).setIcon(R.drawable.ic_baseline_star_border_24px);
        }
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_read, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                //返回
                Intent intent=new Intent();
                setResult(30,intent);
                finish();
                return true;
            }
            case R.id.add_news: {
                //标记收藏
                if(getCollectedItem(url) == null) {
                    Toast.makeText(getApplicationContext(), "已收藏，请到首页收藏列表查看",
                            Toast.LENGTH_SHORT).show();
                    //先获取item
                    TencentNewsXmlParser.NewsItem newsItem = getItem(url);
                    //再写item到收藏db
                    ContentValues values = getContentValues(newsItem);
                    MainActivity.database.insertWithOnConflict(NewsDbSchema.Newstable.NAME1, null,
                            values, SQLiteDatabase.CONFLICT_IGNORE);
                    readMenu.findItem(R.id.add_news).setIcon(R.drawable.ic_baseline_star_24px);
                }else{
                    Toast.makeText(getApplicationContext(), "已移出收藏",
                            Toast.LENGTH_SHORT).show();
                    //删除
                    TencentNewsXmlParser.NewsItem newsItem = getCollectedItem(url);
                    ContentValues values = getContentValues(newsItem);
                    MainActivity.database.delete(NewsDbSchema.Newstable.NAME1, NewsDbSchema.Newstable.Cols.LINK +
                            " = ? ", new String[] {newsItem.link});
                    readMenu.findItem(R.id.add_news).setIcon(R.drawable.ic_baseline_star_border_24px);
                }
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateNews(TencentNewsXmlParser.NewsItem item){
        ContentValues values = getContentValues(item);
        MainActivity.database.update(NewsDbSchema.Newstable.NAME, values, NewsDbSchema.Newstable.Cols.LINK +
                " = ? ", new String[] {item.getLink()});
    }

    public static TencentNewsXmlParser.NewsItem getItem(String link){
        TencentNewsXmlParser.NewsItem item = new TencentNewsXmlParser.NewsItem();
        NewsCursorWrapper cursor = queryNews(null,null);
        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                item = cursor.getNewsItem();
                if ( item.link.equals(link)) return item;
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }
        return null;
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

    public static TencentNewsXmlParser.NewsItem getCollectedItem(String link){
        TencentNewsXmlParser.NewsItem item = null;
        NewsCursorWrapper cursor = queryCollectedNews(null,null);
        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                item = cursor.getNewsItem();
                if ( item.link.equals(link)) return item;
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }
        return null;
    }

    private static NewsCursorWrapper queryCollectedNews(String whereClause, String[] whereArgs){
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

