package com.gali.apps.movielibrary;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int REQ_MANUAL_INTERNET_DIALOG = 1;
    SQLHelper sqlHelper;
    Cursor cursor;
    MovieCursorAdapter adapter;
    ShareActionProvider mShareActionProvider;

    int currentPosition=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.addBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,AddMovieDialogActivity.class);
                startActivityForResult(intent,REQ_MANUAL_INTERNET_DIALOG);
            }
        });

        ListView listView= (ListView) findViewById(R.id.moviesLV);
        sqlHelper = new SQLHelper(this);

        cursor= sqlHelper.getReadableDatabase().query(DBConstants.TABLE_NAME, null, null, null, null, null, null);

        String[] fromColums=new String[]{ DBConstants.COLUMN_SUBJECT};
        int[] toTV= new int[] {android.R.id.text1 };

        //adapter= new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor,fromColums, toTV);
        adapter= new MovieCursorAdapter(this, cursor);

        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //        Toast.makeText(MainActivity.this,"on item click "+position,Toast.LENGTH_SHORT);
                cursor.moveToPosition(position);

                //do edit
                doEdit(cursor);
            }

        });

        registerForContextMenu(listView);



    }

    private Intent createShareIntent(String imdbID) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");

        String urlIMDB = "http://www.imdb.com/title/"+imdbID;
        shareIntent.putExtra(Intent.EXTRA_TEXT, urlIMDB);
        return shareIntent;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //to get the positon on the list
        currentPosition= ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
        getMenuInflater().inflate(R.menu.single_movie_context_menu, menu);



    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {

        cursor.moveToPosition(currentPosition);
        switch (item.getItemId()) {
            case R.id.editMovieCMI:
                doEdit(cursor);
                break;
            case R.id.deleteMovieCMI:
                String subject = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_SUBJECT));
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setMessage(getResources().getString(R.string.areYouSureYouWantToDeleteThisMovie) + "\n" + subject);
                alertDialogBuilder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int dbId = cursor.getInt(cursor.getColumnIndex(DBConstants.COLUMN_ID));
                        sqlHelper.getWritableDatabase().delete(DBConstants.TABLE_NAME, DBConstants.COLUMN_ID + "=?", new String[]{"" + dbId});
                        cursor = sqlHelper.getReadableDatabase().query(DBConstants.TABLE_NAME, null, null, null, null, null, null);
                        adapter.swapCursor(cursor);
                    }
                }).setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                break;
            case R.id.shareMovieCMI:
                String imdbID = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_IMDBID));
                if (imdbID!=null) {
                    //Toast.makeText(this,"imdb id: "+imdbID,Toast.LENGTH_SHORT).show();
                    mShareActionProvider = (ShareActionProvider) item.getActionProvider();
                    mShareActionProvider.setShareIntent(createShareIntent(imdbID));

                } else {
                    Toast.makeText(this,R.string.imdbIdNotFoundMessage,Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return  true;
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode  == RESULT_CANCELED){
            return;
        }

        else {
            if(requestCode == REQ_MANUAL_INTERNET_DIALOG) {
                boolean manual = data.getBooleanExtra("manual",false);
                if (manual) {
                    Intent intent = new Intent(MainActivity.this,AddEditActivity.class);
                    startActivity(intent);

                } else {
                    Intent intent = new Intent(MainActivity.this,InternetMovieSearchActivity.class);
                    startActivity(intent);
                }

            }


        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cursor = sqlHelper.getReadableDatabase().query(DBConstants.TABLE_NAME, null, null, null, null, null, null);
        adapter.swapCursor(cursor);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()== R.id.deleteAllOMI) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setTitle(getResources().getString(R.string.areYouSureYouWantToDeleteAllItems));
            alertDialogBuilder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sqlHelper.getWritableDatabase().delete(DBConstants.TABLE_NAME,null,null);

                    cursor = sqlHelper.getReadableDatabase().query(DBConstants.TABLE_NAME, null, null, null, null, null, null);
                    adapter.swapCursor(cursor);
                }
            }).setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        } else {//exit
            finish();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    private void doEdit(Cursor cursor) {
        //do edit
        String subject= cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_SUBJECT));
        String body= cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_BODY));
        String url= cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_URL));
        int watched = cursor.getInt(cursor.getColumnIndex(DBConstants.COLUMN_WATCHED));
        float rate= cursor.getFloat(cursor.getColumnIndex(DBConstants.COLUMN_RATE));
        String imdbID = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_IMDBID));

        int dbId= cursor.getInt(cursor.getColumnIndex(DBConstants.COLUMN_ID));

        Cursor cursor2 = sqlHelper.getReadableDatabase().query(DBConstants.TABLE_NAME, new String[]{DBConstants.COLUMN_WATCHED}, DBConstants.COLUMN_ID + "=?", new String[]{"" + dbId}, null, null, null);
        int watchedFromDB = -1;
        while (cursor2.moveToNext()) {
            watchedFromDB = cursor2.getInt(cursor2.getColumnIndex(DBConstants.COLUMN_WATCHED));
        }

        Intent intent= new Intent(MainActivity.this, AddEditActivity.class);
        intent.putExtra("subject", subject);
        intent.putExtra("body", body);
        intent.putExtra("url", url);
        intent.putExtra("watched", watchedFromDB);
        intent.putExtra("rate", rate);
        intent.putExtra("imdbID", imdbID);

        intent.putExtra("id", dbId);



        startActivity(intent);

    }
}
