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

package care.data4life.datadonation.internal.data.service.networking

import care.data4life.datadonation.mock.fake.defaultResponse
import care.data4life.datadonation.mock.stub.service.networking.HttpFeatureConfiguratorStub
import care.data4life.datadonation.mock.stub.service.networking.HttpResponseValidatorConfiguratorStub
import care.data4life.datadonation.mock.stub.service.networking.plugin.HttpErrorPropagatorStub
import care.data4life.datadonation.mock.stub.service.networking.plugin.HttpSuccessfulResponseValidatorStub
import care.data4life.sdk.util.test.runWithContextBlockingTest
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.features.HttpCallValidator
import io.ktor.client.features.HttpClientFeature
import io.ktor.util.AttributeKey
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.native.concurrent.ThreadLocal
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class HttpClientConfiguratorTest {
    @BeforeTest
    fun setUp() {
        Counter.amount = 0
    }

    @Test
    fun `It fulfils HttpClientConfigurator`() {
        val configurator: Any = HttpClientConfigurator

        assertTrue(configurator is Networking.HttpClientConfigurator)
    }

    @Test
    fun `Given configure is called with a HttpClientConfig and a List of HttpFeatureInstaller it installs a given Feature`() = runWithContextBlockingTest(GlobalScope.coroutineContext) {
        // Given
        val capturedPluginConfig = Channel<Any>()
        val capturedSubConfig = Channel<Any?>()

        val subConfig = object {}
        val stubFeatureConfigurator = HttpFeatureConfiguratorStub<Any, Any?>()

        stubFeatureConfigurator.whenConfigure = { pluginConfig, subConfiguration ->
            launch {
                capturedPluginConfig.send(pluginConfig)
                capturedSubConfig.send(subConfiguration)
            }
        }

        val features = listOf(
            Networking.HttpFeatureInstaller(
                FeatureStub,
                stubFeatureConfigurator,
                subConfig,
            )
        )

        // When
        HttpClient(MockEngine) {
            HttpClientConfigurator.configure(
                this,
                features
            )

            engine {
                addHandler {
                    defaultResponse(this)
                }
            }
        }

        // Then
        assertTrue(capturedPluginConfig.receive() is FeatureStub.Config)
        assertEquals(
            actual = capturedSubConfig.receive(),
            expected = subConfig
        )
    }

    @Test
    fun `Given configure is called with a HttpClientConfig and a List of HttpFeatureInstaller it installs a arbitrary number of Features`() = runWithContextBlockingTest(GlobalScope.coroutineContext) {
        // Given
        val subConfig = object {}
        val stubFeatureConfigurator = HttpFeatureConfiguratorStub<Any, Any?>()

        stubFeatureConfigurator.whenConfigure = { _, _ ->
            Counter.amount++
        }

        val features = listOf(
            Networking.HttpFeatureInstaller(
                FeatureStub,
                stubFeatureConfigurator,
                subConfig,
            ),
            Networking.HttpFeatureInstaller(
                FeatureStub,
                stubFeatureConfigurator,
                subConfig,
            ),
            Networking.HttpFeatureInstaller(
                FeatureStub,
                stubFeatureConfigurator,
                subConfig,
            )
        )

        // When
        HttpClient(MockEngine) {
            HttpClientConfigurator.configure(
                this,
                features
            )

            engine {
                addHandler {
                    defaultResponse(this)
                }
            }
        }

        // Then
        assertEquals(
            actual = Counter.amount,
            expected = features.size
        )
    }

    @Test
    fun `Given configure is called with a HttpClientConfig and a HttpResponseValidation, it calls the Configurator with its appropriate parameter`() = runWithContextBlockingTest(GlobalScope.coroutineContext) {
        // Given
        val configurator = HttpResponseValidatorConfiguratorStub()
        val successfulResponse = HttpSuccessfulResponseValidatorStub()
        val errorPropagator = HttpErrorPropagatorStub()

        val responseValidation = Networking.HttpResponseValidation(
            configurator,
            successfulResponse,
            errorPropagator
        )

        val capturedResponseConfig = Channel<HttpCallValidator.Config>()
        val capturedSuccessfulResponse = Channel<Networking.HttpSuccessfulResponseValidator>()
        val capturedErrorPropagation = Channel<Networking.HttpErrorPropagator>()

        configurator.whenConfigure = { delegatedResponseConfig, delegatedSuccessfulResponse, delegatedErrorPropagation ->
            launch {
                capturedResponseConfig.send(delegatedResponseConfig)
                capturedSuccessfulResponse.send(delegatedSuccessfulResponse!!)
                capturedErrorPropagation.send(delegatedErrorPropagation!!)
            }
        }

        // When
        HttpClient {
            HttpClientConfigurator.configure(
                this,
                responseValidator = responseValidation
            )
        }

        // Then
        val pluginConfig: Any = capturedResponseConfig.receive()
        assertTrue(pluginConfig is HttpCallValidator.Config)
        assertSame(
            actual = capturedSuccessfulResponse.receive(),
            expected = successfulResponse
        )
        assertSame(
            actual = capturedErrorPropagation.receive(),
            expected = errorPropagator
        )
    }

    @ThreadLocal
    private object Counter {
        var amount = 0
    }

    private class FeatureStub {
        class Config

        companion object Feature : HttpClientFeature<Config, FeatureStub> {
            override val key: AttributeKey<FeatureStub> = AttributeKey("FeatureStub")

            override fun install(feature: FeatureStub, scope: HttpClient) = Unit

            override fun prepare(block: Config.() -> Unit): FeatureStub {
                Config().apply(block)
                return FeatureStub()
            }
        }
    }
}
