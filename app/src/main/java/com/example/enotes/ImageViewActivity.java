package com.example.enotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class ImageViewActivity extends AppCompatActivity {

    Button btnDelete;

    private int currentPictureID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        btnDelete = findViewById(R.id.btnDelete);

//        int imageId = getIntent().getIntExtra("imageId", 0);
        int subjectId = getIntent().getIntExtra("subjectId", 0);
        int imagePosition = getIntent().getIntExtra("imagePosition", 0);

        DatabaseHelper databaseHelper = new DatabaseHelper(ImageViewActivity.this);

        Cursor cursor = databaseHelper.getAllImages(subjectId);

        List<byte[]> imageDataList = new ArrayList<>();
        List<Integer> imageIdList = new ArrayList<>();

        while (cursor.moveToNext()) {
            byte[] imageData = cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_DATA));
            int imageId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_ID));
            imageDataList.add(imageData);
            imageIdList.add(imageId);
        }

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        ImageViewPagerAdapter adapter = new ImageViewPagerAdapter(imageDataList);
        viewPager.setAdapter(adapter);

        viewPager.setOffscreenPageLimit(1);
        viewPager.setCurrentItem(imagePosition, false);

        currentPictureID = imageIdList.get(imagePosition);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ImageViewActivity.this);
                builder.setMessage("Are you sure you want to delete this image?")
                        .setTitle("Delete Image")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DatabaseHelper databaseHelper = new DatabaseHelper(ImageViewActivity.this);
                                databaseHelper.deleteImage(currentPictureID);
                                Toasty.success(ImageViewActivity.this, "Image Deleted", Toasty.LENGTH_SHORT, true).show();
                                finish();
                                SubjectViewActivity.updateImages(ImageViewActivity.this, subjectId);
                                SubjectsFragment.updateSubjectPictures(ImageViewActivity.this, SubjectViewActivity.subjectPosition, SubjectViewActivity.gridPictures.getCount());
                            }
                        })
                        .setNegativeButton("Cancel", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                // Get the current image ID and print it to the console
                currentPictureID = imageIdList.get(position);
                System.out.println("Current image ID: " + currentPictureID);
            }
        });

        System.out.println("Current image ID: " + currentPictureID);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private PhotoView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
