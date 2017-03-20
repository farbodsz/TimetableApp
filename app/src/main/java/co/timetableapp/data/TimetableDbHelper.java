package co.timetableapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.threeten.bp.LocalDate;

import co.timetableapp.data.schema.AssignmentsSchema;
import co.timetableapp.data.schema.ClassDetailsSchema;
import co.timetableapp.data.schema.ClassTimesSchema;
import co.timetableapp.data.schema.ClassesSchema;
import co.timetableapp.data.schema.ExamsSchema;
import co.timetableapp.data.schema.SqlHelperKt;
import co.timetableapp.data.schema.SubjectsSchema;
import co.timetableapp.data.schema.TermsSchema;
import co.timetableapp.data.schema.TimetablesSchema;
import co.timetableapp.framework.Class;

public final class TimetableDbHelper extends SQLiteOpenHelper {

    private static TimetableDbHelper sInstance;

    private static final int DATABASE_VERSION = 3;
    static final String DATABASE_NAME = "Timetable.db";

    private static final String LOG_TAG = "TimetableDbHelper";

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
        Log.i(LOG_TAG, "onCreate() called");

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
        Log.i(LOG_TAG, "onUpgrade() called with oldVersion " + oldVersion +
                " and newVersion " + newVersion);

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
                        SubjectsSchema.COL_ABBREVIATION + SqlHelperKt.TEXT_TYPE + " DEFAULT ''");

            case 2:
                LocalDate defaultDate = Class.NO_DATE;

                db.execSQL("ALTER TABLE " + ClassesSchema.TABLE_NAME + " ADD COLUMN " +
                        ClassesSchema.COL_START_DATE_DAY_OF_MONTH + SqlHelperKt.INTEGER_TYPE +
                        " DEFAULT " + defaultDate.getDayOfMonth());
                db.execSQL("ALTER TABLE " + ClassesSchema.TABLE_NAME + " ADD COLUMN " +
                        ClassesSchema.COL_START_DATE_MONTH + SqlHelperKt.INTEGER_TYPE +
                        " DEFAULT " + defaultDate.getMonthValue());
                db.execSQL("ALTER TABLE " + ClassesSchema.TABLE_NAME + " ADD COLUMN " +
                        ClassesSchema.COL_START_DATE_YEAR + SqlHelperKt.INTEGER_TYPE +
                        " DEFAULT " + defaultDate.getYear());

                db.execSQL("ALTER TABLE " + ClassesSchema.TABLE_NAME + " ADD COLUMN " +
                        ClassesSchema.COL_END_DATE_DAY_OF_MONTH + SqlHelperKt.INTEGER_TYPE +
                        " DEFAULT " + defaultDate.getDayOfMonth());
                db.execSQL("ALTER TABLE " + ClassesSchema.TABLE_NAME + " ADD COLUMN " +
                        ClassesSchema.COL_END_DATE_MONTH + SqlHelperKt.INTEGER_TYPE +
                        " DEFAULT " + defaultDate.getMonthValue());
                db.execSQL("ALTER TABLE " + ClassesSchema.TABLE_NAME + " ADD COLUMN " +
                        ClassesSchema.COL_END_DATE_YEAR + SqlHelperKt.INTEGER_TYPE +
                        " DEFAULT " + defaultDate.getYear());
                break;

            default:
                throw new IllegalArgumentException("onUpgrade() called with unknown oldVersion "
                        + oldVersion);
        }
    }

}
