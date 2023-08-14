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

package com.matrixreq.command;

import java.util.ArrayList;
import java.util.List;

public class CommandOutput {

    private List<String> output;
    private List<String> errorOutput;
    private int exitStatus;

    public CommandOutput() {
        this.output = new ArrayList<>();
        this.errorOutput = new ArrayList<>();
    }

    public void addOutputLine(String line) {
        output.add(line);
    }

    public void addErrorLine(String line){
        errorOutput.add(line);
    }

    public int getExitStatus() {
        return exitStatus;
    }

    public void setExitStatus(int exitStatus) {
        this.exitStatus = exitStatus;
    }

    public List<String> getOutput() {
        return output;
    }

    public void setOutput(List<String> output) {
        this.output = output;
    }

    public List<String> getErrorOutput() {
        return errorOutput;
    }

    public void setErrorOutput(List<String> errorOutput) {
        this.errorOutput = errorOutput;
    }
}
