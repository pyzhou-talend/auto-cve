package org.talend.cveUtil.utils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.cveUtil.common.Context;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LogFileSearcher {

    private static final Logger log = LoggerFactory.getLogger(LogFileSearcher.class);

    public static void main(String[] args) throws IOException {
        final LogFileSearcher logFileSearcher = new LogFileSearcher();

        List<String> jarList = new ArrayList<>();
        jarList.add(Context.jarName);
        for (String jar : jarList) {

            String path = Context.forest_folder + Context.branch.replaceAll("/", "_");         // 替换为你的目录路径
            logFileSearcher.searchJar(jar, path, ".log");

            logFileSearcher.searchJar(jar, Context.tdi_studio_se_components, ".xml");
            logFileSearcher.searchJar(jar, Context.tdi_studio_ee_components, ".xml");
            logFileSearcher.searchJar(jar, Context.tdi_studio_ee_components_sap, ".xml");
            logFileSearcher.searchJar(jar, Context.tbd_studio_se_components, ".xml");

//            logFileSearcher.searchJar(jar, Context.tcommon_studio_se_plugins + "org.talend.designer.maven.repo.tck/", "pom.xml");
//            logFileSearcher.searchJar(jar, Context.tcommon_studio_se_plugins + "org.talend.designer.maven.repo.tcksdk/", "pom.xml");
//            logFileSearcher.searchJar(jar, Context.tcommon_studio_se_plugins + "org.talend.designer.maven.repo.tcompv0/", "pom.xml");
//            logFileSearcher.searchJar(jar, Context.tcommon_studio_se_plugins + "org.talend.designer.maven.tos/", "pom.xml");
        }




    }

    public LogFileSearcher() {
        final File searchResultFile = new File(Context.result_folder + Context.jarName + "/search_result.log");
        final File foundResult = new File(Context.result_folder + Context.jarName + "/FoundResult.log");
//            FileUtils.forceDeleteOnExit(resultFile);
        final boolean delete = searchResultFile.delete();
        log.info("Delete exist search Result file {} {}", searchResultFile.getAbsolutePath(), delete);
        final boolean foundResultdelete = foundResult.delete();
        log.info("Delete exist found Result file {} {}", searchResultFile.getAbsolutePath(), foundResultdelete);
    }


    final List<File> errorList = new ArrayList<>();

    public void searchJar(String jarName, String path, String suffix) {


        Path startPath = Paths.get(path);
        try {
            final List<File> files = this.searchLogs(startPath, jarName, suffix);
            List<String> names = new ArrayList<>();
            for (File file : files) {
                if (file.getName().startsWith("tree_")) {
                    if (!names.contains(file.getParentFile().getName())) {
                        names.add(file.getParentFile().getName());
                    }

                    //tree_bigtable.log
                    final String substring = file.getName().substring(5);
                    //bigtable.log
                    final String substring1 = substring.substring(0, substring.lastIndexOf("."));
                    //bigtable
                    if (!substring.contains("integration")) {
                        names.add("    " + substring1);
                    }

                } else {//javajet components
                    //D:\DE\git_CVE\tdi-studio-se\main\plugins\org.talend.designer.components.localprovider\components\tAmazonEMRManage\tAmazonEMRManage_java.xml
                    final String absolutePath = file.getAbsolutePath();
                    if(absolutePath.contains("studio") && !names.contains("#studio")) {
                        names.add("#studio");
                    } else if (absolutePath.contains("tdi-studio-se") && !names.contains("#tdi-studio-se")) {
                        names.add("#tdi-studio-se");
                    } else if (absolutePath.contains("components_dynamic") && !names.contains("#tdi-studio-ee")) {
                        names.add("#tdi-studio-ee-dynamic");
                    } else if (absolutePath.contains("tdi-studio-ee") && !names.contains("#tdi-studio-ee")) {
                        names.add("#tdi-studio-ee");
                    } else if (absolutePath.contains("tbd-studio-se") && !names.contains("#tbd-studio-se")) {
                        names.add("#tbd-studio-se");
                    } else if (absolutePath.contains("tcommon-studio-se")) {
                        if (!names.contains("tcommon-studio-se")) {
                            names.add("tcommon-studio-se");
                        }
                        names.add("    " + file.getAbsolutePath());
                        continue;
                    }
                    names.add("    " + file.getName().substring(0, file.getName().lastIndexOf("_java.")));

                }


            }
//            final String collect = files.stream().map(f -> f.getName().substring(5)).map(e-> e.substring(0,e.lastIndexOf("."))).collect(Collectors.joining("\n"));
//            names.forEach(System.out::println);
            final String collect = names.stream().collect(Collectors.joining("\n"));
            FileUtils.writeLines(new File(Context.result_folder + jarName + "/search_result.log"), names, true);
            final List<String> strings = FileUtils.readLines(new File(Context.result_folder + Context.jarName + "/comp2treeMap.log"), Charset.defaultCharset());




            final HashSet<String> foundFileSet = new HashSet<>();
            for (File file : files) {
                for (String string : strings) {
                    if (string.endsWith(file.getName())) {
                        final String componentFolder = string.substring(0, string.lastIndexOf("<>"));
                        log.info("Folder found {}", componentFolder);
                        if (componentFolder.contains("runtime") || componentFolder.contains("definition")) {//tcompV0

                            foundFileSet.add(new File(componentFolder).getParentFile().getAbsolutePath());
                        } else if (componentFolder.contains("integration")) {
                            //ignore
                        } else {
                            foundFileSet.add(componentFolder);
                        }
                    }
                }
            }
//            System.out.println(foundFileSet.size());
            log.info("Appending {} size:{}",Context.result_folder + jarName + "/FoundResult.log",foundFileSet.size());
            FileUtils.writeLines(new File(Context.result_folder + jarName + "/FoundResult.log"), foundFileSet, true);
            System.out.println(collect);


            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            errorList.forEach(e -> System.err.println("BUILD FAILURE Found in file: " + e.getAbsolutePath()));
            FileUtils.writeLines(new File(Context.result_folder + jarName + "/buildError.log"), errorList, true);
            errorList.clear();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<File> searchLogs(Path startPath, String jarName, String suffix) throws IOException {
        final Pattern KEYWORD_PATTERN = Pattern.compile("\\b" + jarName + "\\b", Pattern.CASE_INSENSITIVE); // 正则表达式匹配"cxf"
        final List<File> fileList = new ArrayList<>();

        Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (Files.isRegularFile(file) && file.toString().endsWith(suffix)) {
                    boolean found = false;
                    boolean errorFound = false;
                    try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                        String line;
                        int lineNumber = 0;
                        while ((line = reader.readLine()) != null) {
                            if (line.endsWith("test")){
                                continue;
                            }
                            lineNumber++;
                            Matcher matcher = KEYWORD_PATTERN.matcher(line);
                            if (line.contains("BUILD FAILURE")) {
                                System.err.println("BUILD FAILURE Found in file: " + file + " at line: " + lineNumber);
                                errorFound = true;
                                break;
                            }
                            if (matcher.find()) {
                                found = true;
                                log.debug("Found in file: " + file + " at line: " + lineNumber);
                                log.debug("Line content: " + line);
                                // 如果只需要找到第一个匹配就跳出，可以取消下面这行的注释
//                                 break;
                            }
                        }
                    }

                    if (found) {
                        log.info("Found file:{}",file);
                        fileList.add(file.toFile());
                    }

                    if (errorFound) {
                        errorList.add(file.toFile());
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                System.err.println("Failed to visit: " + file);
                exc.printStackTrace();
                return FileVisitResult.CONTINUE;
            }
        });

        return fileList;
    }


}