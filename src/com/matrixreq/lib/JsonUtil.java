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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Administrator
 */
public class JsonUtil {
    /**
     * Adds a top level property in an existing json, without knowledge of its structure
     * @param jsonSource - can be null or empty, in this case we start with an empty JSON object: {}
     * @param jsonProperty
     * @param jsonValue
     * @return 
     */
    public static String addJsonTopProperty (String jsonSource, String jsonProperty, String jsonValue) {
        if (StringUtils.isEmpty(jsonSource))
            jsonSource = "{}";
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(jsonSource).getAsJsonObject();
        obj.addProperty(jsonProperty, jsonValue);
        return obj.toString();
    }

    /**
     * Adds a 2nd level property in an existing json, without knowledge of its structure
     * @param jsonSource - can be null or empty, in this case we start with an empty JSON object: {}
     * @param jsonProperty
     * @param jsonValue
     * @return 
     */
    public static String addJson2ndLevelProperty (String jsonSource, String jsonFirstLevel, String jsonProperty, String jsonValue) {
        if (StringUtils.isEmpty(jsonSource))
            jsonSource = "{}";
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(jsonSource).getAsJsonObject();
        JsonObject second = obj.get(jsonFirstLevel).getAsJsonObject();
        second.addProperty(jsonProperty, jsonValue);
        return obj.toString();
    }

    /**
     * This method appends a full JSON object to the end of another existing json object
     * Base: { a:a, b:b } child: { c:c } Result : { a:a, b:b, child: {c:c} }
     * @param base
     * @param childName
     * @param childObject
     * @return 
     * @throws java.lang.Exception 
     */
    public static String appendJsonObjectToEnd(String base, String childName, String childObject) throws Exception {
        if (StringUtils.isEmpty(base))
            throw new Exception("base string is not a JSON object");
        if (StringUtils.isEmpty(childName))
            throw new Exception("childName is empty");
        if (StringUtils.isEmpty(childObject))
            throw new Exception("childObject is empty");
        base = base.trim();
        if (! base.endsWith("}") || ! base.startsWith("{"))
            throw new Exception("base string is not a JSON object");
        String ret;
        String insideBase = base.substring(1, base.length() - 1).trim();
        if (insideBase.contains("\""))
            // base is not empty: let's add a comma
            insideBase += ",";
        ret = "{" + insideBase + "\"" + childName + "\":" + childObject + "}";
        return ret;
    }
    
    /**
     * One of the answers of http://stackoverflow.com/questions/10174898/how-to-check-whether-a-given-string-is-valid-json-in-java
     * @param in
     * @return true if the string is not empty and JSON valid
     */
    public static boolean isJsonSyntaxOk (String in) {
        if (StringUtils.isEmpty(in))
            return false;
        Gson gson = new Gson();
        try {
            gson.fromJson(in, Object.class);
            return true;
        } catch(Exception ex) { 
            return false;
        }
    }
    
    public static String toPrettyFormat(String jsonString) 
    {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }
}
