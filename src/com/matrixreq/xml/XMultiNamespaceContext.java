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

import org.apache.commons.lang3.tuple.Pair;

import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class XMultiNamespaceContext implements NamespaceContext {
    final Map<String,String> contexts = new HashMap<>();

    @SafeVarargs
    public XMultiNamespaceContext(Pair<String, String>... contexts) {
        for (Pair<String,String> pair : contexts) {
            this.contexts.put(pair.getLeft(), pair.getRight());
        }
    }

    public XMultiNamespaceContext(String prefix, String namespace) {
        this.contexts.put(prefix, namespace);
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
