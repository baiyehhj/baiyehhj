package org.arithmetic;

import java.io.IOException;
import java.util.*;

/**
 * 算术题生成器主类，支持题目生成和答案验证功能
 */
public class ArithmeticGenerator {
    private static final String EXERCISES_FILE = "Exercises.txt";
    private static final String ANSWERS_FILE = "Answers.txt";
    private static final String GRADE_FILE = "Grade.txt";
    private static final Set<String> expressionSet = new HashSet<>();
    private static final Random random = new Random();

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                printHelp();
                return;
            }

            // 验证答案模式
            if (args[0].equals("-e") && args.length == 4 && args[2].equals("-a")) {
                String exerciseFile = args[1];
                String answerFile = args[3];
                checkAnswers(exerciseFile, answerFile);
            }
            // 生成题目模式
            else if (args.length == 2 || args.length == 4) {
                int num = 10;
                int range = -1;

                // 解析参数
                for (int i = 0; i < args.length; i++) {
                    if (args[i].equals("-n")) {
                        num = Integer.parseInt(args[i + 1]);
                        if (num <= 0 || num > 10000) {
                            System.err.println("题目数量必须在1-10000之间");
                            return;
                        }
                    } else if (args[i].equals("-r")) {
                        range = Integer.parseInt(args[i + 1]);
                        if (range < 1) {
                            System.err.println("范围必须是正整数");
                            return;
                        }
                    }
                }

                if (range == -1) {
                    System.err.println("必须指定范围参数 -r");
                    printHelp();
                    return;
                }

                generateExercises(num, range);
            } else {
                printHelp();
            }
        } catch (NumberFormatException e) {
            System.err.println("参数必须是数字");
            printHelp();
        } catch (Exception e) {
            e.printStackTrace();
            printHelp();
        }
    }

    // 生成题目
    private static void generateExercises(int num, int range) throws IOException {
        expressionSet.clear();
        List<String> exercises = new ArrayList<>(num);
        List<String> answers = new ArrayList<>(num);

        System.out.println("正在生成" + num + "道题目，范围：" + range + "以内...");

        int maxAttempts = num * 100;
        int attempts = 0;
        int generated = 0;
        int fractionCount = 0; // 统计包含分数的题目数量

        while (generated < num && attempts < maxAttempts) {
            attempts++;

            // 生成1-3个运算符的表达式
            ExpressionTree tree = ExpressionUtils.generateExpressionTree(3, range);
            if (tree == null) {
                continue;
            }

            String expression = tree.toExpression(); // 这里会自动将 * / 转换为 × ÷
            String answer = tree.calculate();

            if (answer == null) {
                continue;
            }

            // 检查运算符数量
            if (ExpressionUtils.countOperators(expression) > 3) {
                continue;
            }

            // 检查是否包含分数（确保题目多样性）
            boolean containsFraction = expression.contains("/") || expression.contains("'");
            if (containsFraction) {
                fractionCount++;
            }

            // 如果已经生成了一半题目但分数题目太少，增加分数题目的概率
            if (generated > num / 2 && fractionCount < generated / 2) {
                if (!containsFraction) {
                    continue;
                }
            }

            // 使用表达式树进行标准化去重
            String normalized = tree.normalize();
            if (expressionSet.contains(normalized)) {
                continue;
            }

            // 符合条件，加入列表
            expressionSet.add(normalized);
            exercises.add("题目" + (generated + 1) + ": " + expression + " =");
            answers.add("答案" + (generated + 1) + ": " + answer);
            generated++;

            if (generated % 100 == 0) {
                System.out.println("已生成" + generated + "/" + num + "道题目");
            }
        }

        if (generated < num) {
            System.err.println("警告：仅生成" + generated + "道题");
        }

        try {
            FileUtils.writeToFile(EXERCISES_FILE, exercises);
            FileUtils.writeToFile(ANSWERS_FILE, answers);
        } catch (IOException e) {
            System.err.println("文件写入失败：" + e.getMessage());
            return;
        }

        System.out.println("生成完成！");
        System.out.println("题目文件：" + EXERCISES_FILE);
        System.out.println("答案文件：" + ANSWERS_FILE);
    }

    // 验证答案
    private static void checkAnswers(String exerciseFile, String answerFile) throws IOException {
        List<String> exercises = FileUtils.readFromFile(exerciseFile);
        List<String> answers = FileUtils.readFromFile(answerFile);

        if (exercises.size() != answers.size()) {
            System.err.println("错误：题目数量与答案数量不匹配");
            return;
        }

        List<Integer> correct = new ArrayList<>();
        List<Integer> wrong = new ArrayList<>();

        for (int i = 0; i < exercises.size(); i++) {
            String exercise = exercises.get(i);
            // 提取表达式部分（去掉"题目X: "和" ="）
            String expr = exercise.substring(exercise.indexOf(":") + 2, exercise.lastIndexOf("=")).trim();

            String userAnswer = answers.get(i);
            // 提取答案部分（去掉"答案X: "）
            if (userAnswer.contains(":")) {
                userAnswer = userAnswer.substring(userAnswer.indexOf(":") + 2).trim();
            }

            String correctAnswer = ExpressionUtils.calculateExpression(expr);

            if (correctAnswer != null && correctAnswer.equals(userAnswer)) {
                correct.add(i + 1);
            } else {
                wrong.add(i + 1);
            }
        }

        // 写入成绩
        List<String> gradeContent = new ArrayList<>();
        gradeContent.add("Correct: " + correct.size() + " (" + FileUtils.formatNumbers(correct) + ")");
        gradeContent.add("Wrong: " + wrong.size() + " (" + FileUtils.formatNumbers(wrong) + ")");

        try {
            FileUtils.writeToFile(GRADE_FILE, gradeContent);
            System.out.println("成绩已生成到 " + GRADE_FILE);
        } catch (IOException e) {
            System.err.println("生成成绩文件失败: " + e.getMessage());
        }
    }

    // 打印帮助信息
    private static void printHelp() {
        System.out.println("小学四则运算");
        System.out.println(".java 源文件：cd \"src\\main\\java\\org\\arithmetic\" ");
        System.out.println("执行编译文件：javac -encoding UTF-8 *.java ");
        System.out.println("回到java根目录：cd ..\\.. ");
        System.out.println("生成题目: java org.arithmetic.ArithmeticGenerator -n <数量> -r <范围> ");
        System.out.println("例如: java org.arithmetic.ArithmeticGenerator -n 100 -r 20 ");
        System.out.println("验证答案: java org.arithmetic.ArithmeticGenerator -e <exercisefile> -a <answerfile> ");
        System.out.println("例如: java org.arithmetic.ArithmeticGenerator -e Exercises.txt -a MyAnswers.txt ");
    }
}