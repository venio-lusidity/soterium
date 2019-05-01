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

import com.lusidity.framework.reports.ReportHandler;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RsaX
{

	/**
	 *
	 * @param file The der public certificate.
	 * @return A PublicKey
	 */
	public static PublicKey getPublicKey(File file){
		PublicKey result = null;
		try(FileInputStream fis = new FileInputStream(file)){
			byte[] keyBytes = null;
			try(DataInputStream dis = new DataInputStream(fis)){
				keyBytes = new byte[dis.available()];
				dis.readFully(keyBytes);
			}
			catch (Exception ex){
				if(null!=ReportHandler.getInstance()){
					ReportHandler.getInstance().warning(ex);
				}
			}
			if(null!=keyBytes)
			{
				X509EncodedKeySpec spec=new X509EncodedKeySpec(keyBytes);
				KeyFactory kf=KeyFactory.getInstance("RSA");
				result=kf.generatePublic(spec);
			}
		}
		catch (Exception ex){
			if(null!=ReportHandler.getInstance()){
				ReportHandler.getInstance().warning(ex);
			}
		}
		return result;
	}

	/**
	 *
	 * @param file The der private key.
	 * @return A PrivateKey
	 */
	public static PrivateKey getPrivateKey(File file){
		PrivateKey result = null;
		try(FileInputStream fis = new FileInputStream(file))
		{
			try(DataInputStream dis=new DataInputStream(fis))
			{
				byte[] keyBytes=new byte[(int) file.length()];
				dis.readFully(keyBytes);
				dis.close();

				PKCS8EncodedKeySpec spec=new PKCS8EncodedKeySpec(keyBytes);
				KeyFactory kf=KeyFactory.getInstance("RSA");
				result = kf.generatePrivate(spec);
			}
			catch (Exception ignored){}
		}
		catch (Exception ignored){}
		return result;
	}

	public static String sign(PrivateKey privateKey, String message) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException {
		Signature sign = Signature.getInstance("SHA1withRSA");
		sign.initSign(privateKey);
		sign.update(message.getBytes("UTF-8"));
		return new String(Base64.encodeBase64(sign.sign()), "UTF-8");
	}


	public static boolean verify(PublicKey publicKey, String message, String signature) throws SignatureException, NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
		Signature sign = Signature.getInstance("SHA1withRSA");
		sign.initVerify(publicKey);
		sign.update(message.getBytes("UTF-8"));
		return sign.verify(Base64.decodeBase64(signature.getBytes("UTF-8")));
	}

	public static String encrypt(String rawText, PublicKey publicKey) throws IOException, GeneralSecurityException {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		return Base64.encodeBase64String(cipher.doFinal(rawText.getBytes("UTF-8")));
	}

	public static String decrypt(String cipherText, PrivateKey privateKey) throws IOException, GeneralSecurityException {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return new String(cipher.doFinal(Base64.decodeBase64(cipherText)), "UTF-8");
	}
}
