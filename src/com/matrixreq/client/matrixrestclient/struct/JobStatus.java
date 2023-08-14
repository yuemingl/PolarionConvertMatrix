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

import com.matrixreq.matrix.JobFileType;
import java.util.ArrayList;
import java.util.List;
public class JobStatus {

    protected List<JobFileType> jobFile;
    protected String status;
    protected int progress;
    protected String visibleName;

    /**
     * Gets the value of the jobFile property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the jobFile property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJobFile().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JobFileType }
     * 
     * 
     */
    public List<JobFileType> getJobFile() {
        if (jobFile == null) {
            jobFile = new ArrayList<JobFileType>();
        }
        return this.jobFile;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatus(String value) {
        this.status = value;
    }

    /**
     * Gets the value of the progress property.
     * 
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Sets the value of the progress property.
     * 
     */
    public void setProgress(int value) {
        this.progress = value;
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

}
