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

package com.matrixreq.polarionxmlconvert;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.matrixreq.client.matrixrestclient.MatrixRestClient;
import com.matrixreq.client.matrixrestclient.struct.FileAndKey;
import com.matrixreq.lib.FileUtil;
import com.matrixreq.lib.MatrixLibException;
import com.matrixreq.lib.StringUtil;

public class Images {

    private String picFolder;
    private String docInput;
    private String itemTracker;

    private String urlToFileName(String url) {
        return StringUtil.urlEncode(url.replace("https://polarion.smdpd.com/polarion/",""));
    }

    private String fileNameToUrl(String fileName) {
        return "https://polarion.smdpd.com/polarion/" + StringUtil.urlDecode(fileName);
    }

    public void setPicFolder(String picFolder) {
        this.picFolder = picFolder;
    }

    public void setDocInput(String docInput) {
        this.docInput = docInput;
    }

    public void setItemTracker(String itemTracker) {
        this.itemTracker = itemTracker;
    }    

    List<String> urls = new ArrayList<>();
    public void reloadAllImages() {
        ArrayList<String> listFiles = FileUtil.listFiles(picFolder, null);
        for (String s: listFiles) 
            urls.add(fileNameToUrl(s));
    }

    List<String> workItems = new ArrayList<>();
    public void reloadAllWorkItems() throws IOException {
        ArrayList<String> lines = FileUtil.readUtf8FileArray(itemTracker);
        for (String l: lines) {
            String workItem = StringUtil.before(l, "|");
            if (! workItems.contains(workItem))
                workItems.add(workItem);
        }
    }
    public List<String> getUrls() {
        return urls;
    }

    public List<String> getWorkItems() {
        return workItems;
    }

    public String convertImages(MatrixRestClient cli, String project, String htmlDescription) throws MatrixLibException {
        for (String url: getUrls())
        if (htmlDescription.contains(url)) {
            System.out.println("Picture found: " + url);
            String imageFile = picFolder + "/" + urlToFileName(url);
            FileAndKey upload = cli.uploadFile(new File(imageFile), project);
            String newUrl = cli.getBaseUrl() + "/" + project + "/file/" + upload.fileId + "?key=" + upload.key;
            htmlDescription = htmlDescription.replace(url, newUrl);
            System.out.println("\t--> " + newUrl);
        }        
        return htmlDescription;
    }
    
    
}
