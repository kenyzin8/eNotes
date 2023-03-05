package com.example.enotes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ListAdapter extends ArrayAdapter<ListData>
{
    public ListAdapter(@NonNull Context context, ArrayList<ListData> dataArrayList) {
        super(context, R.layout.list_subjects, dataArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        ListData listData = getItem(position);
        if(view == null)
        {
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_subjects, parent, false);
        }

        TextView tvSubjects = view.findViewById(R.id.tvSubjects);
        TextView tvSchedule = view.findViewById(R.id.tvSchedule);
        TextView tvPictures = view.findViewById(R.id.tvPictures);

        tvSubjects.setText(listData.subject);
        tvSchedule.setText(listData.schedule);
        tvPictures.setText(listData.pictures + " Pictures");

        return view;
    }
}