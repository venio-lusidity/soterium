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
import com.lusidity.framework.text.StringX;
import org.json.JSONArray;
import org.json.JSONObject;

public
class Language
{

    public enum Keys
    {
        languageFamily,
        languageName,
        nativeName,
        code639_1,
        code639_2T,
        code639_2B,
        code639_3,
        code639_6
    }

    @SuppressWarnings("UtilityClassWithoutPrivateConstructor")
    private static
    class ObjectHolder
    {
        private static final Object SEMAPHORE = new Object();
    }
    
    private static JSONArray ISO639Table = null;
    
    public static JSONArray getISO639Table()
    {
        synchronized (Language.getSemaphore())
        {
            if(null == Language.ISO639Table)
            {

                Language.ISO639Table = Language.load();
            }
        }
        
        return Language.ISO639Table;
    }
    
    private static
    Object getSemaphore()
    {
        return ObjectHolder.SEMAPHORE;
    }
    
    public static
    Iso639 get(Keys key, String value)
    {
        Iso639 result = null;

        if((null != key) && !StringX.isBlank(value))
        {
            int len = Language.getISO639Table().length();
            for(int i=0;i<len;i++)
            {
                JSONObject jsonObject = JsonEngine.getJsonObject(Language.ISO639Table, i);
                if(null!=jsonObject)
                {
                    String test = JsonEngine.getString(jsonObject, key.toString());
                    if(StringX.equalsIgnoreCase(value, test))
                    {
                        result = new Iso639(jsonObject);
                        break;
                    }
                }
            }
        }
        
        return result;
    }
    
    public static Keys getKeyFor(String value)
    {
        Keys result = null;

        if(!StringX.isBlank(value))
        {
            Keys[] keys = Keys.values();

            for(Keys key: keys)
            {
                if(Language.isValid(key, value))
                {
                    result = key;
                    break;
                }
            }
        }
        
        return result;
    }
    
    public static Boolean isValid(Keys key, String value)
    {
        return (null!=Language.get(key, value));
    }
    
    public static boolean isValid(String value)
    {
        Keys key = Language.getKeyFor(value);
        return Language.isValid(key, value);
    }
    
    public static JSONArray load()
    {
        JSONArray results = new JSONArray();

        try
        {
            results.put(new JSONObject().put("languageFamily", "Afro-Asiatic").put("languageName", "Afar").put("nativeName", "Afaraf").put("code639_1", "aa").put("code639_2T", "aar").put("code639_2B", "aar").put("code639_3", "aar").put("code639_6", "aars"));
            results.put(new JSONObject().put("languageFamily", "Afro-Asiatic").put("languageName", "Amharic").put("nativeName", "አማርኛ").put("code639_1", "am").put("code639_2T", "amh").put("code639_2B", "amh").put("code639_3", "amh").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Afro-Asiatic").put("languageName", "Arabic").put("nativeName", "العربية").put("code639_1", "ar").put("code639_2T", "ara").put("code639_2B", "ara").put("code639_3", "ara + 30").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Afro-Asiatic").put("languageName", "Hausa").put("nativeName", "Hausa, هَوُسَ").put("code639_1", "ha").put("code639_2T", "hau").put("code639_2B", "hau").put("code639_3", "hau").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Afro-Asiatic").put("languageName", "Hebrew (modern)").put("nativeName", "עברית").put("code639_1", "he").put("code639_2T", "heb").put("code639_2B", "heb").put("code639_3", "heb").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Afro-Asiatic").put("languageName", "Maltese").put("nativeName", "Malti").put("code639_1", "mt").put("code639_2T", "mlt").put("code639_2B", "mlt").put("code639_3", "mlt").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Afro-Asiatic").put("languageName", "Oromo").put("nativeName", "Afaan Oromoo").put("code639_1", "om").put("code639_2T", "orm").put("code639_2B", "orm").put("code639_3", "orm + 4").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Afro-Asiatic").put("languageName", "Somali").put("nativeName", "Soomaaliga, af Soomaali").put("code639_1", "so").put("code639_2T", "som").put("code639_2B", "som").put("code639_3", "som").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Afro-Asiatic").put("languageName", "Tigrinya").put("nativeName", "ትግርኛ").put("code639_1", "ti").put("code639_2T", "tir").put("code639_2B", "tir").put("code639_3", "tir").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Algonquian").put("languageName", "Cree").put("nativeName", "ᓀᐦᐃᔭᐍᐏᐣ").put("code639_1", "cr").put("code639_2T", "cre").put("code639_2B", "cre").put("code639_3", "cre + 6").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Algonquian").put("languageName", "Ojibwe, Ojibwa").put("nativeName", "ᐊᓂᔑᓈᐯᒧᐎᓐ").put("code639_1", "oj").put("code639_2T", "oji").put("code639_2B", "oji").put("code639_3", "oji + 7").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Austroasiatic").put("languageName", "Khmer").put("nativeName", "ខ្មែរ, ខេមរភាសា, ភាសាខ្មែរ").put("code639_1", "km").put("code639_2T", "khm").put("code639_2B", "khm").put("code639_3", "khm").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Austroasiatic").put("languageName", "Vietnamese").put("nativeName", "Tiếng Việt").put("code639_1", "vi").put("code639_2T", "vie").put("code639_2B", "vie").put("code639_3", "vie").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Austronesian").put("languageName", "Chamorro").put("nativeName", "Chamoru").put("code639_1", "ch").put("code639_2T", "cha").put("code639_2B", "cha").put("code639_3", "cha").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Austronesian").put("languageName", "Fijian").put("nativeName", "vosa Vakaviti").put("code639_1", "fj").put("code639_2T", "fij").put("code639_2B", "fij").put("code639_3", "fij").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Austronesian").put("languageName", "Hiri Motu").put("nativeName", "Hiri Motu").put("code639_1", "ho").put("code639_2T", "hmo").put("code639_2B", "hmo").put("code639_3", "hmo").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Austronesian").put("languageName", "Indonesian").put("nativeName", "Bahasa Indonesia").put("code639_1", "id").put("code639_2T", "ind").put("code639_2B", "ind").put("code639_3", "ind").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Austronesian").put("languageName", "Javanese").put("nativeName", "basa Jawa").put("code639_1", "jv").put("code639_2T", "jav").put("code639_2B", "jav").put("code639_3", "jav").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Austronesian").put("languageName", "Malagasy").put("nativeName", "fiteny malagasy").put("code639_1", "mg").put("code639_2T", "mlg").put("code639_2B", "mlg").put("code639_3", "mlg + 10").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Austronesian").put("languageName", "Malay").put("nativeName", "bahasa Melayu, بهاس ملايو‎").put("code639_1", "ms").put("code639_2T", "msa").put("code639_2B", "may").put("code639_3", "msa + 13").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Austronesian").put("languageName", "Māori").put("nativeName", "te reo Māori").put("code639_1", "mi").put("code639_2T", "mri").put("code639_2B", "mao").put("code639_3", "mri").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Austronesian").put("languageName", "Marshallese").put("nativeName", "Kajin M̧ajeļ").put("code639_1", "mh").put("code639_2T", "mah").put("code639_2B", "mah").put("code639_3", "mah").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Austronesian").put("languageName", "Nauru").put("nativeName", "Ekakairũ Naoero").put("code639_1", "na").put("code639_2T", "nau").put("code639_2B", "nau").put("code639_3", "nau").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Austronesian").put("languageName", "Samoan").put("nativeName", "gagana fa'a Samoa").put("code639_1", "sm").put("code639_2T", "smo").put("code639_2B", "smo").put("code639_3", "smo").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Austronesian").put("languageName", "Sundanese").put("nativeName", "Basa Sunda").put("code639_1", "su").put("code639_2T", "sun").put("code639_2B", "sun").put("code639_3", "sun").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Austronesian").put("languageName", "Tagalog").put("nativeName", "Wikang Tagalog, ᜏᜒᜃᜅ᜔ ᜆᜄᜎᜓᜄ᜔").put("code639_1", "tl").put("code639_2T", "tgl").put("code639_2B", "tgl").put("code639_3", "tgl").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Austronesian").put("languageName", "Tonga (Tonga Islands)").put("nativeName", "faka Tonga").put("code639_1", "to").put("code639_2T", "ton").put("code639_2B", "ton").put("code639_3", "ton").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Austronesian").put("languageName", "Tahitian").put("nativeName", "Reo Tahiti").put("code639_1", "ty").put("code639_2T", "tah").put("code639_2B", "tah").put("code639_3", "tah").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Aymaran").put("languageName", "Aymara").put("nativeName", "aymar aru").put("code639_1", "ay").put("code639_2T", "aym").put("code639_2B", "aym").put("code639_3", "aym + 2").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Constructed").put("languageName", "Esperanto").put("nativeName", "Esperanto").put("code639_1", "eo").put("code639_2T", "epo").put("code639_2B", "epo").put("code639_3", "epo").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Constructed").put("languageName", "Interlingua").put("nativeName", "Interlingua").put("code639_1", "ia").put("code639_2T", "ina").put("code639_2B", "ina").put("code639_3", "ina").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Constructed").put("languageName", "Interlingue").put("nativeName", "Originally called Occidental; then Interlingue after WWII").put("code639_1", "ie").put("code639_2T", "ile").put("code639_2B", "ile").put("code639_3", "ile").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Constructed").put("languageName", "Ido").put("nativeName", "Ido").put("code639_1", "io").put("code639_2T", "ido").put("code639_2B", "ido").put("code639_3", "ido").put("code639_6", "idos"));
            results.put(new JSONObject().put("languageFamily", "Constructed").put("languageName", "Volapük").put("nativeName", "Volapük").put("code639_1", "vo").put("code639_2T", "vol").put("code639_2B", "vol").put("code639_3", "vol").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Creole").put("languageName", "Bislama").put("nativeName", "Bislama").put("code639_1", "bi").put("code639_2T", "bis").put("code639_2B", "bis").put("code639_3", "bis").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Creole").put("languageName", "Haitian; Haitian Creole").put("nativeName", "Kreyòl ayisyen").put("code639_1", "ht").put("code639_2T", "hat").put("code639_2B", "hat").put("code639_3", "hat").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Creole").put("languageName", "Sango").put("nativeName", "yângâ tî sängö").put("code639_1", "sg").put("code639_2T", "sag").put("code639_2B", "sag").put("code639_3", "sag").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Dené–Yeniseian").put("languageName", "Navajo, Navaho").put("nativeName", "Diné bizaad, Dinékʼehǰí").put("code639_1", "nv").put("code639_2T", "nav").put("code639_2B", "nav").put("code639_3", "nav").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Dravidian").put("languageName", "Kannada").put("nativeName", "ಕನ್ನಡ").put("code639_1", "kn").put("code639_2T", "kan").put("code639_2B", "kan").put("code639_3", "kan").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Dravidian").put("languageName", "Malayalam").put("nativeName", "മലയാളം").put("code639_1", "ml").put("code639_2T", "mal").put("code639_2B", "mal").put("code639_3", "mal").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Dravidian").put("languageName", "Tamil").put("nativeName", "தமிழ்").put("code639_1", "ta").put("code639_2T", "tam").put("code639_2B", "tam").put("code639_3", "tam").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Dravidian").put("languageName", "Telugu").put("nativeName", "తెలుగు").put("code639_1", "te").put("code639_2T", "tel").put("code639_2B", "tel").put("code639_3", "tel").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Eskimo–Aleut").put("languageName", "Inupiaq").put("nativeName", "Iñupiaq, Iñupiatun").put("code639_1", "ik").put("code639_2T", "ipk").put("code639_2B", "ipk").put("code639_3", "ipk + 2").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Eskimo–Aleut").put("languageName", "Inuktitut").put("nativeName", "ᐃᓄᒃᑎᑐᑦ").put("code639_1", "iu").put("code639_2T", "iku").put("code639_2B", "iku").put("code639_3", "iku + 2").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Eskimo–Aleut").put("languageName", "Kalaallisut, Greenlandic").put("nativeName", "kalaallisut, kalaallit oqaasii").put("code639_1", "kl").put("code639_2T", "kal").put("code639_2B", "kal").put("code639_3", "kal").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Afrikaans").put("nativeName", "Afrikaans").put("code639_1", "af").put("code639_2T", "afr").put("code639_2B", "afr").put("code639_3", "afr").put("code639_6", "afrs"));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Albanian").put("nativeName", "gjuha shqipe").put("code639_1", "sq").put("code639_2T", "sqi").put("code639_2B", "alb").put("code639_3", "sqi + 4").put("code639_6", "–"));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Aragonese").put("nativeName", "aragonés").put("code639_1", "an").put("code639_2T", "arg").put("code639_2B", "arg").put("code639_3", "arg").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Armenian").put("nativeName", "Հայերեն").put("code639_1", "hy").put("code639_2T", "hye").put("code639_2B", "arm").put("code639_3", "hye").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Assamese").put("nativeName", "অসমীয়া").put("code639_1", "as").put("code639_2T", "asm").put("code639_2B", "asm").put("code639_3", "asm").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Avestan").put("nativeName", "avesta").put("code639_1", "ae").put("code639_2T", "ave").put("code639_2B", "ave").put("code639_3", "ave").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Belarusian").put("nativeName", "беларуская мова").put("code639_1", "be").put("code639_2T", "bel").put("code639_2B", "bel").put("code639_3", "bel").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Bengali; Bangla").put("nativeName", "বাংলা").put("code639_1", "bn").put("code639_2T", "ben").put("code639_2B", "ben").put("code639_3", "ben").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Bihari").put("nativeName", "भोजपुरी").put("code639_1", "bh").put("code639_2T", "bih").put("code639_2B", "bih").put("code639_3", "–").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Bosnian").put("nativeName", "bosanski jezik").put("code639_1", "bs").put("code639_2T", "bos").put("code639_2B", "bos").put("code639_3", "bos").put("code639_6", "boss"));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Breton").put("nativeName", "brezhoneg").put("code639_1", "br").put("code639_2T", "bre").put("code639_2B", "bre").put("code639_3", "bre").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Bulgarian").put("nativeName", "български език").put("code639_1", "bg").put("code639_2T", "bul").put("code639_2B", "bul").put("code639_3", "bul").put("code639_6", "buls"));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Catalan; Valencian").put("nativeName", "català, valencià").put("code639_1", "ca").put("code639_2T", "cat").put("code639_2B", "cat").put("code639_3", "cat").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Cornish").put("nativeName", "Kernewek").put("code639_1", "kw").put("code639_2T", "cor").put("code639_2B", "cor").put("code639_3", "cor").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Corsican").put("nativeName", "corsu, lingua corsa").put("code639_1", "co").put("code639_2T", "cos").put("code639_2B", "cos").put("code639_3", "cos").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Croatian").put("nativeName", "hrvatski jezik").put("code639_1", "hr").put("code639_2T", "hrv").put("code639_2B", "hrv").put("code639_3", "hrv").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Czech").put("nativeName", "čeština, český jazyk").put("code639_1", "cs").put("code639_2T", "ces").put("code639_2B", "cze").put("code639_3", "ces").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Danish").put("nativeName", "dansk").put("code639_1", "da").put("code639_2T", "dan").put("code639_2B", "dan").put("code639_3", "dan").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Divehi; Dhivehi; Maldivian;").put("nativeName", "ދިވެހި").put("code639_1", "dv").put("code639_2T", "div").put("code639_2B", "div").put("code639_3", "div").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Dutch").put("nativeName", "Nederlands, Vlaams").put("code639_1", "nl").put("code639_2T", "nld").put("code639_2B", "dut").put("code639_3", "nld").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "English").put("nativeName", "English").put("code639_1", "en").put("code639_2T", "eng").put("code639_2B", "eng").put("code639_3", "eng").put("code639_6", "engs"));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Faroese").put("nativeName", "føroyskt").put("code639_1", "fo").put("code639_2T", "fao").put("code639_2B", "fao").put("code639_3", "fao").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "French").put("nativeName", "français, langue française").put("code639_1", "fr").put("code639_2T", "fra").put("code639_2B", "fre").put("code639_3", "fra").put("code639_6", "fras"));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Galician").put("nativeName", "galego").put("code639_1", "gl").put("code639_2T", "glg").put("code639_2B", "glg").put("code639_3", "glg").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "German").put("nativeName", "Deutsch").put("code639_1", "de").put("code639_2T", "deu").put("code639_2B", "ger").put("code639_3", "deu").put("code639_6", "deus"));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Greek, Modern").put("nativeName", "ελληνικά").put("code639_1", "el").put("code639_2T", "ell").put("code639_2B", "gre").put("code639_3", "ell").put("code639_6", "ells"));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Gujarati").put("nativeName", "ગુજરાતી").put("code639_1", "gu").put("code639_2T", "guj").put("code639_2B", "guj").put("code639_3", "guj").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Hindi").put("nativeName", "हिन्दी, हिंदी").put("code639_1", "hi").put("code639_2T", "hin").put("code639_2B", "hin").put("code639_3", "hin").put("code639_6", "hins"));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Irish").put("nativeName", "Gaeilge").put("code639_1", "ga").put("code639_2T", "gle").put("code639_2B", "gle").put("code639_3", "gle").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Icelandic").put("nativeName", "Íslenska").put("code639_1", "is").put("code639_2T", "isl").put("code639_2B", "ice").put("code639_3", "isl").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Italian").put("nativeName", "italiano").put("code639_1", "it").put("code639_2T", "ita").put("code639_2B", "ita").put("code639_3", "ita").put("code639_6", "itas"));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Kashmiri").put("nativeName", "कश्मीरी, كشميري‎").put("code639_1", "ks").put("code639_2T", "kas").put("code639_2B", "kas").put("code639_3", "kas").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Kurdish").put("nativeName", "Kurdî, كوردی‎").put("code639_1", "ku").put("code639_2T", "kur").put("code639_2B", "kur").put("code639_3", "kur + 3").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Latin").put("nativeName", "latine, lingua latina").put("code639_1", "la").put("code639_2T", "lat").put("code639_2B", "lat").put("code639_3", "lat").put("code639_6", "lats"));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Luxembourgish, Letzeburgesch").put("nativeName", "Lëtzebuergesch").put("code639_1", "lb").put("code639_2T", "ltz").put("code639_2B", "ltz").put("code639_3", "ltz").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Limburgish, Limburgan, Limburger").put("nativeName", "Limburgs").put("code639_1", "li").put("code639_2T", "lim").put("code639_2B", "lim").put("code639_3", "lim").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Lithuanian").put("nativeName", "lietuvių kalba").put("code639_1", "lt").put("code639_2T", "lit").put("code639_2B", "lit").put("code639_3", "lit").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Latvian").put("nativeName", "latviešu valoda").put("code639_1", "lv").put("code639_2T", "lav").put("code639_2B", "lav").put("code639_3", "lav + 2").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Manx").put("nativeName", "Gaelg, Gailck").put("code639_1", "gv").put("code639_2T", "glv").put("code639_2B", "glv").put("code639_3", "glv").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Macedonian").put("nativeName", "македонски јазик").put("code639_1", "mk").put("code639_2T", "mkd").put("code639_2B", "mac").put("code639_3", "mkd").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Marathi (Marāṭhī)").put("nativeName", "मराठी").put("code639_1", "mr").put("code639_2T", "mar").put("code639_2B", "mar").put("code639_3", "mar").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Norwegian Bokmål").put("nativeName", "Norsk bokmål").put("code639_1", "nb").put("code639_2T", "nob").put("code639_2B", "nob").put("code639_3", "nob").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Nepali").put("nativeName", "नेपाली").put("code639_1", "ne").put("code639_2T", "nep").put("code639_2B", "nep").put("code639_3", "nep").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Norwegian Nynorsk").put("nativeName", "Norsk nynorsk").put("code639_1", "nn").put("code639_2T", "nno").put("code639_2B", "nno").put("code639_3", "nno").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Norwegian").put("nativeName", "Norsk").put("code639_1", "no").put("code639_2T", "nor").put("code639_2B", "nor").put("code639_3", "nor + 2").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Occitan").put("nativeName", "occitan, lenga d'òc").put("code639_1", "oc").put("code639_2T", "oci").put("code639_2B", "oci").put("code639_3", "oci").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Old Church Slavonic, Church Slavonic, Old Bulgarian").put("nativeName", "ѩзыкъ словѣньскъ").put("code639_1", "cu").put("code639_2T", "chu").put("code639_2B", "chu").put("code639_3", "chu").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Oriya").put("nativeName", "ଓଡ଼ିଆ").put("code639_1", "or").put("code639_2T", "ori").put("code639_2B", "ori").put("code639_3", "ori").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Ossetian, Ossetic").put("nativeName", "ирон æвзаг").put("code639_1", "os").put("code639_2T", "oss").put("code639_2B", "oss").put("code639_3", "oss").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Panjabi, Punjabi").put("nativeName", "ਪੰਜਾਬੀ, پنجابی‎").put("code639_1", "pa").put("code639_2T", "pan").put("code639_2B", "pan").put("code639_3", "pan").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Pāli").put("nativeName", "पाऴि").put("code639_1", "pi").put("code639_2T", "pli").put("code639_2B", "pli").put("code639_3", "pli").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Persian (Farsi)").put("nativeName", "فارسی").put("code639_1", "fa").put("code639_2T", "fas").put("code639_2B", "per").put("code639_3", "fas + 2").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Polish").put("nativeName", "język polski, polszczyzna").put("code639_1", "pl").put("code639_2T", "pol").put("code639_2B", "pol").put("code639_3", "pol").put("code639_6", "pols"));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Pashto, Pushto").put("nativeName", "پښتو").put("code639_1", "ps").put("code639_2T", "pus").put("code639_2B", "pus").put("code639_3", "pus + 3").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Portuguese").put("nativeName", "português").put("code639_1", "pt").put("code639_2T", "por").put("code639_2B", "por").put("code639_3", "por").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Romansh").put("nativeName", "rumantsch grischun").put("code639_1", "rm").put("code639_2T", "roh").put("code639_2B", "roh").put("code639_3", "roh").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Romanian").put("nativeName", "limba română").put("code639_1", "ro").put("code639_2T", "ron").put("code639_2B", "rum").put("code639_3", "ron").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Russian").put("nativeName", "русский язык").put("code639_1", "ru").put("code639_2T", "rus").put("code639_2B", "rus").put("code639_3", "rus").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Sanskrit (Saṁskṛta)").put("nativeName", "संस्कृतम्").put("code639_1", "sa").put("code639_2T", "san").put("code639_2B", "san").put("code639_3", "san").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Sardinian").put("nativeName", "sardu").put("code639_1", "sc").put("code639_2T", "srd").put("code639_2B", "srd").put("code639_3", "srd + 4").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Sindhi").put("nativeName", "सिन्धी, سنڌي، سندھی‎").put("code639_1", "sd").put("code639_2T", "snd").put("code639_2B", "snd").put("code639_3", "snd").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Serbian").put("nativeName", "српски језик").put("code639_1", "sr").put("code639_2T", "srp").put("code639_2B", "srp").put("code639_3", "srp").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Scottish Gaelic; Gaelic").put("nativeName", "Gàidhlig").put("code639_1", "gd").put("code639_2T", "gla").put("code639_2B", "gla").put("code639_3", "gla").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Sinhala, Sinhalese").put("nativeName", "සිංහල").put("code639_1", "si").put("code639_2T", "sin").put("code639_2B", "sin").put("code639_3", "sin").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Slovak").put("nativeName", "slovenčina, slovenský jazyk").put("code639_1", "sk").put("code639_2T", "slk").put("code639_2B", "slo").put("code639_3", "slk").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Slovene").put("nativeName", "slovenski jezik, slovenščina").put("code639_1", "sl").put("code639_2T", "slv").put("code639_2B", "slv").put("code639_3", "slv").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Spanish; Castilian").put("nativeName", "español, castellano").put("code639_1", "es").put("code639_2T", "spa").put("code639_2B", "spa").put("code639_3", "spa").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Swedish").put("nativeName", "Svenska").put("code639_1", "sv").put("code639_2T", "swe").put("code639_2B", "swe").put("code639_3", "swe").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Tajik").put("nativeName", "тоҷикӣ, toğikī, تاجیکی‎").put("code639_1", "tg").put("code639_2T", "tgk").put("code639_2B", "tgk").put("code639_3", "tgk").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Ukrainian").put("nativeName", "українська мова").put("code639_1", "uk").put("code639_2T", "ukr").put("code639_2B", "ukr").put("code639_3", "ukr").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Urdu").put("nativeName", "اردو").put("code639_1", "ur").put("code639_2T", "urd").put("code639_2B", "urd").put("code639_3", "urd").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Walloon").put("nativeName", "walon").put("code639_1", "wa").put("code639_2T", "wln").put("code639_2B", "wln").put("code639_3", "wln").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Welsh").put("nativeName", "Cymraeg").put("code639_1", "cy").put("code639_2T", "cym").put("code639_2B", "wel").put("code639_3", "cym").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Western Frisian").put("nativeName", "Frysk").put("code639_1", "fy").put("code639_2T", "fry").put("code639_2B", "fry").put("code639_3", "fry").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Indo-European").put("languageName", "Yiddish").put("nativeName", "ייִדיש").put("code639_1", "yi").put("code639_2T", "yid").put("code639_2B", "yid").put("code639_3", "yid + 2").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Japonic").put("languageName", "Japanese").put("nativeName", "日本語 (にほんご)").put("code639_1", "ja").put("code639_2T", "jpn").put("code639_2B", "jpn").put("code639_3", "jpn").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Language isolate").put("languageName", "Basque").put("nativeName", "euskara, euskera").put("code639_1", "eu").put("code639_2T", "eus").put("code639_2B", "baq").put("code639_3", "eus").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Language isolate").put("languageName", "Korean").put("nativeName", "한국어 (韓國語), 조선어 (朝鮮語)").put("code639_1", "ko").put("code639_2T", "kor").put("code639_2B", "kor").put("code639_3", "kor").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Mongolic").put("languageName", "Mongolian").put("nativeName", "монгол").put("code639_1", "mn").put("code639_2T", "mon").put("code639_2B", "mon").put("code639_3", "mon + 2").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Akan").put("nativeName", "Akan").put("code639_1", "ak").put("code639_2T", "aka").put("code639_2B", "aka").put("code639_3", "aka + 2").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Bambara").put("nativeName", "bamanankan").put("code639_1", "bm").put("code639_2T", "bam").put("code639_2B", "bam").put("code639_3", "bam").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Chichewa; Chewa; Nyanja").put("nativeName", "chiCheŵa, chinyanja").put("code639_1", "ny").put("code639_2T", "nya").put("code639_2B", "nya").put("code639_3", "nya").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Ewe").put("nativeName", "Eʋegbe").put("code639_1", "ee").put("code639_2T", "ewe").put("code639_2B", "ewe").put("code639_3", "ewe").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Fula; Fulah; Pulaar; Pular").put("nativeName", "Fulfulde, Pulaar, Pular").put("code639_1", "ff").put("code639_2T", "ful").put("code639_2B", "ful").put("code639_3", "ful + 9").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Herero").put("nativeName", "Otjiherero").put("code639_1", "hz").put("code639_2T", "her").put("code639_2B", "her").put("code639_3", "her").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Igbo").put("nativeName", "Asụsụ Igbo").put("code639_1", "ig").put("code639_2T", "ibo").put("code639_2B", "ibo").put("code639_3", "ibo").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Kikuyu, Gikuyu").put("nativeName", "Gĩkũyũ").put("code639_1", "ki").put("code639_2T", "kik").put("code639_2B", "kik").put("code639_3", "kik").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Kinyarwanda").put("nativeName", "Ikinyarwanda").put("code639_1", "rw").put("code639_2T", "kin").put("code639_2B", "kin").put("code639_3", "kin").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Kongo").put("nativeName", "KiKongo").put("code639_1", "kg").put("code639_2T", "kon").put("code639_2B", "kon").put("code639_3", "kon + 3").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Kwanyama, Kuanyama").put("nativeName", "Kuanyama").put("code639_1", "kj").put("code639_2T", "kua").put("code639_2B", "kua").put("code639_3", "kua").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Ganda").put("nativeName", "Luganda").put("code639_1", "lg").put("code639_2T", "lug").put("code639_2B", "lug").put("code639_3", "lug").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Lingala").put("nativeName", "Lingála").put("code639_1", "ln").put("code639_2T", "lin").put("code639_2B", "lin").put("code639_3", "lin").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Luba-Katanga").put("nativeName", "Tshiluba").put("code639_1", "lu").put("code639_2T", "lub").put("code639_2B", "lub").put("code639_3", "lub").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "North Ndebele").put("nativeName", "isiNdebele").put("code639_1", "nd").put("code639_2T", "nde").put("code639_2B", "nde").put("code639_3", "nde").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Ndonga").put("nativeName", "Owambo").put("code639_1", "ng").put("code639_2T", "ndo").put("code639_2B", "ndo").put("code639_3", "ndo").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "South Ndebele").put("nativeName", "isiNdebele").put("code639_1", "nr").put("code639_2T", "nbl").put("code639_2B", "nbl").put("code639_3", "nbl").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Kirundi").put("nativeName", "Ikirundi").put("code639_1", "rn").put("code639_2T", "run").put("code639_2B", "run").put("code639_3", "run").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Shona").put("nativeName", "chiShona").put("code639_1", "sn").put("code639_2T", "sna").put("code639_2B", "sna").put("code639_3", "sna").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Southern Sotho").put("nativeName", "Sesotho").put("code639_1", "st").put("code639_2T", "sot").put("code639_2B", "sot").put("code639_3", "sot").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Swahili").put("nativeName", "Kiswahili").put("code639_1", "sw").put("code639_2T", "swa").put("code639_2B", "swa").put("code639_3", "swa + 2").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Swati").put("nativeName", "SiSwati").put("code639_1", "ss").put("code639_2T", "ssw").put("code639_2B", "ssw").put("code639_3", "ssw").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Tswana").put("nativeName", "Setswana").put("code639_1", "tn").put("code639_2T", "tsn").put("code639_2B", "tsn").put("code639_3", "tsn").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Tsonga").put("nativeName", "Xitsonga").put("code639_1", "ts").put("code639_2T", "tso").put("code639_2B", "tso").put("code639_3", "tso").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Twi").put("nativeName", "Twi").put("code639_1", "tw").put("code639_2T", "twi").put("code639_2B", "twi").put("code639_3", "twi").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Venda").put("nativeName", "Tshivenḓa").put("code639_1", "ve").put("code639_2T", "ven").put("code639_2B", "ven").put("code639_3", "ven").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Wolof").put("nativeName", "Wollof").put("code639_1", "wo").put("code639_2T", "wol").put("code639_2B", "wol").put("code639_3", "wol").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Xhosa").put("nativeName", "isiXhosa").put("code639_1", "xh").put("code639_2T", "xho").put("code639_2B", "xho").put("code639_3", "xho").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Yoruba").put("nativeName", "Yorùbá").put("code639_1", "yo").put("code639_2T", "yor").put("code639_2B", "yor").put("code639_3", "yor").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Niger–Congo").put("languageName", "Zulu").put("nativeName", "isiZulu").put("code639_1", "zu").put("code639_2T", "zul").put("code639_2B", "zul").put("code639_3", "zul").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Nilo-Saharan").put("languageName", "Kanuri").put("nativeName", "Kanuri").put("code639_1", "kr").put("code639_2T", "kau").put("code639_2B", "kau").put("code639_3", "kau + 3").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Northeast Caucasian").put("languageName", "Avaric").put("nativeName", "авар мацӀ, магӀарул мацӀ").put("code639_1", "av").put("code639_2T", "ava").put("code639_2B", "ava").put("code639_3", "ava").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Northeast Caucasian").put("languageName", "Chechen").put("nativeName", "нохчийн мотт").put("code639_1", "ce").put("code639_2T", "che").put("code639_2B", "che").put("code639_3", "che").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Northwest Caucasian").put("languageName", "Abkhaz").put("nativeName", "аҧсуа бызшәа, аҧсшәа").put("code639_1", "ab").put("code639_2T", "abk").put("code639_2B", "abk").put("code639_3", "abk").put("code639_6", "abks"));
            results.put(new JSONObject().put("languageFamily", "Quechuan").put("languageName", "Quechua").put("nativeName", "Runa Simi, Kichwa").put("code639_1", "qu").put("code639_2T", "que").put("code639_2B", "que").put("code639_3", "que + 44").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Sino-Tibetan").put("languageName", "Burmese").put("nativeName", "ဗမာစာ").put("code639_1", "my").put("code639_2T", "mya").put("code639_2B", "bur").put("code639_3", "mya").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Sino-Tibetan").put("languageName", "Chinese").put("nativeName", "中文 (Zhōngwén), 汉语, 漢語").put("code639_1", "zh").put("code639_2T", "zho").put("code639_2B", "chi").put("code639_3", "zho + 13").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Sino-Tibetan").put("languageName", "Dzongkha").put("nativeName", "རྫོང་ཁ").put("code639_1", "dz").put("code639_2T", "dzo").put("code639_2B", "dzo").put("code639_3", "dzo").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Sino-Tibetan").put("languageName", "Nuosu").put("nativeName", "ꆈꌠ꒿ Nuosuhxop").put("code639_1", "ii").put("code639_2T", "iii").put("code639_2B", "iii").put("code639_3", "iii").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Sino-Tibetan").put("languageName", "Tibetan Standard, Tibetan, Central").put("nativeName", "བོད་ཡིག").put("code639_1", "bo").put("code639_2T", "bod").put("code639_2B", "tib").put("code639_3", "bod").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "South Caucasian").put("languageName", "Georgian").put("nativeName", "ქართული").put("code639_1", "ka").put("code639_2T", "kat").put("code639_2B", "geo").put("code639_3", "kat").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Tai–Kadai").put("languageName", "Lao").put("nativeName", "ພາສາລາວ").put("code639_1", "lo").put("code639_2T", "lao").put("code639_2B", "lao").put("code639_3", "lao").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Tai–Kadai").put("languageName", "Thai").put("nativeName", "ไทย").put("code639_1", "th").put("code639_2T", "tha").put("code639_2B", "tha").put("code639_3", "tha").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Tai–Kadai").put("languageName", "Zhuang, Chuang").put("nativeName", "Saɯ cueŋƅ, Saw cuengh").put("code639_1", "za").put("code639_2T", "zha").put("code639_2B", "zha").put("code639_3", "zha + 16").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Tupian").put("languageName", "Guaraní").put("nativeName", "Avañe'ẽ").put("code639_1", "gn").put("code639_2T", "grn").put("code639_2B", "grn").put("code639_3", "grn + 5").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Turkic").put("languageName", "Azerbaijani").put("nativeName", "azərbaycan dili").put("code639_1", "az").put("code639_2T", "aze").put("code639_2B", "aze").put("code639_3", "aze + 2").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Turkic").put("languageName", "Bashkir").put("nativeName", "башҡорт теле").put("code639_1", "ba").put("code639_2T", "bak").put("code639_2B", "bak").put("code639_3", "bak").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Turkic").put("languageName", "Chuvash").put("nativeName", "чӑваш чӗлхи").put("code639_1", "cv").put("code639_2T", "chv").put("code639_2B", "chv").put("code639_3", "chv").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Turkic").put("languageName", "Kazakh").put("nativeName", "қазақ тілі").put("code639_1", "kk").put("code639_2T", "kaz").put("code639_2B", "kaz").put("code639_3", "kaz").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Turkic").put("languageName", "Kyrgyz").put("nativeName", "Кыргызча, Кыргыз тили").put("code639_1", "ky").put("code639_2T", "kir").put("code639_2B", "kir").put("code639_3", "kir").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Turkic").put("languageName", "South Azerbaijani").put("nativeName", "تورکجه‎").put("code639_1", "az").put("code639_2T", "azb").put("code639_2B", "azb").put("code639_3", "azb").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Turkic").put("languageName", "Turkmen").put("nativeName", "Türkmen, Түркмен").put("code639_1", "tk").put("code639_2T", "tuk").put("code639_2B", "tuk").put("code639_3", "tuk").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Turkic").put("languageName", "Turkish").put("nativeName", "Türkçe").put("code639_1", "tr").put("code639_2T", "tur").put("code639_2B", "tur").put("code639_3", "tur").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Turkic").put("languageName", "Tatar").put("nativeName", "татар теле, tatar tele").put("code639_1", "tt").put("code639_2T", "tat").put("code639_2B", "tat").put("code639_3", "tat").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Turkic").put("languageName", "Uyghur, Uighur").put("nativeName", "Uyƣurqə, ئۇيغۇرچە‎").put("code639_1", "ug").put("code639_2T", "uig").put("code639_2B", "uig").put("code639_3", "uig").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Turkic").put("languageName", "Uzbek").put("nativeName", "O‘zbek, Ўзбек, أۇزبېك‎").put("code639_1", "uz").put("code639_2T", "uzb").put("code639_2B", "uzb").put("code639_3", "uzb + 2").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Uralic").put("languageName", "Estonian").put("nativeName", "eesti, eesti keel").put("code639_1", "et").put("code639_2T", "est").put("code639_2B", "est").put("code639_3", "est + 2").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Uralic").put("languageName", "Finnish").put("nativeName", "suomi, suomen kieli").put("code639_1", "fi").put("code639_2T", "fin").put("code639_2B", "fin").put("code639_3", "fin").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Uralic").put("languageName", "Hungarian").put("nativeName", "magyar").put("code639_1", "hu").put("code639_2T", "hun").put("code639_2B", "hun").put("code639_3", "hun").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Uralic").put("languageName", "Komi").put("nativeName", "коми кыв").put("code639_1", "kv").put("code639_2T", "kom").put("code639_2B", "kom").put("code639_3", "kom + 2").put("code639_6", ""));
            results.put(new JSONObject().put("languageFamily", "Uralic").put("languageName", "Northern Sami").put("nativeName", "Davvisámegiella").put("code639_1", "se").put("code639_2T", "sme").put("code639_2B", "sme").put("code639_3", "sme").put("code639_6", ""));

        }
        catch (Exception ex)
        {
            
        }

        return results;
    }
}
