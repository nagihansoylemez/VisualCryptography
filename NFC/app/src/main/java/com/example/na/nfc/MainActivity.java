package com.example.na.nfc;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.DialogPreference;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.preference.CheckBoxPreference;

import com.example.na.nfc.listeners.CryptoListener;

import java.io.IOException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,CryptoListener {

    private static final String TAG ="TestApp";
    private Button cryptoBtn,chooseImage, sendShareOne,sendShareTwo;
    private int PICK_IMAGE_REQUEST=1;
    private ImageView original,shareOne,shareTwo;
    private VisualCrypter mCrypter;
    private ProgressDialog cryptoProgress;
    private Bitmap choosenImage;

    // TODO : Create imageViews, run algorithm, show shares on ui.
    // TODO : check devices NFC compatibility.
    // DONE : Sharing algorithm(python copy.) Ref : https://mail.google.com/mail/u/0/#inbox/1555b04bb5c889ea

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_master);

        ActionBar actionBar = getActionBar();
        if(actionBar!=null) {
            actionBar.setLogo(R.mipmap.ic_launcher);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }else{
            Log.i(TAG,"Action BAR is NULL");
        }
        cryptoBtn = (Button) findViewById(R.id.crypto_button);
        chooseImage = (Button) findViewById(R.id.uploadImgButton);

        original = (ImageView) findViewById(R.id.originalImageView);
        shareOne = (ImageView) findViewById(R.id.sharedOneImageView);
        shareTwo = (ImageView) findViewById(R.id.sharedTwoImageView);

        // verify permission for marshmallow and +
        verifyStoragePermissions(this);
        // setup listeners...
        cryptoBtn.setOnClickListener(this);
        chooseImage.setOnClickListener(this);
        cryptoProgress = new ProgressDialog(MainActivity.this);
        cryptoProgress.setTitle("VisualCrypter");
        cryptoProgress.setMessage("Resim Şifreleniyor Lütfen Bekleyiniz...");
        cryptoProgress.setIndeterminate(false);
        cryptoProgress.setMax(100);
        cryptoProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        cryptoProgress.setCancelable(false);
        cryptoProgress.create();



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                choosenImage = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                final ImageView imageView = (ImageView) findViewById(R.id.originalImageView);
                imageView.setImageBitmap(choosenImage);
                mCrypter = new VisualCrypter(getApplicationContext(), choosenImage, this);
            }
            catch (IOException e) {
                Log.i(TAG, "onActivityResult : " + e);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onClick(View v) {

        if(v.equals(cryptoBtn)){
            Log.i(TAG,"crypto button clicked!");
            if(mCrypter!=null) {
                Log.i(TAG,"Calculating pixels..");
                showProgressDialog();
                new Thread(mCrypter).start();
            }else{
                AlertDialog.Builder imageAlert = new AlertDialog.Builder(MainActivity.this);
                imageAlert.setMessage("Resim seçiniz!");
                imageAlert.show();
                // alertdialog eklenecek.
            }
        }

        if(v.equals(chooseImage)){
            Intent galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(galleryIntent, "Resim Seç"), PICK_IMAGE_REQUEST);
        }

        if(v.equals(sendShareOne)){

        }
    }

    @Override
    public void onFinish() {
                closeProgressDialog();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                int s1h=mCrypter.getShareOne().getHeight();
                int s1w = mCrypter.getShareOne().getWidth();
                //shareTwo.setImageBitmap(mCrypter.getShareTwo());
                shareOne.setImageBitmap(mCrypter.getShareOne());
                shareTwo.setImageBitmap(mCrypter.getShareTwo());

            }
        });


    }

    private void showProgressDialog(){
        Log.i(TAG,"Showing progress..");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cryptoProgress.show();
            }
        });

    }

    private void closeProgressDialog(){
        Log.i(TAG,"Closing progress..");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cryptoProgress.dismiss();
            }
        });

    }
}