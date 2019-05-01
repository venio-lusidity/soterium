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

package com.lusidity.framework.math;

import java.text.DecimalFormat;

public class IntegerX {
    public static int parseInt(String str) {
        int result = 0;
        try
        {
            result = Integer.valueOf(str);
        }
        catch (Exception ignored){}
        return result;
    }

    public static Integer checkedCast(long l) {
        Integer result = null;
        if((l>=Integer.MIN_VALUE) && (l<=Integer.MAX_VALUE)){
            result = (int)l;
        }
        return result;
    }

    public static String insertCommas(float num, boolean decimals){
        DecimalFormat formatter = new DecimalFormat(decimals ? "#,###.00" : "#,###");
        return formatter.format(num);
    }
}
