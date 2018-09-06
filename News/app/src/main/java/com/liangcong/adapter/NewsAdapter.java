package com.liangcong.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.liangcong.news.R;
import com.liangcong.web.TencentNewsXmlParser;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder>{

    private List<TencentNewsXmlParser.NewsItem> newsDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView newsTitleTextView;
        public TextView newsDescripTextView;
        public NewsViewHolder(View v) {
            super(v);
            newsTitleTextView = (TextView) v.findViewById(R.id.news_title);
            newsDescripTextView = (TextView) v.findViewById(R.id.news_content);
        }

        public void bind(TencentNewsXmlParser.NewsItem newsItem){
            newsTitleTextView.setText(newsItem.title);
            newsDescripTextView.setText(newsItem.description);
        }
    }
    // Provide a suitable constructor (depends on the kind of dataset)
    public NewsAdapter(List<TencentNewsXmlParser.NewsItem> myDataset) {
        newsDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public NewsAdapter.NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_textview, parent, false);
        // set the view's size, margins, paddings and layout parameters
        // ...
        NewsViewHolder vh = new NewsViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(NewsViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.bind(newsDataset.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return newsDataset.size();
    }
}
