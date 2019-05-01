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

package com.lusidity.domains.internet;

import com.lusidity.blockers.ValueBlocker;
import com.lusidity.data.field.KeyDataCollection;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.system.primitives.UriValue;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;

import java.util.UUID;

@SuppressWarnings({
    "EqualsAndHashcode",
    "unused"
})
@AtSchemaClass(name = "Website", discoverable = true)
public class Website extends BaseDomain
{
    private static ValueBlocker VALUE_BLOCKER = new ValueBlocker();
	private KeyDataCollection<UriValue> pageURIs=null;

	// Constructors
	public Website(JsonData dso, Object indexId) {super(dso, indexId);}

    public Website(){
        super();
    }

	// Overrides
	@Override
    public boolean equals(Object o) {
        boolean result = false;
        if(o instanceof Website){
            Website that = (Website)o;
            if((null!=that.getPageURIs()) && (null!=this.getPageURIs()) && !that.getPageURIs().isEmpty() && !this.pageURIs.isEmpty()){
                for(UriValue thisUri: this.getPageURIs()){
                    for(UriValue thatUri: that.getPageURIs()){
                        result = thisUri.equals(thatUri);
                        if(result){
                            break;
                        }
                    }
                    if(result){
                        break;
                    }
                }
            }
        }
        return result;
    }

	public KeyDataCollection<UriValue> getPageURIs()
	{
		if (null==this.pageURIs)
		{
			this.pageURIs=new KeyDataCollection<>(this, "pageURIs", UriValue.class, false, false, false, null);
		}
		return this.pageURIs;
	}

	// Methods
	public static Website getOrCreate(UriValue uriValue) throws ApplicationException
	{
		Website result = VertexFactory.getInstance().getByIdentifier(Website.class, uriValue);
		if(null==result) {
			result = Website.make(uriValue);
		}
		return result;
	}

    private static Website make(UriValue uriValue) throws ApplicationException
    {
        Website result = null;
        UUID id = null;
        try
        {
            id = Website.VALUE_BLOCKER.block(uriValue.fetchValue().getValue());
            result = VertexFactory.getInstance().getByIdentifier(Website.class, uriValue);
            if(null==result) {
                result = new Website();
                result.fetchIdentifiers().add(uriValue);
                result.getPageURIs().add(uriValue);
                result.save();
            }
        }
        catch (Exception ignored){

        }
        finally {
            if(null!=id){
                Website.VALUE_BLOCKER.unblock(uriValue.fetchValue().getValue(), id);
            }
        }
        return result;
    }
}
