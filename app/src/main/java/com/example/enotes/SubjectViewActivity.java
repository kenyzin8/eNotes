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
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.ProgressDialog;
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
import android.os.AsyncTask;
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
import java.io.InputStream;
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
    public static int subjectID;
    private DatabaseHelper databaseHelper;
    public static ImageView btnBack;
    public static TextView tvSubjectName;
    public static TextView tvSubjectPictures;
    public static Button btnAddImage;
    public static ImageView btnDeleteSubject;
    public static ImageView btnImportImage;
    public static RecyclerView recyclerPictures;
    private Uri imageUri;
    private static final int CAMERA_REQUEST_CODE = 1888;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    public static int subjectPosition;
    public static ArrayList<ImageData> imageDataList;
    public static boolean isImportAllowed = true;
    public static boolean isDeleteAllowed = true;
    private static final int REQUEST_CODE_SELECT_IMAGE = 1;
    public static ImageAdapter imageAdapter;


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
        recyclerPictures = findViewById(R.id.recyclerPictures);

//        gridPictures.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                int imageId = (int) view.getTag();
//                Intent intent = new Intent(SubjectViewActivity.this, ImageViewActivity.class);
//                intent.putExtra("imageId", imageId);
//                intent.putExtra("subjectId", subjectID);
//                intent.putExtra("imagePosition", i);
//
//                System.out.println(imageId + " " + subjectID + " " + i);
//                startActivity(intent);
//            }
//        });

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

        if(isImportAllowed){
            btnImportImage.setVisibility(View.VISIBLE);
        }
        else {
            btnImportImage.setVisibility(View.INVISIBLE);
        }

        if(isDeleteAllowed) {
            btnDeleteSubject.setVisibility(View.VISIBLE);
        }
        else {
            btnDeleteSubject.setVisibility(View.INVISIBLE);
        }

        databaseHelper = new DatabaseHelper(SubjectViewActivity.this);
        subjectID = databaseHelper.getIDBySubjectName(subjectName);


        new LoadImagesTask().execute();
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
                new ProcessImageTask(SubjectViewActivity.this).execute(imageUri);
            }

        }
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            if (imageUri != null) {
                new ProcessImageTask(SubjectViewActivity.this).execute(imageUri);
            }
        }
    }

    private class ProcessImageTask extends AsyncTask<Uri, Void, byte[]> {
        private Context context;
        private ProgressDialog progressDialog;

        public ProcessImageTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Processing image...");
            progressDialog.show();
        }

        @Override
        protected byte[] doInBackground(Uri... uris) {
            Uri imageUri = uris[0];
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                // Check and correct the orientation of the image
                Bitmap orientedBitmap = correctImageOrientation(context, imageUri, bitmap);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                orientedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                return stream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        private Bitmap correctImageOrientation(Context context, Uri photoUri, Bitmap bitmap) {
            try {
                ExifInterface exif = new ExifInterface(context.getContentResolver().openInputStream(photoUri));
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                Matrix matrix = new Matrix();
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        matrix.postRotate(90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        matrix.postRotate(180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        matrix.postRotate(270);
                        break;
                    default:
                        return bitmap;
                }

                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(byte[] byteArray) {
            super.onPostExecute(byteArray);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if (byteArray != null) {
                DatabaseHelper databaseHelper = new DatabaseHelper(context);
                int id = databaseHelper.saveImage(databaseHelper.getIDBySubjectName(subjectName), byteArray, new Date());

                ImageData newImage = new ImageData(byteArray, id);
                ((ImageAdapter)recyclerPictures.getAdapter()).insertImage(newImage, id);

                Toasty.success(context, "Image Added", Toast.LENGTH_SHORT, true).show();
                SubjectsFragment.updateSubjectPictures(context, subjectPosition, ((ImageAdapter)recyclerPictures.getAdapter()).getItemCount());

                String pluralHandler = imageDataList.size() == 1 ? " Picture" : "Pictures";
                tvSubjectPictures.setText(imageDataList.size() + " " + pluralHandler);
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

    private class LoadImagesTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(SubjectViewActivity.this);
            progressDialog.setMessage("Loading images...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            databaseHelper = new DatabaseHelper(SubjectViewActivity.this);
            Cursor cursor = databaseHelper.getAllImages(subjectID);
            imageDataList = new ArrayList<>();
            List<Integer> imageIds = new ArrayList<>();

            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") byte[] byteArray = cursor.getBlob(cursor.getColumnIndex(COLUMN_IMAGE_DATA));
                    @SuppressLint("Range") int imageId = cursor.getInt(cursor.getColumnIndex(COLUMN_IMAGE_ID));
                    imageDataList.add(new ImageData(byteArray, imageId));
                    imageIds.add(imageId);
                } while (cursor.moveToNext());
            }
            cursor.close();

            ImageAdapter.tempImageIds = imageIds;

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            imageAdapter = new ImageAdapter(SubjectViewActivity.this, imageDataList, ImageAdapter.tempImageIds);
            imageAdapter.setOnAllImagesLoadedListener(new ImageAdapter.OnAllImagesLoadedListener() {
                @Override
                public void onAllImagesLoaded() {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }
            });

            if(ImageAdapter.tempImageIds.size() == 0) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

            recyclerPictures.setAdapter(imageAdapter);

            String pluralHandler = imageAdapter.getItemCount() == 1 ? " Picture" : "Pictures";
            tvSubjectPictures.setText(imageAdapter.getItemCount() + " " + pluralHandler);
        }
    }
}