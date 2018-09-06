package com.liangcong.news;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.liangcong.adapter.TabAdapter;
import com.liangcong.recyclerview.RecyclerViewFragment;
import com.liangcong.web.TencentNewsXmlParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static TabAdapter adapter;
    private TabLayout tabLayout;
    public static ViewPager viewPager;



    private ProgressDialog progressDialog;

    private Map<String,ArrayList<TencentNewsXmlParser.NewsItem>> newsItems = new HashMap<String,ArrayList<TencentNewsXmlParser.NewsItem>>();
    private Map<String, String> channelURLs = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        adapter = new TabAdapter(getSupportFragmentManager());

        channelURLs.put("国内", "http://news.qq.com/newsgn/rss_newsgn.xml");

        channelURLs.put("国际", "http://news.qq.com/newsgj/rss_newswj.xml");

        channelURLs.put("社会", "http://news.qq.com/newssh/rss_newssh.xml");

        newsItems.put("国内", new GetNewsList(channelURLs.get("国内")).getNews());

        newsItems.put("国际", new GetNewsList(channelURLs.get("国际")).getNews());

        newsItems.put("社会", new GetNewsList(channelURLs.get("社会")).getNews());

        adapter.addFragment( RecyclerViewFragment.newInstance(newsItems.get("国内")), "国内");
        adapter.addFragment( RecyclerViewFragment.newInstance(newsItems.get("国际")), "国际");
        adapter.addFragment( RecyclerViewFragment.newInstance(newsItems.get("社会")), "社会");


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
