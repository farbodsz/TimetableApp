package com.satsumasoftware.timetable.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.satsumasoftware.timetable.framework.Assignment;

import java.util.ArrayList;

public final class AssignmentsUtils {

    private static final String LOG_TAG = "AssignmentsUtils";

    public static ArrayList<Assignment> getAssignments(Context context) {
        ArrayList<Assignment> assignments = new ArrayList<>();
        TimetableDbHelper dbHelper = TimetableDbHelper.getInstance(context);
        Cursor cursor = dbHelper.getReadableDatabase().query(
                AssignmentsSchema.TABLE_NAME, null, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            assignments.add(new Assignment(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return assignments;
    }

    public static void addAssignment(Context context, Assignment assignment) {
        ContentValues values = new ContentValues();
        values.put(AssignmentsSchema._ID, assignment.getId());
        values.put(AssignmentsSchema.COL_CLASS_ID, assignment.getClassId());
        values.put(AssignmentsSchema.COL_TITLE, assignment.getTitle());
        values.put(AssignmentsSchema.COL_DETAIL, assignment.getDetail());
        values.put(AssignmentsSchema.COL_DUE_DATE_DAY_OF_MONTH, assignment.getDueDate().getDayOfMonth());
        values.put(AssignmentsSchema.COL_DUE_DATE_MONTH, assignment.getDueDate().getMonthValue());
        values.put(AssignmentsSchema.COL_DUE_DATE_YEAR, assignment.getDueDate().getYear());
        values.put(AssignmentsSchema.COL_COMPLETION_PROGRESS, assignment.getCompletionProgress());

        SQLiteDatabase db = TimetableDbHelper.getInstance(context).getWritableDatabase();
        db.insert(AssignmentsSchema.TABLE_NAME, null, values);
        Log.i(LOG_TAG, "Added Assignment with id " + assignment.getId());
    }

    public static void deleteAssignment(Context context, int assignmentId) {
        SQLiteDatabase db = TimetableDbHelper.getInstance(context).getWritableDatabase();
        db.delete(AssignmentsSchema.TABLE_NAME,
                AssignmentsSchema._ID + "=?",
                new String[] {String.valueOf(assignmentId)});
        Log.i(LOG_TAG, "Deleted Assignment with id " + assignmentId);
    }

    public static void replaceAssignment(Context context, int oldAssignmentId, Assignment newAssignment) {
        Log.i(LOG_TAG, "Replacing Assignment...");
        deleteAssignment(context, oldAssignmentId);
        addAssignment(context, newAssignment);
    }

    public static int getHighestAssignmentId(Context context) {
        SQLiteDatabase db = TimetableDbHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(
                AssignmentsSchema.TABLE_NAME, null, null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

}
