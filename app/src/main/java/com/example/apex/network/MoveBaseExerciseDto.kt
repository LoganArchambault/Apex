package com.example.apex.network

import com.example.apex.data.Exercise
import kotlinx.serialization.Serializable

@Serializable
data class MoveBaseResponse(
    val success: Boolean,
    val data: List<MoveBaseExerciseDto> = emptyList()
)

@Serializable
data class MoveBaseExerciseDto(
    val id: String,
    val name: String,
    val category: String = "",
    val equipment: String = "",
    val target: String = "",
    val instructions: String = "",
    val instructionSteps: List<String> = emptyList(),
    val lang: String = "en",
    val attribution: String = ""
)

fun MoveBaseExerciseDto.toModel() = Exercise(id = id, nom = name,
    categorie = category, equipement = equipment, muscleCible = target,
    instructions = instructions.ifBlank { instructionSteps.joinToString("\n") },
    langue = lang, attribution = attribution)
