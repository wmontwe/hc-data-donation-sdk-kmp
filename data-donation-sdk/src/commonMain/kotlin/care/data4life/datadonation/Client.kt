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

package care.data4life.datadonation

import care.data4life.datadonation.core.listener.ListenerContract
import care.data4life.datadonation.core.model.ConsentDocument
import care.data4life.datadonation.core.model.UserConsent
import care.data4life.datadonation.internal.di.initKoin
import care.data4life.datadonation.internal.domain.usecases.*
import care.data4life.datadonation.internal.io.IOInternalContract
import kotlinx.coroutines.launch
import org.koin.core.KoinApplication

class Client internal constructor(
    private val configuration: Contract.Configuration,
    koinApplication: KoinApplication
) : Contract.DataDonation {
    private val createUserContent: UsecaseContract.CreateUserConsent by koinApplication.koin.inject()
    private val fetchConsentDocuments: UsecaseContract.FetchConsentDocuments by koinApplication.koin.inject()
    private val fetchUserConsents: UsecaseContract.FetchUserConsents by koinApplication.koin.inject()
    private val revokeUserConsent: UsecaseContract.RevokeUserConsent by koinApplication.koin.inject()
    private val usecaseRunner: IOInternalContract.UsecaseRunner by koinApplication.koin.inject()

    override fun fetchConsentDocuments(
        consentDocumentVersion: Int?,
        language: String?,
        consentKey: String,
        listener: ListenerContract.ResultListener<List<ConsentDocument>>
    ) {
        val parameter = FetchConsentDocuments.Parameter(
            version = consentDocumentVersion,
            language = language,
            consentKey = consentKey
        )

        usecaseRunner.run(
            listener,
            fetchConsentDocuments,
            parameter
        )
    }

    override fun createUserConsent(
        consentKey: String,
        consentDocumentVersion: Int,
        listener: ListenerContract.ResultListener<UserConsent>
    ) {
        val parameter = CreateUserConsent.Parameter(
            configuration.getDonorKeyPair(),
            consentKey,
            consentDocumentVersion
        )

        usecaseRunner.run(
            listener,
            createUserContent,
            parameter
        )
    }

    override fun fetchUserConsents(
        consentKey: String,
        listener: ListenerContract.ResultListener<List<UserConsent>>
    ) {
        val parameter = FetchUserConsents.Parameter(consentKey)

        usecaseRunner.run(
            listener,
            fetchUserConsents,
            parameter
        )
    }

    override fun fetchAllUserConsents(
        listener: ListenerContract.ResultListener<List<UserConsent>>
    ) {
        val parameter = FetchUserConsents.Parameter()

        usecaseRunner.run(
            listener,
            fetchUserConsents,
            parameter
        )
    }

    override fun revokeUserConsent(
        consentKey: String,
        callback: ListenerContract.Callback
    ) {
        val parameter = RevokeUserConsent.Parameter(consentKey)

        usecaseRunner.run(
            callback,
            revokeUserConsent,
            parameter
        )
    }

    // TODO: Remove -> Wrong level of abstraction
    private fun <ReturnType : Any> Usecase<ReturnType>.runForListener(
        listener: ListenerContract.ResultListener<ReturnType>
    ) {
        configuration.getCoroutineScope().launch {
            try {
                listener.onSuccess(this@runForListener.execute())
            } catch (ex: Exception) {
                listener.onError(ex)
            }
        }
    }

    // TODO: Remove -> Wrong level of abstraction
    private fun <ReturnType : Any> Usecase<ReturnType>.runForListener(
        listener: ListenerContract.Callback
    ) {
        configuration.getCoroutineScope().launch {
            try {
                execute()
                listener.onSuccess()
            } catch (ex: Exception) {
                listener.onError(ex)
            }
        }
    }

    companion object Factory : Contract.DataDonationFactory {
        override fun getInstance(
            configuration: Contract.Configuration
        ): Contract.DataDonation {
            return Client(configuration, initKoin(configuration))
        }
    }
}
