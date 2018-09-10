package com.java.liangcong.database.NewsDbSchema;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NewsBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "newsBase.db";

    public NewsBaseHelper(Context context){
        super(context, DATABASE_NAME,null,VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("create table " + NewsDbSchema.Newstable.NAME + "(" +
        " news_id integer primary key autoincrement, " +
                NewsDbSchema.Newstable.Cols.TITLE + ", "+
                NewsDbSchema.Newstable.Cols.TYPE + ", " +
                NewsDbSchema.Newstable.Cols.PUBDATE + ", " +
                NewsDbSchema.Newstable.Cols.DESCRIPTION + ", " +
                NewsDbSchema.Newstable.Cols.LINK + " unique)");

        db.execSQL("create table " + NewsDbSchema.Newstable.NAME1 + "(" +
                " collection_id integer primary key autoincrement, " +
                NewsDbSchema.Newstable.Cols.TITLE + ", "+
                NewsDbSchema.Newstable.Cols.TYPE + ", " +
                NewsDbSchema.Newstable.Cols.PUBDATE + ", " +
                NewsDbSchema.Newstable.Cols.DESCRIPTION + ", " +
                NewsDbSchema.Newstable.Cols.LINK + " unique)");
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
