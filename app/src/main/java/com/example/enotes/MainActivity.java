package com.example.enotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.enotes.databinding.ActivityMainBinding;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {
    ViewPager2 viewPager;
    ImageView btnAddSubject;
    int cardColor = 0;
    AlertDialog colorPickerDialog;
    private DatabaseHelper dbHelper;
    private AdView homeAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);

        SubjectsFragment subjectsFragment = new SubjectsFragment();

        btnAddSubject = findViewById(R.id.btnAdd);

        btnAddSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFormDialog();
            }
        });

        FragmentStateAdapter adapter = new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0:
                        return subjectsFragment;
                    default:
                        throw new IllegalArgumentException("Invalid position: " + position);
                }
            }
            @Override
            public int getItemCount() {
                return 1;
            }
        };

        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false);

        homeAdView = findViewById(R.id.homeAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        homeAdView.loadAd(adRequest);

        homeAdView.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                super.onAdClicked();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }
        });
    }

    private void showFormDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View view = LayoutInflater.from(this).inflate(R.layout.form_layout, null);

        TextInputLayout subjectLayout = view.findViewById(R.id.subjectLayout);
        TextInputLayout scheduleDayLayout = view.findViewById(R.id.scheduleDayLayout);
        TextInputLayout scheduleTimeLayout = view.findViewById(R.id.scheduleTimeLayout);
        EditText subjectEditText = view.findViewById(R.id.subjectEditText);
        EditText scheduleDayEditText = view.findViewById(R.id.scheduleDayEditText);
        EditText scheduleTimeEditText = view.findViewById(R.id.scheduleTimeEditText);

        builder.setView(view);

        builder.setTitle("Add Subject");
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dbHelper = new DatabaseHelper(MainActivity.this);

                if(!dbHelper.isNameUnique(subjectEditText.getText().toString().trim()))
                {
                    Toasty.error(MainActivity.this, "Subject already exist.", Toast.LENGTH_SHORT, true).show();
                    return;
                }

                String subject = subjectEditText.getText().toString().trim();
                String scheduleDay = scheduleDayEditText.getText().toString().trim();
                String scheduleTime = scheduleTimeEditText.getText().toString().trim();

                if(subject.isEmpty() || scheduleDay.isEmpty() || scheduleDay.isEmpty())
                {
                    Toasty.error(MainActivity.this, "Missing Fields", Toast.LENGTH_SHORT, true).show();
                    return;
                }

                AlertDialog.Builder colorPickerBuilder = new AlertDialog.Builder(MainActivity.this);

                View colorPickerView = LayoutInflater.from(MainActivity.this).inflate(R.layout.color_picker, null);

                GridView colorPickerGridView = colorPickerView.findViewById(R.id.colorPickerGridView);

                ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(MainActivity.this);
                colorPickerGridView.setAdapter(colorPickerAdapter);

                colorPickerGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        colorPickerAdapter.setSelectedPosition(position);
                        cardColor = colorPickerAdapter.getSelectedColor();
                        String schedule = scheduleDay + "\n" + scheduleTime;
                        SubjectsFragment.addSubject(MainActivity.this, subject, schedule, 0, cardColor);
                        dbHelper = new DatabaseHelper(MainActivity.this);

                        dbHelper.AddSubject(subject, schedule, cardColor);
                        Toasty.success(MainActivity.this, subject + "- Subject Added", Toast.LENGTH_SHORT, true).show();
                        colorPickerDialog.dismiss();
                    }
                });

                colorPickerBuilder.setTitle("Pick Color");
                colorPickerBuilder.setNegativeButton("Cancel", null);

                colorPickerBuilder.setView(colorPickerView);

                colorPickerDialog = colorPickerBuilder.create();
                colorPickerDialog.show();
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}