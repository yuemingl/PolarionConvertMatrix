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

import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Yves
 */
public class XPath {
    private final javax.xml.xpath.XPath xpath;
    
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
    
    public XPath(String namespace, String url) {
        XPathFactory factory = XPathFactory.newInstance();
        xpath = factory.newXPath();
        xpath.setNamespaceContext(new MyContext (namespace, url));
    }        

    public XPath() {
        XPathFactory factory = XPathFactory.newInstance();
        xpath = factory.newXPath();
    }
    
    public ArrayList<XElement> getElementList(XDocument doc, String xpathExpression) throws XmlException {
        try {
            XPathExpression expr = xpath.compile(xpathExpression);
            NodeList nodeSet = (NodeList) expr.evaluate(doc.getUnderlyingDocument(), XPathConstants.NODESET);
            ArrayList<XElement> ret = new ArrayList<>();
            for (int i = 0; i < nodeSet.getLength(); i++) 
                if (nodeSet.item(i).getNodeType() == Node.ELEMENT_NODE)
                    ret.add(new XElement(nodeSet.item(i)));
            return ret;
        } catch (Exception ex) {
            throw new XmlException();
        }
    }
}
