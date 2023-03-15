package com.example.enotes;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import es.dmoral.toasty.Toasty;

public class ShareFragment extends Fragment {

    ImageView btnUpload, btnHelp;
    public static boolean isSharingAllowed = true;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_share, container, false);
        btnUpload = view.findViewById(R.id.btnShare);
        btnHelp = view.findViewById(R.id.btnHelp);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isSharingAllowed)
                {
                    Toasty.warning(getContext(), "Not Yet Supported", Toasty.LENGTH_LONG).show();
                }
                else
                {
                    Toasty.error(getContext(), "Sharing Disabled", Toasty.LENGTH_LONG).show();
                }
            }
        });

        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return view;
    }
}