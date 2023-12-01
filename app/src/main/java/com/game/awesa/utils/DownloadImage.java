package com.game.awesa.utils;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.ImageView;

import java.io.IOException;
import java.util.HashMap;

public class DownloadImage extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;

    public DownloadImage(ImageView bmImage) {
        this.bmImage = (ImageView) bmImage;
        bmImage.setImageResource(android.R.drawable.gallery_thumb);
    }

    protected Bitmap doInBackground(String... urls) {
        Bitmap myBitmap = null;
        MediaMetadataRetriever mMRetriever = null;
        try {
            mMRetriever = new MediaMetadataRetriever();
            if (Build.VERSION.SDK_INT >= 14)
                mMRetriever.setDataSource(urls[0], new HashMap<String, String>());
            else
                mMRetriever.setDataSource(urls[0]);
            myBitmap = mMRetriever.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();


        } finally {
            if (mMRetriever != null) {
                try {
                    mMRetriever.release();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return myBitmap;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }
}
