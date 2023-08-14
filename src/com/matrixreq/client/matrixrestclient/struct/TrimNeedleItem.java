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

import com.matrixreq.client.matrixrestclient.MatrixRestClient;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class TrimNeedleItem {
    public String itemOrFolderRef;   // can be item or folder
    public String title;
    public String project;
    public String lastModDate;
    public String creationDate;
    
    public List<LinkItem> upLinkList;
    public List<LinkItem> downLinkList;
    
    public List <NeedleFieldValue> fieldVal;
    public String labels;  // "(label),(label2)"
    
    public ArrayList<String> getLabels() {
        try {
            return MatrixRestClient.decodeLabelField(labels);
        }
        catch (Exception ex){
            return new ArrayList<>();
        }
    }
}
