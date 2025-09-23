package com.plagiarism.checker;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import java.io.*;
import java.util.ArrayList;

/**
 * 数据访问层：负责所有文件读写操作
 */
public class FileAccessor {

    private static final Charset[] SUPPORTED_CHARSETS = {
            StandardCharsets.UTF_8,
            Charset.forName("GBK"),
            StandardCharsets.ISO_8859_1
    };

    /**
     * 读取文件内容，增加缓冲流提高大文件处理性能
     * @param filePath 文件路径
     * @return 文件内容字符串
     * @throws IOException 读写异常
     */
    public String readFile(String filePath) throws IOException {
        // 使用缓冲流读取，更适合大文件，且保持一致的编码处理
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator()); // 保持系统一致的换行符
            }
            // 移除最后一个多余的换行符
            if (content.length() > 0) {
                content.setLength(content.length() - System.lineSeparator().length());
            }
            return content.toString();
        }
    }

    /**
     * 按行读取文件，保持原始行内容（仅去除首尾空格）
     * @param filePath 文件路径
     * @return 每行内容的列表
     * @throws IOException 读写异常
     */
    public List<String> readAllLines(String filePath) throws IOException {
        // 显式使用缓冲流，确保与readFile方法的读取逻辑一致
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line.trim()); // 仅去除首尾空格，保留原始行结构
            }
            return lines;
        }
    }

    // 从类路径读取资源文件（JAR 包内资源专用）
    public List<String> readAllLinesFromClasspath(String resourceName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            if (is == null) {
                throw new IOException("类路径中找不到资源：" + resourceName);
            }
            // 按行读取资源内容
            return reader.lines().collect(Collectors.toList());
        }
    }

    /**
     * 写入结果到文件
     * @param filePath 文件路径
     * @param content 要写入的内容
     * @throws IOException 读写异常
     */
    public void writeFile(String filePath, String content) throws IOException {
        // 确保父目录存在
        Files.createDirectories(Paths.get(filePath).getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath), StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }

}