import org.arithmetic.ArithmeticGenerator;
import org.arithmetic.ExpressionTree;
import org.arithmetic.FileUtils;
import org.arithmetic.*;
import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

import static org.junit.jupiter.api.Assertions.*;


class ArithmeticTest {
    private static final String TEST_EXERCISES_FILE = "TestExercises.txt";
    private static final String TEST_ANSWERS_FILE = "TestAnswers.txt";
    private static final String TEST_GRADE_FILE = "TestGrade.txt";

    @BeforeEach
    @AfterEach
    void cleanupFiles() {
        deleteIfExists(TEST_EXERCISES_FILE);
        deleteIfExists(TEST_ANSWERS_FILE);
        deleteIfExists(TEST_GRADE_FILE);
        deleteIfExists("Exercises.txt");
        deleteIfExists("Answers.txt");
        deleteIfExists("Grade.txt");
    }

    private void deleteIfExists(String filename) {
        try {
            Files.deleteIfExists(Paths.get(filename));
        } catch (IOException e) {
            // 忽略删除错误
        }
    }

    // 测试用例1: 测试参数验证 - 缺少必需参数
    @Test
    void testMissingRequiredParameters() {
        String[] args1 = {"-n", "10"}; // 缺少-r参数
        String[] args2 = {"-r", "10"}; // 缺少-n参数
        String[] args3 = {}; // 无参数

        assertAll(
                () -> assertDoesNotThrow(() -> ArithmeticGenerator.main(args1)),
                () -> assertDoesNotThrow(() -> ArithmeticGenerator.main(args2)),
                () -> assertDoesNotThrow(() -> ArithmeticGenerator.main(args3))
        );
    }

    // 测试用例2: 测试数值范围控制
    @Test
    void testNumberRangeControl() throws IOException {
        String[] args = {"-n", "50", "-r", "10"};
        ArithmeticGenerator.main(args);

        List<String> exercises = FileUtils.readFromFile("Exercises.txt");
        List<String> answers = FileUtils.readFromFile("Answers.txt");

        // 验证所有数字都在范围内
        Pattern numberPattern = Pattern.compile("\\d+");
        Pattern fractionPattern = Pattern.compile("(\\d+)/(\\d+)");

        for (String exercise : exercises) {
            String expr = exercise.substring(exercise.indexOf(":") + 2, exercise.lastIndexOf("=")).trim();

            // 检查自然数
            Matcher numberMatcher = numberPattern.matcher(expr);
            while (numberMatcher.find()) {
                int num = Integer.parseInt(numberMatcher.group());
                assertTrue(num <= 10 && num >= 0, "数字超出范围: " + num);
            }

            // 检查分数分母
            Matcher fractionMatcher = fractionPattern.matcher(expr);
            while (fractionMatcher.find()) {
                int denominator = Integer.parseInt(fractionMatcher.group(2));
                assertTrue(denominator <= 10 && denominator > 0, "分母超出范围: " + denominator);
            }
        }
    }

    // 测试用例3: 测试减法结果非负
    @Test
    void testSubtractionNonNegative() throws IOException {
        String[] args = {"-n", "100", "-r", "20"};
        ArithmeticGenerator.main(args);

        List<String> exercises = FileUtils.readFromFile("Exercises.txt");
        List<String> answers = FileUtils.readFromFile("Answers.txt");

        for (int i = 0; i < exercises.size(); i++) {
            String exercise = exercises.get(i);
            String answer = answers.get(i).substring(answers.get(i).indexOf(":") + 2);

            if (exercise.contains("-")) {
                // 验证答案不为负
                assertFalse(answer.contains("-"), "减法结果为负: " + exercise + " = " + answer);

                // 使用表达式树验证所有减法运算
                String expr = exercise.substring(exercise.indexOf(":") + 2, exercise.lastIndexOf("=")).trim();
                ExpressionTree tree = ExpressionUtils.buildExpressionTree(expr);
                assertNotNull(tree, "无法构建表达式树: " + expr);

                // 验证所有减法运算结果非负
                assertTrue(ExpressionUtils.verifyAllSubtractionsNonNegative(tree),
                        "减法运算产生负数: " + expr);
            }
        }
    }

    // 测试用例4: 测试除法结果为真分数
    @Test
    void testDivisionResultProperFraction() throws IOException {
        String[] args = {"-n", "100", "-r", "20"};
        ArithmeticGenerator.main(args);

        List<String> exercises = FileUtils.readFromFile("Exercises.txt");
        List<String> answers = FileUtils.readFromFile("Answers.txt");

        for (int i = 0; i < exercises.size(); i++) {
            String exercise = exercises.get(i);

            if (exercise.contains("÷") || exercise.contains("/")) {
                // 使用表达式树验证所有除法运算
                String expr = exercise.substring(exercise.indexOf(":") + 2, exercise.lastIndexOf("=")).trim();
                ExpressionTree tree = ExpressionUtils.buildExpressionTree(expr);
                assertNotNull(tree, "无法构建表达式树: " + expr);

                // 验证所有除法运算结果为真分数
                assertTrue(ExpressionUtils.verifyAllDivisionsProperFraction(tree),
                        "除法运算结果不是真分数: " + expr);
            }
        }
    }

    // 测试用例5: 测试运算符数量不超过3个
    @Test
    void testOperatorCountLimit() throws IOException {
        String[] args = {"-n", "200", "-r", "50"};
        ArithmeticGenerator.main(args);

        List<String> exercises = FileUtils.readFromFile("Exercises.txt");

        for (String exercise : exercises) {
            String expr = exercise.substring(exercise.indexOf(":") + 2, exercise.lastIndexOf("=")).trim();
            int operatorCount = ExpressionUtils.countOperators(expr);
            assertTrue(operatorCount <= 3, "运算符数量超过3个: " + expr + " (数量: " + operatorCount + ")");
        }
    }

    // 测试用例6: 测试题目不重复
    @Test
    void testNoDuplicateExpressions() throws IOException {
        String[] args = {"-n", "100", "-r", "20"};
        ArithmeticGenerator.main(args);

        List<String> exercises = FileUtils.readFromFile("Exercises.txt");
        Set<String> normalizedExpressions = new HashSet<>();

        for (String exercise : exercises) {
            String expr = exercise.substring(exercise.indexOf(":") + 2, exercise.lastIndexOf("=")).trim();
            ExpressionTree tree = ExpressionUtils.buildExpressionTree(expr);
            assertNotNull(tree, "无法构建表达式树: " + expr);

            String normalized = tree.normalize();
            assertFalse(normalizedExpressions.contains(normalized),
                    "发现重复表达式: " + expr + " -> " + normalized);
            normalizedExpressions.add(normalized);
        }
    }

    // 测试用例7: 测试交换律去重
    @Test
    void testCommutativeLawDeduplication() {
        // 测试加法交换律
        ExpressionTree add1 = new ExpressionTree("+",
                new ExpressionTree("3"),
                new ExpressionTree("5")
        );
        ExpressionTree add2 = new ExpressionTree("+",
                new ExpressionTree("5"),
                new ExpressionTree("3")
        );
        assertEquals(add1.normalize(), add2.normalize(), "加法交换律去重失败");

        // 测试乘法交换律
        ExpressionTree mul1 = new ExpressionTree("*",
                new ExpressionTree("4"),
                new ExpressionTree("2")
        );
        ExpressionTree mul2 = new ExpressionTree("*",
                new ExpressionTree("2"),
                new ExpressionTree("4")
        );
        assertEquals(mul1.normalize(), mul2.normalize(), "乘法交换律去重失败");

        // 测试结合律去重
        ExpressionTree assoc1 = new ExpressionTree("+",
                new ExpressionTree("+",
                        new ExpressionTree("1"),
                        new ExpressionTree("2")
                ),
                new ExpressionTree("3")
        );
        ExpressionTree assoc2 = new ExpressionTree("+",
                new ExpressionTree("1"),
                new ExpressionTree("+",
                        new ExpressionTree("2"),
                        new ExpressionTree("3")
                )
        );
        assertEquals(assoc1.normalize(), assoc2.normalize(), "结合律去重失败");
    }

    // 测试用例8: 测试文件格式正确性
    @Test
    void testFileFormatCorrectness() throws IOException {
        String[] args = {"-n", "10", "-r", "10"};
        ArithmeticGenerator.main(args);

        List<String> exercises = FileUtils.readFromFile("Exercises.txt");
        List<String> answers = FileUtils.readFromFile("Answers.txt");

        // 验证题目文件格式
        Pattern exercisePattern = Pattern.compile("^题目\\d+: .+ =$");
        for (int i = 0; i < exercises.size(); i++) {
            String exercise = exercises.get(i);
            assertTrue(exercisePattern.matcher(exercise).matches(),
                    "题目格式错误: " + exercise);
            assertTrue(exercise.startsWith("题目" + (i + 1) + ":"),
                    "题目编号错误: " + exercise);
        }

        // 验证答案文件格式
        Pattern answerPattern = Pattern.compile("^答案\\d+: .+$");
        for (int i = 0; i < answers.size(); i++) {
            String answer = answers.get(i);
            assertTrue(answerPattern.matcher(answer).matches(),
                    "答案格式错误: " + answer);
            assertTrue(answer.startsWith("答案" + (i + 1) + ":"),
                    "答案编号错误: " + answer);
        }
    }

    // 测试用例9: 测试分数格式和运算
    @Test
    void testFractionFormatAndOperations() {
        // 测试真分数格式
        Fraction f1 = new Fraction(3, 5);
        assertEquals("3/5", f1.toString());

        // 测试带分数格式
        Fraction f2 = new Fraction(11, 4);
        assertEquals("2'3/4", f2.toString());

        // 测试分数加法
        Fraction f3 = new Fraction(1, 6);
        Fraction f4 = new Fraction(1, 8);
        Fraction sum = f3.add(f4);
        assertEquals("7/24", sum.toString(), "分数加法错误: 1/6 + 1/8 = 7/24");

        // 测试分数减法
        Fraction f5 = new Fraction(3, 4);
        Fraction f6 = new Fraction(1, 4);
        Fraction diff = f5.subtract(f6);
        assertEquals("1/2", diff.toString(), "分数减法错误: 3/4 - 1/4 = 1/2");

        // 测试分数乘法
        Fraction f7 = new Fraction(2, 3);
        Fraction f8 = new Fraction(3, 4);
        Fraction product = f7.multiply(f8);
        assertEquals("1/2", product.toString(), "分数乘法错误: 2/3 × 3/4 = 1/2");

        // 测试分数除法
        Fraction f9 = new Fraction(1, 2);
        Fraction f10 = new Fraction(2, 3);
        Fraction quotient = f9.divide(f10);
        assertEquals("3/4", quotient.toString(), "分数除法错误: 1/2 ÷ 2/3 = 3/4");
    }

    // 测试用例10: 测试答案验证功能
    @Test
    void testAnswerValidationFunction() throws IOException {
        // 创建测试文件
        List<String> exercises = Arrays.asList(
                "题目1: 3 + 5 =",
                "题目2: 10 - 4 =",
                "题目3: 6 × 2 =",
                "题目4: 8 ÷ 2 =",
                "题目5: 1/2 + 1/4 ="
        );
        List<String> userAnswers = Arrays.asList(
                "答案1: 8",   // 正确
                "答案2: 5",   // 错误
                "答案3: 12",  // 正确
                "答案4: 3",   // 错误
                "答案5: 3/4"  // 正确
        );

        FileUtils.writeToFile(TEST_EXERCISES_FILE, exercises);
        FileUtils.writeToFile(TEST_ANSWERS_FILE, userAnswers);

        String[] args = {"-e", TEST_EXERCISES_FILE, "-a", TEST_ANSWERS_FILE};
        ArithmeticGenerator.main(args);

        // 验证成绩文件
        assertTrue(Files.exists(Paths.get("Grade.txt")));
        List<String> grade = FileUtils.readFromFile("Grade.txt");

        assertEquals(2, grade.size());
        assertTrue(grade.get(0).contains("Correct: 3"));
        assertTrue(grade.get(0).contains("1, 3, 5"));
        assertTrue(grade.get(1).contains("Wrong: 2"));
        assertTrue(grade.get(1).contains("2, 4"));
    }

    // 测试用例11: 测试大规模生成（性能测试）
    @Test
    void testLargeScaleGeneration() throws IOException {
        String[] args = {"-n", "10000", "-r", "100"};

        long startTime = System.currentTimeMillis();
        ArithmeticGenerator.main(args);
        long endTime = System.currentTimeMillis();

        // 验证生成时间合理（10秒内）
        long duration = endTime - startTime;
        assertTrue(duration < 10000, "生成10000道题目时间过长: " + duration + "ms");

        // 验证文件内容
        List<String> exercises = FileUtils.readFromFile("Exercises.txt");
        List<String> answers = FileUtils.readFromFile("Answers.txt");

        assertEquals(10000, exercises.size());
        assertEquals(10000, answers.size());

        // 验证所有题目都符合要求
        for (int i = 0; i < 100; i++) { // 抽样检查100道题
            String exercise = exercises.get(i);
            String answer = answers.get(i);

            String expr = exercise.substring(exercise.indexOf(":") + 2, exercise.lastIndexOf("=")).trim();
            int operatorCount = ExpressionUtils.countOperators(expr);
            assertTrue(operatorCount <= 3, "运算符数量超过限制");

            // 验证答案计算正确
            String calculated = ExpressionUtils.calculateExpression(expr);
            String expected = answer.substring(answer.indexOf(":") + 2);
            assertEquals(expected, calculated, "答案计算错误: " + expr);
        }
    }

    // 测试用例12: 测试括号处理
    @Test
    void testParenthesesHandling() {
        // 测试括号表达式计算
        assertEquals("11", ExpressionUtils.calculateExpression("(3 + 4) * 2 - 3"));
        assertEquals("8", ExpressionUtils.calculateExpression("3 + 4 * 2 - 3"));
        assertEquals("5", ExpressionUtils.calculateExpression("20 / (3 + 1)"));

        // 测试嵌套括号
        assertEquals("7", ExpressionUtils.calculateExpression("(3 + (4 - 2)) * 2 - 3"));
    }

    // 测试用例13: 测试表达式树构建和计算
    @Test
    void testExpressionTreeConstruction() {
        // 测试简单表达式
        ExpressionTree simple = ExpressionUtils.buildExpressionTree("3 + 5");
        assertNotNull(simple);
        assertEquals("8", simple.calculate());

        // 测试带括号表达式
        ExpressionTree withParen = ExpressionUtils.buildExpressionTree("(3 + 5) * 2");
        assertNotNull(withParen);
        assertEquals("16", withParen.calculate());

        // 测试分数表达式
        ExpressionTree fraction = ExpressionUtils.buildExpressionTree("1/2 + 1/3");
        assertNotNull(fraction);
        assertEquals("5/6", fraction.calculate());
    }

    // 测试用例14: 测试边界情况
    @Test
    void testEdgeCases() {
        // 测试最小范围
        String[] args = {"-n", "5", "-r", "1"};
        assertDoesNotThrow(() -> ArithmeticGenerator.main(args));

        // 测试最小题目数量
        String[] args2 = {"-n", "1", "-r", "10"};
        assertDoesNotThrow(() -> ArithmeticGenerator.main(args2));

        // 测试无效参数处理
        String[] args3 = {"-n", "0", "-r", "10"};
        assertDoesNotThrow(() -> ArithmeticGenerator.main(args3));

        String[] args4 = {"-n", "10001", "-r", "10"};
        assertDoesNotThrow(() -> ArithmeticGenerator.main(args4));
    }




    // 新增测试用例15: 测试分数与整数混合运算
    @Test
    void testMixedFractionAndIntegerOperations() {
        // 测试整数加分数
        assertEquals("1'1/2", ExpressionUtils.calculateExpression("1 + 1/2"));
        // 测试分数加整数
        assertEquals("3'2/5", ExpressionUtils.calculateExpression("2/5 + 3"));
        // 测试整数减分数
        assertEquals("1'1/2", ExpressionUtils.calculateExpression("2 - 1/2"));
        // 测试分数减整数（需保证非负）
        assertEquals("1'1/2", ExpressionUtils.calculateExpression("3 - 2 + 1/2"));
        // 测试整数乘分数
        assertEquals("2", ExpressionUtils.calculateExpression("4 * 1/2"));
        // 测试分数乘整数
        assertEquals("1'1/2", ExpressionUtils.calculateExpression("1/2 * 3"));
        // 测试整数除分数
        assertEquals("4", ExpressionUtils.calculateExpression("2 ÷ 1/2"));
        // 测试分数除整数
        assertEquals("1/6", ExpressionUtils.calculateExpression("1/2 ÷ 3"));
    }

    // 新增测试用例16: 测试表达式去重的边界情况
    @Test
    void testDeduplicationEdgeCases() {
        // 测试带括号的交换律情况
        ExpressionTree bracketedAdd1 = new ExpressionTree("+",
                new ExpressionTree("*", new ExpressionTree("2"), new ExpressionTree("3")),  // 修正：直接使用三参数构造函数
                new ExpressionTree("5")
        );
        ExpressionTree bracketedAdd2 = new ExpressionTree("+",
                new ExpressionTree("5"),
                new ExpressionTree("*", new ExpressionTree("3"), new ExpressionTree("2"))   // 修正：直接使用三参数构造函数
        );
        assertEquals(bracketedAdd1.normalize(), bracketedAdd2.normalize(),
                "带括号的加法交换律去重失败");

        // 测试不同运算符组合的去重
        ExpressionTree expr1 = new ExpressionTree("+",
                new ExpressionTree("-", new ExpressionTree("5"), new ExpressionTree("3")),  // 修正
                new ExpressionTree("2")
        );
        ExpressionTree expr2 = new ExpressionTree("+",
                new ExpressionTree("2"),
                new ExpressionTree("-", new ExpressionTree("5"), new ExpressionTree("3"))  // 修正
        );
        assertEquals(expr1.normalize(), expr2.normalize(), "混合运算符交换律去重失败");
    }

    // 新增测试用例17: 测试文件读写异常处理
    @Test
    void testFileIOErrorHandling() {
        // 测试读取不存在的文件
        assertDoesNotThrow(() -> {
            String[] args = {"-e", "NonExistentExercises.txt", "-a", "Answers.txt"};
            ArithmeticGenerator.main(args);
        }, "读取不存在的文件应能正常处理");

        // 测试写入只读文件（模拟）
        assertDoesNotThrow(() -> {
            // 创建只读文件
            File readOnlyFile = new File("ReadOnlyExercises.txt");
            FileUtils.writeToFile("ReadOnlyExercises.txt", Collections.singletonList("test"));
            readOnlyFile.setReadOnly();

            // 尝试写入
            String[] args = {"-n", "5", "-r", "10"};
            ArithmeticGenerator.main(args);

            // 清理
            readOnlyFile.setWritable(true);
            readOnlyFile.delete();
        }, "写入只读文件应能正常处理");
    }

    // 新增测试用例18: 集成测试 - 完整流程验证
    @Test
    void testCompleteWorkflow() throws IOException {
        // 1. 生成题目
        String[] generateArgs = {"-n", "20", "-r", "15"};
        ArithmeticGenerator.main(generateArgs);

        // 验证生成结果
        List<String> exercises = FileUtils.readFromFile("Exercises.txt");
        List<String> answers = FileUtils.readFromFile("Answers.txt");
        assertEquals(20, exercises.size());
        assertEquals(20, answers.size());

        // 2. 复制答案作为用户答案（模拟全部正确）
        List<String> userAnswers = new ArrayList<>(answers);
        FileUtils.writeToFile("UserAnswers.txt", userAnswers);

        // 3. 验证答案
        String[] gradeArgs = {"-e", "Exercises.txt", "-a", "UserAnswers.txt"};
        ArithmeticGenerator.main(gradeArgs);

        // 验证评分结果
        List<String> grade = FileUtils.readFromFile("Grade.txt");
        assertTrue(grade.get(0).contains("Correct: 20"), "全部正确的评分错误");
        assertTrue(grade.get(1).contains("Wrong: 0"), "全部正确时不应有错误");

        // 4. 模拟部分错误答案
        List<String> wrongAnswers = new ArrayList<>();
        for (int i = 0; i < answers.size(); i++) {
            if (i % 5 == 0) { // 每5题错1题
                wrongAnswers.add(answers.get(i).replaceAll("\\d+", "999"));
            } else {
                wrongAnswers.add(answers.get(i));
            }
        }
        FileUtils.writeToFile("WrongAnswers.txt", wrongAnswers);

        // 5. 重新验证
        String[] wrongGradeArgs = {"-e", "Exercises.txt", "-a", "WrongAnswers.txt"};
        ArithmeticGenerator.main(wrongGradeArgs);

        // 验证评分结果
        List<String> wrongGrade = FileUtils.readFromFile("Grade.txt");
        assertTrue(wrongGrade.get(0).contains("Correct: 16"), "部分正确的评分错误");
        assertTrue(wrongGrade.get(1).contains("Wrong: 4"), "错误数量计算错误");
    }

    // 新增测试用例19: 测试分数化简功能
    @Test
    void testFractionSimplification() {
        // 测试可化简分数
        Fraction f1 = new Fraction(4, 6);
        assertEquals("2/3", f1.toString(), "分数化简错误");

        // 测试整数作为分数
        Fraction f2 = new Fraction(5, 1);
        assertEquals("5", f2.toString(), "整数应显示为整数形式");

        // 测试分子为0的情况
        Fraction f3 = new Fraction(0, 5);
        assertEquals("0", f3.toString(), "零应显示为0");

        // 测试负分数（虽然生成中不应出现，但计算逻辑应支持）
        Fraction f4 = new Fraction(-3, 6);
        assertEquals("-1/2", f4.toString(), "负分数化简错误");

        // 测试带分数化简
        Fraction f5 = new Fraction(10, 3);
        assertEquals("3'1/3", f5.toString(), "带分数化简错误");
    }

}