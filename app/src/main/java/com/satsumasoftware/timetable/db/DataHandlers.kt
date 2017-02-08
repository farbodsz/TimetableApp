package com.satsumasoftware.timetable.db

import android.content.ContentValues
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

    class AssignmentDataHandler : TimetableItemDataHandler<Assignment> {

        override val tableName = AssignmentsSchema.TABLE_NAME

        override val itemIdCol = AssignmentsSchema._ID
        override val timetableIdCol = AssignmentsSchema.COL_TIMETABLE_ID

        override fun createFromCursor(cursor: Cursor) = Assignment.from(cursor)

        override fun propertiesAsContentValues(item: Assignment): ContentValues {
            val values = ContentValues()
            with(values) {
                put(AssignmentsSchema._ID, item.id)
                put(AssignmentsSchema.COL_TIMETABLE_ID, item.timetableId)
                put(AssignmentsSchema.COL_CLASS_ID, item.classId)
                put(AssignmentsSchema.COL_TITLE, item.title)
                put(AssignmentsSchema.COL_DETAIL, item.detail)
                put(AssignmentsSchema.COL_DUE_DATE_DAY_OF_MONTH, item.dueDate.dayOfMonth)
                put(AssignmentsSchema.COL_DUE_DATE_MONTH, item.dueDate.monthValue)
                put(AssignmentsSchema.COL_DUE_DATE_YEAR, item.dueDate.year)
                put(AssignmentsSchema.COL_COMPLETION_PROGRESS, item.completionProgress)
            }
            return values
        }
    }

    class ClassDataHandler : TimetableItemDataHandler<Class> {

        override val tableName = ClassesSchema.TABLE_NAME

        override val itemIdCol = ClassesSchema._ID
        override val timetableIdCol = ClassesSchema.COL_TIMETABLE_ID

        override fun createFromCursor(cursor: Cursor) = Class.from(cursor)

        override fun propertiesAsContentValues(item: Class): ContentValues {
            val values = ContentValues()
            with(values) {
                put(ClassesSchema._ID, item.id)
                put(ClassesSchema.COL_TIMETABLE_ID, item.timetableId)
                put(ClassesSchema.COL_SUBJECT_ID, item.subjectId)
                put(ClassesSchema.COL_MODULE_NAME, item.moduleName)
                put(ClassesSchema.COL_START_DATE_DAY_OF_MONTH, item.startDate.dayOfMonth)
                put(ClassesSchema.COL_START_DATE_MONTH, item.startDate.monthValue)
                put(ClassesSchema.COL_START_DATE_YEAR, item.startDate.year)
                put(ClassesSchema.COL_END_DATE_DAY_OF_MONTH, item.endDate.dayOfMonth)
                put(ClassesSchema.COL_END_DATE_MONTH, item.endDate.monthValue)
                put(ClassesSchema.COL_END_DATE_YEAR, item.endDate.year)
            }
            return values
        }
    }

    class ClassDetailDataHandler : DataHandler<ClassDetail> {

        override val tableName = ClassDetailsSchema.TABLE_NAME

        override val itemIdCol = ClassDetailsSchema._ID

        override fun createFromCursor(cursor: Cursor) = ClassDetail(cursor) // TODO use static factory

        override fun propertiesAsContentValues(item: ClassDetail): ContentValues {
            val values = ContentValues()
            with(values) {
                put(ClassDetailsSchema._ID, item.id)
                put(ClassDetailsSchema.COL_CLASS_ID, item.classId)
                put(ClassDetailsSchema.COL_ROOM, item.room)
                put(ClassDetailsSchema.COL_BUILDING, item.building)
                put(ClassDetailsSchema.COL_TEACHER, item.teacher)
            }
            return values
        }
    }

    class ClassTimeDataHandler : TimetableItemDataHandler<ClassTime> {

        override val tableName = ClassTimesSchema.TABLE_NAME

        override val itemIdCol = ClassTimesSchema._ID
        override val timetableIdCol = ClassTimesSchema.COL_TIMETABLE_ID

        override fun createFromCursor(cursor: Cursor) = ClassTime(cursor) // TODO use static factory

        override fun propertiesAsContentValues(item: ClassTime): ContentValues {
            val values = ContentValues()
            with(values) {
                put(ClassTimesSchema._ID, item.id)
                put(ClassTimesSchema.COL_TIMETABLE_ID, item.timetableId)
                put(ClassTimesSchema.COL_CLASS_DETAIL_ID, item.classDetailId)
                put(ClassTimesSchema.COL_DAY, item.day.value)
                put(ClassTimesSchema.COL_WEEK_NUMBER, item.weekNumber)
                put(ClassTimesSchema.COL_START_TIME_HRS, item.startTime.hour)
                put(ClassTimesSchema.COL_START_TIME_MINS, item.startTime.minute)
                put(ClassTimesSchema.COL_END_TIME_HRS, item.endTime.hour)
                put(ClassTimesSchema.COL_END_TIME_MINS, item.endTime.minute)
            }
            return values
        }
    }

    class ExamDataHandler : TimetableItemDataHandler<Exam> {

        override val tableName = ExamsSchema.TABLE_NAME

        override val itemIdCol = ExamsSchema._ID
        override val timetableIdCol = ExamsSchema.COL_TIMETABLE_ID

        override fun createFromCursor(cursor: Cursor) = Exam.from(cursor)

        override fun propertiesAsContentValues(item: Exam): ContentValues {
            val values = ContentValues()
            with(values) {
                put(ExamsSchema._ID, item.id)
                put(ExamsSchema.COL_TIMETABLE_ID, item.timetableId)
                put(ExamsSchema.COL_SUBJECT_ID, item.subjectId)
                put(ExamsSchema.COL_MODULE, item.moduleName)
                put(ExamsSchema.COL_DATE_DAY_OF_MONTH, item.date.dayOfMonth)
                put(ExamsSchema.COL_DATE_MONTH, item.date.monthValue)
                put(ExamsSchema.COL_DATE_YEAR, item.date.year)
                put(ExamsSchema.COL_START_TIME_HRS, item.startTime.hour)
                put(ExamsSchema.COL_START_TIME_MINS, item.startTime.minute)
                put(ExamsSchema.COL_DURATION, item.duration)
                put(ExamsSchema.COL_SEAT, item.seat)
                put(ExamsSchema.COL_ROOM, item.room)
                put(ExamsSchema.COL_IS_RESIT, if (item.resit) 1 else 0)
            }
            return values
        }
    }

    class SubjectDataHandler : TimetableItemDataHandler<Subject> {

        override val tableName = SubjectsSchema.TABLE_NAME

        override val itemIdCol = SubjectsSchema._ID
        override val timetableIdCol = SubjectsSchema.COL_TIMETABLE_ID

        override fun createFromCursor(cursor: Cursor) = Subject.from(cursor)

        override fun propertiesAsContentValues(item: Subject): ContentValues {
            val values = ContentValues()
            with(values) {
                put(SubjectsSchema._ID, item.id)
                put(SubjectsSchema.COL_TIMETABLE_ID, item.timetableId)
                put(SubjectsSchema.COL_NAME, item.name)
                put(SubjectsSchema.COL_ABBREVIATION, item.abbreviation)
                put(SubjectsSchema.COL_COLOR_ID, item.colorId)
            }
            return values
        }
    }

    class TermDataHandler : TimetableItemDataHandler<Term> {

        override val tableName = TermsSchema.TABLE_NAME

        override val itemIdCol = TermsSchema._ID
        override val timetableIdCol = TermsSchema.COL_TIMETABLE_ID

        override fun createFromCursor(cursor: Cursor) = Term.from(cursor)

        override fun propertiesAsContentValues(item: Term): ContentValues {
            val values = ContentValues()
            with(values) {
                put(TermsSchema._ID, item.id)
                put(TermsSchema.COL_TIMETABLE_ID, item.timetableId)
                put(TermsSchema.COL_NAME, item.name)
                put(TermsSchema.COL_START_DATE_DAY_OF_MONTH, item.startDate.dayOfMonth)
                put(TermsSchema.COL_START_DATE_MONTH, item.startDate.monthValue)
                put(TermsSchema.COL_START_DATE_YEAR, item.startDate.year)
                put(TermsSchema.COL_END_DATE_DAY_OF_MONTH, item.endDate.dayOfMonth)
                put(TermsSchema.COL_END_DATE_MONTH, item.endDate.monthValue)
                put(TermsSchema.COL_END_DATE_YEAR, item.endDate.year)
            }
            return values
        }
    }

    class TimetableDataHandler : DataHandler<Timetable> {

        override val tableName = TimetablesSchema.TABLE_NAME

        override val itemIdCol = TimetablesSchema._ID

        override fun createFromCursor(cursor: Cursor) = Timetable.from(cursor)

        override fun propertiesAsContentValues(item: Timetable): ContentValues {
            val values = ContentValues()
            with(values) {
                put(TimetablesSchema._ID, item.id)
                put(TimetablesSchema.COL_NAME, item.name)
                put(TimetablesSchema.COL_START_DATE_DAY_OF_MONTH, item.startDate.dayOfMonth)
                put(TimetablesSchema.COL_START_DATE_MONTH, item.startDate.monthValue)
                put(TimetablesSchema.COL_START_DATE_YEAR, item.startDate.year)
                put(TimetablesSchema.COL_END_DATE_DAY_OF_MONTH, item.endDate.dayOfMonth)
                put(TimetablesSchema.COL_END_DATE_MONTH, item.endDate.monthValue)
                put(TimetablesSchema.COL_END_DATE_YEAR, item.endDate.year)
                put(TimetablesSchema.COL_WEEK_ROTATIONS, item.weekRotations)
            }
            return values
        }
    }

}
