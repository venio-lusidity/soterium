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

package com.lusidity.framework.system.logging;

import com.lusidity.framework.text.StringX;
import org.joda.time.DateTime;
import org.json.JSONObject;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class JSONLogFormatter extends Formatter {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String MESSAGE_FORMAT = "%s: %s";

    @Override
    public String format(LogRecord record) {
        JSONObject result = new JSONObject();
        try
        {
            result.put("level", record.getLevel().toString().toLowerCase());
            if(this.isCritical(record)){
                result.put("critical", true);
            }
            result.put("time", new DateTime(record.getMillis()));
            result.put("message", this.formatMessage(record));
        }
        catch (Exception ignored){}

        return result.toString() + '\n';
    }
    private boolean isCritical(LogRecord record) {
        boolean result = false;
        if(!StringX.isBlank(this.formatMessage(record))){
            if(StringX.startsWithIgnoreCase(this.formatMessage(record), "critical")){
                result = true;
            }
        }
        return result;
    }
}
