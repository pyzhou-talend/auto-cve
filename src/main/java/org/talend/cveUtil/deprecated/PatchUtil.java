package org.talend.cveUtil.deprecated;

import static org.talend.cveUtil.common.Context.component;
import static org.talend.cveUtil.common.Context.component_ee;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.cveUtil.common.ConfigManager;
import org.talend.cveUtil.common.Context;
import org.talend.cveUtil.fileVisitors.CopyTcompV0FV;
import org.talend.cveUtil.utils.MvnUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PatchUtil {

    private static final Logger log = LoggerFactory.getLogger(PatchUtil.class);

    /*
    before build patch,
    Run switch branch
    Run MvnUtil cleanInstall
    Run DependencyTree for all TCK and TcompV0, it will generate comp2treeMap.log
    This log will be used to located jar
    Run logFileSearchermvn dependency
     */

    public static void main(String[] args) throws IOException {
        final PatchUtil patchUtil = new PatchUtil();
        String oldValue = patchUtil.extractVersionFromFile(component+"/pom.xml");
        patchUtil.replaceVersion(component,oldValue, ConfigManager.getInstance().getProperty("tcompV0_patch_version"));
        patchUtil.replaceVersion(component_ee,oldValue,ConfigManager.getInstance().getProperty("tcompV0_patch_version"));
        final MvnUtil mvnUtil = new MvnUtil();
        mvnUtil.cleanInstall(component,true,false);
        mvnUtil.cleanInstall(component_ee,true,false);
        patchUtil.buildPatch();

    }

    private void buildPatch() throws IOException {
        final File foundResult = new File(Context.result_folder + Context.jarName + "/FoundResult.log");
        final File patchFolder = new File(Context.result_folder + Context.jarName + "/patch/");
        FileUtils.deleteDirectory(patchFolder);
        final String tcompV0_patch_version = ConfigManager.getInstance().getProperty("tcompV0_patch_version");
        final Properties tckListProperties = getTckListProperties();
        log.info("Reading {}",foundResult.getAbsolutePath());
        final List<String> jarFolder = FileUtils.readLines(foundResult);
        final ArrayList<File> carFile = new ArrayList<>();
        for (String path : jarFolder) {
            path = path.replaceAll("\\\\","/");
            log.info("Processing path: {}",path);
            if (path.startsWith(Context.se) || path.startsWith(Context.ee) || path.startsWith(Context.cloud)) {//tck
                boolean isStudiotck = false;
                for (Object o : tckListProperties.keySet()) {
                    if (path.contains(o.toString())) {
                        isStudiotck = true;
                        break;
                    }
                }

                // 遍历目录和子目录
                if (isStudiotck) {
                    log.info("Finding car for: {}" , path);
                    Files.walkFileTree(Path.of(path), new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            // 检查是否是car文件
                            log.debug("Checking file: {}",file);
                            if (file.getFileName().toString().endsWith(".car")) {
                                carFile.add(file.toFile());

                                FileUtils.copyFile(file.toFile(), new File(Context.result_folder + Context.jarName + "/patch/" + file.getFileName()));
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            // 如果需要，可以在这里添加对目录的访问逻辑
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }else {
                    log.info("{} is not studio component, skip",path);
                }




            } else if(path.startsWith(component) || path.startsWith(component_ee) ){//tcompV0
                log.info("processing TcompV0 component: {}", path);

                // 遍历目录和子目录
                Files.walkFileTree(Path.of(path), new CopyTcompV0FV(path) );



            } else {
                //ignore
                log.error("Unprocess path: {}",path);
            }

        }

        createInstallFile(carFile);

        final List<String> lines = FileUtils.readLines(new File(Context.result_folder + Context.jarName + "/search_result.log"));
        String flag = "";
        for (String line : lines) {
            System.out.println(line);
            if (line.contains("tdi-studio-se")) {
                flag = "tdi-studio-se";
            } else if (line.contains("tdi-studio-ee-dynamic")) {
                flag="tdi-studio-ee-dynamic";
            } else if (line.contains("tdi-studio-ee")) {
                flag = "tdi-studio-ee";
            } else if (line.contains("tbd-studio-se")) {
                flag = "tbd-studio-se";
            } else if (line.startsWith(" ")) {
                System.out.println("flag: " + flag);

                if (line.endsWith("_java")) {
                    final String comFolder = line.trim().substring(0, line.length() - 9);
                    System.out.println(comFolder);
                    final File destination = new File(Context.result_folder + Context.jarName + "/patch/" + flag + "/" + comFolder);
                    if ("tdi-studio-se".equals(flag)) {
                        File sourceFloder = new File(Context.tdi_studio_se_components + comFolder);
                        FileUtils.copyDirectory(sourceFloder, destination);
                    } else if ("tdi-studio-ee-dynamic".equals(flag)) {
                        File sourceFloder = new File(Context.tdi_studio_ee_components_sap + comFolder);
                        FileUtils.copyDirectory(sourceFloder, destination);
                    } else if ("tdi-studio-ee".equals(flag)) {
                        File sourceFloder = new File(Context.tdi_studio_ee_components + comFolder);
                        FileUtils.copyDirectory(sourceFloder, destination);
                    } else if ("tbd-studio-se".equals(flag)) {
                        File sourceFloder = new File(Context.tbd_studio_se_components + comFolder);
                        FileUtils.copyDirectory(sourceFloder, destination);
                    } else if ("tcommon-studio-se".equals(flag)) {
                        //do nothing
                    } else if ("".equals(flag)) {
                        continue;
                    } else {
                        throw new RuntimeException("Unknown flag " + flag);
                    }

                }


            } else {
                flag = "";
            }
        }

        log.info("Patch folder: {}",patchFolder);
    }

    public void replaceVersion(String path, String oldValue, String newValue) {
        // 指定需要搜索的目录
        Path directoryPath = Paths.get(path);
        log.info("About replace {} to {} in {}",oldValue,newValue,path);
        // 指定要查找和替换的字符串

        // 遍历目录和子目录
        try {
            Files.walkFileTree(directoryPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // 检查是否是pom.xml文件
                    if (file.getFileName().toString().equalsIgnoreCase("pom.xml")) {
                        replaceTextInFile(file, oldValue, newValue);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    // 如果需要，可以在这里添加对目录的访问逻辑
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 替换文件中的文本
    private static void replaceTextInFile(Path file, String searchString, String replaceString) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        List<String> newLines = new ArrayList<>();
        for (String line : lines) {
            newLines.add(line.replace(searchString, replaceString));
        }
        Files.write(file, newLines, StandardCharsets.UTF_8);
    }

    private Properties getCompBundleProperties() {
        return readProperties("/ComponentBundleName.properties");
    }

    private Properties getTckListProperties() {
        return readProperties("/tckConnectorList.properties");
    }

    private Properties readProperties(String file) {
        Properties props = new Properties();
        InputStream inputStream = null;

        try {
            inputStream = getClass().getResourceAsStream(file);

            if (inputStream == null) {
                throw new RuntimeException("config.properties not found in the classpath");
            }

            props.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return props;
    }


    public String extractVersionFromFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 检查行是否包含<version>标签
                if (line.contains("<version>")) {
                    // 提取版本号（这里假设标签和值在同一行内）
                    int startIndex = line.indexOf("<version>") + "<version>".length();
                    int endIndex = line.indexOf("</version>");
                    // 如果找到起始和结束索引，则返回版本号
                    if (startIndex >= 0 && endIndex > startIndex) {
                        final String version = line.substring(startIndex, endIndex);
                        log.info("Found version in pom {}", version);
                        return version;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 如果没有找到版本标签，则返回null
        return null;
    }


    public int findFirstDigitIndex(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {
                return i; // 返回第一个数字的索引
            }
        }
        return -1; // 如果没有找到数字，则返回-1
    }


    private void createInstallFile(List<File> carFile) throws IOException {
        StringBuilder template = new StringBuilder();

        template.append( "@echo off  \n" +
                "set /p userInput=\"Please enter the studio root location: \"  \n" +
                "echo studio localtion: %userInput%  \n");
        for (File file : carFile) {
            template.append("java -jar ");
            template.append(file.getName());
            template.append(" studio-deploy -f --location %userInput%\n");
        }
        template.append("pause");
        final File installFile = new File(Context.result_folder + Context.jarName + "/patch/install.bat");
        FileUtils.write(installFile,template.toString());


    }
}
