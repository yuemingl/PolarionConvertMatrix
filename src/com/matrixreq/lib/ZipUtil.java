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

import static com.matrixreq.lib.FileUtil.runOnWindows;
import java.io.File;
import java.util.ArrayList;

import com.matrixreq.lib.ExecUtil.ExecException;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.slf4j.Logger;

/**
 *
 * @author Yves
 */
public class ZipUtil {
    /**
     * Create a zip with an array of files
     * @param logger may be null
     * @param sourceFolder
     * @param sourceFiles
     * @param zipFileName
     * @throws com.matrixreq.lib.ExecUtil.ExecException
     */
    public static void zipFiles (Logger logger, String sourceFolder, String [] sourceFiles, String zipFileName) throws ExecUtil.ExecException {
        if (runOnWindows()) {
            try {
                // Initiate Zip Parameters which define various properties such
                // as compression method, etc.
                ZipParameters parameters = new ZipParameters();
                parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
                parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
                
                ZipFile zipFile = new ZipFile(sourceFolder + "\\" + zipFileName);
                for (String s: sourceFiles) {
                    File f = new File(sourceFolder, s);
                    if (f.isDirectory())
                        zipFile.addFolder(f, parameters);
                    else
                        zipFile.addFile(f, parameters);
                }
            } catch (ZipException ex) {
                throw new ExecUtil.ExecException("Error creating zip: " + ex.getMessage());
            }
        }
        else {
            String [] start = new String [] {"zip", "-r", "-q", zipFileName};
            String [] zip = new String [start.length + sourceFiles.length];
            int i;
            for (i = 0; i < start.length; i++)
                zip [i] = start [i];
            for (i = 0; i < sourceFiles.length; i++)
                zip [i + start.length] = sourceFiles [i];
            ExecUtil.execInDirectory(sourceFolder, zip, logger);
        }
    }
    
    /**
     * Create a zip with some files
     * @param logger may be null
     * @param sourceFolder
     * @param sourceFiles
     * @param zipFileName
     * @throws com.matrixreq.lib.ExecUtil.ExecException
     */
    public static void zipFiles (Logger logger, String sourceFolder, String sourceFiles, String zipFileName) throws ExecUtil.ExecException {
        zipFiles(logger, sourceFolder, new String[]{sourceFiles}, zipFileName);
    }

    /**
     * Zip a single file without storing all it's hierarchy (for storing filter.xmlz)
     * @param sourceFolder
     * @param sourceFile
     * @param zipFileName
     * @throws ExecException
     */
    public static void zipSingleFile(String sourceFolder, String sourceFile, String zipFileName) throws ExecException {
        if (runOnWindows()) {
            try {
                // Initiate Zip Parameters which define various properties such
                // as compression method, etc.
                ZipParameters parameters = new ZipParameters();
                parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
                parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
                
                ZipFile zipFile = new ZipFile(zipFileName);
                File f = new File(sourceFolder, sourceFile);
                zipFile.addFile(f, parameters);
            } catch (ZipException ex) {
                throw new ExecUtil.ExecException("Error creating zip: " + ex.getMessage());
            }
        }
        else {
            String [] zip = new String [] {"zip", "-q", "-j", zipFileName, sourceFile};   // -j: -j   junk (don't record) directory names
            ExecUtil.execInDirectory(sourceFolder, zip, null);
        }
    }

    /**
     * Unzip one file from a ZIP
     * @param zipFile the zip file
     * @param extractFolder the extract folder
     * @param fileToExtract may be null, in this case the whole zip is unzipped
     * @throws MatrixLibException 
     */
    public static void unzipFile (File zipFile, File extractFolder, String fileToExtract) throws MatrixLibException {
        if (runOnWindows()) {
            try {
                // Initiate ZipFile object with the path/name of the zip file.
                ZipFile zip = new ZipFile(zipFile);
                if (fileToExtract == null)
                    zip.extractAll(extractFolder.getAbsolutePath());
                else
                    zip.extractFile(fileToExtract, extractFolder.getAbsolutePath());
            } catch (ZipException ex) {
                throw new MatrixLibException("Can't unzip: " + ex.getMessage());
            }
        }
        else {
            String [] unzip;
            // the -o flag is to overwrite files if they exist already
            if (fileToExtract == null)
                unzip = new String [] {"unzip", "-o", "-d", extractFolder.getAbsolutePath(), zipFile.getAbsolutePath()};
            else
                unzip = new String [] {"unzip", "-o", "-d", extractFolder.getAbsolutePath(), zipFile.getAbsolutePath(), fileToExtract};
            ArrayList<String> content = ExecUtil.exec(unzip);
            for (String s: content)
                if (s.startsWith("E|"))
                    throw new MatrixLibException(s);
        }
    }
    
}
