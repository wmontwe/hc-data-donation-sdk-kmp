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

import CapturingResultListener
import care.data4life.datadonation.encryption.hybrid.HybridEncryption
import care.data4life.datadonation.encryption.signature.SignatureKeyPrivate
import care.data4life.datadonation.encryption.symmetric.EncryptionSymmetricKey
import care.data4life.datadonation.internal.data.exception.MissingCredentialsException
import care.data4life.datadonation.internal.data.model.*
import care.data4life.datadonation.internal.data.service.ConsentService
import care.data4life.datadonation.internal.domain.mock.MockConsentDataStore
import care.data4life.datadonation.internal.domain.mock.MockDonationDataStore
import care.data4life.datadonation.internal.domain.mock.MockUserSessionTokenDataStore
import care.data4life.datadonation.internal.domain.repositories.DonationRepository
import care.data4life.datadonation.internal.domain.repositories.UserConsentRepository
import care.data4life.datadonation.internal.utils.Base64Encoder
import care.data4life.datadonation.internal.utils.toJsonString
import io.ktor.utils.io.charsets.*
import runTest
import kotlin.test.*

abstract class DonateResourcesTest {

    private val dummyNonce = "random_nonce"
    private val dummyPublicKey64Encoded = "publicKey64Encoded"
    private val dummySignature = "signature"
    private val dummyEncryptedRequest = byteArrayOf(1, 2, 3)
    private val dummyEncryptedRequest64Encoded = "encryptedRequest64Encoded"
    private val dummyEncryptedSignedMessage = byteArrayOf(4, 5)

    private val dummyResourceList = listOf("resource1", "resource2", "resource3")

    private val mockUserConsentDataStore = MockConsentDataStore()
    private val mockDonationDataStore = MockDonationDataStore()
    private val userConsentRepository =
        UserConsentRepository(mockUserConsentDataStore, MockUserSessionTokenDataStore())
    private val donationRepository = DonationRepository(mockDonationDataStore)

    private val signatureKey = object: SignatureKeyPrivate {
        override fun sign(data: ByteArray) = byteArrayOf()
        override fun serializedPrivate() = DummyData.keyPair.private
        override val pkcs8Private = ""
        override fun verify(data: ByteArray, signature: ByteArray) = true
        override fun serializedPublic() = DummyData.keyPair.public
        override val pkcs8Public = ""
    }

    private val requestJsonString =
        DonationRequest(signatureKey.pkcs8Public, dummyNonce).toJsonString()

    private val consentMessage = ConsentMessage(
        ConsentService.defaultDonationConsentKey,
        ConsentSignatureType.NormalUse.apiValue,
        dummyEncryptedRequest64Encoded
    )

    private val signedConsentJsonString =
        SignedConsentMessage(consentMessage.toJsonString(), dummySignature).toJsonString()

    private val encryptor = object : HybridEncryption {
        override fun encrypt(plaintext: ByteArray) = when (plaintext.decodeToString()) {
            requestJsonString -> dummyEncryptedRequest
            signedConsentJsonString -> dummyEncryptedSignedMessage
            else -> byteArrayOf()
        }
        override fun decrypt(ciphertext: ByteArray) = Result.success(byteArrayOf())

    }

    private val base64Encoder = object : Base64Encoder {
        override fun encode(src: ByteArray) = dummyEncryptedRequest64Encoded
        override fun decode(src: ByteArray, charset: Charset) = ""

    }

    /*private val encryptionSymmetricKey = object : EncryptionSymmetricKey {
        override fun decrypt(encrypted: ByteArray, associatedData: ByteArray) = Result.success(byteArrayOf())
        override fun encrypt(plainText: ByteArray, associatedData: ByteArray) = ByteArray(0)
        override fun serialized() = ByteArray(0)
        override val pkcs8 = ""

    }*/

    private val donateResources =
        DonateResources(
            donationRepository,
            userConsentRepository,
            encryptor,
            base64Encoder)
            { signatureKey }



    private val capturingListener = DonateResourcesListener()

    @Test
    fun donateResourcesTest() = runTest {
        //Given
        var result: DonationPayload? = null
        mockDonationDataStore.whenRequestDonationToken = { dummyNonce }
        mockUserConsentDataStore.whenSignUserConsent = { _, _ -> dummySignature }
        mockDonationDataStore.whenDonateResources = {  payload ->  result = payload }

        //When
        donateResources.runWithParams(
            DonateResources.Parameters(DummyData.keyPair, dummyResourceList),
            capturingListener
        )

        //Then
        assertEquals(capturingListener.captured, Unit)
        assertTrue(result!!.request.contentEquals(dummyEncryptedSignedMessage))
        assertEquals(result!!.documents.size, dummyResourceList.size)
        assertNull(capturingListener.error)
    }

    @Test
    fun donateResourcesTestWithoutKeyFails() = runTest {
        //Given
        mockDonationDataStore.whenRequestDonationToken = { dummyNonce }
        mockUserConsentDataStore.whenSignUserConsent = { _, _ -> dummySignature }

        //When
        donateResources.runWithParams(
            DonateResources.Parameters(null, dummyResourceList),
            capturingListener
        )

        //Then
        assertNull(capturingListener.captured)
        assertTrue(capturingListener.error is MissingCredentialsException)
    }

    class DonateResourcesListener: CapturingResultListener<Unit>()

}

