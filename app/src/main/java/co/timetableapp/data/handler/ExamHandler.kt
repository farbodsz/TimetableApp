package co.timetableapp.data.handler

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import co.timetableapp.data.schema.ExamsSchema
import co.timetableapp.model.Exam
import co.timetableapp.receiver.AlarmReceiver
import co.timetableapp.util.DateUtils
import co.timetableapp.util.PrefUtils

class ExamHandler(context: Context) : TimetableItemHandler<Exam>(context) {

    override val tableName = ExamsSchema.TABLE_NAME

    override val itemIdCol = ExamsSchema._ID

    override val timetableIdCol = ExamsSchema.COL_TIMETABLE_ID

    override fun createFromCursor(cursor: Cursor) = Exam.from(cursor)

    override fun createFromId(id: Int) = Exam.create(context, id)

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

    override fun addItem(item: Exam) {
        super.addItem(item)

        if (!item.isInPast()) {
            addAlarmForExam(context, item)
        }
    }

    override fun deleteItem(itemId: Int) {
        super.deleteItem(itemId)

        AlarmReceiver().cancelAlarm(context, AlarmReceiver.Type.EXAM, itemId)
    }

    companion object {
        @JvmStatic
        fun addAlarmForExam(context: Context, exam: Exam) {
            val minsBefore = PrefUtils.getExamNotificationTime(context).toLong()
            val remindDate = exam.makeDateTimeObject().minusMinutes(minsBefore)

            AlarmReceiver().setAlarm(context,
                    AlarmReceiver.Type.EXAM,
                    DateUtils.asCalendar(remindDate),
                    exam.id)
        }
    }

}
