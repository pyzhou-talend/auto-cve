package org.talend.cveUtil.singleUtils;

import org.talend.cveUtil.common.Context;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListTUJ {

    public static void main(String[] args) throws IOException, URISyntaxException {
        ListTUJ listTUJ = new ListTUJ();

        //        scanTestCycle();//扫描最新的test cycle
        //        listFeature();//扫描最新的studio component列表
        //        listTUJ.scanJavajetComponents();//扫描所有javajet component

//        listTUJ.scanAPITestGroup(); //扫描APITestgroup 结果在resources/MiddleResult/APITestGroups.csv

        listTUJ.cleanDuplicateItem();

    }

    public void cleanDuplicateItem() throws IOException, URISyntaxException {
        List<String> strings =
                Files.readAllLines(Path.of(this.getClass().getResource("/MiddleResult/APITestGroups.csv").toURI()));
        HashSet<String> strings1 = new HashSet<>();
        for (String string : strings) {
            String[] split = string.split(",");
            strings1.add(split[1]);
        }
        System.out.println(String.join("\n",strings1));
    }

    public void scanAPITestGroup() {
        // 替换为你想要扫描的文件夹路径
        String targetPath =
                "C:\\work\\git\\data-processing-runtime\\data-processing-runtime-qa\\dpruntime-tests-api\\components-tests\\tests";
        String keyword = "@group";
        scanKeyword(targetPath, keyword);
    }

    public static void scanKeyword(String directoryPath, String keyword) {
        Path startPath = Paths.get(directoryPath);

        if (!Files.exists(startPath)) {
            System.err.println("错误：路径不存在 -> " + directoryPath);
            return;
        }

        try (Stream<Path> walk = Files.walk(startPath)) {
            walk.filter(Files::isRegularFile) // 只处理文件，跳过文件夹
                    .forEach(path -> searchInFile(path, keyword));
        } catch (IOException e) {
            System.err.println("无法遍历目录: " + e.getMessage());
        }
    }

    private static void searchInFile(Path path, String keyword) {
        boolean find = false;
        List<String> lines = new ArrayList<>();
        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (String line : lines) {
            if (line.contains(keyword) &&
                    ((line.chars().filter(e -> e == '/').count() == 1) //有且仅有一个 /
                            || line.contains("Processor"))
            ) {
                find = true;
                String[] groups = line.split(" ");
                System.out.printf("%s,%s%n",
                        path.getFileName(), groups[3]);
            }
        }

        if (!find) {
            System.err.println("未找到关键字 '" + keyword + "' 在文件: " + path.getFileName());
        }

    }


    /*
    扫描最新的studio component列表
     */

    public static void listFeature() throws IOException {
        String filePath =
                Context.tcommon_studio_ee + "\\main\\plugins\\org.talend.studiolite.p2.featmanage\\resources\\category.xml";
        List<String> features = Files.readAllLines(Paths.get(filePath)).stream().map(String::trim)
                .filter(line -> line.startsWith("<feature"))
                .map(feature -> feature.substring(feature.indexOf("id=\"") + 4, feature.lastIndexOf("\"")))
                .map(name -> name.substring(0, name.length() - 8))
                .collect(Collectors.toList());
        List<String> result = new ArrayList<>();
        for (String feature : features) {
            //            System.out.println(feature);
            String cName = feature;
            //            System.out.println("cName: " + cName);

            if (cName.startsWith("org.talend.studio.components.tck.")) {
                cName = (cName.substring(33));
            } else if (cName.startsWith("org.talend.studio.components.tcompv0.")) {
                cName = (cName.substring(37));
            } else if (cName.startsWith("org.talend.lite.di.component.")) {
                cName = (cName.substring(29));
            } else if (cName.startsWith("org.talend.lite.bd.components.")) {
                cName = (cName.substring(30));
            } else {
                cName = "";
            }

            //删除无用字符
            if (cName.contains(".connector")) {
                cName = cName.replace(".connector", "");
            } else if (cName.contains(".studio")) {
                cName = cName.replace(".studio", "");
            }

            if (cName.length() > 0) {
                result.add(cName);
            }

            //            System.out.println("result.size: " + result.size());
        }

        result.forEach(System.out::println);
        //        System.out.println("result.size: " + result.size());
    }



    /*
    扫描最新的test cycle
     */

    public static void scanTestCycle() throws IOException {
        // --- 配置区域 ---
        String rootPath = "C:\\work\\git\\tuj\\tuj\\javaCycles"; // 待扫描的根目录

        String csvFilePath = "mapping.csv";
        //        Map<String, String> stringStringMap = TckMigrateUtils.readMappingFile(ListTUJ.class, "mappingTCK_TcompV0_TUJ.csv");
        // ----------------

        try {

            System.out.println("正在扫描目录（深度为2）...\n");

            // 2. 遍历目录（maxDepth 设为 2）
            // 注意：depth 1 是根目录下的直接子目录，depth 2 是二级目录
            try (Stream<Path> paths = Files.walk(Paths.get(rootPath))) {
                paths.filter(Files::isDirectory) // 只看文件夹
                        .filter(path -> !path.equals(Paths.get(rootPath))) // 排除根目录自身
                        .forEach(path -> {
                            int nameCount = path.getNameCount();
                            if (nameCount > 6) {
                                String folderName = path.toAbsolutePath().toString();
                                String tuj = folderName.substring(folderName.indexOf("javaCycles\\") + 11);
                                System.out.println(tuj);
                            }
                        });
            }

        } catch (NoSuchFileException e) {
            System.err.println("错误：找不到指定的文件或路径 -> " + e.getMessage());
        }
    }

    public void scanJavajetComponents() throws IOException {
        File file = new File("C:\\work\\IDE_Workspace\\auto-cve\\src\\main\\resources\\javajetComponents.txt");
        String absolutePath = file.getAbsolutePath();
        scanSubFoldersToFile(absolutePath, Context.tdi_studio_se_components, Context.tdi_studio_ee_components,
                Context.tdi_studio_ee_components_sap);
    }

    /**
     * 扫描多个目录并将子文件夹名称写入指定文件
     *
     * @param outputFilePath 输出文件的路径
     * @param paths          要扫描的源目录
     */
    public static void scanSubFoldersToFile(String outputFilePath, String... paths) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            for (String pathStr : paths) {
                writer.write("========== 目录扫描: " + pathStr + " ==========");
                writer.newLine();

                Path rootPath = Paths.get(pathStr);
                if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
                    writer.write("  [跳过] 路径不存在或不是目录");
                    writer.newLine();
                    continue;
                }

                // 使用 Files.walk 遍历
                try (Stream<Path> walk = Files.walk(rootPath, 1)) {
                    // 获取所有子文件夹名字并写入
                    walk.filter(Files::isDirectory)
                            .filter(path -> !path.equals(rootPath))
                            .map(path -> path.getFileName().toString())
                            .forEach(name -> {
                                try {
                                    writer.write(name);
                                    writer.newLine();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                }
                writer.newLine(); // 不同根目录间留空行
            }

            System.out.println("扫描完成！结果已保存至: " + outputFilePath);

        } catch (IOException e) {
            System.err.println("文件写入失败: " + e.getMessage());
        }
    }
}