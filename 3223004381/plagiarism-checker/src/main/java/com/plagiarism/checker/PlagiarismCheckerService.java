package com.plagiarism.checker;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class PlagiarismCheckerService {
    private final FileAccessor fileAccessor;
    private final TextPreprocessor preprocessor;
    private final SimilarityCalculator calculator;
    private final DecimalFormat resultFormatter = new DecimalFormat("0.00"); // 保留两位小数

    public PlagiarismCheckerService(FileAccessor fileAccessor, TextPreprocessor preprocessor, SimilarityCalculator calculator) {
        this.fileAccessor = fileAccessor;
        this.preprocessor = preprocessor;
        this.calculator = calculator;
    }

    public void checkPlagiarism(String originalPath, String plagiarizedPath, String resultPath) throws IOException {
        SimilarityCalculator calculator = new SimilarityCalculator();

        String originalText = fileAccessor.readFile(originalPath);
        String plagiarizedText = fileAccessor.readFile(plagiarizedPath);

        List<String> originalWords = preprocessor.preprocess(originalText);
        List<String> plagiarizedWords = preprocessor.preprocess(plagiarizedText);

        double similarity = calculator.calculateSimilarity(originalWords, plagiarizedWords);
        similarity = Math.min(1.0, Math.max(0.0, similarity)); // 限制在[0,1]

        String result = resultFormatter.format(similarity * 100); // 转为百分比并格式化

        try {
            // 确保父目录存在
            java.nio.file.Path path = java.nio.file.Paths.get(resultPath);
            if (path.getParent() != null) {
                java.nio.file.Files.createDirectories(path.getParent());
            }
            fileAccessor.writeFile(resultPath, result);
        } catch (IOException e) {
            System.err.println("写入文件失败: " + e.getMessage());
            e.printStackTrace();
            throw e; // 重新抛出异常，让上层处理
        }
    }
}