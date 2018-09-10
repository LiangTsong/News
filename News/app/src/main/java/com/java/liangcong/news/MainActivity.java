package com.java.liangcong.news;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.java.liangcong.adapter.TabAdapter;
import com.java.liangcong.recyclerview.RecyclerViewFragment;
import com.java.liangcong.search.SearchActivity;
import com.java.liangcong.taborder.TabOrderActivity;
import com.java.liangcong.web.TencentNewsXmlParser;
import com.java.liangcong.collection.CollectionActivity;
import com.liangcong.news.R;

import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.java.liangcong.database.NewsDbSchema.NewsBaseHelper;
import com.java.liangcong.database.NewsDbSchema.NewsDbSchema;

public class MainActivity extends AppCompatActivity {

    public static TabAdapter adapter;
    private TabLayout tabLayout;
    public static ViewPager viewPager;

    private Map<String, String> channelURLs = new HashMap<>();

    private static ArrayList<String> Tabs = new ArrayList<>();

    final static public String TABS_FILE_NAME = "TABS.json";

    //数据库
    private Context context;
    public static SQLiteDatabase database;

    public static ContentValues getContentValues(TencentNewsXmlParser.NewsItem item){
        ContentValues values = new ContentValues();
        values.put(NewsDbSchema.Newstable.Cols.TITLE, item.title);
        values.put(NewsDbSchema.Newstable.Cols.TYPE, item.type);
        values.put(NewsDbSchema.Newstable.Cols.PUBDATE, item.pubdate);
        values.put(NewsDbSchema.Newstable.Cols.LINK, item.link);
        values.put(NewsDbSchema.Newstable.Cols.DESCRIPTION, item.description);
        return values;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("News");

        //数据库
        context = getApplicationContext();
        database = new NewsBaseHelper(context).getWritableDatabase();

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        adapter = new TabAdapter(getSupportFragmentManager());

        //读取标签设置
        Tabs = jsonStringToTabs(readFromPhone(TABS_FILE_NAME));
        for(String tab: Tabs){
            adapter.addFragment( RecyclerViewFragment.newInstance(tab), tab);
        }

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
            case R.id.action_collection: {
                //当点按收藏按钮时
                Intent intent = new Intent();
                intent.setClass(this, CollectionActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.action_tab:
            {
                //进入标签控制页面
                Intent intent = new Intent(context, TabOrderActivity.class);
                Log.d("ORDER", "onOptionsItemSelected: 即将进入标签控制");
                startActivityForResult(intent,10);
                return true;
            }
            case R.id.search_btn:{
                //进入搜索页面
                Intent intent = new Intent(context, SearchActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.help:{
                //帮助
                showHelpDialog();
                return true;
            }
            case R.id.about:{
                //关于
                showAboutDialog();
                return true;
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==10){
            update();
        }
    }

    public static String TabsToJsonString(ArrayList<String> tabs) {
        JSONStringer stringer = new JSONStringer();
        try {
            stringer.object();
            stringer.key("Tabs");
            stringer.array();
            for(int i = 0; i < tabs.size(); i++){
                stringer.object();
                stringer.key("tab:name").value(tabs.get(i));
                stringer.endObject();
            }
            stringer.endArray();
            stringer.endObject();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return stringer.toString();
    }

    public ArrayList<String> jsonStringToTabs(String str) {
        if(str.equals("NULL")) {
            //返回默认Tabs
            ArrayList<String> default_Tabs = new ArrayList<>();
            default_Tabs.add("国内新闻");
            default_Tabs.add("国际新闻");
            default_Tabs.add("社会新闻");
            default_Tabs.add("电影娱乐");
            default_Tabs.add("军事新闻");

            saveToPhone(TABS_FILE_NAME,TabsToJsonString(default_Tabs));


            return default_Tabs;
        }

        ArrayList<String> mTabs = new ArrayList<>();
        try {
            JSONTokener jsonTokener = new JSONTokener(str);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            JSONArray array = jsonObject.getJSONArray("Tabs");
            for (int i = 0; i < array.length(); i++) {
                JSONObject temp = null;
                temp = ((JSONObject) array.get(i));
                mTabs.add(temp.getString("tab:name"));
            }
        } catch(JSONException e){
            e.printStackTrace();
        }

        return mTabs;
    }

    public void saveToPhone(String filename, String content) {
        try {
            FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(content.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readFromPhone(String filename) {
        String res = "";
        try{
            FileInputStream fin = openFileInput(MainActivity.TABS_FILE_NAME);
            int length = fin.available();
            byte [] buffer = new byte[length];
            fin.read(buffer);
            res = EncodingUtils.getString(buffer, "UTF-8");
            fin.close();
        } catch (FileNotFoundException e) {
            return "NULL";//如果是空，则使用默认Tab
        } catch (IOException e) {
            return "NULL";//如果是空，则使用默认Tab
        }
        return res;
    }

    public void update(){
        adapter = new TabAdapter(getSupportFragmentManager());
        //读取标签设置
        Tabs = jsonStringToTabs(readFromPhone(TABS_FILE_NAME));
        for(String tab: Tabs){
            adapter.addFragment( RecyclerViewFragment.newInstance(tab), tab);
        }
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle("是否退出News？")
                .setIcon(R.drawable.ic_baseline_exit_to_app_24px)
                .setPositiveButton("退出", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“确认”后的操作
                        MainActivity.this.finish();

                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“返回”后的操作,这里不设置没有任何操作
                    }
                }).show();
        // super.onBackPressed();
    }

    private void showHelpDialog(){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MainActivity.this);
        normalDialog.setIcon(R.drawable.ic_baseline_mood_24px);
        normalDialog.setTitle("帮助");
        normalDialog.setMessage("1. 首页右上方依次为搜索、收藏库、分类按钮。\n" +
                "2. 点击新闻条目以阅读。\n3. 新闻阅读界面，右上方为收藏、分享按钮。\n4. 阅读过的新闻会变成灰色，同时内容会缓存，以供后续阅读。\n" +
                "5. 主页状态，下拉可更新新闻列表。");
        normalDialog.setPositiveButton("好",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });

        // 显示
        normalDialog.show();
    }

    private void showAboutDialog(){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MainActivity.this);
        normalDialog.setIcon(R.drawable.ic_baseline_copyright_24px);
        normalDialog.setTitle("关于");
        normalDialog.setMessage("梁聪\nTHU 计64\n2016013314\n2018夏季学期Java大作业");
        normalDialog.setPositiveButton("好",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });

        // 显示
        normalDialog.show();
    }
}
