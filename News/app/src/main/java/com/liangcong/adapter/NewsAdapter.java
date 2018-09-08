package com.liangcong.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
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

import database.NewsDbSchema.NewsDbSchema;

import static com.liangcong.news.MainActivity.getContentValues;
import static database.NewsDbSchema.NewsDbSchema.Newstable.Cols.HTML;

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
            newsTitleTextView.setText(Html.fromHtml(newsItem.title));
            newsDescripTextView.setText(Html.fromHtml(newsItem.description));
            newsDateTextView.setText(Html.fromHtml(newsItem.pubdate));
            newsTypeTextView.setText(newsItem.type);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(itemView.getContext(), DisplayNewsActivity.class);
            String url = newsItem.link;
            intent.putExtra("NEWS_URL",url);
            context.startActivity(intent);
            //同时，该页面被标记为看过
            newsItem.setRead(1);
            newsItem.setTitle("<font color=\"#c2c2c2\">" +newsItem.getTitle());
            newsItem.setDescription("<font color=\"#c2c2c2\">" +newsItem.getDescription());
            newsItem.setDate("<font color=\"#c2c2c2\">" +newsItem.getDate());
            updateNews(newsItem);
            notifyDataSetChanged();
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
        if(newsDataset.size() > position) holder.bind(newsDataset.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return newsDataset.size();
    }

    public void updateNews(TencentNewsXmlParser.NewsItem item){
        String url = item.getLink();
        ContentValues values = getContentValues(item);
        MainActivity.database.update(NewsDbSchema.Newstable.NAME, values, NewsDbSchema.Newstable.Cols.LINK +
        " = ? ", new String[] {item.getLink()});
    }
}