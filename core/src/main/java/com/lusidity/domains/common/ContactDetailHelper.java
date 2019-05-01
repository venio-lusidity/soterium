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

package com.lusidity.domains.common;

import com.lusidity.Environment;
import com.lusidity.factories.VertexFactory;

@SuppressWarnings({
	"UtilityClassWithoutPrivateConstructor",
	"NonFinalUtilityClass"
})
public class ContactDetailHelper
{
// Methods
	public static BaseContactDetail make(BaseContactDetail.CategoryTypes categoryType, String value, String ext)
	{
		BaseContactDetail result=null;
		try
		{
			//noinspection SwitchStatementWithTooManyBranches
			switch (categoryType)
			{
				case home_email:
				case work_email:
					result=new Email(categoryType, value);
					break;
				case work_cell:
				case home_cell:
				case home_fax:
				case work_fax:
				case work_dsn:
				case home_mobile:
				case work_mobile:
				case home_phone:
				case work_phone:
					result=new PhoneNumber(categoryType, value, ext);
					break;
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return result;
	}

	public static Email getEmail(String emailAddress)
	{
		return VertexFactory.getInstance().getByPropertyIgnoreCase(Email.class, "value", emailAddress);
	}
}
