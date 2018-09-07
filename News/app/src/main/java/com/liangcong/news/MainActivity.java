package com.liangcong.news;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.liangcong.adapter.TabAdapter;
import com.liangcong.recyclerview.RecyclerViewFragment;
import com.liangcong.web.TencentNewsXmlParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import database.NewsDbSchema.NewsBaseHelper;
import database.NewsDbSchema.NewsDbSchema;

public class MainActivity extends AppCompatActivity {

    public static TabAdapter adapter;
    private TabLayout tabLayout;
    public static ViewPager viewPager;

    private ProgressDialog progressDialog;

    private Map<String, String> channelURLs = new HashMap<>();

    //数据库
    private Context context;
    public static SQLiteDatabase database;

    public static ContentValues getContentValues(TencentNewsXmlParser.NewsItem item){
        ContentValues values = new ContentValues();
        values.put(NewsDbSchema.Newstable.Cols.TITLE, item.getTitle());
        values.put(NewsDbSchema.Newstable.Cols.TYPE, item.getType());
        values.put(NewsDbSchema.Newstable.Cols.PUBDATE, item.getDate());
        values.put(NewsDbSchema.Newstable.Cols.LINK, item.getLink());
        values.put(NewsDbSchema.Newstable.Cols.DESCRIPTION, item.getDescription());
        return values;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //数据库
        context = getApplicationContext();
        database = new NewsBaseHelper(context).getWritableDatabase();

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        adapter = new TabAdapter(getSupportFragmentManager());

        adapter.addFragment( RecyclerViewFragment.newInstance("国内"), "国内");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_collection:
                //未完待续...当点按收藏按钮时...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
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
}
