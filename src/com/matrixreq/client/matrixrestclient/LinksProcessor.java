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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.matrixreq.client.GenericRestClient;
import com.matrixreq.client.matrixrestclient.struct.ExternalLink;
import com.matrixreq.client.matrixrestclient.struct.ExternalLinks;
import com.matrixreq.client.matrixrestclient.struct.MatrixItem;
import com.matrixreq.client.matrixrestclient.struct.PayloadForWfgw;
import com.matrixreq.client.matrixrestclient.struct.PluginSettings;
import com.matrixreq.client.matrixrestclient.struct.ProjectDetails;
import com.matrixreq.lib.MatrixLibException;
import com.matrixreq.lib.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Yves
 */
public class LinksProcessor {
    String instanceUrl;
    String token;
    
    public LinksProcessor(String instanceUrl, String token, String domainName) {
        this.instanceUrl = MatrixRestClient.fixInstance(instanceUrl.replace("/rest/1", ""), domainName);
        this.token = token;
    }
    
    private ArrayList<Integer> plugins;
    private HashMap<String, ArrayList<ExternalLink>> itemMap;
    
    public void getAllLinks(String project) throws MatrixLibException {
        plugins = new ArrayList<>();
        itemMap = new HashMap<>();
        
        // We ask the normal api for project deta, to have the list of plugins
        String restUrl = instanceUrl;
        if (! restUrl.endsWith("/rest/1"))
            restUrl += "/rest/1";
        MatrixRestClient matrixCli = new MatrixRestClient(restUrl);
        matrixCli.setTokenAuthorization(token);
        ProjectDetails projectDetails = matrixCli.getProjectDetails(project);
        if (projectDetails == null || projectDetails.getPluginSettingsList() == null)
            return;
        for (PluginSettings sett: projectDetails.getPluginSettingsList())
            if (! plugins.contains(sett.pluginId)) {
                switch (sett.pluginId) {
                    case 211:
                    case 212:
                        // We only accept Jira Cloud and Jira Server plugins
                        plugins.add(sett.pluginId);
                        break;
                }
            }
        
        // Now we go through the wfgw 
        // ttps://matrixspecs.matrixreq.com/rest/2/wfgw/?payload=%7B%
        String externalUrl = instanceUrl;
        if (externalUrl.endsWith("/rest/1"))
            externalUrl = externalUrl.replace("/rest/1", "/rest/2");
        else
            externalUrl = externalUrl + "/rest/2";
        GenericRestClient externalApi = new GenericRestClient(externalUrl);
        externalApi.addHeader("Authorization", "Token " + token);        
        for (Integer plugin: plugins) {
            Payload payload = new Payload(plugin, project);
            Gson gson = new Gson();
            String api = "/wfgw/?payload=" + StringUtil.urlEncode(gson.toJson(payload));
            String allLinks = externalApi.restGet(api);
            List<ExternalLinks> list = gson.fromJson(allLinks, new TypeToken<List<ExternalLinks>>(){}.getType());            
            if (list != null && ! list.isEmpty()) 
                addAll(list, plugin);
        }
    }

    private void addAll(List<ExternalLinks> list, int plugin) {
        for (ExternalLinks ext: list) {
            String matrixItem = ext.matrixItem.matrixItem;
            ArrayList<ExternalLink> current = itemMap.get(matrixItem);
            if (current == null)
                current = new ArrayList<>();
            for (ExternalLink oneLink: ext.links)
                if (oneLink.plugin == plugin)
                    current.add(oneLink);
            itemMap.put(matrixItem, current);
        }
    }
    
    @SuppressWarnings("unused")
    private class PayloadProject {
        private String project;
        public PayloadProject(String project) {
            this.project = project;
        }
    }

    @SuppressWarnings("unused")
    private class Payload {
        private Integer pluginId;
        private String action;
        private PayloadProject matrixItem;
        public Payload (Integer pluginId, String project) {
            this.pluginId = pluginId;
            this.matrixItem = new PayloadProject(project);
            this.action = "GetIssues";
        }
    }
    
    public ArrayList<ExternalLink> getLinksForMatrixItem(String matrixItem) {
        return itemMap.get(matrixItem);
    }
    
    public List<String> getLinksForExternalItem(String externalItem) {
        ArrayList<String> ret = new ArrayList<>();
        for (String matrixItem: itemMap.keySet()) {
            boolean keep = false;
            ArrayList<ExternalLink> externals = itemMap.get(matrixItem);
            if (externals != null)
                for (ExternalLink external: externals)
                    if (externalItem.equals(external.externalItemId))
                        keep = true;
            if (keep)
                ret.add(matrixItem);
        }
        Collections.sort(ret);
        return ret;
    }
    
    public void addLink(String project, String matrixRef, int pluginId, String externalId, String externalTitle, String externalUrl) throws Exception {
        ExternalLink ext = new ExternalLink();
        ext.externalItemId = externalId;
        ext.externalItemTitle = externalTitle;
        ext.externalItemUrl = externalUrl;
        ext.plugin = pluginId;
        PayloadForWfgw links = new PayloadForWfgw();
        links.externalItems = new ArrayList<>();
        links.externalItems.add(ext);
        links.matrixItem = new MatrixItem();
        links.matrixItem.project = project;
        links.matrixItem.matrixItem = matrixRef;
        links.action = "CreateLinks";
        links.pluginId = pluginId;
        Gson gson = new Gson();
        String payloadS = gson.toJson(links);
        GenericRestClient externalApi = new GenericRestClient(instanceUrl + "/rest/2/wfgw");
        externalApi.addHeader("Authorization", "Token " + token);        
        ArrayList<NameValuePair> param = new ArrayList<>();
        param = GenericRestClient.addParameter(param, "payload", payloadS);
        CloseableHttpResponse httpResponse = externalApi.restPostExtended("/", param);
        ret = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
        lastStatus = httpResponse.getStatusLine().getStatusCode();
        lastStatusMessage = httpResponse.getStatusLine().getReasonPhrase();
    }

    public String ret;
    public int lastStatus;
    public String lastStatusMessage;
}
