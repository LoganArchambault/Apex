package com.example.apex

import com.example.apex.util.ResourceException
import com.example.apex.network.RetrofitInstance
import com.example.apex.repository.MoveBaseRepository
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class MoveBaseApiTest {
    private lateinit var server: MockWebServer
    @Before fun preparer() { server = MockWebServer(); server.start() }
    @After fun fermer() { server.shutdown() }
    private fun repository(key: String = "cle-test") = MoveBaseRepository(
        RetrofitInstance.creerService(key, server.url("/").toString()), key, langue = { "fr" })

    @Test fun rechercheEnFrancaisAvecEntetesEtEnveloppeMoveBase() = runBlocking {
        server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody("""
            {"success":true,"data":[{"id":"0025","name":"barbell bench press",
            "category":"chest","equipment":"barbell","target":"pectorals","lang":"fr",
            "instructions":"Poussez la barre.","champInconnu":true}],
            "meta":{"total":1,"offset":0,"limit":100}}
        """.trimIndent()))
        val resultats = repository().chercherExercices("  bench press  ")
        assertEquals("0025", resultats.single().id)
        assertEquals("Poussez la barre.", resultats.single().instructions)
        val requete = server.takeRequest(2, TimeUnit.SECONDS)!!
        assertEquals("/api/v1/exercises", requete.requestUrl!!.encodedPath)
        assertEquals("bench press", requete.requestUrl!!.queryParameter("search"))
        assertEquals("fr", requete.requestUrl!!.queryParameter("lang"))
        assertEquals("cle-test", requete.getHeader("X-RapidAPI-Key"))
        assertEquals("movebase.p.rapidapi.com", requete.getHeader("X-RapidAPI-Host"))
    }

    @Test fun cleAbsenteNeLanceAucuneRequete() = runBlocking {
        val erreur = runCatching { repository("").chercherExercices("press") }.exceptionOrNull()
        assertEquals(R.string.erreur_movebase_cle, (erreur as ResourceException).uiMessage.resource)
        assertEquals(0, server.requestCount)
    }

    @Test fun quotaEtAuthentificationAffichentUneErreurExplicite() = runBlocking {
        for ((code, ressource) in listOf(403 to R.string.erreur_movebase_acces,
            429 to R.string.erreur_movebase_quota, 503 to R.string.erreur_movebase_http)) {
            server.enqueue(MockResponse().setResponseCode(code))
            val erreur = runCatching { repository().chercherExercices("press") }.exceptionOrNull()
            val message = (erreur as ResourceException).uiMessage
            assertEquals(ressource, message.resource)
            if (code == 503) assertEquals(listOf(503), message.arguments)
        }
    }

    @Test fun resultatVideEtInstructionsParEtapes() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"success":true,"data":[]}"""))
        assertTrue(repository().chercherExercices("inconnu").isEmpty())
        server.enqueue(MockResponse().setBody("""{"success":true,"data":[
            {"id":"1","name":"squat","lang":"fr","instructionSteps":["Descendez.","Remontez."]}]}
        """))
        assertEquals("Descendez.\nRemontez.", repository().chercherExercices("squat").single().instructions)
    }
}
