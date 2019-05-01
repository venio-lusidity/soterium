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

package com.lusidity.domains.acs.security;


import com.lusidity.data.DataVertex;
import com.lusidity.data.field.KeyData;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;

@AtSchemaClass(name = "Identity Verification", description = "Identity registration forms compliance data.", discoverable = false)
public class IdentityVerification extends BaseDomain
{

	private KeyData<IdentityVerification.Status> status = null;
	private KeyData<String> relatedId = null;
	private KeyData<String> approver = null;
	private KeyData<String> approverId = null;
	private KeyData<Boolean> d2875 = null;
	private KeyData<Boolean> d787 = null;

	public IdentityVerification() {
		super();
	}

	public IdentityVerification(JsonData dso, Object indexId) {
		super(dso, indexId);
	}

	public KeyData<String> fetchRelatedId() {
		if (null == this.relatedId) {
			this.relatedId = new KeyData<>(this, "relatedId", String.class, false, null);
		}
		return this.relatedId;
	}

	public KeyData<String> fetchApprover() {
		if (null == this.approver) {
			this.approver = new KeyData<>(this, "approver", String.class, false, null);
		}
		return this.approver;
	}

	public KeyData<String> fetchApproverId() {
		if (null == this.approverId) {
			this.approverId = new KeyData<>(this, "approverId", String.class, false, null);
		}
		return this.approverId;
	}

	public KeyData<Boolean> fetchD2875() {
		if (null == this.d2875) {
			this.d2875 = new KeyData<>(this, "d2875", Boolean.class, false, false);
		}
		return this.d2875;
	}

	public KeyData<Boolean> fetchD787() {
		if (null == this.d787) {
			this.d787 = new KeyData<>(this, "d787", Boolean.class, false, false);
		}
		return this.d787;
	}

	public KeyData<IdentityVerification.Status> fetchStatus() {
		if (null == this.status) {
			this.status = new KeyData<>(this, "status", IdentityVerification.Status.class, false, Status.waiting);
		}
		return this.status;
	}

	public enum Status {
		completed,
		waiting,
		invalidated
	}

	public static synchronized IdentityVerification getOrCreate(String uri, ProcessStatus status) {
		IdentityVerification result = null;
		Class<? extends DataVertex> cls = IdentityVerification.class;
		result = VertexFactory.getInstance().getByPropertyIgnoreCase(cls, "relatedId", uri);
		if (null != status) {
			status.fetchQueries().getValue().increment();
		}
		if(null ==result){
			result = new IdentityVerification();
		}
		else if(null != status) {
			status.fetchMatches().getValue().increment();
		}
		return result;
	}

}

