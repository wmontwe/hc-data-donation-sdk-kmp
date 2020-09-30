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

package care.data4life.datadonation.internal.domain.usecases

import care.data4life.datadonation.core.model.KeyPair
import care.data4life.datadonation.encryption.hybrid.HybridEncryptor
import care.data4life.datadonation.internal.data.model.RegistrationRequest
import care.data4life.datadonation.internal.data.model.SignedConsentMessage
import care.data4life.datadonation.internal.domain.repositories.RegistrationRepository
import care.data4life.datadonation.internal.domain.repositories.UserConsentRepository
import care.data4life.datadonation.internal.utils.Base64Encoder
import care.data4life.datadonation.internal.utils.toJsonString
import io.ktor.utils.io.core.*

internal class RegisterNewDonor(
    private val registrationRepository: RegistrationRepository,
    private val consentRepository: UserConsentRepository,
    private val encryptor: HybridEncryptor,
    private val base64encoder: Base64Encoder
) :
    ParameterizedUsecase<KeyPair, Unit>() {

    override suspend fun execute() {
        val token = registrationRepository.requestRegistrationToken()
        val request = RegistrationRequest(base64encoder.encode(parameter.public), token)
        // TODO Double check if String needs to be UTF-16 encode instead of UTF-8
        val message = base64encoder.encode(encryptor.encrypt(request.toJsonString().toByteArray()))
        val signature = consentRepository.signUserConsent(message)
        val signedMessage = SignedConsentMessage(message, signature)
        // TODO Double check if String needs to be UTF-16 encode instead of UTF-8
        val payload = encryptor.encrypt(signedMessage.toJsonString().toByteArray())
        registrationRepository.registerNewDonor(payload)
    }

}

