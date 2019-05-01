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

package com.lusidity.cache.acs;

import com.lusidity.data.DataVertex;
import org.joda.time.DateTime;

import java.util.Objects;

public class AcsCachedItem
{

	private final boolean read;
	private final boolean write;
	private final boolean delete;
	private final boolean denied;
	private final boolean inScope;
	private final boolean authorized;
	private DateTime expiresDateTime = null;
	private long size = 0;
	private DateTime created = DateTime.now();
	private DateTime lastAccessed= DateTime.now();
	private String key = null;

	// Constructors
	public AcsCachedItem(DataVertex context, boolean authorized, boolean inScope, boolean read, boolean write, boolean delete, boolean denied, DateTime expiresDateTime)
	{
		super();
		this.authorized = authorized;
		this.read = read;
		this.write = write;
		this.delete = delete;
		this.denied=denied;
		this.inScope = inScope;
		this.key = context.fetchId().getValue();
		this.expiresDateTime = expiresDateTime;
	}

	// Overrides
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj instanceof AcsCachedItem){
			AcsCachedItem that = (AcsCachedItem) obj;
			result =Objects.equals(this.getKey(), that.getKey());
		}
		return result;
	}

	public String getKey()
	{
		return this.key;
	}

	public void touch()
	{
		this.lastAccessed=DateTime.now();
	}

	public boolean isExpired(DateTime expiresAt)
	{
		// if this.expiresDateTime is null don't expire the cached item
		return (null!=this.expiresDateTime) && this.expiresDateTime.isBefore(expiresAt);
	}

	// Getters and setters
	public boolean isExpired()
	{
		return this.expiresDateTime.isBefore(DateTime.now());
	}

	public DateTime getCreated() {
		return this.created;
	}

	public DateTime getLastAccessed(){
		return this.lastAccessed;
	}

	public long getSizeInBytes(){
		return this.size;
	}

	public boolean isRead()
	{
		return this.read;
	}

	public boolean isWrite()
	{
		return this.write;
	}

	public boolean isDelete()
	{
		return this.delete;
	}

	public boolean isDenied()
	{
		return this.denied;
	}

	public DateTime getExpiresDateTime()
	{
		return this.expiresDateTime;
	}

	public long getSize()
	{
		return this.size;
	}

	public boolean isInScope()
	{
		return this.inScope;
	}

	public boolean isAuthorized()
	{
		return this.authorized;
	}
}
