/*
    Copyright (c) 2014-2023 Matrix Requirements GmbH - https://matrixreq.com

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.   
*/

package com.matrixreq.xml;

import com.matrixreq.lib.XmlUtil;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import static org.w3c.dom.Node.CDATA_SECTION_NODE;
import static org.w3c.dom.Node.TEXT_NODE;
import org.w3c.dom.NodeList;

/**
 *
 * @author Yves
 */
public class XElement {

    protected final Element elementBelow;

    public XElement (Node n) {
        elementBelow = (Element) n;
    }

    public boolean isNull() {
        return elementBelow == null;
    }

    public Element getUnderlyingElement() {
        return elementBelow;
    }

    public String getNodeName() {
        if (isNull())
            return null;
        return elementBelow.getNodeName();
    }

    public String getLocalName() {
        if (isNull())
            return null;
        return elementBelow.getLocalName();
    }

    private String getTextContent() {
        if (isNull())
            return null;
        return elementBelow.getTextContent();
    }

    public XElement getFirstChildElement() {
        if (isNull())
            return null;
        Node child = elementBelow.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE)
                return new XElement(child);
            child = child.getNextSibling();
        }
        return null;
    }

    /**
     * Get first child element of a given name -- if you need to look for a local name (regardless of the namespace), use getFirstChildElementLocal
     * @param childName
     * @return
     */
    public XElement getFirstChildElement(String childName) {
        if (isNull())
            return null;
        XElement child = getFirstChildElement();
        while (child != null) {
            if (child.getNodeName().equals(childName))
                return child;
            child = child.getNextSiblingElement();
        }
        return null;
    }

    /**
     * Get first child element of a given local name
     * @param childName
     * @return
     */
    public XElement getFirstChildElementLocal(String childName) {
        if (isNull())
            return null;
        XElement child = getFirstChildElement();
        while (child != null) {
            if (childName.equals(child.getLocalName()))
                return child;
            child = child.getNextSiblingElement();
        }
        return null;
    }

    public XElement getNextSiblingElement() {
        if (isNull())
            return null;
        Node child = elementBelow.getNextSibling();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE)
                return new XElement(child);
            child = child.getNextSibling();
        }
        return null;
    }

    public XElement getNextSiblingElement(String siblingName) {
        if (isNull())
            return null;
        Node child = elementBelow.getNextSibling();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(siblingName))
                return new XElement(child);
            child = child.getNextSibling();
        }
        return null;
    }

    public XElement getNextSiblingElementLocal(String siblingName) {
        if (isNull())
            return null;
        Node child = elementBelow.getNextSibling();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(siblingName))
                return new XElement(child);
            child = child.getNextSibling();
        }
        return null;
    }

    public XElement getPreviousSiblingElement(String siblingName) {
        if (isNull())
            return null;
        Node child = elementBelow.getPreviousSibling();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(siblingName))
                return new XElement(child);
            child = child.getPreviousSibling();
        }
        return null;
    }

    public XElement getPreviousSiblingElementLocal(String siblingName) {
        if (isNull())
            return null;
        Node child = elementBelow.getPreviousSibling();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(siblingName))
                return new XElement(child);
            child = child.getPreviousSibling();
        }
        return null;
    }

    public XElement getNextTwinElement() {
        return getNextSiblingElement(getNodeName());
    }

    public XElement getNextTwinElementLocal() {
        return getNextSiblingElementLocal(getLocalName());
    }

    public String getChildElementText(String childName) {
        if (isNull())
            return null;
        XElement child = getFirstChildElement(childName);
        if (child == null)
            return null;
        return child.getText();
    }

    /**
     * The Java getTextContent method on an element returns a concat of all text of all descendants which is almost
     * never what we want
     * @return texts just below this element
     */
    public String getText() {
        if (isNull())
            return null;
        String text = "";
        Node child = elementBelow.getFirstChild();
        while (child != null) {
            switch (child.getNodeType()) {
                case TEXT_NODE:
                case CDATA_SECTION_NODE:
                    text += " " + child.getNodeValue().trim();
            }
            child = child.getNextSibling();
        }
        return text.trim();
    }

    public String getChildElementTextLocal(String childName) {
        if (isNull())
            return null;
        XElement child = getFirstChildElementLocal(childName);
        if (child == null)
            return null;
        return child.getTextContent();
    }

    /**
     * Returns an attribute, or null if not found
     * @param attributeName
     * @return
     */
    public String getAttribute (String attributeName) {
        if (isNull())
            return null;
        try {
            return elementBelow.getAttribute(attributeName);
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * @param attributeName
     * @return null if doesn't exist or not a number
     */
    public Integer getIntAttribute (String attributeName) {
        String v = getAttribute(attributeName);
        if (v.isEmpty())
            return null;
        try {
            return Integer.parseInt(v);
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns an integer attribute, or -1 if not found, empty, or not a number
     * @param attributeName
     * @return
     */
    public int getIntAttributeOrMinus1 (String attributeName) {
        Integer i = getIntAttribute(attributeName);
        if (i == null)
            return -1;
        return i;
    }

    /**
     * This method works nicely but if there's an exception it does write an error to stderr
     * @return a String with the text representation of the node
     * @throws XmlException
     */
    public String nodeToString() throws XmlException  {
        try {
            Transformer xform = TransformerFactory.newInstance().newTransformer();
            xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            DOMSource source = new DOMSource(elementBelow);
            StreamResult res = new StreamResult(new StringWriter());
            xform.transform(source, res);
            String out = res.getWriter().toString();
            return out;
        }
        catch (Exception e) {
            throw new XmlException(e);
        }
    }

    public XElement addAttribute(String name, String value) throws XmlException {
        if (isNull())
            return null;
        Document doc = elementBelow.getOwnerDocument();
        Attr attr = doc.createAttribute(name);
        attr.setValue(value);
        elementBelow.setAttributeNode(attr);
        return this;
    }

    public XElement addAttribute (String name, int value) throws XmlException  {
        return addAttribute(name, Integer.toString(value));
    }

    public void appendTextToElement(String text) throws XmlException {
        if (isNull())
            throw new XmlException("No element below");
        if (text == null)
            throw new XmlException("No text");
        Document doc = elementBelow.getOwnerDocument();
        elementBelow.appendChild(doc.createTextNode(text));
    }

    public void appendTextOrXmlToElement (String text) throws XmlException {
        if (isNull())
            throw new XmlException("No element below");
        if (text == null)
            throw new XmlException("No text");
        Document doc = elementBelow.getOwnerDocument();
        try {
            if (XDocument.isStringXmlValid(text)) {
                Document subDoc = XmlUtil.stringToXmlDoc (text);
                elementBelow.appendChild(doc.importNode(subDoc.getDocumentElement(), true));
            }
            else
                elementBelow.appendChild(doc.createTextNode(text));
        }
        catch (Exception e) {
            throw new XmlException (e);
        }
    }

    /**
     * Appends a new child element with a text inside to an existing element
     * @param nameSpace the child element namespace
     * @param childName the name of the element to create
     * @param childText the text inside the element or null
     * @return the element created
     * @throws com.matrixreq.xml.XmlException
     */
    public XElement appendTextElementChildNS (String nameSpace, String childName, String childText) throws XmlException {
        if (isNull())
            throw new XmlException("No element below");
        Document doc = elementBelow.getOwnerDocument();
        Element childElement = doc.createElementNS(nameSpace, childName);
        XElement childElementX = new XElement(childElement);
        if (StringUtils.isNotEmpty(childText))
            childElementX.appendTextToElement(childText);
        elementBelow.appendChild(childElement);
        return childElementX;
    }

    /**
     * Appends a new child element with a text inside to an existing element
     * @param nameSpace the child element namespace
     * @param childName the name of the element to create
     * @param cDATAText
     * @return the element created
     * @throws com.matrixreq.xml.XmlException
     */
    public XElement appendCDATAElementChildNS (String nameSpace, String childName, String cDATAText) throws XmlException {
        if (isNull())
            throw new XmlException("No element below");
        Document doc = elementBelow.getOwnerDocument();
        Element childElement = doc.createElementNS(nameSpace, childName);
        XElement childElementX = new XElement(childElement);
        if (StringUtils.isNotEmpty(cDATAText))
            childElement.appendChild(doc.createCDATASection(cDATAText));
        elementBelow.appendChild(childElement);
        return childElementX;
    }

    public XElement appendElementNS (String nameSpace, String childName) throws XmlException {
        if (isNull())
            throw new XmlException("No element below");
        Document doc = elementBelow.getOwnerDocument();
        Element childElement = doc.createElementNS(nameSpace, childName);
        elementBelow.appendChild(childElement);
        XElement childElementX = new XElement(childElement);
        return childElementX;
    }

    public XElement appendElement (String childName) throws XmlException {
        if (isNull())
            throw new XmlException("No element below");
        Document doc = elementBelow.getOwnerDocument();
        Element childElement = doc.createElement(childName);
        elementBelow.appendChild(childElement);
        XElement childElementX = new XElement(childElement);
        return childElementX;
    }

    /**
     * Copies this XML element and its subtree to another document
     * @param targetDoc - receiving document
     * @param targetMother - receiving mother in that document
     * @return the newly inserted element (in the receiving xml)
     * @throws com.matrixreq.xml.XmlException
     */
    public XElement copyAlienXmlElement (Document targetDoc, Element targetMother) throws XmlException {
        if (isNull())
            throw new XmlException("No element below");
        Node copiedNode = targetDoc.importNode(elementBelow, true);  // , true means import all the tree
        return new XElement(targetMother.appendChild(copiedNode));
    }

    public void appendCDATA(String text) throws XmlException {
        if (isNull())
            throw new XmlException("No element below");
        Document doc = elementBelow.getOwnerDocument();
        elementBelow.appendChild(doc.createCDATASection(text));
    }

    public String formatDoubleSmart (double value) {
        return new DecimalFormat("#.##").format(value);
    }

    public void addAttributeDoubleSmart (String name, double value) throws XmlException  {
        addAttribute (name, formatDoubleSmart(value));
    }

    public ArrayList<String> getAttributeLocalNames() throws XmlException {
        if (isNull())
            throw new XmlException("No element below");
        NamedNodeMap attributes = elementBelow.getAttributes();
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < attributes.getLength(); i++)
            names.add(attributes.item(i).getLocalName());
        return names;
    }

    public void appendComment(String commentString) throws XmlException {
        if (isNull())
            throw new XmlException("No element below");
        Document doc = elementBelow.getOwnerDocument();
        Comment comm = doc.createComment(commentString);
        elementBelow.appendChild(comm);
    }

    /**
     * Computes an XPath String expression on a document
     * http://viralpatel.net/blogs/java-xml-xpath-tutorial-parse-xml/
     * @param xpathExpression
     * @param ns - set to null if no namespace is used
     * @param url
     * @return the XPath result
     * @throws com.matrixreq.xml.XmlException
     */
    public String getXPathString(String xpathExpression, String ns, String url) throws XmlException {
        if (isNull())
            throw new XmlException("No element below");
        try {
            javax.xml.xpath.XPath xPath =  XPathFactory.newInstance().newXPath();
            if (ns != null)
                xPath.setNamespaceContext(new XMultiNamespaceContext (ns, url));
            String res = xPath.compile(xpathExpression).evaluate(elementBelow);
            return res;
        } catch (Exception ex) {
            throw new XmlException(ex);
        }
    }

    public ArrayList<XElement> getXPathElements(String xpathExpression, String ns, String url) throws XmlException {
        if (isNull())
            throw new XmlException("No element below");
        try {
            javax.xml.xpath.XPath xPath =  XPathFactory.newInstance().newXPath();
            if (ns != null)
                xPath.setNamespaceContext(new XMultiNamespaceContext(ns, url));
            XPathExpression expr = xPath.compile(xpathExpression);
            NodeList nodeSet = (NodeList) expr.evaluate(elementBelow, XPathConstants.NODESET);
            ArrayList<XElement> ret = new ArrayList<>();
            for (int i = 0; i < nodeSet.getLength(); i++)
                if (nodeSet.item(i).getNodeType() == Node.ELEMENT_NODE)
                    ret.add(new XElement(nodeSet.item(i)));
            return ret;
        } catch (Exception ex) {
            throw new XmlException(ex);
        }
    }

    public XElement getXPathElement (String xpathExpression, String ns, String url) throws XmlException {
        ArrayList<XElement> elements = getXPathElements(xpathExpression, ns, url);
        if (elements.isEmpty())
            return null;
        return elements.get(0);
    }

    public XElement getParentElement () throws XmlException {
        if (isNull())
            throw new XmlException("No element below");
        return new XElement (elementBelow.getParentNode());
    }

    public void removeAttribute(String attribute) throws XmlException {
        if (isNull())
            throw new XmlException("No element below");
        elementBelow.removeAttribute(attribute);
    }

    public void removeChild(XElement item) throws XmlException {
        if (isNull() || item == null || item.isNull())
            throw new XmlException("No element below");
        elementBelow.removeChild(item.getUnderlyingElement());
    }

    public void removeFromParent() throws XmlException {
        if (isNull())
            throw new XmlException("No element below");
        XElement parent = new XElement(elementBelow.getParentNode());
        parent.removeChild(this);
    }

    public void moveToNewParent(XElement parentFolder, XElement beforeThisOne) throws XmlException {
        if (isNull() || parentFolder == null || beforeThisOne == null || parentFolder.isNull() || beforeThisOne.isNull())
            throw new XmlException("No element below");
        elementBelow.getParentNode().removeChild(elementBelow);
        parentFolder.getUnderlyingElement().insertBefore(elementBelow, beforeThisOne.getUnderlyingElement());
    }

    /**
     * Removes any text or cdata child
     * @throws XmlException
     */
    public void removeText() throws XmlException {
        if (isNull())
            throw new XmlException("No element below");
        ArrayList<Node> toDel = new ArrayList<>();
        Node child = elementBelow.getFirstChild();
        while (child != null) {
            switch (child.getNodeType()) {
                case TEXT_NODE:
                case CDATA_SECTION_NODE:
                    toDel.add(child);
                    break;
            }
            child = child.getNextSibling();
        }
        for (Node node: toDel)
            elementBelow.removeChild(node);
    }

    /**
     * @param localName
     * @return the list of all children elements of this element having the element local name
     */
    public ArrayList <XElement> getAllChildrenElementLocal(String localName) {
        ArrayList <XElement> children = new ArrayList<>();
        XElement child = getFirstChildElementLocal(localName);
        while (child != null) {
            children.add(child);
            child = child.getNextTwinElementLocal();
        }
        return children;
    }

    /**
     * @return the list of all children elements of this element
     */
    public ArrayList <XElement> getAllChildrenElement() {
        ArrayList <XElement> children = new ArrayList<>();
        XElement child = getFirstChildElement();
        while (child != null) {
            children.add(child);
            child = child.getNextSiblingElement();
        }
        return children;
    }
}
