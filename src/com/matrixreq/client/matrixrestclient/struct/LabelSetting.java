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

/**
 *
 * @author Administrator
 */
public class LabelSetting {
    public String label;
    public ArrayList<String> categories;
    public String reportName;

    public LabelStyle style;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }
    
    public boolean renameOneCategory(String oldShort, String newShort) {
        if (categories != null || categories.contains(oldShort)) {
            categories = StringUtil.replaceOneValue(categories, oldShort, newShort);
            return true;
        }
        return false;
    }
 
    public void createSimpleLabel(String name, String backColor, String foreColor) {
        label = name;
        reportName = name;
        style = new LabelStyle();
        style.label = new LabelOnOff();
        style.label.on = new LabelDisplay(name, foreColor, backColor);
        style.label.off = new LabelDisplay(name, backColor, foreColor);
        style.filter = new LabelOnOff();
        style.filter.on = new LabelDisplay(name, foreColor, backColor);
        style.filter.off = new LabelDisplay(name, backColor, foreColor);
    }

    public static class LabelStyle {
        public LabelOnOff label;
        public LabelOnOff filter;
    }

    public static class LabelOnOff {
        public LabelDisplay on;
        public LabelDisplay off;
    }
    public static class LabelDisplay {

        public LabelDisplay(String name, String foreColor, String backColor) {
            this.displayName = name;
            this.foreground = foreColor;
            this.background = backColor;
        }

        public String foreground;
        public String background;
        public String icon;
        public String displayName;
        public String toolip;
    }
	public String getDisplayName() {
		return reportName;
	}
}

