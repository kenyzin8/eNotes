package com.example.enotes;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

public class SubjectsFragment extends Fragment
{
    ListView listView;
    ListAdapter listAdapter;
    ArrayList<ListData> dataArrayList = new ArrayList<>();
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

        dataArrayList.add(new ListData("Computer Programming 1", "Monday", 0));
        dataArrayList.add(new ListData("Computer Programming 1", "qwe", 0));
        dataArrayList.add(new ListData("Computer Programming 1", "Monday", 0));
        dataArrayList.add(new ListData("Computer Programming 1", "Monday", 123));
        dataArrayList.add(new ListData("Computer Programming 1", "Monday", 0));
        dataArrayList.add(new ListData("Computer Programming 1", "Monday", 0));
        dataArrayList.add(new ListData("Computer Programming 1", "Monday", 0));
        dataArrayList.add(new ListData("Computer Programming 1", "Monday", 0));
        dataArrayList.add(new ListData("Computer Programming 1", "Monday", 0));
        dataArrayList.add(new ListData("Computer Programming 1", "Monday", 0));
        dataArrayList.add(new ListData("Computer Programming 1", "Monday", 0));
        dataArrayList.add(new ListData("Computer Programming 1", "Monday", 0));
        dataArrayList.add(new ListData("Computer Programming 1", "Monday", 0));
        dataArrayList.add(new ListData("Computer Programming 1", "Monday", 0));
        dataArrayList.add(new ListData("Computer Programming 1", "Monday", 0));


        listAdapter = new ListAdapter(requireActivity(), dataArrayList);
        listView.setAdapter(listAdapter);

        return view;
    }
}