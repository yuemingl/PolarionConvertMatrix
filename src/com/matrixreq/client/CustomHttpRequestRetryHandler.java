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

package com.matrixreq.client;

import com.matrixreq.lib.LoggerConfig;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class CustomHttpRequestRetryHandler implements HttpRequestRetryHandler {

    private static final int DELAY_TO_RETRY_SECONDS = 5;
    private final Logger logger = LoggerConfig.getLogger();
    private final int maxRetriesCount;
    private final int maxDurationMinutes;

    public CustomHttpRequestRetryHandler(int maxRetriesCount, int maxDurationMinutes) {
        this.maxRetriesCount = maxRetriesCount;
        this.maxDurationMinutes = maxDurationMinutes;
    }

    /**
     * Triggered only in case of exception
     *
     * @param exception      The cause
     * @param executionCount Retry attempt sequence number
     * @param context        {@link HttpContext}
     * @return True if we want to retry request, false otherwise
     */
    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {

        Throwable rootCause = ExceptionUtils.getRootCause(exception);
        logger.warn("request attempt failed, root cause", exception);

        if (executionCount >= maxRetriesCount) {
            logger.warn("request failed after {} retries in {} minute(s)", executionCount, maxDurationMinutes);
            return false;
        } else if (rootCause instanceof SocketTimeoutException || exception instanceof SocketTimeoutException) {
            return true;
        } else if (rootCause instanceof SocketException
                || rootCause instanceof InterruptedIOException
                || rootCause instanceof SSLException
                || exception instanceof SocketException
                || exception instanceof InterruptedIOException
                || exception instanceof SSLException) {
            try {
                Thread.sleep(DELAY_TO_RETRY_SECONDS * 1000);
            } catch (InterruptedException e) {
                logger.debug("Interrupted exception", e);
            }
            return true;
        } else {
            return false;
        }
    }
}
