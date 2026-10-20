package com.example.apex.data


data class Workout(
    val id: Int,
    val titre: String,
    val typeActivite: EnumTypeActivite,
    val lieu: String,
    val isOutside: Boolean,
    val intensite: Int,
    val notes: String,
    val done: Boolean,
    val favori: Boolean
)

