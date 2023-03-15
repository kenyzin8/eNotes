package com.example.enotes;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.List;


public class SettingsFragment extends Fragment {

    public static Switch displaySubjectSchedule,
            displaySubjectPicture,
            enableImageImport,
            enableSubjectSharing,
            enableSubjectDeletion,
            enableImageSaving,
            enableImageDeletion;


    ArrayList<Integer> oldSubjectColors = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        displaySubjectSchedule = view.findViewById(R.id.switchDisplaySubjectSchedule);
        displaySubjectPicture = view.findViewById(R.id.switchDisplaySubjectPicture);
        enableImageImport = view.findViewById(R.id.switchAllowImageImport);
        enableSubjectSharing = view.findViewById(R.id.switchAllowShare);
        enableSubjectDeletion = view.findViewById(R.id.switchEnableSubjectDeletion);
        enableImageSaving = view.findViewById(R.id.switchEnableSaveImageToPhone);
        enableImageDeletion = view.findViewById(R.id.switchEnableImageDeletion);

        displaySubjectSchedule.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ListAdapter.isDisplaySubjectScheduleOn = b;
                SubjectsFragment.refreshSubjects();
            }
        });

        displaySubjectPicture.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ListAdapter.isDisplayPictureOn = b;
                SubjectsFragment.refreshSubjects();
            }
        });

        enableImageImport.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SubjectViewActivity.isImportAllowed = b;
            }
        });

        enableSubjectSharing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ShareFragment.isSharingAllowed = b;
            }
        });

        enableSubjectDeletion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SubjectViewActivity.isDeleteAllowed = b;
            }
        });

        enableImageSaving.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ImageViewPagerAdapter.isSavingAllowed = b;
            }
        });

        enableImageDeletion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ImageViewActivity.isDeleteAllowed = b;
            }
        });

        return view;
    }
}