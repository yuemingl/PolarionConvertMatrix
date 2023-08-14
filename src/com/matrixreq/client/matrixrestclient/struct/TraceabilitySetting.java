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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Yves
 */
public class TraceabilitySetting {
    
    public ArrayList<TraceRuleCategory> rules;
    
    public boolean renameOneCategory(String oldShort, String newShort) {
        boolean modified = false;
        if (rules != null)
            for (TraceRuleCategory rule: rules)
                modified |= rule.renameOneCategory(oldShort, newShort);
        return modified; 
    }    

    public List<String> getUpCategoriesForCategory(String categoryShort) {
        if (rules != null)
            for (TraceRuleCategory tr: rules)
                if (tr.category.equals(categoryShort)) 
                    return tr.getUpCategories();
        return new ArrayList<>();
    }

    public List<String> getDownCategoriesForCategory(String categoryShort) {
        if (rules != null)
            for (TraceRuleCategory tr: rules)
                if (tr.category.equals(categoryShort)) 
                    return tr.getDownCategories();
        return new ArrayList<>();
    }

    public boolean removeOneCategory(String catShort) { 
        boolean modified = false;
        if (rules != null) {
            int ruleNo = 0;
            while (ruleNo < rules.size()) {
                TraceRuleCategory rule = rules.get(ruleNo);
                if (rule.category.equals(catShort)) {
                    rules.remove(ruleNo);
                    modified = true;
                }
                else {
                    modified |= rule.removeOneCategory(catShort);
                    ruleNo++;
                }
            }
        }
        return modified; 
    }
}
