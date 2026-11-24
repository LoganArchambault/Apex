package com.example.apex.repository

import com.example.apex.R
import com.example.apex.util.UiMessage
import com.example.apex.util.ResourceException
import com.example.apex.util.langueApplication
import com.example.apex.data.Exercise
import com.example.apex.network.MoveBaseApiService
import com.example.apex.network.toModel
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

class MoveBaseRepository(
    private val api: MoveBaseApiService,
    private val apiKey: String,
    /** Langue demandée à l'API : celle de l'application au moment de la recherche. */
    private val langue: () -> String = { langueApplication() }
) : IExerciseRepository {
    override suspend fun chercherExercices(motCle: String): List<Exercise> {
        if (apiKey.isBlank()) throw ResourceException(UiMessage(R.string.erreur_movebase_cle))
        return try {
            val langueDemandee = langue()
            val reponse = api.chercherExercices(motCle.trim(), langue = langueDemandee)
            if (!reponse.success) throw ResourceException(UiMessage(R.string.erreur_movebase_recherche))
            reponse.data.sortedByDescending { it.lang == langueDemandee }.map { it.toModel() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            throw ResourceException(UiMessage(R.string.erreur_connexion), e)
        } catch (e: HttpException) {
            val message = when (e.code()) {
                401, 403 -> UiMessage(R.string.erreur_movebase_acces)
                429 -> UiMessage(R.string.erreur_movebase_quota)
                else -> UiMessage(R.string.erreur_movebase_http, listOf(e.code()))
            }
            throw ResourceException(message, e)
        } catch (e: SerializationException) {
            throw ResourceException(UiMessage(R.string.erreur_movebase_reponse), e)
        }
    }
}
