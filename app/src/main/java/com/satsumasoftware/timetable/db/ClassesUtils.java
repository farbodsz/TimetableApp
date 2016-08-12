package com.satsumasoftware.timetable.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.satsumasoftware.timetable.framework.ClassDetail;
import com.satsumasoftware.timetable.framework.ClassTime;

import java.util.ArrayList;

public final class ClassesUtils {

    public static ArrayList<Integer> getClassDetailIds(Context context, int classId) {
        TimetableDbHelper dbHelper = TimetableDbHelper.getInstance(context);

        Cursor cursor = dbHelper.getReadableDatabase().query(
                ClassDetailsMapSchema.TABLE_NAME,
                null,
                ClassDetailsMapSchema._ID + "=?",
                new String[] {String.valueOf(classId)},
                null, null, null);
        cursor.moveToFirst();

        ArrayList<Integer> classDetailIds = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            classDetailIds.add(cursor.getInt(
                    cursor.getColumnIndex(ClassDetailsMapSchema.COL_CLASS_DETAIL_ID)));
            cursor.moveToNext();
        }
        cursor.close();

        return classDetailIds;
    }

    public static ArrayList<ClassDetail> getClassDetailsFromIds(Context context, ArrayList<Integer> classDetailIds) {
        TimetableDbHelper dbHelper = TimetableDbHelper.getInstance(context);
        ArrayList<ClassDetail> classDetails = new ArrayList<>();

        for (int classDetailId : classDetailIds) {
            Cursor cursor = dbHelper.getReadableDatabase().query(
                    ClassDetailsSchema.TABLE_NAME,
                    null,
                    ClassDetailsSchema._ID + "=?",
                    new String[]{String.valueOf(classDetailId)},
                    null, null, null);
            cursor.moveToFirst();
            String room = cursor.getString(cursor.getColumnIndex(ClassDetailsSchema.COL_ROOM));
            String teacher = cursor.getString(cursor.getColumnIndex(ClassDetailsSchema.COL_TEACHER));
            ArrayList<Integer> classTimeIds = getClassTimeIds(context, classDetailId);
            classDetails.add(new ClassDetail(classDetailId, room, teacher, classTimeIds));
            cursor.close();
        }

        return classDetails;
    }

    public static ArrayList<Integer> getClassTimeIds(Context context, int classDetailId) {
        TimetableDbHelper dbHelper = TimetableDbHelper.getInstance(context);

        Cursor cursor = dbHelper.getReadableDatabase().query(
                ClassDetailTimesMapSchema.TABLE_NAME,
                null,
                ClassDetailTimesMapSchema._ID + "=?",
                new String[] {String.valueOf(classDetailId)},
                null, null, null);
        cursor.moveToFirst();

        ArrayList<Integer> classTimeIds = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            classTimeIds.add(cursor.getInt(
                    cursor.getColumnIndex(ClassDetailTimesMapSchema.COL_CLASS_TIME_ID)));
            cursor.moveToNext();
        }
        cursor.close();

        return classTimeIds;
    }

    public static ArrayList<ClassTime> getClassTimesFromIds(Context context, ArrayList<Integer> classTimeIds) {
        TimetableDbHelper dbHelper = TimetableDbHelper.getInstance(context);
        ArrayList<ClassTime> classTimes = new ArrayList<>();

        for (int classTimeId : classTimeIds) {
            Cursor cursor = dbHelper.getReadableDatabase().query(
                    ClassTimesSchema.TABLE_NAME,
                    null,
                    ClassTimesSchema._ID,
                    new String[] {String.valueOf(classTimeId)},
                    null, null, null);
            cursor.moveToFirst();
            classTimes.add(new ClassTime(cursor));
            cursor.close();
        }

        return classTimes;
    }

    public static int getHighestClassTimeId(Context context) {
        SQLiteDatabase db = TimetableDbHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(
                ClassTimesSchema.TABLE_NAME, null, null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }
}
