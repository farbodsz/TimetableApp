package com.satsumasoftware.timetable.db;

import android.provider.BaseColumns;

public final class SubjectsSchema implements BaseColumns {

    public static final String TABLE_NAME = "subjects";
    public static final String COL_NAME = "name";

    protected static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            _ID + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.PRIMARY_KEY_AUTOINCREMENT + SchemaUtilsKt.COMMA_SEP +
            COL_NAME + SchemaUtilsKt.TEXT_TYPE +
            " )";

    protected static final String SQL_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
