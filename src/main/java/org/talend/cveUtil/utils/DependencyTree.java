package org.talend.cveUtil.utils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.cveUtil.common.Context;
import org.talend.cveUtil.common.RunCMD;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DependencyTree {
    private static final Logger log = LoggerFactory.getLogger(DependencyTree.class);
    public static void main(String[] args) {
        /*
        Switch to the correct branch first !!! #GitUtil
         */
        final DependencyTree dependencyTree = new DependencyTree();
        dependencyTree.listTCK();
        dependencyTree.listTcompV0();
//        dependencyTree.listRepo(Context.connectors_lib_se);
//        dependencyTree.listRepo(Context.talend_sap_api);
//        dependencyTree.connectorsList.add(new File(Context.tsap_rfc_server));

//        dependencyTree.listParent();

        dependencyTree.runMvnTree(Context.branch);
//        dependencyTree.connectorsList.stream().map(File::getName).forEach(System.out::println);
    }

    public void listTCK(){
        this.findPomXmlFolders(new File(Context.se));
        this.connectorsList.remove(new File(Context.se));
        this.findPomXmlFolders(new File(Context.ee));
        this.connectorsList.remove(new File(Context.ee));
        this.findPomXmlFolders(new File(Context.cloud));
        this.connectorsList.remove(new File(Context.cloud));
//        log.debug(this.connectorsList.size());

    }

    public void listTcompV0(){
        this.findPomXmlFolders(new File(Context.component+"/components"));
        this.connectorsList.remove(new File(Context.component+"/components"));
        this.findPomXmlFolders(new File(Context.component_ee));
        this.connectorsList.remove(new File(Context.component_ee));

//        log.debug(this.connectorsList.size());
    }

    public void listParent(){
        final DependencyTree dependencyTree = new DependencyTree();
        dependencyTree.connectorsList.add(new File(Context.se));
        dependencyTree.connectorsList.add(new File(Context.ee));
        dependencyTree.connectorsList.add(new File(Context.cloud));
        dependencyTree.connectorsList.add(new File(Context.component));
        dependencyTree.connectorsList.add(new File(Context.component_ee));
        dependencyTree.connectorsList.add(new File(Context.connectors_lib_se));
        dependencyTree.runMvnTree("parents");
    }

    public void listRepo(String repo){
        this.findPomXmlFolders(new File(repo));
        this.connectorsList.remove(new File(repo));
//        log.debug(this.connectorsList.size());
    }
    public void runMvnTree(String parentFolder) {
        final HashMap<File, Object> map = new HashMap<>();

        for (File file : connectorsList) {
            log.info("mvn dependency:tree in {}",file.getAbsolutePath() );
            final String repositoryName = getRepository(file);
            String folder = Context.forest_folder +parentFolder.replaceAll("/","_")+"/"+ repositoryName + "/";
            final File repo = new File(folder);
            if(!repo.exists()){
                repo.mkdirs();
            }
            map.put(file,folder+  "tree_" + file.getName() + ".log");

            RunCMD.runCMD("mvn dependency:tree>"+folder+  "tree_" + file.getName() + ".log",file,false);
        }

        final String collect = map.entrySet().stream().map(e -> e.getKey().getAbsolutePath() + "<>" + e.getValue()).collect(Collectors.joining("\n"));
        try {
            FileUtils.write(new File(Context.result_folder + Context.jarName + "/comp2treeMap.log"),collect, Charset.defaultCharset());
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }


    }


    private String getRepository(File file){
        if(new File(Context.git_repository).equals(file.getParentFile())){// tsap-rfc-server only repo
            return file.getName();
        }
        final String substring = file.getAbsolutePath().substring(Context.git_repository.length());
        final String repo = substring.substring(0,substring.indexOf("\\"));
        log.debug("repository: "+ repo);
        return repo;

    }

    public List<File> connectorsList = new ArrayList<>();
    /**
     * 递归查找包含pom.xml的文件夹
     *
     * @param dir 当前遍历的目录
     */
    public void findPomXmlFolders(File dir) {
        // 确保传入的确实是一个目录
        if (dir.isDirectory()) {
            // 列出目录下的所有文件和文件夹
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    // 如果是目录，则递归调用
                    if(file.isDirectory() && "src".equals(file.getName())){
                        // 如果找到了src，则打印其所在的目录
                        log.debug("Found src in: " + file.getParent());
                        connectorsList.add(file.getParentFile());
                    }else if (file.isDirectory()) {
                        findPomXmlFolders(file);
                    }
                }
            }
        }
    }

    public void findCVEjar(){
//        final LogFileSearcher logFileSearcher = new LogFileSearcher();
//        logFileSearcher.searchLogs()


    }
}
