package org.talend.cveUtil.utils;

import static org.talend.cveUtil.common.Context.component;

import org.talend.cveUtil.common.Context;
import org.talend.cveUtil.common.RunCMD;

public class MvnUtil {
    public static void main(String[] args) {
        final MvnUtil mvnUtil = new MvnUtil();
//        mvnUtil.cleanAndInstallAll();

//        mvnUtil.cleanInstall(component,true,false);
//        mvnUtil.cleanInstall(component_ee,true,false);
//        mvnUtil.cleanInstall(se,true,false);
//        mvnUtil.cleanInstall(ee,true,false);
//        mvnUtil.cleanInstall(cloud,true,false);
//        mvnUtil.cleanInstall(talend_sap_api,true,false);
//        mvnUtil.cleanInstall(tsap_rfc_server,true,false);
//        mvnUtil.cleanInstall(connectors_lib_se,true,false);
    }

    public void cleanInstall(String path,boolean skipTests, boolean asynchronous){
        RunCMD.runCMD("mvn clean install" + (skipTests?" -DskipTests":""),path,asynchronous);
    }
    public void cleanInstall(String path,boolean skipTests){
        RunCMD.runCMD("mvn clean install" + (skipTests?" -DskipTests":""),path);
    }

    public void cleanInstall(String path,boolean skipTests, String logFilePath){
        RunCMD.runCMD("mvn clean install" + (skipTests?" -DskipTests":"") + " > " + logFilePath,path);
    }

    public void cleanInstall(String path){
        cleanInstall(path,true);
    }
    
    public void cleanAndInstallAll(){
        cleanInstall(component);
        cleanInstall(Context.component_ee);
        cleanInstall(Context.se);
        cleanInstall(Context.ee);
        cleanInstall(Context.cloud);
        cleanInstall(Context.talend_sap_api);
        cleanInstall(Context.tsap_rfc_server);
    }
}
