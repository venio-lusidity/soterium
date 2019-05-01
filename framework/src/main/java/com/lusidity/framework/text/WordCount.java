/*
 * Copyright (c) 2008-2012, Venio, Inc.
 * All Rights Reserved Worldwide.
 *
 * This computer software is protected by copyright law and international treaties.
 * It may not be duplicated, reproduced, distributed, compiled, executed,
 * reverse-engineered, or used in any other way, in whole or in part, without the
 * express written consent of Venio, Inc.
 *
 * Portions of this computer software also embody trade secrets, patents, and other
 * protected intellectual property of Venio, Inc. and third parties and are subject to
 * applicable laws, regulations, treaties, agreements, and other legal mechanisms.
 */

package com.lusidity.framework.text;

import java.text.BreakIterator;

public class WordCount
{
    private WordCount()
    {
	    super();
    }

    /**
     * Count words.
     *
     * @param text
     *     Text.
     * @return Number of words in text.
     */
    public static int count(String text)
    {
        int nWords = 0;

        BreakIterator bi = BreakIterator.getWordInstance();
        bi.setText(text);
        int index = bi.first();
        while (index != BreakIterator.DONE)
        {
            nWords++;
            index = bi.next();
        }

        return nWords;
    }
}
