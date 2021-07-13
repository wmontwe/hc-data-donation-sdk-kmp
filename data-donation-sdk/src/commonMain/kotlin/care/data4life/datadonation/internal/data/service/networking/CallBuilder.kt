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

import care.data4life.datadonation.core.model.ModelContract.Environment
import care.data4life.datadonation.internal.data.service.networking.Networking.CallBuilder.Companion.ACCESS_TOKEN_FIELD
import care.data4life.datadonation.internal.data.service.networking.Networking.CallBuilder.Companion.ACCESS_TOKEN_VALUE_PREFIX
import care.data4life.datadonation.lang.CoreRuntimeError
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.host
import io.ktor.client.request.parameter
import io.ktor.client.request.port
import io.ktor.client.request.request
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.URLProtocol
import io.ktor.http.contentType

internal class CallBuilder private constructor(
    private val client: HttpClient,
    private val host: String,
    private val protocol: URLProtocol,
    private val port: Int?
) : Networking.CallBuilder {
    private var headers: Header = mapOf()
    private var parameter: Parameter = mapOf()
    private var accessToken: AccessToken? = null
    private var useJson: Boolean = false
    private var body: Any? = null

    override fun setHeaders(header: Header): Networking.CallBuilder {
        return this.also { this.headers = header }
    }

    override fun setParameter(parameter: Parameter): Networking.CallBuilder {
        return this.also { this.parameter = parameter }
    }

    override fun setAccessToken(token: AccessToken): Networking.CallBuilder {
        return this.also { this.accessToken = token }
    }

    override fun useJsonContentType(): Networking.CallBuilder {
        return this.also { this.useJson = true }
    }

    override fun setBody(body: Any): Networking.CallBuilder {
        return this.also { this.body = body }
    }

    private fun validateBodyAgainstMethod(method: Networking.Method) {
        if (body != null) {
            if (method == Networking.Method.GET) {
                throw CoreRuntimeError.RequestValidationFailure(
                    "GET cannot be combined with a RequestBody."
                )
            }
        } else {
            if (method != Networking.Method.GET) {
                throw CoreRuntimeError.RequestValidationFailure(
                    "${method.name.toUpperCase()} must be combined with a RequestBody."
                )
            }
        }
    }

    private fun setBody(builder: HttpRequestBuilder) {
        if (body != null) {
            builder.body = body!!
        }
    }

    private fun setMandatoryFields(
        builder: HttpRequestBuilder,
        method: Networking.Method,
        path: Path
    ) {
        validateBodyAgainstMethod(method)

        builder.host = host
        builder.method = HttpMethod(method.name)
        builder.url.protocol = protocol
        builder.url.path(path)
        setBody(builder)
    }

    private fun setPort(builder: HttpRequestBuilder) {
        if (port is Int) {
            builder.port = port
        }
    }

    private fun addHeader(builder: HttpRequestBuilder) {
        headers.forEach { (field, value) ->
            builder.header(field, value)
        }
    }

    private fun setParameter(builder: HttpRequestBuilder) {
        parameter.forEach { (field, value) ->
            builder.parameter(field, value)
        }
    }

    private fun setAccessToken(builder: HttpRequestBuilder) {
        if (accessToken is String) {
            builder.header(
                ACCESS_TOKEN_FIELD,
                "$ACCESS_TOKEN_VALUE_PREFIX $accessToken"
            )
        }
    }

    private fun setContentType(builder: HttpRequestBuilder) {
        if (useJson) {
            builder.contentType(ContentType.Application.Json)
        }
    }

    private fun buildQuery(
        builder: HttpRequestBuilder,
        method: Networking.Method,
        path: Path
    ) {
        setMandatoryFields(builder, method, path)
        setPort(builder)
        addHeader(builder)
        setParameter(builder)
        setAccessToken(builder)
        setContentType(builder)
    }

    override suspend fun execute(
        method: Networking.Method,
        path: Path
    ): Any = client.request { buildQuery(this, method, path) }

    companion object : Networking.CallBuilderFactory {
        override fun getInstance(
            environment: Environment,
            client: HttpClient,
            port: Int?
        ): Networking.CallBuilder {
            return CallBuilder(
                client,
                environment.url,
                URLProtocol.HTTPS,
                port
            )
        }
    }
}
