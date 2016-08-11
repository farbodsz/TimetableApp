package com.satsumasoftware.timetable

fun String.title(): String {
    val words = this.split(" ")
    val builder = StringBuilder()
    for (word in words) {
        val titledWord = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase()
        builder.append(titledWord)
                .append(" ")
    }
    return builder.toString().trim()
}
