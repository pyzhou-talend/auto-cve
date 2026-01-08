package org.talend.cveUtil.fileVisitors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchTreeLogFV extends SimpleFileVisitor<Path> {
    private static final Logger log = LoggerFactory.getLogger(SearchTreeLogFV.class);

    public SearchTreeLogFV(String jarName, String suffix, List<File> fileList, List<File> errorList){
        KEYWORD_PATTERN = Pattern.compile("\\b" + jarName + "\\b", Pattern.CASE_INSENSITIVE);
        this.suffix = suffix;
        this.fileList = fileList;
        this.errorList = errorList;

    }

    Pattern KEYWORD_PATTERN ; // 正则表达式匹配"artifactId"
    List<File> fileList = new ArrayList<>();
    List<File> errorList;

    String suffix;


        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (Files.isRegularFile(file) && file.toString().endsWith(suffix)) {
            boolean found = false;
            boolean errorFound = false;
            try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                String line;
                int lineNumber = 0;
                while ((line = reader.readLine()) != null) {
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

}
