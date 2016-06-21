package com.example.na.nfc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.example.na.nfc.listeners.CryptoListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class VisualCrypter implements Runnable{

    private static final String TAG = "TestApp";
    private int imgWidth,imgHeight,shareWidth,shareHeight;
    private CryptoListener mCryptoListener;
    private Bitmap originalImage,grayImage,binarized,shareOne,shareTwo,xored;
    private Context mContext;
    private OtsuBinarize otsu;
    private final int [][] patterns ={
            {1,1,0,0},
            {1,0,1,0},
            {1,0,0,1},
            {0,1,1,0},
            {0,1,0,1},
            {0,0,1,1}
        };

    public VisualCrypter(Context context,Bitmap original,CryptoListener listener){

        if(original!=null && original.getHeight()>0 && original.getWidth() > 0){
            this.originalImage = original;
            imgWidth = originalImage.getWidth();
            imgHeight = originalImage.getHeight();
            shareWidth = imgWidth * 2;
            shareHeight = imgHeight * 2;
            shareOne  = Bitmap.createBitmap(shareWidth,shareHeight,Bitmap.Config.ARGB_8888);
            shareTwo = Bitmap.createBitmap(shareWidth,shareHeight,Bitmap.Config.ARGB_8888);
            binarized = Bitmap.createBitmap(original.getWidth(),original.getHeight(), Bitmap.Config.ARGB_8888);

            mCryptoListener = listener;
            this.mContext = context;
        }
    }


    private void calculatePixels(){

        toGrayscale(originalImage);
        Log.i(TAG,"Binarized started");
        binarized = OtsuBinarize.binarizeImage(grayImage,binarized);
        Log.i(TAG,"Binarized finished");

        for(int x=0;x<shareWidth/2;x++){
            for(int y=0;y<shareHeight/2;y++){
                int pixel = binarized.getPixel(x,y);
                int rnd = randomNumber(1,100);
                int []pat = patterns[rnd];


                shareOne.setPixel(x*2,y*2,setColorFromPattern(pat[0]) );
                shareOne.setPixel(x*2+1,y*2,setColorFromPattern(pat[1]));
                shareOne.setPixel(x*2,y*2+1,setColorFromPattern(pat[2]));
                shareOne.setPixel(x*2+1,y*2+1,setColorFromPattern(pat[3]));

                if(pixel == -1){
                    shareTwo.setPixel(x*2,y*2,setColorFromPattern(1-pat[0]));
                    shareTwo.setPixel(x*2+1,y*2,setColorFromPattern(1-pat[1]));
                    shareTwo.setPixel(x*2,y*2+1,setColorFromPattern(1-pat[2]));
                    shareTwo.setPixel(x*2+1,y*2+1,setColorFromPattern(1-pat[3]));
                }else {
                    shareTwo.setPixel(x*2,y*2,setColorFromPattern(pat[0]));
                    shareTwo.setPixel(x*2+1,y*2,setColorFromPattern(pat[1]));
                    shareTwo.setPixel(x*2,y*2+1,setColorFromPattern(pat[2]));
                    shareTwo.setPixel(x*2+1,y*2+1,setColorFromPattern(pat[3]));


                }

            }

        }


        if(mCryptoListener!=null) {

            writeToFileShare1();
            writeToFileShare2();
            XORAndSave();
            mCryptoListener.onFinish();

        }


    }

    private int setColorFromPattern(int pat){

        if(pat == 0) {
            return Color.WHITE;
        }

        return Color.BLACK;
    }





    private int randomNumber(int min, int max){

        Random rand = new Random();
        int randomNum = rand.nextInt(((max - min) + 1)) + min;
        return randomNum %6;
    }

    public Bitmap getShareOne(){
        return this.shareOne;
    }

    public Bitmap getShareTwo(){
        return this.shareTwo;
    }

    private void writeToFileShare1(){
        OutputStream fOut = null;
        File file = null;
        try {
            String path = Environment.getExternalStorageDirectory().toString();
            file = new File(path, "share1.png");
            fOut = new FileOutputStream(file);
            shareOne.compress(Bitmap.CompressFormat.PNG, 100, fOut); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored

            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fOut != null) {
                    fOut.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            MediaStore.Images.Media.insertImage(mContext.getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
    private void writeToFileShare2(){
        OutputStream fOut = null;
        File file = null;
        try {
            String path = Environment.getExternalStorageDirectory().toString();
            file = new File(path, "share2.png");
            fOut = new FileOutputStream(file);
            shareTwo.compress(Bitmap.CompressFormat.PNG, 100, fOut); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored

            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fOut != null) {
                    fOut.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            MediaStore.Images.Media.insertImage(mContext.getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
    private void writeToFileResult(){
        OutputStream fOut = null;
        File file = null;
        try {
            String path = Environment.getExternalStorageDirectory().toString();
            file = new File(path, "result.png");
            fOut = new FileOutputStream(file);
            xored.compress(Bitmap.CompressFormat.PNG, 100, fOut); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored

            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fOut != null) {
                    fOut.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            MediaStore.Images.Media.insertImage(mContext.getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void XORAndSave(){

        xored = Bitmap.createBitmap(shareWidth,shareHeight,Bitmap.Config.ARGB_8888);
        for(int i=0;i<shareOne.getWidth();i++){


            for(int j=0;j<shareTwo.getHeight();j++){
                xored.setPixel(i,j,setColorFromPattern(XOR(shareOne.getPixel(i,j),shareTwo.getPixel(i,j))));
            }
        }


        writeToFileResult();
    }

    private int XOR(int a, int b ){
        if(a == b){
            return 1;
        }

        return 0;
    }

    @Override
    public void run() {
        calculatePixels();
    }


    private void toGrayscale(Bitmap bmpOriginal)
    {

        Log.i(TAG,"toGrayscale Started...");
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        grayImage = bmpGrayscale;
        Log.i(TAG,"toGrayscale Finished");


    }


}
