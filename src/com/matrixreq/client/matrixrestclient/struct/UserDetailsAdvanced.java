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

/**
 *
 * @author Yves
 */
public class UserDetailsAdvanced {
    public int id;  
    public String login;
    public String email; 
    public String firstName; 
    public String lastName; 
    public String signatureImage;  
    public int customeradmin;  // 0 or 1
    public int passwordAgeInDays;
    public int badLogins;  
    public int badLoginsBefore;
    public int superadmin;  // 0 or 1
    public String userStatus;  // normal, deleted, ...

    public ArrayList<Token> tokenList;
}
