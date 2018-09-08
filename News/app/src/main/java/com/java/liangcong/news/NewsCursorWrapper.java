package com.java.liangcong.news;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.java.liangcong.web.TencentNewsXmlParser;

import database.NewsDbSchema.NewsDbSchema;

public class NewsCursorWrapper extends CursorWrapper {
    public NewsCursorWrapper(Cursor cursor){
        super(cursor);
    }

    public TencentNewsXmlParser.NewsItem getNewsItem(){
        String title = getString(getColumnIndex(NewsDbSchema.Newstable.Cols.TITLE));
        String date = getString(getColumnIndex(NewsDbSchema.Newstable.Cols.PUBDATE));
        String type = getString(getColumnIndex(NewsDbSchema.Newstable.Cols.TYPE));
        String link = getString(getColumnIndex(NewsDbSchema.Newstable.Cols.LINK));
        String description = getString(getColumnIndex(NewsDbSchema.Newstable.Cols.DESCRIPTION));

        TencentNewsXmlParser.NewsItem item = new TencentNewsXmlParser.NewsItem();
        item.setTitle(title);
        item.setDate(date);
        item.setType(type);
        item.setLink(link);
        item.setDescription(description);

        return item;
    }
}
