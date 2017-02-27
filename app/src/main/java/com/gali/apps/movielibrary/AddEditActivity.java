package com.gali.apps.movielibrary;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.gali.apps.movielibrary.R.id.subject;

public class AddEditActivity extends AppCompatActivity {

    boolean photoChanged = false;

    private final static String CAPTURED_PHOTO_URI_KEY = "mCapturedImageURI";
    static final int REQUEST_TAKE_PHOTO   = 1;
    private Uri cameraImageUri;

    int id = -1;
    SQLHelper sqlHelper;
    EditText subjectET;
    EditText bodyET;
    EditText urlET;
    //SeekBar seekBar;
    RatingBar ratingBar;
    ImageView watchedIV;
    int watched = -1;

    ImageView posterIV;
    Bitmap imgBmap = null;

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                //Uri uri = FileProvider.getUriForFile(this,"com.gali.apps.android.fileprovider",photoFile);
                cameraImageUri = FileProvider.getUriForFile(this,"com.gali.apps.android.fileprovider",photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        if (cameraImageUri!=null)
            outState.putString(CAPTURED_PHOTO_URI_KEY, cameraImageUri.toString());
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(CAPTURED_PHOTO_URI_KEY)) {
            cameraImageUri = Uri.parse(savedInstanceState.getString(CAPTURED_PHOTO_URI_KEY));    }
        super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            if (cameraImageUri!=null) {
                getContentResolver().notifyChange(cameraImageUri, null);

                try {
                    imgBmap = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), cameraImageUri);
                    posterIV.setImageBitmap(imgBmap);
                    photoChanged = true;
                } catch (Exception e) {
                    Toast.makeText(this, R.string.imgFailedToLoadMsg, Toast.LENGTH_SHORT).show();
                    //Log.e("Camera", e.toString());
                    e.printStackTrace();
                }

                urlET.setText(cameraImageUri.getPath());
            } else {
                Toast.makeText(this, R.string.imgFailedToCaptureMsg, Toast.LENGTH_SHORT).show();

            }
        }

    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File photo = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        Log.d("photo file: ",photo.getAbsolutePath());
        return photo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);
        sqlHelper = new SQLHelper(this);
        id = getIntent().getIntExtra("id",-1);

        subjectET = (EditText)findViewById(R.id.subjectET);
        bodyET = (EditText)findViewById(R.id.bodyET);
        urlET = (EditText)findViewById(R.id.urlET);
        posterIV = (ImageView)findViewById(R.id.urlImgIV);
        ratingBar = (RatingBar)findViewById(R.id.rateRB);
        watchedIV = (ImageView)findViewById(R.id.watchedIV);

        String subject= getIntent().getStringExtra("subject");
        String body= getIntent().getStringExtra("body");
        String url= getIntent().getStringExtra("url");
        float rate = getIntent().getFloatExtra("rate",0);
        watched = getIntent().getIntExtra("watched",-1);
        final String imdbid = getIntent().getStringExtra("imdbID");

        String title = getResources().getString(R.string.TitleAddNewMovie);
        if (subject != null) {
            title = getResources().getString(R.string.TitleEditDetailsFor)+subject;
        }

        setTitle(title);
        bodyET.setText(body);
        subjectET.setText(subject);
        urlET.setText(url);

        ratingBar.setRating(rate);


        if(watched==1) {
            watchedIV.setImageResource(R.drawable.eye);
        } else {
            watchedIV.setImageResource(R.drawable.closedeye);
        }


        if (id==-1) {
            String imageUrl = urlET.getText().toString();
            DownloadImageTask downloadImgeTask = new DownloadImageTask();
            downloadImgeTask.execute(imageUrl);
        } else {
            try {
                Cursor cursor = sqlHelper.getReadableDatabase().query(DBConstants.TABLE_NAME,
                        new String[]{DBConstants.COLUMN_IMG}, DBConstants.COLUMN_ID + "=?",
                        new String[]{"" + id}, null, null, null);


                String imgStr = null;
                while (cursor.moveToNext()) {
                    imgStr = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_IMG));
                }
                if (imgStr!=null) {
                    byte[] encodeByte = Base64.decode(imgStr, Base64.DEFAULT);
                    imgBmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                    posterIV.setImageBitmap(imgBmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        watchedIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (watched==1) {
                    watchedIV.setImageResource(R.drawable.closedeye);
                } else {
                    watchedIV.setImageResource(R.drawable.eye);
                }
                watched = -watched;
            }
        });

        findViewById(R.id.showURLBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imageUrl= urlET.getText().toString();

                //if (imageUrl.startsWith(Environment.getExternalStorageDirectory().getPath())) {
                //String localPath =    cameraImageUri.getPath();
                if (imageUrl.startsWith("/my_images/")) {
                    Toast.makeText(AddEditActivity.this,R.string.shootNewPhotoToReplaceCurrentMsg,Toast.LENGTH_SHORT).show();
                } else {
                    if (!InternetChecker.isConnected(AddEditActivity.this)) {
                        Toast.makeText(AddEditActivity.this, R.string.noInternetConnectionMsg, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    DownloadImageTask downloadImgeTask = new DownloadImageTask();
                    downloadImgeTask.execute(imageUrl);
                }
            }
        });

        findViewById(R.id.cameraIV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        findViewById(R.id.okBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject= subjectET.getText().toString().trim();
                String body= bodyET.getText().toString();
                String url= urlET.getText().toString();
                float rate = ratingBar.getRating();

                if (subject.length()==0) {
                    Toast.makeText(AddEditActivity.this,getResources().getString(R.string.movieMustHaveATitle),Toast.LENGTH_LONG).show();
                    return;
                }

                Cursor cursor = sqlHelper.getReadableDatabase().query(DBConstants.TABLE_NAME, null, null, null, null, null, null);
                while (cursor.moveToNext()) {
                    String movieTitle = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_SUBJECT));
                    if (movieTitle.equals(subject)) {
                        int dbId = cursor.getInt(cursor.getColumnIndex(DBConstants.COLUMN_ID));
                        if (dbId!=id) {
                            Toast.makeText(AddEditActivity.this, getResources().getString(R.string.movieTitleAlreadyExists) + subject, Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                }


                ContentValues contentValues= new ContentValues();
                contentValues.put(DBConstants.COLUMN_SUBJECT, subject);
                contentValues.put(DBConstants.COLUMN_BODY, body);
                contentValues.put(DBConstants.COLUMN_URL, url);
                contentValues.put(DBConstants.COLUMN_RATE, rate);
                contentValues.put(DBConstants.COLUMN_WATCHED, watched);
                contentValues.put(DBConstants.COLUMN_IMDBID, imdbid);

                if (photoChanged) {
                    //get String from image Bitmap
                    String imgBmapStr = null;
                    if (imgBmap != null) {
                        Log.d("bmapsize before",""+imgBmap.getByteCount());
                        Bitmap converetdImage = getResizedBitmap(imgBmap, 500);
                        Log.d("bmapsize after1",""+converetdImage.getByteCount());
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        converetdImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        Log.d("bmapsize after2",""+converetdImage.getByteCount());
                        byte[] b = baos.toByteArray();
                        imgBmapStr = Base64.encodeToString(b, Base64.DEFAULT);
                    }


                    contentValues.put(DBConstants.COLUMN_IMG, imgBmapStr);
                }
                if(id ==-1) {
                    sqlHelper.getWritableDatabase().insert(DBConstants.TABLE_NAME, null, contentValues);
                }
                else
                {
                    //we got the id from mainActivity
                    sqlHelper.getWritableDatabase().update(DBConstants.TABLE_NAME, contentValues, DBConstants.COLUMN_ID+"=?" , new String[] { ""+id });

                }

                Intent intent = new Intent(AddEditActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                //finish();
            }
        });

        findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
    }


    class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(AddEditActivity.this);
            dialog.setTitle(getResources().getString(R.string.connecting));
            dialog.setMessage(getResources().getString(R.string.pleaseWait));
            dialog.setCancelable(true);
            dialog.show();
            Log.d("pre", "exe");
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap= null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(params[0]);
                // open a connection
                connection = (HttpURLConnection) url.openConnection();
                InputStream in = (InputStream) url.getContent();
                bitmap = BitmapFactory.decodeStream(in);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap downloadedImage) {

            posterIV.setImageBitmap(downloadedImage);
            dialog.dismiss();
            imgBmap = downloadedImage;
            photoChanged = true;

        }
    }

}
