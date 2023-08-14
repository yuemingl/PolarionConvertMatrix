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

import com.matrixreq.lib.FileUtil;
import com.matrixreq.lib.XmlUtil;
import com.matrixreq.lib.ZipUtil;
import com.matrixreq.lib.XmlUtil.XmlUtilException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
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
public class XDocument {


    public static final String XML_ZIP_EXTENSION = "xmlz";
    

    /**
     * Converts an XML string to a document
     * @param s
     * @return the XML document
     * @throws XmlException
     */
    public static XDocument stringToXmlDoc (String s) throws XmlException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        DocumentBuilder builder;
        try {
            builder = docFactory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(s)));
            XDocument xdoc = new XDocument(doc);
            return xdoc;
        } catch (Exception ex) {
            throw new XmlException(ex.getMessage());
        }
    }

    /**
     * Load an XML file from a File
     * @param fXmlFile
     * @return
     * @throws XmlException
     */
    public static XDocument loadXmlFile(File fXmlFile) throws XmlException {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            // http://www.edankert.com/defaultnamespaces.html : if we don't do this, we can't search with XPath on namespaces
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder;
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            return new XDocument(doc);
        } catch (Exception ex) {
            throw new XmlException(ex.getMessage());
        }
    }

    /**
     * Loads an XML document from a file path given the path as a string
     * @param fileName
     * @return
     * @throws XmlException
     */
    public static XDocument loadXmlFile(String fileName) throws XmlException {
        File fXmlFile = new File(fileName);
        return loadXmlFile (fXmlFile);
    }

    /**
     * This function can load an xml file or one stored in a zip, provided the zip only has one file in it
     * @param fileName
     * @return
     * @throws XmlException
     */
    public static XDocument loadXmlFileFromXmlOrZip(String fileName) throws XmlException {
        File fXmlFile = new File(fileName);
        if (fXmlFile.exists())
            return loadXmlFile(fXmlFile);
        File fZipFile = new File(fileName.replace(".xml", "." + XML_ZIP_EXTENSION));
        if (! fZipFile.exists())
            throw new XmlException("File doesn't exist");
        try {
            String tempFolder = FileUtil.createTempFolder("unzip");
            ZipUtil.unzipFile(fZipFile, new File(tempFolder), null);
            ArrayList<String> listFiles = FileUtil.listFiles(tempFolder, null);
            if (listFiles == null || listFiles.size() != 1)
                throw new Exception("No files in that zip");
            fXmlFile = new File(tempFolder, listFiles.get(0));
            XDocument ret = loadXmlFile(fXmlFile);
            FileUtil.removeFolderSafe(tempFolder);
            return ret;
        }
        catch (Exception ex) {
            throw new XmlException("Unable to unzip " + fZipFile.getAbsolutePath() + " - " + ex.getMessage());
        }
    }

    /**
     * Use this method if you're sure it's a valid string. It returns a null if there's an exception
     * @param s
     * @return
     */
    public static XDocument stringToXmlDocSafe (String s) {
        try {
            return stringToXmlDoc (s);
        }
        catch (Exception ignore) {
            return null;
        }
    }
    private final Document docBelow;
    private XMultiNamespaceContext namespaces;

    public XDocument(Document doc) {
        docBelow = doc;
    }

    public Document getUnderlyingDocument() {
        return docBelow;
    }

    public XElement getDocumentElement() {
        if (docBelow == null)
            return null;
        return new XElement(docBelow.getDocumentElement());
    }

    public void storeXml(File outputFile) throws XmlException {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 4);
            Transformer transformer = transformerFactory.newTransformer();
            // transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            // transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(docBelow);
            StreamResult result =  new StreamResult(outputFile);
            transformer.transform(source, result);
        } catch (Exception ex) {
            throw new XmlException(ex.getMessage());
        }
    }

    /**
     * Stores an XML as a xml file in a zip (for filter.xmlz)
     * @param outputFile - name of the .xml file. This function will actually create a .xmlz instead
     * @throws XmlException
     */
    public void storeXmlAsZip(File outputFile) throws XmlException {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 4);
            Transformer transformer = transformerFactory.newTransformer();
            // transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            // transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(docBelow);
            StreamResult result =  new StreamResult(outputFile);
            transformer.transform(source, result);

            String finalLocalName = outputFile.getName().replace(".xml", "." + XML_ZIP_EXTENSION);
            ZipUtil.zipFiles(null, outputFile.getParent(), outputFile.getName(), finalLocalName);
            outputFile.delete();
            
        } catch (Exception ex) {
            throw new XmlException(ex.getMessage());
        }
    }


    public String getAsString() throws XmlException {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 4);
            Transformer transformer = transformerFactory.newTransformer();
            // transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            // transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(docBelow);
            StringWriter sw = new StringWriter();
            StreamResult result =  new StreamResult(sw);
            transformer.transform(source, result);
            return sw.toString();
        } catch (Exception ex) {
            throw new XmlException(ex.getMessage());
        }
    }

    public static XDocument newDocument() throws XmlException {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            return new XDocument(doc);
        } catch (Exception ex) {
            throw new XmlException(ex.getMessage());
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
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Computes an XPath String expression on a document
     * http://viralpatel.net/blogs/java-xml-xpath-tutorial-parse-xml/
     * @param xpathExpression
     * @return the XPath result
     * @throws com.matrixreq.xml.XmlException
     */
    public String getXPathString(String xpathExpression) throws XmlException {
        try {
            XPath xPath =  XPathFactory.newInstance().newXPath();
            if (namespaces != null)
                xPath.setNamespaceContext(namespaces);
            String res = xPath.compile(xpathExpression).evaluate(docBelow);
            return res;
        } catch (Exception ex) {
            throw new XmlException(ex);
        }
    }

    /**
     * Computes an XPath String expression on a document
     * http://viralpatel.net/blogs/java-xml-xpath-tutorial-parse-xml/
     * @param xpathExpression
     * @return the XPath result
     * @throws com.matrixreq.xml.XmlException
     */
    public XElement getXPathElement(String xpathExpression) throws XmlException {
        try {
            ArrayList<XElement> xPathElements = getXPathElements(xpathExpression);
            if (xPathElements.isEmpty())
                return null;
            return xPathElements.get(0);
        } catch (Exception ex) {
            throw new XmlException(ex);
        }
    }

    /**
     * Computes an XPath String expression on a document
     * http://viralpatel.net/blogs/java-xml-xpath-tutorial-parse-xml/
     * @param xpathExpression
     * @return the XPath result
     * @throws com.matrixreq.xml.XmlException
     */
    public ArrayList<XElement> getXPathElements(String xpathExpression) throws XmlException {
        try {
            XPath xPath =  XPathFactory.newInstance().newXPath();
            if (namespaces != null)
                xPath.setNamespaceContext(namespaces);
            NodeList nodeSet = (NodeList)xPath.compile(xpathExpression).evaluate(docBelow, XPathConstants.NODESET);
            if (nodeSet == null)
                return null;
            ArrayList<XElement> ret = new ArrayList<>();
            for (int i = 0; i < nodeSet.getLength(); i++)
                ret.add(new XElement(nodeSet.item(i)));
            return ret;
        } catch (Exception ex) {
            throw new XmlException(ex);
        }
    }

    static private class NullResolver implements EntityResolver {
      @Override
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
     * @throws com.matrixreq.xml.XmlException
     */
    public XDocument readXmlFromStream(InputStream is) throws XmlException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            dbf.setValidating(false);
            dbf.setIgnoringComments(false);
            dbf.setIgnoringElementContentWhitespace(true);
            dbf.setNamespaceAware(true);

            DocumentBuilder db = dbf.newDocumentBuilder();
            EntityResolver e = new NullResolver();
            db.setEntityResolver(e);

            return new XDocument (db.parse(is));
        } catch (Exception ex) {
            throw new XmlException(ex);
        }
    }

    public XDocument fromJson(String jsonString, String enclosingElementName) throws XmlException {
        try {
            Document d = XmlUtil.fromJson(jsonString, enclosingElementName);
            return new XDocument(d);
        }
        catch (XmlUtilException e)  {
            throw new XmlException(e);
        }
    }

    public XDocument fromJsonWithNS(String jsonString, String enclosingElementName, String nameSpace) throws XmlException {
        try {
            Document d = XmlUtil.fromJsonWithNS(jsonString, enclosingElementName, nameSpace);
            return new XDocument(d);
        }
        catch (XmlUtilException e)  {
            throw new XmlException(e);
        }
    }

    /**
     * Call this if you want subsequent xpath calls to use a namespace - only 1 is supported by this class at this point
     * Call with (null, null) to cancel
     * @param namespace
     * @param url
     */
    public void setNameSpaceForXPath(String namespace, String url) {
        setNameSpacesForXPath(Pair.of(namespace, url));
    }

    public void setNameSpacesForXPath(Pair<String, String>... contexts) {
        this.namespaces = new XMultiNamespaceContext(contexts);
    }

    /**
     * See here: https://stackoverflow.com/questions/25588555/preventing-empty-xml-elements-are-converted-to-self-closing-elements
     * This output generator will not auto-close any element
     * @param out
     * @throws XmlException
     */
    public void saveWithNoAutoEnd(File out) throws XmlException  {
        try {
            XMLStreamWriter writer = XMLOutputFactory.newFactory().createXMLStreamWriter(new FileOutputStream(out));
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(getUnderlyingDocument());
            transformer.transform(source, new StAXResult(writer));
        }
        catch (Exception ex) {
            throw new XmlException(ex.getMessage());
        }
    }
}
