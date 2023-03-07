package com.example.enotes;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private List<Bitmap> mBitmaps;

    public ImageAdapter(Context context, List<Bitmap> bitmaps) {
        mContext = context;
        mBitmaps = bitmaps;
    }

    @Override
    public int getCount() {
        return mBitmaps.size();
    }

    @Override
    public Bitmap getItem(int position) {
        return mBitmaps.get(position);
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

        Bitmap bitmap = getItem(position);

        imageView.setImageBitmap(bitmap);

        return view;
    }
}
