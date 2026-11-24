package com.example.apex

import com.example.apex.data.Exercise
import com.example.apex.viewmodel.ExerciceFormState
import org.junit.Assert.*
import org.junit.Test

class ExerciseFormTest {
    private val formulaire = ExerciceFormState(Exercise("1", "Squat"))
    @Test fun accepteLaVirguleDecimaleEtLaChargeNulle() {
        assertEquals(12.5, formulaire.copy(charge = "12,5").versModele()!!.charge, 0.0)
        assertEquals(0.0, formulaire.versModele()!!.charge, 0.0)
    }
    @Test fun refuseValeursVidesNegativesNonFiniesEtRepetitionsDecimales() {
        for (charge in listOf("", "-1", "NaN", "Infinity")) {
            assertNull(formulaire.copy(charge = charge).versModele())
        }
        for (nombre in listOf("", "0", "-1", "1.5", "abc")) {
            assertNull(formulaire.copy(series = nombre).versModele())
            assertNull(formulaire.copy(repetitions = nombre).versModele())
        }
    }
}
