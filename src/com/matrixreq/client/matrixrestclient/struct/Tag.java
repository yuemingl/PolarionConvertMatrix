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

import java.sql.Timestamp;

/**
 *
 * @author Yves
 */
public class Tag {
    public int id;
    public int auditId;
    public Timestamp auditTime;
    public String label;
    public String comments;
    public String tagType;
    public Timestamp tagCreation;
    public String userLogin;
    public Integer baseProjectId;
    public String baseProjectName;
    public String baseProjectTag;
    public Timestamp baseAuditCreation;
    public Integer baseAuditId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAuditId() {
        return auditId;
    }

    public void setAuditId(int auditId) {
        this.auditId = auditId;
    }

    public Timestamp getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(Timestamp auditTime) {
        this.auditTime = auditTime;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getTagType() {
        return tagType;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    public Timestamp getTagCreation() {
        return tagCreation;
    }

    public void setTagCreation(Timestamp tagCreation) {
        this.tagCreation = tagCreation;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public Integer getBaseProjectId() {
        return baseProjectId;
    }

    public void setBaseProjectId(Integer baseProjectId) {
        this.baseProjectId = baseProjectId;
    }

    public String getBaseProjectName() {
        return baseProjectName;
    }

    public void setBaseProjectName(String baseProjectName) {
        this.baseProjectName = baseProjectName;
    }

    public String getBaseProjectTag() {
        return baseProjectTag;
    }

    public void setBaseProjectTag(String baseProjectTag) {
        this.baseProjectTag = baseProjectTag;
    }

    public Timestamp getBaseAuditCreation() {
        return baseAuditCreation;
    }

    public void setBaseAuditCreation(Timestamp baseAuditCreation) {
        this.baseAuditCreation = baseAuditCreation;
    }

    public Integer getBaseAuditId() {
        return baseAuditId;
    }

    public void setBaseAuditId(Integer baseAuditId) {
        this.baseAuditId = baseAuditId;
    }

    
}
