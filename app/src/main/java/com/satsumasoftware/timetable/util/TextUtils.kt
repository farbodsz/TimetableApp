package com.satsumasoftware.timetable.util

fun String.title(): String {
    if (this.trim().isEmpty()) {
        return this
    }

    val words = this.split(" ")
    val builder = StringBuilder()
    words.forEach {
        val titledWord = it.substring(0, 1).toUpperCase() + it.substring(1)
        builder.append(titledWord)
                .append(" ")
    }
    return builder.toString().trim()
}
