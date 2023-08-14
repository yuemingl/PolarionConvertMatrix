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

import com.matrixreq.lib.ExecUtil.ExecException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Yves
 */
public class FileUtil {

    /**
     * True if we run on Windows
     * @return 
     */
    static public boolean runOnWindows () {
        return System.getProperty("os.name").startsWith("Win");
    }
    
    static public boolean runOnMac () {
        return System.getProperty("os.name").startsWith("Mac");
    }

    public static boolean runOnLinux() {
        return ! (runOnMac() || runOnWindows());
    }

    public static boolean waitForFile(Path filePath, int timeout) {
        Instant now = Instant.now();
        Instant expiringTime = now.plus(timeout, ChronoUnit.MILLIS);
        while (now.isBefore(expiringTime)) {
            if (Files.exists(filePath)) {
                return true;
            }
            now = Instant.now();
        }
        return false;
    }

    static public class MatrixFileException extends MatrixLibException {
        /**
         *
         */
        private static final long serialVersionUID = -3186113506998400805L;

        public MatrixFileException(String message) {
            super(message);
            addDetail("File exception");
        }
    } 
    
    /**
     * From http://stackoverflow.com/questions/326390/how-to-create-a-java-string-from-the-contents-of-a-file
     * @param path
     * @param encoding
     * @return the string that is contained in the text file
     * @throws IOException 
     */
    public static String readFile(String path, Charset encoding) throws IOException 
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }    

    /**
     * Reads a UTF8 encoded text file into a single string
     * @param path
     * @return
     * @throws IOException 
     */
    public static String readUtf8File(String path) throws IOException {
        return readFile(path, StandardCharsets.UTF_8);
    }
    
    /**
     * Reads a UTF8 text file into an array of lines
     * @param path
     * @return
     * @throws IOException
     */
    public static ArrayList<String> readUtf8FileArray(String path) throws IOException {
        List<String> stringList = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        return new ArrayList<> (stringList);
    }

    /**
     * Creates a temporary file
     * @param header
     * @param ext
     * @return
     * @throws IOException 
     */
    public static String createTempFile (String header, String ext) throws IOException {
        File temp = File.createTempFile(header, ext);
        return temp.getAbsolutePath();
    }

    /**
     * Creates a temporary file NAME only, deletes right away the file itself
     * @param header
     * @param ext
     * @return
     * @throws IOException 
     */
    public static String createTempFileNameButNoFile (String header, String ext) throws IOException {
        File temp = File.createTempFile(header, ext);
        String filename = temp.getAbsolutePath();
        temp.delete();
        return filename;
    }
    
    /**
     * Creates a temporary folder
     * @param header Prefix to use
     * @return
     * @throws IOException 
     */
    public static String createTempFolder (String header) throws IOException {
        Path path = Files.createTempDirectory(header);
        return path.toString();
    }
    
    /**
     * Creates a text file with the full content passed as a single string
     * @param filename
     * @param content
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException 
     */
    public static void createTextFile(String filename, String content) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(filename, "UTF-8");
        writer.print(content);
        writer.close();        
    }

    /**
     * Creates a text file with the full content passed as a single string
     * @param file
     * @param content
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static void createTextFile(File file, String content) throws FileNotFoundException, UnsupportedEncodingException {
        createTextFile(file.getAbsolutePath(), content);
    }

    /**
     * Creates a folder if it doesn't exist, and creates upper folder as well if needed
     * @param folderName 
     */
    public static void createFolderIfDoesntExist(String folderName) {
        File f = new File(folderName);
        f.mkdirs();
    }
    
    /**
     * Do recursively on a folder:
     *   - change owner to matrixadmin
     *   - set sticky bit and rwx for the group
     * @param folder
     * @throws com.matrixreq.lib.ExecUtil.ExecException
     */
    public static void setTomcatPermissionsRecursive (String folder) throws ExecException {
        ExecUtil.exec("chown -R tomcat:tomcat " + folder);
        ExecUtil.exec("chmod -R g+rwxs " + folder);
    }

    /**
     * Change permission on a file so that only tomcat user can read/write it, the others cannot even read it
     * @param file
     * @throws com.matrixreq.lib.ExecUtil.ExecException
     */
    public static void setTomcatPermissionOnly (String file) throws ExecException {
        ExecUtil.exec("chown tomcat:tomcat " + file);
        ExecUtil.exec("chmod go-rwxs " + file);
    }
    
    /**
     * Replaces a series of strings by other strings in a file
     * @param fileName the file to transform
     * @param search the strings to search
     * @param replace the strings to replace the ones found. [0] in search is replaces by [0] in replace etc
     * @throws com.matrixreq.oops.util.FileUtil.MatrixFileException 
     */
    public static void replaceAllStringsInTextFile (String fileName, String [] search, String [] replace) throws MatrixFileException {
        if (fileName == null || search == null || replace == null || search.length != replace.length)
            throw new MatrixFileException ("Wrong parameters to replaceAllStringsInTextFile");
        try {
            String content = FileUtil.readUtf8File(fileName);
            for (int transform = 0; transform < search.length; transform++)
                content = content.replace(search[transform], replace[transform]);
            FileUtil.createTextFile(fileName, content);
        } catch (IOException ex) {
            throw new MatrixFileException("Unable to replace string in file [" + fileName + "]");
        }
    }

    /**
     * Replaces a series of strings by other strings in a file
     * @param fileName the file to transform
     * @param search the strings to search
     * @param replace the strings to replace the ones found. [0] in search is replaces by [0] in replace etc
     * @throws com.matrixreq.oops.util.FileUtil.MatrixFileException 
     */
    public static void replaceStringsInTextFile (String fileName, String search, String replace) throws MatrixFileException {
        replaceAllStringsInTextFile(fileName, new String[]{search}, new String[]{replace});
    }

    /**
     * Extracts the content of META-INF/MANIFEST.MF from a war file and returns it as an array of strings
     * In order to have a manifest in the war using netbeans's ant and TeamCity: https://matrixreq.atlassian.net/browse/MATRIX-137
     * @param warFile
     * @return
     * @throws IOException
     */
    public static ArrayList<String> getManifestFromWar (String warFile) throws IOException {
        try {
            String [] unzip = new String [] {"unzip", "-p", warFile, "META-INF/MANIFEST.MF"};
            ArrayList<String> content = ExecUtil.exec(unzip);
            if (content.isEmpty())
                throw new IOException("Unable to retrieve manifest");
            for (String s: content)
                if (s.startsWith("E|"))
                    throw new IOException("Unable to retrieve manifest");
            return content;
        }
        catch (ExecUtil.ExecException e) {
            throw new IOException("Unable to unzip war file");
        }
    }

    /**
     * Retrieve the Implementation-Version string from the manifest of a .war file.
     * If the .war file doesn't exist, tries to read from the folder whose name is the same, without the .war
     * @param warFile
     * @return
     * @throws IOException 
     */
    public static String getWarVersion(String warFile) throws IOException {
        ArrayList<String> manifest;
        try {
            manifest = getManifestFromWar(warFile);
        }
        catch (IOException e) {
            String warFolder = warFile.replace(".war", "");
            String warFileName = warFolder + "/META-INF/MANIFEST.MF";
            manifest = FileUtil.readUtf8FileArray(warFileName);
        }
        for (String s: manifest)
            if (s.startsWith("Implementation-Version:"))
                return s.substring(1 + s.indexOf(":")).trim();
        throw new IOException("No version in the manifest");
    }

    /**
     * List all files in folder
     * @param folder
     * @param mask can be null
     * @return list of local names inside the folder
     */
    public static ArrayList<String> listFiles (String folder, String mask) {
        ArrayList<String> ret = new ArrayList<String> ();
        File dir = new File(folder);
        File[] files;
        if (mask == null)
            files = dir.listFiles();        
        else {
            FileFilter fileFilter = new WildcardFileFilter(mask);
            files = dir.listFiles(fileFilter);        
        }
        if (files != null)
            for (File f: files)
                ret.add(f.getName());
        return ret;
    }
    
    /**
     * Recursive deletion of a folder
     * @param folderName
     * @throws IOException if deletion didn't went well
     */
    public static void removeFolder (String folderName) throws IOException {
        FileUtils.deleteDirectory(new File(folderName));
    }

    /**
     * Recursive deletion of a folder
     * @param folderName
     */
    public static void removeFolderSafe (String folderName) {
        try {
            if (StringUtils.isNotEmpty(folderName))
                FileUtils.deleteDirectory(new File(folderName));
        } catch (IOException ignore) {
        }
    }

    /**
     * @param in
     * @return size of the file
     */
    public static long getFileSize (File in) {
        return in.length();
    }

    /**
     * @param in
     * @return size of the file
     */
    public static long getFileSize (String in) {
        return (new File(in)).length();
    }
    
    public static String convertFilePath (String in) {
        if (runOnWindows())
            in = in . replace ("/", "\\");
        return in;
    }

    public static void copyFile (File source, File target) throws IOException {
        FileUtils.copyFile(source, target);
    }
    
    public static void copyFileToDirectory (File source, File target) throws IOException {
        FileUtils.copyFileToDirectory(source, target);
    }
    
    public static String extractLastFilePart (String path) {
        if (StringUtils.isEmpty(path))
            return null;
        path = path.replace("\\", "/");
        String[] split = StringUtils.split(path, "/");
        return split[split.length - 1];
    }

    public static List<File> recursiveListFiles(String basePath) {
        File base = new File(basePath);
        Collection<File> listFiles = FileUtils.listFiles(base, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        List<File> ret = new ArrayList<>();
        ret.addAll(listFiles);
        return ret;
    }

    public static String md5(String path) throws ExecException {
        /*
$ md5sum /home/clouds/ugentec/webapps/ugentec_clouds.war
db582fea97b55c2864f3c05d4e10865e  /home/clouds/ugentec/webapps/ugentec_clouds.war
        */
        ArrayList<String> exec = ExecUtil.exec(new String[]{"md5sum", path});
        return StringUtil.before(exec.get(0), " ");
    }

    public static byte[] readFromRessourceAsByteArray(Class theClass, String path) throws IOException
    {
        return  IOUtils.toByteArray(theClass.getResourceAsStream(path));
    }

    public static String readFromRessourceAsString(Class theClass, String path) throws IOException
    {
        return  IOUtils.toString(theClass.getResourceAsStream(path));
    }

}
