package com.liangcong.recyclerview;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.liangcong.adapter.NewsAdapter;
import com.liangcong.news.R;
import com.liangcong.web.TencentNewsXmlParser;

import java.util.ArrayList;

public class RecyclerViewFragment extends Fragment {

    private RecyclerView newsRecyclerView;
    private RecyclerView.Adapter newsAdapter;
    private RecyclerView.LayoutManager newsLayoutManager;

    private ProgressDialog progressDialog;

    public static Fragment newInstance(ArrayList<TencentNewsXmlParser.NewsItem> news) {
        RecyclerViewFragment fragment = new RecyclerViewFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("news",news);
        fragment.setArguments(bundle);
        return fragment;
    }

    private ArrayList<TencentNewsXmlParser.NewsItem> displayItems = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        LinearLayout newsRecyclerViewContainer =(LinearLayout) inflater.inflate(R.layout.recyclerview, container, false);

        newsRecyclerView = newsRecyclerViewContainer.findViewById(R.id.recyclerView);
        newsRecyclerView.addItemDecoration(new DividerItemDecoration(newsRecyclerView.getContext(),DividerItemDecoration.VERTICAL));

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        newsRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        newsLayoutManager = new LinearLayoutManager(newsRecyclerView.getContext());
        newsRecyclerView.setLayoutManager(newsLayoutManager);

        if(getArguments()!=null){
            displayItems = getArguments().getParcelableArrayList("news");
        }

        newsAdapter = new NewsAdapter(displayItems, newsRecyclerView.getContext());

        newsRecyclerView.setAdapter(newsAdapter);

        waitForNews();


        //return newsRecyclerView;
        return newsRecyclerViewContainer;
    }

    public void waitForNews(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(displayItems.size() == 0){

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgressDialog(newsRecyclerView.getContext(),"加载中......");
                        }
                    });
                    SystemClock.sleep(1000);
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        newsAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
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

