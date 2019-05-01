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

package com.lusidity.framework.security;

import com.lusidity.framework.text.StringX;

public class ObfuscateX {
    private static final String base = "69ceaet7-efbf-4d0d-8t8b-4ba58t05bb24";

    public static String obfuscate(String str, String seed, String base) throws Exception {
        TripleDES tripleDES = new TripleDES(ObfuscateX.getSeed(seed, StringX.isBlank(base) ? ObfuscateX.base : base));
        return tripleDES.encrypt(str);
    }

    private static String getSeed(String seed, String base) {
        String result = seed;
        if(!StringX.isBlank(seed)){
            if(seed.length()<32){
                int len = 32-seed.length();
                String temp = StringX.substring(base, 0, len);
                result = String.format("%s%s", temp, seed);
            }
        }
        return result;
    }

    public static String decrypt(String str, String seed, String base) throws Exception {
        TripleDES tripleDES = new TripleDES(ObfuscateX.getSeed(seed, StringX.isBlank(base) ? ObfuscateX.base : base));
        return tripleDES.decrypt(str);
    }
}
