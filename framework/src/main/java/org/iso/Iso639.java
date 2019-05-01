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

package org.iso;

import com.lusidity.framework.json.JsonEngine;
import org.json.JSONObject;

public class Iso639
{

    private String languageFamily = null;
    private String languageName = null;
    private String nativeName = null;
    private String code639_1 = null;
    private String code639_2T = null;
    private String code639_2B = null;
    private String code639_3 = null;
    private String code639_6 = null;

    public
    Iso639(JSONObject jsonObject)
    {
        super();

        this.languageFamily = JsonEngine.getString(jsonObject, Language.Keys.languageFamily.toString());
        this.languageName = JsonEngine.getString(jsonObject, Language.Keys.languageName.toString());
        this.nativeName = JsonEngine.getString(jsonObject, Language.Keys.nativeName.toString());
        this.code639_1 = JsonEngine.getString(jsonObject, Language.Keys.code639_1.toString());
        this.code639_2B = JsonEngine.getString(jsonObject, Language.Keys.code639_2B.toString());
        this.code639_2T = JsonEngine.getString(jsonObject, Language.Keys.code639_2T.toString());
        this.code639_3 = JsonEngine.getString(jsonObject, Language.Keys.code639_3.toString());
        this.code639_6 = JsonEngine.getString(jsonObject, Language.Keys.code639_6.toString());
    }

    public String getLanguageFamily() {
        return this.languageFamily;
    }

    public String getLanguageName() {
        return this.languageName;
    }

    public String getNativeName() {
        return this.nativeName;
    }

    public String getCode639_1() {
        return this.code639_1;
    }

    public String getCode639_2T() {
        return this.code639_2T;
    }

    public void setCode639_2T(String code639_2T) {
        this.code639_2T = code639_2T;
    }

    public String getCode639_2B() {
        return this.code639_2B;
    }

    public void setCode639_2B(String code639_2B) {
        this.code639_2B = code639_2B;
    }

    public String getCode639_3() {
        return this.code639_3;
    }

    public String getCode639_6() {
        return this.code639_6;
    }
}
