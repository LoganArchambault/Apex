package com.example.apex.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/** Disponibilité de l'authentification sur l'appareil. */
enum class DisponibiliteAuth {
    DISPONIBLE,
    /** Matériel présent, mais ni biométrie ni code/schéma/NIP configuré. */
    AUCUN_IDENTIFIANT,
    NON_SUPPORTEE
}

/**
 * Authentification biométrique avec repli sur les identifiants de l'appareil (code, schéma, NIP).
 * Liée à l'activité qui l'a créée ; elle n'est jamais conservée par un ViewModel.
 */
class BiometricAuthManager(private val activity: FragmentActivity) {
    // BIOMETRIC_WEAK | DEVICE_CREDENTIAL est la combinaison prise en charge sur toutes les
    // versions d'Android ; DEVICE_CREDENTIAL fournit la solution de repli.
    private val authentificateurs = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

    fun disponibilite(): DisponibiliteAuth =
        when (BiometricManager.from(activity).canAuthenticate(authentificateurs)) {
            BiometricManager.BIOMETRIC_SUCCESS -> DisponibiliteAuth.DISPONIBLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> DisponibiliteAuth.AUCUN_IDENTIFIANT
            else -> DisponibiliteAuth.NON_SUPPORTEE
        }

    fun authentifier(
        titre: String,
        sousTitre: String,
        onSucces: () -> Unit,
        onEchec: () -> Unit = {}
    ) {
        if (disponibilite() != DisponibiliteAuth.DISPONIBLE) {
            onEchec()
            return
        }
        val prompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSucces()
                }

                // Annulation ou erreur définitive : l'application reste verrouillée.
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onEchec()
                }
            })

        // Pas de bouton négatif : il est interdit lorsque DEVICE_CREDENTIAL est permis.
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(titre)
            .setSubtitle(sousTitre)
            .setAllowedAuthenticators(authentificateurs)
            .build()
        prompt.authenticate(info)
    }
}
