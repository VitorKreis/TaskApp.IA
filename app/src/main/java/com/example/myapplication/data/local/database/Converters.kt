package com.example.myapplication.data.local.database

import androidx.room.TypeConverter
import org.json.JSONArray

class Converters {
    @TypeConverter
    fun fromTagsList(tags: List<String>): String {
        return JSONArray(tags).toString()
    }

    @TypeConverter
    fun toTagsList(data: String): List<String> {
        if (data.isEmpty() || data == "[]") return emptyList()
        val jsonArray = JSONArray(data)
        return (0 until jsonArray.length()).map { jsonArray.getString(it) }
    }
}
