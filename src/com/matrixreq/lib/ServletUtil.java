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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * From http://blog.frankel.ch/get-the-handle-on-the-manifest-mf-in-a-webapp
 * @author Yves
 */
public class ServletUtil {
    /**
     * Retrieve a war version from a FilterConfig
     * @param filterConfig
     * @return war version or "?"
     */
    public static String getMyVersion (FilterConfig filterConfig) {
        return getMyVersion (filterConfig.getServletContext());
    }

    /**
     * Retrieve a war version from a ServletRequest
     * @param request
     * @return war version or "?"
     */
    public static String getMyVersion (ServletRequest request) {
        return getMyVersion (request.getServletContext());
    }

    /**
     * Retrieve a war version from a ServletContext
     * @param context
     * @return war version or "?"
     */
    public static String getMyVersion (ServletContext context) {
        try {
            InputStream inputStream = context.getResourceAsStream("/META-INF/MANIFEST.MF");
            Manifest manifest;        
            manifest = new Manifest(inputStream);
            String version = manifest.getMainAttributes().getValue("Implementation-Version");
            if (StringUtils.isNotEmpty(version) && version.contains("{"))
                version = "2.2.999.9999";
            return version;
        } catch (IOException ex) {
            return "?";
        }
    }
    /**
     * Sends a file as a HttpServletResponse
     * @param response
     * @param mimeType
     * @param pathToFile
     * @throws MatrixLibException 
     */
    public static void servletSendFile (HttpServletResponse response, String mimeType, String pathToFile) throws MatrixLibException {
        try {
            response.setContentType(mimeType);
            if (FileUtil.runOnWindows())
                pathToFile = pathToFile.replace("/", "\\");
            File f = new File(pathToFile);
            FileInputStream is = new FileInputStream(f);
            IOUtils.copyLarge(is, response.getOutputStream());
        } catch (IOException ex) {
            throw new MatrixLibException(ex);
        }
    }

    /**
     * Sends a file as a HttpServletResponse
     * @param response
     * @param mimeType
     * @param pathToFile
     * @param visibleName
     * @param cacheAgeInSeconds: can be null
     * @param disposition: can be attachment or inline
     * @throws MatrixLibException 
     */
    public static void servletSendFile (HttpServletResponse response, String mimeType, String pathToFile, String visibleName, Integer cacheAgeInSeconds, String disposition) throws MatrixLibException {
        try {
            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", disposition + "; filename=" + visibleName);
            if (cacheAgeInSeconds != null)
                response.setHeader("Cache-Control", "max-age=" + cacheAgeInSeconds);
            if (FileUtil.runOnWindows())
                pathToFile = pathToFile.replace("/", "\\");
            File f = new File(pathToFile);
            FileInputStream is = new FileInputStream(f);
            IOUtils.copyLarge(is, response.getOutputStream());
        } catch (IOException ex) {
            throw new MatrixLibException(ex);
        }
    }

    public static void servletSendFile (HttpServletResponse response, String mimeType, String pathToFile, String visibleName, Integer cacheAgeInSeconds) throws MatrixLibException {
        servletSendFile(response, mimeType, pathToFile, visibleName, cacheAgeInSeconds, "attachment");
    }
}
