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

import care.data4life.datadonation.Contract
import care.data4life.datadonation.core.model.Environment
import care.data4life.datadonation.encryption.resolveEncryptionModule
import care.data4life.datadonation.internal.data.service.DonationService
import care.data4life.datadonation.internal.data.service.ServiceContract
import care.data4life.datadonation.internal.data.service.resolveServiceModule
import care.data4life.datadonation.internal.data.storage.*
import care.data4life.datadonation.internal.domain.repository.resolveRepositoryModule
import care.data4life.datadonation.internal.domain.usecases.*
import care.data4life.datadonation.internal.io.IOContract
import care.data4life.datadonation.internal.io.resolveIOModule
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import kotlinx.datetime.Clock
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.binds
import org.koin.dsl.koinApplication
import org.koin.dsl.module

// TODO: Break down dependencies and move them in their corresponding packages
internal fun initKoin(configuration: Contract.Configuration): KoinApplication {
    return koinApplication {
        modules(
            resolveRootModule(configuration),
            resolveCoreModule(),
            resolveIOModule(),
            resolveStorageModule(),
            resolveUsecaseModule(),
            resolveRepositoryModule(),
            resolveEncryptionModule(),
            resolveServiceModule()
        )
    }
}

internal fun resolveRootModule(configuration: Contract.Configuration): Module {
    return module {
        single {
            configuration
        } binds arrayOf(
            Contract.Configuration::class,
            IOContract.ScopeProvider::class,
            StorageContract.CredentialProvider::class,
            StorageContract.UserSessionTokenProvider::class
        )

        single<Environment> { configuration.getEnvironment() }

        single<Clock> { Clock.System }
    }
}

internal fun resolveCoreModule(): Module {
    return module {
        single {
            HttpClient {
                install(JsonFeature) {
                    serializer =
                        KotlinxSerializer(
                            kotlinx.serialization.json.Json {
                                isLenient = true
                                ignoreUnknownKeys = true
                                allowSpecialFloatingPointValues = true
                                useArrayPolymorphism = false
                            }
                        )
                }
                install(Logging) {
                    logger = SimpleLogger()
                    level = LogLevel.ALL
                }
            }
        }

        // Services
        single<ServiceContract.DonationService> {
            DonationService(get(), get())
        }
    }
}

private class SimpleLogger : Logger {
    override fun log(message: String) {
        println("HttpClient: $message")
    }
}
