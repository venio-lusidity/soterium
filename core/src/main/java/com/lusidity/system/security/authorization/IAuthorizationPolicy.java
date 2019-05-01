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

package com.lusidity.system.security.authorization;

import com.lusidity.data.DataVertex;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.email.EmailMessage;
import com.lusidity.framework.exceptions.ApplicationException;

public interface IAuthorizationPolicy
{
	EmailMessage create(DataVertex context, BasePrincipal principal, ProcessStatus processStatus)
		throws ApplicationException;

	EmailMessage update(DataVertex context, BasePrincipal principal, ProcessStatus processStatus)
		throws ApplicationException;

	EmailMessage delete(DataVertex context, BasePrincipal principal, ProcessStatus processStatus)
		throws ApplicationException;
}
