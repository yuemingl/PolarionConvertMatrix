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
public class TraceRuleCategory {
    public String category;
    public String end_point;
    public Boolean creates_end;
    public int expected;
    public List<String> reporting;
    public List<TraceRule> down_rules;
    public List<TraceRule> up_rules;

    public boolean renameOneCategory(String oldShort, String newShort) {
        boolean modified = false;
        if (oldShort.equals(category)) {
            category = newShort;
            modified = true;
        }
        if (down_rules != null)
            for (TraceRule rule: down_rules)
                modified |= rule.renameOneCategory(oldShort, newShort);
        if (up_rules != null)
            for (TraceRule rule: up_rules)
                modified |= rule.renameOneCategory(oldShort, newShort);
        return modified;
    }
    
    public ArrayList<String> getUpCategories() {
        ArrayList<String> ret = new ArrayList<>();
        for (TraceRule rule: up_rules)
            if (rule.any_of != null)
                ret.addAll(rule.any_of);
        return ret;
    }

    public ArrayList<String> getDownCategories() {
        ArrayList<String> ret = new ArrayList<>();
        for (TraceRule rule: down_rules)
            if (rule.any_of != null)
                ret.addAll(rule.any_of);
        return ret;
    }
    
    public boolean removeOneCategory(String catShort) {
        boolean modified = false;

        if (down_rules != null) {
            int ruleNo = 0;
            while (ruleNo < down_rules.size()) {
                TraceRule rule = down_rules.get(ruleNo);
                if (rule.any_of != null && rule.any_of.contains(catShort)) {
                    if (rule.any_of.size() == 1)
                        down_rules.remove(ruleNo);
                    else {
                        rule.any_of.remove(catShort);
                        ruleNo++;
                    }
                }
                else
                    ruleNo++;
            }
        }

        if (up_rules != null) {
            int ruleNo = 0;
            while (ruleNo < up_rules.size()) {
                TraceRule rule = up_rules.get(ruleNo);
                if (rule.any_of != null && rule.any_of.contains(catShort)) {
                    if (rule.any_of.size() == 1)
                        up_rules.remove(ruleNo);
                    else {
                        int pos = rule.any_of.indexOf(catShort);
                        rule.any_of.remove(pos);
                        ruleNo++;
                    }
                }
                else
                    ruleNo++;
            }
        }

        return modified;
    }
    
}
