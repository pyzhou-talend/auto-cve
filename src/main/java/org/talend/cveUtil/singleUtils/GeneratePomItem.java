package org.talend.cveUtil.singleUtils;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GeneratePomItem {

    static final String doubleSpace = "  ";

    /*
    dependency result generate exclusion and dependencyManager
    will remove duplicate ones
     */
    public static void main(String[] args) throws IOException {
        final GeneratePomItem generatePomItem = new GeneratePomItem();
//        generatePomItem.generateExclude("\t");
        generatePomItem.generatedependencyManagement("        ","${spring-boot-test.version}");

    }

    private void generateExclude(String space) throws IOException {
        final URL resource = getClass().getResource("/Exclusion");
        System.out.println(resource.getFile());
        final File file = new File(resource.getFile());
        final List<String> strings = FileUtils.readLines(file, Charset.defaultCharset());
        StringBuilder sb = new StringBuilder();
        sb.append(space).append("<exclusions>").append("\n");
        Set<String> alreadyExist = new HashSet<>();

        for (String line : strings) {
            System.out.println(line);
            final String[] split = line.substring(line.indexOf("-") + 2, line.lastIndexOf(":")).split(":");
            if(alreadyExist.contains(split[1])){
                continue;
            }else {
                alreadyExist.add(split[1]);
            }
            sb.append(space).append(doubleSpace).append("<exclusion>").append("\n");
            sb.append(space).append(doubleSpace).append(doubleSpace).append("<groupId>").append(split[0]).append("</groupId>").append("\n");
            sb.append(space).append(doubleSpace).append(doubleSpace).append("<artifactId>").append(split[1]).append("</artifactId>").append("\n");
            sb.append(space).append(doubleSpace).append("</exclusion>").append("\n");
        }
        sb.append(space).append("</exclusions>").append("\n");
        System.out.println(sb.toString());

    }

    private void generatedependencyManagement(String space,String version) throws IOException {
        final URL resource = getClass().getResource("/Exclusion");
        System.out.println(resource.getFile());
        final File file = new File(resource.getFile());
        final List<String> strings = FileUtils.readLines(file, Charset.defaultCharset());
        StringBuilder sb = new StringBuilder();
        Set<String> alreadyExist = new HashSet<>();
        sb.append(space).append("<dependencyManagement>").append("\n");
        sb.append(space).append(doubleSpace).append("<dependencies>").append("\n");
        for (String line : strings) {
            System.out.println(line);
            final String[] split = line.substring(line.indexOf("-") + 2, line.lastIndexOf(":")).split(":");
            if(alreadyExist.contains(split[1])){
                continue;
            }else {
                alreadyExist.add(split[1]);
            }
            sb.append(space).append(doubleSpace).append(doubleSpace).append("<dependency>").append("\n");
            sb.append(space).append(doubleSpace).append(doubleSpace).append(doubleSpace).append("<groupId>").append(split[0]).append("</groupId>").append("\n");
            sb.append(space).append(doubleSpace).append(doubleSpace).append(doubleSpace).append("<artifactId>").append(split[1]).append("</artifactId>").append("\n");
            sb.append(space).append(doubleSpace).append(doubleSpace).append(doubleSpace).append("<version>").append(version).append("</version>").append("\n");
            sb.append(space).append(doubleSpace).append(doubleSpace).append("</dependency>").append("\n");
        }
        sb.append(space).append(doubleSpace).append("</dependencies>").append("\n");
        sb.append(space).append("</dependencyManagement>").append("\n");
        System.out.println(sb.toString());

    }
}
