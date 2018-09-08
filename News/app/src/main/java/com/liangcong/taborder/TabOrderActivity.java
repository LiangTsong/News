package com.liangcong.taborder;

import android.content.Context;
import android.content.Intent;
import android.drm.DrmStore;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.liangcong.adapter.TabOrderAdapter;
import com.liangcong.addtab.AddTabActivity;
import com.liangcong.news.MainActivity;
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
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class TabOrderActivity extends AppCompatActivity {

    private ListView listView;

    private Context context;
    private TabOrderAdapter tabAdapter;
    private ArrayList<String> Tabs;
    private ArrayList<String> oldTabs;

    public SparseBooleanArray checkedItemPositions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_order);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("标签管理");

        listView = (ListView)findViewById(R.id.listView);

        Tabs = jsonStringToTabs(readFromPhone(MainActivity.TABS_FILE_NAME));
        //Log.d("NEWS", "onCreate: 获取的第一个Tag是"+ Tabs.get(0));
        oldTabs = Tabs;

        context = this;
        tabAdapter = new TabOrderAdapter(Tabs, context);
        listView.setAdapter(tabAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                checkedItemPositions = listView.getCheckedItemPositions();
                Log.d("ORDER", "onItemClick: "+position+checkedItemPositions.get(position));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_order, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.add_tab: {
                Intent intent = new Intent();
                intent.setClass(this, AddTabActivity.class);
                startActivityForResult(intent, 20);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 20){
            //更新
            Tabs = jsonStringToTabs(readFromPhone(MainActivity.TABS_FILE_NAME));
            oldTabs = Tabs;
            context = this;
            tabAdapter = new TabOrderAdapter(Tabs, context);
            listView.setAdapter(tabAdapter);
        }
    }

        public String TabsToJsonString(ArrayList<String> tabs) {

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

    public void deleteTabs(View view){
        //先全变成未选择
        listView.setSelected(false);

        for(int i = 0; i < Tabs.size(); i++){
            if(checkedItemPositions.get(i)==true){
                //删除
                Tabs.remove(oldTabs.get(i));
            }
        }
        //写入
        saveToPhone(MainActivity.TABS_FILE_NAME,TabsToJsonString(Tabs));
        //显示最新
        tabAdapter.notifyDataSetChanged();
        Intent intent=new Intent();
        setResult(10,intent);
        finish();
    }
}
