package com.liangcong.news;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import com.liangcong.adapter.TabOrderAdapter;
import com.liangcong.recyclerview.RecyclerViewFragment;
import com.liangcong.taborder.TabOrderActivity;
import com.liangcong.web.TencentNewsXmlParser;

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
import java.util.UUID;

import database.NewsDbSchema.NewsBaseHelper;
import database.NewsDbSchema.NewsDbSchema;

public class MainActivity extends AppCompatActivity {

    public static TabAdapter adapter;
    private TabLayout tabLayout;
    public static ViewPager viewPager;

    private ProgressDialog progressDialog;

    private Map<String, String> channelURLs = new HashMap<>();

    private static ArrayList<String> Tabs = new ArrayList<>();

    final static public String TABS_FILE_NAME = "TABS.json";

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
        values.put(NewsDbSchema.Newstable.Cols.READ, item.getRead());
        values.put(NewsDbSchema.Newstable.Cols.SAVED, item.getSaved());
        values.put(NewsDbSchema.Newstable.Cols.HTML, item.getHtml());
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
            case R.id.action_collection:
                //未完待续...当点按收藏按钮时...
                return true;
            case R.id.action_tab:
            {
                //进入标签控制页面
                Intent intent = new Intent(context, TabOrderActivity.class);
                Log.d("ORDER", "onOptionsItemSelected: 即将进入标签控制");
                context.startActivity(intent);
            }

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

    public static String TabsToJsonString(ArrayList<String> tabs) throws JSONException {
        JSONStringer stringer = new JSONStringer();
        stringer.object();
        stringer.key("Tabs");
        stringer.array();
        for(int i = 0; i < Tabs.size(); i++){
            stringer.object();
            stringer.key("tab:name").value(Tabs.get(i));
            stringer.endObject();
        }
        stringer.endArray();
        stringer.endObject();
        return stringer.toString();
    }

    public static ArrayList<String> jsonStringToTabs(String str) {
        if(str.equals("")) {
            //返回默认Tabs
            ArrayList<String> default_Tabs = new ArrayList<>();
            default_Tabs.add("国内");
            default_Tabs.add("国际");
            default_Tabs.add("社会");
            default_Tabs.add("电影");
            default_Tabs.add("军事");

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

    public void saveToPhone(String filename, String content) throws IOException {
        FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
        fos.write(content.getBytes());
        fos.close();
    }

    public String readFromPhone(String filename) {
        StringBuilder sb = new StringBuilder();
        FileInputStream fis = null;
        try {
            fis = openFileInput(filename);
            int tempbyte;
            while ((tempbyte = fis.read()) != -1) {
                sb.append((char) tempbyte);
            }
            fis.close();
        } catch (FileNotFoundException e) {
            return "";//如果是空，则使用默认Tab
        } catch (IOException e) {
            return "";
        }
        return sb.toString();
    }
}
