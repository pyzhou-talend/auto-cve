package org.talend.cveUtil.deprecated;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.cveUtil.common.Context;
import org.talend.cveUtil.common.RunCMD;

public class SwitchBranch {
    private static final Logger LOG = LoggerFactory.getLogger(SwitchBranch.class);

    public static void main(String[] args) {
//        switchBranch("8.0");
//        switchBranch("7.3");
//        switchBranch("pyzhou/TDI-51152-cve_nimbus-jose-jwt");
//        final SwitchBranch switchBranch = new SwitchBranch();
//        switchBranch.cleanAll();
        System.out.println(Context.tbd_studio_se_components);
        System.out.println(Context.tdi_studio_se_components);
        System.out.println(Context.tdi_studio_ee_components);

    }

    public static void switchBranch(String branch) {
        if("8.0".equals(branch)){
            checkoutAndPull("master", Context.se);
            checkoutAndPull("master", Context.ee);
            checkoutAndPull("master", Context.cloud);
            checkoutAndPull("main", Context.connectors_lib_se);
            checkoutAndPull("maintenance/8.0", Context.component);
            checkoutAndPull("maintenance/8.0", Context.component_ee);
            checkoutAndPull("maintenance/8.0.2", Context.tdi_studio_se);
            checkoutAndPull("maintenance/8.0.2", Context.tdi_studio_ee);
            checkoutAndPull("maintenance/8.0.2", Context.tbd_studio_se);
            checkoutAndPull("maintenance/8.0.2", Context.tcommon_studio_se);
            checkoutAndPull("maintenance/8.0.2", Context.tcommon_studio_ee);
            checkoutAndPull("master", Context.talend_sap_api);
            checkoutAndPull("master", Context.tsap_rfc_server);
        }else if("7.3".equals(branch)){
            checkoutAndPull("maintenance/1.27", Context.se);
            checkoutAndPull("maintenance/1.27", Context.ee);
            checkoutAndPull("maintenance/1.27", Context.cloud);
            checkoutAndPull("maintenance/7.3", Context.connectors_lib_se);
            checkoutAndPull("maintenance/7.3", Context.component);
            checkoutAndPull("maintenance/7.3", Context.component_ee);
            checkoutAndPull("maintenance/7.3", Context.tdi_studio_se);
            checkoutAndPull("maintenance/7.3", Context.tdi_studio_ee);
            checkoutAndPull("maintenance/7.3", Context.tbd_studio_se);
            checkoutAndPull("maintenance/7.3", Context.tcommon_studio_se);
            checkoutAndPull("maintenance/7.3", Context.tcommon_studio_ee);
            checkoutAndPull("maintenance/8.1", Context.talend_sap_api);
            checkoutAndPull("maintenance/7.3", Context.tsap_rfc_server);
        }else if("master".equals(branch)){
            checkoutAndPull("master", Context.se);
            checkoutAndPull("master", Context.ee);
            checkoutAndPull("master", Context.cloud);
            checkoutAndPull("main", Context.connectors_lib_se);
            checkoutAndPull("master", Context.component);
            checkoutAndPull("master", Context.component_ee);
            checkoutAndPull("master", Context.tdi_studio_se);
            checkoutAndPull("master", Context.tdi_studio_ee);
            checkoutAndPull("master", Context.tbd_studio_se);
            checkoutAndPull("master", Context.tcommon_studio_se);
            checkoutAndPull("master", Context.tcommon_studio_ee);
            checkoutAndPull("master", Context.talend_sap_api);
            checkoutAndPull("master", Context.tsap_rfc_server);

        }else {
            checkoutAndPull(branch, Context.se);
            checkoutAndPull(branch, Context.ee);
            checkoutAndPull(branch, Context.cloud);
            checkoutAndPull(branch, Context.connectors_lib_se);
            checkoutAndPull(branch, Context.component);
            checkoutAndPull(branch, Context.component_ee);
            checkoutAndPull(branch, Context.tdi_studio_se);
            checkoutAndPull(branch, Context.tdi_studio_ee);
            checkoutAndPull(branch, Context.tbd_studio_se);
            checkoutAndPull(branch, Context.tcommon_studio_se);
            checkoutAndPull(branch, Context.tcommon_studio_ee);
            checkoutAndPull(branch, Context.talend_sap_api);
            checkoutAndPull(branch, Context.tsap_rfc_server);
        }
    }



    private static void checkoutAndPull(String branch, String path){
        LOG.info("Running checkoutAndPull for {} {}",branch,path);
        RunCMD.runCMD("git reset --hard", path,false);
        RunCMD.runCMD("git fetch",path,false);
        RunCMD.runCMD("git checkout "+branch, path,false);
        RunCMD.runCMD("git pull", path,false);
    }

    private void creatBranch(String branchName, String path){
        RunCMD.runCMD("git checkout -b " +branchName,path);
    }

    private void cleanRepository(String path){
        RunCMD.runCMD("git clean -df",path);
    }

    private void cleanAll(){
        this.cleanRepository(Context.se);
        this.cleanRepository(Context.ee);
        this.cleanRepository(Context.cloud);
        this.cleanRepository(Context.connectors_lib_se);
        this.cleanRepository(Context.component);
        this.cleanRepository(Context.component_ee);
        this.cleanRepository(Context.tdi_studio_se);
        this.cleanRepository(Context.tdi_studio_ee);
        this.cleanRepository(Context.tbd_studio_se);
        this.cleanRepository(Context.tcommon_studio_se);
        this.cleanRepository(Context.tcommon_studio_ee);
        this.cleanRepository(Context.talend_sap_api);
        this.cleanRepository(Context.tsap_rfc_server);
    }


}
