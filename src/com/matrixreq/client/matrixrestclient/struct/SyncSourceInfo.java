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
 * See https://matrixreq.atlassian.net/wiki/display/MATRIX/Agile+Rocks
 * @author Yves
 */
public class SyncSourceInfo {
    /// visible url, displayed to the user
    protected String url;
    /// tech url, on which we check the uniqueness
    protected String techUrl;
    
    protected String date;
    protected String author;
    protected int version;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getTechUrl() {
        return techUrl;
    }

    public void setTechUrl(String techUrl) {
        this.techUrl = techUrl;
    }
    
    
}
