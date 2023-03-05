package com.example.enotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.enotes.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ViewPager2 viewPager;
    ImageView viewPagerIndicator;
    TextView btnSubjects;
    TextView btnShare;
    TextView btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        viewPagerIndicator = findViewById(R.id.viewPagerIndicator);
        btnSubjects = findViewById(R.id.btnSubjects);
        btnShare = findViewById(R.id.btnShare);
        btnSettings = findViewById(R.id.btnSettings);

        SubjectsFragment subjectsFragment = new SubjectsFragment();
        ShareFragment shareFragment = new ShareFragment();
        SettingsFragment settingsFragment = new SettingsFragment();

        btnSubjects.setTypeface(null, Typeface.BOLD);
        btnShare.setTypeface(null, Typeface.NORMAL);
        btnSettings.setTypeface(null, Typeface.NORMAL);

        FragmentStateAdapter adapter = new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0:
                        return subjectsFragment;
                    case 1:
                        return shareFragment;
                    case 2:
                        return settingsFragment;
                    default:
                        throw new IllegalArgumentException("Invalid position: " + position);
                }
            }

            @Override
            public int getItemCount() {
                return 3;
            }
        };
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Calculate the new position of the indicator and move it
                int indicatorWidth = viewPagerIndicator.getWidth();
                int offset = (int) ((position + positionOffset) * indicatorWidth);
                viewPagerIndicator.setTranslationX(offset);
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        btnSubjects.setTypeface(null, Typeface.BOLD);
                        btnShare.setTypeface(null, Typeface.NORMAL);
                        btnSettings.setTypeface(null, Typeface.NORMAL);
                        break;
                    case 1:
                        btnSubjects.setTypeface(null, Typeface.NORMAL);
                        btnShare.setTypeface(null, Typeface.BOLD);
                        btnSettings.setTypeface(null, Typeface.NORMAL);
                        break;
                    case 2:
                        btnSubjects.setTypeface(null, Typeface.NORMAL);
                        btnShare.setTypeface(null, Typeface.NORMAL);
                        btnSettings.setTypeface(null, Typeface.BOLD);
                        break;
                }
            }
        });
    }
}