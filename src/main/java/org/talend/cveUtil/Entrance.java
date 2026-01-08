package org.talend.cveUtil;

import static org.talend.cveUtil.common.Context.cloud;
import static org.talend.cveUtil.common.Context.component;
import static org.talend.cveUtil.common.Context.component_ee;
import static org.talend.cveUtil.common.Context.connectors_lib_se;
import static org.talend.cveUtil.common.Context.ee;
import static org.talend.cveUtil.common.Context.se;
import static org.talend.cveUtil.common.Context.talend_sap_api;
import static org.talend.cveUtil.common.Context.tsap_rfc_server;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.cveUtil.common.Context;
import org.talend.cveUtil.utils.DependencyTree;
import org.talend.cveUtil.utils.GitUtil;
import org.talend.cveUtil.utils.LogFileSearcher;
import org.talend.cveUtil.utils.MvnUtil;
import java.io.File;
import java.io.IOException;

public class Entrance {


    private static final Logger log = LoggerFactory.getLogger(LogFileSearcher.class);

    public static void main(String[] args) throws IOException, GitAPIException {
        String branch = "";
        System.out.println("Util start:");
        if(args.length > 2){
            String options = args[0];
            if("GitUtil".equalsIgnoreCase(options)){
                String goal = args[1];
                final GitUtil gitUtil = new GitUtil();
                if("cleanAll".equalsIgnoreCase(goal)){
                    gitUtil.cleanAll();
                } else if ("switchBranch".equalsIgnoreCase(goal)) {
                    gitUtil.switchBranch();
                } else if ("createBranchViaResult".equalsIgnoreCase(goal)) {
                    gitUtil.createBranchViaResult();
                } else if ("commitListRepos".equalsIgnoreCase(goal)) {
                    gitUtil.commitListRepos();
                } else if ("PushListRepos".equalsIgnoreCase(goal)) {
                    gitUtil.PushListRepos();
                } else if ("cherryPick".equalsIgnoreCase(goal)) {
                    gitUtil.cherryPick();
                } else {
                    printInfo();
                }
            } else if ("DependencyTree".equalsIgnoreCase(options)) {
                String goal = args[1];
                Context.jarName = args[2];
                boolean noSplit = false;
                boolean needClean = false;
                boolean noBuild = false;
                boolean switchBranch = false;
                boolean generateResult = false;

                for (int i = 3; i < args.length; i++) {
                    String arg = args[i];

                    if("-ns".equals(arg)||"--no-split".equals(arg)){
                        noSplit = true;

                    } else if ("-c".equals(arg)||"--clean".equals(arg)) {
                        needClean = true;
                    } else if ("-nb".equals(arg)||"--no-build".equals(arg)) {
                        noBuild = true;
                    } else if ("-s".equals(arg)||"--switch".equals(arg)) {
                        switchBranch = true;
                        if(i+1 <args.length){
                            branch = args[i+1];
                        } else {
                            System.err.println("-switch needs a branch name");
                            printInfo();
                            return;
                        }


                    } else if ("-g".equals(arg)||"--generate".equals(arg)) {
                        generateResult = true;
                    }
                }

                DependencyTree dependencyTree = new DependencyTree();
                GitUtil gitUtil = new GitUtil();

                if(needClean){
                    log.info("### Will clean the Repositories");
                    if("tck".equalsIgnoreCase(goal)){
                        gitUtil.cleanRepository(se);
                        gitUtil.cleanRepository(ee);
                        gitUtil.cleanRepository(cloud);
                    } else if ("tcompV0".equalsIgnoreCase(goal)) {
                        gitUtil.cleanRepository(component);
                        gitUtil.cleanRepository(component_ee);
                    } else if ("others".equalsIgnoreCase(goal)){
                        gitUtil.cleanRepository(talend_sap_api);
                        gitUtil.cleanRepository(tsap_rfc_server);
                        gitUtil.cleanRepository(connectors_lib_se);
                    } else if ("all".equalsIgnoreCase(goal)){
                        gitUtil.cleanRepository(se);
                        gitUtil.cleanRepository(ee);
                        gitUtil.cleanRepository(cloud);
                        gitUtil.cleanRepository(component);
                        gitUtil.cleanRepository(component_ee);
                        gitUtil.cleanRepository(talend_sap_api);
                        gitUtil.cleanRepository(tsap_rfc_server);
                        gitUtil.cleanRepository(connectors_lib_se);
                    }
                }

                if(switchBranch){
                    log.info("### Will switch to branch <{}>",branch);
                    if(branch.startsWith("-")){
                            System.err.println("Invalid branch name: "+branch);
                            printInfo();
                            return;
                    }else {
                        Context.branch = branch;
                        gitUtil.switchBranch(gitUtil.normalizeBranch(branch));

                    }
                }

                if(!noBuild){
                    log.info("### Will maven build the Repositories");
                    final String buildLogPath = Context.result_folder + Context.jarName + "/mvnBuildLog/";
                    final File file = new File(buildLogPath);
                    if(!file.exists()){
                        file.mkdirs();
                    }
                    final MvnUtil mvnUtil = new MvnUtil();
                    if("tck".equalsIgnoreCase(goal)){
                        mvnUtil.cleanInstall(se,true,buildLogPath + "connectors-se.log");
                        mvnUtil.cleanInstall(ee,true,buildLogPath + "connectors-ee.log");
                        mvnUtil.cleanInstall(cloud,true,buildLogPath + "cloud-components.log");
                    } else if ("tcompV0".equalsIgnoreCase(goal)) {
                        mvnUtil.cleanInstall(component,true,buildLogPath + "components.log");
                        mvnUtil.cleanInstall(component_ee,true,buildLogPath + "components-ee.log");
                    } else if ("others".equalsIgnoreCase(goal)){
                        mvnUtil.cleanInstall(talend_sap_api,true,buildLogPath + "talend_sap_api.log");
                        mvnUtil.cleanInstall(tsap_rfc_server,true,buildLogPath + "tsap_rfc_server.log");
                        mvnUtil.cleanInstall(connectors_lib_se,true,buildLogPath + "connectors_lib_se.log");
                    } else if ("all".equalsIgnoreCase(goal)){
                        mvnUtil.cleanInstall(se,true,buildLogPath + "connectors-se.log");
                        mvnUtil.cleanInstall(ee,true,buildLogPath + "connectors-ee.log");
                        mvnUtil.cleanInstall(cloud,true,buildLogPath + "cloud-components.log");
                        mvnUtil.cleanInstall(component,true,buildLogPath + "components.log");
                        mvnUtil.cleanInstall(component_ee,true,buildLogPath + "components-ee.log");
                        mvnUtil.cleanInstall(talend_sap_api,true,buildLogPath + "talend_sap_api.log");
                        mvnUtil.cleanInstall(tsap_rfc_server,true,buildLogPath + "tsap_rfc_server.log");
                        mvnUtil.cleanInstall(connectors_lib_se,true,buildLogPath + "connectors_lib_se.log");
                    }
                }

                if(noSplit){
                    log.info("### Will run mvn dependency:tree without split");
                    if("tck".equalsIgnoreCase(goal)){
                        dependencyTree.connectorsList.add(new File(Context.se));
                        dependencyTree.connectorsList.add(new File(Context.ee));
                        dependencyTree.connectorsList.add(new File(Context.cloud));
                    } else if ("tcompV0".equalsIgnoreCase(goal)) {
                        dependencyTree.connectorsList.add(new File(Context.component));
                        dependencyTree.connectorsList.add(new File(Context.component_ee));
                    } else if ("others".equalsIgnoreCase(goal)){
                        dependencyTree.connectorsList.add(new File(Context.connectors_lib_se));
                        dependencyTree.connectorsList.add(new File(Context.talend_sap_api));
                        dependencyTree.connectorsList.add(new File(Context.tsap_rfc_server));
                    } else if ("all".equalsIgnoreCase(goal)){
                        dependencyTree.connectorsList.add(new File(Context.se));
                        dependencyTree.connectorsList.add(new File(Context.ee));
                        dependencyTree.connectorsList.add(new File(Context.cloud));
                        dependencyTree.connectorsList.add(new File(Context.component));
                        dependencyTree.connectorsList.add(new File(Context.component_ee));
                        dependencyTree.connectorsList.add(new File(Context.connectors_lib_se));
                        dependencyTree.connectorsList.add(new File(Context.talend_sap_api));
                        dependencyTree.connectorsList.add(new File(Context.tsap_rfc_server));
                    }
                    dependencyTree.runMvnTree("no_split");

                }else {
                    log.info("### Will run mvn dependency:tree into split result");
                    if("tck".equalsIgnoreCase(goal)){
                        dependencyTree.listTCK();
                    } else if ("tcompV0".equalsIgnoreCase(goal)) {
                        dependencyTree.listTcompV0();
                    } else if ("others".equalsIgnoreCase(goal)){
                        dependencyTree.listRepo(Context.connectors_lib_se);
                        dependencyTree.listRepo(Context.talend_sap_api);
                        dependencyTree.connectorsList.add(new File(Context.tsap_rfc_server));
                    } else if ("all".equalsIgnoreCase(goal)){
                        dependencyTree.listTCK();
                        dependencyTree.listTcompV0();
                        dependencyTree.listRepo(Context.talend_sap_api);
                        dependencyTree.connectorsList.add(new File(Context.tsap_rfc_server));
                    }
                    dependencyTree.runMvnTree(Context.branch);
                }

                if(generateResult){
                    log.info("### Will generate search result");
                    final LogFileSearcher logFileSearcher = new LogFileSearcher();
                    String path = Context.forest_folder + Context.branch.replaceAll("/","_");         // 替换为你的目录路径
                    logFileSearcher.searchJar(Context.jarName,path,".log");
                    logFileSearcher.searchJar(Context.jarName,Context.tdi_studio_se_components,".xml");
                    logFileSearcher.searchJar(Context.jarName,Context.tdi_studio_ee_components,".xml");
                    logFileSearcher.searchJar(Context.jarName,Context.tdi_studio_ee_components_sap,".xml");
                    logFileSearcher.searchJar(Context.jarName,Context.tbd_studio_se_components,".xml");
//                    logFileSearcher.searchJar(Context.jarName,Context.tcommon_studio_se_plugins + "org.talend.designer.maven.repo.tck/","pom.xml");
//                    logFileSearcher.searchJar(Context.jarName,Context.tcommon_studio_se_plugins + "org.talend.designer.maven.repo.tcksdk/","pom.xml");
//                    logFileSearcher.searchJar(Context.jarName,Context.tcommon_studio_se_plugins + "org.talend.designer.maven.repo.tcompv0/","pom.xml");
//                    logFileSearcher.searchJar(Context.jarName,Context.tcommon_studio_se_plugins + "org.talend.designer.maven.tos/","pom.xml");
                }




            }else if ("MvnUtil".equalsIgnoreCase(options)) {

            }else if ("PatchUtil".equalsIgnoreCase(options)) {

            }else  {
                printInfo();

            }

        }else {
            printInfo();
        }


    }


    public static void printInfo(){
//        System.out.println("please use the correct command:");
//        System.out.println("Usage: GitUtil/MvnUtil/PatchUtil/DependencyTree args");
//        System.out.println("GitUtil cleanAll/switchBranch/createBranchViaResult/commitListRepos/PushListRepos/cherryPick");
        System.out.println("DependencyTree tck/tcompV0/others/all jarGroupID [-options]");
        System.out.println("[Command]:");
        System.out.println("others means connectors_lib_se tsap_rfc_server talend_sap_api");
        System.out.println("jarGroupID: please enter the maven group id of the cve jar");


        System.out.println("[Options]:");
        System.out.println("-ns --no-split  Do not split the maven tree result into split component file.(Not recommended)");
        System.out.println("-c  --clean  Clean the repository before run. Careful that all change will be dismiss");
        System.out.println("-nb --no-build  Do not run maven build before maven dependency tree.(Not recommended)");
        System.out.println("-s --switch  [branchName] Switch to the target branch before run. Note: Repositories will be cleaned before switch.");
        System.out.println("-g --generate  Generate search result into {result_folder}/search_result.log");


//        System.out.println("MvnUtil cleanAndInstallAll/repository");
//        System.out.println("PatchUtil buildPatch");

    }
}
