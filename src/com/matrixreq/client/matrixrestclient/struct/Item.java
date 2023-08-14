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
 *
 * @author Yves
 */
public class Item {
    public String itemRef;
    public String title;
    public ArrayList <Item> itemList;
    public FieldAndValueList fieldValList;
    public int isFolder;
    public int partial;
    
    public ArrayList<String> getAllItems (boolean includeItems, boolean includeFolders) {
        ArrayList<String> ret = new ArrayList<String>();
        if (isFolder == 0) {
            if (includeItems) 
                ret.add(itemRef);
        }
        else {
            if (includeFolders) 
                ret.add(itemRef);
            if (itemList != null) {
                for (Item item: itemList) {
                    ArrayList<String> one = item.getAllItems (includeItems, includeFolders);
                    ret.addAll(one);
                }
            }
        }
        return ret;
    }
    
}
