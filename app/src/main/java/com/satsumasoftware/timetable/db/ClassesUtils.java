package com.satsumasoftware.timetable.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.framework.ClassDetail;
import com.satsumasoftware.timetable.framework.ClassTime;

import java.util.ArrayList;

public final class ClassesUtils {

    public static ArrayList<Integer> getClassDetailIds(Context context, int classId) {
        TimetableDbHelper dbHelper = TimetableDbHelper.getInstance(context);

        Cursor cursor = dbHelper.getReadableDatabase().query(
                ClassDetailsMapSchema.TABLE_NAME,
                null,
                ClassDetailsMapSchema.COL_CLASS_ID + "=?",
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
                ClassDetailTimesMapSchema.COL_CLASS_DETAIL_ID + "=?",
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

    public static int getHighestClassId(Context context) {
        SQLiteDatabase db = TimetableDbHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(
                ClassesSchema.TABLE_NAME, null, null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public static void addClass(Context context, Class cls) {
        ContentValues values = new ContentValues();
        values.put(ClassesSchema._ID, cls.getId());
        values.put(ClassesSchema.COL_SUBJECT_ID, cls.getSubjectId());

        SQLiteDatabase db = TimetableDbHelper.getInstance(context).getWritableDatabase();
        db.insert(ClassTimesSchema.TABLE_NAME, null, values);
    }

    public static void deleteClass(Context context, int classId) {
        SQLiteDatabase db = TimetableDbHelper.getInstance(context).getWritableDatabase();
        db.delete(ClassesSchema.TABLE_NAME,
                ClassesSchema._ID + "=?",
                new String[] {String.valueOf(classId)});
    }

    public static void replaceClass(Context context, int oldClassId, Class newClass) {
        deleteClass(context, oldClassId);
        addClass(context, newClass);
    }

    public static int getHighestClassDetailId(Context context) {
        SQLiteDatabase db = TimetableDbHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(
                ClassDetailsSchema.TABLE_NAME, null, null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public static void addClassDetail(Context context, ClassDetail classDetail) {
        ContentValues values = new ContentValues();
        values.put(ClassDetailsSchema._ID, classDetail.getId());
        values.put(ClassDetailsSchema.COL_ROOM, classDetail.getRoom());
        values.put(ClassDetailsSchema.COL_TEACHER, classDetail.getTeacher());

        SQLiteDatabase db = TimetableDbHelper.getInstance(context).getWritableDatabase();
        db.insert(ClassDetailsSchema.TABLE_NAME, null, values);
    }

    public static void deleteClassDetail(Context context, int classDetailId) {
        SQLiteDatabase db = TimetableDbHelper.getInstance(context).getWritableDatabase();
        db.delete(ClassDetailsSchema.TABLE_NAME,
                ClassDetailsSchema._ID + "=?",
                new String[] {String.valueOf(classDetailId)});
    }

    public static void replaceClassDetail(Context context, int oldClassDetailId, ClassDetail newClassDetail) {
        deleteClassDetail(context, oldClassDetailId);
        addClassDetail(context, newClassDetail);
    }

    public static int getHighestClassTimeId(Context context) {
        SQLiteDatabase db = TimetableDbHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(
                ClassTimesSchema.TABLE_NAME, null, null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public static void addClassTime(Context context, ClassTime classTime) {
        ContentValues values = new ContentValues();
        values.put(ClassTimesSchema._ID, classTime.getId());
        values.put(ClassTimesSchema.COL_DAY, classTime.getDay().getValue());
        values.put(ClassTimesSchema.COL_START_TIME_HRS, classTime.getStartTime().getHour());
        values.put(ClassTimesSchema.COL_START_TIME_MINS, classTime.getStartTime().getMinute());
        values.put(ClassTimesSchema.COL_START_TIME_HRS, classTime.getEndTime().getHour());
        values.put(ClassTimesSchema.COL_START_TIME_MINS, classTime.getEndTime().getMinute());

        SQLiteDatabase db = TimetableDbHelper.getInstance(context).getWritableDatabase();
        db.insert(ClassTimesSchema.TABLE_NAME, null, values);
    }

    public static void deleteClassTime(Context context, int classTimeId) {
        SQLiteDatabase db = TimetableDbHelper.getInstance(context).getWritableDatabase();
        db.delete(ClassTimesSchema.TABLE_NAME,
                ClassTimesSchema._ID + "=?",
                new String[] {String.valueOf(classTimeId)});
    }

    public static void replaceClassTime(Context context, int oldClassTimeId, ClassTime newClassTime) {
        deleteClassTime(context, oldClassTimeId);
        addClassTime(context, newClassTime);
    }

    public static void addClassToDetailsLinks(Context context, int classId, ArrayList<Integer> classDetailIds) {
        for (int classDetail : classDetailIds) {
            ContentValues values = new ContentValues();
            values.put(ClassDetailsMapSchema.COL_CLASS_ID, classId);
            values.put(ClassDetailsMapSchema.COL_CLASS_DETAIL_ID, classDetail);

            SQLiteDatabase db = TimetableDbHelper.getInstance(context).getWritableDatabase();
            db.insert(ClassDetailsMapSchema.TABLE_NAME, null, values);
        }
    }

    public static void deleteClassToDetailsLinks(Context context, int classId) {
        SQLiteDatabase db = TimetableDbHelper.getInstance(context).getWritableDatabase();
        db.delete(ClassDetailsMapSchema.TABLE_NAME,
                ClassDetailsMapSchema.COL_CLASS_ID + "=?",
                new String[] {String.valueOf(classId)});
    }

    public static void replaceClassToDetailsLinks(Context context, int classId, ArrayList<Integer> classDetailIds) {
        deleteClassToDetailsLinks(context, classId);
        addClassToDetailsLinks(context, classId, classDetailIds);
    }

    public static void addClassDetailToTimesLinks(Context context, int classDetailId, ArrayList<Integer> classTimeIds) {
        for (int classTimeId : classTimeIds) {
            ContentValues values = new ContentValues();
            values.put(ClassDetailTimesMapSchema.COL_CLASS_DETAIL_ID, classDetailId);
            values.put(ClassDetailTimesMapSchema.COL_CLASS_TIME_ID, classTimeId);

            SQLiteDatabase db = TimetableDbHelper.getInstance(context).getWritableDatabase();
            db.insert(ClassDetailTimesMapSchema.TABLE_NAME, null, values);
        }
    }

    public static void deleteClassDetailToTimesLinks(Context context, int classDetailId) {
        SQLiteDatabase db = TimetableDbHelper.getInstance(context).getWritableDatabase();
        db.delete(ClassDetailTimesMapSchema.TABLE_NAME,
                ClassDetailTimesMapSchema.COL_CLASS_DETAIL_ID + "=?",
                new String[] {String.valueOf(classDetailId)});
    }

    public static void replaceClassDetailToTimesLinks(Context context, int classDetailId, ArrayList<Integer> classTimeIds) {
        deleteClassDetailToTimesLinks(context, classDetailId);
        addClassDetailToTimesLinks(context, classDetailId, classTimeIds);
    }
}
