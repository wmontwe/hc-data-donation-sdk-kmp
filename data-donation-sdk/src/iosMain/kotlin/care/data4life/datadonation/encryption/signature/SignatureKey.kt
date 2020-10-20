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

package care.data4life.datadonation.encryption.signature

import care.data4life.datadonation.encryption.*
import platform.CoreFoundation.CFStringRef
import platform.Security.*

actual fun SignatureKeyPrivate(size: Int, algorithm: Algorithm.Signature): SignatureKeyPrivate {
    val params: Pair<CFStringRef, SecKeyAlgorithm> = algorithm.toAttributes()
    return SignatureKeyNative(params.first, params.second, size)
}


private fun Algorithm.Signature.toAttributes(): Pair<CFStringRef, SecKeyAlgorithm> {
    return when (this) {
        is Algorithm.Signature.RsaPSS -> {
            kSecAttrKeyTypeRSA!! to when (hashSize) {
                HashSize.Hash256 -> kSecKeyAlgorithmRSASignatureDigestPSSSHA256!!
            }
        }
    }
}

actual fun SignatureKeyPrivate(
    serializedPrivate: ByteArray,
    serializedPublic: ByteArray,
    size: Int,
    algorithm: Algorithm.Signature
): SignatureKeyPrivate {


    return SignatureKeyNative(
        KeyNative.buildSecKeyRef(serializedPrivate, algorithm, KeyNative.KeyType.Public),
        KeyNative.buildSecKeyRef(serializedPublic, algorithm, KeyNative.KeyType.Private),
        algorithm.toAttributes().first
    )
}

actual fun SignatureKeyPublic(
    serialized: ByteArray,
    size: Int,
    algorithm: Algorithm.Signature
): SignatureKeyPublic {
    return SignatureKeyNative(
    KeyNative.buildSecKeyRef(serialized, algorithm, KeyNative.KeyType.Public),
    KeyNative.buildSecKeyRef(serialized, algorithm, KeyNative.KeyType.Private),
    algorithm.toAttributes().first
    )
}
