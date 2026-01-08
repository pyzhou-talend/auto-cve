package org.talend.cveUtil.common;

public class Context {



    public static  String branch = ConfigManager.getInstance().getProperty("branch");
    public static String jarName = ConfigManager.getInstance().getProperty("cve_jar_artifactId");
    public static final String forest_folder = ConfigManager.getInstance().getProperty("forest_folder");
    public static final String result_folder =  ConfigManager.getInstance().getProperty("result_folder");
    public static final String git_repository = ConfigManager.getInstance().getProperty("git_repository");
    public static final String git_your_name = ConfigManager.getInstance().getProperty("git_your_name");
    public static final String git_user = ConfigManager.getInstance().getProperty("git_user");
    public static final String git_token = ConfigManager.getInstance().getProperty("git_token");
    public final static String se = git_repository+"connectors-se";
    public static final String ee = git_repository+"connectors-ee";
    public static final String cloud = git_repository+"cloud-components";
    public static final String component = git_repository+"components";
    public static final String component_ee = git_repository+"components-ee";
    public static final String tdi_studio_se = git_repository+"tdi-studio-se";
    public static final String tdi_studio_ee = git_repository+"tdi-studio-ee";
    public static final String tbd_studio_se = git_repository+"tbd-studio-se";
    public static final String tdi_studio_se_components = git_repository+"tdi-studio-se/main/plugins/org.talend.designer.components.localprovider/components/";
    public static final String tdi_studio_ee_components = git_repository+"tdi-studio-ee/main/plugins/org.talend.designer.components.tisprovider/components/";
    public static final String tdi_studio_ee_components_sap = git_repository+"tdi-studio-ee/main/plugins/org.talend.designer.components.tisprovider/components_dynamic/";
    public static final String tbd_studio_se_components = git_repository+"tbd-studio-se/main/plugins/org.talend.designer.components.bigdata/components/";
//    public static final String tbd_studio_se_components_plugins = git_repository+"tbd-studio-se/main/plugins/org.talend.designer.components.bigdata/components/";
    public static final String tcommon_studio_se_plugins = git_repository+"tcommon-studio-se/main/plugins/";
    public static final String tcommon_studio_ee_plugins = git_repository+"tcommon-studio-ee/main/plugins/";
    public static final String tcommon_studio_se = git_repository+"tcommon-studio-se";
    public static final String tcommon_studio_ee = git_repository+"tcommon-studio-ee";
    public static final String connectors_lib_se = git_repository+"connectors-lib-se";

    public static final String talend_sap_api = git_repository+"talend-sap-api";
    public static final String tsap_rfc_server = git_repository+"tsap-rfc-server";



}
