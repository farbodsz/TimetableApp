package com.satsumasoftware.timetable.db;

public final class ClassDetailsMapSchema {

    public static final String TABLE_NAME = "class_details_map";
    public static final String COL_CLASS_ID = "class_id";
    public static final String COL_CLASS_DETAIL_ID = "class_detail_id";

    protected static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            COL_CLASS_ID + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_CLASS_DETAIL_ID + SchemaUtilsKt.INTEGER_TYPE +
            " )";

    protected static final String SQL_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
