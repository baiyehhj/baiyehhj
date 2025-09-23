package com.plagiarism.checker;

import java.io.IOException;
import java.util.*;

public class ConfigLoader {
    private Set<String> stopwords = new HashSet<>();
    private Map<String, String> synonyms = new HashMap<>();
    private final FileAccessor fileAccessor;  // 复用FileAccessor实例

    // 构造函数注入FileAccessor，便于测试和复用
    public ConfigLoader(FileAccessor fileAccessor) {
        this.fileAccessor = fileAccessor;
    }

    // 提供默认构造函数保持兼容性
    public ConfigLoader() {
        this.fileAccessor = new FileAccessor();
    }

    //从类路径加载 stopwords.txt（
    public void loadStopwords() throws IOException {
        List<String> lines = fileAccessor.readAllLinesFromClasspath("stopwords.txt");
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (!trimmedLine.isEmpty()) {
                stopwords.add(trimmedLine);
            }
        }
    }

    // 从类路径加载 synonyms.txt
    public void loadSynonyms() throws IOException {
        List<String> lines = fileAccessor.readAllLinesFromClasspath("synonyms.txt");
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) continue;

            String[] parts = trimmedLine.split(",");
            if (parts.length < 2) {
                System.err.println("无效的同义词配置行: " + trimmedLine);
                continue;
            }

            String standardWord = parts[parts.length - 1].trim();
            for (int i = 0; i < parts.length - 1; i++) {
                String synonym = parts[i].trim();
                if (!synonym.isEmpty() && !standardWord.isEmpty()) {
                    synonyms.put(synonym, standardWord);
                }
            }
        }
    }

    // getter方法
    public Set<String> getStopwords() { return stopwords; }
    public Map<String, String> getSynonyms() { return synonyms; }
}