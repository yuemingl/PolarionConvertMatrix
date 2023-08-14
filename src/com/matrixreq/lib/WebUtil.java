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

package com.matrixreq.lib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * This class allows downloading files from http and https resources on the web
 * WARNING: It does bypass all the SSL security, accepting all certificates
 * @author Yves
 */
public class WebUtil {
    private boolean customDelay = false;
    private int nbSecDelay;
    
    @SuppressWarnings("unused") 
    private static final WebUtil staticInstance = new WebUtil(1);

    /**
     * Normal constructor
     */
    public WebUtil () {
        customDelay = false;        
    }
    
    /**
     * Private constructor, called by a static instance, to call just once the trust manager functions
     * @param i 
     */
    private WebUtil (int i) {
        try {
            /*
             *  fix for
             *    Exception in thread "main" javax.net.ssl.SSLHandshakeException:
             *       sun.security.validator.ValidatorException:
             *           PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException:
             *               unable to find valid certification path to requested target
             * FROM: http://www.rgagnon.com/javadetails/java-fix-certificate-problem-in-HTTPS.html
             * I implemented this is a private constructor of a static instance, so that it's called before all other things
             */
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {  }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {  }
                }
            };
            
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }

            };
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (KeyManagementException | NoSuchAlgorithmException ex) {
            Logger.getLogger(WebUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Read a web page into a UTF8 string
     * @param url
     * @return
     * @throws MatrixLibException 
     */
    public static String getTextFromWeb (URL url) throws MatrixLibException {
        try {
            /*
            THis is one way to deal with certificates, but it means doing this for every domain
            The other way is the fix in the static private constructor above
            if (url.getProtocol().equals("https")) {
                // http://stackoverflow.com/questions/8378235/javax-net-ssl-sslexception-java-security-invalidalgorithmparameterexception-th
                System.setProperty("javax.net.ssl.trustStore", "/home/clouds/conf/secure.ts");
                System.setProperty("javax.net.ssl.trustStorePassword", "matrix");                
            }*/
            StringWriter writer = new StringWriter();
            InputStream webStream = url.openStream();
            IOUtils.copy(webStream, writer, "UTF-8");
            String res = writer.toString();
            return res;
        } catch (IOException ex) {
            throw new MatrixLibException (ex.getMessage());
        }
    }

    /**
     * Download a file from the web. Works with http:// and https://
     * No conversion is made, the file is written as it is downloaded
     * @param url
     * @param localPath
     * @throws MatrixLibException 
     */
    public static void downloadFileFromWeb (URL url, String localPath) throws MatrixLibException {
        try {        
            org.apache.commons.io.FileUtils.copyURLToFile(url, new File (localPath));
        } catch (IOException ex) {
            throw new MatrixLibException (ex.getMessage());
        }
    }

    public int lastStatus;
    public String lastStatusMessage;
    
    public String getTextFromWebBasicAuth (URL url, String user, String password) throws MatrixLibException {
        String rightPart = user + ":" + password;
        String authBase64 = StringUtil.encodeBase64(rightPart);
        String auth = "Basic " + authBase64 ;
        try {
            CloseableHttpClient httpClient = HttpClients.custom().build();
            HttpGet get = new HttpGet(url.toURI().toString());

            if (customDelay) {
                RequestConfig defaultRequestConfig = RequestConfig.custom()
                    .setSocketTimeout(1000 * nbSecDelay)
                    .setConnectTimeout(1000 * nbSecDelay)
                    .setConnectionRequestTimeout(1000 * nbSecDelay)
                    .setStaleConnectionCheckEnabled(true)
                    .build();            
                RequestConfig requestConfig = RequestConfig.copy(defaultRequestConfig).build();            
                get.setConfig(requestConfig);
            }
            
            get.setHeader("Authorization", auth);
            CloseableHttpResponse httpResponse = httpClient.execute(get);
            String ret = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            lastStatus = httpResponse.getStatusLine().getStatusCode();
            lastStatusMessage = httpResponse.getStatusLine().getReasonPhrase();
            return ret;
        } catch (IOException | URISyntaxException ex) {
            throw new MatrixLibException(ex);
        }
    }

    /**
     * For very big files the normal method is insane: it does wait to have the whole stream in RAM before to dump it on the file
     * Here we call wget 
     * @param url
     * @param user
     * @param password
     * @param file
     * @throws MatrixLibException 
     */
    public void getFileFromWebBasicAuthLinux (URL url, String user, String password, File file) throws MatrixLibException {
        try {
            // wget fails if file exists
            if (file.exists())
                file.delete();
            ExecUtil.exec(new String[]{
                "wget",
                "-O",
                file.getAbsolutePath(),
                "--auth-no-challenge",
                "--http-user",
                user,
                "--http-password",
                password,
                url.toExternalForm()
            });
        } catch (Exception ex) {
            LoggerConfig.getLogger().error("Exception " + ex.getMessage() + " in getFileFromWebBasicAuthLinux", ex);
            throw new MatrixLibException(ex);
        }
    }
    
    public void getFileFromWebBasicAuth (URL url, String user, String password, File file) throws MatrixLibException {
        if (! FileUtil.runOnWindows()) {
            getFileFromWebBasicAuthLinux (url, user, password, file);
            return;
        }
            
        String rightPart = user + ":" + password;
        String authBase64 = StringUtil.encodeBase64(rightPart);
        String auth = "Basic " + authBase64 ;
        try {
            CloseableHttpClient httpClient = HttpClients.custom().build();
            HttpGet get = new HttpGet(url.toURI().toString());

            if (customDelay) {
                RequestConfig defaultRequestConfig = RequestConfig.custom()
                    .setSocketTimeout(1000 * nbSecDelay)
                    .setConnectTimeout(1000 * nbSecDelay)
                    .setConnectionRequestTimeout(1000 * nbSecDelay)
                    .setStaleConnectionCheckEnabled(true)
                    .build();            
                RequestConfig requestConfig = RequestConfig.copy(defaultRequestConfig).build();            
                get.setConfig(requestConfig);
            }
            
            get.setHeader("Authorization", auth);
            CloseableHttpResponse httpResponse = httpClient.execute(get);
            // String ret = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            
            byte[] content  = EntityUtils.toByteArray(httpResponse.getEntity());        
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content);
            } catch (IOException ex) {
                throw new MatrixLibException(ex, "OopsClient.getFile - local IO");
            }
            
            lastStatus = httpResponse.getStatusLine().getStatusCode();
            lastStatusMessage = httpResponse.getStatusLine().getReasonPhrase();
        } catch (IOException | URISyntaxException ex) {
            LoggerConfig.getLogger().error("Error getFileFromWebBasicAuth: " + ex.getMessage(), ex);
            throw new MatrixLibException(ex);
        }
    }

    
    /**
     * Submits a POST on the web
     * @param url
     * @param user
     * @param password
     * @param postEntity Optional string to post (can be null)
     * @param mimeType must be specified if postEntity is not null
     * @return
     * @throws MatrixLibException 
     */
    public String postTextToWebBasicAuth (URL url, String user, String password, String postEntity, String mimeType) throws MatrixLibException {
        String rightPart = user + ":" + password;
        String authBase64 = StringUtil.encodeBase64(rightPart);
        String auth = "Basic " + authBase64 ;
        try {
            CloseableHttpClient httpClient = HttpClients.custom().build();
            HttpPost post = new HttpPost(url.toURI().toString());

            if (customDelay) {
                RequestConfig defaultRequestConfig = RequestConfig.custom()
                    .setSocketTimeout(1000 * nbSecDelay)
                    .setConnectTimeout(1000 * nbSecDelay)
                    .setConnectionRequestTimeout(1000 * nbSecDelay)
                    .setStaleConnectionCheckEnabled(true)
                    .build();            
                RequestConfig requestConfig = RequestConfig.copy(defaultRequestConfig).build();            
                post.setConfig(requestConfig);
            }
            if (postEntity != null) {
                StringEntity entity = new StringEntity(postEntity, "UTF-8");
                post.addHeader("Accept" , mimeType);
                post.setEntity(entity);
            }
            
            post.setHeader("Authorization", auth);
            
            CloseableHttpResponse httpResponse = httpClient.execute(post);
            
            String ret = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            lastStatus = httpResponse.getStatusLine().getStatusCode();
            lastStatusMessage = httpResponse.getStatusLine().getReasonPhrase();
            return ret;
        } catch (IOException | URISyntaxException ex) {
            throw new MatrixLibException(ex);
        }
    }

    /**
     * Submits a POST on the web
     * @param url
     * @param user
     * @param password
     * @param postJson Optional string to post (can be null)
     * @return
     * @throws MatrixLibException 
     */
    public String postTextToWebBasicAuthJson (URL url, String user, String password, String postJson) throws MatrixLibException {
        String rightPart = user + ":" + password;
        String authBase64 = StringUtil.encodeBase64(rightPart);
        String auth = "Basic " + authBase64 ;
        try {
            CloseableHttpClient httpClient = HttpClients.custom().build();
            HttpPost post = new HttpPost(url.toURI().toString());

            if (customDelay) {
                RequestConfig defaultRequestConfig = RequestConfig.custom()
                    .setSocketTimeout(1000 * nbSecDelay)
                    .setConnectTimeout(1000 * nbSecDelay)
                    .setConnectionRequestTimeout(1000 * nbSecDelay)
                    .setStaleConnectionCheckEnabled(true)
                    .build();            
                RequestConfig requestConfig = RequestConfig.copy(defaultRequestConfig).build();            
                post.setConfig(requestConfig);
            }
            if (postJson != null) {
                StringEntity entity = new StringEntity(postJson, "UTF-8");
                entity.setContentType(MimeTypeUtil.MIMETYPE_JSON + "; charset=utf-8");
                post.setEntity(entity);
            }
            
            post.addHeader("Accept", MimeTypeUtil.MIMETYPE_JSON);
            post.setHeader("Authorization", auth);
            
            CloseableHttpResponse httpResponse = httpClient.execute(post);
            
            String ret = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            lastStatus = httpResponse.getStatusLine().getStatusCode();
            lastStatusMessage = httpResponse.getStatusLine().getReasonPhrase();
            return ret;
        } catch (IOException | URISyntaxException ex) {
            throw new MatrixLibException(ex);
        }
    }
    
    /**
     * Submits a POST of a file on the web
     * @param url
     * @param user
     * @param password
     * @param fileToPost
     * @return
     * @throws MatrixLibException 
     */
    public String postFileToWebBasicAuth (URL url, String user, String password, File fileToPost) throws MatrixLibException {
        String rightPart = user + ":" + password;
        String authBase64 = StringUtil.encodeBase64(rightPart);
        String auth = "Basic " + authBase64 ;
        return postFileToWebSpecialAuth(url, auth, fileToPost);
    }

    /**
     * Submits a POST of a file on the web
     * @param url
     * @param auth - something like Basic xxx
     * @param fileToPost
     * @return
     * @throws MatrixLibException 
     */
    public String postFileToWebSpecialAuth (URL url, String auth, File fileToPost) throws MatrixLibException {
        try {
            CloseableHttpClient httpClient = HttpClients.custom().build();
            HttpPost post = new HttpPost(url.toURI().toString());

            if (customDelay) {
                RequestConfig defaultRequestConfig = RequestConfig.custom()
                    .setSocketTimeout(1000 * nbSecDelay)
                    .setConnectTimeout(1000 * nbSecDelay)
                    .setConnectionRequestTimeout(1000 * nbSecDelay)
                    .setStaleConnectionCheckEnabled(true)
                    .build();            
                RequestConfig requestConfig = RequestConfig.copy(defaultRequestConfig).build();            
                post.setConfig(requestConfig);
            }
            String mimeType = MimeTypeUtil.getContentType(fileToPost.getName());
            ContentType contentType;
            FileBody fileBody;
            if (mimeType.contains(";")) {
                String before = StringUtil.before(mimeType, ";").trim();
                String after = StringUtil.after(mimeType, ";").trim().replace("charset=", "");
                contentType = ContentType.create(before, after);
            }
            else
                contentType = ContentType.create(mimeType);
            fileBody = new FileBody(fileToPost, contentType, fileToPost.getName());
            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addPart("file", fileBody)
                    .build();

            post.setEntity(reqEntity);
            
            post.setHeader("Authorization", auth);
            
            CloseableHttpResponse httpResponse = httpClient.execute(post);
            
            String ret = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            lastStatus = httpResponse.getStatusLine().getStatusCode();
            lastStatusMessage = httpResponse.getStatusLine().getReasonPhrase();
            return ret;
        } catch (IOException | URISyntaxException ex) {
            throw new MatrixLibException(ex);
        }
    }


    /**
     * Submits a POST on the web
     * @param url
     * @param user
     * @param password
     * @return
     * @throws MatrixLibException 
     */
    public String putTextToWebBasicAuth (URL url, String user, String password) throws MatrixLibException {
        String rightPart = user + ":" + password;
        String authBase64 = StringUtil.encodeBase64(rightPart);
        String auth = "Basic " + authBase64 ;
        try {
            CloseableHttpClient httpClient = HttpClients.custom().build();
            HttpPut put = new HttpPut(url.toURI().toString());

            if (customDelay) {
                RequestConfig defaultRequestConfig = RequestConfig.custom()
                    .setSocketTimeout(1000 * nbSecDelay)
                    .setConnectTimeout(1000 * nbSecDelay)
                    .setConnectionRequestTimeout(1000 * nbSecDelay)
                    .setStaleConnectionCheckEnabled(true)
                    .build();            
                RequestConfig requestConfig = RequestConfig.copy(defaultRequestConfig).build();            
                put.setConfig(requestConfig);
            }
            
            put.setHeader("Authorization", auth);
            
            CloseableHttpResponse httpResponse = httpClient.execute(put);
            
            String ret = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            lastStatus = httpResponse.getStatusLine().getStatusCode();
            lastStatusMessage = httpResponse.getStatusLine().getReasonPhrase();
            return ret;
        } catch (IOException | URISyntaxException ex) {
            throw new MatrixLibException(ex);
        }
    }

    /**
     * Submits a POST on the web
     * @param url
     * @param postEntity Optional string to post (can be null)
     * @param mimeType must be specified if postEntity is not null
     * @return
     * @throws MatrixLibException 
     */
    public String postTextToWeb(URL url, String postEntity, String mimeType) throws MatrixLibException {
        try {
            CloseableHttpClient httpClient = HttpClients.custom().build();
            HttpPost post = new HttpPost(url.toURI().toString());

            if (customDelay) {
                RequestConfig defaultRequestConfig = RequestConfig.custom()
                    .setSocketTimeout(1000 * nbSecDelay)
                    .setConnectTimeout(1000 * nbSecDelay)
                    .setConnectionRequestTimeout(1000 * nbSecDelay)
                    .setStaleConnectionCheckEnabled(true)
                    .build();            
                RequestConfig requestConfig = RequestConfig.copy(defaultRequestConfig).build();            
                post.setConfig(requestConfig);
            }
            if (postEntity != null) {
                StringEntity entity = new StringEntity(postEntity, "UTF-8");
                post.setEntity(entity);
                post.setHeader("Content-type", mimeType);
            }
            
            CloseableHttpResponse httpResponse = httpClient.execute(post);

            String ret;
            try {
                ret = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            }
            catch (Exception | Error ignore) {
                ret = "?";
            }
            lastStatus = httpResponse.getStatusLine().getStatusCode();
            lastStatusMessage = httpResponse.getStatusLine().getReasonPhrase();
            return ret;
        } catch (IOException | URISyntaxException ex) {
            throw new MatrixLibException(ex);
        }
    }

    
    /**
     * Call this function to set a custom delay wait of N seconds
     * @param nbSec 
     */
    public void setDelay (int nbSec) {
        customDelay = true;
        nbSecDelay = nbSec;
    }

    /**
     * Returns the host name of an IP address (reverse DNS)
     * @param ip
     * @return 
     */
    public static String getReverseDns (String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            return addr.getHostName();
        } catch (UnknownHostException ex) {
            return "?";
        }
    }
    
    /**
     * Retrieves a file from the web using a HTTP GET 
     * @param url
     * @param localFile
     * @return the mime type obtained by the HTTP protocol or null if none
     * @throws com.matrixreq.lib.MatrixLibException
     */
    public static String getFileFromWebReturnMimeType (URL url, File localFile) throws MatrixLibException {
        try {
            CloseableHttpClient client = HttpClients.custom().build();
            HttpGet get = new HttpGet(url.toExternalForm());
            // Re
            CloseableHttpResponse httpResponse = client.execute(get);
            byte[] byteArray = EntityUtils.toByteArray(httpResponse.getEntity());
            FileUtils.writeByteArrayToFile(localFile, byteArray);
            int lastStatus = httpResponse.getStatusLine().getStatusCode();
            if (lastStatus != 200)
                throw new MatrixLibException("Error " + lastStatus + " retrieveing file");
            Header contentHeader = httpResponse.getFirstHeader("Content-Type");
            if (contentHeader == null)
                return null;
            return contentHeader.getValue();
        } catch (IOException | ParseException ex) {
            throw new MatrixLibException(ex);
        }
    }
}
