package com.gali.apps.movielibrary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by 1 on 2/12/2017.
 */

public class SQLHelper extends SQLiteOpenHelper {
    Context context;

    public SQLHelper(Context context ) {
        super(context, "movies.db", null, 1);
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        String SQLCreate="CREATE TABLE "+DBConstants.TABLE_NAME+" ("+DBConstants.COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+DBConstants.COLUMN_SUBJECT+" TEXT,  "+DBConstants.COLUMN_BODY+" TEXT,  "+DBConstants.COLUMN_URL+" TEXT, "+DBConstants.COLUMN_WATCHED+" INT,  "+DBConstants.COLUMN_RATE+" REAL, "+DBConstants.COLUMN_IMG+" TEXT, "+DBConstants.COLUMN_IMDBID+" TEXT  )";
        Log.d("sql", SQLCreate);

        db.execSQL(SQLCreate);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
