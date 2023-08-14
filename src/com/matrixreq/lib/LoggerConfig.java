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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import java.io.File;
import org.slf4j.LoggerFactory;

/**
 * Logger configurator. Should be called by the first filter's init. And since we don't know the filter order, we can 
 * call this multiple times, it will only be doing something the first time
 * @author Administrator
 */
public class LoggerConfig {
    // We initialize with a valid but default logger
    private static org.slf4j.Logger logger = LoggerFactory.getLogger("logger");

    // This public static can be called at any time, it always returns a valid logger
    public static org.slf4j.Logger getLogger() {
        return logger;
    }
    
    private static boolean configured = false;
    private static boolean storeExceptions = false;

    /**
     * Calls this to start/stop storing exceptions in ExceptionLog
     * @param doStore 
     */
    public static void storeExceptions (boolean doStore) {
        storeExceptions = doStore;
    }

    public static boolean isStoreExceptions() {
        return storeExceptions;
    }
    
    
    /**
     * Configures a log (logback) with time based rollback policy
     * Inspired from http://logback.10977.n7.nabble.com/Basic-example-of-programmatically-setting-the-configuration-td12667.html
     * @param path Folder in which to store the log
     * @param filePrefix will be appended with ".date.log"
     */
    static public void configure (String path, String filePrefix) {
        configure (path, filePrefix, false);
    }
    
    /**
     * Configures a log (logback) with time based rollback policy
     * Inspired from http://logback.10977.n7.nabble.com/Basic-example-of-programmatically-setting-the-configuration-td12667.html
     * @param path Folder in which to store the log
     * @param filePrefix will be appended with ".date.log"
     * @param withClassName
     */
    static public void configure (String path, String filePrefix, boolean withClassName) {
        
        if (configured)
            return;
        
        Logger rootLogger = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME); 

        LoggerContext loggerContext = rootLogger.getLoggerContext(); 
        loggerContext.reset(); 

        RollingFileAppender<ILoggingEvent> rfAppender = new RollingFileAppender<>(); 
        rfAppender.setContext(loggerContext); 

        @SuppressWarnings("rawtypes") 
        TimeBasedRollingPolicy fwRollingPolicy = new TimeBasedRollingPolicy(); 
        fwRollingPolicy.setContext(loggerContext); 
        fwRollingPolicy.setFileNamePattern(path + File.separator + filePrefix + ".%d{yyyy-MM-dd}.log"); 
        fwRollingPolicy.setParent(rfAppender); 
        fwRollingPolicy.start(); 

        PatternLayoutEncoder encoder = new PatternLayoutEncoder(); 
        encoder.setContext(loggerContext); 
        if (withClassName)
            encoder.setPattern("%d{HH:mm:ss.SSS,Europe/Paris} %-5level %class{0}.%M %msg%n"); 
        else
            encoder.setPattern("%d{HH:mm:ss.SSS,Europe/Paris} %-5level %msg%n"); 
        encoder.start(); 

        rfAppender.setEncoder(encoder); 
        rfAppender.setRollingPolicy(fwRollingPolicy); 
        rfAppender.start(); 

        rootLogger.addAppender(rfAppender); 
        rootLogger.addAppender(ExceptionLog.getStaticInstance());

         // generate some output 

        // StatusPrinter.print(loggerContext); 
        
        logger = rootLogger;
        configured = true;
    }
    
    public static void setLevel(Level level) {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger)org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        if (level != root.getLevel()) {
            root.setLevel(Level.INFO);
            LoggerConfig.getLogger().info("Changing log level to " + level);
            root.setLevel(level);
        }
    }
}
