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
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Administrator
 */
public class LabelsSetting {
    public ArrayList<LabelSetting> labels;
    public ArrayList<LabelGroup> groups;

    public ArrayList<LabelSetting> getLabels() {
        return labels;
    }

    public void setLabels(ArrayList<LabelSetting> labels) {
        this.labels = labels;
    }

    public ArrayList<LabelGroup> getGroups() {
        return groups;
    }

    public void setGroups(ArrayList<LabelGroup> groups) {
        this.groups = groups;
    }
	
    public boolean renameOneCategory(String oldShort, String newShort) {
        boolean modified = false;
        if (labels != null)
            for (LabelSetting label: labels)
                modified |= label.renameOneCategory(oldShort, newShort);
        return modified; 
    }    

    public boolean doesCategoryHasLabel (String catShort) {
        if (labels != null)
            for (LabelSetting label: labels)
                if (label.getCategories().contains(catShort))
                    return true;
        return false;
    }
    
    public String getDisplayLabel(String label) {
        if (labels != null)
            for (LabelSetting lab: labels)
                if (lab.label.equals(label)) {
                    if (StringUtils.isNotEmpty(lab.getDisplayName()))
                        return lab.getDisplayName();
                    else
                        return label;   
                }
        return label;
    }

    public ArrayList<String> getLabelsForCategory(String catShort) {
        ArrayList<String> ret = new ArrayList<>();
        if (labels != null)
            for (LabelSetting label: labels)
                if (label.getCategories().contains(catShort))
                    ret.add(label.label);
        return ret;
    }
}
