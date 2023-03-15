package com.example.enotes;

import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "eNotes";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_SUBJECTS = "Subjects";
    private static final String COLUMN_SUBJECT_ID = "subject_id";
    private static final String COLUMN_SUBJECT_NAME = "subject_name";
    private static final String COLUMN_SUBJECT_SCHEDULE = "subject_schedule";
    private static final String COLUMN_SUBJECT_COLOR = "subject_color";

    private static final String TABLE_IMAGES = "Images";
    public static final String COLUMN_IMAGE_ID = "image_id";
    public static final String COLUMN_IMAGE_DATA = "image_data";
    private static final String COLUMN_IMAGE_DATE_TAKEN = "image_date_taken";
    private static final String FK_COLUMN_SUBJECT_ID = "subject_id";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createSubjectsTableQuery =
                "CREATE TABLE " + TABLE_SUBJECTS + " ("
                        + COLUMN_SUBJECT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_SUBJECT_NAME + " TEXT NOT NULL, "
                        + COLUMN_SUBJECT_SCHEDULE + " TEXT NOT NULL, "
                        + COLUMN_SUBJECT_COLOR + " INTEGER NOT NULL)";

        sqLiteDatabase.execSQL(createSubjectsTableQuery);

        String createImagesTableQuery =
                "CREATE TABLE " + TABLE_IMAGES + " ("
                        + COLUMN_IMAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_IMAGE_DATA + " BLOB NOT NULL, "
                        + COLUMN_IMAGE_DATE_TAKEN + " TEXT NOT NULL, "
                        + FK_COLUMN_SUBJECT_ID + " INTEGER NOT NULL, "
                        + "FOREIGN KEY (" + FK_COLUMN_SUBJECT_ID + ") REFERENCES " + TABLE_SUBJECTS + "(" + COLUMN_SUBJECT_ID + "))";

        sqLiteDatabase.execSQL(createImagesTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBJECTS);

        onCreate(sqLiteDatabase);
    }

    public void AddSubject(String subjectName, String subjectSched, int subjectColor)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COLUMN_SUBJECT_NAME, subjectName);
        values.put(COLUMN_SUBJECT_SCHEDULE, subjectSched);
        values.put(COLUMN_SUBJECT_COLOR, String.valueOf(subjectColor));

        long newRowId = db.insert(TABLE_SUBJECTS, null, values);

        db.close();
    }

    public void deleteSubject(int subjectId) {
        SQLiteDatabase db = getWritableDatabase();
        String[] args = {String.valueOf(subjectId)};
        db.delete(TABLE_IMAGES, FK_COLUMN_SUBJECT_ID + "=?", args);
        db.delete(TABLE_SUBJECTS, COLUMN_SUBJECT_ID + "=?", args);
        db.close();
    }

    public Cursor getAllSubjects()
    {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM Subjects ORDER BY subject_id ASC ";

        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor;
    }

    public Cursor getSubject(String subjectName)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM Subjects WHERE subject_name = '" + subjectName + "'";

        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor;
    }

    public boolean isNameUnique(String subjectName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_SUBJECTS +
                " WHERE " + COLUMN_SUBJECT_NAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{subjectName});
        boolean isUnique = cursor.getCount() == 0;
        cursor.close();
        return isUnique;
    }

    public void saveImage(int subjectID, byte[] byteArray, Date date)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IMAGE_DATA, byteArray);
        values.put(COLUMN_IMAGE_DATE_TAKEN, date.getTime());
        values.put(FK_COLUMN_SUBJECT_ID, subjectID);
        db.insert(TABLE_IMAGES, null, values);
        db.close();
    }

    public void saveImageToLocalDisc(Context context, byte[] imageData) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "IMG_" + timeStamp + ".jpg";
        File imageFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageName);

        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            outputStream.write(imageData);
            outputStream.flush();
            outputStream.close();

            // Add the image to the MediaStore database
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, imageName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                OutputStream os = context.getContentResolver().openOutputStream(uri);
                os.write(imageData);
                os.close();
            } else {
                MediaScannerConnection.scanFile(context,
                        new String[]{imageFile.getAbsolutePath()},
                        new String[]{"image/jpeg"}, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteImage(int imageId) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = COLUMN_IMAGE_ID + " = ?";
        String[] whereArgs = {String.valueOf(imageId)};
        db.delete(TABLE_IMAGES, whereClause, whereArgs);
    }

    @SuppressLint("Range")
    public byte[] loadImage(int imageId) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {COLUMN_IMAGE_DATA};
        String selection = COLUMN_IMAGE_ID + "=?";
        String[] selectionArgs = {String.valueOf(imageId)};
        Cursor cursor = db.query(TABLE_IMAGES, projection, selection, selectionArgs, null, null, null);
        byte[] imageData = null;
        if (cursor.moveToFirst()) {
            imageData = cursor.getBlob(cursor.getColumnIndex(COLUMN_IMAGE_DATA));
        }
        cursor.close();
        db.close();
        return imageData;
    }

    public int getIDBySubjectName(String subjectName) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {COLUMN_SUBJECT_ID};
        String selection = COLUMN_SUBJECT_NAME + " = ?";
        String[] selectionArgs = {subjectName};
        Cursor cursor = db.query(TABLE_SUBJECTS, projection, selection, selectionArgs, null, null, null);
        int subjectId = -1;
        if (cursor.moveToFirst()) {
            subjectId = cursor.getInt(0);
        }
        cursor.close();
        return subjectId;
    }

    public Cursor getAllImages(int subjectID) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {COLUMN_IMAGE_ID, COLUMN_IMAGE_DATA};
        String selection = COLUMN_SUBJECT_ID + " = ?";
        String[] selectionArgs = {String.valueOf(subjectID)};
        String sortOrder = COLUMN_IMAGE_ID + " DESC";
        Cursor cursor = db.query(TABLE_IMAGES, projection, selection, selectionArgs, null, null, sortOrder);
        return cursor;
    }

    public int getNumImagesForSubject(int subjectId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                "COUNT(*) AS total_rows"
        };

        String selection = FK_COLUMN_SUBJECT_ID + " = ?";

        String[] selectionArgs = {
                String.valueOf(subjectId)
        };

        Cursor cursor = db.query(
                TABLE_IMAGES,           // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null                    // don't specify a sort order
        );

        int numRows = 0;

        if (cursor.moveToFirst()) {
            numRows = cursor.getInt(cursor.getColumnIndexOrThrow("total_rows"));
        }

        cursor.close();

        return numRows;
    }
}
