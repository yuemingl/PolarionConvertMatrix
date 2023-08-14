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

package com.matrixreq.lib;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.matrixreq.xml.XDocument;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import static org.w3c.dom.Node.CDATA_SECTION_NODE;
import static org.w3c.dom.Node.TEXT_NODE;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 *
 * @author Yves
 */
public class XmlUtil {
    
    private static String customXsltPath = null;
    private final static String XSLT_TOOL_LINUX = "/home/matrix/bin/xslt/xslt.sh";
    private final static String XSLT_TOOL_WINDOWS = "c:\\home\\matrix\\bin\\xslt\\xslt.cmd";

    /**
     * @return the custom path if caller did set it through setXsltPath, one of the default static above otherwise
     */
    private static String getXsltPath () {
        if (customXsltPath != null)
            return customXsltPath;
        if (FileUtil.runOnWindows())
            return XSLT_TOOL_WINDOWS;

        return XSLT_TOOL_LINUX;
    }
 
    /**
     * Call this method to specify where to find our custom xslt jar program
     * @param path
     */
    public static void setXsltPath (String path) {
        customXsltPath = path;
    }

    public static class XmlUtilException extends MatrixLibException {

        /**
         *
         */
        private static final long serialVersionUID = -3891094149621753016L;

        public XmlUtilException() {
            super("XmlUtilException");
        }
    }
    /**
     * This method works nicely but if there's an exception it does write an error to stderr
     * @param node Any node from an XML document
     * @return a String with the text representation of the node
     * @throws XmlUtilException 
     */
    public static String nodeToString(Node node) throws XmlUtilException  {
        try {
            Transformer xform = TransformerFactory.newInstance().newTransformer();
            xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            DOMSource source = new DOMSource(node);
            StreamResult res = new StreamResult(new StringWriter());
            xform.transform(source, res);
            String out = res.getWriter().toString();
            return out;
        }
        catch (TransformerException e) {
            throw new XmlUtilException();
        }
    }

    /**
     * Converts an XML string to a document
     * @param s
     * @return the XML document
     * @throws com.matrixreq.lib.XmlUtil.XmlUtilException
     */
    public static Document stringToXmlDoc (String s) throws XmlUtilException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();  
        docFactory.setNamespaceAware(true);
        DocumentBuilder builder;  
        try {
            builder = docFactory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(s)));  
            return doc;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new XmlUtilException();
        }
    }

    /**
     * Use this method if you're sure it's a valid string. It returns a null if there's an exception
     * @param s
     * @return 
     */
    public static Document stringToXmlDocSafe (String s) {
        try {
            return stringToXmlDoc (s);
        }
        catch (Exception ignore) {
            return null;
        }
    }

    // This method applies the xslFilename to inFilename and writes
    // the output to outFilename.
    // From: http://www.exampledepot.8waytrips.com/egs/javax.xml.transform/BasicXsl.html
    public static void xsl(String inFilename, String outFilename, String xslFilename) throws MatrixLibException {
        // Since we want to have XSLT 2.0 capability we have to use SaxonHE9
        // But if we link all our programs to SAXON it is giving us a lot of headache.
        // The deciding factor was when we discovered that a program linked with saxon9he.jar doesn't write CDATA on output anymore
        
        // I decide then to create a command line tool to convert xslts 
        
        String command = getXsltPath();
        ArrayList<String> res = ExecUtil.exec(new String[]{command, inFilename, xslFilename, outFilename});
        for (String s: res) {
            if (s.startsWith("E|")) {
                MatrixLibException ex = new MatrixLibException("XSLT error");
                for (String s2: res)
                    if (s2.startsWith("E|")) 
                        ex.addDetail(s2);
                throw ex;
            }
            else
                if (s.contains("FATAL ERROR"))
                    throw new MatrixLibException("XSLT: " + s);
                else
                    if (s.contains("ERROR"))
                        throw new MatrixLibException("XSLT: " + s);
        }
    }

    public static void addXmlAttrib (Document doc, Element element, String name, String value)  {
        Attr attr = doc.createAttribute(name);
        attr.setValue(value);
        element.setAttributeNode(attr);              
    }
    
    public static void addXmlAttrib (Document doc, Element element, String name, int value)  {
        addXmlAttrib (doc, element, name, Integer.toString(value));
    }

    /**
     * Checks if the text is valid XML. If this is the case, adds it as XML, else adds it as a text node
     * @param doc The base doc
     * @param element The element into which to add
     * @param text The text
     */
    public static void appendTextOrXmlToElement (Document doc, Element element, String text) {
        try {
            if (isStringXmlValid(text)) {
                Document subDoc = stringToXmlDoc (text);
                element.appendChild(doc.importNode(subDoc.getDocumentElement(), true));
            }
            else
                element.appendChild(doc.createTextNode(text));
        }
        catch (Exception e) {
            element.appendChild(doc.createTextNode(text));
        }
    }

    private static class SimpleErrorHandler implements ErrorHandler {
        @Override
        public void warning(SAXParseException e) throws SAXException {
            // System.out.println(e.getMessage());
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            // System.out.println(e.getMessage());
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            // System.out.println(e.getMessage());
        }
    }    
    
    // Check if a string is XML valid. We use a SAX Parser here since we don't need the DOM after the test has been made
    // See http://www.edankert.com/validate.html for code samples
    public static boolean isStringXmlValid (String in) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            
            SAXParser parser = factory.newSAXParser();
            
            XMLReader reader = parser.getXMLReader();
            reader.setErrorHandler(new SimpleErrorHandler());
            reader.parse(new InputSource(new StringReader(in)));
            return true;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            return false;
        }
    }
    
    /**
     * Adds a text to an element 
     * @param doc
     * @param element
     * @param text 
     */
    public static void appendTextToElement (Document doc, Element element, String text) {
        element.appendChild(doc.createTextNode(text));
    }
    
    /**
     * Appends a new child element with a text inside to an existing element
     * @param doc
     * @param mother the mother element
     * @param nameSpace the child element namespace
     * @param childName the name of the element to create
     * @param childText the text inside the element or null
     * @return the element created
     */
    public static Element appendTextElementChildNS (Document doc, Element mother, String nameSpace, String childName, String childText) {
        Element childElement = doc.createElementNS(nameSpace, childName);
        if (StringUtils.isNotEmpty(childText))
            appendTextToElement(doc, childElement, childText);
        mother.appendChild(childElement);
        return childElement;
    }

    /**
     * Load an XML file from a File 
     * @param fXmlFile
     * @return
     * @throws com.matrixreq.lib.XmlUtil.XmlUtilException 
     */
    public static Document loadXmlFile(File fXmlFile) throws XmlUtilException {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            // http://www.edankert.com/defaultnamespaces.html : if we don't do this, we can't search with XPath on namespaces
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder;
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            return doc;
        } catch (ParserConfigurationException ex) {
            throw new XmlUtilException();
        } catch (SAXException ex) {
            throw new XmlUtilException();
        } catch (IOException ex) {
            throw new XmlUtilException();
        }
    }
    
    
    /**
     * Loads an XML document from a file path given the path as a string
     * @param fileName
     * @return
     * @throws XmlUtilException
     */
    public static Document loadXmlFile(String fileName) throws XmlUtilException {
        File fXmlFile = new File(fileName);
        return loadXmlFile (fXmlFile);
    }

    /**
     * This function can load an xml file or one stored in a zip, provided the zip only has one file in it
     * @param fileName
     * @return
     * @throws XmlUtilException
     */
    public static Document loadXmlFileFromXmlOrZip(String fileName) throws XmlUtilException {
        File fXmlFile = new File(fileName);
        if (fXmlFile.exists())
            return loadXmlFile(fXmlFile);
        File fZipFile = new File(fileName.replace(".xml", "." + XDocument.XML_ZIP_EXTENSION));
        if (! fZipFile.exists()) 
            throw new XmlUtilException();
        try {
            String tempFolder = FileUtil.createTempFolder("unzip");
            ZipUtil.unzipFile(fZipFile, new File(tempFolder), null);
            ArrayList<String> listFiles = FileUtil.listFiles(tempFolder, null);
            if (listFiles == null || listFiles.size() != 1)
                throw new Exception("No files in that zip");
            fXmlFile = new File(tempFolder, listFiles.get(0));
            Document ret = loadXmlFile(fXmlFile);
            FileUtil.removeFolderSafe(tempFolder);
            return ret;
        }
        catch (Exception ex) {
            throw new XmlUtilException();
        }
    }

    
    /**
     * Returns an attribute, or an empty string if not found
     * @param node
     * @param attributeName
     * @return 
     */
    public static String getAttribute (Node node, String attributeName) {
        try {
            return node.getAttributes().getNamedItem(attributeName).getNodeValue();        
        }
        catch (Exception e) {
            return "";
        }
    }

    /**
     * Returns an integer attribute, or -1 if not found, empty, or not a number
     * @param node
     * @param attributeName
     * @return 
     */
    public static int getIntAttribute (Node node, String attributeName) {
        String v = getAttribute(node, attributeName);
        if (v.isEmpty())
            return -1;
        try {
            return Integer.parseInt(v);
        }
        catch (Exception e) {
            return -1;
        }
    }

    /**
     * Get first child element of a given name
     * @param element
     * @param childName
     * @return 
     */
    public static Element getFirstChildElement(Element element, String childName) {
        if (element == null)
            return null;
        Element child = getFirstChildElement(element);
        while (child != null) {
            if (child.getNodeName().equals(childName))
                return (Element) child;
            child = getNextSiblingElement(child);
        }
        return null;
    }
    
    // MATRIX-2588: it happens that the local name is null - call getNodeName instead
    // This post says it's only if you have a "Level1" DOM whatever that means, but it happens also somehow else
    // https://stackoverflow.com/questions/42562866/why-does-getlocalname-return-null
    public static String getLocalNameSafe(Element element) {
        String ret = element.getLocalName();
        if (ret != null)
            return ret;
        return element.getNodeName();
    }

    /**
     * Get first child element of a given LOCAL name
     * @param element
     * @param childLocalName
     * @return 
     */
    public static Element getFirstChildElementLocal(Element element, String childLocalName) {
        if (element == null)
            return null;
        Element child = getFirstChildElement(element);
        while (child != null) {
            String childName = XmlUtil.getLocalNameSafe(child);
            if (childLocalName.equals(childName))
                return (Element) child;
            child = getNextSiblingElement(child);
        }
        return null;
    }

    /**
     * Get first descendant element of a given name - when we cannot use XPAth
     * @param element
     * @param childName
     * @return 
     */
    public static Element getFirstDescendantElement(Element element, String childName) {
        if (element == null)
            return null;
        Element child = getFirstChildElement(element);
        while (child != null) {
            if (child.getNodeName().equals(childName))
                return child;
            Element lower = getFirstDescendantElement(child, childName);
            if (lower != null)
                return lower;
            child = getNextSiblingElement(child);
        }
        return null;
    }

    
    /**
     * Get next sibling element of a given name
     * @param element
     * @param childName
     * @return 
     */
    public static Element getNextSiblingElement(Element element, String childName) {
        if (element == null)
            return null;
        Element child = getNextSiblingElement(element);
        while (child != null) {
            if (childName.equals(child.getNodeName()))
                return (Element) child;
            child = getNextSiblingElement(child);
        }
        return null;
    }

    /**
     * @param element
     * @return next twin: next sibling with same name
     */
    public static Element getNextTwinElement(Element element) {
        String elementName = element.getNodeName();
        return getNextSiblingElement(element, elementName);
    }
    
    /**
     * Get next sibling element of a given LOCAL name
     * @param element
     * @param childLocalName
     * @return 
     */
    public static Element getNextSiblingElementLocal(Element element, String childLocalName) {
        if (element == null)
            return null;
        Element child = getNextSiblingElement(element);
        while (child != null) {
            if (childLocalName.equals(XmlUtil.getLocalNameSafe(child)))
                return (Element) child;
            child = getNextSiblingElement(child);
        }
        return null;
    }

    /**
     * Retrieve first child Element of an XML element
     * @param element
     * @return 
     */
    public static Element getFirstChildElement(Element element) {
        if (element == null)
            return null;
        Node child = element.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE)
                return (Element) child;
            child = child.getNextSibling();
        }
        return null;
    }

    /**
     * Get next sibling element of an element
     * @param element
     * @return 
     */
    public static Element getNextSiblingElement(Element element) {
        if (element == null)
            return null;
        Node child = element.getNextSibling();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE)
                return (Element) child;
            child = child.getNextSibling();
        }
        return null;
    }

    /**
     * Get previous sibling element of an element
     * @param element
     * @return 
     */
    public static Element getPreviousSiblingElement(Element element) {
        if (element == null)
            return null;
        Node child = element.getPreviousSibling();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE)
                return (Element) child;
            child = child.getPreviousSibling();
        }
        return null;
    }

    
    public static String getChildElementText(Element element, String childName) {
        if (element == null)
            return "";
        Element child = getFirstChildElement (element, childName);
        if (child == null)
            return "";
        return child.getTextContent();
    }

    /**
     * Copies a subtree to another document
     * @param targetDoc - receiving document
     * @param targetMother - receiving mother in that document
     * @param alienElement - subtree from another tree to copy
     * @return the newly inserted element (in the receiving xml)
     */
    public static Element copyAlienXmlElement (Document targetDoc, Element targetMother, Element alienElement) {
        Node copiedNode = targetDoc.importNode(alienElement, true);  // , true means import all the tree
        return (Element) targetMother.appendChild(copiedNode);
    }

    /**
     * Computes an XPath String expression on a document
     * http://viralpatel.net/blogs/java-xml-xpath-tutorial-parse-xml/
     * @param xmlDocument
     * @param xpathExpression
     * @return the XPath result
     */
    public static String getXPathString (Document xmlDocument, String xpathExpression) {
        try {
            XPath xPath =  XPathFactory.newInstance().newXPath();
            String res = xPath.compile(xpathExpression).evaluate(xmlDocument);
            return res;
        } catch (XPathExpressionException ex) {
            return "XPATH Exception: " + ex.getMessage();
        } catch (Exception ex) {
            return "Exception: " + ex.getMessage();
        }
    }
    
    /**
     * Stores an XML file to the disk
     * @param doc The XML document
     * @param outputFile The output File object
     * @throws XmlUtilException 
     */
    public static void storeXml (Document doc, File outputFile) throws XmlUtilException {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            // transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result =  new StreamResult(outputFile);
            transformer.transform(source, result);          
        } catch (TransformerConfigurationException ex) {
            throw new XmlUtilException();
        } catch (TransformerException ex) {
            throw new XmlUtilException();
        }
    }

    /**
     * Naive implementation: replaces all < with &lt; etc..
     * @param xml
     * @return 
     */
    public static String xmlToHtml (String xml) {
        return xml.replace("&", "&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    /**
     * Adds a CDATA text to an element 
     * @param doc
     * @param element
     * @param text 
     */
    public static void appendCDATAToElement (Document doc, Element element, String text) {
        element.appendChild(doc.createCDATASection(text));
    }

    /**
     * Formats a double with up 2 decimal digits, but removes unneeded zeroes at the end.
     * That is: 
     *   1.2345 will be 1.23
     *   1.2567 will be 1.26
     *   1.101 will be 1.1
     *   1.0005 will be 1
     * @param value
     * @return 
     */
    public static String formatDoubleSmart (double value) {
        return new DecimalFormat("#.##").format(value);
    }
    
    public static void addXmlAttribDoubleSmart (Document doc, Element element, String name, double value)  {
        addXmlAttrib (doc, element, name, formatDoubleSmart(value));
    }

    /**
     * Creates an XML document
     * @return
     * @throws ParserConfigurationException 
     */
    public static Document newDocument () throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        // MATRIX-2588 : see https://stackoverflow.com/questions/42562866/why-does-getlocalname-return-nulls
        docFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        return doc;
    }
    
    /**
     * @param elem
     * @param descendantName
     * @param attribute
     * @return The attribute of a descendant that we know exists only in one element. If it's not the case, returns empty string
     */
    public static String getSingleDescendantAttribute (Element elem, String descendantName, String attribute) {
        NodeList list = elem.getElementsByTagName(descendantName);
        if (list.getLength() == 1) {
            Element child = (Element) list.item(0);
            return child.getAttribute(attribute);
        }
        return "";
    }
    
    static class NullResolver implements EntityResolver {
      public InputSource resolveEntity(String publicId, String systemId) throws SAXException,
          IOException {
        return new InputSource(new StringReader(""));
      }
    }    
    
    /**
     * Read XML as DOM.
     * From http://www.java2s.com/Code/Java/XML/ReadXmlfromInputStreamandreturnDocument.htm
     * @param is
     * @return 
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    public static Document readXmlFromStream(InputStream is) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        dbf.setValidating(false);
        dbf.setIgnoringComments(false);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setNamespaceAware(true);

        DocumentBuilder db = null;
        db = dbf.newDocumentBuilder();
        EntityResolver e = new NullResolver();
        db.setEntityResolver(e);

        return db.parse(is);
    }

    /**
     * MATRIX-3805: fixes a problem where the json has key containing spaces ==> the XML was invalid
     * @param json
     * @return 
     */
    private static String replaceSpaceInJsonKeys(String json) {
        JsonParser parser = new JsonParser();
        JsonElement jsonTree = parser.parse(json);    
        JsonElement jsonTreeFixed = fixJsonElement(jsonTree);
        return jsonTreeFixed.toString();
    }
    
    /**
     * Recursively fix all spaces in keys to underscores
     * @param element
     * @return 
     */
    private static JsonElement fixJsonElement(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            JsonObject out = new JsonObject();
            for (Map.Entry<String, JsonElement> entry: object.entrySet()) {
                String key = entry.getKey();
                String newKey;
                if (key.contains(" ")) 
                    newKey = key.replace(" ", "_");
                else
                    newKey = key;
                out.add(newKey, fixJsonElement(entry.getValue()));
            }
            return out;
        }
        else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            JsonArray out = new JsonArray();
            for (JsonElement subElement: array) 
                out.add(fixJsonElement(subElement));
            return out;
        }
        else 
            return element;
    }    
    
    public static Document fromJson (String jsonString, String enclosingElementName) throws XmlUtilException {
        try {
            return fromJsonInternal(jsonString, enclosingElementName);
        }
        catch (XmlUtilException x) {
            String temp = replaceSpaceInJsonKeys(jsonString);
            return fromJsonInternal(temp, enclosingElementName);
        }
    }
    
    /**
     * Creates an XML equivalent of a JSON string 
     * @param jsonString may be null, in this case the XML will only contain an empty element
     * @param enclosingElementName the name of the top level element 
     * @return an XML document
     * @throws com.matrixreq.lib.XmlUtil.XmlUtilException 
     */
    protected static Document fromJsonInternal (String jsonString, String enclosingElementName) throws XmlUtilException {
        
        if (StringUtils.isEmpty(jsonString))
            return XmlUtil.stringToXmlDoc("<" + enclosingElementName + " />");
        
        JSONObject json = new JSONObject(jsonString);
        String xml = XML.toString(json, enclosingElementName);
        return XmlUtil.stringToXmlDoc(xml);
    }
    
    public static Document fromJsonWithNS (String jsonString, String enclosingElementName, String nameSpace) throws XmlUtilException {
        try {
            return fromJsonWithNSInternal(jsonString, enclosingElementName, nameSpace);
        }
        catch (XmlUtilException x) {
            String temp = replaceSpaceInJsonKeys(jsonString);
            return fromJsonWithNSInternal(temp, enclosingElementName, nameSpace);
        }
    }
    
    /**
     * Creates an XML equivalent of a JSON string 
     * @param jsonString may be null, in this case the XML will only contain an empty element
     * @param enclosingElementName the name of the top level element 
     * @param nameSpace the namespace of the target document
     * @return an XML document
     * @throws com.matrixreq.lib.XmlUtil.XmlUtilException 
     */
    protected static Document fromJsonWithNSInternal (String jsonString, String enclosingElementName, String nameSpace) throws XmlUtilException {
        
        if (StringUtils.isEmpty(jsonString))
            return XmlUtil.stringToXmlDoc("<" + enclosingElementName + " />");
        
        JSONObject json = new JSONObject(jsonString);
        String xml = XML.toString(json, enclosingElementName);
        // MATRIX-4981 This is to avoid passing on escaped characters to the XML
        // This is an issue for the dropdowns which can be partially escaped
        String escapedXml = convertLabelsToCDATA(xml);

        // We merely need to add the NS to the top level element before XML parsing, so I replace in the text <top> with <top xmlns='...'>
        xml = "<" + enclosingElementName + " xmlns='" + nameSpace + "'>" + escapedXml.substring(2 + enclosingElementName.length());
        
        Document doc = XmlUtil.stringToXmlDoc(xml);
        return doc;
    }

    public static String convertLabelsToCDATA(String xml) {
        Pattern p = Pattern.compile("<label>([^<].?.*?[^>]?)</label>");
        Matcher m = p.matcher(xml);
        StringBuilder b = new StringBuilder();
        int lastIndex = 0;
        while (m.find()) {
            b.append(xml, lastIndex, m.start(1));
            String content = m.group(1);
            String unescaped1 = StringEscapeUtils.unescapeXml(content);
            String unescaped2 = StringEscapeUtils.unescapeXml(unescaped1);
            b.append("<![CDATA[")
                    .append(unescaped2)
                    .append("]]>");
            lastIndex = m.end(1);
        }
        if (lastIndex < xml.length()) {
            b.append(xml, lastIndex, xml.length());
        }
        return b.toString();
    }

    /**
     * Returns the text that is immediately below this element
     * If you want the normal Java call, use Element.getTextContent()
     * @param element
     * @return 
     */
    public static String getText(Element element) {
        String text = "";
        Node child = element.getFirstChild();
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
    
}
