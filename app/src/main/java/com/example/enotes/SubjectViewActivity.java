package com.example.enotes;

import static android.content.ContentValues.TAG;
import static com.example.enotes.DatabaseHelper.COLUMN_IMAGE_DATA;
import static com.example.enotes.DatabaseHelper.COLUMN_IMAGE_ID;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
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
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.Manifest;

import es.dmoral.toasty.Toasty;

public class SubjectViewActivity extends AppCompatActivity {

    private String subjectName;
    private int subjectID;
    private DatabaseHelper databaseHelper;
    public static TextView btnBack;
    public static TextView tvSubjectName;
    public static TextView tvSubjectPictures;
    public static Button btnAddImage;

    public static ImageView btnDeleteSubject;
    public static ImageView btnImportImage;

    public static GridView gridPictures;
    private Uri imageUri;
    private static final int CAMERA_REQUEST_CODE = 1888;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    public static int subjectPosition;
    ArrayList<ImageData> imageDataList;

    public static boolean isImportAllowed = true;
    public static boolean isDeleteAllowed = true;

    private static final int REQUEST_CODE_SELECT_IMAGE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_view);

        Intent intent = getIntent();
        if (intent != null) {
            subjectName = intent.getStringExtra("subjectName");
            subjectPosition = intent.getIntExtra("subjectPosition", 0);
        }

        btnBack = findViewById(R.id.btnBack);
        tvSubjectName = findViewById(R.id.tvSubjectName);
        tvSubjectPictures = findViewById(R.id.tvPicturesView);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnDeleteSubject = findViewById(R.id.btnDeleteSubject);
        btnImportImage = findViewById(R.id.btnImportImage);
        gridPictures = findViewById(R.id.gridPictures);

        gridPictures.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), "Long click detected!", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        gridPictures.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int imageId = (int) view.getTag();
                Intent intent = new Intent(SubjectViewActivity.this, ImageViewActivity.class);
                intent.putExtra("imageId", imageId);
                intent.putExtra("subjectId", subjectID);
                intent.putExtra("imagePosition", i);

                System.out.println(imageId + " " + subjectID + " " + i);
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

        btnDeleteSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SubjectViewActivity.this);
                builder.setTitle("Delete subject");
                builder.setMessage("Are you sure you want to delete this subject? All images inside this subject will be deleted as well.");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        databaseHelper = new DatabaseHelper(SubjectViewActivity.this);
                        databaseHelper.deleteSubject(subjectID);
                        finish();
                        SubjectsFragment.removeSubject(SubjectViewActivity.this, subjectPosition);
                        Toasty.success(SubjectViewActivity.this, "Subject has been deleted.", Toasty.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
            }
        });


        btnImportImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImageFromGallery();
            }
        });

        if(isImportAllowed)
        {
            btnImportImage.setVisibility(View.VISIBLE);
        }
        else
        {
            btnImportImage.setVisibility(View.INVISIBLE);
        }

        if(isDeleteAllowed)
        {
            btnDeleteSubject.setVisibility(View.VISIBLE);
        }
        else
        {
            btnDeleteSubject.setVisibility(View.INVISIBLE);
        }

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
    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                    if (bitmap != null) {
                        int targetWidth = bitmap.getWidth();
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
                        Toasty.success(SubjectViewActivity.this, "Image Imported", Toast.LENGTH_SHORT, true).show();
                        SubjectsFragment.updateSubjectPictures(SubjectViewActivity.this, subjectPosition, gridPictures.getCount());

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            if (imageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    if (bitmap != null) {
                        int targetWidth = bitmap.getWidth();
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
                        Toasty.success(SubjectViewActivity.this, "Image Added", Toast.LENGTH_SHORT, true).show();
                        SubjectsFragment.updateSubjectPictures(SubjectViewActivity.this, subjectPosition, gridPictures.getCount());

                        int rowsDeleted = getContentResolver().delete(imageUri, null, null);
                        if (rowsDeleted > 0) {
                            Log.d(TAG, "Image file deleted successfully");
                        } else {
                            Log.e(TAG, "Failed to delete image file");
                        }
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
            File imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (imageFile != null) {
                imageUri = FileProvider.getUriForFile(this, "com.example.myapp.fileprovider", imageFile);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                System.out.println(imageUri);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        }
    }
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);

        return imageFile;
    }


    private void loadImages() {

        databaseHelper = new DatabaseHelper(SubjectViewActivity.this);
        Cursor cursor = databaseHelper.getAllImages(subjectID);
        imageDataList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") byte[] byteArray = cursor.getBlob(cursor.getColumnIndex(COLUMN_IMAGE_DATA));
                @SuppressLint("Range") int imageId = cursor.getInt(cursor.getColumnIndex(COLUMN_IMAGE_ID));
                imageDataList.add(new ImageData(byteArray, imageId));
            } while (cursor.moveToNext());
        }
        cursor.close();

        List<Integer> imageIds = new ArrayList<>();
        for (ImageData imageData : imageDataList) {
            imageIds.add(imageData.getImageId());
        }

        ImageAdapter adapter = new ImageAdapter(this, imageDataList, imageIds);
        gridPictures.setAdapter(adapter);
        String pluralHandler = gridPictures.getCount() == 1 ? " Picture" : "Pictures";
        tvSubjectPictures.setText(gridPictures.getCount() + " " + pluralHandler);
    }

    public static void updateImages(Context context, int subjectID) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        Cursor cursor = databaseHelper.getAllImages(subjectID);
        List<ImageData> imageDataList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") byte[] byteArray = cursor.getBlob(cursor.getColumnIndex(COLUMN_IMAGE_DATA));
                @SuppressLint("Range") int imageId = cursor.getInt(cursor.getColumnIndex(COLUMN_IMAGE_ID));
                imageDataList.add(new ImageData(byteArray, imageId));
            } while (cursor.moveToNext());
        }
        cursor.close();

        List<Integer> imageIds = new ArrayList<>();
        for (ImageData imageData : imageDataList) {
            imageIds.add(imageData.getImageId());
        }

        ImageAdapter adapter = new ImageAdapter(context, imageDataList, imageIds);
        gridPictures.setAdapter(adapter);

        String pluralHandler = gridPictures.getCount() == 1 ? " Picture" : "Pictures";
        tvSubjectPictures.setText(gridPictures.getCount() + " " + pluralHandler);
    }
}