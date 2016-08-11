package com.satsumasoftware.timetable.db;

import android.provider.BaseColumns;

public final class ClassDetailsMapSchema implements BaseColumns {

    public static final String TABLE_NAME = "classes";
    public static final String COL_CLASS_DETAIL_ID = "class_detail_id";

    protected static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            _ID + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.PRIMARY_KEY_AUTOINCREMENT + SchemaUtilsKt.COMMA_SEP +
            COL_CLASS_DETAIL_ID + SchemaUtilsKt.INTEGER_TYPE +
            " )";

    protected static final String SQL_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
