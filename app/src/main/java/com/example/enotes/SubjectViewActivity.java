package com.example.enotes;

import static com.example.enotes.DatabaseHelper.COLUMN_IMAGE_DATA;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SubjectViewActivity extends AppCompatActivity {

    private String subjectName;
    private int subjectID;
    DatabaseHelper databaseHelper;
    TextView btnBack;
    TextView tvSubjectName;
    TextView tvSubjectPictures;
    Button btnAddImage;

    GridView gridPictures;

    private static final int CAMERA_REQUEST_CODE = 1888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_view);

        Intent intent = getIntent();
        if (intent != null) {
            subjectName = intent.getStringExtra("subjectName");
        }

        btnBack = findViewById(R.id.btnBack);
        tvSubjectName = findViewById(R.id.tvSubjectName);
        tvSubjectPictures = findViewById(R.id.tvPicturesView);
        btnAddImage = findViewById(R.id.btnAddImage);
        gridPictures = findViewById(R.id.gridPictures);

        gridPictures.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        });

        tvSubjectName.setText(subjectName);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectViewActivity.this.finish();
            }
        });

        databaseHelper = new DatabaseHelper(SubjectViewActivity.this);
        subjectID = databaseHelper.getIDBySubjectName(subjectName);
        loadImages();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            if (bitmap != null) {

                // Create a matrix for rotating the image
                Matrix matrix = new Matrix();
                matrix.postRotate(90);

                // Rotate the image
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                databaseHelper = new DatabaseHelper(SubjectViewActivity.this);
                databaseHelper.saveImage(databaseHelper.getIDBySubjectName(subjectName), byteArray, new Date());
                loadImages();
            }
        }
    }

    private void loadImages()
    {
        List<Bitmap> bitmaps = new ArrayList<>();
        databaseHelper = new DatabaseHelper(SubjectViewActivity.this);
        Cursor cursor = databaseHelper.getAllImages(subjectID);
        System.out.println(subjectID + " " + subjectName);
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") byte[] byteArray = cursor.getBlob(cursor.getColumnIndex(COLUMN_IMAGE_DATA ));
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                bitmaps.add(bitmap);
            } while (cursor.moveToNext());
        }
        cursor.close();
        ImageAdapter adapter = new ImageAdapter(this, bitmaps);
        gridPictures.setAdapter(adapter);
        tvSubjectPictures.setText(gridPictures.getCount() + " Pictures");
    }
}