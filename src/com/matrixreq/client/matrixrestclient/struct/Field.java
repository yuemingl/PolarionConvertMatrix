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

/**
 *
 * @author Yves
 */
public class Field {
    public int id;
    public String label;
    public String fieldType;
    public String fieldParam;   // fieldParam is returned when asking one category
    public String parameter;    // parameter is returned when asking all categories in a project (!)

    public boolean isSameAs (Field f2) {
        return fieldType.equals(f2.fieldType) && label.equals(f2.label);
    }
}
