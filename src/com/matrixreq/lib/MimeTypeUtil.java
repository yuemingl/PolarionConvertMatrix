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
import java.io.IOException;
import java.nio.file.Files;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Administrator
 */
public class MimeTypeUtil {
    public static final String MIMETYPE_BINARY = "application/octet-stream";
    public static final String MIMETYPE_BMP = "image/bmp";
    public static final String MIMETYPE_CSS = "text/css";
    public static final String MIMETYPE_CSV = "text/csv";
    public static final String MIMETYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String MIMETYPE_HTML = "text/html";
    public static final String MIMETYPE_JAVASCRIPT = "application/javascript";
    public static final String MIMETYPE_JPG = "image/jpeg";
    public static final String MIMETYPE_JSON = "application/json";
    public static final String MIMETYPE_ODT = "application/vnd.oasis.opendocument.text";
    public static final String MIMETYPE_PDF = "application/pdf";
    public static final String MIMETYPE_PNG = "image/png";
    public static final String MIMETYPE_TEXT = "text/plain";
    public static final String MIMETYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String MIMETYPE_XML = "text/xml";
    public static final String MIMETYPE_XSL = "text/xml+xsl";
    public static final String MIMETYPE_ZIP = "application/zip";

    public static final String MIMETYPE_DEFAULT = MIMETYPE_BINARY;
    
    private static String getSystemMime (String fileName) {
        File f = new File(fileName);
        try {
            String type = Files.probeContentType(f.toPath());
            // Mac returns this as text/javascript, not application/javascript
            if (FileUtil.runOnMac() && type != null && type.equals("text/javascript")) {
                type = "application/javascript";
            }
            return type;
        } catch (IOException ex) {
            return null;
        }
    }
    
    /**
     * Returns the mime type of a file, given its file name
     * @param fileName can have path before, we only use the extension
     * @return mime type. Defaults to application/octet-stream for unknown extensions
     */
    public static String getMimeTypeOfFile(String fileName) {
        String extension = fileName.substring(1 + fileName.lastIndexOf('.'));
        // We have sometime some illegal names (as per Windows) when asking for mimetypes of files like "image2015-9-6 16:14:58.png"
        fileName = "x." + extension;
        switch (extension.toLowerCase()) {
            case "xsl":
            case "xslt":
                // this is returned as XML by the smart detector below
                return MIMETYPE_XSL;
            default:
                String mime = getSystemMime(fileName);
                if (! StringUtils.isEmpty(mime))
                    return mime;
                switch (extension.toLowerCase()) {
                    case "bmp":
                        return MIMETYPE_BMP;
                    case "css":
                        return MIMETYPE_CSS;
                    case "csv":
                        return MIMETYPE_CSV;
                    case "docx":
                        return MIMETYPE_DOCX;
                    case "html":
                    case "htm":
                        return MIMETYPE_HTML;
                    case "jpg":
                        return MIMETYPE_JPG;
                    case "js":
                        return MIMETYPE_JAVASCRIPT;
                    case "json":
                        return MIMETYPE_JSON;
                    case "odt":
                        return MIMETYPE_ODT;
                    case "pdf":
                        return MIMETYPE_PDF;
                    case "png":
                        return MIMETYPE_PNG;
                    case "txt":
                        return MIMETYPE_TEXT;
                    case "xlsx":
                        return MIMETYPE_XLSX;
                    case "zip":
                        return MIMETYPE_ZIP;
                }
            return MIMETYPE_DEFAULT;
        }
    }
    
    /**
     * Returns the full content type including UTF8 encoding for the usual suspects: html, xml, json, ...
     * @param nick : html, json, xml, css, ...
     * @return 
     */
    public static String getContentType (String nick) {
        String mime = getMimeTypeOfFile("x." + nick);
        return mime + ";charset=UTF-8";
    }
    
    /**
     * Sets the content type of an HttpServletResponse
     * @param response
     * @param nick : html, json, xml, css, ...
     */
    public static void setContentType (HttpServletResponse response, String nick) {
        response.setContentType(getContentType(nick));
    }
    
    public static String getExtensionForMimeType (String mimeType) {
        switch (mimeType) {
            case MIMETYPE_BMP:
                return "bmp";
            case MIMETYPE_CSS:
                return "css";
            case MIMETYPE_CSV:
                return "csv";
            case MIMETYPE_DOCX:
                return "docx";
            case MIMETYPE_HTML:
                return "html";
            case MIMETYPE_JPG:
                return "jpg";
            case MIMETYPE_JAVASCRIPT:
                return "js";
            case MIMETYPE_JSON:
                return "json";
            case MIMETYPE_ODT:
                return "odt";
            case MIMETYPE_PDF:
                return "pdf";
            case MIMETYPE_PNG:
                return "png";
            case MIMETYPE_TEXT:
                return "txt";
            case MIMETYPE_XLSX:
                return "xlsx";
            case MIMETYPE_XSL:
                return "xslt";
            case MIMETYPE_ZIP:
                return "zip";
        }
        // The most important ones are the images
        if (mimeType.contains("jpeg") || mimeType.contains("jpg"))
            return "jpg";
        if (mimeType.contains("png"))
            return "png";
        if (mimeType.contains("bitmap"))
            return "bmp";
        if (mimeType.contains("tiff"))
            return "tif";
        if (mimeType.contains("xml"))
            return "xml";
        // last resort: binary
        return "bin";
    }
}
