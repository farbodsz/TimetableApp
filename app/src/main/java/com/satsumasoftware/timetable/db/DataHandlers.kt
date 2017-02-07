package com.satsumasoftware.timetable.db

import android.database.Cursor
import com.satsumasoftware.timetable.framework.*

object DataHandlers {

    class AssignmentDataHandler : DataHandler<Assignment> {
        override fun createFromCursor(cursor: Cursor) = Assignment.from(cursor)
    }

    class ClassDataHandler : DataHandler<Class> {
        override fun createFromCursor(cursor: Cursor) = Class.from(cursor)
    }

    class ClassDetailDataHandler : DataHandler<ClassDetail> {
        override fun createFromCursor(cursor: Cursor) = ClassDetail(cursor) // TODO use static factory
    }

    class ClassTimeDataHandler : DataHandler<ClassTime> {
        override fun createFromCursor(cursor: Cursor) = ClassTime(cursor) // TODO use static factory
    }

    class ExamDataHandler : DataHandler<Exam> {
        override fun createFromCursor(cursor: Cursor) = Exam.from(cursor)
    }

    class SubjectDataHandler : DataHandler<Subject> {
        override fun createFromCursor(cursor: Cursor) = Subject.from(cursor)
    }

    class TermDataHandler : DataHandler<Term> {
        override fun createFromCursor(cursor: Cursor) = Term.from(cursor)
    }

    class TimetableDataHandler : DataHandler<Timetable> {
        override fun createFromCursor(cursor: Cursor) = Timetable.from(cursor)
    }

}
