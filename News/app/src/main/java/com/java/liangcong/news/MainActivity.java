package com.java.liangcong.news;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.java.liangcong.adapter.TabAdapter;
import com.java.liangcong.recyclerview.RecyclerViewFragment;
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

import database.NewsDbSchema.NewsBaseHelper;
import database.NewsDbSchema.NewsDbSchema;

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
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("NEWS", "onActivityResult: 已经返回");
        if (requestCode==10){
            Log.d("NEWS", "onActivityResult: if执行");
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
        Log.d("NEWS", "TabsToJsonString: 转为字符串："+stringer.toString());
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

            Log.d("NEWS", "jsonStringToTabs: 已经存到手机");

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
}
