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

package com.lusidity.discover.providers.wikipedia;

import com.lusidity.Environment;
import com.lusidity.discover.DiscoveryItem;
import com.lusidity.discover.providers.ThirdPartyProviders;
import com.lusidity.domains.acs.security.SystemCredentials;
import com.lusidity.domains.internet.Wiki;
import com.lusidity.framework.internet.http.HttpClientX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

public class WikipediaProvider implements ThirdPartyProviders {

    private static final String URL_FORMAT =
        "https://en.wikipedia.org/w/apiKey.php?action=query&titles=%s&prop=revisions&rvprop=content&redirects&format=json";

    @Override
    public Collection<DiscoveryItem> discover(String phrase, int start, int limit) {
        Collection<DiscoveryItem> results = new ArrayList<>();
        try {
            String urlFormatted = String.format(WikipediaProvider.URL_FORMAT, StringX.urlEncode(phrase.toLowerCase()));
            URI uri= URI.create(urlFormatted);
            String requested = HttpClientX.getString(uri);
            if (!StringUtils.isBlank(requested)) {
                JsonData response = new JsonData(requested);
                JsonData pages = response.getFromPath("query::pages");
                if(null!=pages){
                    for(String key: pages.keys()){
                        WikiItem page = this.parse(pages, key);
                        if(null!=page){
                            results.add(page);
                        }
                    }
                }
            }
        }
        catch (Exception ex){
            Environment.getInstance().getReportHandler().severe(ex);
        }
        return results;
    }

    private WikiItem parse(JsonData pages, String key) {
        WikiItem result = null;
        JsonData page = pages.getFromPath(key);
        String content = page.getString("revisions::*");
        if(!StringX.isBlank(content)){
            WikipediaParser parser = new WikipediaParser(content);
            WikipediaInfoBox infoBox = parser.getInfoBox();
            if(null!=infoBox){
                result = new WikiItem(SystemCredentials.getInstance(), null, null);
                result.build(infoBox.getTitle(), infoBox.getDescription(), null, infoBox.getHomepage(), 1.0, Wiki.class, 1);
            }
        }
        return result;
    }
}
