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

package com.lusidity.framework.internet.wikipedia;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/**
 * User: jjszucs Date: 7/10/12 Time: 20:04
 */
public
class WikipediaX
{
	private static final Pattern CLEANSE_PATTERN = Pattern.compile("[\\s/\\\\#%&{}<>\\?\\+]");
// -------------------------- STATIC METHODS --------------------------

    /**
     * "Guess" URL for a Wikipedia article.
     *
     * @param languageCode
     *     Language code.
     * @param title
     *     Article title.
     * @return "Guessed" URL.
     *
     * @throws java.net.URISyntaxException
     */
    public static
    URI guessArticleUri(String languageCode, CharSequence title)
        throws URISyntaxException
    {
        String cleanTitle = WikipediaX.cleanseTitle(title);
        //  See http://stackoverflow.com/questions/724043/http-url-address-encoding-in-java
        //      for this non-obvious behavior of java.net.URI
        return new URI(
            "http",
            languageCode + ".wikipedia.org",
            "/wiki/" + cleanTitle,
            null
        );
    }

    /**
     * Cleanse a Wikipedia page title, replacing characters that are not safe for use in URIs with underscores.
     *
     * @param title
     *     Title to cleanse.
     * @return Cleansed title.
     */
    public static
    String cleanseTitle(CharSequence title)
    {
        return WikipediaX.CLEANSE_PATTERN.matcher(title).replaceAll("_");
    }

    /**
     * "Guess" URL for a Wikipedia category.
     *
     * @param languageCode
     *     Language code.
     * @param title
     *     Category title.
     * @return "Guessed" URL.
     *
     * @throws java.net.URISyntaxException
     */
    public static
    URI guessCategoryUri(String languageCode, String title)
        throws URISyntaxException
    {
        String cleanTitle = WikipediaX.cleanseTitle(title);
        return new URI(
            "http",
            languageCode + ".wikipedia.org",
            "/wiki/Category:" + cleanTitle,
            null
        );
    }

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * Private constructor. This is a utility class.
     */
    private
    WikipediaX()
    {
	    super();
    }
}
