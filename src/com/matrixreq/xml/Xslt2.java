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

import com.matrixreq.lib.ExecUtil;
import com.matrixreq.lib.FileUtil;
import java.util.ArrayList;

/**
 *
 * @author Yves
 */
public class Xslt2 {

    // ---------------- XLST 2.0 ----------------------
    
    private static String customXslt2Path = null;
    private final static String XSLT2_TOOL_LINUX = "/home/matrix/bin/xslt/xslt.sh";
    private final static String XSLT2_TOOL_WINDOWS = "c:\\home\\matrix\\bin\\xslt\\xslt.cmd";
    private final static String XSLT2_TOOL_MAC = "/Users/oops/bin/xslt/xslt.sh";

    /**
     * @return the custom path if caller did set it through setXsltPath, one of the default static above otherwise
     */
    private static String getXslt2Path () {
        if (customXslt2Path != null)
            return customXslt2Path;
        if (FileUtil.runOnWindows())
            return XSLT2_TOOL_WINDOWS;
        if (FileUtil.runOnMac())
            return XSLT2_TOOL_MAC;
        return XSLT2_TOOL_LINUX;
    }
    
    /**
     * Call this method to specify where to find our custom xslt jar program
     * @param path
     */
    public static void setXslt2Path (String path) {
        customXslt2Path = path;
    }

    // This method applies the xslFilename to inFilename and writes
    // the output to outFilename.
    // From: http://www.exampledepot.8waytrips.com/egs/javax.xml.transform/BasicXsl.html
    public static void xsl2(String inFilename, String outFilename, String xslFilename) throws XmlException {
        try {
            // Since we want to have XSLT 2.0 capability we have to use SaxonHE9
            // But if we link all our programs to SAXON it is giving us a lot of headache.
            // The deciding factor was when we discovered that a program linked with saxon9he.jar doesn't write CDATA on output anymore
            
            // I decide then to create a command line tool to convert xslts
            
            String command = getXslt2Path();
            ArrayList<String> res = ExecUtil.exec(new String[]{command, inFilename, xslFilename, outFilename});
            for (String s: res)
                if (s.startsWith("E|")) {
                    String error = "XSLT error";
                    for (String s2: res)
                        if (s2.startsWith("E|"))
                            error += "|" + s2.substring(2);
                    throw new XmlException(error);
                }
        } catch (Exception ex) {
            throw new XmlException(ex);
        }
    }

    
}
