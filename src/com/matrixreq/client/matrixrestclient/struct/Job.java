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

import java.util.Date;

public class Job {

    public int jobId;
    public int progress;
    public String status;
    public Date jobBirth;
    public Date jobLastWrite;
    public String project;

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getJobBirth() {
        return jobBirth;
    }

    public void setJobBirth(Date jobBirth) {
        this.jobBirth = jobBirth;
    }

    public Date getJobLastWrite() {
        return jobLastWrite;
    }

    public void setJobLastWrite(Date jobLastWrite) {
        this.jobLastWrite = jobLastWrite;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }
    
    
}
