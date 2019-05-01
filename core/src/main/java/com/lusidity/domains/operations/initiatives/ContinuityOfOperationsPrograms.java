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

package com.lusidity.domains.operations.initiatives;

import com.lusidity.data.field.KeyData;
import com.lusidity.domains.BaseDomain;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;
import org.joda.time.DateTime;

@AtSchemaClass(name="Continuity of Operations Programs", discoverable=false,
	description="Continuity of Operations (COOP) is the initiative that ensures that Federal Government departments and agencies are able to continue operation of their essential functions under a " +
	            "broad range of circumstances including all-hazard emergencies as well as natural, man-made, and technological threats and national security emergencies.")
public class ContinuityOfOperationsPrograms extends BaseDomain
{
	private KeyData<DateTime> tested=null;
	private KeyData<DateTime> securityControlsTested=null;
	private KeyData<DateTime> annualSecurityReview=null;

// Constructors
	public ContinuityOfOperationsPrograms()
	{
		super();
	}

	@SuppressWarnings("unused")
	public ContinuityOfOperationsPrograms(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	@SuppressWarnings("unused")
	public KeyData<DateTime> fetchSecurityControlsTested()
	{
		if (null==this.securityControlsTested)
		{
			this.securityControlsTested=new KeyData<>(this, "securityControlsTested", DateTime.class, false, null);
		}
		return this.securityControlsTested;
	}

	@SuppressWarnings("unused")
	public KeyData<DateTime> fetchAnnualSecurityReview()
	{
		if (null==this.annualSecurityReview)
		{
			this.annualSecurityReview=new KeyData<>(this, "annualSecurityReview", DateTime.class, false, null);
		}
		return this.annualSecurityReview;
	}

	public KeyData<DateTime> fetchTested()
	{
		if (null==this.tested)
		{
			this.tested=new KeyData<>(this, "tested", DateTime.class, false, null);
		}
		return this.tested;
	}

}
