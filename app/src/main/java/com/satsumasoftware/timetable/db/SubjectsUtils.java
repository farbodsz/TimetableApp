package com.satsumasoftware.timetable.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.framework.Subject;

import java.util.ArrayList;

public final class SubjectsUtils {

    private static final String LOG_TAG = "SubjectUtils";

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
        Log.i(LOG_TAG, "Added Subject with id " + subject.getId());
    }

    private static void deleteSubject(Context context, int subjectId) {
        SQLiteDatabase db = TimetableDbHelper.getInstance(context).getWritableDatabase();
        db.delete(SubjectsSchema.TABLE_NAME,
                SubjectsSchema._ID + "=?",
                new String[] {String.valueOf(subjectId)});
        Log.i(LOG_TAG, "Deleted Subject with id " + subjectId);
    }

    private static void deleteClassesWithSubject(Context context, int subjectId) {
        SQLiteDatabase db = TimetableDbHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(
                ClassesSchema.TABLE_NAME,
                null,
                ClassesSchema.COL_SUBJECT_ID + "=?",
                new String[] {String.valueOf(subjectId)},
                null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Class cls = new Class(context, cursor);
            ClassesUtils.completelyDeleteClass(context, cls);
            cursor.moveToNext();
        }
        cursor.close();
        Log.i(LOG_TAG, "Deleted all Class-related entries associated with Subject of id " +
                subjectId + ")");
    }

    public static void completelyDeleteSubject(Context context, int subjectId) {
        Log.i(LOG_TAG, "Deleting everything related to Subject of id " + subjectId);

        deleteClassesWithSubject(context, subjectId);
        deleteSubject(context, subjectId);
    }

    public static void replaceSubject(Context context, int oldSubjectId, Subject newSubject) {
        Log.i(LOG_TAG, "Replacing Subject...");
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
