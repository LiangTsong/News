package com.java.liangcong.search;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.java.liangcong.adapter.CollectedNewsAdapter;
import com.java.liangcong.news.MainActivity;
import com.java.liangcong.news.NewsCursorWrapper;
import com.java.liangcong.web.TencentNewsXmlParser;
import com.liangcong.news.R;

import java.util.ArrayList;

import database.NewsDbSchema.NewsDbSchema;

import static com.java.liangcong.collection.CollectionActivity.collectedNews;

public class SearchActivity extends AppCompatActivity {
    private EditText editText;
    private Button button;
    public static ArrayList<TencentNewsXmlParser.NewsItem> searchedNews = new ArrayList<>();
    private CollectedNewsAdapter newsAdapter;
    private ListView listView;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        editText = findViewById(R.id.editText);
        button = findViewById(R.id.button);
        listView = findViewById(R.id.search_listView);
        searchedNews.clear();

        button.setHint("请输入关键词");

        ActionBar ab = getSupportActionBar();
        ab.setTitle("搜索");
        ab.setDisplayHomeAsUpEnabled(true);

        context = this;
        newsAdapter = new CollectedNewsAdapter(searchedNews, context);
        listView.setAdapter(newsAdapter);

        editText.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    // 先隐藏键盘
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(getCurrentFocus()
                                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    //进行搜索操作的方法，在该方法中可以加入mEditSearchUser的非空判断
                    searchedNews.clear();
                    String str = editText.getText().toString();
                    //在数据库对str搜索，获取searchedNews，并刷新列表
                    getNewsItem(str);
                    newsAdapter.notifyDataSetChanged();
                }
                return false;
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                finish();
                return true;
            }
            case R.id.question:{
                showNormalDialog();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void search(View view){
        searchedNews.clear();
        String str = editText.getText().toString();
        //在数据库对str搜索，获取searchedNews，并刷新列表
        getNewsItem(str);
        newsAdapter.notifyDataSetChanged();
    }

    public static void getNewsItem (String keyword){

        NewsCursorWrapper cursor = queryNews(null,null);
        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                TencentNewsXmlParser.NewsItem item = cursor.getNewsItem();
                if(item.title.contains(keyword) || item.description.contains(keyword)
                        || item.type.contains(keyword) || item.pubdate.contains(keyword))
                    searchedNews.add(item);
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }
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

    private void showNormalDialog(){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(this);
        normalDialog.setIcon(R.drawable.ic_baseline_mood_24px);
        normalDialog.setTitle("搜索解疑");
        normalDialog.setMessage("请每次输入一个词语，以提升搜索体验。点击搜索按钮，下方将展示News安装以来加载的所有新闻项目中，包含了" +
                "您输入的关键词的项目。");
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

