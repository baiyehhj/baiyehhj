package org.arithmetic;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件工具类，提供文件读写及数字列表格式化功能
 * 用于处理算术题生成器中的文件操作，包括题目、答案和成绩文件的读写
 */
public class FileUtils {
    // 写入文件
    public static void writeToFile(String filename, List<String> content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (String line : content) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    // 读取文件
    public static List<String> readFromFile(String filename) throws IOException {
        List<String> content = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    content.add(line.trim());
                }
            }
        }
        return content;
    }

    // 格式化数字列表
    public static String formatNumbers(List<Integer> numbers) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numbers.size(); i++) {
            sb.append(numbers.get(i));
            if (i < numbers.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}