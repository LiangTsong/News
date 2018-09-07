package com.liangcong.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.liangcong.news.DisplayNewsActivity;
import com.liangcong.news.MainActivity;
import com.liangcong.news.R;
import com.liangcong.recyclerview.RecyclerViewFragment;
import com.liangcong.web.TencentNewsXmlParser;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder>{

    private List<TencentNewsXmlParser.NewsItem> newsDataset;
    private Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class NewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // each data item is just a string in this case
        public TextView newsTitleTextView;
        public TextView newsDescripTextView;
        public TextView newsDateTextView;
        public TextView newsTypeTextView;

        private TencentNewsXmlParser.NewsItem newsItem;


        public NewsViewHolder(View v) {
            super(v);

            itemView.setOnClickListener(this);

            newsTitleTextView = (TextView) v.findViewById(R.id.news_title);
            newsDescripTextView = (TextView) v.findViewById(R.id.news_content);
            newsDateTextView = (TextView) v.findViewById(R.id.date);
            newsTypeTextView = (TextView) v.findViewById(R.id.type);
        }

        public void bind(TencentNewsXmlParser.NewsItem item){
            newsItem = item;
            newsTitleTextView.setText(newsItem.title);
            newsDescripTextView.setText(newsItem.description);
            newsDateTextView.setText(newsItem.pubdate);
            newsTypeTextView.setText(newsItem.type);
        }

        @Override
        public void onClick(View view) {
           Intent intent = new Intent(itemView.getContext(), DisplayNewsActivity.class);
           String url = newsItem.link;
           intent.putExtra("NEWS_URL",url);
           context.startActivity(intent);
        }
    }
    // Provide a suitable constructor (depends on the kind of dataset)
    public NewsAdapter(List<TencentNewsXmlParser.NewsItem> myDataset, Context context) {
        newsDataset = myDataset;
        this.context = context;
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

