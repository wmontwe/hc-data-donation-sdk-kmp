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

package care.data4life.datadonation

import care.data4life.datadonation.DataDonationSDK.Environment
import care.data4life.datadonation.consentdocument.ConsentDocumentContract
import care.data4life.datadonation.mock.fixture.ConsentFixtures.sampleConsentDocument
import care.data4life.datadonation.mock.fixture.ConsentFixtures.sampleUserConsent
import care.data4life.datadonation.mock.stub.UserSessionTokenProviderStub
import care.data4life.datadonation.mock.stub.consentdocument.ConsentDocumentInteractorStub
import care.data4life.datadonation.mock.stub.userconsent.UserConsentInteractorStub
import care.data4life.datadonation.userconsent.UserConsentContract
import care.data4life.sdk.util.test.coroutine.runWithContextBlockingTest
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.context.stopKoin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ClientTest {
    @BeforeTest
    fun setUp() {
        stopKoin()
    }

    @Test
    fun `It fulfils DataDonationFactory`() {
        val factory: Any = Client

        assertTrue(factory is DataDonationSDK.DataDonationClientFactory)
    }

    @Test
    fun `Given getInstance is called with a Configuration it returns a DataDonation`() {
        val client: Any = Client.getInstance(
            Environment.DEV,
            UserSessionTokenProviderStub()
        )

        assertTrue(client is DataDonationSDK.DataDonationClient)
    }

    @Test
    fun `Given createUserConsent is called with a ConsentDocumentVersion and a Language it builds and delegates its Parameter to the Usecase and returns a runnable Flow which emits a UserConsent`() = runWithContextBlockingTest(GlobalScope.coroutineContext) {
        // Given
        val consentFlow = UserConsentInteractorStub()
        val consent = sampleUserConsent

        val version = "23"
        val consentDocumentKey = "custom-consent-key"

        val capturedKey = Channel<String>()
        val capturedVersion = Channel<String>()

        consentFlow.whenCreateUserConsent = { delegatedKey, delegatedVersion ->
            launch {
                capturedKey.send(delegatedKey)
                capturedVersion.send(delegatedVersion)
            }
            consent
        }

        val di = koinApplication {
            modules(
                module {
                    single<UserConsentContract.Interactor> {
                        consentFlow
                    }

                    single<ConsentDocumentContract.Interactor> {
                        ConsentDocumentInteractorStub()
                    }
                }
            )
        }

        val client = Client(di)

        // When
        client.createUserConsent(
            consentDocumentKey,
            version
        ).ktFlow.collect { result ->
            // Then
            assertSame(
                actual = result,
                expected = consent
            )
        }

        assertEquals(
            actual = capturedKey.receive(),
            expected = consentDocumentKey,
        )

        assertEquals(
            actual = capturedVersion.receive(),
            expected = version,
        )
    }

    @Test
    fun `Given fetchConsentDocuments is called with a consentDocumentKey it builds and delegates its Parameter to the Usecase and returns a runnable Flow which emits a List of ConsentDocument`() = runWithContextBlockingTest(GlobalScope.coroutineContext) {
        // Given
        val consentDocumentFlow = ConsentDocumentInteractorStub()
        val documents = listOf(sampleConsentDocument)

        val version = "23"
        val language = "de-j-old-n-kotlin-x-done"
        val consentDocumentKey = "abc"

        val capturedKey = Channel<String>()
        val capturedVersion = Channel<String?>()
        val capturedLanguage = Channel<String?>()

        consentDocumentFlow.whenFetchConsentDocuments = { delegatedKey, delegatedVersion, delegatedLangauge ->
            launch {
                capturedKey.send(delegatedKey)
                capturedVersion.send(delegatedVersion)
                capturedLanguage.send(delegatedLangauge)
            }
            documents
        }

        val di = koinApplication {
            modules(
                module {
                    single<UserConsentContract.Interactor> {
                        UserConsentInteractorStub()
                    }

                    single<ConsentDocumentContract.Interactor> {
                        consentDocumentFlow
                    }
                }
            )
        }

        val client = Client(di)

        // When
        client.fetchConsentDocuments(
            consentDocumentKey,
            version,
            language,
        ).ktFlow.collect { result ->
            // Then
            assertSame(
                expected = documents,
                actual = result
            )
        }

        assertEquals(
            actual = capturedKey.receive(),
            expected = consentDocumentKey
        )

        assertEquals(
            actual = capturedVersion.receive(),
            expected = version,
        )

        assertEquals(
            actual = capturedLanguage.receive(),
            expected = language,
        )
    }

    @Test
    fun `Given fetchUserConsents is called with a consentDocumentKey it builds and delegates its Parameter to the Usecase and returns a runnable Flow which emits a List of UserConsent`() = runWithContextBlockingTest(GlobalScope.coroutineContext) {
        // Given
        val consentFlow = UserConsentInteractorStub()
        val consents = listOf(sampleUserConsent)

        val capturedKey = Channel<String?>()

        consentFlow.whenFetchUserConsents = { delegatedKey ->
            launch {
                capturedKey.send(delegatedKey)
            }
            consents
        }

        val di = koinApplication {
            modules(
                module {
                    single<UserConsentContract.Interactor> {
                        consentFlow
                    }

                    single<ConsentDocumentContract.Interactor> {
                        ConsentDocumentInteractorStub()
                    }
                }
            )
        }

        val consentDocumentKey = "key"
        val client = Client(di)

        // When
        client.fetchUserConsents(
            consentDocumentKey
        ).ktFlow.collect { result ->
            // Then
            assertSame(
                actual = result,
                expected = consents
            )
        }

        assertEquals(
            actual = capturedKey.receive(),
            expected = consentDocumentKey
        )
    }

    @Test
    fun `Given fetchAllUserConsents is called it builds and delegates its Parameter to the Usecase and returns a runnable Flow which emits a List of UserConsent`() = runWithContextBlockingTest(GlobalScope.coroutineContext) {
        // Given
        val consentFlow = UserConsentInteractorStub()
        val consents = listOf(sampleUserConsent)

        val capturedKey = Channel<String?>()

        consentFlow.whenFetchUserConsents = { delegatedKey ->
            launch {
                capturedKey.send(delegatedKey)
            }
            consents
        }

        val di = koinApplication {
            modules(
                module {
                    single<UserConsentContract.Interactor> {
                        consentFlow
                    }

                    single<ConsentDocumentContract.Interactor> {
                        ConsentDocumentInteractorStub()
                    }
                }
            )
        }

        val client = Client(di)

        // When
        client.fetchAllUserConsents().ktFlow.collect { result ->
            // Then
            assertSame(
                actual = result,
                expected = consents
            )
        }

        assertNull(capturedKey.receive())
    }

    @Test
    fun `Given revokeUserConsent is called with a consentDocumentKey it builds and delegates its Parameter to the Usecase and returns a runnable Flow which just runs`() = runWithContextBlockingTest(GlobalScope.coroutineContext) {
        // Given
        val consentFlow = UserConsentInteractorStub()

        val consentDocumentKey = "custom-consent-key"

        val capturedKey = Channel<String>()

        consentFlow.whenRevokeUserConsent = { delegatedKey ->
            launch {
                capturedKey.send(delegatedKey)
            }
            sampleUserConsent
        }

        val di = koinApplication {
            modules(
                module {
                    single<UserConsentContract.Interactor> {
                        consentFlow
                    }

                    single<ConsentDocumentContract.Interactor> {
                        ConsentDocumentInteractorStub()
                    }
                }
            )
        }

        val client = Client(di)

        // When
        client.revokeUserConsent(consentDocumentKey).ktFlow.collect { result ->
            // Then
            assertSame(
                actual = result,
                expected = sampleUserConsent
            )
        }

        assertEquals(
            actual = capturedKey.receive(),
            expected = consentDocumentKey
        )
    }
}
