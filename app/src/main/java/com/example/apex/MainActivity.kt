package com.example.apex

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.apex.security.BiometricAuthManager
import com.example.apex.security.DisponibiliteAuth
import com.example.apex.security.LockManager
import com.example.apex.ui.ApexApp
import com.example.apex.ui.screens.LockScreen
import com.example.apex.ui.theme.ApexTheme
import kotlinx.coroutines.flow.MutableStateFlow

// FragmentActivity : requise par BiometricPrompt.
class MainActivity : FragmentActivity() {
    private val authManager by lazy { BiometricAuthManager(this) }

    /** Séance à ouvrir, demandée par une notification ; consommée une fois la navigation faite. */
    private val seanceDemandee = MutableStateFlow<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            // Véritable lancement (pas une rotation) : écran de verrouillage si l'option est active.
            LockManager.verrouillerSiActif()
            lireIntent(intent)
        } else {
            seanceDemandee.value = savedInstanceState.getInt(EXTRA_ID_WORKOUT, 0).takeIf { it != 0 }
        }

        enableEdgeToEdge()
        setContent {
            ApexTheme {
                val verrouille by LockManager.verrouille.collectAsStateWithLifecycle()
                val idSeance by seanceDemandee.collectAsStateWithLifecycle()

                // Une seule demande par verrouillage ; un échec ou une annulation laisse l'app verrouillée.
                LaunchedEffect(verrouille) {
                    if (verrouille) deverrouiller()
                }

                if (verrouille) {
                    // Rien du contenu des séances n'est composé tant que l'authentification n'a pas réussi.
                    LockScreen(onDeverrouillerClick = ::deverrouiller)
                } else {
                    ApexApp(
                        idSeanceDemandee = idSeance,
                        onSeanceOuverte = { seanceDemandee.value = null },
                        disponibiliteAuth = { authManager.disponibilite() },
                        authentifier = { onSucces -> authentifier(onSucces) }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        lireIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        seanceDemandee.value?.let { outState.putInt(EXTRA_ID_WORKOUT, it) }
    }

    private fun lireIntent(intent: Intent?) {
        val id = intent?.getIntExtra(EXTRA_ID_WORKOUT, 0) ?: 0
        if (id != 0) {
            seanceDemandee.value = id
            intent?.removeExtra(EXTRA_ID_WORKOUT)
        }
    }

    private fun deverrouiller() {
        if (authManager.disponibilite() != DisponibiliteAuth.DISPONIBLE) {
            // Les identifiants de l'appareil ont été retirés depuis l'activation : l'option ne
            // peut plus fonctionner. On la désactive plutôt que de bloquer l'utilisateur.
            (application as ApexApplication).preferencesRepository.modifierVerrouillage(false)
            LockManager.deverrouiller()
            return
        }
        authentifier { LockManager.deverrouiller() }
    }

    private fun authentifier(onSucces: () -> Unit) {
        authManager.authentifier(
            titre = getString(R.string.auth_titre),
            sousTitre = getString(R.string.auth_sous_titre),
            onSucces = onSucces
        )
    }

    companion object {
        const val EXTRA_ID_WORKOUT = "com.example.apex.ID_WORKOUT"
    }
}
