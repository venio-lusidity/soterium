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

package com.lusidity.framework.internet.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Helper class for XML-related functions.
 *
 * @author jjszucs
 */
public class XmlHelper
{
/*
 *
 * Static Methods
 *
 */
    /**
     * Get text content from a child element with the specified tag name. If more than one child element matches the
     * specified tag name, the value of the first matching child element will be returned.
     *
     * @param element
     *     XML DOM element.
     * @param tagName
     *     Tag name.
     * @return Text value or the specified child element or null if not found.
     */
    public static String getTextValue(Element element, String tagName)
    {
        String value = null;

        NodeList nodes = element.getElementsByTagName(tagName);
        if (null != nodes)
        {
            int nNodes = nodes.getLength();
            if (nNodes > 0)
            {
                Element node = (Element) nodes.item(0);
                if (null != node)
                {
                    Node firstChild = node.getFirstChild();
                    if (null != firstChild)
                    {
                        value = firstChild.getNodeValue();
                    }
                }
            }
        }
        return value;
    }

/*
 *
 * Constructors
 *
 */
    private XmlHelper()
    {
	    super();
    }
}
