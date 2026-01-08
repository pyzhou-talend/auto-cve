package org.talend.cveUtil.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class RunCMD {
    private static final Logger log = LoggerFactory.getLogger(RunCMD.class);

    public static void runCMD(String cmd, File path) {
        runCMD(cmd,path,true);
    }

    public static void runCMD(String cmd, String path) {
        int i = runCMD(cmd, new File(path), true);
        System.out.println("Command {"+cmd+"} executed with exit code: " + i);
    }

    public static void runCMD(String cmd, String path, boolean asynchronous) {
        int i = runCMD(cmd, new File(path), asynchronous);
        System.out.println("Command {"+cmd+"} executed with exit code: " + i);
    }
    public static int runCMD(String cmd, File path, boolean asynchronous) {
        Runtime runtime = Runtime.getRuntime();
        String[] env_tSystem_2 = null; //it must be null to get system env
//        final Map<String, String> getenv = System.getenv();

        final Process exec;
        try {
            log.info("Will run command {} in {}","cmd /c " + cmd , path);
            exec = runtime.exec("cmd /c " + cmd, env_tSystem_2, path);
            if(!asynchronous){

                    BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
                    String line;
                log.debug(" None asynchronous Reading normal Input");
                    while ((line = reader.readLine()) != null) {
                        log.trace(line);
                    }

                    BufferedReader readerE = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
                log.debug(" None asynchronous Reading Error Input");
                    while ((line = readerE.readLine()) != null) {
                        log.error(line);
                    }
                return  exec.exitValue();

            }




        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(asynchronous){
            Thread normalOutput = new Thread(() -> {
                try {
                    BufferedReader reader1 = new BufferedReader(
                            new InputStreamReader(exec.getInputStream()));
                    log.info(" asynchronous Reading normal Input");
                    String line1 = "";
                    try {
                        while ((line1 = reader1.readLine()) != null) {
                            log.trace(line1);
                        }
                    } finally {
                        reader1.close();
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            });
            normalOutput.start();

            Thread errorOutput = new Thread(() -> {
                try {
                    BufferedReader reader1 = new BufferedReader(
                            new InputStreamReader(exec.getErrorStream()));
                    log.info("asynchronous Reading Error Input");
                    String line1 = "";
                    try {
                        while ((line1 = reader1.readLine()) != null) {
                            log.error(line1);
                        }
                    } finally {
                        reader1.close();
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            });
            errorOutput.start();
        }

        return 0;


    }
}
