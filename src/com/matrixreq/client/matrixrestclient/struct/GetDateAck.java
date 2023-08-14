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

package com.matrixreq.client.matrixrestclient.struct;

public class GetDateAck {

    protected String dateIso8601;
    protected String timeUserFormat;
    protected String dateUserFormat;
    protected String timeCustomerFormat;
    protected String dateCustomerFormat;
    protected String dateformat;
    protected String timeformat;
    protected String timeZone;
    protected String timeZoneDesc;
    protected String customerDateformat;
    protected String customerTimeformat;
    protected String customerTimezone;
    protected String customerTimezoneDesc;

    public String getDateIso8601() {
        return dateIso8601;
    }

    public void setDateIso8601(String dateIso8601) {
        this.dateIso8601 = dateIso8601;
    }

    public String getTimeUserFormat() {
        return timeUserFormat;
    }

    public void setTimeUserFormat(String timeUserFormat) {
        this.timeUserFormat = timeUserFormat;
    }

    public String getDateUserFormat() {
        return dateUserFormat;
    }

    public void setDateUserFormat(String dateUserFormat) {
        this.dateUserFormat = dateUserFormat;
    }

    public String getDateformat() {
        return dateformat;
    }

    public void setDateformat(String dateformat) {
        this.dateformat = dateformat;
    }

    public String getTimeformat() {
        return timeformat;
    }

    public void setTimeformat(String timeformat) {
        this.timeformat = timeformat;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getTimeZoneDesc() {
        return timeZoneDesc;
    }

    public void setTimeZoneDesc(String timeZoneDesc) {
        this.timeZoneDesc = timeZoneDesc;
    }

    public String getCustomerDateformat() {
        return customerDateformat;
    }

    public void setCustomerDateformat(String customerDateformat) {
        this.customerDateformat = customerDateformat;
    }

    public String getCustomerTimeformat() {
        return customerTimeformat;
    }

    public void setCustomerTimeformat(String customerTimeformat) {
        this.customerTimeformat = customerTimeformat;
    }

    public String getCustomerTimezone() {
        return customerTimezone;
    }

    public void setCustomerTimezone(String customerTimezone) {
        this.customerTimezone = customerTimezone;
    }

    public String getCustomerTimezoneDesc() {
        return customerTimezoneDesc;
    }

    public void setCustomerTimezoneDesc(String customerTimezoneDesc) {
        this.customerTimezoneDesc = customerTimezoneDesc;
    }

    public String getTimeCustomerFormat() {
        return timeCustomerFormat;
    }

    public void setTimeCustomerFormat(String timeCustomerFormat) {
        this.timeCustomerFormat = timeCustomerFormat;
    }

    public String getDateCustomerFormat() {
        return dateCustomerFormat;
    }

    public void setDateCustomerFormat(String dateCustomerFormat) {
        this.dateCustomerFormat = dateCustomerFormat;
    }
    
    
}
