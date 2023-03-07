package com.example.enotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

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
    private static final String COLUMN_IMAGE_ID = "image_id";
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
                        + COLUMN_IMAGE_DATA + " TEXT NOT NULL, "
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

    public void saveImage(int subjectID, byte[] byteArray, Date date)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IMAGE_DATA, byteArray);
        values.put(COLUMN_IMAGE_DATE_TAKEN, date.getTime());
        values.put(FK_COLUMN_SUBJECT_ID, subjectID);
        System.out.println(subjectID);
        db.insert(TABLE_IMAGES, null, values);
        db.close();
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
        String[] projection = {COLUMN_IMAGE_DATA};
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
