package org.talend.cveUtil.utils;

import static org.talend.cveUtil.common.Context.branch;
import static org.talend.cveUtil.common.Context.cloud;
import static org.talend.cveUtil.common.Context.component;
import static org.talend.cveUtil.common.Context.component_ee;
import static org.talend.cveUtil.common.Context.connectors_lib_se;
import static org.talend.cveUtil.common.Context.ee;
import static org.talend.cveUtil.common.Context.git_repository;
import static org.talend.cveUtil.common.Context.git_token;
import static org.talend.cveUtil.common.Context.git_user;
import static org.talend.cveUtil.common.Context.jarName;
import static org.talend.cveUtil.common.Context.result_folder;
import static org.talend.cveUtil.common.Context.se;
import static org.talend.cveUtil.common.Context.talend_sap_api;
import static org.talend.cveUtil.common.Context.tbd_studio_se;
import static org.talend.cveUtil.common.Context.tcommon_studio_ee;
import static org.talend.cveUtil.common.Context.tcommon_studio_se;
import static org.talend.cveUtil.common.Context.tdi_studio_ee;
import static org.talend.cveUtil.common.Context.tdi_studio_se;
import static org.talend.cveUtil.common.Context.tsap_rfc_server;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.EmptyCommitException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefDatabase;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.cveUtil.common.ConfigManager;
import org.talend.cveUtil.common.Context;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GitUtil {
    private static final Logger log = LoggerFactory.getLogger(GitUtil.class);
    private UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(git_user, git_token);

    public static void main(String[] args) throws GitAPIException, IOException {
        GitUtil gitUtil = new GitUtil();
        gitUtil.switchBranch();
//        gitUtil.cleanAll();

//        gitUtil.createBranchViaResult();
//        gitUtil.commitListRepos();
//        gitUtil.PushListRepos();
//        gitUtil.cherryPick();
    }

    /*
    依据search_result.log的结果，提交文件列出来所有repository中的所有修改
    所以尽量在mvn clean install前修改并提交pom的修改，否则会因为格式问题有大量无效修改
     */
    public void commitListRepos() throws IOException, GitAPIException {
        final List<String> repos = getRepos();
        //pyzhou/TDI-51498_dnsjava_cve_8.0
        final String jiraId = ConfigManager.getInstance().getProperty("jira_id");
        final String devBranch = Context.git_your_name +"/" + jiraId + "_" + jarName + "_cve_" + normalizeBranch();
        for (String repo : repos) {
            log.info("About to commit pom files in dev branch {} in repository: {}", devBranch, repo);

            try (Git git = Git.open(new File(git_repository + repo))) {
                git.add().addFilepattern(".").call();

                try {
                    final RevCommit revCommit = git.commit().setMessage("fix(" + jiraId + "): " + ConfigManager.getInstance().getProperty("full_jar_name_and_version")).setAllowEmpty(false).call();
                    log.info("Commit ID: {}",revCommit);
                    final File commitList = new File(Context.result_folder + Context.jarName + "/commitList.txt");
                    String line = repo + ":" + revCommit.getId() + "\n";
                    FileUtils.write(commitList, line, true);
                } catch (EmptyCommitException e) {
                    e.printStackTrace();
                }


            }
        }
    }

/*
提交dev branch到云端
maintenance 开头的branch不能提交，会检查并报错
 */
    public void PushListRepos() throws IOException, GitAPIException {
        final List<String> repos = getRepos();
        //pyzhou/TDI-51498_dnsjava_cve_8.0
        final String jiraId = ConfigManager.getInstance().getProperty("jira_id");
        final String devBranch = Context.git_your_name +"/" + jiraId + "_" + jarName + "_cve_" + normalizeBranch();
        for (String repo : repos) {
            log.info("About to push dev branch {} in repository: {}", devBranch, repo);

            try (Git git = Git.open(new File(git_repository + repo))) {
                //https://github.com/Talend/cloud-components/compare/pyzhou/TDI-51498_dnsjava_cve_8.0?expand=1
                checkBranchStatus(git);
                final Iterable<PushResult> origin = git.push().setCredentialsProvider(provider).setRemote("origin").call();
                log.info("push done.");

            }
            log.info("PR: https://github.com/Talend/{}/compare/{}...{}?expand=1", repo, getBaseBranch(repo), devBranch);
        }
    }

    /*
    依据search_result.log的结果来创建dev branch
     */

    public void createBranchViaResult() throws IOException, GitAPIException {
        final List<String> repos = getRepos();
        final String devBranch = Context.git_your_name +"/" + ConfigManager.getInstance().getProperty("jira_id") + "_" + jarName + "_cve_" + normalizeBranch();
        for (String repo : repos) {
            log.info("About to create and checkout dev branch {} in repository: {}", devBranch, repo);
            try (Git git = Git.open(new File(git_repository + repo))) {
                git.branchCreate().setName(devBranch).setForce(true).call();
                git.checkout().setName(devBranch).call();
            }
        }
    }

    /*
    获取search_result.log中的repository列表
     */
    private List<String> getRepos() throws IOException {
        final List<String> search_result = FileUtils.readLines(new File(result_folder + jarName + "/search_result.log"));
        List<String> result = new ArrayList<>();
        final List<String> collect = search_result.stream().filter(e -> !e.startsWith(" ")).collect(Collectors.toList());
        result.addAll(collect);
        return result;
    }


    /*
    获得各个repository的base branch
     */
    private String getBaseBranch(String repo) {
        String branchName = normalizeBranch();
        if ("8.0".equals(branchName)) {
            switch (repo) {
                case "connectors-se":
                case "connectors-ee":
                case "cloud-components":
                case "talend-sap-api":
                case "tsap-rfc-server":
                    return "master";
                case "components":
                case "components-ee":
                    return "maintenance/8.0";
                case "connectors-lib-se":
                    return "main";
                default:
                    return "maintenance/8.0.2";
            }
        } else if ("7.3".equals(branchName)) {
            switch (repo) {
                case "connectors-se":
                case "connectors-ee":
                case "cloud-components":
                    return "maintenance/1.27";
                case "talend-sap-api":
                    return "maintenance/8.1";
                default:
                    return "maintenance/7.3";
            }
        } else if ("master".equals(branchName)) {
            switch (repo) {
                case "connectors-lib-se":
                    return "main";
                default:
                    return "master";
            }
        } else if ("tdp".equals(branchName)) {
            return "maintenance/tdp";
        } else {
            return "master";
        }
    }

    public void switchBranch() {
        String branchName = normalizeBranch();
        switchBranch(branchName);
    }
    public void switchBranch(String branchName) {

        if ("8.0".equals(branchName)) {
            checkoutAndPull("master", se);
            checkoutAndPull("master", ee);
            checkoutAndPull("master", cloud);
            checkoutAndPull("main", connectors_lib_se);
            checkoutAndPull("maintenance/8.0", component);
            checkoutAndPull("maintenance/8.0", component_ee);
            checkoutAndPull("maintenance/8.0.2", tdi_studio_se);
            checkoutAndPull("maintenance/8.0.2", tdi_studio_ee);
            checkoutAndPull("maintenance/8.0.2", tbd_studio_se);
            checkoutAndPull("maintenance/8.0.2", tcommon_studio_se);
            checkoutAndPull("maintenance/8.0.2", tcommon_studio_ee);
            checkoutAndPull("master", talend_sap_api);
            checkoutAndPull("master", tsap_rfc_server);
        } else if ("7.3".equals(branchName)) {
            checkoutAndPull("maintenance/1.27", se);
            checkoutAndPull("maintenance/1.27", ee);
            checkoutAndPull("maintenance/1.27", cloud);
            checkoutAndPull("maintenance/7.3", connectors_lib_se);
            checkoutAndPull("maintenance/7.3", component);
            checkoutAndPull("maintenance/7.3", component_ee);
            checkoutAndPull("maintenance/7.3", tdi_studio_se);
            checkoutAndPull("maintenance/7.3", tdi_studio_ee);
            checkoutAndPull("maintenance/7.3", tbd_studio_se);
            checkoutAndPull("maintenance/7.3", tcommon_studio_se);
            checkoutAndPull("maintenance/7.3", tcommon_studio_ee);
            checkoutAndPull("maintenance/8.1", talend_sap_api);
            checkoutAndPull("maintenance/7.3", tsap_rfc_server);
        } else if ("master".equals(branchName)) {
            checkoutAndPull("master", se);
            checkoutAndPull("master", ee);
            checkoutAndPull("master", cloud);
            checkoutAndPull("main", connectors_lib_se);
            checkoutAndPull("master", component);
            checkoutAndPull("master", component_ee);
            checkoutAndPull("master", tdi_studio_se);
            checkoutAndPull("master", tdi_studio_ee);
            checkoutAndPull("master", tbd_studio_se);
            checkoutAndPull("master", tcommon_studio_se);
            checkoutAndPull("master", tcommon_studio_ee);
            checkoutAndPull("master", talend_sap_api);
            checkoutAndPull("master", tsap_rfc_server);

        } else if ("tdp".equals(branchName)) {

            checkoutAndPull("maintenance/tdp", component);


        } else {
            checkoutAndPull(branchName, se);
            checkoutAndPull(branchName, ee);
            checkoutAndPull(branchName, cloud);
            checkoutAndPull(branchName, connectors_lib_se);
            checkoutAndPull(branchName, component);
            checkoutAndPull(branchName, component_ee);
            checkoutAndPull(branchName, tdi_studio_se);
            checkoutAndPull(branchName, tdi_studio_ee);
            checkoutAndPull(branchName, tbd_studio_se);
            checkoutAndPull(branchName, tcommon_studio_se);
            checkoutAndPull(branchName, tcommon_studio_ee);
            checkoutAndPull(branchName, talend_sap_api);
            checkoutAndPull(branchName, tsap_rfc_server);
        }
    }

    public boolean checkoutAndPull(String targetBranch, String repoPath) {

        try (Git git = Git.open(new File(repoPath))) {
            git.clean().setCleanDirectories(true).setForce(true).setIgnore(false).call();
            git.reset().setMode(ResetCommand.ResetType.HARD).call();

            git.fetch().setCredentialsProvider(provider).call();
            // 切换到master分支（或main分支，根据你的仓库设置）
            git.checkout().setName(targetBranch).call();
//            // 拉取远程的最新更改
//            // 注意：这里假设远程仓库的默认远程名称为origin，并且你想要拉取的是origin/master（或origin/main）
            git.pull().setCredentialsProvider(provider).call();
//            git.pull().setRemoteName("origin").setRefSpecs(new String[]{"+refs/heads/" + targetBranch + ":refs/heads/" + targetBranch})
//                    .call();
            log.info("Reset changes and pulled latest updates in {} from remote branch: {}",repoPath,targetBranch);

        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
            log.warn("Error occurred while working with Git repository: {}",repoPath);
            return false;
        }
        return true;
    }


     public void cleanRepository(String repoPath) {
        log.info("Cleaning repository: {}",repoPath);
        try (Git git = Git.open(new File(repoPath))) {
            git.clean().setForce(true).setCleanDirectories(true).call();
            git.reset().setMode(ResetCommand.ResetType.HARD).call();
        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
            log.error("Error occurred while CLEAN with Git repository: {}",repoPath);
        }
    }

    public void cleanAll() {
        cleanRepository(se);
        cleanRepository(ee);
        cleanRepository(cloud);
        cleanRepository(connectors_lib_se);
        cleanRepository(component);
        cleanRepository(component_ee);
        cleanRepository(tdi_studio_se);
        cleanRepository(tdi_studio_ee);
        cleanRepository(tbd_studio_se);
        cleanRepository(tcommon_studio_se);
        cleanRepository(tcommon_studio_ee);
        cleanRepository(talend_sap_api);
        cleanRepository(tsap_rfc_server);
    }

    private void createBranch(String repoPath, String branchName) {
        try (Git git = Git.open(new File(repoPath))) {
            git.branchCreate().setName(branchName).call();
        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
            log.error("Error occurred while create branch {} with Git repository: {}",branchName,repoPath);
        }
    }


    public String normalizeBranch(String branch) {
        branch = branch.trim();
        if ("8.0".equals(branch) || "maintenance/8.0".equals(branch)) {
            return "8.0";
        } else if ("7.3".equals(branch) || "maintenance/7.3".equals(branch)) {
            return "7.3";
        } else if ("master".equals(branch)) {
            return "master";
        } else if ("maintenance/tdp".equals(branch)) {
            return "tdp";
        } else {
            return branch;
        }
    }

    private String normalizeBranch() {
        return normalizeBranch(branch);
    }

/*
创建master dev分支 并cherry pick commitList.txt 中的commit到master 基dev branch
 */
    public void cherryPick() throws IOException {

        final File commitList = new File(Context.result_folder + Context.jarName + "/commitList.txt");
        final List<String> lines = FileUtils.readLines(commitList,"utf-8");
        for (String line : lines) {
            final int i = line.indexOf(":");
            String repo = line.substring(0, i);
            String commitID = line.substring(i);
            try (Git git = Git.open(new File(git_repository + repo))) {
                checkoutAndPull("connectors-lib-se".equals(repo) ? "main" : "master", git_repository + repo);
                final String devBranch_target = Context.git_your_name +"/" + ConfigManager.getInstance().getProperty("jira_id") + "_" + jarName + "_cve_master";
                git.branchCreate().setName(devBranch_target).call();
                log.info("Creating branch {}", devBranch_target);
                git.cherryPick().include(ObjectId.fromString(commitID)).call();
                log.info("Cherry-pick");
                git.push().setCredentialsProvider(provider).setRemote("origin").call();
                log.info("PR: https://github.com/Talend/{}/compare/{}...{}?expand=1", repo, "connectors-lib-se".equals(repo) ? "main" : "master", devBranch_target);

            } catch (GitAPIException | IOException e) {
                e.printStackTrace();
                log.error("Error occurred while cherry-pick with Git repository {}", repo);
            }


        }


    }

    /*
    检查并禁止push maintenance branch
     */
    private void checkBranchStatus(Git git) throws IOException {
        final Repository repository = git.getRepository();
        final RefDatabase refDatabase = repository.getRefDatabase();

        final List<Ref> heads = refDatabase.getRefsByPrefix("HEAD");
        for (Ref head : heads) {
            System.out.println(head.getName());
            // 获取当前 HEAD 指向的引用
            // 检查 HEAD 是否指向一个分支（即是否是一个符号引用）
            if (head.isSymbolic()) {
                // 获取并打印短分支名
                String branchName = head.getTarget().getName().substring(11); // 去除 "refs/heads/" 前缀
                System.out.println("当前分支是: " + branchName);
                if(branchName.startsWith("maintenance")){
                    throw new RuntimeException("Current branch should not be push: " + branchName);
                }

            } else {
                // 如果 HEAD 直接指向一个提交（如在一个分离 HEAD 状态下），则没有当前分支
                System.out.println("当前不在任何分支上（分离 HEAD 状态）");
            }

        }
    }


    private void oldCherryPick() throws IOException {
        final List<String> repos = getRepos();
        for (String repo : repos) {
            try (Git git = Git.open(new File(git_repository + repo))) {
                final String jiraId = ConfigManager.getInstance().getProperty("jira_id");
                final String devBranch = Context.git_your_name +"/" + jiraId + "_" + jarName + "_cve_" + normalizeBranch();
                checkoutAndPull(devBranch, git_repository + repo);
                final Iterable<RevCommit> call = git.log().call();
                final RevCommit next = call.iterator().next();
                log.info("last commit: {}", next.toString());
//                git.checkout().setName("connectors-lib-se".equals(repo)?"main":"master").call();
                log.info("checking out {}", getBaseBranch(repo));


                checkoutAndPull("connectors-lib-se".equals(repo) ? "main" : "master", git_repository + repo);

                final String devBranch_target = Context.git_your_name +"/" + ConfigManager.getInstance().getProperty("jira_id") + "_" + jarName + "_cve_master";
                git.branchCreate().setName(devBranch_target).call();
                log.info("Creating branch {}", devBranch_target);
                git.cherryPick().include(next).call();
                log.info("Cherry-pick");
                git.push().setCredentialsProvider(provider).setRemote("origin").call();
                log.info("PR: https://github.com/Talend/{}/compare/{}...{}?expand=1", repo, "connectors-lib-se".equals(repo) ? "main" : "master", devBranch_target);

            } catch (GitAPIException | IOException e) {
                e.printStackTrace();
                log.error("Error occurred while cherry-pick with Git repository {}", repo);
            }
        }
    }
}
