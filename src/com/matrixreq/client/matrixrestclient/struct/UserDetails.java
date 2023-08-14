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

import com.matrixreq.lib.DateUtil;
import com.matrixreq.lib.MatrixLibException;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Yves
 */
public class UserDetails extends User {
    public String signatureImage;  
    public Integer nbReadWriteProjectsNow;
    public Integer admin;  // 0 or 1  
    public Integer passwordAgeInDays;
    public Integer badLogins;  
    public Integer badLoginsBefore;
    public List<ProjectAccess> projects;
    public List<InfoUpdate> infoUpdates;
    
    public UserDetails(Integer id, String login, String email) {
        super(id,login,email);
    }
    
    public String getOlderReadWriteAccess() {
        Date oldest = new Date();
        if (projects != null) {
            for (ProjectAccess project: projects) {
                if (project.accesses != null && ! project.accesses.isEmpty()) {
                    String lastDateS = project.accesses.get(project.accesses.size() - 1).startDate8601;
                    try {
                        Date lastDate = DateUtil.parseDateIso8601z(lastDateS);
                        if (lastDate.before(oldest))
                            oldest = lastDate;
                    } catch (MatrixLibException ex) {
                        // ignore
                    }
                }
            }
        }
        return DateUtil.formatDateUtcIso8601z(oldest);
    }
}
