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

import care.data4life.datadonation.core.model.ConsentDocument
import care.data4life.datadonation.core.model.UserConsent
import care.data4life.datadonation.internal.data.model.ConsentSignature
import care.data4life.datadonation.internal.data.model.DonationPayload

internal interface ServiceContract {
    interface ConsentService {
        suspend fun fetchConsentDocuments(
            accessToken: String,
            version: Int?,
            language: String?,
            consentKey: String
        ): List<ConsentDocument>

        suspend fun fetchUserConsents(
            accessToken: String,
            latest: Boolean?,
            consentKey: String? = null
        ): List<UserConsent>

        suspend fun createUserConsent(
            accessToken: String,
            version: Int,
            language: String?
        )

        suspend fun requestSignatureRegistration(
            accessToken: String,
            message: String
        ): ConsentSignature

        suspend fun requestSignatureDonation(
            accessToken: String,
            message: String
        ): ConsentSignature

        suspend fun revokeUserConsent(accessToken: String, language: String?)

        companion object {
            const val XSRF_VALIDITY = 23 * 60 * 60 * 1000

            object Endpoints {
                const val userConsents = "userConsents"
                const val consentDocuments = "consentDocuments"
                const val token = "xsrf"
            }

            object Parameters {
                const val consentDocumentKey = "key"
                const val userConsentDocumentKey = "consentDocumentKey"
                const val latest = "latest"
                const val language = "language"
                const val version = "version"
            }

            object Headers {
                const val XSRFToken = "X-Csrf-Token"
            }
        }
    }

    interface DonationService {
        suspend fun requestToken(): String
        suspend fun registerNewDonor(payload: ByteArray)
        suspend fun donateResources(payload: DonationPayload)

        companion object {
            object Endpoints {
                const val token = "token"
                const val register = "register"
                const val donate = "donate"
            }

            object FormDataEntries {
                const val request = "request"
                const val signature = "signature_"
                const val donation = "donation_"
            }

            object FormDataHeaders {
                const val fileName = "filename="
            }
        }
    }

    companion object {
        const val DEFAULT_DONATION_CONSENT_KEY = "d4l.data-donation.broad"
    }
}
