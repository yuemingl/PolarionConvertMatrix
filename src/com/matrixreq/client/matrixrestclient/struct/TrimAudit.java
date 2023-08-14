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
import java.util.List;

/**
 *
 * @author Yves
 */
public class TrimAudit {
    protected String userLogin;
    protected String dateTime;
    protected String dateTimeUserFormat;
    protected String action;
    protected String entity;
    protected String reason;
    protected String projectLabel;
    
    protected String reportRef;
    protected String reportTitle;
    protected Integer reportJobId;
    
    protected TrimNeedleItem itemBefore;
    protected TrimNeedleItem itemAfter;
    protected TrimNeedleItem itemUp;
    protected TrimNeedleItem itemDown;
    
    protected int auditId;
    
    protected ArrayList <TechAuditType> techAudit;
    
    protected List <Tag> tags;

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getDateTimeUserFormat() {
        return dateTimeUserFormat;
    }

    public void setDateTimeUserFormat(String dateTimeUserFormat) {
        this.dateTimeUserFormat = dateTimeUserFormat;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getProjectLabel() {
        return projectLabel;
    }

    public void setProjectLabel(String projectLabel) {
        this.projectLabel = projectLabel;
    }

    public String getReportRef() {
        return reportRef;
    }

    public void setReportRef(String reportRef) {
        this.reportRef = reportRef;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }

    public Integer getReportJobId() {
        return reportJobId;
    }

    public void setReportJobId(Integer reportJobId) {
        this.reportJobId = reportJobId;
    }

    public TrimNeedleItem getItemBefore() {
        return itemBefore;
    }

    public void setItemBefore(TrimNeedleItem itemBefore) {
        this.itemBefore = itemBefore;
    }

    public TrimNeedleItem getItemAfter() {
        return itemAfter;
    }

    public void setItemAfter(TrimNeedleItem itemAfter) {
        this.itemAfter = itemAfter;
    }

    public TrimNeedleItem getItemUp() {
        return itemUp;
    }

    public void setItemUp(TrimNeedleItem itemUp) {
        this.itemUp = itemUp;
    }

    public TrimNeedleItem getItemDown() {
        return itemDown;
    }

    public void setItemDown(TrimNeedleItem itemDown) {
        this.itemDown = itemDown;
    }

    public int getAuditId() {
        return auditId;
    }

    public void setAuditId(int auditId) {
        this.auditId = auditId;
    }

    public ArrayList<TechAuditType> getTechAudit() {
        return techAudit;
    }

    public void setTechAudit(ArrayList<TechAuditType> techAudit) {
        this.techAudit = techAudit;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
    
    
}
