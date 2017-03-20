package co.timetableapp.util

fun String.title(): String {
    val string = this.trim()

    if (string.isEmpty()) {
        return string
    }

    val words = string.split(" ")
    val builder = StringBuilder()
    words.forEach {
        if (it.isNotEmpty()) {
            val titledWord = it.substring(0, 1).toUpperCase() + it.substring(1)
            builder.append(titledWord)
                    .append(" ")
        }
    }

    return builder.toString().trim()
}
