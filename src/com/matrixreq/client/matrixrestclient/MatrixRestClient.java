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

package com.matrixreq.client.matrixrestclient;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.matrixreq.client.GenericRestClient;
import com.matrixreq.client.matrixrestclient.struct.*;
import com.matrixreq.lib.DateUtil;
import com.matrixreq.lib.LoggerConfig;
import com.matrixreq.lib.MatrixLibException;
import com.matrixreq.lib.StringUtil;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.matrixreq.matrix.JobFileType;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Yves
 */
public class MatrixRestClient extends GenericRestClient {
    private boolean silent = false;
    
    /**
     * Constructor
     * @param restUrl URL to the rest service (should ends with /1)
     */
    public MatrixRestClient (String restUrl) {
        // set a default timeout to 20s
        super(restUrl, 20);
    }

    /**
     * Constructor
     *
     * @param restUrl URL to the rest service (should ends with /1)
     */
    public MatrixRestClient(String restUrl, int timeoutInSecond) {
        super(restUrl, timeoutInSecond);
    }

    /**
     *
     * @param restUrl endpoint url
     * @param timeoutInSecond timeout http client
     * @param retry number of retry http client
     */
    public MatrixRestClient(String restUrl, int timeoutInSecond, int retry) {
        super(restUrl, timeoutInSecond, retry);
    }
    
    /**
     * Login with a session: the cookieStore variable will be generated with the cookies sent by the server including JSESSIONID
     * We also decode here the cookie list to set a "x-csrf" http header that has the same value as the incoming csrf header
     * @param user
     * @param pwd
     * @return
     * @throws MatrixLibException
     */
    public String loginWithSession(String user, String pwd) throws MatrixLibException {
        // need to do a login
        ArrayList<NameValuePair> params = new ArrayList<>();            
        params = addParameter(params, "password", pwd);
        String login = restPostGetCookies("/user/" + StringUtil.urlEncode(user) + "/login", params);
        List<Cookie> list = cookieStore.getCookies();
        String csrf = null;
        String session = null;
        if (list != null)
            for (Cookie cookie: list) {
                switch (cookie.getName()) {
                    case "csrf":
                        csrf = cookie.getValue();
                        break;
                    case "JSESSIONID":
                        session = cookie.getValue();
                        break;
                }
            }
        if (csrf != null)
            addHeader("x-csrf", csrf);
        return session;
    }

    /**
     * Utility to convert a customer instance "mindmaze" into "https://mindmaze.matrixreq.com". If the parameter already starts with "http" no change is made
     * @param baseUrl
     * @return 
     */
    public static String fixInstance(String baseUrl, String domain) {
        if (! baseUrl.startsWith("http")) {
            baseUrl = "https://" + baseUrl;
            if (! baseUrl.endsWith("." + domain))
                baseUrl += "." + domain;
        }
        return baseUrl;
    }

    public static String fixInstance(String baseUrl) {
        return fixInstance(baseUrl, "matrixreq.com");
    }

    
    public void setSilent (boolean silent) {
        this.silent = silent;
    }
    
    public GetAllCateg getAllCategory(String project) throws MatrixLibException {
        String s = restGet("/" + project + "/cat");
        try {
            GetAllCateg allCateg = gson.fromJson(s, GetAllCateg.class);
            return allCateg;
        } catch (JsonSyntaxException e) {
            System.out.println("Unexpected result when getting categories: " +  s);
            throw new MatrixLibException (e);
        }
    }
    
    public GetAllCateg getAllCategoryAsAdmin(String project) throws MatrixLibException {
        String s = restGet("/" + project + "/cat?adminUI=1");
        try {
            GetAllCateg allCateg = gson.fromJson(s, GetAllCateg.class);
            return allCateg;
        } catch (JsonSyntaxException e) {
            throw new MatrixLibException (e);
        }
    }
    
    /**
     * GET /1/{project}/cat/{category}
     * @param project
     * @param category
     * @return a CategoryAndItems object
     * @throws MatrixLibException 
     */
    public CategoryAndItems getCategory (String project, String category) throws MatrixLibException {
        String s = restGet("/" + project + "/cat/" + category);
        try {
            CategoryAndItems categoryAndItems = gson.fromJson(s, CategoryAndItems.class);
            return categoryAndItems;
        } catch (JsonSyntaxException e) {
            throw new MatrixLibException (e);
        }
    }

    /**
     * GET /1/{project}/item/{item}
     * @param project
     * @param item
     * @return an ItemAndValue object
     * @throws MatrixLibException 
     */
    public ItemAndValue getItem(String project, String item) throws MatrixLibException {
        String s = restGet("/" + project + "/item/" + item);
        try {
            ItemAndValue itemStruct = gson.fromJson(s, ItemAndValue.class);
            return itemStruct;
        } catch (JsonSyntaxException e) {
            throw new MatrixLibException (e);
        }
    }

    /**
     * GET /1/{project}/item/{item}
     * @param project
     * @param item
     * @param date
     * @return an ItemAndValue object
     * @throws MatrixLibException 
     */
    public ItemAndValue getItemAtDate(String project, String item, String date) throws MatrixLibException {
        String action = "/" + project + "/item/" + item + "?atDate=" + StringUtil.urlEncode(date);
        String s = restGet(action);
        try {
            ItemAndValue itemStruct = gson.fromJson(s, ItemAndValue.class);
            return itemStruct;
        } catch (JsonSyntaxException e) {
            throw new MatrixLibException (e);
        }
    }

    /**
     * GET /1/{project}/item/{item}
     * @param project
     * @param item
     * @return an ItemAndValue object
     * @throws MatrixLibException 
     */
    public ItemAndValueAndHistory getItemWithHistory(String project, String item) throws MatrixLibException {
        String s = restGet("/" + project + "/item/" + item + "?history=1");
        try {
            ItemAndValueAndHistory itemStruct = gson.fromJson(s, ItemAndValueAndHistory.class);
            return itemStruct;
        } catch (JsonSyntaxException e) {
            throw new MatrixLibException (e);
        }
    }

    /**
     * GET /1/{project}/item/{item}
     * @param project
     * @param item
     * @return an ItemAndValue object
     * @throws MatrixLibException 
     */
    public ItemAndValueAndChildren getItemAndChildren(String project, String item) throws MatrixLibException {
        String s = restGet("/" + project + "/item/" + item + "?children=yes");
        try {
            ItemAndValueAndChildren itemStruct = gson.fromJson(s, ItemAndValueAndChildren.class);
            return itemStruct;
        } catch (JsonSyntaxException e) {
            throw new MatrixLibException (e);
        }
    }
    
    /**
     * GET /1/{project}/item/{item}
     * @param project
     * @param item
     * @param options
     * @return an ItemAndValue object
     * @throws MatrixLibException 
     */
    public ItemAndValueAndChildren getItemAndChildrenOptions(String project, String item, String options) throws MatrixLibException {
        String action = "/" + project + "/item/" + item + "?children=yes&" + options;
        String s = restGet(action);
        try {
            ItemAndValueAndChildren itemStruct = gson.fromJson(s, ItemAndValueAndChildren.class);
            return itemStruct;
        } catch (JsonSyntaxException e) {
            throw new MatrixLibException (e);
        }
    }
    
    /**
     * Delete (inactivate) an item
     * @param project
     * @param itemRef
     * @param reason
     * @return a json object containing status
     * @throws MatrixLibException 
     */
    public String deleteItem(String project, String itemRef, String reason) throws MatrixLibException {
        String s = restDelete("/" + project + "/item/" + itemRef + "?reason=" + StringUtil.urlEncode(reason));
        return s;
    }

    /**
     * Only works for version >= 2.3
     * @param job
     * @param reason
     * @return
     * @throws MatrixLibException
     */
	public String deleteJob(int job, String reason) throws MatrixLibException {
        String s = restDelete("/all/job/" + job + "?reason=" + StringUtil.urlEncode(reason));
        return s;
	}

    /**
     * Delete (inactivate) a folder, even if empty
     * @param project
     * @param folderRef
     * @param reason
     * @return a json object containing status
     * @throws MatrixLibException 
     */
    public String deleteFolderEvenIfEmpty(String project, String folderRef, String reason) throws MatrixLibException {
        String s = restDelete("/" + project + "/item/" + folderRef + "?confirm=yes&reason=" + StringUtil.urlEncode(reason));
        return s;
    }

    /**
     * Delete (inactivate) an item
     * @param project
     * @param upItemRef
     * @param downItemRef
     * @param reason
     * @return a json object containing status
     * @throws MatrixLibException 
     */
    public String deleteLink(String project, String upItemRef, String downItemRef, String reason) throws MatrixLibException {
        String s = restDelete("/" + project + "/itemlink/" + upItemRef + "/" + downItemRef + "?reason=" + StringUtil.urlEncode(reason));
        return s;
    }

    /**
     * Delete (inactivate) a field
     * @param project
     * @param category
     * @param id field ID
     * @param reason 
     * @return  
     * @throws MatrixLibException  
     */
    public String deleteField(String project, String category, int id, String reason) throws MatrixLibException {
        String s = restDelete("/" + project + "/field/" + category + "?field=" + id + "&reason=" + StringUtil.urlEncode(reason));
        return s;
    }

    /**
     * Perform a needle search with additional parameters
     * @param project
     * @param needle
     * @param parameters - if multiple, put a & between them, don't start with ? or & though
     * @return
     * @throws MatrixLibException 
     */
    public TrimNeedle findWithParameters(String project, String needle, String parameters) throws MatrixLibException {
        String s = restGet("/" + project + "/needle?search=" + StringUtil.urlEncode(needle) + "&" + parameters);
        try {
            return gson.fromJson(s, TrimNeedle.class);
        } catch (JsonSyntaxException e) {
            throw new MatrixLibException (e);
        }
    }

    public TrimNeedle find(String project, String needle) throws MatrixLibException {
        String s = restGet("/" + project + "/needle?search=" + StringUtil.urlEncode(needle));
        try {
            TrimNeedle trim = gson.fromJson(s, TrimNeedle.class);
            return trim;
        } catch (JsonSyntaxException e) {
            throw new MatrixLibException (e);
        }
    }

    public String[] findMini(String project, String needle) throws MatrixLibException {
        String s = restGet("/" + project + "/needleminimal?search=" + StringUtil.urlEncode(needle));
        try {
            String[] trim = gson.fromJson(s, String[].class);
            return trim;
        } catch (JsonSyntaxException e) {
            throw new MatrixLibException (e);
        }
    }

    public String[] getTimeZone() throws MatrixLibException {
        String s = restGet("/all/timezone");
        try {
            return gson.fromJson(s, String[].class);
        } catch (JsonSyntaxException e) {
            throw new MatrixLibException (e);
        }
    }

    public UserList getAllUsers() throws MatrixLibException {
        String s = restGet("/user");
        try {
            return gson.fromJson(s, UserList.class);
        } catch (JsonSyntaxException e) {
            throw new MatrixLibException (e);
        }
    }

    public UserListWithDetails getAllUsersWithDetails() throws MatrixLibException {
        String s = restGet("/user?details=1");
        try {
            return gson.fromJson(s, UserListWithDetails.class);
        } catch (JsonSyntaxException e) {
            throw new MatrixLibException (e);
        }
    }

    public GroupList getAllGroups() throws MatrixLibException {
        String s = restGet("/group");
        try {
            return gson.fromJson(s, GroupList.class);
        } catch (JsonSyntaxException e) {
            throw new MatrixLibException (e);
        }
    }

    public GroupListDetails getAllGroupsWithDetails() throws MatrixLibException {
        String s = restGet("/group?details=1");
        try {
            return gson.fromJson(s, GroupListDetails.class);
        } catch (JsonSyntaxException e) {
            throw new MatrixLibException (e);
        }
    }

    public void deleteGroup(String groupName, String reason) throws MatrixLibException {
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("reason", reason));
        urlParameters.add(new BasicNameValuePair("confirm", "yes"));
        restDeleteExtended("/group/" + StringUtil.urlEncode(groupName), urlParameters, null);
    }
    
    public int addGroup(String groupName, String reason) throws MatrixLibException {
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("reason", reason));
        String s = restPost("/group/" + StringUtil.urlEncode(groupName), urlParameters);
        return StringUtil.stringToIntZero(s);
    }
    
    public String addGroupToProject(String groupName, String projectShort, int permission) throws MatrixLibException {
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("permission", "" + permission));
        String s = restPost("/group/" + StringUtil.urlEncode(groupName) + "/project/" + StringUtil.urlEncode(projectShort), urlParameters);
        return s; 
    }
    
    public String renameGroup(String groupName, String newGroupName) throws MatrixLibException {
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("newName", newGroupName));
        String s = restPut("/group/" + StringUtil.urlEncode(groupName) + "/rename", urlParameters);
        return s; 
    }
    
    /**
     * renameUser was implemented in Matrix 2.2
     * @param userName
     * @param newUserName
     * @return
     * @throws MatrixLibException 
     */
    public String renameUser(String userName, String newUserName) throws MatrixLibException {
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("newLogin", newUserName));
        String s = restPut("/user/" + StringUtil.urlEncode(userName) + "/rename", urlParameters);
        return s; 
    }
    
    public void addUserToGroup(String groupName, String user, String reason) throws MatrixLibException {
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("reason", reason));
        restPut("/group/" + StringUtil.urlEncode(groupName) + "/user/" + StringUtil.urlEncode(user), urlParameters);
    }
    
    public ItemList getItemList(String project, String doc) throws MatrixLibException {
        String s = restGet("/" + project + "/itemlist/" + doc);
        try {
            return gson.fromJson(s, ItemList.class);
        } catch (JsonSyntaxException e) {
            throw new MatrixLibException (e);
        }
    }

    /**
     * Upload a project to the instance
     * @param f 
     * @param projectLabel may be null
     * @param projectShortLabel may be null
     * @param overwrite set to true to overwrite project if it exists
     * @return the job ID  
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public int uploadProject(File f, String projectLabel, String projectShortLabel, boolean overwrite) throws MatrixLibException {
        try {
            String url = "/";
            ArrayList<String> param = new ArrayList<>();
            if (StringUtils.isNotEmpty(projectLabel))
                param.add("label=" + URLEncoder.encode(projectLabel, "UTF-8"));
            if (StringUtils.isNotEmpty(projectShortLabel))
                param.add("shortLabel=" + URLEncoder.encode(projectShortLabel, "UTF-8"));
            if (overwrite)
                param.add("overwrite=yes");
            if (param.size() > 0) {
                url += "?" + param.get(0);
                for (int p = 1; p < param.size(); p++)
                    url += "&" + param.get(p);
            }
            String ret = restPostFile(url, f);
            this.lastStatusMessage = ret;
            Job job = gson.fromJson(ret, Job.class);
            return job.jobId;
        } catch (Exception ex) {
            LoggerConfig.getLogger().error("Failed to upload project {} projectLabel", projectLabel, ex);
            throw new MatrixLibException (ex);
        }
    }

    /**
     * Upload a file to the instance
     * @param f 
     * @param project 
     * @return the file description, including the key
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public FileAndKey uploadFile(File f, String project) throws MatrixLibException {
        try {
            String url = "/" + project + "/file";
            if (StringUtils.isEmpty(project))
                throw new MatrixLibException("Missing argument: project");
            if (! f.exists() || ! f.isFile())
                throw new MatrixLibException("Bad argument: not a file");
            String ret = restPostFile(url, f);
            FileAndKey fileAndKey = gson.fromJson(ret, FileAndKey.class);
            return fileAndKey;
        } catch (MatrixLibException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new MatrixLibException (ex);
        }
    }

    /**
     * Upload a file to the instance
     * @param f 
     * @param project 
     * @param reason 
     * @return the file description, including the key
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public ConvertWordAck convertWordFile(File f, String project, String reason) throws MatrixLibException {
        try {
            String url = "/" + project + "/wordconvert?reason=" + StringUtil.urlEncode(reason);
            if (StringUtils.isEmpty(project))
                throw new MatrixLibException("Missing argument: project");
            if (! f.exists() || ! f.isFile())
                throw new MatrixLibException("Bad argument: not a file");
            String ret = restPostFile(url, f);
            ConvertWordAck ack = gson.fromJson(ret, ConvertWordAck.class);
            return ack;
        } catch (MatrixLibException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new MatrixLibException (ex);
        }
    }
    
    
    /**
     * Import items from an XML file
     * @param f 
     * @param project 
     * @param reason 
     * @return the job ID  
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public int importItems(File f, String project, String reason) throws MatrixLibException {
        String url = "/" + project + "/import?reason=" + StringUtil.urlEncode(reason);
        String ret = restPostFile(url, f);
        Job job = gson.fromJson(ret, Job.class);
        return job.jobId;
    }

    public int addNewUser (String userName, String password, String email) throws MatrixLibException {
        try {
            String serviceUrl = "/user?login=" + URLEncoder.encode(userName, "UTF-8")
                    + "&password=" + URLEncoder.encode(password, "UTF-8")
                    + "&email=" + URLEncoder.encode(email, "UTF-8");
            String ret = restPost (serviceUrl, null);
            // LoggerConfig.getLogger().info("Result of addNewUser: {}", ret);
            UserAck userAck = gson.fromJson(ret, UserAck.class);
            if (userAck.userId <= 0)
                throw new MatrixLibException("Unable to add user");
            return userAck.userId;
        } catch (UnsupportedEncodingException ex) {
            // LoggerConfig.getLogger().error("Error in addNewUser", ex);
            throw new MatrixLibException (ex);
        }
    }

    /**
     * Variant of the above, with first name and last name and using the (1.3+) json argument instead of discrete arguments
     * @param userName
     * @param password
     * @param email
     * @param firstName
     * @param lastName
     * @return
     * @throws MatrixLibException 
     */
    public int addNewUser (String userName, String password, String email, String firstName, String lastName) throws MatrixLibException {
        try {
            CreateUser createUser = new CreateUser();
            createUser.setLogin(userName);
            createUser.setAdmin(0);
            createUser.setEmail(email);
            createUser.setFirstName(firstName);
            createUser.setLastName(lastName);
            if (StringUtils.isNotEmpty(password)) {
                createUser.setPassword(password);
                createUser.setPasswordIsEncrypted(0);
            }
            String json = gson.toJson(createUser);
            String serviceUrl = "/user?json=" + URLEncoder.encode(json, "UTF-8");
            String ret = restPost (serviceUrl, null);
            LoggerConfig.getLogger().info("Result of addNewUser: {}", ret);
            UserAck userAck = gson.fromJson(ret, UserAck.class);
            if (userAck.userId <= 0)
                throw new MatrixLibException("Unable to add user");
            return userAck.userId;
        } catch (UnsupportedEncodingException ex) {
            LoggerConfig.getLogger().error("Error in addNewUser", ex);
            throw new MatrixLibException (ex);
        }
    }



    /**
     * Variant of the above, with first name and last name and NOT using the (1.3+) json argument instead of discrete arguments
     * @param userName
     * @param password
     * @param email
     * @param firstName
     * @param lastName
     * @return
     * @throws MatrixLibException 
     */
    public int addNewUser_1_2 (String userName, String password, String email, String firstName, String lastName) throws MatrixLibException {
        try {
            String serviceUrl = "/user?login=" + URLEncoder.encode(userName, "UTF-8")
                    + "&password=" + URLEncoder.encode(password, "UTF-8")
                    + "&first=" + URLEncoder.encode(firstName, "UTF-8")
                    + "&last=" + URLEncoder.encode(lastName, "UTF-8")
                    + "&email=" + URLEncoder.encode(email, "UTF-8");
            String ret = restPost (serviceUrl, null);
            LoggerConfig.getLogger().info("Result of addNewUser: {}", ret);
            UserAck userAck = gson.fromJson(ret, UserAck.class);
            if (userAck.userId <= 0)
                throw new MatrixLibException("Unable to add user");
            return userAck.userId;
        } catch (UnsupportedEncodingException ex) {
            LoggerConfig.getLogger().error("Error in addNewUser", ex);
            throw new MatrixLibException (ex);
        }
    }
    
    /**
     * @param userName
     * @param projectShort
     * @param level 3 for admin, 2 for read-write, 1 for read-only, 0 to kill access
     * @return
     * @throws MatrixLibException 
     */
    public String addUserProject(String userName, String projectShort, int level) throws MatrixLibException {
        try {
            String serviceUrl = "/user/" + URLEncoder.encode(userName, "UTF-8")
                    + "/" + URLEncoder.encode(projectShort, "UTF-8")
                    + "?permission=" + level;
            return restPost (serviceUrl, null);
        } catch (UnsupportedEncodingException ex) {
            throw new MatrixLibException (ex);
        }
    }

    /**
     * Updates a user's password
     * @param userName
     * @param pwd
     * @return
     * @throws MatrixLibException 
     */
    public String updateUserPassword (String userName, String pwd) throws MatrixLibException {
        try {
            String serviceUrl = "/user/" + URLEncoder.encode(userName, "UTF-8")
                    + "?password=" + URLEncoder.encode(pwd, "UTF-8");
            return restPut (serviceUrl, null);
        } catch (UnsupportedEncodingException ex) {
            throw new MatrixLibException (ex);
        }
    }

    /**
     * Updates a user's record (we could also add the signatures stuffs)
     * @param userName - of the user to change
     * @param pwd
     * @param email
     * @param firstName
     * @param lastName
     * @param admin
     * @return "{Ok}"
     * @throws MatrixLibException 
     */
    public String updateUserDetails (String userName, String pwd, String email, String firstName, String lastName, boolean admin) throws MatrixLibException {
        try {
            UserEdit ue = new UserEdit(null, userName, email, pwd, firstName, lastName, admin ? 1 : 0);
            String jsonString = gson.toJson(ue);
            String serviceUrl = "/user/" + URLEncoder.encode(userName, "UTF-8")
                    + "?json=" + URLEncoder.encode(jsonString, "UTF-8");
            return restPut (serviceUrl, null);
        } catch (UnsupportedEncodingException ex) {
            throw new MatrixLibException (ex);
        }
    }
    
    /**
     * @param project
     * @param label
     * @param cat
     * @param fieldType
     * @param fieldParam may be null
     * @param reason
     * @return
     * @throws MatrixLibException 
     */
    public FieldId addField(String project, String label, String cat, String fieldType, String fieldParam, String reason) throws MatrixLibException {
        String action = "/" + project + "/cat" 
                + "?label=" + StringUtil.urlEncode(label)
                + "&category=" + StringUtil.urlEncode(cat)
                + "&fieldType=" + StringUtil.urlEncode(fieldType)
                + "&reason=" + StringUtil.urlEncode(reason);
        if (StringUtils.isNotEmpty(fieldParam))
            action += "&fieldParam=" + StringUtil.urlEncode(fieldParam);
        String s = restPost(action, null);
        FieldId ret = gson.fromJson(s, FieldId.class);
        if (ret.fieldId == 0)
            throw new MatrixLibException("Error adding field: " + s);
        return ret;
    }

    /**
     * Adds a folder
     * @param project
     * @param parent
     * @param label
     * @param reason
     * @param fieldValList may be null
     * @return 
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public ItemAndSerial addFolder(String project, String parent, String label, String reason, FieldAndValueList fieldValList) throws MatrixLibException {
        String action = "/" + project + "/folder";
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("label", label));
        urlParameters.add(new BasicNameValuePair("parent", parent));
        urlParameters.add(new BasicNameValuePair("reason", reason));
        if (fieldValList != null && fieldValList.fieldVal != null)
            for (FieldAndValue fv: fieldValList.fieldVal)
        	urlParameters.add(new BasicNameValuePair("fx" + fv.id, fv.value));
        String s = restPost(action, urlParameters);
        ItemAndSerial ret = gson.fromJson(s, ItemAndSerial.class);
        if (ret.itemId == 0)
            throw new MatrixLibException("Error adding item: " + s);
        return ret;
    }

    /**
     * Adds an item
     * @param project
     * @param parent
     * @param title
     * @param reason
     * @param fieldValList may be null
     * @return 
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public ItemAndSerial addItem(String project, String parent, String title, String reason, FieldAndValueList fieldValList) throws MatrixLibException {
        return addItem (project, parent, title, reason, fieldValList, null);
    }

    /**
     * Adds an item
     * @param project
     * @param parent
     * @param title
     * @param reason
     * @param fieldValList may be null
     * @param author
     * @return 
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public ItemAndSerial addItem(String project, String parent, String title, String reason, FieldAndValueList fieldValList, String author) throws MatrixLibException {
        return addItem(project, parent, title, reason, fieldValList, author, null);
    }

    /**
     * Adds an item
     * @param project
     * @param parent
     * @param title
     * @param reason
     * @param fieldValList may be null
     * @param author
     * @param labels
     * @return 
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public ItemAndSerial addItem(String project, String parent, String title, String reason, FieldAndValueList fieldValList, String author, ArrayList <String> labels) throws MatrixLibException {
        String action = "/" + project + "/item";
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("title", title));
        urlParameters.add(new BasicNameValuePair("folder", parent));
        urlParameters.add(new BasicNameValuePair("reason", reason));
        if (author != null)
            urlParameters.add(new BasicNameValuePair("author", author));
        if (fieldValList != null && fieldValList.fieldVal != null)
            for (FieldAndValue fv: fieldValList.fieldVal)
        	urlParameters.add(new BasicNameValuePair("fx" + fv.id, fv.value));
        if (labels != null) {
            String combine = StringUtil.joinArrayWith(labels, ",");
            urlParameters.add(new BasicNameValuePair("labels", combine));
        }
        String s = restPost(action, urlParameters);
        ItemAndSerial ret = gson.fromJson(s, ItemAndSerial.class);
        if (ret.itemId == 0)
            throw new MatrixLibException("Error adding item: " + s);
        return ret;
    }
    
    
    /**
     * Adds an item
     * @param project
     * @param item
     * @param title
     * @param reason
     * @param fieldValList may be null
     * @param labels - only use this if the server version is >= 1.7. Can be null
     * @return 
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public String updateItem(String project, String item, String title, String reason, FieldAndValueList fieldValList,
            List<String> labels) throws MatrixLibException {
        String action = "/" + project + "/item/" + item;
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("title", title));
        urlParameters.add(new BasicNameValuePair("reason", reason));
        if (fieldValList != null && fieldValList.fieldVal != null)
            for (FieldAndValue fv: fieldValList.fieldVal)
        	urlParameters.add(new BasicNameValuePair("fx" + fv.id, fv.value));
        if (labels != null) {
            if (getServerVersion().startsWith("1.6"))
                throw new MatrixLibException("You cannot set labels this way for versions before 1.7. They were set in fields");
            urlParameters.add(new BasicNameValuePair("labels", StringUtil.joinArrayWith(labels, ",")));
        }
        String s = restPut(action, urlParameters);
        return s;
    }

    
    /**
     * Updates an item, changing only the fields passed
     * @param project
     * @param item
     * @param title
     * @param reason
     * @param fieldValList may be null
     * @return 
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public String updateItemOnlyThose(String project, String item, String title, String reason, FieldAndValueList fieldValList) throws MatrixLibException {
        String action = "/" + project + "/item/" + item;
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("onlyThoseFields", "1"));
        urlParameters.add(new BasicNameValuePair("onlyThoseLabels", "1"));
        urlParameters.add(new BasicNameValuePair("title", title));
        urlParameters.add(new BasicNameValuePair("reason", reason));
        if (fieldValList != null && fieldValList.fieldVal != null)
            for (FieldAndValue fv: fieldValList.fieldVal)
        	    urlParameters.add(new BasicNameValuePair("fx" + fv.id, fv.value));
        String s = restPut(action, urlParameters);
        return s;
    }

    
    /**
     * This only works for 1.7 and higher servers
     * @param project
     * @param itemRef
     * @param label 
     * @param reason 
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public void unsetLabelInItem(String project, String itemRef, String label, String reason) throws MatrixLibException {
        if (getServerVersion().startsWith("1.6"))
            throw new MatrixLibException("You cannot (un)set labels this way for versions before 1.7. They were set in fields");
        ItemAndValue item = getItem(project, itemRef);
        // Retrieve the labels and remove the one we don't want anymore
        ArrayList<String> oldLabels = item.labels;
        ArrayList<String> newLabels = computeNewLabels(oldLabels, label);
        updateItem(project, itemRef, item.title, reason, item.fieldValList, newLabels);
    }

    /**
     * This only works for 1.7 and higher servers. Nothing is done (and no exception fired) if the item already include the label
     * @param project
     * @param itemRef
     * @param label 
     * @param reason 
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public void setLabelInItem(String project, String itemRef, String label, String reason) throws MatrixLibException {
        if (getServerVersion().startsWith("1.6"))
            throw new MatrixLibException("You cannot set labels this way for versions before 1.7. They were set in fields");
        ItemAndValue item = getItem(project, itemRef);
        // Retrieve the labels and remove the one we don't want anymore
        ArrayList<String> labels = item.labels;
        if (labels.contains(label))
            return;
        labels.add(label);
        updateItem(project, itemRef, item.title, reason, item.fieldValList, labels);
    }
    
    private ArrayList<String> computeNewLabels (ArrayList<String> oldLabels, String label) {
        ArrayList<String> newLabels = new ArrayList<>();
        if (oldLabels != null)
            for (String s: oldLabels)
                if (! s.equals(label))
                    newLabels.add(s);
        return newLabels;
    }
    
    
    /**
     * Adds an item
     * @param project
     * @param upItem
     * @param Item
     * @param reason
     * @return 
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public String addLink(String project, String upItem, String downItem, String reason) throws MatrixLibException {
        String action = "/" + project + "/itemlink/" + upItem + "/" + downItem;
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("reason", reason));
        String s = restPost(action, urlParameters);
        return s;
    }

    
    /**
     * Get all project and category settings
     * @param project
     * @return
     * @throws MatrixLibException 
     */
    public AllSetting getAllSetting (String project) throws MatrixLibException {
        String action = "/" + project + "/setting";
        String s = restGet(action);
        AllSetting ret = gson.fromJson(s, AllSetting.class);
        return ret;
    }

    /**
     * Get one project setting
     * @param project
     * @param setting
     * @return
     * @throws MatrixLibException 
     */
    public String getSetting(String project, String setting) throws MatrixLibException {
        AllSetting allSetting = getAllSetting (project);
        if (allSetting == null || allSetting.settingList == null)
            return null;
        for (Setting set: allSetting.settingList) 
            if (setting.equals(set.key))
                return set.value;
        return null;
    }

    /**
     * Retrieve the labels setting of a project into a decoded JSON object
     * @param project
     * @return
     * @throws MatrixLibException 
     */
    public LabelsSetting getLabelsSetting(String project) throws MatrixLibException {
        String labelSetting = getSetting(project, "labels");
        if (StringUtils.isEmpty(labelSetting))
            throw new MatrixLibException("Cant find label settings for this project");
        LabelsSetting labelsSetting = gson.fromJson(labelSetting, LabelsSetting.class);
        if (labelsSetting == null)
            throw new MatrixLibException("Unable to decode label settings for this project");
        return labelsSetting;
    }
    
    /**
     * Sets or replaces a setting in a project
     * @param user
     * @param setting
     * @param value
     * @return REST API return
     * @throws MatrixLibException 
     */
    public String setUserSetting(String user, String setting, String value) throws MatrixLibException {
        String action = "/user/" + user + "/setting";
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("key", setting));
        urlParameters.add(new BasicNameValuePair("value", value));
        String s = restPost(action, urlParameters);
        return s;
    }

    /**
     * Sets or replaces a customer setting 
     * @param setting
     * @param value
     * @return REST API return
     * @throws MatrixLibException 
     */
    public String setCustomerSetting(String setting, String value) throws MatrixLibException {
        return setProjectSetting("all", setting, value);
    }

    /**
     * Sets or replaces a setting in a project
     * @param project
     * @param setting
     * @param value
     * @return REST API return
     * @throws MatrixLibException 
     */
    public String setProjectSetting(String project, String setting, String value) throws MatrixLibException {
        String action = "/" + project + "/setting";
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("key", setting));
        urlParameters.add(new BasicNameValuePair("value", value));
        String s = restPost(action, urlParameters);
        return s;
    }

    /**
     * Sets or replaces a setting in a category
     * @param project
     * @param category
     * @param setting
     * @param value
     * @return REST API return
     * @throws MatrixLibException 
     */
    public String setCategorySetting(String project, String category, String setting, String value) throws MatrixLibException {
        String action = "/" + project + "/cat/" + category + "/setting";
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("key", setting));
        urlParameters.add(new BasicNameValuePair("value", value));
        String s = restPost(action, urlParameters);
        return s;
    }
    
    /**
     * Deletes a category
     * @param project
     * @param category
     * @param reason
     * @return 
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public String deleteCategory(String project, String category, String reason) throws MatrixLibException {
        String s = restDelete("/" + project + "/cat/" + category + "?reason=" + StringUtil.urlEncode(reason));
        return s;
        
    }

    /**
     * @return all projects (short labels) in an instance
     * @throws MatrixLibException 
     */
    public ArrayList<String> getAllProjects() throws MatrixLibException {
        String action = "/?silent=1";
        String s = restGet(action);
        ListProjectAndSettings list = gson.fromJson(s, ListProjectAndSettings.class);
        ArrayList<String> ret = new ArrayList<>();
        if (list != null && list.getProject() != null)
            for (ProjectType proj: list.getProject())
                ret.add(proj.getShortLabel());
        return ret;
    }

    /**
     * @return all projects (short labels) in an instance
     * @throws MatrixLibException 
     */
    public ListProjectAndSettings getAllProjectsAndSettings() throws MatrixLibException {
        String action = "/?silent=1&adminUI=1";
        String s = restGet(action);
        ListProjectAndSettings list = gson.fromJson(s, ListProjectAndSettings.class);
        if (list.getServerVersion() == null)
            list.setServerVersion("1.6.999.999");
        return list;
    }
    /**
     * @return all projects  in an instance and the serverVersion
     * @throws MatrixLibException 
     */
    public ListProjectAndSettings  getProjectAndServerVersion() throws MatrixLibException {
        String action = "/?silent=1&adminUI=1&output=project,serverVersion";
        String s = restGet(action);
        ListProjectAndSettings list = gson.fromJson(s, ListProjectAndSettings.class);
        if (list.getServerVersion() == null)
            list.setServerVersion("1.6.999.999");
        return list;
    }

    /**
     * Only available since 1.8.437
     * @return server status
     * @throws MatrixLibException 
     */
    public ServerStatus getServerStatus()  throws MatrixLibException {
        String action = "/all/status?silent=1";
        String s = restGet(action);
        ServerStatus ret = gson.fromJson(s, ServerStatus.class);
        return ret;
    }
    
    private String serverVersion = null;
    
    /**
     * @return server's version, or 1.6.999.999 if the server has not implemented the right methods yet
     * @throws MatrixLibException 
     */
    public String getServerVersion () throws MatrixLibException {
        if (serverVersion == null) {
            ListProjectAndSettings l = getAllProjectsAndSettings();
            serverVersion = l.getServerVersion();
        }
        return serverVersion;
    }
    
    /**
     * Get the list of users for a project
     * @param project
     * @return a User structure
     * @throws com.matrixreq.lib.MatrixLibException
     */
    public ProjectDetails getProjectDetails (String project) throws MatrixLibException {
        /*
{
userPermission: [
{
id: 1,
login: "MatrixReader",
email: "yves@matrixreq.com",
permission: 1
},
{
id: 28,
login: "matrixreq.wolfgang.huber",
email: "info@matrixreq.com",
permission: 2
},
{
id: 281,
login: "matrixreq.yves.berquin",
email: "info@matrixreq.com",
permission: 2
},
{
id: 3,
login: "testRunner",
email: "?",
permission: 3
}
]
}        
        */
        String action = "/" + project;
        String s = restGet(action);
        ProjectDetails details = gson.fromJson(s, ProjectDetails.class);
        return details;
    }

    public String deleteProject(String project) throws MatrixLibException {
        String action = "/" + project + "?confirm=yes";
        String s = restDelete(action);
        return s;
    }

    public String deleteUser(String user) throws MatrixLibException {
        String action = "/user/" + user + "?confirm=yes";
        String s = restDelete(action);
        return s;
    }

    /**
     * Adds a category to a project
     * @param project 
     * @param shortDesc short description, capital letters
     * @param longDesc long description, plural
     * @param reason
     * @return 
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public CategoryAdd addCategory(String project, String shortDesc, String longDesc, String reason) throws MatrixLibException {
        String action = "/" + project 
                + "?label=" + StringUtil.urlEncode(longDesc)
                + "&shortLabel=" + StringUtil.urlEncode(shortDesc)
                + "&reason=" + StringUtil.urlEncode(reason);
        String s = restPost(action, null);
        CategoryAdd ret = gson.fromJson(s, CategoryAdd.class);
        if (ret.getCategoryId() == 0)
            throw new MatrixLibException("Error adding category: " + s);
        return ret;
    }

    /**
     * Sends a message to the log 
     * @param message
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public void sendLog(String message) throws MatrixLibException {
        String action = "/all/log";
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("message", message));
        restPost(action, urlParameters);
    }
    
    /**
     * Gets a date, formatted for this user
     * @param date may be null or empty
     * @return
     * @throws MatrixLibException 
     */
    public GetDateAck getDate(String date) throws MatrixLibException {
        String action = "/all/date";
        if (StringUtils.isNotEmpty(date)) {
            action += "?date=" + date;
            if (silent)
                action += "&silent=1";
        }
        else
            if (silent)
                action += "?silent=1";
        String s = restGet(action);
        GetDateAck gd = gson.fromJson(s, GetDateAck.class);
        return gd;
    }
    
    /**
     * Get job status https://clouds.matrixreq.com/rest/1/PROT/job/5852
     * @param project
     * @param jobId
     * @return
     * @throws MatrixLibException 
     */
    public JobStatus getJobStatus (String project, int jobId) throws MatrixLibException {
        String action = "/";
        if (StringUtils.isEmpty(project))
            action += "all";
        else
            action += project;
        action += "/job/" + jobId;
        String s = restGet(action);
        JobStatus ret = gson.fromJson(s, JobStatus.class);
        return ret;
    }

    /**
     * Launches a job
     * @param projectName
     * @param reportRef
     * @param options may be empty. 
     * @return 
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public JobId launchReport(String projectName, String reportRef, String options) throws MatrixLibException {
        String serviceUrl = "/" + projectName + "/report/" + reportRef;
        if (StringUtils.isNotEmpty(options))
            serviceUrl += "?" + options;
        String s = restPost (serviceUrl, null);
        JobId ret = gson.fromJson(s, JobId.class);
        return ret;
    }

    /**
     * Launches a job for a SIGN- item
     * @param projectName
     * @param signRef
     * @param options may be empty. 
     * @return 
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public JobId launchSignedReport(String projectName, String signRef, String format) throws MatrixLibException {
        String serviceUrl = "/" + projectName + "/signedreport/" + signRef;
        ArrayList<NameValuePair> params = new ArrayList<>();
        if (StringUtils.isNotEmpty(format))
            params = GenericRestClient.addParameter(params, "format", format);
        params = GenericRestClient.addParameter(params, "url", this.baseUrl.replace("/rest/1", ""));
        params = GenericRestClient.addParameter(params, "resturl", this.baseUrl);
        String s = restPost (serviceUrl, params);
        JobId ret = gson.fromJson(s, JobId.class);
        return ret;
    }


    /**
     * Retrieves a report file
     * @param projectName
     * @param reportJob
     * @param reportFileId 
     * @param options optional list of options like "format=html&itemList=X"
     * @return  
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public String getReportFile(String projectName, int reportJob, int reportFileId, String options) throws MatrixLibException {
        String action = "/" + projectName + "/job/" + reportJob + "/" + reportFileId;
        if (StringUtils.isNotEmpty(options))
            action += "?" + options;
        String s = restGet(action);
        return s;
    }

    /**
     * Retrieves a report file
     * @param localFileName
     * @param projectName
     * @param reportJob
     * @param reportFileId 
     * @param options optional list of options like "format=html&itemList=X"
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public void getReportFileAsFile(String localFileName, String projectName, int reportJob, int reportFileId, String options) throws MatrixLibException {
        String action = "/" + projectName + "/job/" + reportJob + "/" + reportFileId;
        if (StringUtils.isNotEmpty(options))
            action += "?" + options;
        restGetFile(action, localFileName);
    }
    
    /**
     * Retrieves a report file
     * @param localFileName
     * @param projectName
     * @param simple
     * @param excludeCategories something like DOC,SIGN -- may be null
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public void getSchemaFileAsFile(String localFileName, String projectName, boolean simple, String excludeCategories) throws MatrixLibException {
        String action = "/" + projectName + "/schema?simple=" + (simple ? 1 : 0);
        if (excludeCategories != null)
            action += "?excludeCategories=" + excludeCategories;
        restGetFile(action, localFileName);
    }
    
    /**
     * https://clouds.matrixreq.com/rest/1/TEST/tree?fancy&filter=comment_good
     * @param projectName
     * @param filters optional, may be null. otherwise list filters
     * @return 
     * @throws com.matrixreq.lib.MatrixLibException
     */
    public ArrayList<FancyLeaf> getFancyTree(String projectName, ArrayList<String> filters) throws MatrixLibException {
        String param = "";
        if (filters != null)
            param = "&filter=" + StringUtil.joinArrayWith(filters, ",");
        String action = "/" + projectName + "/tree?fancy" + param;
        String s = restGet(action);
        ArrayList<FancyLeaf> fancy = gson.fromJson(s, new TypeToken<ArrayList<FancyLeaf>>(){}.getType());
        return fancy;
    }

    /**
     * https://clouds.matrixreq.com/rest/1/TEST/tree?fancy&filter=comment_good
     * @param projectName
     * @param filters optional, may be null. otherwise list filters
     * @return 
     * @throws com.matrixreq.lib.MatrixLibException
     */
    public ArrayList<FancyLeaf> getFancyTreeAtDate(String projectName, ArrayList<String> filters, Date at) throws MatrixLibException {
        String param = "";
        if (filters != null)
            param = "&filter=" + StringUtil.joinArrayWith(filters, ",");
        if (at != null)
            param += "&atDate=" + DateUtil.formatDateUtcIso8601(at);
        String action = "/" + projectName + "/tree?fancy" + param;
        String s = restGet(action);
        ArrayList<FancyLeaf> fancy = gson.fromJson(s, new TypeToken<ArrayList<FancyLeaf>>(){}.getType());
        return fancy;
    }
    
    /**
     * Launches an export of some items
     * @param project
     * @param itemList
     * @return 
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public JobId exportItems(String project, String itemList) throws MatrixLibException {
        String serviceUrl = "/" + project + "/export?itemList=" + itemList;
        String s = restGet (serviceUrl);
        JobId ret = gson.fromJson(s, JobId.class);
        return ret;
    }

    /**
     * Download the given file from the job
     * @param project The project the job was run in
     * @param jobId The id of the job
     * @param fileId The id of the file to get
     * @param fileName The local filename to store the job file in
     * @throws MatrixLibException Error occured downloading the file
     */
    public void getJobFile(String project, int jobId, int fileId, String fileName) throws MatrixLibException {
        String serviceUrl = "/" + project + "/job/" + jobId + "/" + fileId;
        restGetFile(serviceUrl, fileName);
    }

    /**
     * Download the last file from the job
     * @param job The job to get the file for
     * @param fileName The local filename to store the job file in
     * @throws MatrixLibException Error occured downloading the file
     */
    public void getLastJobFile(String project, int jobId, JobStatus job, String fileName) throws MatrixLibException {
        List<JobFileType> jobFiles = job.getJobFile();
        if (jobFiles.size() > 0) {
            JobFileType file = jobFiles.get(jobFiles.size() - 1);
            String serviceUrl = "/" + project + "/job/" + jobId + "/" + file.getJobFileId();
            restGetFile(serviceUrl, fileName);
        } else {
            throw new MatrixLibException("Job has no files");
        }
    }

    /**
     * Sets or replaces a setting in a project
     * https://matrixspecs.matrixreq.com/VALID/TSPEC-60
     * @param pluginId
     * @param project - may be null : if null, applies to the plugin as a whole
     * @param setting
     * @param value
     * @param encrypted
     * @return REST API return
     * @throws MatrixLibException 
     */
    public String setPluginSetting(int pluginId, String project, String setting, String value, boolean encrypted) throws MatrixLibException {
        String action = "/wlt/setting?plugin=" + pluginId
                 + "&name=" + setting + "&value=" + StringUtil.urlEncode(value) + "&encrypted=" + (encrypted ? "1" : "0");
        if (StringUtils.isNotEmpty(project))
            action += "&project=" + project;
        String s = restPut(action, null);
        return s;
    }

    public JobsWithUrl getAllJobs() throws MatrixLibException {
        return getAllJobs("all");
    }
    public JobsWithUrl getAllJobs(String project) throws MatrixLibException {
        String action = "/" + project + "/job/";
        String s = restGet(action);
        return gson.fromJson(s, JobsWithUrl.class);
    }
    
    public UserDetails getUserDetails (String user) throws MatrixLibException {
        String action = "/user/" + user + "/details";
        String s = restGet(action);
        return gson.fromJson(s, UserDetails.class);
    }
    
    public UserDetailsAdvanced getUserDetailsAdvanced (String user) throws MatrixLibException {
        String action = "/user/" + user;
        String s = restGet(action);
        return gson.fromJson(s, UserDetailsAdvanced.class);
    }
    
    public TrimAuditList getUserAudit(String user, Integer startAt, Integer maxResults) throws MatrixLibException {
        try {
            String action = "/user/" + user + "/audit";
            ArrayList<NameValuePair> urlParameters = new ArrayList<>();
            if (startAt != null)
                urlParameters.add(new BasicNameValuePair("startAt", "" + startAt));
            if (maxResults != null)
                urlParameters.add(new BasicNameValuePair("maxResults", "" + maxResults));
            CloseableHttpResponse httpResponse = restGetExtended(action, urlParameters, null);
            String ret = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            if (httpResponse.getStatusLine().getStatusCode() != 200)
                throw new MatrixLibException("Error " + httpResponse.getStatusLine().getStatusCode());
            return gson.fromJson(ret, TrimAuditList.class);
        } catch (Exception ex) {
            throw new MatrixLibException(ex);
        }
    }

    public TrimAuditList getProjectAudit(String project, Integer startAt, Integer maxResults) throws MatrixLibException {
        try {
            String action = "/" + project + "/audit";
            ArrayList<NameValuePair> urlParameters = new ArrayList<>();
            if (startAt != null)
                urlParameters.add(new BasicNameValuePair("startAt", "" + startAt));
            if (maxResults != null)
                urlParameters.add(new BasicNameValuePair("maxResults", "" + maxResults));
            CloseableHttpResponse httpResponse = restGetExtended(action, urlParameters, null);
            String ret = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            if (httpResponse.getStatusLine().getStatusCode() != 200)
                throw new MatrixLibException("Error " + httpResponse.getStatusLine().getStatusCode());
            return gson.fromJson(ret, TrimAuditList.class);
        } catch (Exception ex) {
            throw new MatrixLibException(ex);
        }
    }
    
    public TrimAuditList getProjectAuditWithTech(String project, Integer startAt, Integer maxResults) throws MatrixLibException {
        try {
            String action = "/" + project + "/audit";
            ArrayList<NameValuePair> urlParameters = new ArrayList<>();
            if (startAt != null)
                urlParameters.add(new BasicNameValuePair("startAt", "" + startAt));
            if (maxResults != null)
                urlParameters.add(new BasicNameValuePair("maxResults", "" + maxResults));
            urlParameters.add(new BasicNameValuePair("tech", "yes"));
            CloseableHttpResponse httpResponse = restGetExtended(action, urlParameters, null);
            String ret = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            if (httpResponse.getStatusLine().getStatusCode() != 200)
                throw new MatrixLibException("Error " + httpResponse.getStatusLine().getStatusCode());
            return gson.fromJson(ret, TrimAuditList.class);
        } catch (Exception ex) {
            throw new MatrixLibException(ex);
        }
    }

    public String [] getHtmlDiff (String [] html, String spanElement, String cssAdd, String cssDelete, String cssFormat) throws MatrixLibException {
        try {
            String action = "/all/compareHtml";
            HtmlCompareRequest req = new HtmlCompareRequest();
            req.versions = html;
            req.spanElement = spanElement;
            req.cssClassAdded = cssAdd;
            req.cssClassDeleted = cssDelete;
            req.cssClassFormatChange = cssFormat;

            ArrayList<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("arg", gson.toJson(req)));
            String ret = restPost (action, urlParameters);
            HtmlCompareResponse response = gson.fromJson(ret, HtmlCompareResponse.class);
            return response.html;
        } catch (Exception ex) {
            throw new MatrixLibException(ex);
        }
    }
    
    public ProjectFileList getAllFiles(String project) throws MatrixLibException {
        try {
            String action = "/" + project + "/file";
            String ret = restGet (action);
            ProjectFileList response = gson.fromJson(ret, ProjectFileList.class);
            return response;
        } catch (Exception ex) {
            throw new MatrixLibException(ex);
        }
    }
    
    /**
     * Moves an item to a new folder / new pos
     * @param project
     * @param itemRef
     * @param newFolderRef
     * @param newPos ! Starts at 1 ! can be null
     * @param reason
     * @return
     * @throws MatrixLibException 
     */
    public String moveItemToFolder(String project, String itemRef, String newFolderRef, Integer newPos, String reason) throws MatrixLibException {
        String action = "/" + project + "/item/" + itemRef + "?";
        if (newPos != null)
            action += "newPosition=" + newPos + "&";
        if (newFolderRef != null)
            action += "&newFolder=" + newFolderRef + "&";
        action += "reason=" + StringUtil.urlEncode(reason);
        String s = restPut(action, null);
        return s;
    }

    /**
     * @param project
     * @param receivingFolder
     * @param itemList
     * @param reason
     * @return
     * @throws MatrixLibException
     */    
    public String moveIn(String project, String receivingFolder, String itemList, String reason) throws MatrixLibException {
        String action = "/" + project + "/movein/" + receivingFolder;
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters = addParameter(urlParameters, "items", itemList);
        if (StringUtils.isNotEmpty(reason))
            urlParameters = addParameter(urlParameters, "reason", reason);
        String s = restPost(action, urlParameters);
        System.out.println(s);
        return s;
    }
    
    /**
     * Moves an item to a new folder / new pos
     * @param project
     * @param fieldId
     * @param newPos ! Starts at 1 !
     * @param reason
     * @return
     * @throws MatrixLibException 
     */
    public String moveField(String project, int fieldId, int newPos, String reason) throws MatrixLibException {
        String action = "/" + project + "/field"
                 + "?field=" + fieldId + "&order=" + newPos + "&reason=" + StringUtil.urlEncode(reason);
        String s = restPut(action, null);
        return s;
    }

    
    /**
     * @param user
     * @param purpose: should be "oauth" or "password_reset"
     * @param reason: optional : free string so that the user remembers what it is for
     * @param validityHours: optional: defaults to infinite if null
     * @param tokenValue: optional: actual value of the token; Leave that to null to ask the API to generate it for you
     * @return
     * @throws MatrixLibException 
     */    
    public TokenAck addToken(String user, String purpose, String reason, Integer validityHours, String tokenValue) throws MatrixLibException {
        String action = "/user/" + StringUtil.urlEncode(user) + "/token";
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters = addParameter(urlParameters, "purpose", purpose);
        if (StringUtils.isNotEmpty(reason))
            urlParameters = addParameter(urlParameters, "reason", reason);
        if (StringUtils.isNotEmpty(tokenValue))
            urlParameters = addParameter(urlParameters, "value", tokenValue);
        if (validityHours != null)
            urlParameters = addParameter(urlParameters, "validity", "" + validityHours);
        String s = restPost(action, urlParameters);
        System.out.println(s);
        return gson.fromJson(s, TokenAck.class);
    }

    /**
     * Set this client in a token authorization mode - do not mix that up with the user/pwd
     * @param token 
     */    
    public void setTokenAuthorization(String token) {
        String auth = "Token " + token;
        addHeader("Authorization", auth);
    }

    /**
     * @param user
     * @param token
     * @return 
     * @throws com.matrixreq.lib.MatrixLibException
     */    
    public String deleteToken(String user, String token) throws MatrixLibException {
        String action = "/user/" + user + "/token?value=" + StringUtil.urlEncode(token);
        return restDelete(action);
    }

    /**
     * @param newPassword
     * @param token
     * @return 
     * @throws com.matrixreq.lib.MatrixLibException
     */    
    public User resetPassword(String token, String newPassword) throws MatrixLibException {
        String action = "/user/xxx/reset_password";
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters = addParameter(urlParameters, "token", token);
        urlParameters = addParameter(urlParameters, "new_password", newPassword);
        String res = restPost(action, urlParameters);
        return gson.fromJson(res, User.class);
    }
    
    
    public int cloneProject (String project, String newShort, String newLong, boolean keepContent) throws MatrixLibException {
        try {
            String serviceUrl = "/" + project + "/clone?label=" + URLEncoder.encode(newLong, "UTF-8") 
                    + "&shortLabel=" + URLEncoder.encode(newShort, "UTF-8")
                    + "&keepContent=" + (keepContent ? "1" : "0");
            String ret = restPost (serviceUrl, null);
            Job job = gson.fromJson(ret, Job.class);
            return job.jobId;
        } catch (UnsupportedEncodingException ex) {
            LoggerConfig.getLogger().error("Error in addNewUser", ex);
            throw new MatrixLibException (ex);
        }
    }

    public String renameCategory (String project, String oldCategoryShort, String newCategoryShort, String newCategoryLong, String reason) throws MatrixLibException {
        String action = "/" + project + "/cat/" + oldCategoryShort 
                 + "?shortLabel=" + newCategoryShort + "&label=" + StringUtil.urlEncode(newCategoryLong) + "&reason=" + StringUtil.urlEncode(reason);
        String s = restPut(action, null);
        return s;
    }

    /**
     * Rename an existing fields
     * @param project Project of the field
     * @param fieldID ID of the field
     * @param newName New name for the field
     * @param reason Explanatory text
     * @return Response from the server
     * @throws MatrixLibException Error renaming
     */
    public String renameField (String project, int fieldID, String newName, String reason) throws MatrixLibException {
        String action = "/" + project + "/field"
                + "?field=" + fieldID + "&label=" + StringUtil.urlEncode(newName) + "&reason=" + StringUtil.urlEncode(reason);
        String s = restPut(action, null);
        return s;
    }

    public static final String START_LABEL = "(";
    public static final String END_LABEL = ")";
    public static final String SEPARATOR_LABEL = ",";
    
    /**
     * Converts a list of labels from the REST API in the form "(a),(b)" to a list a,b
     * @param field
     * @return list of labels (empty list if none)
     * @throws java.lang.Exception 
     */
    public static ArrayList<String> decodeLabelField (String field) throws Exception {
        ArrayList<String> labels = new ArrayList<>();
        if (! StringUtils.isEmpty(field)) {
            String[] split = field.split(SEPARATOR_LABEL);
            for (String one: split) {
                if (one.startsWith(START_LABEL) && one.endsWith(END_LABEL)) {
                    String actualLabel = one.substring(START_LABEL.length(), one.length() - END_LABEL.length());
                    if (StringUtils.isNotEmpty(actualLabel))
                        // We don't add the label if the DB contains "()"
                        labels.add(actualLabel);
                }
                else
                    throw new Exception("Error decoding field for labels: " + field);
            }
        }
        return labels;
    }

    public String setJobProgress(String project, int jobId, int progress, String text, String pathToFile) throws MatrixLibException {
        try {
            String url = "/" + project + "/job/" + jobId + "?progress=" + progress;
            if (StringUtils.isNotEmpty(text))
                url += "&status=" + URLEncoder.encode(text, "UTF-8");
            String ret;
            if (pathToFile == null) 
                ret = restPost(url, null);
            else
                ret = restPostFile(url, new File(pathToFile));
            return ret;
        } catch (UnsupportedEncodingException ex) {
            throw new MatrixLibException (ex);
        }
    }

    public String [] getDeletedProjects() throws MatrixLibException {
        String[] deletedProjects = new String[]{};
        String deletedProjectsSetting = getSetting("all", "deleted_projects");
        if (StringUtils.isNotEmpty(deletedProjectsSetting)) {
            DeletedProjectsSetting deletedProject = gson.fromJson(deletedProjectsSetting, DeletedProjectsSetting.class);
            deletedProjects = deletedProject.deleted;
        }
        return deletedProjects;
    }

    public LicenseStatus getLicenseStatus() throws MatrixLibException {
        String action = "/all/license";
        String s = restGet(action);
        LicenseStatus ret = gson.fromJson(s, LicenseStatus.class);
        return ret;        
    }

    /**
     * Restores an item to a previous version
     * @param project
     * @param itemRef
     * @param version : the version to restore
     * @param reason
     * @return a json object containing status
     * @throws MatrixLibException 
     */
    public String restoreItem(String project, String itemRef, String reason) throws MatrixLibException {
        String action = "/" + project + "/item/" + itemRef;
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("reason", reason));
        String s = restPost(action, urlParameters);
        return s;
    }

    /**
     * Restores an item to a previous version
     * @param project
     * @param itemRef
     * @param version : the version to restore
     * @param reason
     * @return a json object containing status
     * @throws MatrixLibException 
     */
    public String restoreItem(String project, String itemRef, int version, String reason) throws MatrixLibException {
        String action = "/" + project + "/item/" + itemRef;
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("at", "" + version));
        urlParameters.add(new BasicNameValuePair("reason", reason));
        String s = restPost(action, urlParameters);
        return s;
    }

    class DeletedProjectsSetting {
        String[] deleted;
    }
    
    class Job {
        int jobId;
    }
    
    class UserAck {
        int userId;
    }

}
