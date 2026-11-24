package com.example.apex.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.apex.ApexApplication

object WorkoutViewModelFactory {
    val liste = viewModelFactory {
        initializer { WorkoutViewModel((this[APPLICATION_KEY] as ApexApplication).repository) }
    }
    fun creation(idWorkout: Int? = null) = viewModelFactory {
        initializer {
            val app = this[APPLICATION_KEY] as ApexApplication
            CreateWorkoutViewModel(app.repository, app.exerciseRepository, idWorkout)
        }
    }
    fun details(idWorkout: Int) = viewModelFactory {
        initializer {
            val app = this[APPLICATION_KEY] as ApexApplication
            WorkoutDetailsViewModel(app.repository, app.suiviRepository, idWorkout)
        }
    }
    val parametres = viewModelFactory {
        initializer { ParametresViewModel((this[APPLICATION_KEY] as ApexApplication).preferencesRepository) }
    }
}
