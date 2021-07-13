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

import io.ktor.client.HttpClientConfig
import io.ktor.client.features.HttpClientFeature
import io.ktor.client.features.HttpResponseValidator

internal object ClientConfigurator : Networking.ClientConfigurator {
    override fun configure(
        config: HttpClientConfig<*>,
        installers: Map<HttpClientFeature<*, *>, Pair<Networking.Configurator<Any, Any>, Any>>,
        responseValidator: Pair<Networking.ResponseValidatorConfigurator, Pair<Networking.ResponseValidator?, Networking.ErrorPropagator?>>
    ) {
        installers.forEach { (plugin, configurator) ->
            config.install(plugin) {
                configurator.first.configure(
                    this,
                    configurator.second
                )
            }
        }

        val (configurator, auxiliary) = responseValidator
        config.HttpResponseValidator { configurator.configure(this, auxiliary) }
    }
}
