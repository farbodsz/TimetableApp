package co.timetableapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;

import co.timetableapp.data.schema.AssignmentsSchema;
import co.timetableapp.data.schema.ClassDetailsSchema;
import co.timetableapp.data.schema.ClassTimesSchema;
import co.timetableapp.data.schema.ClassesSchema;
import co.timetableapp.data.schema.EventsSchema;
import co.timetableapp.data.schema.ExamsSchema;
import co.timetableapp.data.schema.SqlHelperKt;
import co.timetableapp.data.schema.SubjectsSchema;
import co.timetableapp.data.schema.TermsSchema;
import co.timetableapp.data.schema.TimetablesSchema;
import co.timetableapp.model.Class;

public final class TimetableDbHelper extends SQLiteOpenHelper {

    private static TimetableDbHelper sInstance;

    private static final int DATABASE_VERSION = 6;
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
        db.execSQL(EventsSchema.SQL_CREATE);
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
                // Add subject abbreviations
                db.execSQL("ALTER TABLE " + SubjectsSchema.TABLE_NAME + " ADD COLUMN " +
                        SubjectsSchema.COL_ABBREVIATION + SqlHelperKt.TEXT_TYPE + " DEFAULT ''");

            case 2:
                // Add start/end dates for classes
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

            case 3:
                // Rename the corrupt database
                String oldTableName = "classes_old";
                db.execSQL("ALTER TABLE " + ClassesSchema.TABLE_NAME + " RENAME TO " + oldTableName);

                // Go through the corrupt classes table and store rows into an ArrayList but
                // ignoring rows with duplicate id values.
                ArrayList<Integer> ids = new ArrayList<>();
                ArrayList<Class> classes = new ArrayList<>();

                Cursor cursor = db.query(oldTableName, null, null, null, null, null, null);
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    Class cls = Class.from(cursor);
                    if (!ids.contains(cls.getId())) {
                        ids.add(cls.getId());
                        classes.add(cls);
                    }
                    cursor.moveToNext();
                }
                Log.d(LOG_TAG, "Keeping " + ids.size() + " out of " + cursor.getCount() + " rows");
                cursor.close();

                // Create the new table and insert the stored rows
                db.execSQL(ClassesSchema.SQL_CREATE);
                Log.d(LOG_TAG, "Created a new classes table: " + ClassesSchema.TABLE_NAME);

                for (Class item : classes) {
                    ContentValues values = new ContentValues();
                    values.put(ClassesSchema._ID, item.getId());
                    values.put(ClassesSchema.COL_TIMETABLE_ID, item.getTimetableId());
                    values.put(ClassesSchema.COL_SUBJECT_ID, item.getSubjectId());
                    values.put(ClassesSchema.COL_MODULE_NAME, item.getModuleName());
                    values.put(ClassesSchema.COL_START_DATE_DAY_OF_MONTH,
                            item.getStartDate().getDayOfMonth());
                    values.put(ClassesSchema.COL_START_DATE_MONTH,
                            item.getStartDate().getMonthValue());
                    values.put(ClassesSchema.COL_START_DATE_YEAR, item.getStartDate().getYear());
                    values.put(ClassesSchema.COL_END_DATE_DAY_OF_MONTH,
                            item.getEndDate().getDayOfMonth());
                    values.put(ClassesSchema.COL_END_DATE_MONTH, item.getEndDate().getMonthValue());
                    values.put(ClassesSchema.COL_END_DATE_YEAR, item.getEndDate().getYear());

                    db.insert(ClassesSchema.TABLE_NAME, null, values);
                    Log.d(LOG_TAG, "Inserted class of id " + item.getId() + " to the new table");
                }

                db.execSQL("DROP TABLE " + oldTableName);
                Log.d(LOG_TAG, "Deleted the corrupt classes table: " + oldTableName);

            case 4:
                // Create the events table
                db.execSQL(EventsSchema.SQL_CREATE);

            case 5:
                // Add exam notes
                db.execSQL("ALTER TABLE " + ExamsSchema.TABLE_NAME + " ADD COLUMN " +
                        ExamsSchema.COL_NOTES + SqlHelperKt.TEXT_TYPE + " DEFAULT ''");
                break;

            default:
                throw new IllegalArgumentException("onUpgrade() called with unknown oldVersion "
                        + oldVersion);
        }
    }

}
