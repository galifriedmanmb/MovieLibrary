package com.gali.apps.movielibrary;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import static android.R.attr.id;

/**
 * Created by 1 on 2/15/2017.
 */

public class MovieCursorAdapter extends CursorAdapter {
    SQLHelper sqlHelper;
    Context context;
    //static Cursor cursor;

    public MovieCursorAdapter(Context context, Cursor c) {
        super(context, c);
        this.context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v= LayoutInflater.from(context).inflate(R.layout.single_movie_item , null);
        return v;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        final int dbId = cursor.getInt(cursor.getColumnIndex(DBConstants.COLUMN_ID));

        //this.cursor = cursor;
        final String subject = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_SUBJECT));
        TextView subjectTV  = (TextView) view.findViewById(R.id.subjectLTV);
        subjectTV.setText(subject);

        //final String body = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_BODY));
        //final String imageUrl = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_URL));

/*
        subjectTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this,"on item click "+position,Toast.LENGTH_SHORT);
                //cursor.moveToPosition(position);

                //do edit
                doEdit(dbId,subject,body,imageUrl);
            }
        });
*/


        ImageView watchedIV = (ImageView) view.findViewById(R.id.watchedIV );
        watchedIV.setTag(R.id.dbId,dbId);
        //watchedIV.setTag(R.id.watched,-1);
        int watched = cursor.getInt(cursor.getColumnIndex(DBConstants.COLUMN_WATCHED));
        if(watched==1) {
            watchedIV.setImageResource(R.drawable.eye);
            watchedIV.setTag(R.id.watched,1);
        } else {
            watchedIV.setImageResource(R.drawable.closedeye);
            watchedIV.setTag(R.id.watched,-1);
        }

        float rate = cursor.getFloat(cursor.getColumnIndex(DBConstants.COLUMN_RATE));
        if (rate>5)
            view.setBackgroundColor(Color.GREEN);
        else
            view.setBackgroundColor(Color.RED);
        TextView rateTV = (TextView)view.findViewById(R.id.rateTV);
        rateTV.setText(""+rate);

        String imgStr = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_IMG));
        if (imgStr!=null) {
            try {
                byte[] encodeByte = Base64.decode(imgStr, Base64.DEFAULT);
                Bitmap imgBmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                ((ImageView) view.findViewById(R.id.posterLIV)).setImageBitmap(imgBmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        watchedIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int watched = Integer.parseInt(v.getTag(R.id.watched).toString());
                int watchedValueToSet = -watched;
                int watchedImgResIdToSet = R.drawable.closedeye;
                if (watchedValueToSet==1) {
                    watchedImgResIdToSet = R.drawable.eye;
                }

                sqlHelper = new SQLHelper(context);

                ContentValues contentValues= new ContentValues();
                contentValues.put(DBConstants.COLUMN_WATCHED, watchedValueToSet);

                int dbId = Integer.parseInt(v.getTag(R.id.dbId).toString());
                sqlHelper.getWritableDatabase().update(DBConstants.TABLE_NAME, contentValues, DBConstants.COLUMN_ID+"=?" , new String[] { ""+dbId });

                ((ImageView)v).setImageResource(watchedImgResIdToSet);
                v.setTag(R.id.watched,watchedValueToSet);

            }
        });

    }

}
