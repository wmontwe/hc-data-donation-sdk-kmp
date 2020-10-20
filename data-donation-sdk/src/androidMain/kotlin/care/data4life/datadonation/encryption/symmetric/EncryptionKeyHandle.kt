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

package care.data4life.datadonation.encryption.symmetric

import care.data4life.datadonation.encryption.Asn1Exportable
import care.data4life.datadonation.encryption.KeyHandleTink
import care.data4life.datadonation.encryption.protos.PublicHandle
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplate
import com.google.crypto.tink.KeysetHandle
import kotlinx.serialization.DeserializationStrategy

class EncryptionKeySymmetricHandle<Proto> :
    KeyHandleTink<Proto>, EncryptionSymmetricKey
        where Proto : Asn1Exportable,
              Proto : PublicHandle {

    constructor(keyTemplate: KeyTemplate, deserializer: DeserializationStrategy<Proto>)
            : super(keyTemplate, deserializer)

    constructor(handle: KeysetHandle, deserializer: DeserializationStrategy<Proto>)
            : super(handle, deserializer)

    constructor(serializedKeyset: ByteArray, deserializer: DeserializationStrategy<Proto>)
            : super(serializedKeyset, deserializer)


    override val pkcs8: String
        get() = deserializePrivate()

    override fun serialized(): ByteArray = serializePrivate()

    override fun encrypt(plainText: ByteArray, associatedData: ByteArray): ByteArray {
        return handle.getPrimitive(Aead::class.java)
            .encrypt(plainText,associatedData)
    }

    override fun decrypt(encrypted: ByteArray, associatedData: ByteArray):Result<ByteArray> {
         return kotlin.runCatching { handle.getPrimitive(Aead::class.java)
            .decrypt(encrypted,associatedData) }
    }


}





