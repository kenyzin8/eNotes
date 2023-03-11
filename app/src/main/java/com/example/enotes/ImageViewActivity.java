package com.example.enotes;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

public class ImageViewActivity extends AppCompatActivity {
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 1500;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        int imageId = getIntent().getIntExtra("imageId", 0);

        DatabaseHelper dbHelper = new DatabaseHelper(ImageViewActivity.this);
        byte[] imageData = dbHelper.loadImage(imageId);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);
        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        int scaleFactor = 1;

        if (imageWidth > displayMetrics.widthPixels || imageHeight > displayMetrics.heightPixels) {
            scaleFactor = Math.min(Math.round((float) imageWidth / (float) displayMetrics.widthPixels),
                    Math.round((float) imageHeight / (float) displayMetrics.heightPixels));
        }

        RequestOptions requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(imageWidth / scaleFactor, imageHeight / scaleFactor);

        Glide.with(ImageViewActivity.this)
                .load(imageData)
                .apply(requestOptions)
                .into((ImageView) findViewById(R.id.imageView));
    }
}