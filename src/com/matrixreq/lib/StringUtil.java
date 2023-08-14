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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Yves
 */
public class StringUtil {
    
    /**
     * Split a string like "  ab     cd   e" into {"ab", "cd", "e"}
     * Note that the first space is skipped. If we used string.split("\\s+") the first space is returned as 1st split
     * @param base
     * @return an array of strings
     */
    public static String[] splitOnAnySpace (String base) {
        // return base.split("\\s+");
        return StringUtils.split(base);
    }

    /**
     * Creates a string with the concatenation of all elements of the array separated by a string delimitor
     * @param array
     * @param with
     * @return 
     */
    public static String joinArrayWith(String[] array, String with) {
        if (array == null || array.length < 1)
            return "";
        String out = array [0];
        for (int i = 1; i < array.length; i++)
            out += with + array[i];
        return out;
    }

    /**
     * Creates a string with the concatenation of all elements of the array separated by a string delimitor
     * @param list
     * @param with
     * @return 
     */
    public static String joinArrayWith(List<String> list, String with) {
        if (list == null || list.size() < 1)
            return "";
        String out = list.get(0);
        for (int i = 1; i < list.size(); i++)
            out += with + list.get(i);
        return out;
    }
    
    /**
     * Encodes a string to base64
     * @param in
     * @return 
     */
    public static String encodeBase64 (String in) {
        try {
            return org.apache.commons.codec.binary.StringUtils.newStringUtf8(Base64.encodeBase64(in.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException ignore) {
            return null;
        }
    }

    /**
     * Decodes a base64 string
     * @param in
     * @return 
     */
    public static String decodeBase64 (String in) {
        try {
            return org.apache.commons.codec.binary.StringUtils.newStringUtf8(Base64.decodeBase64(in.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException ignore) {
            return null;
        }
    }
    
    /**
     * Formats a double with 3 decimals
     * @param in
     * @return 
     */
    public static String formatDouble3 (double in) {
        DecimalFormat df = new DecimalFormat("#.###");        
        return df.format(in);
    }

    /**
     * Format a double with custom format string like #.##
     * @param in
     * @param format
     * @return 
     */
    public static String formatDouble (double in, String format) {
        DecimalFormat df = new DecimalFormat(format);        
        return df.format(in);
    }
    
    /**
     * Cut a string to atmost N chars
     * @param in
     * @return 
     */
    public static String dispN (String in, int N) {
        if (StringUtils.isEmpty(in))
            return "";
        if (in.length() < N)
            return in;
        return in.substring(0, N-4) + " ...";
    }

    public static String htmlEscape(String base) {
        return StringEscapeUtils.escapeHtml4(base);
    }

    /**
     * Naive implementation - remove opening and closing xml tag
     * @param x For example "<x:y> some <b>text</b> </x:y>"
     * @return For example "some <b>text</b>"
     */
    public static String removeFirstLastXmlTag(String x){
        int firstClose = x.indexOf(">");
        if (firstClose >= 0)
            x = x.substring(firstClose + 1);
        int lastOpen = x.lastIndexOf("<");
        if (lastOpen >= 0)
            x = x.substring(0, lastOpen);
        return x;
    }

    /**
     * Splits a comma delimited csv line with double quotes around strings into elements
     * @param in
     * @return 
     */
    public static ArrayList<String> splitCsvLine (String in) {
        ArrayList<String> fields = new ArrayList<>();
        boolean inString = false;
        String currentString = "";
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            switch (c) {
                case '"':
                    inString = ! inString;
                    break;
                case ',':
                    if (! inString) {
                        fields.add(currentString);
                        currentString = "";
                    }
                    else
                        currentString += c;
                    break;
                default:
                    currentString += c;
            }
        }
        fields.add(currentString);
        return fields;
    }

    /**
     * Encode a (UTF8) string into HEX
     * @param in
     * @return 
     */
    public static String encodeToHex (String in) {
        return Hex.encodeHexString(in.getBytes());
    }
    
    /**
     * Decodes a HEX string into a UTF8 string. Throws exception if input string is not hex
     * @param in
     * @return 
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public static String decodeFromHex (String in) throws MatrixLibException {
        try {
            byte[] res = Hex.decodeHex(in.toCharArray());
            return new String (res, "UTF-8");
        } catch (DecoderException | UnsupportedEncodingException ex) {
            throw new MatrixLibException("Not a HEX string");
        }
    }
    
    /**
     * We only deal with UTF8 strings
     * @param in
     * @return 
     */
    public static String urlEncode (String in) {
        try {
            return URLEncoder.encode(in, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            LoggerConfig.getLogger().error("Unsupported UTF8 : ???");
            // best guess
            return in;
        }
    }

    /**
     * We only deal with UTF8 strings
     * @param in
     * @return 
     */
    public static String urlDecode (String in) {
        try {
            return URLDecoder.decode(in, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            LoggerConfig.getLogger().error("Unsupported UTF8 : ???");
            // best guess
            return in;
        }
    }
    
    
    /**
     * This function takes a string like a company name and transforms into a valid lowercase letters domain name
     * @param in
     * @return a valid domain name, "x" if empty or only contains garbage. Prefix with x if original starts with a number
     */
    public static String normalizeDomainPartName (String in) {
        String out = "";
        for (char c: in.toCharArray()) {
            if (c >= 'A' && c <= 'Z')
                out += Character.toLowerCase(c);
            else
                if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9'))
                    out += c;
        }
        if (StringUtils.isEmpty(out) || (out.charAt(0) < 'a' || out.charAt(0) > 'z'))
            out = 'x' + out;
        return out;
    }
    
    /**
     * This function takes a string like a project name and transforms into a valid name without white space or delimiters
     * @param in
     * @return a valid domain name, "x" if empty or only contains garbage. Prefix with x if original starts with a number
     */
    public static String normalizeLettersDigits (String in) {
        String out = "";
        for (char c: in.toCharArray()) {
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9'))
                out += c;
        }
        if (StringUtils.isEmpty(out) || (out.charAt(0) >= '0' && out.charAt(0) <= '9'))
            out = 'X' + out;
        return out;
    }

    /**
     * Converts a string to an int, no exception
     * @param in
     * @return the int or zero if not valid
     */
    public static int stringToIntZero (String in) {
        return stringToInt (in, 0);
    }

    /**
     * Converts a string to an int, no exception
     * @param in
     * @param valueIfInvalid
     * @return the int or zero if not valid
     */
    public static int stringToInt (String in, int valueIfInvalid) {
        if (StringUtils.isEmpty(in))
            return valueIfInvalid;
        try {
            return Integer.parseInt(in);
        }
        catch (Exception ignore) {
            return valueIfInvalid;
        }
    }

    /**
     * Converts a string to a double, no exception
     * @param in
     * @return the double or zero if not valid
     */
    public static double stringToDoubleZero (String in) {
        return stringToDouble(in, 0.0);
    }

    /**
     * Converts a string to a double, no exception
     * @param in
     * @param valueIfInvalid
     * @return the double or zero if not valid
     */
    public static double stringToDouble (String in, double valueIfInvalid) {
        if (StringUtils.isEmpty(in))
            return valueIfInvalid;
        try {
            return Double.parseDouble(in);
        }
        catch (Exception ignore) {
            return valueIfInvalid;
        }
    }

    
    /**
     * Returns a substring between 2 delimitors, or an empty string if not found
     * @param base
     * @param start
     * @param end
     * @return 
     */
    public static String between (String base, String start, String end) {
        if (StringUtils.isEmpty(base) || StringUtils.isEmpty(start) || StringUtils.isEmpty(end))
            return "";
        int startPos = base.indexOf(start);
        if (startPos < 0)
            return "";
        int endPos = base.indexOf(end, startPos + start.length());
        if (endPos < 0)
            return "";
        return base.substring(startPos + start.length(), endPos);
    }

    /**
     * http://stackoverflow.com/questions/7552253/how-to-remove-special-characters-from-a-string
     * ... The regex matches everything that is not a letter in any language and not a separator (whitespace, linebreak etc.)...
     * it allows digits and the dot
     * @param in input string
     * @return same string, with all special chars removed
     */
    public static String removeSpecialChars (String in) {
        return in.replaceAll("[^\\p{L}\\p{Z}\\p{N}\\.]","");
    }

    /**
     * Variations on the above function removeSpecialChars that allows dashes
     * @param in input string
     * @return same string, with all special chars removed
     */
    public static String removeSpecialCharsAllowDash (String in) {
        return in.replaceAll("[^\\p{L}\\p{Z}\\p{N}\\.\\-]","");
    }
    
    /**
     * Variations on the above function removeSpecialChars that allows dashes and underscores
     * @param in input string
     * @return same string, with all special chars removed
     */
    public static String removeSpecialCharsAllowDashUnderscore (String in) {
        return in.replaceAll("[^\\p{L}\\p{Z}\\p{N}\\.\\-\\_]","");
    }
    /**
     * @param base can be null or empty. In this case the function returns ""
     * @param needle can be null or empty. In this case the function returns ""
     * @return Part of the string in base before the needle
     */
    public static String before (String base, String needle) {
        if (StringUtils.isEmpty(base) || StringUtils.isEmpty(needle))
            return "";
        if (! base.contains(needle))
            return "";
        return base.substring(0, base.indexOf(needle));
    }
    
    /**
     * @param base can be null or empty. In this case the function returns ""
     * @param needle can be null or empty. In this case the function returns ""
     * @return Part of the string in base before the needle
     */
    public static String after (String base, String needle) {
        if (StringUtils.isEmpty(base) || StringUtils.isEmpty(needle))
            return "";
        if (! base.contains(needle))
            return "";
        return base.substring(base.indexOf(needle) + needle.length());
    }

    /**
     * Adds a string to an ArrayList of strings, only if it's not there yet
     * @param list
     * @param string
     * @return 
     */
    public static ArrayList<String> addStringIfNotThere (ArrayList<String> list, String string) {
        if (list == null)
            return null;
        if (string == null)
            return list;
        for (String s: list)
            if (s.equals(string))
                return list;
        ArrayList<String> ret = list;
        ret.add(string);
        return ret;
    }
    
    /**
     * Convenience function ('caus I always has to look for it)
     * @param ar
     * @return a String array conversion of the ArrayList array
     */
    public static String[] arrayListToArray (List<String> ar) {
        if (ar == null)
            return new String[]{};
        return ar.toArray(new String[ar.size()]);
    }

    /**
     * Convenience function ('caus I always has to look for it)
     * @param ar
     * @return a ArrayList conversion of the array
     */
    public static ArrayList<String> arrayToArrayList (String [] ar) {
        if (ar == null)
            return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(ar));
    }
    
    /**
     * General utility to convert an input stream to a UTF8 string
     * @param is
     * @return
     * @throws MatrixLibException 
     */
    public static String inputStreamToString (InputStream is) throws MatrixLibException {
        try {
            return IOUtils.toString(is, "UTF-8");
        } catch (IOException ex) {
            throw new MatrixLibException("Can't convert stream");
        }
    }

    public static Integer [] intsFromDots (String version) {
        if (StringUtils.isEmpty(version))
            return null;
        // When we compare a post-2.4 version like 2.3.4.109-cb03e35, we stop the comparison at the dash
        if (version.contains("-"))
            version = before(version, "-");
        String [] parts = StringUtils.split(version, ".");
        Integer[] ret = new Integer[parts.length];
        for (int loop = 0; loop < parts.length; loop++)
            ret [loop] = StringUtil.stringToIntZero(parts[loop]);
        return ret;
    }
    
    private static boolean versionCompare (String base, String compare, boolean equalHigher) {
        if (StringUtils.isEmpty(base) || StringUtils.isEmpty(compare))
            // we don't compare empty strings
            return false;
        if (compare.contains("-") && !base.contains("-"))
            // We compare a pre-2.4 version like 2.3.13115.20969 to a post-2.4 version like 2.3.4.109-cb03e35
            // The version with a dash is higher anyway
            return true;
        if (! compare.contains("-") && base.contains("-"))
            // Inverse order vs previous test
            return false;
        Integer[] baseInt = intsFromDots(base);
        if (baseInt == null)
            // we don't compare against anything
            return false;
        Integer[] compareInt = intsFromDots(compare);
        if (compareInt == null)
            // we don't compare against anything
            return false;
        for (int loop = 0; loop < baseInt.length; loop++) {
            if (loop >= compareInt.length)
                // versions were the same until now, and the base has at least one more digit : compare is lower
                return false;
            if (baseInt[loop] < compareInt[loop])
                return true;
            if (baseInt[loop] > compareInt[loop])
                return false;
        }
        if (baseInt.length < compareInt.length)
            // versions are the same for the base part, but compare has more numbers : compare is bigger
            return true;
        // versions are totally the same
        return equalHigher;
    }

    public static boolean versionBiggerThan (String base, String compare) {
        return versionCompare(base, compare, false);
    }

    public static boolean versionBiggerThanOrEqual (String base, String compare) {
        return versionCompare(base, compare, true);
    }

    /**
     * @param base
     * @param lookup
     * @return true if the lookup value is in the array
     */    
    public static boolean isStringInArray (String [] base, String lookup) {
        if (base == null || lookup == null)
            return false;
        return Arrays.asList(base).contains(lookup);
    }
    
    /**
     * @param list
     * @param lookup
     * @return true if the lookup value is in the array
     */
    public static boolean isStringInArray (List<String> list, String lookup) {
        if (list == null || lookup == null)
            return false;
        return list.contains(lookup);
    }

    /**
     * Removes all characters before or after the IPv4 address in numerical form
     * @param ip
     * @return 
     */
    public static String cleanupIpV4 (String ip) throws Exception {
        String out = "";
        boolean inIt = false;
        boolean after = false;
        for (Character c: ip.toCharArray()) {
            switch (c) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    if (! inIt) 
                        inIt = true;
                    out = out + c;
                    break;
                case '.':
                    if (! inIt) 
                        inIt = true;
                    out = out + c;
                    break;
                default:
                    if (inIt) {
                        inIt = false;
                        after = true;
                    }
                    break;
            }
            if (after)
                break;
        }
        if (StringUtils.isEmpty(out))
            throw new Exception("Not an IPv4 address (empty)");
        if (out.length() < 7)
            throw new Exception("Not an IPv4 address (too short)");
        String [] parts = StringUtils.split(out,".");
        if (parts == null || parts.length != 4)
            throw new Exception("Not an IPv4 address (no 4 groups separated by dots)");
        for (String part: parts) {
            int val = StringUtil.stringToInt(part, -1);
            if (val < 0 || val > 255)
                throw new Exception("Not an IPv4 address (at least one number not in 0..255)");
        }
        // all good
        return out;
    }
    
    /**
     * Replaces in the same place the old value by the new, if found. If multiple values exist, all are replaced.
     * @param base may be null, returns null in this case
     * @param oldValue
     * @param newValue
     * @return 
     */
    public static ArrayList<String> replaceOneValue(ArrayList<String> base, String oldValue, String newValue) {
        if (base == null || ! base.contains(oldValue))
            return base;
        ArrayList<String> ret = new ArrayList<>();
        for (String s: base)
            if (s.equals(oldValue))
                ret.add(newValue);
            else
                ret.add(s);
        return ret;
    }
    
    /**
     * Computes the number of times a substring appears in a string
     * @param text
     * @param needle
     * @return 
     */
    public static int nbOfSubString(String text, String needle) {
        if (text == null || needle == null || StringUtils.isEmpty(needle))
            return 0;
       
        int nb = 0;
        int index = text.indexOf(needle);
        while (index >= 0) {
            nb++;
            index = text.indexOf(needle, index + needle.length());
        }
        return nb;
    }
    
    // This replicates the PHP sha1 so that we can authenticate the same way.
    public static String sha1(String s) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return byteArray2Hex(MessageDigest.getInstance("SHA1").digest(s.getBytes("UTF-8")));
    }

    private static final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    public static String byteArray2Hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (final byte b : bytes) {
            sb.append(HEX[(b & 0xF0) >> 4]);
            sb.append(HEX[b & 0x0F]);
        }
        return sb.toString();
    }

    /**
     * I never know whether to use String.split or StringUtils.split. 
     * They act very differently as soon as the splitter is longer than 1 char
     * This one splits a string with another string acting as delimitor so
     * splitOnString("-x-before-vafter", "-v") gives ["-x-before","after"]
     * @param base
     * @param splitter
     * @return
     */
    public static String[] splitOnString(String base, String splitter) {
        return base.split(splitter);
    }

    public static String[] splitOnDot(String base) {
        return StringUtil.splitOnString(base, "\\.");
    }

    /**
     * Computes an MD5 from a simple string
     * @param data
     * @return
     * @throws DecoderException
     */
    public static String getMD5Hash(String data) throws DecoderException {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(data.getBytes("UTF-8"));
            return byteArray2Hex(hash); // make it printable
        } catch (Exception ex) {
            throw new DecoderException();
        }
    }
}
