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

import java.util.*;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * XPath class, with default namespace
 * See http://www.edankert.com/defaultnamespaces.html
 * @author Yves
 */
public class XPathUtil {
    private XPath xpath;

    class MyContext implements NamespaceContext {
        private final String defaultPrefix;
        private final String defaultNameSpace;

        public MyContext (String defaultPrefix, String defaultNameSpace) {
            this.defaultNameSpace = defaultNameSpace;
            this.defaultPrefix = defaultPrefix;
        }

        @Override
        public String getNamespaceURI( String prefix) {
            if ( prefix.equals( this.defaultPrefix ))
                return defaultNameSpace;
            return null;
        }

        @Override
        public String getPrefix( String namespaceURI) {
            if ( namespaceURI.equals(defaultNameSpace))
                return defaultPrefix;
            return null;
        }

        @Override
        public Iterator <String> getPrefixes( String namespaceURI) {
            ArrayList <String> list = new ArrayList<>();
            if ( namespaceURI.equals(defaultNameSpace))
                list.add(defaultPrefix);
            return list.iterator();
        }
    }

    private static class MultiNamespaceContext implements NamespaceContext {
        final Map<String,String> contexts;

        @SafeVarargs
        public MultiNamespaceContext(Pair<String, String>... contexts) {
            this.contexts = new HashMap<>();
            for (Pair<String,String> pair : contexts) {
                this.contexts.put(pair.getLeft(), pair.getRight());
            }
        }
        public void addContext(String prefix, String uri) {
            contexts.put(prefix, uri);
        }

        @Override
        public String getNamespaceURI(String prefix) {
            return contexts.get(prefix);
        }

        @Override
        public String getPrefix(String namespaceURI) {
            for (Map.Entry<String,String> entry : contexts.entrySet()) {
                if (entry.getValue().equals(namespaceURI)) {
                    return entry.getKey();
                }
            }
            return null;
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            return contexts.values().iterator();
        }
    }

    /**
     * Constructor with default prefix
     * @param defaultPrefix
     * @param defaultNameSpace
     */
    public XPathUtil(String defaultPrefix, String defaultNameSpace) {
        XPathFactory factory = XPathFactory.newInstance();
        xpath = factory.newXPath();
        xpath.setNamespaceContext(new MyContext (defaultPrefix, defaultNameSpace));
    }

    /**
     * Constructor for cases with multiple namespaces
     * @param namespaces An instance of a
     */
    public XPathUtil(Pair<String, String>... namespaces) {
        XPathFactory factory = XPathFactory.newInstance();
        xpath = factory.newXPath();
        MultiNamespaceContext namespaceContext = new MultiNamespaceContext(namespaces);
        xpath.setNamespaceContext(namespaceContext);
    }

    /**
     * Constructor with no default prefix
     */
    public XPathUtil() {
        XPathFactory factory = XPathFactory.newInstance();
        xpath = factory.newXPath();
    }

    public String xPath (Document xmlDocument, String xpathExpression) {
        try {
            String res = xpath.compile(xpathExpression).evaluate(xmlDocument);
            return res;
        } catch (XPathExpressionException ex) {
            return "Exception: " + ex.getMessage();
        }
    }

    public NodeList xPathNodeList (Document xmlDocument, String xpathExpression) throws XmlUtil.XmlUtilException {
        try {
            XPathExpression expr = xpath.compile(xpathExpression);
            NodeList nodeSet = (NodeList)expr.evaluate(xmlDocument, XPathConstants.NODESET);
            return nodeSet;
        } catch (XPathExpressionException ex) {
            throw new XmlUtil.XmlUtilException();
        }
    }
}
