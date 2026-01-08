package org.talend.cveUtil.fileVisitors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.cveUtil.common.ConfigManager;
import org.talend.cveUtil.common.Context;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.Properties;

public class CopyTcompV0FV extends SimpleFileVisitor<Path> {


    public CopyTcompV0FV(String filePath){
        this.filePath =filePath;
    }
    private static final Logger log = LoggerFactory.getLogger(CopyTcompV0FV.class);
    Properties compBundleProperties = getCompBundleProperties();

    final String tcompV0_patch_version = ConfigManager.getInstance().getProperty("tcompV0_patch_version");

    final String filePath;



        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        // 检查是否是jar文件
            String fileName = file.toFile().getName();
//            System.out.println("####"+fileName);
        if (fileName.endsWith(".jar") && !fileName.contains("integration")) {
            if (fileName.endsWith("bundle.jar")) {
                if (fileName.contains("runtime")) {
                    return FileVisitResult.CONTINUE;
                }

                for (Object key : compBundleProperties.keySet()) {
                    if (fileName.contains(key.toString())) {
                        final String name = compBundleProperties.getProperty(key.toString()).replace("${components.version}", tcompV0_patch_version);
                        final File destination = new File(Context.result_folder + Context.jarName + "/patch/plugins/" + name);
                        FileUtils.copyFile(file.toFile(), destination);
                        log.info("Copied file from {} to {}", file.toFile().getName(), destination);
                        break;
                    }
                }



            } else if (fileName.endsWith("javadoc.jar") || fileName.endsWith("sources.jar") || fileName.endsWith("tests.jar")) {
                //ignore
                return FileVisitResult.CONTINUE;
            } else {
                final int firstDigitIndex = findFirstDigitIndex(fileName);
                final String artifactId = fileName.substring(0, firstDigitIndex - 1);
                final String version = fileName.substring(firstDigitIndex, fileName.lastIndexOf("."));
                if (!version.endsWith(tcompV0_patch_version)) {
                    throw new RuntimeException("Wrong version! expect:" + tcompV0_patch_version + " but get " + version + " in file " + fileName);
                }
                final File destination = new File(Context.result_folder + Context.jarName + "/patch/configuration/.m2/repository/org/talend/components/" + artifactId + "/" + version + "/" + fileName);
                FileUtils.copyFile(file.toFile(), destination);
                log.info("Copied file from {} to {}", file.toFile().getName(), destination);

            }

        }
        return FileVisitResult.CONTINUE;
    }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        // 如果需要，可以在这里添加对目录的访问逻辑
        return FileVisitResult.CONTINUE;
    }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                            throws IOException
        {
//            if (!copied) {
//                throw new RuntimeException("Jar name not found: " + filePath);
//            }
            Objects.requireNonNull(dir);
            if (exc != null)
                throw exc;
            return FileVisitResult.CONTINUE;
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

    public int findFirstDigitIndex(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {
                return i; // 返回第一个数字的索引
            }
        }
        return -1; // 如果没有找到数字，则返回-1
    }


}
