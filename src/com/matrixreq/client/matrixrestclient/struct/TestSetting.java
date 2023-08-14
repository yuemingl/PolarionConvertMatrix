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

package com.matrixreq.client.matrixrestclient.struct;

import com.matrixreq.lib.StringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Yves
 */
public class TestSetting {
    public String xtcType;
    public ArrayList<String> cloneSources;
    public Map<String, TestColumns> render;
    public List<PerStep> perStep;
    public List<Object> columnsSteps;  // if this is defined it's an obsolete structure (pre 1.5?)
    public List<TestResultManual> manual;
    public List<TestResultAutomatic> automatic;
    public String defaultTestResultResult;
    
    /**
     * @param oldShort
     * @param newShort
     * @return true if the object has been modified
     */
    public boolean renameOneCategory(String oldShort, String newShort) {
        boolean modified = false;
        if (cloneSources != null)
            if (cloneSources.contains(oldShort)) {
                cloneSources = StringUtil.replaceOneValue(cloneSources, oldShort, newShort);
                modified = true;
            }
        if (render != null) {
            Set<String> keySet = render.keySet();
            if (keySet.contains(oldShort)) {
                Map<String, TestColumns> newMap = new HashMap<>();
                for (String key: keySet) {
                    if (key.equals(oldShort))
                        newMap.put(newShort, render.get(key));
                    else
                        newMap.put(key, render.get(key));
                }
                render = newMap;
            }
            modified = true;
        }
        return modified; 
    }        
    
    public boolean isObsolete() {
        return columnsSteps != null && columnsSteps.size() > 0;
    }

    public PerStep getStepResultDisplay(String content) {
        for (PerStep per: perStep)
            if (content.equals(per.code))
                return per;
        return null;
    }

    public TestResult getGlobalResult(String content) {
        if (content.contains("|"))
            content = StringUtils.split(content,"|")[0];
        if (automatic != null)
            for (TestResult tr: automatic)
                if (tr.code.equals(content))
                    return tr;
        
        if (manual != null)
            for (TestResult tr: manual)
                if (tr.code.equals(content))
                    return tr;
        return null;
    }

    /**
     * Remove one category from this setting
     * @param catShort
     * @return true if something has been modified
     */
    public boolean removeOneCategory(String catShort) {
        boolean modified = false;
        
        if (this.isObsolete())
            // Don't know how to deal with these anymore
            return false;
        
        if (cloneSources != null)
            if (cloneSources.contains(catShort)) {
                cloneSources.remove(catShort);
                modified = true;
            }
        
        if (render != null) {
            Set<String> keySet = render.keySet();
            if (keySet.contains(catShort)) {
                render.remove(catShort);
                modified = true;
            }
        }
      
        return modified;
    }

}

/*

{
  "xtcType": "XTC",
  "cloneSources": ["UC","HTC","STC","UTC"],
  "presetFields":[ 
      {"field":"name", "value":"Version"},
      {"field":"name", "value":"Tester" } ],
  "render": {
    "UC": {
      "columns":[
        { "name": "Action", "field": "action", "editor": "text" },
        { "name": "Expected Result", "field": "expected", "editor": "text" }
    ]},
    "HTC":{"columns":[
        { "name": "Action", "field": "action", "editor": "text" },
        { "name": "Expected Result", "field": "expected", "editor": "text" }
    ]},
    "STC":{"columns":[
        { "name": "Action", "field": "action", "editor": "text" },
        { "name": "Expected Result", "field": "expected", "editor": "text" }
    ]},
    "UTC":{"columns":[
        { "name": "Action", "field": "action", "editor": "text" },
        { "name": "Expected Result", "field": "expected", "editor": "text" }
    ]},
    "XTC":{"columns":[
        { "name": "Action Result", "field": "action", "editor": "none" },
        { "name": "Expected Result", "field": "expected", "editor": "none" },
        { "name": "Passed/Failed", "field": "result", "editor": "result" },
        { "name": "Comment", "field": "comment", "editor": "text" }
    ]}
  },
  "defaultTestResultResult":"an|warning|not started",
  "automatic": [
    {
      "human": "passed",
      "code": "ap",
      "render": "ok",
      "rule": "all",
      "param": "p"
    },
    {
      "human": "failed",
      "code": "af",
      "render": "error",
      "rule": "one",
      "param": "f"
    },
    {
      "human": "not started",
      "code": "an",
      "render": "warning",
      "rule": "all",
      "param": ""
    },
    {
      "human": "documentation",
      "code": "ad",
      "render": "warning",
      "rule": "one",
      "param": "d"
    },
    {
      "human": "in progress",
      "code": "ai",
      "render": "warning",
      "rule": "",
      "param": ""
    }
  ],
  "manual": [
    {
      "human": "passed",
      "command": "passed",
      "render": "ok",
      "code": "p"
    },
    {
      "human": "passed with deviation",
      "command": "passed with deviation",
      "render": "ok",
      "code": "pf"
    },
    {
      "human": "failed",
      "command": "failed",
      "render": "error",
      "code": "f"
    },
    {
      "human": "",
      "command": "not executed",
      "render": "warning",
      "code": "r"
    },
    {
      "human": "to be decided",
      "command": "to be decided",
      "render": "warning",
      "code": "tbd"
    },
    {
      "human": "documentation",
      "command": "documentation",
      "render": "warning",
      "code": "d"
    },
    {
      "human": "in progress",
      "command": "in progress",
      "render": "warning",
      "code": "i"
    }
  ],
  "perStep": [
    {
      "human": "passed",
      "command": "passed",
      "render": "ok",
      "code": "p",
      "key": "p",
      "image": "success.png"
    },
    {
      "human": "failed",
      "command": "failed",
      "render": "error",
      "code": "f",
      "key": "f",
      "image": "fail.png"
    },
    {
      "human": "documentation",
      "command": "documentation",
      "render": "warning",
      "code": "d",
      "key": "d",
      "image": "docu.png"
    },
    {
      "human": "",
      "command": "not executed",
      "render": "warning",
      "code": "",
      "key": "r",
      "image": ""
    }
  ]
}

*/
