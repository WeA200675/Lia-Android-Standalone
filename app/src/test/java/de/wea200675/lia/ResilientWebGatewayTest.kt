package de.wea200675.lia

import de.wea200675.lia.core.ResilientWebGateway
import de.wea200675.lia.core.WebGateway
import java.io.IOException
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResilientWebGatewayTest {
    @Test fun preservesSuccessfulPrimaryResponse() {
        val gateway = ResilientWebGateway(successGateway("Aktuelle Antwort"))

        val result = runSuspend { gateway.query("anonymisierte Frage") }

        assertEquals("Aktuelle Antwort", result.getOrThrow())
    }

    @Test fun usesLocalFallbackAfterPrimaryFailure() {
        val gateway = ResilientWebGateway(
            primary = failureGateway(),
            fallback = successGateway("Sichere Offline-Antwort")
        )

        val result = runSuspend { gateway.query("anonymisierte Frage") }

        assertEquals("Sichere Offline-Antwort", result.getOrThrow())
    }

    @Test fun returnsDeterministicMessageWhenBothGatewaysFail() {
        val gateway = ResilientWebGateway(
            primary = failureGateway(),
            fallback = failureGateway()
        )

        val result = runSuspend { gateway.query("anonymisierte Frage") }

        assertTrue(result.isSuccess)
        assertEquals(
            "Die Websuche ist momentan nicht verfügbar. Lokaler Offline-Modus aktiv.",
            result.getOrThrow()
        )
    }

    private fun successGateway(response: String) = object : WebGateway {
        override suspend fun query(anonymizedQuery: String) = Result.success(response)
    }

    private fun failureGateway() = object : WebGateway {
        override suspend fun query(anonymizedQuery: String): Result<String> =
            Result.failure(IOException("nicht verfügbar"))
    }

    private fun <T> runSuspend(block: suspend () -> T): T {
        var outcome: Result<T>? = null
        block.startCoroutine(object : Continuation<T> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(result: Result<T>) {
                outcome = result
            }
        })
        return checkNotNull(outcome).getOrThrow()
    }
}
