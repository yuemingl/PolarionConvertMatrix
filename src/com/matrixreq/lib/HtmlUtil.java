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

import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

/**
 * Quick-n-dirty Html utilities
 * @author Yves
 */
public class HtmlUtil {
    private enum State {BASE, SPACE, ATTRIBUTE, VALUE, VALUE_QUOTE, VALUE_DOUBLEQUOTE};
    
    /**
     * Extracts the list of attributes and values from a string containing a tag like <img sr='abc'>"
     * @param html
     * @return list of string pairs: attribute and value
     */
    public static ArrayList<String[]> extractAttributesFromHtmlTag(String html) {
        ArrayList <String[]> list = new ArrayList<>();
        String currentAttribute = "";
        String currentValue = "";
        State state = State.BASE;
        
        // <span class="mfig" data-mid="X1">
        
        if (StringUtils.isEmpty(html))
            return list;
        
        for (char c: html.toCharArray()) {
            switch (state) {
                case BASE:
                    // looking for end of element
                    if (c == ' ') {
                        state = State.SPACE;
                    }
                    break;
                case SPACE:
                    // looking for first letter of attribute
                    if (Character.isLetter(c)) {
                        state = State.ATTRIBUTE;
                        currentAttribute = "" + c;
                    }
                    break;
                case ATTRIBUTE:
                    // looking for space or equal, accumulating attribute 
                    switch (c) {
                        case ' ':
                        case '=':
                            state = State.VALUE;
                            break;
                        default:
                            currentAttribute += c;
                    }
                    break;
                case VALUE:
                    // looking for quote or dble quote
                    switch (c) {
                        case '\'':
                            state = State.VALUE_QUOTE;
                            currentValue = "";
                            break;
                        case '"':
                            state = State.VALUE_DOUBLEQUOTE;
                            currentValue = "";
                            break;
                    }
                    break;
                case VALUE_QUOTE:
                    if (c == '\'') {
                        list.add(new String[]{currentAttribute, currentValue});
                        state = State.SPACE;
                    }
                    else
                        currentValue += c;
                    break;
                case VALUE_DOUBLEQUOTE:
                    if (c == '"') {
                        list.add(new String[]{currentAttribute, currentValue});
                        state = State.SPACE;
                    }
                    else
                        currentValue += c;
                    break;
            }
        }
        return list;
        
    }

    /*
        Splits an html string into a list of strings, each string being either a tag or a text
        Warning: very crude - doesn't handle complicated cases like < or > in attributes and such
    */    
    public static String [] splitHtml(String html) {
        if (StringUtils.isEmpty(html))
            return null;
        ArrayList <String> list = new ArrayList<>();
        String current = "";
        for (char c: html.toCharArray()) {
            switch (c) {
                case '<':
                    if (StringUtils.isNotEmpty(current)) 
                        list.add(current);
                    current = "<";
                    break;
                case '>':
                    current += ">";
                    list.add(current);
                    current = "";
                    break;
                default:
                    current += c;
            }
        }
        if (StringUtils.isNotEmpty(current)) 
            list.add(current);
        return list.toArray(new String[list.size()]);
    }
    
    public static org.jsoup.nodes.Document parseHtml(String html) {
        return Jsoup.parse(html);
    }
    
    // Adapted from https://stackoverflow.com/questions/5640334/how-do-i-preserve-line-breaks-when-using-jsoup-to-convert-html-to-plain-text
    public static String htmlToTextKeepEndOfLines(String html) {
        org.jsoup.nodes.Document document = HtmlUtil.parseHtml(html);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
        document.select("br").append("\\n");
        document.select("p").prepend("\\n\\n");
        document.select("ul").prepend("\\n");
        document.select("li").append("\\n");
        String s = document.html().replaceAll("\\\\n", "\n");
        return Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
    }
}
