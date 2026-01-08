package org.talend.cveUtil;

import static org.talend.cveUtil.common.Context.branch;
import static org.talend.cveUtil.common.Context.connectors_lib_se;
import static org.talend.cveUtil.common.Context.talend_sap_api;
import static org.talend.cveUtil.common.Context.tsap_rfc_server;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.talend.cveUtil.common.Context;
import org.talend.cveUtil.utils.DependencyTree;
import org.talend.cveUtil.utils.GitUtil;
import org.talend.cveUtil.utils.LogFileSearcher;
import org.talend.cveUtil.utils.MvnUtil;
import java.io.File;
import java.io.IOException;

public class RunAll {
    public static void main(String[] args) throws IOException, GitAPIException {
        System.out.println("--------------GitUtil-------------");
        GitUtil gitUtil = new GitUtil();
        gitUtil.switchBranch();

        System.out.println("--------------MvnUtil-------------");

        final MvnUtil mvnUtil = new MvnUtil();
        mvnUtil.cleanAndInstallAll();
        System.out.println("--------------DependencyTree-------------");

        final DependencyTree dependencyTree = new DependencyTree();
        dependencyTree.listTCK();
        dependencyTree.listTcompV0();
//        dependencyTree.listParent();
        dependencyTree.listRepo(connectors_lib_se);
        dependencyTree.listRepo(talend_sap_api);
        dependencyTree.connectorsList.add(new File(tsap_rfc_server));
        dependencyTree.runMvnTree(branch);

        System.out.println("--------------LogFileSearcher-------------");
        final LogFileSearcher logFileSearcher = new LogFileSearcher();
//
//
        String path = Context.forest_folder + Context.branch.replaceAll("/","_");         // 替换为你的目录路径
        logFileSearcher.searchJar(Context.jarName,path,".log");
        logFileSearcher.searchJar(Context.jarName,Context.tdi_studio_se_components,".xml");
        logFileSearcher.searchJar(Context.jarName,Context.tdi_studio_ee_components,".xml");
        logFileSearcher.searchJar(Context.jarName,Context.tdi_studio_ee_components_sap,".xml");
        logFileSearcher.searchJar(Context.jarName,Context.tbd_studio_se_components,".xml");


        gitUtil.createBranchViaResult();
//        gitUtil.commitAndPush();


    }
}
