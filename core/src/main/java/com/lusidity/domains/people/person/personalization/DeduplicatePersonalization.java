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

package com.lusidity.domains.people.person.personalization;

import com.lusidity.data.field.KeyData;
import com.lusidity.domains.people.person.Personalization;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;

import java.net.URI;

@AtSchemaClass(name="Deduplication Personalization", discoverable=false, writable=true)
public class DeduplicatePersonalization extends Personalization
{
	private KeyData<URI> artifactUri=null;
	private KeyData<String> matchId=null;
	private KeyData<String> response=null;
	// Constructors
	public DeduplicatePersonalization()
	{
		super();
	}

	public DeduplicatePersonalization(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	public KeyData<URI> fetchArtifactUri()
	{
		if(null==this.artifactUri){
			this.artifactUri = new KeyData<>(this, "artifactUri", URI.class,  false, null);
		}
		return this.artifactUri;
	}

	public KeyData<String> fetchMatchId()
	{
		if(null==this.matchId){
			this.matchId = new KeyData<>(this, "matchId", String.class, false, null);
		}
		return this.matchId;
	}

	public KeyData<String> fetchResponse()
	{
		if(null==this.response){
			this.response = new KeyData<>(this, "response", String.class, false, "enclave");
		}
		return this.response;
	}


	// Getters and setters


}

