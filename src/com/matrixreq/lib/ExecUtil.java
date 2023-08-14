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

import com.matrixreq.command.CommandOutput;
import org.slf4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Yves
 */
public class ExecUtil {

    static class ReentrantStringList {
        private final ArrayList<String> list = new ArrayList<>();
        private final Object syncObj = new Object();
        
        public void add(String s) {
            synchronized (syncObj) {
                list.add(s);
            }
        }

        public ArrayList<String> getList() {
            return list;
        }

    }
    
    /**
     * http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
     * @author Yves
     */
    static class StreamGobbler extends Thread
    {
        InputStream is;
        String prefix;
        ReentrantStringList list;

        StreamGobbler(InputStream is, String prefix, ReentrantStringList list)
        {
            this.is = is;
            this.prefix = prefix;
            this.list = list;
        }

        @Override
        public void run()
        {
            try
            {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null)
                    list.add(prefix + line);    
            } catch (IOException ioe) {
                ioe.printStackTrace();  
            }
        }
    }
    
    static public class ExecException extends MatrixLibException {
        /**
         *
         */
        private static final long serialVersionUID = 7395051711857975777L;

        public ExecException(String message) {
            super(message);
            addDetail("Exec exception");
        }

        public ExecException(String message, Exception ex) {
            super(message, ex);
        }
    }

    public static ArrayList<String> exec (String command) throws ExecException  {
        try {
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            ArrayList<String> output = new ArrayList<>();
            String line;			
            while ((line = reader.readLine())!= null) 
                output.add(line);
            
            reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = reader.readLine())!= null) 
                output.add("E|"+ line);            
            
            return output;
        } catch (IOException ex) {
            throw new ExecException("IOException while running command", ex);
        } catch (InterruptedException ex) {
            throw new ExecException("InterruptedException while running command", ex);
        }
    }

    public static ArrayList<String> exec (String command, String[] env, File directory) throws ExecException  {
        try {
            Process p = Runtime.getRuntime().exec(command, env, directory);
            p.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            ArrayList<String> output = new ArrayList<>();
            String line;
            while ((line = reader.readLine())!= null)
                output.add(line);

            reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = reader.readLine())!= null)
                output.add("E|"+ line);

            return output;
        } catch (IOException ex) {
            throw new ExecException("IOException while running command", ex);
        } catch (InterruptedException ex) {
            throw new ExecException("InterruptedException while running command", ex);
        }
    }

    public static CommandOutput exec(Map<String, String> envp, String... cmd) throws ExecException {
        CommandOutput commandOutput = new CommandOutput();
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command(cmd);
            pb.environment().putAll(envp);
            Process p = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                commandOutput.addOutputLine(line);
            }

            reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = reader.readLine()) != null) {
                commandOutput.addErrorLine(line);
            }
            int exitStatus = p.waitFor();
            commandOutput.setExitStatus(exitStatus);
            return commandOutput;
        } catch (IOException ex) {
            throw new ExecException("IOException while running command", ex);
        } catch (InterruptedException ex) {
            throw new ExecException("InterruptedException while running command", ex);
        }
    }

    /**
     * Returns the exec return value
     * @param command
     * @return
     * @throws com.matrixreq.lib.ExecUtil.ExecException 
     */
    public static int execInt (String command) throws ExecException  {
        try {
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
            return p.exitValue();
        } catch (IOException ex) {
            throw new ExecException("IOException while running command");
        } catch (InterruptedException ex) {
            throw new ExecException("InterruptedException while running command");
        }
    }

    public static class ExecResult {

        private int intResult;
        private final ArrayList<String> stdout;
        private final ArrayList<String> stderr;
        
        public ExecResult() {
            intResult = -1;
            stdout = new ArrayList<>();
            stderr = new ArrayList<>();
        }
        public void addStdout(String s) {
            stdout.add(s);
        }
        public void addStderr(String s) {
            stderr.add(s);
        }
        public void setIntResult(int intResult) {
            this.intResult = intResult;
        }

        public int getIntResult() {
            return intResult;
        }

        public ArrayList<String> getStdout() {
            return stdout;
        }

        public ArrayList<String> getStderr() {
            return stderr;
        }
        
    }

    /**
     * Returns the exec return value
     * @param command
     * @return
     * @throws com.matrixreq.lib.ExecUtil.ExecException 
     */
    public static ExecResult execDetails (String command) throws ExecException  {
        try {
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
            ExecResult res = new ExecResult();
            res.setIntResult(p.exitValue());
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;			
            while ((line = reader.readLine())!= null)
                res.addStdout(line);
            reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = reader.readLine())!= null)
                res.addStderr(line);
            return res;
        } catch (IOException ex) {
            throw new ExecException("IOException while running command");
        } catch (InterruptedException ex) {
            throw new ExecException("InterruptedException while running command");
        }
    }
    

    /**
     * Executes a command with the binary and all arguments in elements of an array
     * @param command
     * @return 
     * @throws com.matrixreq.lib.ExecUtil.ExecException 
     */
    public static ArrayList<String> exec (String [] command) throws ExecException  {
        return exec (command, null);
    }

    /**
     * Executes a command with the binary and all arguments in elements of an array - and output results as log info
     * See  http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
     * for explanations on why we need to start processing the streams before to wait for command to finish
     * @param directory may be null
     * @param command
     * @param logger may be null
     * @return
     * @throws com.matrixreq.lib.ExecUtil.ExecException
     */
    public static ArrayList<String> execInDirectory (String directory, String [] command, Logger logger) throws ExecException  {
        try {
            String toLog = "About to run command: ";
            for (String s: command) 
                toLog += " " + s;
            if (directory != null) {
                toLog += " in directory " + directory;
            }
            if (logger != null)
                logger.info (toLog);
            
            ReentrantStringList list = new ReentrantStringList();
            
            Process p;
            if (directory == null)
                p = Runtime.getRuntime().exec(command);
            else
                p = Runtime.getRuntime().exec(command, null, new File(directory));
            
            // any error message?
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "E|", list);
            
            // any output?
            StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "", list);
                
            // kick them off
            errorGobbler.start();
            outputGobbler.start();      
            
            // Now if we rush to use the output it may be that the last output is still processed.
            // Let's wait for the 2 other processes to wait as well
            errorGobbler.join();
            outputGobbler.join();
            
            // Check error 
            // int exitVal = p.waitFor();
            // System.out.println("ExitValue: " + exitVal);                

            ArrayList<String> output = list.getList();
            if (logger != null) {
                for (String out: output) 
                    if (out.startsWith("E|"))
                        logger.error(" ... " + out);
                    else
                        logger.info(" ... " + out);
                logger.info ("cmd end - " + command [0]);
            }
            p.waitFor();
            return output;
        } catch (IOException ex) {
            if (logger != null)
                logger.error(ex.getMessage());
            throw new ExecException("IOException while running command");
        } catch (InterruptedException ex) {
            if (logger != null)
                logger.error(ex.getMessage());
            throw new ExecException("InterruptedException while running command");
        } catch (Throwable t) {
            throw new ExecException("Throwable while running command : " + t.getMessage());
        }
    }

    public static ArrayList<String> execInDirectory (String directory, String [] command) throws ExecException {
        return execInDirectory(directory, command, null);
    }

    /**
     * Executes a command with the binary and all arguments in elements of an array - and output results as log info
     * See  http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
     * for explanations on why we need to start processing the streams before to wait for command to finish
     * @param command
     * @param logger may be null
     * @return
     * @throws com.matrixreq.lib.ExecUtil.ExecException
     */
    public static ArrayList<String> exec (String [] command, Logger logger) throws ExecException {
        return execInDirectory(null, command, logger);
    }
    
    /**
     * Executes a script of commands, each being an array compatible with the call to the exec with String []
     * @param script
     * @return
     * @throws com.matrixreq.lib.ExecUtil.ExecException
     */
    public static ArrayList<String> exec (String [][] script) throws ExecException  {
        ArrayList <String> res = new ArrayList <>();
        for (String [] oneCommand: script) {
            ArrayList<String> res2 = ExecUtil.exec(oneCommand);
            res.addAll(res2);
        }
        return res;
    }

    /**
     * Executes a script of commands, each being an array compatible with the call to the exec with String [] with logging of everything
     * @param script
     * @param logger
     * @return
     * @throws ExecException 
     */
    public static ArrayList<String> exec (String [][] script, Logger logger) throws ExecException  {
        ArrayList <String> res = new ArrayList <>();
        for (String [] oneCommand: script) {
            ArrayList<String> res2 = ExecUtil.exec(oneCommand, logger);
            res.addAll(res2);
        }
        return res;
    }

    /**
     * Local class to allow running an exec in a 2nd thread
     */
    private static class DetachThread implements Runnable {
        private final String[] command;
        private final Logger logger;
        DetachThread (String [] command, Logger logger) {
            this.command = command;
            this.logger = logger;
        }
        @Override
        public void run() {
            try {
                exec(command, logger);
            } catch (ExecException ex) {
                logger.error(ex.getMessage());
            }
        }
    }
    
    /**
     * Executes a command with the binary and all arguments in elements of an array in a 2nd thread
     * @param command
     * @param launchLogger (may be null): to have the launch arguments in the log
     * @param execLogger (may be null): to have the command execution in the log
     * @throws ExecException 
     */
    public static void execDetach (String [] command, Logger launchLogger, Logger execLogger) throws ExecException  {
        String toLog = "About to run command in 2nd thread: ";
        for (String s: command) 
            toLog += " " + s;
        if (launchLogger != null)
            launchLogger.info (toLog);
        DetachThread thread = new DetachThread(command, execLogger);
        new Thread(thread).start();
    }

    public static String getHostName() {
        try {
            if (FileUtil.runOnWindows())
                return System.getenv("COMPUTERNAME");
            return StringUtil.joinArrayWith(exec("hostname"), "|");
        } catch (ExecException ex) {
            return "?";
        }
    }
}
