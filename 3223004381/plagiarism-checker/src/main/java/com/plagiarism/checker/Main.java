package com.plagiarism.checker;

import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        // 验证命令行参数
        if (args.length != 3) {
            System.err.println("用法: java -jar main.jar <原文文件路径> <抄袭版文件路径> <结果文件路径>");
            System.exit(1);
        }

        try {
            // 初始化组件
            FileAccessor fileAccessor = new FileAccessor();
            ConfigLoader configLoader = new ConfigLoader();

            // 加载配置文件
            String configDir = Paths.get("").toAbsolutePath().toString();
            configLoader.loadStopwords();
            configLoader.loadSynonyms();

            TextPreprocessor preprocessor = new TextPreprocessor(
                    configLoader.getStopwords(),
                    configLoader.getSynonyms()
            );

            // 使用从配置文件加载的停用词初始化计算器，确保与测试环境一致
            SimilarityCalculator calculator = new SimilarityCalculator();
            PlagiarismCheckerService service = new PlagiarismCheckerService(
                    fileAccessor, preprocessor, calculator
            );

            // 执行查重
            service.checkPlagiarism(args[0], args[1], args[2]);

        } catch (Exception e) {
            System.err.println("程序执行出错: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}