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

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

public class HttpDeleteWithEntity extends HttpEntityEnclosingRequestBase{
    private final static String METHOD_NAME = "DELETE";

	// Constructors
	public HttpDeleteWithEntity(URI uri) {
		super();
		this.setURI(uri);
	}

	// Overrides
	@Override
	public String getMethod() {
		return HttpDeleteWithEntity.METHOD_NAME;
	}
}
