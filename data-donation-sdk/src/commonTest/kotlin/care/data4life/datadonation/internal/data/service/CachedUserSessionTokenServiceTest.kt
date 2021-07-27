/*
 * Copyright (c) 2021 D4L data4life gGmbH / All rights reserved.
 *
 * D4L owns all legal rights, title and interest in and to the Software Development Kit ("SDK"),
 * including any intellectual property rights that subsist in the SDK.
 *
 * The SDK and its documentation may be accessed and used for viewing/review purposes only.
 * Any usage of the SDK for other purposes, including usage for the development of
 * applications/third-party applications shall require the conclusion of a license agreement
 * between you and D4L.
 *
 * If you are interested in licensing the SDK for your own applications/third-party
 * applications and/or if you’d like to contribute to the development of the SDK, please
 * contact D4L by email to help@data4life.care.
 */

package care.data4life.datadonation.internal.data.service

import care.data4life.datadonation.lang.CoreRuntimeError
import care.data4life.datadonation.mock.stub.ClockStub
import care.data4life.datadonation.mock.stub.UserSessionTokenProviderStub
import care.data4life.sdk.util.test.coroutine.runWithContextBlockingTest
import kotlinx.coroutines.GlobalScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.minutes
import kotlin.time.seconds

class CachedUserSessionTokenServiceTest {
    @Test
    fun `It fulfils UserSessionTokenService`() {
        val service: Any = CachedUserSessionTokenService(UserSessionTokenProviderStub(), ClockStub())

        assertTrue(service is ServiceContract.UserSessionTokenService)
    }

    @Test
    fun `Given getUserSessionToken is called, it fails, if it has no valid cached Token and the Provider delegates an Exception`() = runWithContextBlockingTest(GlobalScope.coroutineContext) {
        // Given
        val error = RuntimeException("error")
        val provider = UserSessionTokenProviderStub()
        val time = ClockStub()

        provider.whenGetUserSessionToken = { _, onError ->
            onError(error)
        }

        time.whenNow = {
            kotlinx.datetime.Instant.fromEpochMilliseconds(1.minutes.toLongMilliseconds())
        }

        val service = CachedUserSessionTokenService(provider, time)

        // Then
        val result = assertFailsWith<CoreRuntimeError.MissingSession> {
            // When
            service.getUserSessionToken()
        }

        assertSame(
            actual = result.cause,
            expected = error
        )
    }

    @Test
    fun `Given getUserSessionToken is called, returns a new Token, if it has no valid cached Token and the Provider delegates a String`() = runWithContextBlockingTest(GlobalScope.coroutineContext) {
        // Given
        val token = "tomato"
        val provider = UserSessionTokenProviderStub()
        val time = ClockStub()

        provider.whenGetUserSessionToken = { onSuccess, _ ->
            onSuccess(token)
        }

        time.whenNow = {
            kotlinx.datetime.Instant.fromEpochMilliseconds(1.minutes.toLongMilliseconds())
        }

        val service = CachedUserSessionTokenService(provider, time)

        // When
        val result = service.getUserSessionToken()

        // Then
        assertEquals(
            actual = result,
            expected = token
        )
    }

    @Test
    fun `Given getUserSessionToken is called, it fails, if an internal error happens`() = runWithContextBlockingTest(GlobalScope.coroutineContext) {
        // Given
        val provider = UserSessionTokenProviderStub()
        val time = ClockStub()

        time.whenNow = {
            kotlinx.datetime.Instant.fromEpochMilliseconds(0)
        }

        val service = CachedUserSessionTokenService(provider, time)

        // Then
        val result = assertFailsWith<CoreRuntimeError.MissingSession> {
            // When
            service.getUserSessionToken()
        }

        assertNull(result.cause)
    }

    @Test
    fun `Given getUserSessionToken is called, it returns a cached token, if a token had been previously stored and it used in its 1 minute lifetime`() = runWithContextBlockingTest(GlobalScope.coroutineContext) {
        // Given
        val expectedToken = "potato"
        val tokens = mutableListOf(
            expectedToken,
            "tomato"
        )
        val provider = UserSessionTokenProviderStub()
        val time = ClockStub()
        val lifeTime = mutableListOf(
            kotlinx.datetime.Instant.fromEpochMilliseconds(1.minutes.toLongMilliseconds()),
            kotlinx.datetime.Instant.fromEpochMilliseconds(1.minutes.plus(30.seconds).toLongMilliseconds())
        )

        provider.whenGetUserSessionToken = { onSuccess, _ ->
            onSuccess(tokens.removeAt(0))
        }
        time.whenNow = {
            lifeTime.removeAt(0)
        }

        val service = CachedUserSessionTokenService(provider, time)

        // When
        val result = service.getUserSessionToken()

        // Then
        assertEquals(
            actual = result,
            expected = expectedToken
        )
    }
}
