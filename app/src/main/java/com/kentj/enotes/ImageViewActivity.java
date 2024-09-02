package com.kentj.enotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class ImageViewActivity extends AppCompatActivity {

    private int currentPictureID;
    private TextView tvIndex;
    private int subjectId, imagePosition, subjectPosition, imageDataListSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        ImageView btnDelete = findViewById(R.id.btnDelete);
        ImageView btnBackImageView = findViewById(R.id.btnBackImageView);
        tvIndex= findViewById(R.id.tvIndex);

        subjectId = getIntent().getIntExtra("subjectId", 0);
        imagePosition = getIntent().getIntExtra("imagePosition", 0);
        subjectPosition = getIntent().getIntExtra("subjectPosition", 0);
        imageDataListSize = getIntent().getIntExtra("imageDataListSize", 0);

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
        ImageViewPagerAdapter adapter = new ImageViewPagerAdapter(this, imageDataList);
        viewPager.setAdapter(adapter);

        viewPager.setOffscreenPageLimit(1);
        viewPager.setCurrentItem(imagePosition, false);

        currentPictureID = imageIdList.get(imagePosition);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View customDialogView = LayoutInflater.from(ImageViewActivity.this).inflate(R.layout.delete_image_dialog, null);
                android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(ImageViewActivity.this)
                        .setView(customDialogView)
                        .create();
                Button yesButton = customDialogView.findViewById(R.id.dialogYesButtonDelete);
                Button noButton = customDialogView.findViewById(R.id.dialogNoButtonDelete);
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatabaseHelper databaseHelper = new DatabaseHelper(ImageViewActivity.this);
                        databaseHelper.deleteImage(currentPictureID);

                        Toasty.success(ImageViewActivity.this, "Image Deleted" + imagePosition, Toasty.LENGTH_SHORT, true).show();
                        finish();

                        SubjectViewActivity.imageAdapter.removeImage(currentPictureID);
                        SubjectsFragment.updateSubjectPictures(ImageViewActivity.this, subjectPosition, imageDataListSize);

                        dialog.dismiss();
                    }
                });
                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });


        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPictureID = imageIdList.get(position);
                tvIndex.setText((position + 1) + "/" + adapter.getItemCount());
                imagePosition = position;
            }
        });

        btnBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tvIndex.setText((viewPager.getCurrentItem() + 1) + "/" + adapter.getItemCount());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private PhotoView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
