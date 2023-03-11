package com.example.enotes;

import static com.example.enotes.DatabaseHelper.COLUMN_IMAGE_DATA;
import static com.example.enotes.DatabaseHelper.COLUMN_IMAGE_ID;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.Manifest;

public class SubjectViewActivity extends AppCompatActivity {

    private String subjectName;
    private int subjectID;
    DatabaseHelper databaseHelper;
    TextView btnBack;
    TextView tvSubjectName;
    TextView tvSubjectPictures;
    Button btnAddImage;

    GridView gridPictures;
    private Uri imageUri;
    private static final int CAMERA_REQUEST_CODE = 1888;
    private static final int REQUEST_CAMERA_PERMISSION = 1;

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
                // Get the image ID from the tag of the selected ImageView
                int imageId = (int) view.getTag();

                // Start the ImageViewActivity and pass the image ID as an extra
                Intent intent = new Intent(SubjectViewActivity.this, ImageViewActivity.class);
                intent.putExtra("imageId", imageId);
                startActivity(intent);
            }
        });

        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(SubjectViewActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SubjectViewActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                } else {
                    launchCamera();
                }
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(SubjectViewActivity.this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            System.out.println(imageUri);
            if (imageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    if (bitmap != null) {

                        int targetWidth = bitmap.getWidth(); // Change scale factor to 2
                        int targetHeight = bitmap.getHeight();

                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false);

                        Matrix matrix = new Matrix();

                        matrix.postRotate(90);


                        Bitmap rotatedBitmap = Bitmap.createBitmap(resizedBitmap, 0, 0, targetWidth, targetHeight, matrix, true);

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                        byte[] byteArray = stream.toByteArray();

                        databaseHelper = new DatabaseHelper(SubjectViewActivity.this);
                        databaseHelper.saveImage(databaseHelper.getIDBySubjectName(subjectName), byteArray, new Date());
                        loadImages();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Create a file to store the image
            File imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (imageFile != null) {
                // Get the Uri of the image file
                imageUri = FileProvider.getUriForFile(this, "com.example.myapp.fileprovider", imageFile);

                // Set the Uri as an extra in the camera intent
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                // Launch the camera app
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        }
    }
    private File createImageFile() throws IOException {
        // Create a unique image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";

        // Get the directory where the image will be stored
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Create the image file
        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);

        return imageFile;
    }
    private void loadImages()
    {
        List<Bitmap> bitmaps = new ArrayList<>();
        List<Integer> imageIds = new ArrayList<>(); // Add this line
        databaseHelper = new DatabaseHelper(SubjectViewActivity.this);
        Cursor cursor = databaseHelper.getAllImages(subjectID);
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") byte[] byteArray = cursor.getBlob(cursor.getColumnIndex(COLUMN_IMAGE_DATA));
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                @SuppressLint("Range") int imageId = cursor.getInt(cursor.getColumnIndex(COLUMN_IMAGE_ID)); // Retrieve the image ID from the cursor
                bitmaps.add(bitmap);
                imageIds.add(imageId); // Add the image ID to the list
            } while (cursor.moveToNext());
        }
        cursor.close();
        ImageAdapter adapter = new ImageAdapter(this, bitmaps, imageIds); // Pass the image IDs to the adapter
        gridPictures.setAdapter(adapter);
        tvSubjectPictures.setText(gridPictures.getCount() + " Pictures");
    }
}