package com.satsumasoftware.timetable.db;

import android.provider.BaseColumns;

import com.satsumasoftware.timetable.db.util.SchemaUtilsKt;

public final class ClassesSchema implements BaseColumns {

    public static final String TABLE_NAME = "classes";
    public static final String COL_TIMETABLE_ID = "timetable_id";
    public static final String COL_SUBJECT_ID = "subject_id";
    public static final String COL_MODULE_NAME = "module_name";

    protected static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            _ID + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_TIMETABLE_ID + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_SUBJECT_ID + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_MODULE_NAME + SchemaUtilsKt.TEXT_TYPE +
            " )";

    protected static final String SQL_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
