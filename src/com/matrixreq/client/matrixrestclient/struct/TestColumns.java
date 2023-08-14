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

import java.util.List;

/**
 *
 * @author Yves
 */
public class TestColumns {
    /* 
        Warning : the JSON object is not directly translatable to Java:
        The field name "UC" below is dynamic
    
    
        "render": {
            "UC": {
              "columns":[
                { "name": "Action", "field": "action", "editor": "text" },
                { "name": "Expected Result", "field": "expected", "editor": "text" }
            ]},    
    */
    public List<TestField> columns;
}
