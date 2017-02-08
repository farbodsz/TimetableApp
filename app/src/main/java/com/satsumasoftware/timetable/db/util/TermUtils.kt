package com.satsumasoftware.timetable.db.util

import android.content.Context
import com.satsumasoftware.timetable.db.TimetableDbHelper
import com.satsumasoftware.timetable.db.schema.TermsSchema
import com.satsumasoftware.timetable.framework.Term
import java.util.*

object TermUtils {

    @JvmStatic
    fun getTerms(context: Context, timetableId: Int): ArrayList<Term> {
        val terms = ArrayList<Term>()
        val dbHelper = TimetableDbHelper.getInstance(context)
        val cursor = dbHelper.readableDatabase.query(
                TermsSchema.TABLE_NAME,
                null,
                "${TermsSchema.COL_TIMETABLE_ID}=?",
                arrayOf(timetableId.toString()),
                null, null, null)
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            terms.add(Term.from(cursor))
            cursor.moveToNext()
        }
        cursor.close()
        return terms
    }

}
