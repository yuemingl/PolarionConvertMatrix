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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Yves
 */
public class DateUtil {
    private static final TimeZone utcTz = TimeZone.getTimeZone("UTC");
    private static Calendar utcCalendar = null;
    private static final DateFormat dfIso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final DateFormat dfIso8601Z = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static final DateFormat dfDigits = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final DateFormat dfYYYY_MM_DD = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat dfMilliDigits = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private static final DateFormat dfDateBuilder = new SimpleDateFormat("yyyy/M/d/H/m/s");
    private static final DateFormat dfDateGmtDisplay = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'GMT'");
    private static final DateFormat dfDateOnlyUS = new SimpleDateFormat("d MMM yyyy");
    
    private static final long ONE_MINUTE_IN_MILLIS = 60000; //millisecs

    private static final String [] acceptedStart = new String [] {
            "Africa/",
            "America/",
            "Asia/",
            "Atlantic/",
            "Australia/",
            "Europe/",
            "Indian/",
            "Pacific/",
        };
    
    /**
     * Formats a date 
     * @param d
     * @return full technical date/time like 2014-09-05T09:05:08.986Z. Returns an empty string if d is null
     */
    public static synchronized String formatDateUtcIso8601 (Date d) {
        // from http://stackoverflow.com/questions/3914404/how-to-get-current-moment-in-iso-8601-format
        if (d == null)
            return "";
        dfIso8601.setTimeZone(utcTz);
        return dfIso8601.format(d);
    }

    /**
     * Formats a date 
     * @param d
     * @return full technical date/time like 2014-09-05T09:05:08.986Z. Returns an empty string if d is null
     */
    public static synchronized String formatDateUtcIso8601z (Date d) {
        // from http://stackoverflow.com/questions/3914404/how-to-get-current-moment-in-iso-8601-format
        if (d == null)
            return "";
        dfIso8601Z.setTimeZone(utcTz);
        return dfIso8601Z.format(d);
    }

    /**
     * Formats a date 
     * @param d
     * @return full technical date/time with no separators like 20140905090508986. Returns an empty string if d is null
     */
    public static synchronized String formatDateMilliDigits (Date d) {
        // from http://stackoverflow.com/questions/3914404/how-to-get-current-moment-in-iso-8601-format
        if (d == null)
            return "";
        dfMilliDigits.setTimeZone(utcTz);
        return dfMilliDigits.format(d);
    }

    /**
     * Formats a date 
     * @param d
     * @return US-like date : "13 Jul 2018". Returns an empty string if d is null
     */
    public static synchronized String formatDateOnlyUS (Date d) {
        // from http://stackoverflow.com/questions/3914404/how-to-get-current-moment-in-iso-8601-format
        if (d == null)
            return "";
        dfDateOnlyUS.setTimeZone(utcTz);
        return dfDateOnlyUS.format(d);
    }
    
    /**
     * Formats a date 
     * @param in
     * @return Date full technical date/time with no separators like 20140905090508986. Returns an empty string if d is null
     * @throws MatrixLibException 
     */
    public static Date parseDateMilliDigits (String in) throws MatrixLibException {
        try {
            Date result = dfMilliDigits.parse(in);
            return result;
        } catch (ParseException ex) {
            throw new MatrixLibException("Can't parse this date");
        }
    }
    
    /**
     * http://stackoverflow.com/questions/9043981/how-to-add-minutes-to-my-date
     * @param d
     * @param nbMinutes
     * @return 
     */
    public static Date addMinutesToDate (Date d, long nbMinutes) {
        long t=d.getTime();
        Date after = new Date(t + (nbMinutes * ONE_MINUTE_IN_MILLIS));        
        return after;
    }

    public static Date addDaysToDate (Date d, long nbDays) {
        return addMinutesToDate (d, nbDays * 24 * 60);
    }
    
    /**
     * @param d
     * @return date format as YYYYMMDDHHMMSS - not processing time zones
     */
    public static synchronized String formatDateAllDigits (Date d) {
        if (d == null)
            return "";
        return dfDigits.format(d).substring(0, 14);
    }

    /**
     * @param d
     * @return date format as YYYY-MM-DD - not processing time zones
     */
    public static synchronized String formatDateYYYY_MM_DD (Date d) {
        if (d == null)
            return "";
        return dfYYYY_MM_DD.format(d);
    }

    
    /**
     * Parses a date in the format YYYY-MM-DD
     * @param in
     * @return the Date object
     * @throws MatrixLibException 
     */
    public static synchronized Date parseDateYYYY_MM_DD (String in) throws MatrixLibException {
        if (StringUtils.isEmpty(in))
            throw new MatrixLibException("Can't parse this date");
        try {
            Date result = dfYYYY_MM_DD.parse(in);
            return result;
        } catch (ParseException ex) {
            throw new MatrixLibException("Can't parse this date");
        }
    }

    /**
     * Parses a date in the Iso8601 format
     * @param in
     * @return the Date object
     * @throws MatrixLibException 
     */
    public static synchronized Date parseDateIso8601 (String in) throws MatrixLibException {
        if (StringUtils.isEmpty(in))
            throw new MatrixLibException("Can't parse this date");
        try {
            Date result = dfIso8601.parse(in);
            return result;
        } catch (ParseException ignore) {
            throw new MatrixLibException("Can't parse this date");
        }
    }

    /**
     * Parses a date in the Iso8601 format
     * @param in
     * @return the Date object
     * @throws MatrixLibException 
     */
    public static synchronized Date parseDateIso8601AllowNoTime (String in) throws MatrixLibException {
        if (StringUtils.isEmpty(in))
            throw new MatrixLibException("Can't parse this date");
        try {
            Date result = dfIso8601.parse(in);
            return result;
        } catch (ParseException ignore) {
            try {
                Date result = dfIso8601.parse(in + "T00:00:00.000Z");
                return result;
            } catch (ParseException ignore2) {
                throw new MatrixLibException("Can't parse this date");
            }
        }
    }

    /**
     * Parses a date in the Iso8601 format with a timezone indicator like -0800
     * @param in
     * @return the Date object
     * @throws MatrixLibException 
     */
    public static synchronized Date parseDateIso8601z (String in) throws MatrixLibException {
        if (StringUtils.isEmpty(in))
            throw new MatrixLibException("Can't parse this date (" + in + ") as ISO8601Z");
        try {
            Date result = dfIso8601Z.parse(in);
            return result;
        } catch (ParseException ignore) {
            throw new MatrixLibException("Can't parse this date (" + in + ") as ISO8601Z");
        }
    }
    
    /**
     * Parses a date in the Iso8601 format with any of the 3 forms above
     * @param in
     * @return the Date object
     * @throws MatrixLibException 
     */
    public static synchronized Date parseDateIso8601Flexible(String in) throws MatrixLibException {
        if (StringUtils.isEmpty(in))
            throw new MatrixLibException("Can't parse an empty date");
        try {
            return parseDateIso8601(in);
        }
        catch (Exception ex) {
            try {
                return parseDateIso8601(in);
            }
            catch (Exception ex2) {
                try {
                    return parseDateIso8601AllowNoTime(in);
                }
                catch (Exception ex3) {
                    throw new MatrixLibException("Can parse that ISO8601 date (" + in + ") with any of the 3 methods");
                }
            }
        }
    }

    /** create a date object from string yyyy/M/d/H/m/s or yyyy/M/d
     *  if the date string is yyyy/M/d, 12 hours are added to avoid time at midnight 
     *  which is a bit ambiguous 
     * @param dateStr: month is between 1 and 12
     * @return 
     * @throws com.matrixreq.lib.MatrixLibException
     */
    public static synchronized Date parseDate(String dateStr) throws MatrixLibException {
        try {
            if ( dateStr.split("\\/").length == 3 ) {
                dateStr = dateStr.concat("/12/0/0");
            }
            Date date = dfDateBuilder.parse(dateStr);
            return date;
                    
        } catch (ParseException pe) {
            throw new MatrixLibException("Can't parse date string");
        }
    }
    
    /**
     * Formats a date to YYYY-MM-DD without correcting for time zones, Reverts to formatDateOnlyNoTz with no argument if the format is wrong
     * @param d
     * @param format
     * @return 
     */
    public static synchronized String formatDateOnlyNoTz (Date d, String format) {   
        try {
            if (d == null)
                return "";
            DateFormat df = new SimpleDateFormat(format);
            return df.format(d);
        }
        catch (Exception ex) {
            return formatDateOnlyNoTz (d);
        }
    }
    
    /**
     * Formats a date to YYYY-MM-DD without correcting for time zones
     * @param d
     * @return 
     */
    public static synchronized String formatDateOnlyNoTz (Date d) {
        return dfYYYY_MM_DD.format(d);
    }
    
    /**
     * Be very careful when using this. Calling this routine with a day >= 28 makes this routine return dates on the next month
     * @param in
     * @return
     * @throws MatrixLibException 
     */
    public static synchronized Date getSameDayNextMonth (Date in) throws MatrixLibException {
        if (in == null)
            throw new MatrixLibException("Can't parse this date");
        
        String d = formatDateOnlyNoTz(in);
        int day = Integer.parseInt(d.substring(8, 10));
        int month = Integer.parseInt(d.substring(5, 7));
        int year = Integer.parseInt(d.substring(0, 4));
        if (month == 12) {
            month = 1;
            year++;
        }
        else 
            month++;
        String next = String.format("%04d-%02d-%02d", year, month, day);
        return parseDateYYYY_MM_DD(next);
    }
    
    /**
     * Get a diff between two dates - I NEVER USED IT
     * From http://stackoverflow.com/questions/1555262/calculating-the-difference-between-two-java-date-instances
     * @param date1 the oldest date
     * @param date2 the newest date
     * @param timeUnit the unit in which you want the diff
     * @return the diff value, in the provided unit
     */
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }    

    /**
     * Creates a UTC Calendar
     * @return 
     */
    public static Calendar getUtcCalendar () {
        if (utcCalendar == null) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeZone(utcTz);
            utcCalendar = calendar;
        }
        return utcCalendar;
    }
    
    /**
     * Creates a Calendar in a given time zone
     * @param timeZoneID
     * @return 
     */
    public static Calendar getCalendar (String timeZoneID) {
        if (utcCalendar == null) {
            Calendar calendar = new GregorianCalendar();
            TimeZone tz = getTimeZone(timeZoneID);
            calendar.setTimeZone(tz);
            utcCalendar = calendar;
        }
        return utcCalendar;
    }

    /**
     * Creates a TimeZone object - code is pretty simple but this allows not to ask ourselves all the time how we do this
     * @param timeZoneID like Europe/Brussels (may contain %2F instead of the /)
     * @return the TimeZone that matches 
     */
    public static TimeZone getTimeZone (String timeZoneID) {
        return TimeZone.getTimeZone(timeZoneID.replace("%2F", "/"));
    }
    
    /**
     * List all accepted time zones denominators
     * @return a Sorted list of all accepted zones (although it starts with the unsorted UTC)
     */
    public static ArrayList<String> getAllTimeZones () {
        ArrayList<String> ret = new ArrayList<>();
        ret.add("UTC");
        String [] availableIDs = TimeZone.getAvailableIDs();
        for (String s: availableIDs)
            for (String t: acceptedStart)
                if (s.startsWith(t))
                    ret.add(s);
        Collections.sort(ret);
        return ret;
    }

    /**
     * List all accepted time zones denominators
     * @return a Sorted list of all accepted zone prefixes (although it starts with the unsorted UTC)
     */
    public static String [] getAllTimeZonePrefixes () {
        return acceptedStart;
    }

    /**
     * Formats a date given as UTC to a given time zone in a given format. Format to ISO8601 if format is not valid
     * @param d date in UTC - may be null
     * @param tz Time zone to apply
     * @param format format to apply
     * @return a user-formatted date string, or an empty string if the date is null
     */
    static public String formatDate(Date d, TimeZone tz, String format) {
        try {
            if (d == null)
                return "";
            DateFormat df = new SimpleDateFormat(format);
            df.setTimeZone(tz);
            return df.format(d);
        }
        catch (Exception ex) {
            return formatDateUtcIso8601(d);
        }
    }

    /**
     * Formats a date given as UTC to a given time zone in a given format. Throws an exception if the format is not valid
     * @param d date in UTC - may be null
     * @param tz Time zone to apply
     * @param format format to apply
     * @return a user-formatted date string, or an empty string if the date is null
     * @throws com.matrixreq.lib.MatrixLibException
     */
    static public String formatDateCheck(Date d, TimeZone tz, String format) throws MatrixLibException {
        try {
            if (d == null)
                return "";
            DateFormat df = new SimpleDateFormat(format);
            df.setTimeZone(tz);
            return df.format(d);
        }
        catch (Exception ex) {
            throw new MatrixLibException("Wrong format");
        }
    }
    
    /**
     * @param year
     * @param week
     * @return Monday of the week's week in year year
     * @throws com.matrixreq.lib.MatrixLibException
     */
    static public Date getMondayOfWeek (int year, int week) throws MatrixLibException {
        Calendar c = Calendar.getInstance();
        Date jan1st = DateUtil.parseDateYYYY_MM_DD(year + "-01-01");
        c.setTime(jan1st);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        int increment = 0;
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                break;
            case Calendar.TUESDAY:
                increment = -1;
                break;
            case Calendar.WEDNESDAY:
                increment = -2;
                break;
            case Calendar.THURSDAY:
                increment = -3;
                break;
            case Calendar.FRIDAY:
                increment = 3;
                break;
            case Calendar.SATURDAY:
                increment = 2;
                break;
            case Calendar.SUNDAY:
                increment = 1;
                break;
        }
        
        Date d = DateUtil.addDaysToDate(DateUtil.parseDateYYYY_MM_DD(year + "-01-01"), (week - 1) * 7 + increment);
        return d;
    }
    
    /**
     * Formats a date 
     * @param d
     * @return full technical date/time like 2014-09-05T09:05:08.986Z. Returns an empty string if d is null
     */
    public static synchronized String formatDateGmtDisplay (Date d) {
        // from http://stackoverflow.com/questions/3914404/how-to-get-current-moment-in-iso-8601-format
        if (d == null)
            return "";
        dfDateGmtDisplay.setTimeZone(utcTz);
        return dfDateGmtDisplay.format(d);
    }

    /**
     * Parses a date in the Iso8601 format
     * @param in
     * @return the Date object
     * @throws MatrixLibException 
     */
    public static synchronized Date parseDateIso8601gmt (String in) throws MatrixLibException {
        if (StringUtils.isEmpty(in))
            throw new MatrixLibException("Can't parse this date");
        try {
            DateFormat dfIso8601gmt = dfIso8601;
            dfIso8601gmt.setTimeZone(utcTz);
            Date result = dfIso8601gmt.parse(in);
            return result;
        } catch (ParseException ignore) {
            throw new MatrixLibException("Can't parse this date");
        }
    }
    
}
