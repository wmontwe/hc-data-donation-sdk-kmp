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

package care.data4life.datadonation.internal.domain.mock

import care.data4life.datadonation.core.model.UserConsent
import care.data4life.datadonation.internal.data.model.ConsentSignatureType
import care.data4life.datadonation.internal.domain.repositories.UserConsentRepository
import care.data4life.datadonation.internal.mock.MockException
import io.ktor.utils.io.errors.IOException

class MockConsentDataStore : UserConsentRepository.Remote {

    var whenCreateUserConsent: ((accessToken: String, version: Int, language: String?) -> UserConsent)? =
        null
    var whenFetchUserConsents: ((accessToken: String) -> List<UserConsent>)? = null
    var whenSignUserConsent: ((accessToken: String, message: String) -> String)? = null
    var whenRevokeUserConsent: ((accessToken: String, language: String?) -> Unit)? = null


    override suspend fun createUserConsent(accessToken: String, version: Int, language: String?) {
        whenCreateUserConsent?.invoke(accessToken, version, language)
    }

    override suspend fun fetchUserConsents(accessToken: String): List<UserConsent> =
        whenFetchUserConsents?.invoke(accessToken) ?: throw MockException()

    override suspend fun signUserConsentRegistration(accessToken: String, message: String): String {
        return whenSignUserConsent?.invoke(accessToken, message) ?: throw MockException()
    }

    override suspend fun signUserConsentDonation(accessToken: String, message: String): String {
        return whenSignUserConsent?.invoke(accessToken, message) ?: throw MockException()
    }


    override suspend fun revokeUserConsent(accessToken: String, language: String?) {
        whenRevokeUserConsent?.invoke(accessToken, language)
    }

}