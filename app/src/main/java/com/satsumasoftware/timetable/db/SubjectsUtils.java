package com.satsumasoftware.timetable.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.satsumasoftware.timetable.framework.Subject;

import java.util.ArrayList;

public final class SubjectsUtils {

    public static ArrayList<Subject> getSubjects(Context context) {
        ArrayList<Subject> subjects = new ArrayList<>();
        TimetableDbHelper dbHelper = TimetableDbHelper.getInstance(context);
        Cursor cursor = dbHelper.getReadableDatabase().query(
                SubjectsSchema.TABLE_NAME, null, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            subjects.add(new Subject(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return subjects;
    }

    public static void addSubject(Context context, Subject subject) {
        ContentValues values = new ContentValues();
        values.put(SubjectsSchema._ID, subject.getId());
        values.put(SubjectsSchema.COL_NAME, subject.getName());

        SQLiteDatabase db = TimetableDbHelper.getInstance(context).getWritableDatabase();
        db.insert(SubjectsSchema.TABLE_NAME, null, values);
    }

    public static void deleteSubject(Context context, int subjectId) {
        SQLiteDatabase db = TimetableDbHelper.getInstance(context).getWritableDatabase();
        db.delete(SubjectsSchema.TABLE_NAME,
                SubjectsSchema._ID + "=?",
                new String[] {String.valueOf(subjectId)});
    }

    public static void replaceSubject(Context context, int oldSubjectId, Subject newSubject) {
        deleteSubject(context, oldSubjectId);
        addSubject(context, newSubject);
    }

    public static int getHighestSubjectId(Context context) {
        SQLiteDatabase db = TimetableDbHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(
                SubjectsSchema.TABLE_NAME, null, null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public static Subject getSubjectFromId(Context context, int id) {
        SQLiteDatabase db = TimetableDbHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(
                SubjectsSchema.TABLE_NAME,
                null,
                SubjectsSchema._ID + "=?",
                new String[] {String.valueOf(id)},
                null, null, null);
        cursor.moveToFirst();
        Subject subject = new Subject(cursor);
        cursor.close();
        return subject;
    }

}
