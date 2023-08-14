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

import java.util.List;

/**
 *
 * @author Yves
 */
public class ListProjectAndSettings extends GetProjectListAck {
    protected List<Setting> settings;
    protected List<Setting> customerSettings;
    protected String currentUser;
    protected String serverVersion;
    protected String baseUrl;
    protected String restUrl;

    public ListProjectAndSettings(GetProjectListAck ack) {
        project = ack.getProject();
    }
    public void SetSettings(List<Setting> l) {
        settings = l;
    }

    public List<Setting> getSettings() {
        return settings;
    }

    public List<Setting> getCustomerSettings() {
        return customerSettings;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getRestUrl() {
        return restUrl;
    }

    public void setRestUrl(String restUrl) {
        this.restUrl = restUrl;
    }
    
}
