package com.liangcong.news;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.liangcong.recyclerview.RecyclerViewFragment;
import com.liangcong.web.TencentNewsXmlParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private Map<String,List<TencentNewsXmlParser.NewsItem>> newsItems = new HashMap<String,List<TencentNewsXmlParser.NewsItem>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new Tab1Fragment(), "Tab 1");
        adapter.addFragment( RecyclerViewFragment.newInstance("http://192.168.1.8:8099/rss_newsgn.xml"), "Tab 2");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        /*Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);*/

    }

    /*@Override
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
    }*/


    /*private String transfer(List<TencentNewsXmlParser.NewsItem> items){
        StringBuilder htmlString = new StringBuilder();
        for(TencentNewsXmlParser.NewsItem item: displayItems){
            htmlString.append("<p> ");
            htmlString.append(item.title);
            htmlString.append(item.link);
            htmlString.append(item.description);
            htmlString.append("</p> ");
        }
        return htmlString.toString();
    }*/
}
