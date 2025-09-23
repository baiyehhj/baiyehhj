package com.plagiarism.checker;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SimilarityCalculator {
    // 停用词集合（无意义高频词）
    private final Set<String> stopWords = new HashSet<>();
    // 高频词阈值（超过此频率将降低权重）
    private static final double HIGH_FREQ_THRESHOLD = 0.05;
    // 高频词权重衰减系数
    private static final double HIGH_FREQ_DECAY = 0.3;
    // 干扰字符正则（保留中英文、数字和常见标点，剔除其他干扰符号）
    private static final String NOISE_PATTERN = "[^a-zA-Z0-9\u4e00-\u9fa5，。,;！!？?]";
    // 最小词长度（过滤单字干扰词，如“丽”“医”）
    private static final int MIN_WORD_LENGTH = 2;

    public SimilarityCalculator() {
        // 初始化停用词
        ConfigLoader configLoader = new ConfigLoader();
        try {
            configLoader.loadStopwords();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    /**
     * 相似度计算：结合干扰词过滤、优化TF-IDF和权重融合
     */
    public double calculateSimilarity(List<String> words1, List<String> words2) {
        // 处理空输入
        if (words1.isEmpty() || words2.isEmpty()) {
            return 0.0;
        }

        // 1. 过滤干扰字符和单字干扰词
        List<String> cleanedWords1 = cleanNoiseWords(words1);
        List<String> cleanedWords2 = cleanNoiseWords(words2);

        // 2. 过滤停用词
        List<String> filteredWords1 = filterStopWords(cleanedWords1);
        List<String> filteredWords2 = filterStopWords(cleanedWords2);

        //处理过滤后为空的情况
        if (filteredWords1.isEmpty() || filteredWords2.isEmpty()) {
            return 0.0;
        }

        // 3. 构建文档集合（移除过度扩展的空文档，避免IDF计算异常）
        List<List<String>> documents = buildDocumentSet(filteredWords1, filteredWords2);

        // 4. 计算优化后的TF-IDF向量
        Map<String, Double> tfIdf1 = calculateOptimizedTfIdf(filteredWords1, documents);
        Map<String, Double> tfIdf2 = calculateOptimizedTfIdf(filteredWords2, documents);

        // 5. 优化高频词权重
        optimizeHighFrequencyWords(tfIdf1, filteredWords1.size());
        optimizeHighFrequencyWords(tfIdf2, filteredWords2.size());

        // 6. 计算核心相似度
        double cosineSimilarity = calculateCosineSimilarity(tfIdf1, tfIdf2);
        double jaccardSimilarity = calculateJaccardSimilarity(filteredWords1, filteredWords2);

        // 7. 融合相似度（6:4权重，平衡语义相关性和词汇匹配度）
        return 0.6 * cosineSimilarity + 0.4 * jaccardSimilarity;
    }

    /**
     * 过滤干扰词：剔除特殊字符、单字干扰词
     */
    private List<String> cleanNoiseWords(List<String> words) {
        List<String> cleaned = new ArrayList<>();
        for (String word : words) {
            // 步骤1：移除特殊干扰字符
            String cleanWord = word.replaceAll(NOISE_PATTERN, "").trim();
            // 步骤2：过滤单字和空字符串（保留长度≥2的有效词）
            if (!cleanWord.isEmpty() && cleanWord.length() >= MIN_WORD_LENGTH) {
                cleaned.add(cleanWord);
            }
        }
        return cleaned;
    }

    /**
     * 过滤停用词
     */
    private List<String> filterStopWords(List<String> words) {
        return words.stream()
                .filter(word -> !stopWords.contains(word))
                .collect(Collectors.toList());
    }

    /**
     * 构建文档集合（仅包含两篇目标文档，确保IDF计算合理）
     */
    private List<List<String>> buildDocumentSet(List<String> words1, List<String> words2) {
        List<List<String>> documents = new ArrayList<>();
        documents.add(words1);
        documents.add(words2);
        return documents;
    }

    /**
     * 优化TF-IDF计算：基于真实文档集合，提升核心词权重
     */
    private Map<String, Double> calculateOptimizedTfIdf(List<String> words, List<List<String>> documents) {
        Map<String, Integer> termFreq = buildFrequencyMap(words);
        Map<String, Double> tfIdfMap = new HashMap<>();
        int totalWords = words.size();
        int totalDocs = documents.size();

        for (Map.Entry<String, Integer> entry : termFreq.entrySet()) {
            String word = entry.getKey();
            int freq = entry.getValue();

            // 计算TF（词频/总词数）
            double tf = totalWords == 0 ? 0.0 : (double) freq / totalWords;

            // 计算IDF（log(总文档数/(包含该词的文档数+1))，避免除零）
            int docCount = 0;
            for (List<String> doc : documents) {
                if (doc.contains(word)) {
                    docCount++;
                }
            }
            double idf = Math.log((double) totalDocs / (docCount + 1));

            // 计算TF-IDF（核心词权重更高，干扰词权重更低）
            tfIdfMap.put(word, tf * idf);
        }
        return tfIdfMap;
    }

    /**
     * 优化高频词权重：超过阈值则衰减
     */
    private void optimizeHighFrequencyWords(Map<String, Double> tfIdfMap, int totalWords) {
        if (totalWords == 0) return;

        for (Map.Entry<String, Double> entry : tfIdfMap.entrySet()) {
            double freqRatio = entry.getValue() / totalWords;
            if (freqRatio > HIGH_FREQ_THRESHOLD) {
                entry.setValue(entry.getValue() * HIGH_FREQ_DECAY);
            }
        }
    }

    /**
     * 基于TF-IDF向量计算余弦相似度
     */
    private double calculateCosineSimilarity(Map<String, Double> vector1, Map<String, Double> vector2) {
        Set<String> allWords = new HashSet<>(vector1.keySet());
        allWords.addAll(vector2.keySet());

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String word : allWords) {
            double val1 = vector1.getOrDefault(word, 0.0);
            double val2 = vector2.getOrDefault(word, 0.0);
            dotProduct += val1 * val2;
            norm1 += val1 * val1;
            norm2 += val2 * val2;
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 计算杰卡德相似度（衡量词汇集合重叠度，适配文本长度差异）
     */
    private double calculateJaccardSimilarity(List<String> words1, List<String> words2) {
        Set<String> set1 = new HashSet<>(words1);
        Set<String> set2 = new HashSet<>(words2);

        // 计算交集和并集
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        if (union.isEmpty()) {
            return 0.0;
        }
        return (double) intersection.size() / union.size();
    }

    /**
     * 构建词频映射
     */
    private Map<String, Integer> buildFrequencyMap(List<String> words) {
        Map<String, Integer> freqMap = new HashMap<>();
        for (String word : words) {
            freqMap.put(word, freqMap.getOrDefault(word, 0) + 1);
        }
        return freqMap;
    }

}

