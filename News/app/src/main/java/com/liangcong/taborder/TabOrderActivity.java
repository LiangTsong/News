package com.liangcong.taborder;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.liangcong.adapter.TabOrderAdapter;
import com.liangcong.news.MainActivity;
import com.liangcong.news.R;

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

public class TabOrderActivity extends AppCompatActivity {

    private ListView listView;

    private Context context;
    private TabOrderAdapter tabAdapter;
    private ArrayList<String> Tabs;

    public SparseBooleanArray checkedItemPositions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_order);

        listView = (ListView)findViewById(R.id.listView);

        Tabs = jsonStringToTabs(readFromPhone(MainActivity.TABS_FILE_NAME));
        Log.d("ORDER", "onCreate: 已经得到当前tab，第一个"+Tabs.get(0));

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

    public String TabsToJsonString(ArrayList<String> tabs) throws JSONException {
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

    public ArrayList<String> jsonStringToTabs(String str) {
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
