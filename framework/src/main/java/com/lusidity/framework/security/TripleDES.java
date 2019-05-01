/*
 * Copyright 2018 lusidity inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.lusidity.framework.security;


import com.lusidity.framework.exceptions.ApplicationException;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.security.spec.KeySpec;

public class TripleDES {
    private static final String UNICODE_FORMAT = "UTF8";
    public static final String DESEDE_ENCRYPTION_SCHEME = "DESede";
    private Cipher cipher;
    byte[] arrayBytes;
    private SecretKey key;

    public TripleDES(String encryptionKey) throws Exception {
        super();
        String encryptionScheme = TripleDES.DESEDE_ENCRYPTION_SCHEME;
        this.arrayBytes = encryptionKey.getBytes(TripleDES.UNICODE_FORMAT);
        KeySpec keySpec = new DESedeKeySpec(this.arrayBytes);
        SecretKeyFactory secretKeyFactor = SecretKeyFactory.getInstance(encryptionScheme);
        this.cipher = Cipher.getInstance(encryptionScheme);
        this.key = secretKeyFactor.generateSecret(keySpec);
    }


    public String encrypt(String unencryptedString) throws ApplicationException
    {
        String encryptedString = null;
        try {
            this.cipher.init(Cipher.ENCRYPT_MODE, this.key);
            byte[] plainText = unencryptedString.getBytes(TripleDES.UNICODE_FORMAT);
            byte[] encryptedText = this.cipher.doFinal(plainText);
            encryptedString = new String(Base64.encodeBase64(encryptedText));
        } catch (Exception e) {
            throw new ApplicationException(e);
        }
        return encryptedString;
    }

    public String decrypt(String encryptedString) throws ApplicationException {
        String decryptedText=null;
        try {
            this.cipher.init(Cipher.DECRYPT_MODE, this.key);
            byte[] encryptedText = Base64.decodeBase64(encryptedString);
            byte[] plainText = this.cipher.doFinal(encryptedText);
            decryptedText= new String(plainText);
        } catch (Exception e) {
            throw new ApplicationException(e);
        }
        return decryptedText;
    }
}
