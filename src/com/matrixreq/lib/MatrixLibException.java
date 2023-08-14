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

import java.util.ArrayList;

/**
 *
 * @author Yves
 */
public class MatrixLibException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = -7379897919813961622L;
    public final ArrayList<String> detailsList;

    public MatrixLibException (String message) {
        super(message);
        detailsList = new ArrayList<>();
        detailsList.add(message);
    }

    public MatrixLibException (Exception e) {
        super(e);
        detailsList = new ArrayList<>();
        detailsList.add(e.getMessage());
        for (StackTraceElement s: e.getStackTrace())
            addDetail(s.toString());
    }

    public MatrixLibException (Exception e, String message) {
        this(e);
        addDetail(message);
    }

    public MatrixLibException(String message, Exception ex) {
        super(message, ex);
        detailsList = new ArrayList<>();
    }

    public final void addDetail(String detail) {
        detailsList.add(detail);
    }
}
