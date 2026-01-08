package org.talend.cveUtil.singleUtils;

import org.talend.cveUtil.common.Context;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AutoTestUtil {

    public static void main(String[] args) throws IOException {
        new AutoTestUtil().generateAutoTestGroup();
    }

    public void generateAutoTestGroup() throws IOException {

        // 你的 matched_name 列表（第二列）
        String configFilePath = Context.result_folder + Context.jarName + "/search_result.log";    // 存放关键词的文件路径
        // 1. 从文件中读取关键词列表
        List<String> keywords = Files.readAllLines(Paths.get(configFilePath))
                .stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());

        ArrayList<String> tckTcompV0 = new ArrayList<>();
        ArrayList<String> javajet = new ArrayList<>();
        boolean javajetFlag = false;
        for (String keyword : keywords) {
            if (keyword.contains("#")) {
                javajetFlag = true;
            }
            if (javajetFlag) {
                javajet.add(keyword);
            } else {
                tckTcompV0.add(keyword);
            }
        }
//        System.out.println(tckTcompV0);
//        System.out.println(javajet);

        checkTCKTcompV0(tckTcompV0);
        checkJavajet(javajet);
    }

    public void checkTCKTcompV0(ArrayList<String> tckTcompV0) throws IOException {

        // mapping 文件路径
        Map<String, String> mappingTCK = readCSV2Map("/mappingTCK_TcompV0_TUJ.csv",1,2);

        Map<String, String> mappingAPI = readCSV2Map("/mappingAPITest.csv",0,1);

        // 2️⃣ 按 targetSet 逐个匹配
        Set<String> resultTUJ = new HashSet<>();
        Set<String> resultAPI = new HashSet<>();

        for (String target : tckTcompV0) {
            String tuj = mappingTCK.get(target);
            String apiTest = mappingAPI.get(target);

            if (tuj == null && apiTest == null) {
                System.err.println("[WARN] testcycle not found for module: " + target);
                continue;
            }

            addResult(resultTUJ, tuj);
            addResult(resultAPI, apiTest);

        }
        System.out.println("[Tck & TcompVo result]");
        Set<String> newList = resultTUJ.stream()
                .map(s -> "tuj/java/" + s)
                .collect(Collectors.toSet());
        printResult(newList);
        System.out.println("[API test result]");
        printResult(resultAPI);

    }

    private void addResult(Set<String> resultset, String item) {
        if (item != null && !item.isEmpty()) {
            // 拆 |
            for (String p : item.split("\\|")) {
                String trimmed = p.trim();
                if (!trimmed.isEmpty()) {
                    resultset.add(trimmed);
                }
            }
        }
    }

    private Map<String, String> readCSV2Map(String resourcePath,int keyIndex, int valueIndex) throws IOException {
        Path csvPath = Path.of(new File(this.getClass().getResource(resourcePath).getFile()).getAbsolutePath());
        // 1️⃣ 读取 CSV -> Map<matched_name, matched_path>
        return Files.lines(csvPath)
                .map(line -> line.split(",", -1))
                .collect(Collectors.toMap(
                        cols -> cols[keyIndex],     // matched_name
                        cols -> cols[valueIndex],     // matched_path
                        (existing, replacement) -> existing, // 若重复 key，保留第一个
                        HashMap::new
                ));
    }

    public void checkJavajet(ArrayList<String> javajet) throws IOException {
        // 用于去重存放结果路径
        Set<String> matchedPaths = new HashSet<>();

        // mapping 文件路径
        File mapping = new File(this.getClass().getResource("/mappingJavajetTUJ.csv").getFile());
        try (BufferedReader br = new BufferedReader(new FileReader(mapping))) {
            String line;

            while ((line = br.readLine()) != null) {

                // 跳过空行
                if (line.trim().isEmpty()) {
                    continue;
                }

                // 简单 CSV 切分（你的 CSV 没有引号/逗号转义）
                String[] parts = line.split(",", -1);
                if (parts.length < 2) {
                    continue;
                }

                String componentName = parts[0].trim();
                String path = parts[1].trim();

                if (javajet.contains(componentName) && !path.isEmpty()) {
                    matchedPaths.add("tuj/java/"+path);
                }
            }
        }
        System.out.println("[javajet result]");
        printResult(matchedPaths);
    }

    private void printResult(Set<String> result) {
        if (result.size() > 0) {
            System.out.println(String.join(",", result));
        } else {
            System.out.println("No result.");
        }
    }
}

