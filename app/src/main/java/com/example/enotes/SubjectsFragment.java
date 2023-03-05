package com.example.enotes;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class SubjectsFragment extends Fragment
{
    static ListView listView;
    static ListAdapter listAdapter;
    static ArrayList<ListData> dataArrayList = new ArrayList<>();
    ListData listData;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_subjects, container, false);
        listView = view.findViewById(R.id.listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getActivity(), dataArrayList.get(i).subject, Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    public static void addSubject(Context context, String subject, String schedule, int pictures, int color)
    {
        dataArrayList.add(new ListData(subject, schedule, pictures, color));
        listAdapter = new ListAdapter(context, dataArrayList);

        listView.setAdapter(listAdapter);
    }
}