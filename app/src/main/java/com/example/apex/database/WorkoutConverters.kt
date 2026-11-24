package com.example.apex.database

import androidx.room.TypeConverter
import com.example.apex.data.EnumTypeActivite

class WorkoutConverters {
    @TypeConverter fun versTexte(type: EnumTypeActivite): String = type.name
    @TypeConverter fun versType(type: String): EnumTypeActivite = EnumTypeActivite.valueOf(type)
}
