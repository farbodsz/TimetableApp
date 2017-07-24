/*
 * Copyright 2017 Farbod Salamat-Zadeh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.timetableapp.model

import android.content.Context
import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import co.timetableapp.data.TimetableDbHelper
import co.timetableapp.data.handler.DataNotFoundException
import co.timetableapp.data.schema.SubjectsSchema

/**
 * An object representing a subject the student is studying.
 *
 * @property name the name of the subject (e.g. Mathematics, Computer Science, Music)
 * @property abbreviation an optional abbreviation for the subject (e.g. Ma, CS, Mus)
 * @property colorId the identifier of the [Color] used when displaying this subject
 */
data class Subject(
        override val id: Int,
        override val timetableId: Int,
        var name: String,
        var abbreviation: String,
        var colorId: Int
) : TimetableItem, Comparable<Subject> {

    companion object {

        /**
         * Constructs a [Subject] using column values from the cursor provided
         *
         * @param cursor a query of the subjects table
         * @see [SubjectsSchema]
         */
        @JvmStatic
        fun from(cursor: Cursor): Subject {
            return Subject(cursor.getInt(cursor.getColumnIndex(SubjectsSchema._ID)),
                    cursor.getInt(cursor.getColumnIndex(SubjectsSchema.COL_TIMETABLE_ID)),
                    cursor.getString(cursor.getColumnIndex(SubjectsSchema.COL_NAME)),
                    cursor.getString(cursor.getColumnIndex(SubjectsSchema.COL_ABBREVIATION)),
                    cursor.getInt(cursor.getColumnIndex(SubjectsSchema.COL_COLOR_ID)))
        }

        /**
         * Creates a [Subject] from the [subjectId] and corresponding data in the database.
         *
         * @throws DataNotFoundException if the database query returns no results
         * @see from
         */
        @JvmStatic
        @Throws(DataNotFoundException::class)
        fun create(context: Context, subjectId: Int): Subject {
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    SubjectsSchema.TABLE_NAME,
                    null,
                    "${SubjectsSchema._ID}=?",
                    arrayOf(subjectId.toString()),
                    null, null, null)

            if (cursor.count == 0) {
                cursor.close()
                throw DataNotFoundException(this::class.java, subjectId)
            }

            cursor.moveToFirst()
            val subject = Subject.from(cursor)
            cursor.close()
            return subject
        }

        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<Subject> = object : Parcelable.Creator<Subject> {
            override fun createFromParcel(source: Parcel): Subject = Subject(source)
            override fun newArray(size: Int): Array<Subject?> = arrayOfNulls(size)
        }
    }

    private constructor(source: Parcel) : this(
            source.readInt(),
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readInt()
    )

    override fun compareTo(other: Subject): Int {
        return name.compareTo(other.name)
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeInt(timetableId)
        dest?.writeString(name)
        dest?.writeString(abbreviation)
        dest?.writeInt(colorId)
    }

}
