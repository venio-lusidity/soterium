/*
 * Copyright (c) 2008-2012, Venio, Inc.
 * All Rights Reserved Worldwide.
 *
 * This computer software is protected by copyright law and international treaties.
 * It may not be duplicated, reproduced, distributed, compiled, executed,
 * reverse-engineered, or used in any other way, in whole or in part, without the
 * express written consent of Venio, Inc.
 *
 * Portions of this computer software also embody trade secrets, patents, and other
 * protected intellectual property of Venio, Inc. and third parties and are subject to
 * applicable laws, regulations, treaties, agreements, and other legal mechanisms.
 */

package com.lusidity.services.security;

import com.lusidity.Environment;
import org.omg.CORBA.portable.ApplicationException;
import org.restlet.Request;
import org.restlet.Response;

public
interface Verifier
{
	/*
		*
		* Enumerations
		*
		*/
	enum StatusType
	{
		/** The credentials have been verified. */
		Verified,
		/** The Authorization header is missing or has no value. */
		Credentials_Missing,
		/** The Application Domain credentials do not match any value stored. */
		Application_Domain_Credentials_Missing_or_Invalid,
		/** The Authorization header does not contain an Identity. */
		Identity_Credentials_Missing,
		/** The Identity credentials do not match any value stored. */
		Identity_Credentials_Unknown,
		/** The credentials have not been verified. */
		Not_Verified
	}

	StatusType verify(Environment environment, Request request, Response response)
		throws ApplicationException;
}
