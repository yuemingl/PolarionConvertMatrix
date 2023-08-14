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
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.matrixreq.client.matrixrestclient.struct.ExceptionItem;
import com.matrixreq.client.matrixrestclient.struct.ExceptionItemIso;
import com.matrixreq.client.matrixrestclient.struct.ExceptionStatus;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * RAM list of exceptions
 * @author Administrator
 */
public class ExceptionLog implements Appender<ILoggingEvent> {

    // Below are all the overrides needed for the logger appender
    @Override
    public String getName() {
        return "ExceptionLog";
    }

    @Override
    public void doAppend(ILoggingEvent e) throws LogbackException {
        if (e.getLevel() == Level.ERROR) {
            String msg = e.getFormattedMessage();
            addException(msg);
        }
    }

    @Override
    public void setName(String string) {
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isStarted() {
        return true;
    }

    @Override
    public void setContext(Context cntxt) {
    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public void addStatus(ch.qos.logback.core.status.Status status) {
    }

    @Override
    public void addInfo(String string) {
    }

    @Override
    public void addInfo(String string, Throwable thrwbl) {
    }

    @Override
    public void addWarn(String string) {
    }

    @Override
    public void addWarn(String string, Throwable thrwbl) {
    }

    @Override
    public void addError(String string) {
        addExceptionLocal(string);
    }

    @Override
    public void addError(String string, Throwable thrwbl) {
        addExceptionLocal(string, thrwbl);
    }

    @Override
    public void addFilter(Filter<ILoggingEvent> filter) {
    }

    @Override
    public void clearAllFilters() {
    }

    @Override
    public List<Filter<ILoggingEvent>> getCopyOfAttachedFiltersList() {
        return null;
    }

    @Override
    public FilterReply getFilterChainDecision(ILoggingEvent e) {
        return null;
    }

    private final ArrayList<ExceptionItem> localList = new ArrayList<>();
    private static final ExceptionLog INSTANCE = new ExceptionLog();
    private static final Object OBJ = new Object();

    private int totalNumberExceptions = 0;

    public static final String EXTENSION_PREFIX = "EXT|";
    
    public static void addException(String text) {
        if (LoggerConfig.isStoreExceptions() && ! text.startsWith(EXTENSION_PREFIX)) {
            synchronized (OBJ) {
                INSTANCE.addExceptionLocal(text);
            }
        }
    }

    public static ExceptionStatus getStatus () {
        synchronized (OBJ) {
            return INSTANCE.getStatusLocal();
        }
    }
    /**
     *
     * @return
     */
    public static ArrayList<ExceptionItemIso> getExceptionList () {
        synchronized (OBJ) {
            return INSTANCE.getExceptionListLocal();
        }
    }

    private void addExceptionLocal(String text) {
        totalNumberExceptions++;
        if (localList.size() > 100)
            cleanup();
        ExceptionItem item = new ExceptionItem(text);
        localList.add(item);
    }

    private void addExceptionLocal(String text, Throwable t) {
        totalNumberExceptions++;
        if (localList.size() > 100)
            cleanup();
        ExceptionItem item = new ExceptionItem(text + "-" + t.getMessage());
        localList.add(item);
    }
    
    
    private ArrayList<ExceptionItemIso> getExceptionListLocal () {
        cleanup();
        ArrayList <ExceptionItemIso> ret = new ArrayList<>();
        if (localList != null)
            for (ExceptionItem item: localList)
                ret.add(new ExceptionItemIso(DateUtil.formatDateUtcIso8601(item.date), item.text));
        return ret;
    }

    /**
     * warning Not reentrant. Used only to initialize the logger
     * @return 
     */
    public static ExceptionLog getStaticInstance () {
        return INSTANCE;
    }
    
    // Remove all items in the list that are older than X 
    private void cleanup() {
        Date oldest = DateUtil.addMinutesToDate(new Date(), -60);
        // http://stackoverflow.com/questions/223918/iterating-through-a-list-avoiding-concurrentmodificationexception-when-removing
        Iterator <ExceptionItem> iter = localList.iterator();
        while (iter.hasNext()) {
            ExceptionItem item = iter.next();
            if (item.date.before(oldest))
                iter.remove();
        }
    }
    
    private ExceptionStatus getStatusLocal() {
        ExceptionStatus ret = new ExceptionStatus();
        ret.lastHourExceptions = getExceptionListLocal();
        ret.nbExceptionsStillStart = this.totalNumberExceptions;
        return ret;
    }

    
}
