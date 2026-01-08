package org.talend.cveUtil.deprecated;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefDatabase;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.talend.cveUtil.common.ConfigManager;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class JGitExample {

    public static void main(String[] args) {
        String repoPath = "D:/DE/git_CVE/connectors-se"; // 你的本地仓库路径

        UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider("z742948950@126.com", ConfigManager.getInstance().getProperty("git_token"));

        try (Git git = Git.open(new File(repoPath))) {

//            ResetCommand reset = git.reset();
//            reset.setMode(ResetCommand.ResetType.HARD);
//            reset.call();
//
//            final CleanCommand clean = git.clean();
//            clean.setCleanDirectories(true).setForce(true);
//            clean.call();
//            // 切换到master分支（或main分支，根据你的仓库设置）
//            String targetBranch = "maintenance/1.27"; // 或者 "main"
//            git.checkout().setName(targetBranch).call();

//            final List<DiffEntry> call1 = git.diff().call();
//            for (DiffEntry diffEntry : call1) {
////                diffEntry.get
//            }
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
                } else {
                    // 如果 HEAD 直接指向一个提交（如在一个分离 HEAD 状态下），则没有当前分支
                    System.out.println("当前不在任何分支上（分离 HEAD 状态）");
                }

            }
//
//            // 重置当前HEAD到上一次提交，丢弃所有未提交的更改
//
//
//            // 拉取远程的最新更改
//            // 注意：这里假设远程仓库的默认远程名称为origin，并且你想要拉取的是origin/master（或origin/main）
//            final PullResult call = git.pull().setCredentialsProvider(provider).call();
//            System.out.println(call.isSuccessful());
//            git.pull().setRemoteName("origin").setRefSpecs(new String[]{"+refs/heads/" + targetBranch + ":refs/heads/" + targetBranch})
//                    .call();

//            System.out.println("Reset changes and pulled latest updates from remote branch: " + targetBranch);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error occurred while working with Git repository");
        }
    }
}