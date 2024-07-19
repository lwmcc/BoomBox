package com.mccarty.ritmo.repository.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.mccarty.ritmo.domain.model.Context
import com.mccarty.ritmo.domain.model.Track

class Converters {
    @TypeConverter
    fun fromTrackObject(track: Track?): String? = Gson().toJson(track)

    @TypeConverter
    fun toTrackObject(track: String?): Track? = Gson().fromJson(track, Track::class.java)

    @TypeConverter
    fun fromContextObject(context: Context?): String? = Gson().toJson(context)

    @TypeConverter
    fun toContextObject(context: String?): Context? = Gson().fromJson(context, Context::class.java)
}