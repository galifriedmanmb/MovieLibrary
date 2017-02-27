package com.gali.apps.movielibrary;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class InternetMovieSearchActivity extends AppCompatActivity {

    ListView searchResultsLV;
    ArrayList<Movie> allMovies;
    ArrayAdapter<Movie> adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet_movie_search);

        searchResultsLV = (ListView)findViewById(R.id.searchResultsLV);
        allMovies= new ArrayList<>();
        adapter= new ArrayAdapter<Movie>(this, android.R.layout.simple_list_item_1,  allMovies);


        searchResultsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!InternetChecker.isConnected(InternetMovieSearchActivity.this)) {
                    Toast.makeText(InternetMovieSearchActivity.this, R.string.noInternetConnectionMsg, Toast.LENGTH_SHORT).show();
                    return;
                }
                Movie m = allMovies.get(position);

                ImdbApiDetails imdbApiDetails= new ImdbApiDetails();
                imdbApiDetails.execute("http://www.omdbapi.com/?i="+m.imdbID,m.imageUrl);
            }
        });

        searchResultsLV.setAdapter(adapter);


        Button goBtn= (Button)findViewById(R.id.goBtn);
        goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    if (!InternetChecker.isConnected(InternetMovieSearchActivity.this)) {
                        Toast.makeText(InternetMovieSearchActivity.this, R.string.noInternetConnectionMsg, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    EditText searchET = (EditText)findViewById(R.id.searchET);

                    ImdbApiList imdbApiList= new ImdbApiList();
                    imdbApiList.execute("http://www.omdbapi.com/?s="+searchET.getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        findViewById(R.id.cancelSearchBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public  class ImdbApiList extends AsyncTask<String, Long, String> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(InternetMovieSearchActivity.this);
            dialog.setTitle(getResources().getString(R.string.connecting));
            dialog.setMessage(getResources().getString(R.string.pleaseWait));
            dialog.setCancelable(true);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            BufferedReader input = null;
            HttpURLConnection connection = null;
            StringBuilder response = new StringBuilder();

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection)url.openConnection();
                input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                while ((line=input.readLine())!=null) {
                    response.append(line+"\n");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (input!=null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (connection!=null) {
                    connection.disconnect();
                }
            }
            return response.toString();
        }

        @Override
        protected void onPostExecute(String resultJSON) {
            dialog.dismiss();

            try {
                JSONObject mainObject= new JSONObject(resultJSON);
                JSONArray resultsArray= mainObject.getJSONArray("Search");
                allMovies.clear();

                for(int i=0; i<resultsArray.length(); i++ ) {
                    JSONObject currentObject= resultsArray.getJSONObject(i);
                    String title = currentObject.getString("Title");
                    String imdbID = currentObject.getString("imdbID");
                    String imgUrl = currentObject.getString("Poster");

                    Movie movie = new Movie(title, imdbID, imgUrl);
                    allMovies.add(movie);

                }

                adapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    public  class ImdbApiDetails extends AsyncTask<String, Long, String> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(InternetMovieSearchActivity.this);
            dialog.setTitle(getResources().getString(R.string.connecting));
            dialog.setMessage(getResources().getString(R.string.pleaseWait));
            dialog.setCancelable(true);
            dialog.show();
        }
        @Override
        protected String doInBackground(String... params) {
            StringBuilder response= null;

            try{
                URL website = new URL(params[0]);
                URLConnection connection = website.openConnection();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()));
                response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    response.append(inputLine);
                in.close();

            } catch(Exception ee) {
                ee.printStackTrace();
            }


            Log.d("imdb details",response.toString());
            return response.toString();
        }

        @Override
        protected void onPostExecute(String resultJSON) {
            dialog.dismiss();

            try {
                JSONObject mainObject= new JSONObject(resultJSON);

                    String subject = mainObject.getString("Title");
                    String imdbID = mainObject.getString("imdbID");
                    String body = mainObject.getString("Plot");
                    String imageUrl = mainObject.getString("Poster");
                    String rate = mainObject.getString("imdbRating");

                    Intent intent= new Intent(InternetMovieSearchActivity.this, AddEditActivity.class);
                    intent.putExtra("subject", subject);
                    intent.putExtra("body", body);
                    intent.putExtra("url", imageUrl);
                    intent.putExtra("rate", Float.parseFloat(rate));
                    intent.putExtra("imdbID", imdbID);

                    startActivity(intent);


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }


}
