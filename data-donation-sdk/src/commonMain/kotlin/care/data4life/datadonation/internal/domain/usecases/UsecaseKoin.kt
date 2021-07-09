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

package care.data4life.datadonation.internal.domain.usecases

import care.data4life.datadonation.encryption.EncryptionContract
import care.data4life.datadonation.internal.utils.Base64Factory
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun resolveUsecaseModule(): Module {
    return module {
        single {
            CreateRequestConsentPayload(
                get(),
                get(),
                get<EncryptionContract.HybridEncryptionRegistry>().hybridEncryptionDD,
                Base64Factory.createEncoder()
            )
        }

        single {
            DonateResources(
                get(),
                get(),
                get(),
                get(),
                get<EncryptionContract.HybridEncryptionRegistry>().hybridEncryptionALP
            )
        }

        single { FilterSensitiveInformation() }

        single {
            RegisterNewDonor(
                get(),
                get()
            )
        }

        single<UsecaseContract.FetchConsentDocuments> {
            FetchConsentDocumentsFactory(get())
        }

        single<UsecaseContract.CreateUserConsent> {
            CreateUserConsentFactory(get())
        }

        single<UsecaseContract.FetchUserConsents> {
            FetchUserConsentsFactory(get())
        }

        single {
            RemoveInternalInformation(kotlinx.serialization.json.Json {})
        }

        single<UsecaseContract.RevokeUserConsent> {
            RevokeUserConsentFactory(get())
        }
    }
}