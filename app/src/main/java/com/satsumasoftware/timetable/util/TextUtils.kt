package com.satsumasoftware.timetable.util

fun String.title(): String {
    if (this.trim().length == 0) {
        return this
    }

    val words = this.split(" ")
    val builder = StringBuilder()
    for (word in words) {
        val titledWord = word.substring(0, 1).toUpperCase() + word.substring(1)
        builder.append(titledWord)
                .append(" ")
    }
    return builder.toString().trim()
}
