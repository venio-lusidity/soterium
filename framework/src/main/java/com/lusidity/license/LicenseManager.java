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

package com.lusidity.license;

import com.lusidity.framework.security.RsaX;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Objects;

public class LicenseManager
{
	private File licenseFile = null;
	private File publicKeyFile = null;
	private File privateKeyFile = null;
	private PrivateKey privateKey = null;
	private PublicKey publickKey = null;
	private JSONObject license = null;
	private DateTime expiresOn = null;
	private LicenseManager.AccessLevel accessLevel = LicenseManager.AccessLevel.none;

	public enum AccessLevel{
		none,
		trial,
		monthly,
		quarterly,
		semi_annually,
		annually,
		lifeTime,
		developer,
		community,
		enterprise
	}

	/**
	 * Construct a LicenseManager
	 * @param publicKeyFile A public RSA DER certificate file.
	 * @param privateKeyFile A private RSA DER certificate file.
	 */
	public LicenseManager(File publicKeyFile, File privateKeyFile)
		throws IOException, GeneralSecurityException
	{
		super();
		this.publicKeyFile = publicKeyFile;
		this.privateKeyFile = privateKeyFile;
		this.load();
	}

	/**
	 * Construct a LicenseManager
	 * @param publicKeyFile A public RSA DER certificate file.
	 * @param privateKeyFile A private RSA DER certificate file.
	 * @param licenseFile The license file to load.
	 */
	public LicenseManager(File publicKeyFile, File privateKeyFile, File licenseFile)
		throws IOException, GeneralSecurityException
	{
		super();
		this.publicKeyFile = publicKeyFile;
		this.privateKeyFile = privateKeyFile;
		this.licenseFile = licenseFile;
		this.load();
	}

	/**
	 * Load the public and private keys/certificates.
	 */
	private void load()
		throws IOException, GeneralSecurityException
	{
		if(null!=this.privateKeyFile)
		{
			this.privateKey=RsaX.getPrivateKey(this.privateKeyFile);
		}
		if(null!=this.publicKeyFile)
		{
			this.publickKey=RsaX.getPublicKey(this.publicKeyFile);
		}
		if((null!=this.licenseFile) && (null!=this.privateKey)){
			this.license = new JSONObject(this.getString());
			this.expiresOn = DateTime.parse(this.license.getString("expiresOn"));
			this.accessLevel = Enum.valueOf(LicenseManager.AccessLevel.class, this.license.getString("accessLevel"));
		}
	}

	/**
	 * Make a license file with specified attributes.
	 * @param licenseFileDir The directory in which to save the file in.
	 * @param fileName  The name of the file without an extension.
	 * @param expiresOn  The date the license should expire on.
	 * @param accessLevel The accessLevel of the file, default none, must be change to something else or the file is considered expired.
	 * @param keyValuePairs  Additional properties to apply to the file for custom uses.
	 * @return The created licensed file.
	 * @throws Exception  May throw exceptions.
	 */
	public File create(File licenseFileDir, String fileName, DateTime expiresOn, LicenseManager.AccessLevel accessLevel, Object... keyValuePairs)
		throws Exception
	{
		if(!licenseFileDir.isDirectory()){
			throw new IOException(String.format("The directory does not exists, %s.", licenseFileDir.getAbsolutePath()));
		}
		if(null==expiresOn){
			throw new InvalidParameterException("The parameter expiresOn cannot be null.");
		}
		if(null==accessLevel){
			throw new InvalidParameterException("The parameter accessLevel cannot be null.");
		}

		DateTime expires = expiresOn.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);

		JSONObject data = new JSONObject();
		data.put("expiresOn", expires);
		data.put("accessLevel", accessLevel);

		if ((null!=keyValuePairs) && (keyValuePairs.length>0))
		{
			int nParams=keyValuePairs.length;
			if ((nParams%2)!=0)
			{
				throw new InvalidParameterException("keyValuePairs must be key/value pairs expressed as Object pairs.");
			}
			for (int keyIndex=0; keyIndex<(nParams-1); keyIndex+=2)
			{
				int valueIndex=keyIndex+1;
				String key=keyValuePairs[keyIndex].toString();
				Object value=keyValuePairs[valueIndex];
				data.put(key, value);
			}
		}
		String content = RsaX.encrypt(data.toString(), this.publickKey);
		File result = new File(licenseFileDir, String.format("%s.lic", fileName));
		FileUtils.writeByteArrayToFile(result, content.getBytes(Charset.forName("UTF-8")));
		return result;
	}

	/**
	 * Load the license file into a string.
	 * @return A string representation of the license file.
	 * @throws IOException An IOException
	 */
	@SuppressWarnings("CaughtExceptionImmediatelyRethrown")
	public String getString()
		throws IOException, GeneralSecurityException
	{
		StringBuilder sb = new StringBuilder();
		try(FileInputStream fis = new FileInputStream(this.licenseFile)) {
			try(BufferedReader br = new BufferedReader(new InputStreamReader(fis))){
				String line;
				//noinspection NestedAssignment
				while ((line = br.readLine())!=null){
					sb.append(line);
				}
			}
			catch (IOException ex){
				//noinspection ThrowCaughtLocally
				throw ex;
			}
		}
		catch (IOException ex){
			throw ex;
		}
		String result = null;
		if(sb.length()>0){
			result = RsaX.decrypt(sb.toString(), this.privateKey);
		}
		return result;
	}

	/**
	 * @return The date the license expires on.
	 */
	public DateTime getExpiresOn()
	{
		return this.expiresOn;
	}

	public String getString(String key){
		return (null!=this.license) ? this.license.getString(key) : null;
	}

	public Boolean getBoolean(String key){
		return (null!=this.license) ? this.license.getBoolean(key) : null;
	}

	public Long getLong(String key){
		return (null!=this.license) ? this.license.getLong(key) : null;
	}

	public DateTime getDateTime(String key){
		DateTime result = null;
		String temp = this.getString(key);
		try{
			result = DateTime.parse(temp);
		}
		catch (Exception ignored){}
		return result;
	}

	/**
	 * Is this license file valid, the file must exist, most not have an AccessLevel of none and current date is before expired date.
	 * @return true or false
	 */
	public boolean isLicenseValid(){
		boolean result = (null!=this.license);
		if(result){
			result = !Objects.equals(this.getAccessLevel(), LicenseManager.AccessLevel.none);
		}
		if(result && (null!=this.expiresOn)){
			DateTime current = DateTime.now();
			result = current.isBefore(this.expiresOn);
		}
		return result;
	}

	public LicenseManager.AccessLevel getAccessLevel()
	{
		return this.accessLevel;
	}
}
