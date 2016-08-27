package com.satsumasoftware.timetable.db.util

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.TimetableApplication
import com.satsumasoftware.timetable.db.ClassDetailsSchema
import com.satsumasoftware.timetable.db.ClassTimesSchema
import com.satsumasoftware.timetable.db.ClassesSchema
import com.satsumasoftware.timetable.db.TimetableDbHelper
import com.satsumasoftware.timetable.framework.Class
import com.satsumasoftware.timetable.framework.ClassDetail
import com.satsumasoftware.timetable.framework.ClassTime
import org.threeten.bp.DayOfWeek
import java.util.*

class ClassUtils {

    companion object {

        const val LOG_TAG = "ClassUtils"

        @JvmStatic fun getClasses(activity: Activity): ArrayList<Class> {
            val classes = ArrayList<Class>()

            val timetable = (activity.application as TimetableApplication).currentTimetable!!

            val dbHelper = TimetableDbHelper.getInstance(activity)
            val cursor = dbHelper.readableDatabase.query(
                    ClassesSchema.TABLE_NAME,
                    null,
                    "${ClassesSchema.COL_TIMETABLE_ID}=?",
                    arrayOf(timetable.id.toString()),
                    null, null, null)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                classes.add(Class(cursor))
                cursor.moveToNext()
            }
            cursor.close()

            return classes
        }

        @JvmStatic fun getClassWithId(context: Context, classId: Int): Class? {
            val dbHelper = TimetableDbHelper.getInstance(context)
            val cursor = dbHelper.readableDatabase.query(
                    ClassesSchema.TABLE_NAME,
                    null,
                    "${ClassesSchema._ID}=?",
                    arrayOf(classId.toString()),
                    null, null, null)
            cursor.moveToFirst()
            if (cursor.count == 0) {
                cursor.close()
                return null
            }
            val cls = Class(cursor)
            cursor.close()
            return cls
        }

        @JvmStatic fun getClassesForSubject(context: Context, subjectId: Int): ArrayList<Class> {
            val classes = ArrayList<Class>()
            val dbHelper = TimetableDbHelper.getInstance(context)
            val cursor = dbHelper.readableDatabase.query(
                    ClassesSchema.TABLE_NAME,
                    null,
                    "${ClassesSchema.COL_SUBJECT_ID}=?",
                    arrayOf(subjectId.toString()),
                    null, null, null)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                classes.add(Class(cursor))
                cursor.moveToNext()
            }
            cursor.close()
            return classes
        }

        @JvmStatic fun getClassDetailWithId(context: Context, classDetailId: Int): ClassDetail {
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    ClassDetailsSchema.TABLE_NAME,
                    null,
                    "${ClassDetailsSchema._ID}=?",
                    arrayOf(classDetailId.toString()),
                    null, null, null)
            cursor.moveToFirst()
            val classDetail = ClassDetail(cursor)
            cursor.close()
            return classDetail
        }

        @JvmStatic fun getClassDetailsForClass(context: Context, classId: Int): ArrayList<ClassDetail> {
            val classDetails = ArrayList<ClassDetail>()
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    ClassDetailsSchema.TABLE_NAME,
                    null,
                    "${ClassDetailsSchema.COL_CLASS_ID}=?",
                    arrayOf(classId.toString()),
                    null, null, null)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                classDetails.add(ClassDetail(cursor))
                cursor.moveToNext()
            }
            cursor.close()
            return classDetails
        }

        @JvmStatic fun getClassTimeIds(context: Context, classDetailId: Int): ArrayList<Int> {
            val classTimeIds = ArrayList<Int>()
            val dbHelper = TimetableDbHelper.getInstance(context)
            val cursor = dbHelper.readableDatabase.query(
                    ClassTimesSchema.TABLE_NAME,
                    null,
                    "${ClassTimesSchema.COL_CLASS_DETAIL_ID}=?",
                    arrayOf(classDetailId.toString()),
                    null, null, null)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                classTimeIds.add(cursor.getInt(cursor.getColumnIndex(ClassTimesSchema._ID)))
                cursor.moveToNext()
            }
            cursor.close()
            return classTimeIds
        }

        @JvmStatic fun getClassTimesForDetail(context: Context, classDetailId: Int): ArrayList<ClassTime> {
            val classTimes = ArrayList<ClassTime>()
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    ClassTimesSchema.TABLE_NAME,
                    null,
                    "${ClassTimesSchema.COL_CLASS_DETAIL_ID}=?",
                    arrayOf(classDetailId.toString()),
                    null, null, null)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                classTimes.add(ClassTime(cursor))
                cursor.moveToNext()
            }
            cursor.close()
            return classTimes
        }

        @JvmStatic fun getHighestClassId(context: Context): Int {
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    ClassesSchema.TABLE_NAME,
                    arrayOf(ClassesSchema._ID),
                    null,
                    null,
                    null,
                    null,
                    "${ClassesSchema._ID} DESC")
            if (cursor.count == 0) {
                return 0
            }
            cursor.moveToFirst()
            val highestId = cursor.getInt(cursor.getColumnIndex(ClassesSchema._ID))
            cursor.close()
            return highestId
        }

        @JvmStatic fun addClass(context: Context, cls: Class) {
            val values = ContentValues()
            with(values) {
                put(ClassesSchema._ID, cls.id)
                put(ClassesSchema.COL_TIMETABLE_ID, cls.timetableId)
                put(ClassesSchema.COL_SUBJECT_ID, cls.subjectId)
            }

            val db = TimetableDbHelper.getInstance(context).writableDatabase
            db.insert(ClassesSchema.TABLE_NAME, null, values)
            Log.i(LOG_TAG, "Added Class with id ${cls.id}")
        }

        private fun deleteClass(context: Context, classId: Int) {
            val db = TimetableDbHelper.getInstance(context).writableDatabase
            db.delete(ClassesSchema.TABLE_NAME,
                    "${ClassesSchema._ID}=?",
                    arrayOf(classId.toString()))
            Log.i(LOG_TAG, "Deleted Class with id $classId")
        }

        @JvmStatic fun replaceClass(context: Context, oldClassId: Int, newClass: Class) {
            Log.i(LOG_TAG, "Replacing Class...")
            deleteClass(context, oldClassId)
            addClass(context, newClass)
        }

        @JvmStatic fun getHighestClassDetailId(context: Context): Int {
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    ClassDetailsSchema.TABLE_NAME,
                    arrayOf(ClassDetailsSchema._ID),
                    null,
                    null,
                    null,
                    null,
                    "${ClassDetailsSchema._ID} DESC")
            if (cursor.count == 0) {
                return 0
            }
            cursor.moveToFirst()
            val highestId = cursor.getInt(cursor.getColumnIndex(ClassDetailsSchema._ID))
            cursor.close()
            return highestId
        }

        @JvmStatic fun addClassDetail(context: Context, classDetail: ClassDetail) {
            val values = ContentValues()
            with(values) {
                put(ClassDetailsSchema._ID, classDetail.id)
                put(ClassDetailsSchema.COL_CLASS_ID, classDetail.classId)
                put(ClassDetailsSchema.COL_ROOM, classDetail.room)
                put(ClassDetailsSchema.COL_BUILDING, classDetail.building)
                put(ClassDetailsSchema.COL_TEACHER, classDetail.teacher)
            }

            val db = TimetableDbHelper.getInstance(context).writableDatabase
            db.insert(ClassDetailsSchema.TABLE_NAME, null, values)
            Log.i(LOG_TAG, "Added ClassDetail with id ${classDetail.id}")
        }

        private fun deleteClassDetail(context: Context, classDetailId: Int) {
            val db = TimetableDbHelper.getInstance(context).writableDatabase
            db.delete(ClassDetailsSchema.TABLE_NAME,
                    "${ClassDetailsSchema._ID}=?",
                    arrayOf(classDetailId.toString()))
            Log.i(LOG_TAG, "Deleted ClassDetail with id $classDetailId")
        }

        @JvmStatic fun replaceClassDetail(context: Context, oldClassDetailId: Int,
                                          newClassDetail: ClassDetail) {
            Log.i(LOG_TAG, "Replacing ClassDetail...")
            deleteClassDetail(context, oldClassDetailId)
            addClassDetail(context, newClassDetail)
        }

        @JvmStatic fun getClassTimesForDay(activity: Activity, dayOfWeek: DayOfWeek,
                                           weekNumber: Int): ArrayList<ClassTime> {
            val classTimes = ArrayList<ClassTime>()

            val timetable = (activity.application as TimetableApplication).currentTimetable!!

            val dbHelper = TimetableDbHelper.getInstance(activity)
            val cursor = dbHelper.readableDatabase.query(
                    ClassTimesSchema.TABLE_NAME,
                    null,
                    "${ClassTimesSchema.COL_TIMETABLE_ID}=? AND ${ClassTimesSchema.COL_DAY}=? " +
                            "AND ${ClassTimesSchema.COL_WEEK_NUMBER}=?",
                    arrayOf(timetable.id.toString(), dayOfWeek.value.toString(), weekNumber.toString()),
                    null, null, null)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                classTimes.add(ClassTime(cursor))
                cursor.moveToNext()
            }
            cursor.close()

            return classTimes
        }

        @JvmStatic fun getHighestClassTimeId(context: Context): Int {
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    ClassTimesSchema.TABLE_NAME,
                    arrayOf(ClassTimesSchema._ID),
                    null,
                    null,
                    null,
                    null,
                    "${ClassTimesSchema._ID} DESC")
            if (cursor.count == 0) {
                return 0
            }
            cursor.moveToFirst()
            val highestId = cursor.getInt(cursor.getColumnIndex(ClassTimesSchema._ID))
            cursor.close()
            return highestId
        }

        @JvmStatic fun addClassTime(context: Context, classTime: ClassTime) {
            val values = ContentValues()
            with(values) {
                put(ClassTimesSchema._ID, classTime.id)
                put(ClassTimesSchema.COL_TIMETABLE_ID, classTime.timetableId)
                put(ClassTimesSchema.COL_CLASS_DETAIL_ID, classTime.classDetailId)
                put(ClassTimesSchema.COL_DAY, classTime.day.value)
                put(ClassTimesSchema.COL_WEEK_NUMBER, classTime.weekNumber)
                put(ClassTimesSchema.COL_START_TIME_HRS, classTime.startTime.hour)
                put(ClassTimesSchema.COL_START_TIME_MINS, classTime.startTime.minute)
                put(ClassTimesSchema.COL_END_TIME_HRS, classTime.endTime.hour)
                put(ClassTimesSchema.COL_END_TIME_MINS, classTime.endTime.minute)
            }

            val db = TimetableDbHelper.getInstance(context).writableDatabase
            db.insert(ClassTimesSchema.TABLE_NAME, null, values)
            Log.i(LOG_TAG, "Added ClassTime with id ${classTime.id}")
        }

        private fun deleteClassTime(context: Context, classTimeId: Int) {
            val db = TimetableDbHelper.getInstance(context).writableDatabase
            db.delete(ClassTimesSchema.TABLE_NAME,
                    "${ClassTimesSchema._ID}=?",
                    arrayOf(classTimeId.toString()))
            Log.i(LOG_TAG, "Deleted ClassTime with id $classTimeId")
        }

        @JvmStatic fun replaceClassTime(context: Context, oldClassTimeId: Int, newClassTime: ClassTime) {
            Log.i(LOG_TAG, "Replacing ClassTime...")
            deleteClassTime(context, oldClassTimeId)
            addClassTime(context, newClassTime)
        }

        @JvmStatic fun completelyDeleteClass(context: Context, cls: Class) {
            Log.i(LOG_TAG, "Deleting everything related to Class of id ${cls.id}")

            deleteClass(context, cls.id)

            for (assignment in AssignmentUtils.getAssignmentsForClass(context, cls.id)) {
                AssignmentUtils.deleteAssignment(context, assignment.id)
            }

            for (classDetail in getClassDetailsForClass(context, cls.id)) {
                completelyDeleteClassDetail(context, classDetail.id)
            }
        }

        @JvmStatic fun completelyDeleteClassDetail(context: Context, classDetailId: Int) {
            Log.i(LOG_TAG, "Deleting everything related to ClassDetail of id $classDetailId")

            deleteClassDetail(context, classDetailId)

            for (classTimeId in getClassTimeIds(context, classDetailId)) {
                completelyDeleteClassTime(context, classTimeId)
            }
        }

        @JvmStatic fun completelyDeleteClassTime(context: Context, classTimeId: Int) {
            Log.i(LOG_TAG, "Deleting everything related to ClassTime of id $classTimeId")

            deleteClassTime(context, classTimeId)
        }

    }
}
