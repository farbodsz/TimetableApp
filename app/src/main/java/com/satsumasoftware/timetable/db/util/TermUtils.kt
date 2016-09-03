package com.satsumasoftware.timetable.db.util

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.db.TermsSchema
import com.satsumasoftware.timetable.db.TimetableDbHelper
import com.satsumasoftware.timetable.framework.Term
import com.satsumasoftware.timetable.framework.Timetable
import java.util.*

class TermUtils {

    companion object {

        const val LOG_TAG = "TermUtils"

        @JvmStatic fun getTerms(context: Context, timetable: Timetable): ArrayList<Term> {
            val terms = ArrayList<Term>()
            val dbHelper = TimetableDbHelper.getInstance(context)
            val cursor = dbHelper.readableDatabase.query(
                    TermsSchema.TABLE_NAME,
                    null,
                    "${TermsSchema.COL_TIMETABLE_ID}=?",
                    arrayOf(timetable.id.toString()),
                    null, null, null)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                terms.add(Term(cursor))
                cursor.moveToNext()
            }
            cursor.close()
            return terms
        }

        @JvmStatic fun addTerm(context: Context, term: Term) {
            val values = ContentValues()
            with(values) {
                put(TermsSchema._ID, term.id)
                put(TermsSchema.COL_TIMETABLE_ID, term.timetableId)
                put(TermsSchema.COL_NAME, term.name)
                put(TermsSchema.COL_START_DATE_DAY_OF_MONTH, term.startDate.dayOfMonth)
                put(TermsSchema.COL_START_DATE_MONTH, term.startDate.monthValue)
                put(TermsSchema.COL_START_DATE_YEAR, term.startDate.year)
                put(TermsSchema.COL_END_DATE_DAY_OF_MONTH, term.endDate.dayOfMonth)
                put(TermsSchema.COL_END_DATE_MONTH, term.endDate.monthValue)
                put(TermsSchema.COL_END_DATE_YEAR, term.endDate.year)
            }

            val db = TimetableDbHelper.getInstance(context).writableDatabase
            db.insert(TermsSchema.TABLE_NAME, null, values)
            Log.i(LOG_TAG, "Added Term with id ${term.id}")
        }

        @JvmStatic fun deleteTerm(context: Context, termId: Int) {
            val db = TimetableDbHelper.getInstance(context).writableDatabase
            db.delete(TermsSchema.TABLE_NAME,
                    "${TermsSchema._ID}=?",
                    arrayOf(termId.toString()))
            Log.i(LOG_TAG, "Deleted Term with id $termId")
        }

        @JvmStatic fun replaceTerm(activity: Activity, oldTermId: Int, newTerm: Term) {
            Log.i(LOG_TAG, "Replacing Term...")

            deleteTerm(activity, oldTermId)
            addTerm(activity, newTerm)
        }

        @JvmStatic fun getHighestTermId(context: Context): Int {
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    TermsSchema.TABLE_NAME,
                    arrayOf(TermsSchema._ID),
                    null,
                    null,
                    null,
                    null,
                    "${TermsSchema._ID} DESC")
            if (cursor.count == 0) {
                return 0
            }
            cursor.moveToFirst()
            val highestId = cursor.getInt(cursor.getColumnIndex(TermsSchema._ID))
            cursor.close()
            return highestId
        }

    }
}
