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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Arguments decoder. Always call decode first
 */
public class Args {

    protected Map<String,String> args = null;
    private String []ordered;

    public void decode(String[] proggramArgs) {
        ordered = proggramArgs;
        args = new HashMap<>();
        for (String arg: proggramArgs) {
            if (arg.contains("=")) {
                String before = StringUtil.before(arg, "=");
                String after = StringUtil.after(arg, "=");
                args.put(before, after);
            }
            else
                args.put(arg, "");
        }
    }

    /**
     * @param name of the argument, usually starts with -- but doesn't have to
     * @return null if argument is not there. "" if argument is there but doesn't have a = with a value (so it's the same as --arg= )
     */
    public String get(String name) {
        return args.get(name);
    }
    /**
     * @param name of the argument, assuming it would be passed with "--"
     * @return null if argument is not there. "" if argument is there but doesn't have a = with a value (so it's the same as --arg= )
     */
    public String get__(String name) {
        return get ("--" + name);
    }

    public Integer getIntegerOrZero(String name) {
        return getIntegerOrValue(name, 0);
    }

    public Integer getIntegerOrValue(String name, int defaultValue) {
        String value = args.get(name);
        if (StringUtils.isEmpty(value))
            return null;
        return StringUtil.stringToInt(value, defaultValue);
    }

    public String getByOrder(int order) {
        if (order >= ordered.length)
            return null;
        return ordered[order];
    }
}
