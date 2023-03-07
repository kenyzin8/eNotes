package com.example.enotes;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

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

    private DatabaseHelper dbHelper;

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
                dbHelper = new DatabaseHelper(getActivity());

                Intent intent = new Intent(getActivity(), SubjectViewActivity.class);
                intent.putExtra("subjectName", dataArrayList.get(i).subject);
                startActivity(intent);
            }
        });

        dbHelper = new DatabaseHelper(getActivity());
        loadSubjects(dbHelper.getAllSubjects()); //GET TOTAL PICTURES

        return view;
    }

    public static void addSubject(Context context, String subject, String schedule, int pictures, int color)
    {
        dataArrayList.add(new ListData(subject, schedule, pictures, color));

        listAdapter = new ListAdapter(context, dataArrayList);

        listView.setAdapter(listAdapter);
    }

    public void loadSubjects(Cursor cursor)
    {
        dbHelper = new DatabaseHelper(getActivity());

        while(cursor.moveToNext())
        {
            addSubject(getActivity(), cursor.getString(1), cursor.getString(2), dbHelper.getNumImagesForSubject(cursor.getInt(0)), cursor.getInt(3));
        }
    }
}