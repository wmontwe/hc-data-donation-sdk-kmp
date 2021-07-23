/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2020, D4L data4life gGmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 *  Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package care.data4life.datadonation.internal.data.service

import care.data4life.datadonation.DataDonationSDKPublicAPI
import care.data4life.datadonation.lang.CoreRuntimeError
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.minutes

class CachedUserSessionTokenService(
    private val provider: DataDonationSDKPublicAPI.UserSessionTokenProvider,
    private val clock: Clock
) : ServiceContract.UserSessionTokenService {

    private var cachedValue: String = ""
    private var cachedAt = Instant.fromEpochSeconds(0)

    private fun fetchCachedToken(continuation: Continuation<SessionToken>) {
        if (cachedValue.isEmpty()) {
            throw CoreRuntimeError.MissingSession()
        }

        continuation.resume(cachedValue)
    }

    private fun updateCachedToken(sessionToken: SessionToken) {
        cachedValue = sessionToken
        cachedAt = clock.now()
    }

    private fun handleApiError(error: Exception) {
        throw CoreRuntimeError.MissingSession(error)
    }

    private fun fetchTokenFromApi(continuation: Continuation<SessionToken>) {
        provider.getUserSessionToken(
            { sessionToken ->
                updateCachedToken(sessionToken)
                continuation.resume(sessionToken)
            },
            {
                error ->
                handleApiError(error)
            }
        )
    }

    override suspend fun getUserSessionToken(): SessionToken {
        return suspendCoroutine { continuation ->
            if (cachedAt > clock.now().minus(1.minutes)) {
                fetchCachedToken(continuation)
            } else {
                fetchTokenFromApi(continuation)
            }
        }
    }
}