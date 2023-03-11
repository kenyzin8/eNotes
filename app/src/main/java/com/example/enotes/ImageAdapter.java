package com.example.enotes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private List<ImageData> mImageDataList;
    private List<Integer> mImageIds;

    public ImageAdapter(Context context, List<ImageData> imageDataList, List<Integer> imageIds) {
        mContext = context;
        mImageDataList = imageDataList;
        mImageIds = imageIds;
    }

    @Override
    public int getCount() {
        return mImageDataList.size();
    }

    @Override
    public ImageData getItem(int position) {
        return mImageDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false);
        }

        ImageView imageView = view.findViewById(R.id.imageView);
        int imageId = mImageIds.get(position);
        imageView.setTag(imageId);

        Glide.with(mContext)
                .asBitmap()
                .load(mImageDataList.get(position).getByteArray())
                .override(384, 512)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(resource, 384, 512, false);
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
                        Bitmap compressedBitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(outputStream.toByteArray()));
                        imageView.setImageBitmap(compressedBitmap);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });

        return view;
    }
}
