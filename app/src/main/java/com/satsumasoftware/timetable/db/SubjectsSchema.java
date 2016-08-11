package com.satsumasoftware.timetable.db;

public final class SubjectsSchema {

    public static final String TABLE_NAME = "subjects";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";

    protected static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            COL_ID + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_NAME + SchemaUtilsKt.TEXT_TYPE +
            " )";

    protected final String SQL_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
