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
 * @author Yves
 */
public class TraceRule {
    public String message;
    public String name;
    public String rule;
    public ArrayList<String> any_of;

    public boolean renameOneCategory(String oldShort, String newShort) {
        boolean modified = false;
        if (any_of != null && any_of.contains(oldShort)) {
            any_of = StringUtil.replaceOneValue(any_of, oldShort, newShort);
            modified = true;
        }
        // Below this is more of a guess, but if these messages and names contain the cat short it's probably a good idea to also change them
        if (message.contains(oldShort)) {
            message = message.replace(oldShort, newShort);
            modified = true;
        }
        if (name.contains(oldShort)) {
            name = name.replace(oldShort, newShort);
            modified = true;
        }
        return modified;
    }
}
