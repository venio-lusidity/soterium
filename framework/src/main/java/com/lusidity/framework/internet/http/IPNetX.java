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

package com.lusidity.framework.internet.http;


import com.lusidity.framework.collections.CollectionX;
import com.lusidity.framework.regex.RegExHelper;
import com.lusidity.framework.text.StringX;

import java.net.InetAddress;
import java.util.Collection;
import java.util.regex.Pattern;

public
class IPNetX
{

	public static final Pattern IPV6=Pattern.compile("^([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4}$");
	@SuppressWarnings("unused")
	public static final Pattern IPV4=Pattern.compile(
		"([0-2]?[0-5]?[0-6]{1})\\.([0-2]?[0-5]?[0-6]{1})\\.([0-2]?[0-5]?[0-6]{1})\\.([0-2]?[0-5]?[0-6]{1})"
	);
	private static final Collection<String> TOP_LEVEL_DOMAINS=
		CollectionX.addAll(IPNetX.TOP_LEVEL_DOMAINS, "com", "org", "net", "int", "edu", "gov", "mil");
	public static final int MAX_PORT_RANGE = 65535;
	public static final int MIN_PORT_RANGE = 1;
	public static final int MAC_LENGTH=12;

	public static
	boolean isIPv6(String ipv6)
	{
		boolean result=false;
		if (!StringX.isBlank(ipv6))
		{
			result=IPNetX.IPV6.matcher(ipv6).matches();
		}
		return result;
	}

	public static
	boolean isIPv4(String ipv4)
	{
		boolean result=true;
		if (!StringX.isBlank(ipv4))
		{
			String[] parts=StringX.split(ipv4, ".");
			if ((null!=parts) && (parts.length==4))
			{
				for (String part : parts)
				{
					try
					{
						int num=Integer.parseInt(part);
						result=(num>=0) && (num<256);
					}
					catch (Exception ignored)
					{
						result=false;
					}
					if (!result)
					{
						break;
					}
				}
			}
			else
			{
				result=false;
			}
		}
		return result;
	}

	public static
	boolean isValidIP(String ip)
	{
		return (IPNetX.isIPv4(ip) || IPNetX.isIPv6(ip));
	}

	public static
	String getHostName(String ipOrFqdn, boolean resolveIfIp)
	{
		String result=null;
		if (!StringX.isBlank(ipOrFqdn))
		{
			if (IPNetX.isFQDN(ipOrFqdn))
			{
				String[] parts=StringX.split(ipOrFqdn, ".");
				if ((null!=parts) && (parts.length>=3))
				{
					result=parts[0];
				}
			}
			else
			{
				try
				{
					if (resolveIfIp)
					{
						InetAddress inetAddress=InetAddress.getByName(ipOrFqdn);
						result=inetAddress.getHostName();
					}
				}
				catch (Exception ignored)
				{
				}
			}
		}
		return (!StringX.isBlank(result) && !StringX.contains(result, " ")) ? result : null;
	}

	public static
	boolean isValidMac(String mac)
	{
		String test = StringX.removeNonAlphaNumericCharacters(mac);
		if (!StringX.isBlank(test))
		{
			if((test.length()==IPNetX.MAC_LENGTH))
			{
				test=StringX.insertPeriodically(test, ":", 2);
			}
			else if (mac.length()==17)
			{
				// TODO: I am not sure why i have this one, look into it.
				test=Pattern.compile("[^0-9a-zA-Z]+").matcher(test).replaceAll(":");
			}
		}

		return !StringX.isBlank(test) && RegExHelper.MAC.matcher(test).matches();
	}

	public static
	String getMac(String mac)
	{
		String result = StringX.removeNonAlphaNumericCharacters(mac);
		if (!StringX.isBlank(result))
		{
			if((result.length()==IPNetX.MAC_LENGTH))
			{
				result=StringX.insertPeriodically(result, ":", 2);
			}
			else if (mac.length()==17)
			{
				// TODO: I am not sure why i have this one, look into it.
				result=Pattern.compile("[^0-9a-zA-Z]+").matcher(result).replaceAll(":");
			}
		}
		return IPNetX.isValidMac(result) ? result : "";
	}

	public static
	boolean isFQDN(String displayID)
	{
		boolean result=false;
		if (!StringX.isBlank(displayID) && !IPNetX.isIPv4(displayID) && !IPNetX.isIPv6(displayID))
		{
			String[] parts=StringX.split(displayID.trim().toLowerCase(), ".");
			if (null!=parts)
			{
				String tld=parts[parts.length-1];
				result=(!StringX.isBlank(tld) && IPNetX.TOP_LEVEL_DOMAINS.contains(tld) && (parts.length>=3));
			}
		}
		return result;
	}

	public static
	String getDomainName(String fqdn)
	{
		StringBuilder result= new StringBuilder();
		if (!StringX.isBlank(fqdn) && IPNetX.isFQDN(fqdn))
		{
			String[] parts=StringX.split(fqdn, ".");
			if ((null!=parts) && (parts.length>=3))
			{
				int len=parts.length;
				for (int i=1; i<len; i++)
				{
					if (result.length()>0)
					{
						result.append(String.format("%s.", result));
					}
					result.append(String.format("%s", parts[i]));
				}
			}
		}
		return (result.length()>0) ? result.toString() : null;
	}

	public static
	String formatMAC(String value)
	{
		String result=null;
		if (!StringX.isBlank(value))
		{
			result=StringX.removeNonAlphaNumericCharacters(StringX.clean(value.trim()));
			result=StringX.insertPeriodicallyFromStart(result, ":", 2).toLowerCase();
		}
		return result;
	}

	public static
	long ipToLong(InetAddress ip)
	{
		byte[] octets=ip.getAddress();
		long result=0;
		for (byte octet : octets)
		{
			result<<=8;
			result|=octet&0xff;
		}
		return result;
	}

	public static
	boolean isIPInRange(InetAddress test, InetAddress low, InetAddress high)
	{
		long ipLo=IPNetX.ipToLong(low);
		long ipHi=IPNetX.ipToLong(high);
		long ipToTest=IPNetX.ipToLong(test);

		return ((ipToTest>=ipLo) && (ipToTest<=ipHi));
	}

	public static
	boolean isValidPort(Integer port)
	{
		return (null!=port) && ((port>=IPNetX.MIN_PORT_RANGE) && (port<=IPNetX.MAX_PORT_RANGE));
	}

	public static boolean isAttribute(String value)
	{
		return !StringX.isBlank(value) && (IPNetX.isIPv4(value) || IPNetX.isValidMac(value) || IPNetX.isIPv6(value));
	}
}
