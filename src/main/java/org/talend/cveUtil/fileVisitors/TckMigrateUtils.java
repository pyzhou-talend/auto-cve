package org.talend.cveUtil.fileVisitors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class TckMigrateUtils {

    public static Map<String, String> readMappingFile(Class clazz,String propertiesName) throws IOException {
        InputStream resourceAsStream = clazz.getResourceAsStream(propertiesName);
        Map<String, String> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || !line.contains("=")) {
                    continue; // 跳过空行和无效行
                }

                String[] parts = line.split("=", 2);
                String key = parts[0].trim();
                String value = parts.length > 1 ? parts[1].trim() : "";
                map.put(key, value);
            }
        }
        return map;
    }

    public static Map<String, Map<String, String>> readTableConfig(Class clazz,String propertiesName) throws IOException {
        InputStream resourceAsStream = clazz.getResourceAsStream(propertiesName);
        Map<String, Map<String, String>> resultMap = new HashMap<>();
        Map<String, String> currentSection = null;
        String currentSectionKey = null;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                if (line.startsWith("#")) {
                    currentSectionKey = line.substring(1).trim();
                    currentSection = new HashMap<>();
                    resultMap.put(currentSectionKey, currentSection);
                } else if (line.contains("=") && currentSection != null) {
                    String[] parts = line.split("=", 2);
                    currentSection.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
        return resultMap;
    }


    public static String formatEnum(String input) {
        input = input.replaceAll("^\"|\"$", "");
        input = input.replaceAll("([a-z])([A-Z])", "$1_$2");
        return input.toUpperCase();
    }
}
