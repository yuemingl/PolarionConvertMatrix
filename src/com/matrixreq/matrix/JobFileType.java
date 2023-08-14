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

package com.matrixreq.matrix;

public class JobFileType {

    protected int jobFileId;
    protected String visibleName;
    protected String internalPath;
    protected String mimeType;
    protected String restUrl;

    /**
     * Gets the value of the jobFileId property.
     * 
     */
    public int getJobFileId() {
        return jobFileId;
    }

    /**
     * Sets the value of the jobFileId property.
     * 
     */
    public void setJobFileId(int value) {
        this.jobFileId = value;
    }

    /**
     * Gets the value of the visibleName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVisibleName() {
        return visibleName;
    }

    /**
     * Sets the value of the visibleName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVisibleName(String value) {
        this.visibleName = value;
    }

    /**
     * Gets the value of the internalPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInternalPath() {
        return internalPath;
    }

    /**
     * Sets the value of the internalPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInternalPath(String value) {
        this.internalPath = value;
    }

    /**
     * Gets the value of the mimeType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Sets the value of the mimeType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMimeType(String value) {
        this.mimeType = value;
    }

    /**
      * @param url Set the public rest URL for the file
     */
    public void setRestUrl(String url) {
        restUrl = url;
    }

    /**
     * @return The server url to download this file from
     */
    public String getRestUrl() {
        return restUrl;
    }
}
