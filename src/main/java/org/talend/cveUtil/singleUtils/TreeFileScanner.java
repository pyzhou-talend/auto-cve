package org.talend.cveUtil.singleUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class TreeFileScanner {
    /*
    扫描一个tree文件中包含某个jar的所有行
     */

    public static void main(String[] args) {
        // 指定要扫描的文件夹路径
        String folderPath = "D:\\git\\connectors-ee\\tree.log";

        Path folder = Paths.get(folderPath);

        try (Stream<Path> paths = Files.walk(folder, 2)) { // 深度为1，即只遍历当前文件夹及其直接子文件
            paths.filter(Files::isRegularFile) // 只处理普通文件
                    .forEach(filePath -> {
                        try {
                            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
                            for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
                                String line = lines.get(lineNumber);
                                if (line.contains("io.netty") && !line.contains("4.1.108") && !line.contains("2.0.61.Final")) {
                                    System.out.println("File: " + filePath + ", Line " + (lineNumber + 1) + ": " + line);
//                                    System.out.println(line);
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Error reading file: " + filePath + ". Error: " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error walking the file tree. Error: " + e.getMessage());
        }
    }
}