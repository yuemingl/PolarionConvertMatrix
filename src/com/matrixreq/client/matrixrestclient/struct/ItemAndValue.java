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
public class ItemAndValue {
    public String itemRef;
    public String title;
    public String folderRef;
    public int isFolder;
    public int disabled;
    public int maxVersion;
    public int isUnselected;
    public int itemId;
    public String modDate;
    public String modDateUserFormat;

    public FieldAndValueList fieldValList;
    public ArrayList<String> availableFormats;
    // only from 1.7 on
    public ArrayList<String> labels;
    
    public ArrayList<ItemLink> downLinkList;
    public ArrayList<ItemLink> upLinkList;

    public ArrayList<CatAndRoot> selectSubTree;
}
