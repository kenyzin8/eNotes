package com.kentj.enotes;

import static android.content.ContentValues.TAG;
import static com.kentj.enotes.DatabaseHelper.COLUMN_IMAGE_DATA;
import static com.kentj.enotes.DatabaseHelper.COLUMN_IMAGE_ID;

import androidx.annotation.NonNull;
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
import android.app.Dialog;
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
import android.graphics.drawable.ColorDrawable;
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
import android.view.LayoutInflater;
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

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

import es.dmoral.toasty.Toasty;

public class SubjectViewActivity extends AppCompatActivity {

    private final int CAMERA_REQUEST_CODE = 1888;
    private final int REQUEST_CAMERA_PERMISSION = 1;
    private final int REQUEST_CODE_SELECT_IMAGE = 1;
    private String subjectName;
    private int subjectID;
    private DatabaseHelper databaseHelper;
    private TextView tvSubjectPictures;
    private RecyclerView recyclerPictures;
    private Uri imageUri;
    private int subjectPosition;
    private ArrayList<ImageData> imageDataList;
    public static ImageAdapter imageAdapter;
    private AdView subjectAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_view);

        Intent intent = getIntent();
        if (intent != null) {
            subjectName = intent.getStringExtra("subjectName");
            setSubjectPosition(intent.getIntExtra("subjectPosition", 0));
        }

        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvSubjectName = findViewById(R.id.tvSubjectName);
        tvSubjectPictures = findViewById(R.id.tvPicturesView);
        Button btnAddImage = findViewById(R.id.btnAddImage);
        ImageView btnDeleteSubject = findViewById(R.id.btnDeleteSubject);
        ImageView btnImportImage = findViewById(R.id.btnImportImage);
        recyclerPictures = findViewById(R.id.recyclerPictures);

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
                View customDialogView = LayoutInflater.from(SubjectViewActivity.this).inflate(R.layout.delete_subject_dialog, null);
                android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(SubjectViewActivity.this)
                        .setView(customDialogView)
                        .create();
                Button yesButton = customDialogView.findViewById(R.id.dialogYesButtonDelete);
                Button noButton = customDialogView.findViewById(R.id.dialogNoButtonDelete);
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        databaseHelper = new DatabaseHelper(SubjectViewActivity.this);
                        databaseHelper.deleteSubject(subjectID);
                        finish();
                        SubjectsFragment.removeSubject(SubjectViewActivity.this, subjectPosition);
                        Toasty.success(SubjectViewActivity.this, "Subject has been deleted.", Toasty.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });
        btnImportImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImageFromGallery();
            }
        });

        databaseHelper = new DatabaseHelper(SubjectViewActivity.this);
        setSubjectID(databaseHelper.getIDBySubjectName(subjectName));

        subjectAdView = findViewById(R.id.homeAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        subjectAdView.loadAd(adRequest);

        subjectAdView.setAdListener(new AdListener() {
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
        private Dialog customDialog;

        public ProcessImageTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            customDialog = new Dialog(context);
            customDialog.setContentView(R.layout.custom_progress_dialog);
            customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            customDialog.setCancelable(false);
            customDialog.show();
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
            if (customDialog != null && customDialog.isShowing()) {
                customDialog.dismiss();
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
        private Dialog customDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            customDialog = new Dialog(SubjectViewActivity.this);
            customDialog.setContentView(R.layout.custom_progress_dialog);
            TextView tvProcessing = customDialog.findViewById(R.id.tvProcessing);
            tvProcessing.setText("Loading images...");
            customDialog.setCancelable(false);
            customDialog.show();
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

            imageAdapter = new ImageAdapter(SubjectViewActivity.this, SubjectViewActivity.this, imageDataList, ImageAdapter.tempImageIds);
            imageAdapter.setOnAllImagesLoadedListener(new ImageAdapter.OnAllImagesLoadedListener() {
                @Override
                public void onAllImagesLoaded() {
                    if (customDialog != null && customDialog.isShowing()) {
                        customDialog.dismiss();
                    }
                }
            });

            if(ImageAdapter.tempImageIds.size() == 0) {
                if (customDialog != null && customDialog.isShowing()) {
                    customDialog.dismiss();
                }
            }

            recyclerPictures.setAdapter(imageAdapter);

            String pluralHandler = imageAdapter.getItemCount() == 1 ? " Picture" : "Pictures";
            tvSubjectPictures.setText(imageAdapter.getItemCount() + " " + pluralHandler);
        }
    }

    public void setTvSubjectPictures(String txt) {
        this.tvSubjectPictures.setText(txt);
    }

    public int getSubjectID() {
        return subjectID;
    }

    public void setSubjectID(int subjectID) {
        this.subjectID = subjectID;
    }

    public int getSubjectPosition() {
        return subjectPosition;
    }

    public void setSubjectPosition(int subjectPosition) {
        this.subjectPosition = subjectPosition;
    }

    public ArrayList<ImageData> getImageDataList() {
        return imageDataList;
    }
}