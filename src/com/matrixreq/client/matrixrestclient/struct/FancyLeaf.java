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


/**
 * Utility class to convert the tree structure returned by the SOAP server to the FancyTree structure expected by the GUI
 * This particular class represents an item
 * @author Yves
 */
public class FancyLeaf {
    public String id;
    public String title;
    public String type;
    public Integer isUnselected;
    public String version;
    public ArrayList<FancyLeaf> children;
}
