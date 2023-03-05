package com.example.enotes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

public class ColorPickerAdapter extends BaseAdapter {

    private Context mContext;
    private int[] mColors;
    private int mSelectedPosition = -1;
    public ColorPickerAdapter(Context context) {
        mContext = context;
        mColors = new int[] {
                ContextCompat.getColor(mContext, R.color.picker1),
                ContextCompat.getColor(mContext, R.color.picker2),
                ContextCompat.getColor(mContext, R.color.picker3),
                ContextCompat.getColor(mContext, R.color.picker4),
                ContextCompat.getColor(mContext, R.color.picker5),
                ContextCompat.getColor(mContext, R.color.picker6),
                ContextCompat.getColor(mContext, R.color.picker7),
                ContextCompat.getColor(mContext, R.color.picker8)
        };
    }

    @Override
    public int getCount() {
        return mColors.length;
    }

    @Override
    public Integer getItem(int position) {
        return mColors[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.color_item, parent, false);
        }

        ImageView colorImageView = view.findViewById(R.id.colorImageView);

        int color = getItem(position);

        colorImageView.setBackgroundColor(color);

        return view;
    }

    public void setSelectedPosition(int position) {
        mSelectedPosition = position;
        notifyDataSetChanged();
    }

    public int getSelectedColor() {
        if (mSelectedPosition != -1) {
            return getItem(mSelectedPosition);
        } else {
            return -1;
        }
    }
}
