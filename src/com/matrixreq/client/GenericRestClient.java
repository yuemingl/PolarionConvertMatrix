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

package com.matrixreq.client;

import com.google.gson.Gson;
import com.matrixreq.lib.LoggerConfig;
import com.matrixreq.lib.MatrixLibException;
import com.matrixreq.lib.StringUtil;
import com.matrixreq.lib.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

/**
 * Generic REST client
 * @author Administrator
 */
public class GenericRestClient {
    protected final CloseableHttpClient httpClient;
    protected int lastStatus;
    protected String lastStatusMessage;
    protected String baseUrl;
    protected ArrayList<String []> headers;
    protected String user, pwd;
    protected Gson gson = new Gson();

    private GenericRestClient(String baseUrl, RequestConfig requestConfig) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    private GenericRestClient(String baseUrl, RequestConfig requestConfig, int retry) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler(new CustomHttpRequestRetryHandler(retry, 1))
                .build();
    }

    public GenericRestClient(String baseUrl) {
        this(baseUrl, RequestConfig.custom()
                .setConnectionRequestTimeout(10000)
                .setConnectTimeout(10000)
                .setSocketTimeout(10000).build());
    }

    public GenericRestClient(String baseUrl, int timeoutInSeconds) {
        this(baseUrl, RequestConfig.custom()
                .setConnectionRequestTimeout(1000 * timeoutInSeconds)
                .setConnectTimeout(1000 * timeoutInSeconds)
                .setSocketTimeout(1000 * timeoutInSeconds).build());
    }

    public GenericRestClient(String baseUrl, int timeoutInSeconds, int retry) {
        this(baseUrl, RequestConfig.custom()
                .setConnectionRequestTimeout(1000 * timeoutInSeconds)
                .setConnectTimeout(1000 * timeoutInSeconds)
                .setSocketTimeout(1000 * timeoutInSeconds).build(), retry);
    }
    /**
     * Will be set for all further requests
     * @param user
     * @param pwd
     */
    public final void setAuthorization(String user, String pwd)  {
        String rightPart = user + ":" + pwd;
        String authBase64 = StringUtil.encodeBase64(rightPart);
        String auth = "Basic " + authBase64;
        addHeader("Authorization", auth);
        this.user = user;
        this.pwd = pwd;
    }

    public final void setAuthorizationBearer(String token)  {
        addHeader("Authorization", "Bearer " + token);
        this.user = user;
        this.pwd = pwd;
    }

    protected CookieStore cookieStore = null;

    /**
     * Returns the output of the POST in [0] and the cookie value in [2]
     */
    public String restPostGetCookies(String serviceUrl, ArrayList<NameValuePair> urlParameters) throws MatrixLibException {
        try {
            HttpPost post = new HttpPost(baseUrl + serviceUrl);
            if (urlParameters != null)
                // http://www.mkyong.com/java/apache-httpclient-examples/
                post.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
            if (headers != null)
                for (String[] header: headers)
                    post.setHeader(header[0], header[1]);
            cookieStore = new BasicCookieStore();
            HttpClientContext context = HttpClientContext.create();
            context.setCookieStore(cookieStore);
            CloseableHttpResponse httpResponse = httpClient.execute(post, context);
            String ret = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            lastStatus = httpResponse.getStatusLine().getStatusCode();
            lastStatusMessage = httpResponse.getStatusLine().getReasonPhrase();
            return ret;

        } catch (UnsupportedEncodingException ex) {
            throw new MatrixLibException(ex);
        } catch (IOException ex) {
            throw new MatrixLibException(ex);
        }
    }

    public void addHeader(String name, String value) {
        if (headers == null)
            headers = new ArrayList<>();
        headers.add(new String[]{name,value});
    }

    /**
     * Issue a simple REST GET
     * @param serviceUrl local path info to add to baseUrl
     * @return
     * @throws com.matrixreq.lib.MatrixLibException
     */
    public String restGet (String serviceUrl) throws MatrixLibException {
        HttpGet get = new HttpGet(baseUrl + serviceUrl);
        return restDo(get);
    }

    /**
     * Issue a simple REST GET
     * @param serviceUrl local path info to add to baseUrl
     * @return
     * @throws com.matrixreq.lib.MatrixLibException
     */
    public CloseableHttpResponse restGetExtended (String serviceUrl) throws MatrixLibException {
        HttpGet get = new HttpGet(baseUrl + serviceUrl);
        return restDoExtended(get);
    }

    class HttpSpecialGet extends HttpEntityEnclosingRequestBase {
        public final static String METHOD_NAME = "GET";

        @Override
        public String getMethod() {
            return METHOD_NAME;
        }

        HttpSpecialGet(String url) throws URISyntaxException {
            setURI(new URI(url));
        }
    }

    /**
     * Issue a simple REST GET
     * @param serviceUrl local path info to add to baseUrl
     * @return
     * @throws com.matrixreq.lib.MatrixLibException
     */
    public CloseableHttpResponse restGetExtended (String serviceUrl, ArrayList<NameValuePair> urlParameters, String payload) throws MatrixLibException, UnsupportedEncodingException, URISyntaxException {
        HttpSpecialGet get = new HttpSpecialGet(baseUrl + serviceUrl);
        if (urlParameters != null)
            // http://www.mkyong.com/java/apache-httpclient-examples/
            get.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
        if (payload != null) {
            StringEntity se = new StringEntity(payload, "UTF-8");
            if (payload.startsWith("{"))
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json; charset=utf-8"));
            get.setEntity(se);
        }
        return restDoExtended(get);
    }

    /**
     * Issue a simple REST GET, and put the result in a file
     * @param serviceUrl local path info to add to baseUrl
     * @param fileName
     * @throws com.matrixreq.lib.MatrixLibException
     */
    public void restGetFile (String serviceUrl, String fileName) throws MatrixLibException {
        HttpGet get = new HttpGet(baseUrl + serviceUrl);
        try {
            /*
                if (headers != null)
                    for (String[] header: headers)
                        get.setHeader(header[0], header[1]);
                CloseableHttpResponse httpResponse = httpClient.execute(get);
            */
            CloseableHttpResponse httpResponse = restDoExtended(get);

            // Below is from https://stackoverflow.com/a/52860648
            HttpEntity entity = httpResponse.getEntity();
            try (InputStream inputStream = entity.getContent()) {
                Files.copy(inputStream, Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
            }
            /*
            byte[] content = EntityUtils.toByteArray(httpResponse.getEntity());
            File f = new File(fileName);
            FileUtils.writeByteArrayToFile(f, content);
            */
        } catch (IOException | ParseException ex) {
            throw new MatrixLibException(ex);
        }
    }

    /**
     * Issue a simple REST PUT
     * @param serviceUrl local path info to add to baseUrl
     * @param urlParameters
     * @return
     * @throws com.matrixreq.lib.MatrixLibException
     */
    public String restPut (String serviceUrl, ArrayList<NameValuePair> urlParameters) throws MatrixLibException {
        try {
            HttpPut put = new HttpPut(baseUrl + serviceUrl);
            if (urlParameters != null)
                // http://www.mkyong.com/java/apache-httpclient-examples/
                put.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
            return restDo(put);
        } catch (UnsupportedEncodingException ex) {
            throw new MatrixLibException(ex);
        }
    }


    /**
     * Issue a simple REST PUT
     * @param serviceUrl local path info to add to baseUrl
     * @param urlParameters
     * @return
     * @throws com.matrixreq.lib.MatrixLibException
     */
    public CloseableHttpResponse restPutExtended (String serviceUrl, ArrayList<NameValuePair> urlParameters) throws MatrixLibException {
        try {
            HttpPut put = new HttpPut(baseUrl + serviceUrl);
            if (urlParameters != null)
                // http://www.mkyong.com/java/apache-httpclient-examples/
                put.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
            return restDoExtended(put);
        } catch (UnsupportedEncodingException ex) {
            throw new MatrixLibException(ex);
        }
    }


    /**
     * Issue a simple REST PUT
     * @param serviceUrl local path info to add to baseUrl
     * @param urlParameters
     * @param payload
     * @return
     * @throws com.matrixreq.lib.MatrixLibException
     */
    public CloseableHttpResponse restPutExtended (String serviceUrl, ArrayList<NameValuePair> urlParameters, String payload) throws MatrixLibException {
        try {
            HttpPut put = new HttpPut(baseUrl + serviceUrl);
            if (urlParameters != null)
                // http://www.mkyong.com/java/apache-httpclient-examples/
                put.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
            if (payload != null) {
                StringEntity se = new StringEntity(payload, "UTF-8");
                if (payload.startsWith("{"))
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json; charset=utf-8"));
                put.setEntity(se);
            }
            return restDoExtended(put);
        } catch (UnsupportedEncodingException ex) {
            throw new MatrixLibException(ex);
        }
    }

    public CloseableHttpResponse restPatchExtended (String serviceUrl, ArrayList<NameValuePair> urlParameters, String payload) throws MatrixLibException {
        try {
            HttpPatch patch = new HttpPatch(baseUrl + serviceUrl);
            if (urlParameters != null)
                // http://www.mkyong.com/java/apache-httpclient-examples/
                patch.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
            if (payload != null) {
                StringEntity se = new StringEntity(payload, "UTF-8");
                if (payload.startsWith("{"))
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json; charset=utf-8"));
                patch.setEntity(se);
            }
            return restDoExtended(patch);
        } catch (UnsupportedEncodingException ex) {
            throw new MatrixLibException(ex);
        }
    }


    /**
     * Issue a simple REST POST
     * @param serviceUrl local path info to add to baseUrl
     * @param urlParameters may be null. Use urlParameters.add(new BasicNameValuePair("name", "value")) to fill in this structure
     * @return
     * @throws com.matrixreq.lib.MatrixLibException
     */
    public String restPost (String serviceUrl, ArrayList<NameValuePair> urlParameters) throws MatrixLibException {
        try {
            HttpPost post = new HttpPost(baseUrl + serviceUrl);
            if (urlParameters != null)
                // http://www.mkyong.com/java/apache-httpclient-examples/
                post.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
            return restDo(post);
        } catch (UnsupportedEncodingException ex) {
            throw new MatrixLibException(ex);
        }
    }

    /**
     * Issue a simple REST POST with multiple parts (query params and body)
     * @param serviceUrl local path info to add to baseUrl
     * @param urlParameters may be null. Use urlParameters.add(new BasicNameValuePair("name", "value")) to fill in this structure
     * @param payload the form data to post
     * @return response body
     * @throws com.matrixreq.lib.MatrixLibException
     */
    public String restPost (String serviceUrl, ArrayList<NameValuePair> urlParameters, String payload) throws MatrixLibException {
        if (payload == null) {
            return restPost(serviceUrl, urlParameters);
        }
        String params = "";
        if (urlParameters != null) {
            ArrayList<String> values = new ArrayList<>(urlParameters.size());
            for (NameValuePair p : urlParameters) {
                values.add(p.getName()+"="+p.getValue());
            }
            params = "?" + StringUtil.joinArrayWith(values, "&");
        }
        HttpPost post = new HttpPost(baseUrl + serviceUrl + params);
        StringEntity se = new StringEntity(payload, "UTF-8");
        if (payload.startsWith("{"))
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json; charset=utf-8"));
        post.setEntity(se);

        return restDo(post);
    }

    /**
     * Issue a simple REST POST
     * @param serviceUrl local path info to add to baseUrl
     * @param urlParameters
     * @return
     * @throws com.matrixreq.lib.MatrixLibException
     */
    public CloseableHttpResponse restPostExtended (String serviceUrl, ArrayList<NameValuePair> urlParameters) throws MatrixLibException {
        try {
            HttpPost post = new HttpPost(baseUrl + serviceUrl);
            if (urlParameters != null)
                // http://www.mkyong.com/java/apache-httpclient-examples/
                post.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
            return restDoExtended(post);
        } catch (UnsupportedEncodingException ex) {
            throw new MatrixLibException(ex);
        }
    }

    /**
     * Issue a simple REST POST with a payload
     * @param serviceUrl local path info to add to baseUrl
     * @param urlParameters
     * @param payload
     * @return
     * @throws com.matrixreq.lib.MatrixLibException
     */
    public CloseableHttpResponse restPostExtended (String serviceUrl, ArrayList<NameValuePair> urlParameters, String payload) throws MatrixLibException {
        try {
            HttpPost post = new HttpPost(baseUrl + serviceUrl);

            if (urlParameters != null) {
                ArrayList<NameValuePair> actualParams = new ArrayList<>();
                for (NameValuePair nvp: urlParameters) {
                    if (nvp.getName().equals("jwt")) {
                        // dirty hack: we switch the JWT parameter to a header. See https://developer.atlassian.com/static/connect/docs/latest/concepts/authentication.html
                        LoggerConfig.getLogger().debug("GenericRestClient: switching the JWT parameter to a Authorization header");
                        post.addHeader("Authorization", "JWT " + nvp.getValue());
                    }
                    else {
                        // http://www.mkyong.com/java/apache-httpclient-examples/
                        actualParams.add(nvp);
                    }
                }
                if (! actualParams.isEmpty())
                    post.setEntity(new UrlEncodedFormEntity(actualParams, "UTF-8"));
            }
            if (payload != null) {
                StringEntity se = new StringEntity(payload, "UTF-8");
                if (payload.startsWith("{"))
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json; charset=utf-8"));
                post.setEntity(se);
            }
            return restDoExtended(post);
        } catch (UnsupportedEncodingException ex) {
            throw new MatrixLibException(ex);
        }
    }

    /**
     * Issue a REST POST
     * @param serviceUrl local path info to add to baseUrl
     * @param file
     * @return
     * @throws com.matrixreq.lib.MatrixLibException
     */
    public String restPostFile (String serviceUrl, File file) throws MatrixLibException {
        try {
            WebUtil webUtil = new WebUtil();
            webUtil.setDelay(120);
            String ret = null;
            if (StringUtils.isNotEmpty(user) && StringUtils.isNotEmpty(pwd))
                ret = webUtil.postFileToWebBasicAuth(new URL(baseUrl + serviceUrl), user, pwd, file);
            else {
                // Check if we're using another auth
                boolean gotAuth = false;
                if (headers != null && ! headers.isEmpty()) {
                    for (String [] header: headers) {
                        if (! gotAuth && header[0].equals("Authorization")) {
                            ret = webUtil.postFileToWebSpecialAuth(new URL(baseUrl + serviceUrl), header[1], file);
                            gotAuth = true;
                        }
                    }
                }
                if (! gotAuth)
                    ret = webUtil.postFileToWebBasicAuth(new URL(baseUrl + serviceUrl), null, null, file);
            }

            lastStatus = webUtil.lastStatus;
            lastStatusMessage = webUtil.lastStatusMessage;
            return ret;
        } catch (IOException ex) {
            throw new MatrixLibException(ex);
        }
    }

    /**
     * Issue a simple REST DELETE
     * @param serviceUrl local path info to add to baseUrl
     * @return
     * @throws com.matrixreq.lib.MatrixLibException
     */
    public String restDelete (String serviceUrl) throws MatrixLibException {
        HttpDelete delete = new HttpDelete(baseUrl + serviceUrl);
        return restDo(delete);
    }

    /**
     * Issue a simple REST DELETE
     * @param serviceUrl local path info to add to baseUrl
     * @return
     * @throws com.matrixreq.lib.MatrixLibException
     */
    public CloseableHttpResponse restDeleteExtended (String serviceUrl) throws MatrixLibException {
        HttpDelete delete = new HttpDelete(baseUrl + serviceUrl);
        return restDoExtended(delete);
    }

    class HttpSpecialDelete extends HttpEntityEnclosingRequestBase {
        public final static String METHOD_NAME = "DELETE";

        @Override
        public String getMethod() {
            return METHOD_NAME;
        }

        HttpSpecialDelete(String url) throws URISyntaxException {
            setURI(new URI(url));
        }
    }
    /**
     * Issue a simple REST DELETE
     * @param serviceUrl local path info to add to baseUrl
     * @return
     * @throws com.matrixreq.lib.MatrixLibException
     */
    public CloseableHttpResponse restDeleteExtended (String serviceUrl, ArrayList<NameValuePair> urlParameters, String payload) throws MatrixLibException {
        try {
            HttpSpecialDelete delete = new HttpSpecialDelete(baseUrl + serviceUrl);
            if (urlParameters != null) {
                ArrayList<NameValuePair> actualParams = new ArrayList<>();
                for (NameValuePair nvp: urlParameters) {
                    if (nvp.getName().equals("jwt")) {
                        // dirty hack: we switch the JWT parameter to a header. See https://developer.atlassian.com/static/connect/docs/latest/concepts/authentication.html
                        LoggerConfig.getLogger().debug("GenericRestClient: switching the JWT parameter to a Authorization header");
                        delete.addHeader("Authorization", "JWT " + nvp.getValue());
                    }
                    else {
                        // http://www.mkyong.com/java/apache-httpclient-examples/
                        actualParams.add(nvp);
                    }
                }
                if (! actualParams.isEmpty())
                    delete.setEntity(new UrlEncodedFormEntity(actualParams, "UTF-8"));
            }
            if (payload != null) {
                StringEntity se = new StringEntity(payload, "UTF-8");
                if (payload.startsWith("{"))
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json; charset=utf-8"));
                delete.setEntity(se);
            }
            return restDoExtended(delete);
        } catch (Exception ex) {
            throw new MatrixLibException(ex);
        }
    }


    public String restDo (HttpRequestBase base) throws MatrixLibException {
        try {
            CloseableHttpResponse httpResponse = restDoExtended(base);
            if (httpResponse.getStatusLine().getStatusCode() == 403) {
                LoggerConfig.getLogger().error("403 Forbidden Failed to get result from UR {}", base.getURI().toString());
            }
            String ret = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            lastStatus = httpResponse.getStatusLine().getStatusCode();
            lastStatusMessage = httpResponse.getStatusLine().getReasonPhrase();
            return ret;
        } catch (IOException | ParseException ex) {
            LoggerConfig.getLogger().error("Failed to get result from GET at URL {}", base.getURI().toString(), ex);
            throw new MatrixLibException(ex);
        }

    }

    public CloseableHttpResponse restDoExtended (HttpRequestBase base) throws MatrixLibException {
        try {
            if (headers != null)
                for (String[] header: headers)
                    base.setHeader(header[0], header[1]);
            if (cookieStore != null) {
                HttpClientContext context = HttpClientContext.create();
                context.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
                return httpClient.execute(base, context);
            } else {
                return httpClient.execute(base);
            }
        } catch (IOException | ParseException ex) {
            LoggerConfig.getLogger().error("Error when doing a rest request {} {}", base.getURI(), base.getMethod(), ex);
            throw new MatrixLibException(ex, "error when doing rest request " + base.getURI() + " " + base.getMethod());
        }
    }

    public int getLastStatus() {
        return lastStatus;
    }

    public String getLastStatusMessage() {
        return lastStatusMessage;
    }

    /**
     * Adds a name=value pair to the list of HTTP arguments
     * @param base
     * @param name
     * @param value
     * @return new list with one more pair
     */
    public static ArrayList<NameValuePair> addParameter (ArrayList<NameValuePair> base, String name, String value) {
        base.add(new BasicNameValuePair (name, value));
        return base;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
