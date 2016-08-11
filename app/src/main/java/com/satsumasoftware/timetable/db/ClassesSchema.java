package com.satsumasoftware.timetable.db;

public final class ClassesSchema {

    public static final String TABLE_NAME = "classes";
    public static final String COL_ID = "id";
    public static final String COL_SUBJECT_ID = "subject_id";
    public static final String COL_DAY = "day";
    public static final String COL_START_TIME = "start_time";
    public static final String COL_END_TIME = "end_time";
    public static final String COL_ROOM = "room";
    public static final String COL_TEACHER = "teacher";

    protected static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            COL_ID + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_SUBJECT_ID + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_DAY + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_START_TIME + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_END_TIME + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_ROOM + SchemaUtilsKt.TEXT_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_TEACHER + SchemaUtilsKt.TEXT_TYPE +
            " )";

    protected static final String SQL_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
