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

package care.data4life.datadonation.lang

import care.data4life.sdk.lang.D4LRuntimeException

sealed class CoreRuntimeException(
    message: String?,
    cause: Throwable?
) : D4LRuntimeException(message = message, cause = cause) {

    class InternalFailure : CoreRuntimeException(message = "Internal failure", cause = null)
    class RequestValidationFailure(message: String) : CoreRuntimeException(message = message, cause = null)
    class ResponseTransformFailure : CoreRuntimeException(message = "Unexpected Response", cause = null)
    class MissingCredentialsException(cause: Throwable? = null) : CoreRuntimeException(cause = cause, message = null)
    class MissingSessionException(cause: Throwable? = null) : CoreRuntimeException(cause = cause, message = null)
}
