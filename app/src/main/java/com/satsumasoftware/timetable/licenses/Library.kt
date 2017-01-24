package com.satsumasoftware.timetable.licenses

class Library(val name: String, val website: String?, val author: String,
              val license: License) : Comparable<Library> {

    override fun compareTo(other: Library) = name.compareTo(other.name)

}
