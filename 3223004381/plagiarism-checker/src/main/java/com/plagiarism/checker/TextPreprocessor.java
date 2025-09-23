package com.plagiarism.checker;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import java.util.*;
import java.util.stream.Collectors;

public class TextPreprocessor {
    private Set<String> stopwords;
    private Map<String, String> synonyms;

    // 清洗规则：只保留中英文、数字，移除所有标点和特殊字符
    private static final String NOISE_PATTERN = "[^a-zA-Z0-9\u4e00-\u9fa5，。,;！!？?]";

    public TextPreprocessor(Set<String> stopwords, Map<String, String> synonyms) {
        this.stopwords = new HashSet<>(stopwords); // 防御性复制
        this.synonyms = new HashMap<>(synonyms);   // 防御性复制
    }

    public List<String> preprocess(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 文本清洗：移除所有非文本字符，统一转为小写
        String cleanedText = text.replaceAll(NOISE_PATTERN, " ")
                .toLowerCase()
                .replaceAll("\\s+", " ") // 合并连续空格
                .trim();

        if (cleanedText.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. HanLP分词
        List<Term> terms = HanLP.segment(cleanedText);

        // 3. 处理流程：过滤空字符串→停用词→同义词替换
        return terms.stream()
                .map(term -> term.word.trim())
                .filter(word -> !word.isEmpty())          // 过滤空字符串
                .filter(word -> !stopwords.contains(word)) // 过滤停用词
                .map(this::replaceSynonym)                // 替换同义词
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        Collections::unmodifiableList
                ));
    }

    // 确保同义词双向替换
    private String replaceSynonym(String word) {
        return synonyms.getOrDefault(word, word);
    }
}