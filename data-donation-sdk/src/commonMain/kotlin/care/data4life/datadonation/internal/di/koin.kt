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

package care.data4life.datadonation.internal.di

import care.data4life.datadonation.core.model.KeyPair
import care.data4life.datadonation.internal.data.service.ConsentService
import care.data4life.datadonation.internal.data.service.DonationService
import care.data4life.datadonation.internal.data.store.RegistrationDataStore
import care.data4life.datadonation.internal.data.store.UserConsentDataStore
import care.data4life.datadonation.internal.data.store.UserSessionTokenDataStore
import care.data4life.datadonation.internal.domain.repositories.RegistrationRepository
import care.data4life.datadonation.internal.domain.repositories.UserConsentRepository
import care.data4life.datadonation.internal.domain.usecases.CreateUserConsent
import care.data4life.datadonation.internal.domain.usecases.GetConsentDocument
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun initKoin(donationKeyPair: KeyPair?, getUserSessionToken: () -> String?) = startKoin {
    modules(
        module {
            single<UserSessionTokenDataStore> {
                object :
                    UserSessionTokenDataStore {
                    override fun getUserSessionToken(): String? = getUserSessionToken()
                }
            }
        },
        platformModule,
        coreModule
    )
}

private val coreModule = module {

    //Services
    single { ConsentService() }
    single { DonationService() }


    //DataStores
    single<UserConsentRepository.Remote> { UserConsentDataStore(get()) }
    single<RegistrationRepository.Remote> { RegistrationDataStore(get()) }

    //Repositories
    single {
        UserConsentRepository(
            get()
        )
    }
    single {
        RegistrationRepository(
            get()
        )
    }

    //Usecases
    single { CreateUserConsent(get()) }
    single { GetConsentDocument(get()) }
}

expect val platformModule: Module
