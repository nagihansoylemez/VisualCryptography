package com.example.na.nfc;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;

import com.example.na.nfc.listeners.CryptoListener;


public class MainActivity extends Activity {

    private static final String TAG ="TestApp";
    private Button cryptoBtn;
    private ImageView original,shareOne,shareTwo;
    private VisualCrypter mCrypter;

    // TODO : Create imageViews, run algorithm, show shares on ui.
    // TODO : check devices NFC compatibility.
    // DONE : Sharing algorithm(python copy.) Ref : https://mail.google.com/mail/u/0/#inbox/1555b04bb5c889ea

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);
        cryptoBtn = (Button) findViewById(R.id.crypto_button);
        original = (ImageView) findViewById(R.id.originalImageView);
        shareOne = (ImageView) findViewById(R.id.sharedOneImageView);
        shareTwo = (ImageView) findViewById(R.id.sharedTwoImageView);

    verifyStoragePermissions(this);
        Bitmap bmp = BitmapFactory.decodeResource(getResources(),R.drawable.ktu_test_img);

        Log.i(TAG,"Bmp width: " + bmp.getWidth()+ " heigth : " + bmp.getHeight());

        mCrypter = new VisualCrypter(getApplicationContext(),bmp, new CryptoListener() {
            @Override
            public void onFinish() {

                Log.i(TAG,"calculate finish.");
                //shareOne.setImageBitmap(mCrypter.getShareOne());
                int s1h=mCrypter.getShareOne().getHeight();
                int s1w = mCrypter.getShareOne().getWidth();
                //shareTwo.setImageBitmap(mCrypter.getShareTwo());

                shareOne.setImageBitmap(mCrypter.getShareOne());
                shareTwo.setImageBitmap(mCrypter.getShareTwo());

                mCrypter.writeToFileShare1();
                mCrypter.writeToFileShare2();
                mCrypter.XORAndSave();


                Log.i(TAG,"S1 : W : "+ s1w + " H : " + s1h);
            }
        });



        cryptoBtn.setOnClickListener(new View.OnClickListener() {
           @Override
          public void onClick(View v) {
               Log.i(TAG,"button clicked!");
              mCrypter.calculatePixels();
         }
       });


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
}
