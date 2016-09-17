package com.satsumasoftware.timetable.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.satsumasoftware.timetable.db.util.SchemaUtilsKt;

public final class TimetableDbHelper extends SQLiteOpenHelper {

    private static TimetableDbHelper sInstance;

    private static final int DATABASE_VERSION = 2;
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
        db.execSQL(AssignmentsSchema.SQL_CREATE);
        db.execSQL(ClassDetailsSchema.SQL_CREATE);
        db.execSQL(ClassesSchema.SQL_CREATE);
        db.execSQL(ClassTimesSchema.SQL_CREATE);
        db.execSQL(ExamsSchema.SQL_CREATE);
        db.execSQL(SubjectsSchema.SQL_CREATE);
        db.execSQL(TermsSchema.SQL_CREATE);
        db.execSQL(TimetablesSchema.SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("TimetableDbHelper", "onUpgrade() called with oldVersion " + oldVersion +
                " and new version " + newVersion);

        /*
         * Note that there are no breaks in the switch statement below (except for the last case).
         *
         * This means, for example, if installed version is 2 onUpgrade will get called with
         * oldVersion as 2. The switch case will execute cases 2, 3, 4, etc as the break
         * statements are missing in all the older cases.
         */

        switch (oldVersion) {
            case 1:
                db.execSQL("ALTER TABLE " + SubjectsSchema.TABLE_NAME + " ADD COLUMN " +
                        SubjectsSchema.COL_ABBREVIATION + SchemaUtilsKt.TEXT_TYPE + " DEFAULT ''");
                break;

            default:
                throw new IllegalArgumentException("onUpgrade() called with unknown oldVersion "
                        + oldVersion);
        }
    }

}
