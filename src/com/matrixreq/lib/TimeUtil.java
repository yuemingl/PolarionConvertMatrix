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
import java.util.Date;
import java.util.TimeZone;

/**
 * All date/time utilities return times in the Brussels timezone
 * @author Yves
 */
public class TimeUtil {

    public final static String BRUSSELS_TIME_ZONE = "Europe/Brussels";
    
    /**
     * Checks if 2 times represented as yyyy-MM-dd HH:mm:ss are in the same hour
     * @param time1
     * @param time2
     * @return 
     */
    public static boolean isSameHour(String time1, String time2) {
        return time1.substring(0, 13).equals(time2.substring(0, 13));
    }

    /**
     * Checks if 2 times represented as yyyy-MM-dd HH:mm:ss are in the same day
     * @param time1
     * @param time2
     * @return 
     */
    public static boolean isSameDay(String time1, String time2) {
        return time1.substring(0, 10).equals(time2.substring(0, 10));
    }

    /**
     * Computes the age in days of date string formatted as YYYY-MM-DD
     * @param fileDate
     * @return 
     * @throws com.matrixreq.lib.MatrixLibException 
     */
    public static int ageInDays(String fileDate) throws MatrixLibException {
        try {
            TimeZone tz = TimeZone.getTimeZone(BRUSSELS_TIME_ZONE);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            df.setTimeZone(tz);
            Date question = df.parse(fileDate);
            Date now = new Date();
            long diff = now.getTime() - question.getTime();
            long nbDays = (diff / 1000) / 86400;
            return (int) nbDays;
        } catch (ParseException ignore) {
            throw new MatrixLibException("Wrong date format");
        }
    }

    public static String formatDate (Date d, String timeZone) {
        SimpleDateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dfm.setTimeZone(TimeZone.getTimeZone(timeZone));
        return dfm.format(d);
    }
    
    public static String formatDateBrussels (Date d) {
        return formatDate (d, BRUSSELS_TIME_ZONE);
    }
    
    public static String formatDateUTC (Date d) {
        return formatDate (d, "UTC");
    }    
    
    public static String getCurrentTimeUtcHMS () {
        return getCurrentDateTimeUtc().substring(11);
    }

    public static String getCurrentDateTimeUtc () {
        return formatDateUTC(new Date());
    }

    
}
