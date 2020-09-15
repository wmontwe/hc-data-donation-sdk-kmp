//  BSD 3-Clause License
//
//  Copyright (c) 2019, HPS Gesundheitscloud gGmbH
//  All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are met:
//
//  * Redistributions of source code must retain the above copyright notice, this
//  list of conditions and the following disclaimer.
//
//  * Redistributions in binary form must reproduce the above copyright notice,
//  this list of conditions and the following disclaimer in the documentation
//  and/or other materials provided with the distribution.
//
//  * Neither the name of the copyright holder nor the names of its
//  contributors may be used to endorse or promote products derived from
//  this software without specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
//  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
//  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
//  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
//  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
//  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
//  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
//  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
//  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
//  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
import Foundation
import CryptoSwift

@objc public class CryptoAES: NSObject {
    private var padding: Padding
    
    @objc public override init() {
        //TODO: Add padding as a param
        self.padding = .noPadding
        super.init()
    }
    
    @objc public func encrypt(key: Data, plainText: Data, associatedData: Data) throws -> Data {
            let blockMode = GCM(iv: associatedData.bytes, mode: .combined)
            let aes = try AES(key: key.bytes, blockMode: blockMode, padding: padding)
            let ciphertext = try aes.encrypt(plainText.bytes)
            
            return Data(ciphertext)
    }
    
    @objc public func decrypt(key: Data, encrypted: Data, associatedData: Data) throws -> Data {
            let block = GCM(iv: associatedData.bytes, additionalAuthenticatedData: nil, mode: .combined)
            let aes = try AES(key: key.bytes, blockMode: block, padding: padding)
            let ciphertext = try aes.decrypt(encrypted.bytes)
            
            return Data(ciphertext)
    }
}
