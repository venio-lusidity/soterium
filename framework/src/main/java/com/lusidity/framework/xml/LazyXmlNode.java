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

package com.lusidity.framework.xml;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.internet.http.HttpClientX;
import com.lusidity.framework.text.StringX;
import org.apache.commons.collections.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

public
class LazyXmlNode
{
// ------------------------------ FIELDS ------------------------------

	private static final Pattern PATTERN_PATH_SEPARATOR = Pattern.compile("/");
	private Node node = null;
	private Document document=null;
	

// -------------------------- STATIC METHODS --------------------------

    public LazyXmlNode(Document document, Node node) {
        super();

        if (null == document) {
            throw new IllegalArgumentException("Document cannot be null.");
        }

        this.node = node;
        this.document = document;
    }

	public static LazyXmlNode load(String xml, boolean dtdDisabled)
            throws ApplicationException {
        LazyXmlNode result = null;
        if (!StringX.isBlank(xml)) {
            xml = StringX.stripStart(xml, "\uFEFF");
            xml = StringX.replace(xml, "[^\\x20-\\x7e]", "");
            xml = StringX.replace(xml, "\"utf-8\" ?", "\"utf-8\"?");
            xml = StringX.replace(xml, "[^\\x20-\\x7e\\x0A]", "");
            xml = StringX.replace(xml, ">\r\n<", "><");
            xml = StringX.replace(xml, ">\r<", "><");
            xml = StringX.replace(xml, ">\n<", "><");
            xml = StringX.replace(xml, "< ?xml", "<?xml");
            xml = StringX.replace(xml, "< ? xml", "<?xml");
            xml = StringX.replace(xml, "\" ?>", "\"?>");
            xml = StringX.replace(xml, "\" ? >", "\"?>");
            if (!StringX.startsWith(xml, "<?xml") && !StringX.contains(xml, "\"?>")) {
                xml = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", xml);
            }
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true); 	        
            try {
	            if(dtdDisabled){
		            LazyXmlNode.makeSafe(dbf);
	            }
                DocumentBuilder db = dbf.newDocumentBuilder();
                InputSource inputSource = new InputSource(new StringReader(xml));
                Document document = db.parse(inputSource);
                result = new LazyXmlNode(document, document);
            } catch (Exception e) {
                throw new ApplicationException(e);
            }
        }
        return result;
    }

	/**
     * Load an XML document from a URI.
     *
     * @param uri
     * 	URI.
     *
     * @return Root node.
     */
	public static LazyXmlNode load(URI uri, boolean dtdDisabled)
            throws ApplicationException
	{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

		Document document;
		try
		{
			if(dtdDisabled){
				LazyXmlNode.makeSafe(dbf);
			}
            DocumentBuilder db = dbf.newDocumentBuilder();
            //  Note: DocumentBuilder.parse takes the URI parameter as a String, not an actual URI
            String response = HttpClientX.getString(uri);
            StringReader stringReader = new StringReader(response);
            InputSource inputSource = new InputSource(stringReader);
            document = db.parse(inputSource);
        }
		catch (Exception e)
		{
			throw new ApplicationException(e);
		}
		return new LazyXmlNode(document, document);
	}

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * Load an XML document from a file.
     *
     * @param file File.
     * @return true to load an XML document.
     * @throws com.lusidity.framework.exceptions.ApplicationException
     */
    public static LazyXmlNode load(File file, boolean dtdDisabled)
            throws ApplicationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document document;
        try {
	        if(dtdDisabled){
		        LazyXmlNode.makeSafe(dbf);
	        }
            DocumentBuilder db = dbf.newDocumentBuilder();
            document = db.parse(file);
        } catch (Exception e) {
            throw new ApplicationException(e);
        }

        return new LazyXmlNode(document, document);
    }

// --------------------- GETTER / SETTER METHODS ---------------------

	@SuppressWarnings("unused")
	public
	Node getNode()
	{
		return this.node;
	}

// -------------------------- OTHER METHODS --------------------------

	@SuppressWarnings("UnusedDeclaration")
	public
	void dump(PrintStream printStream)
	{
		this.dump(printStream, 1, this.node);
	}

	@SuppressWarnings("UnusedDeclaration")
	public
	int getIntegerOrZeroByRelativePath(String path)
	{
		int i = 0;
		String value = this.getValueByRelativePath(path);
		if (null != value)
		{
			try {
				i = Integer.parseInt(value);
			}
			catch (Exception ignored){
				i=0;
			}
		}
		return i;
	}

	public
	String getValueByRelativePath(String relativePath)
	{
		String[] values = this.getValuesByRelativePath(relativePath);
		return ((values != null) && (values.length == 1)) ? values[0] : null;
	}

	public
	String[] getValuesByRelativePath(String path)

	{
        String[] results = null;
		Collection<LazyXmlNode> nodes = this.getNodesByRelativePath(path);
        if((null != nodes) && !nodes.isEmpty())
        {
            int nNodes = nodes.size();
            results = new String[nNodes];
            int nodeIdx = 0;
            for (LazyXmlNode node : nodes)
            {
                results[nodeIdx++] = node.getValue();
            }
        }
		return results;
	}

	public
	Collection<LazyXmlNode> getNodesByRelativePath(String path)
	{
		Collection<LazyXmlNode> results;

		synchronized (this.getDocument())
		{
			String workingPath = path.endsWith("/") ? path : (path + '/');
			String[] parts = LazyXmlNode.PATTERN_PATH_SEPARATOR.split(workingPath);

			results = null;

            if(null != parts)
            {
                Collection<LazyXmlNode> workingNodes = new ArrayList<>();
                workingNodes.add(this);

                for (String part : parts)
                {
                    results = new ArrayList<>();
                    for (LazyXmlNode workingNode : workingNodes)
                    {
                        Collection<LazyXmlNode> workingResults = workingNode.getChildrenByName(part);
                        results.addAll(workingResults);
                    }
                    workingNodes = results;
                }
            }
		}

		return results;
	}

	public
	Collection<LazyXmlNode> getChildrenByName(String name)
	{
		Collection<LazyXmlNode> results;
		synchronized (this.getDocument())
		{
			results = new ArrayList<>();
			if ((null != this.node) && this.node.hasChildNodes())
			{
				NodeList children = this.node.getChildNodes();
				if (null != children)
				{
					int nChildren = children.getLength();
					for (int childIdx = 0; childIdx < nChildren; childIdx++)
					{
						Node child = children.item(childIdx);
						String childName = child.getLocalName();
						if (StringX.equalsIgnoreCase(name, childName))
						{
							LazyXmlNode lazyChild = new LazyXmlNode(this.getDocument(), child);
							results.add(lazyChild);
						}
					}
				}
			}
		}
		return results;
	}

	/**
	 * Get this node's text content.
	 *
	 * @return Text content.
	 */
	public
	String getValue()
	{
		return this.node.getTextContent();
	}

	/**
	 * Does this node have at least one child element with the specified local name?
	 *
	 * @param localName
	 * 	Local name.
	 *
	 * @return Node.
	 */
	@SuppressWarnings("UnusedDeclaration")
	public
	boolean hasChild(String localName)
	{
		boolean result = false;
		synchronized (this.getDocument())
		{
			NodeList childNodes = this.node.getChildNodes();
			int nChildNodes = childNodes.getLength();
			for (int i = 0; i < nChildNodes; i++)
			{
				Node childNode = childNodes.item(i);
				if (childNode.getLocalName().equals(localName))
				{
					result = true;
					break;
				}
			}
		}
		return result;
	}

	private
	void dump(PrintStream printStream, int level, Node pNode)
	{
		synchronized (this.getDocument())
		{
			String nodeName = pNode.getNodeName();
			String nodeValue = pNode.getNodeValue();

			for (int indent = 0; indent < level; indent++)
			{
				printStream.print('\t');
			}
			printStream.format("%s=%s\n", nodeName, nodeValue);

			NodeList childNodes = pNode.getChildNodes();
			int nChildren = childNodes.getLength();
			for (int childIdx = 0; childIdx < nChildren; childIdx++)
			{
				Node childNode = childNodes.item(childIdx);
				this.dump(printStream, level + 1, childNode);
			}
		}
	}

	/**
	 * Get DOM document.
	 * @return DOM document.
	 */
	public Document getDocument()
	{
		return this.document;
	}

	public
	String getAttribute(String attribute)
	{
		Node attributeNode = this.node.getAttributes().getNamedItem(attribute);
		return (attributeNode != null) ? attributeNode.getTextContent() : null;
	}

	@SuppressWarnings("unused")
	public
	LazyXmlNode getNodeByRelativePath(String path)
		throws ApplicationException
	{
		Collection<LazyXmlNode> nodes = this.getNodesByRelativePath(path);
		if (nodes.size() != 1)
		{
			throw new ApplicationException("Ambiguous path '%s'.", path);
		}
		return (LazyXmlNode) CollectionUtils.get(nodes, 0);
	}
	private static void makeSafe(DocumentBuilderFactory factory)
		throws ApplicationException
	{
		//boolean dtdDisabled = com.lusidity.Environment.getInstance().getConfig().isXmlDtdDisabled();
		String FEATURE_DTD = "http://apache.org/xml/features/disallow-doctype-decl";
		String FEATURE_GENERAL = "http://xml.org/sax/features/external-general-entities";
		String FEATURE_PARAMETER = "http://xml.org/sax/features/external-parameter-entities";
		String FEATURE_EXTERNAL = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
		String FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
		try
		{
			factory.setFeature(FEATURE_DTD, true);
			//if this version does not support completed disable of DTD, comment above and uncomment below
			/*factory.setFeature(FEATURE_GENERAL, false);
			factory.setFeature(FEATURE_PARAMETER, false);
			factory.setFeature(FEATURE_EXTERNAL, false);
			factory.setXIncludeAware(false);
			factory.setExpandEntityReferences(false);*/
		}
		catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
}
