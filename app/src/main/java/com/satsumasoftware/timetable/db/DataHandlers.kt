package com.satsumasoftware.timetable.db

import android.database.Cursor
import com.satsumasoftware.timetable.db.schema.*
import com.satsumasoftware.timetable.framework.*

object DataHandlers {

    @JvmField val ASSIGNMENTS = AssignmentDataHandler()
    @JvmField val CLASSES = ClassDataHandler()
    @JvmField val CLASS_DETAILS = ClassDetailDataHandler()
    @JvmField val CLASS_TIMES = ClassTimeDataHandler()
    @JvmField val EXAMS = ExamDataHandler()
    @JvmField val SUBJECTS = SubjectDataHandler()
    @JvmField val TERMS = TermDataHandler()
    @JvmField val TIMETABLES = TimetableDataHandler()

    class AssignmentDataHandler : DataHandler<Assignment> {

        override val tableName = AssignmentsSchema.TABLE_NAME

        override val itemIdCol = AssignmentsSchema._ID
        override val timetableIdCol = AssignmentsSchema.COL_TIMETABLE_ID

        override fun createFromCursor(cursor: Cursor) = Assignment.from(cursor)
    }

    class ClassDataHandler : DataHandler<Class> {

        override val tableName = ClassesSchema.TABLE_NAME

        override val itemIdCol = ClassesSchema._ID
        override val timetableIdCol = ClassesSchema.COL_TIMETABLE_ID

        override fun createFromCursor(cursor: Cursor) = Class.from(cursor)
    }

    class ClassDetailDataHandler : DataHandler<ClassDetail> {

        override val tableName = ClassDetailsSchema.TABLE_NAME

        override val itemIdCol = ClassDetailsSchema._ID
        override val timetableIdCol = "" // TODO ClassDetail doesn't have a timetableId

        override fun createFromCursor(cursor: Cursor) = ClassDetail(cursor) // TODO use static factory
    }

    class ClassTimeDataHandler : DataHandler<ClassTime> {

        override val tableName = ClassTimesSchema.TABLE_NAME

        override val itemIdCol = ClassTimesSchema._ID
        override val timetableIdCol = ClassTimesSchema.COL_TIMETABLE_ID

        override fun createFromCursor(cursor: Cursor) = ClassTime(cursor) // TODO use static factory
    }

    class ExamDataHandler : DataHandler<Exam> {

        override val tableName = ExamsSchema.TABLE_NAME

        override val itemIdCol = ExamsSchema._ID
        override val timetableIdCol = ExamsSchema.COL_TIMETABLE_ID

        override fun createFromCursor(cursor: Cursor) = Exam.from(cursor)
    }

    class SubjectDataHandler : DataHandler<Subject> {

        override val tableName = SubjectsSchema.TABLE_NAME

        override val itemIdCol = SubjectsSchema._ID
        override val timetableIdCol = SubjectsSchema.COL_TIMETABLE_ID

        override fun createFromCursor(cursor: Cursor) = Subject.from(cursor)
    }

    class TermDataHandler : DataHandler<Term> {

        override val tableName = TermsSchema.TABLE_NAME

        override val itemIdCol = TermsSchema._ID
        override val timetableIdCol = TermsSchema.COL_TIMETABLE_ID

        override fun createFromCursor(cursor: Cursor) = Term.from(cursor)
    }

    class TimetableDataHandler : DataHandler<Timetable> {

        override val tableName = TimetablesSchema.TABLE_NAME

        override val itemIdCol = TimetablesSchema._ID
        override val timetableIdCol = TimetablesSchema._ID

        override fun createFromCursor(cursor: Cursor) = Timetable.from(cursor)
    }

}
