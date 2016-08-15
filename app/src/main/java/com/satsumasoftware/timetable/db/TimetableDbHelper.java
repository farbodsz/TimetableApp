package com.satsumasoftware.timetable.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public final class TimetableDbHelper extends SQLiteOpenHelper {

    private static TimetableDbHelper sInstance;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Timetable.db";

    public static synchronized TimetableDbHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TimetableDbHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private TimetableDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ClassDetailsSchema.SQL_CREATE);
        db.execSQL(ClassesSchema.SQL_CREATE);
        db.execSQL(ClassTimesSchema.SQL_CREATE);
        db.execSQL(SubjectsSchema.SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(ClassDetailsSchema.SQL_DELETE);
        db.execSQL(ClassesSchema.SQL_DELETE);
        db.execSQL(ClassTimesSchema.SQL_DELETE);
        db.execSQL(SubjectsSchema.SQL_DELETE);
        onCreate(db);
    }

}
