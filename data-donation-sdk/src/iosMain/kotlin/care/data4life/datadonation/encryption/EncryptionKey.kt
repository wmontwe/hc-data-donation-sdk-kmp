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

package care.data4life.datadonation.encryption

import platform.CoreFoundation.CFStringRef
import platform.Security.SecKeyAlgorithm
import platform.Security.kSecAttrKeyTypeRSA
import platform.Security.kSecKeyAlgorithmRSAEncryptionOAEPSHA256
import platform.Security.kSecKeyAlgorithmRSASignatureDigestPSSSHA256


actual fun EncryptionSymmetricKey(size: Int, algorithm: Algorithm.Symmetric): EncryptionSymmetricKey {
    val params: Pair<CFStringRef, SecKeyAlgorithm> = algorithm.toAttributes()
    return EncryptionSymmetricKeyNative(params.first, params.second, size)
}

private fun Algorithm.Symmetric.toAttributes(): Pair<CFStringRef, SecKeyAlgorithm> {
    return when (this) {
        is Algorithm.Symmetric.AES -> TODO()
    }
}


actual fun EncryptionSymmetricKey(
    serializedKey: ByteArray,
    size: Int,
    algorithm: Algorithm.Symmetric
): EncryptionSymmetricKey {

    return EncryptionSymmetricKeyNative(
        KeyNative.buildSecKeyRef(serializedKey, algorithm, KeyNative.KeyType.Symmetric),
        algorithm.toAttributes().first
    )
}

