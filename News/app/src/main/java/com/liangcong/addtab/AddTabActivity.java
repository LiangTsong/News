package com.liangcong.addtab;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.liangcong.adapter.TabOrderAdapter;
import com.liangcong.news.MainActivity;
import com.liangcong.news.R;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddTabActivity extends AppCompatActivity {

    public ArrayList<String> oldTabs;
    public ArrayList<String> allTabs = new ArrayList<>();

    private Context context;
    private ListView listView;
    private TabOrderAdapter tabAdapter;

    public SparseBooleanArray checkedItemPositions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tab);

        oldTabs = jsonStringToTabs(readFromPhone(MainActivity.TABS_FILE_NAME));

        ActionBar ab = getSupportActionBar();
        ab.setTitle("添加标签");

        getMap();

        context = this;
        listView = (ListView)findViewById(R.id.add_listView);
        tabAdapter = new TabOrderAdapter(allTabs, context); //数据源
        listView.setAdapter(tabAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                checkedItemPositions = listView.getCheckedItemPositions();
                Log.d("ORDER", "onItemClick: "+position+checkedItemPositions.get(position));
            }
        });
    }

    public void getMap()  {
        InputStream is = getResources().openRawResource(R.raw.urls);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            is.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }

        String jsonString = writer.toString();

        Gson gson = new Gson();
        Map<String, String> tab_urls = gson.fromJson(jsonString, new TypeToken<Map<String, String>>() {}.getType());
        for(String tab: tab_urls.keySet()){
            allTabs.add(tab);
        }
    }

    public ArrayList<String> jsonStringToTabs(String str) {
        if(str.equals("NULL")) {
            //返回空Tabs

            return new ArrayList<String>();
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

    public void addTabs(View view){

    }
}
