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

package com.lusidity.framework.system;

import com.lusidity.framework.text.StringX;

import java.util.ArrayList;
import java.util.Collection;

public class ReservedWords {

    static final Collection<String> RESERVED_WORDS = new ArrayList<>();
    static {
        ReservedWords.RESERVED_WORDS.add("abstract");
        ReservedWords.RESERVED_WORDS.add("continue");
        ReservedWords.RESERVED_WORDS.add("for");
        ReservedWords.RESERVED_WORDS.add("new");
        ReservedWords.RESERVED_WORDS.add("switch");
        ReservedWords.RESERVED_WORDS.add("assert");
        ReservedWords.RESERVED_WORDS.add("default");
        ReservedWords.RESERVED_WORDS.add("goto");
        ReservedWords.RESERVED_WORDS.add("package");
        ReservedWords.RESERVED_WORDS.add("synchronized");
        ReservedWords.RESERVED_WORDS.add("boolean");
        ReservedWords.RESERVED_WORDS.add("do");
        ReservedWords.RESERVED_WORDS.add("if");
        ReservedWords.RESERVED_WORDS.add("private");
        ReservedWords.RESERVED_WORDS.add("this");
        ReservedWords.RESERVED_WORDS.add("break");
        ReservedWords.RESERVED_WORDS.add("double");
        ReservedWords.RESERVED_WORDS.add("implements");
        ReservedWords.RESERVED_WORDS.add("protected");
        ReservedWords.RESERVED_WORDS.add("throw");
        ReservedWords.RESERVED_WORDS.add("byte");
        ReservedWords.RESERVED_WORDS.add("else");
        ReservedWords.RESERVED_WORDS.add("import");
        ReservedWords.RESERVED_WORDS.add("public");
        ReservedWords.RESERVED_WORDS.add("throws");
        ReservedWords.RESERVED_WORDS.add("case");
        ReservedWords.RESERVED_WORDS.add("enum");
        ReservedWords.RESERVED_WORDS.add("instanceof");
        ReservedWords.RESERVED_WORDS.add("return");
        ReservedWords.RESERVED_WORDS.add("transient");
        ReservedWords.RESERVED_WORDS.add("catch");
        ReservedWords.RESERVED_WORDS.add("extends");
        ReservedWords.RESERVED_WORDS.add("int");
        ReservedWords.RESERVED_WORDS.add("short");
        ReservedWords.RESERVED_WORDS.add("try");
        ReservedWords.RESERVED_WORDS.add("char");
        ReservedWords.RESERVED_WORDS.add("final");
        ReservedWords.RESERVED_WORDS.add("interface");
        ReservedWords.RESERVED_WORDS.add("static");
        ReservedWords.RESERVED_WORDS.add("void");
        ReservedWords.RESERVED_WORDS.add("class");
        ReservedWords.RESERVED_WORDS.add("finally");
        ReservedWords.RESERVED_WORDS.add("long");
        ReservedWords.RESERVED_WORDS.add("strictfp");
        ReservedWords.RESERVED_WORDS.add("volatile");
        ReservedWords.RESERVED_WORDS.add("const");
        ReservedWords.RESERVED_WORDS.add("float");
        ReservedWords.RESERVED_WORDS.add("native");
        ReservedWords.RESERVED_WORDS.add("super");
        ReservedWords.RESERVED_WORDS.add("while");
        ReservedWords.RESERVED_WORDS.add("matches");
        ReservedWords.RESERVED_WORDS.add("collection");
    }

    public static boolean isReserved(String text, boolean ignoreCase)
    {
        boolean result = StringX.isBlank(text);
        if(!result)
        {
            String str = (ignoreCase) ? text.toLowerCase() : text;
            result = ReservedWords.RESERVED_WORDS.contains(str);
        }
        return result;
    }
}
