package com.plagiarism.checker;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class PlagiarismCheckerTest {
    private TextPreprocessor preprocessor;
    private SimilarityCalculator calculator;
    private FileAccessor fileAccessor;

    @Before
    public void setUp() {
        // 1. 初始化完整停用词表（严格对应 stopwords.txt 内容，去除重复项）
        Set<String> stopwords = new HashSet<>();
        stopwords.add("的");
        stopwords.add("是");
        stopwords.add("在");
        stopwords.add("有");
        stopwords.add("不");
        stopwords.add("人");
        stopwords.add("我");
        stopwords.add("你");
        stopwords.add("他");
        stopwords.add("她");
        stopwords.add("它");
        stopwords.add("我们");
        stopwords.add("你们");
        stopwords.add("他们");
        stopwords.add("她们");
        stopwords.add("它们");
        stopwords.add("和");
        stopwords.add("或");
        stopwords.add("与");
        stopwords.add("及");
        stopwords.add("了");
        stopwords.add("着");
        stopwords.add("过");
        stopwords.add("也");
        stopwords.add("还");
        stopwords.add("都");
        stopwords.add("只");
        stopwords.add("又");
        stopwords.add("就");
        stopwords.add("但");
        stopwords.add("而");
        stopwords.add("却");
        stopwords.add("之");
        stopwords.add("以");
        stopwords.add("于");
        stopwords.add("因");
        stopwords.add("为");
        stopwords.add("所以");
        stopwords.add("虽然");
        stopwords.add("但是");
        stopwords.add("而且");
        stopwords.add("并且");
        stopwords.add("不过");
        stopwords.add("如果");
        stopwords.add("那么");
        stopwords.add("只有");
        stopwords.add("才");
        stopwords.add("只要");
        stopwords.add("更");
        stopwords.add("最");
        stopwords.add("再");
        stopwords.add("很");
        stopwords.add("太");
        stopwords.add("极");
        stopwords.add("极了");
        stopwords.add("非常");
        stopwords.add("十分");
        stopwords.add("格外");
        stopwords.add("尤其");
        stopwords.add("稍微");
        stopwords.add("略");
        stopwords.add("一点儿");
        stopwords.add("一些");
        stopwords.add("有的");
        stopwords.add("有些");
        stopwords.add("各个");
        stopwords.add("每个");
        stopwords.add("凡");
        stopwords.add("凡是");
        stopwords.add("所有");
        stopwords.add("全部");
        stopwords.add("任何");
        stopwords.add("每");
        stopwords.add("各");
        stopwords.add("另");
        stopwords.add("另外");
        stopwords.add("其他");
        stopwords.add("其余");
        stopwords.add("这");
        stopwords.add("那");
        stopwords.add("这些");
        stopwords.add("那些");
        stopwords.add("此");
        stopwords.add("彼");
        stopwords.add("这里");
        stopwords.add("那里");
        stopwords.add("这儿");
        stopwords.add("那儿");
        stopwords.add("这么");
        stopwords.add("那么");
        stopwords.add("这样");
        stopwords.add("那样");
        stopwords.add("这么样");
        stopwords.add("那么样");
        stopwords.add("多");
        stopwords.add("少");
        stopwords.add("多少");
        stopwords.add("几");
        stopwords.add("一");
        stopwords.add("个");
        stopwords.add("许多");
        stopwords.add("很多");
        stopwords.add("不少");
        stopwords.add("少数");
        stopwords.add("多数");
        stopwords.add("一切");
        stopwords.add("全");
        stopwords.add("整个");
        stopwords.add("全体");
        stopwords.add("总共");
        stopwords.add("一共");
        stopwords.add("一起");
        stopwords.add("一同");
        stopwords.add("一道");
        stopwords.add("互相");
        stopwords.add("彼此");
        stopwords.add("自己");
        stopwords.add("自身");
        stopwords.add("本身");
        stopwords.add("亲自");
        stopwords.add("大家");
        stopwords.add("大伙");
        stopwords.add("别人");
        stopwords.add("他人");
        stopwords.add("人家");
        stopwords.add("谁");
        stopwords.add("什么");
        stopwords.add("哪");
        stopwords.add("哪里");
        stopwords.add("何时");
        stopwords.add("何地");
        stopwords.add("为何");
        stopwords.add("怎样");
        stopwords.add("如何");
        stopwords.add("啊");
        stopwords.add("呀");
        stopwords.add("呢");
        stopwords.add("吧");
        stopwords.add("吗");
        stopwords.add("哟");
        stopwords.add("哩");
        stopwords.add("啦");
        stopwords.add("哼");
        stopwords.add("哦");
        stopwords.add("嗬");
        stopwords.add("唉");
        stopwords.add("呸");
        stopwords.add("咚");
        stopwords.add("当");
        stopwords.add("哗");
        stopwords.add("轰");
        stopwords.add("嘭");
        stopwords.add("啪");
        stopwords.add("的话");
        stopwords.add("等等");
        stopwords.add("云云");
        stopwords.add("如此");
        stopwords.add("如是");
        stopwords.add("而已");
        stopwords.add("罢了");
        stopwords.add("也好");
        stopwords.add("也罢");
        stopwords.add("就是了");
        stopwords.add("就是");
        stopwords.add("便是");
        stopwords.add("即是");
        stopwords.add("而是");
        stopwords.add("不是");
        stopwords.add("真是");
        stopwords.add("真是的");
        stopwords.add("的确");
        stopwords.add("确实");
        stopwords.add("诚然");
        stopwords.add("固然");
        stopwords.add("当然");
        stopwords.add("自然");
        stopwords.add("显然");
        stopwords.add("明明");
        stopwords.add("其实");
        stopwords.add("究竟");
        stopwords.add("到底");
        stopwords.add("毕竟");
        stopwords.add("终究");
        stopwords.add("终于");
        stopwords.add("总算");
        stopwords.add("偏偏");
        stopwords.add("偏");
        stopwords.add("反倒");
        stopwords.add("反而");
        stopwords.add("反正");
        stopwords.add("横竖");
        stopwords.add("索性");
        stopwords.add("干脆");
        stopwords.add("简直");
        stopwords.add("几乎");
        stopwords.add("将近");
        stopwords.add("大约");
        stopwords.add("大概");
        stopwords.add("大致");
        stopwords.add("大体");
        stopwords.add("多半");
        stopwords.add("也许");
        stopwords.add("或许");
        stopwords.add("可能");
        stopwords.add("恐怕");
        stopwords.add("仿佛");
        stopwords.add("好像");
        stopwords.add("似乎");
        stopwords.add("宛如");
        stopwords.add("犹如");
        stopwords.add("如同");
        stopwords.add("恰似");
        stopwords.add("好比");
        stopwords.add("一般");
        stopwords.add("一样");
        stopwords.add("都一");
        stopwords.add("似的");
        stopwords.add("般");
        stopwords.add("地");
        stopwords.add("得");
        stopwords.add("所");
        stopwords.add("之类");
        stopwords.add("一类");
        stopwords.add("一个");
        stopwords.add("什么的");

        // 2. 初始化完整同义词表（严格对应 synonyms.txt，采用“多对一”映射，统一到最后一个标准词）
        Map<String, String> synonyms = new HashMap<>();
        // 周天、周日 → 星期天（标准词）
        synonyms.put("周天", "星期天");
        synonyms.put("周日", "星期天");
        // 晴朗、晴好 → 晴（标准词）
        synonyms.put("晴朗", "晴");
        synonyms.put("晴好", "晴");
        // 影片、片子 → 电影（标准词）
        synonyms.put("影片", "电影");
        synonyms.put("片子", "电影");
        // 爸爸、爹爹 → 父亲（标准词）
        synonyms.put("爸爸", "父亲");
        synonyms.put("爹爹", "父亲");
        // 妈妈、娘亲 → 母亲（标准词）
        synonyms.put("妈妈", "母亲");
        synonyms.put("娘亲", "母亲");
        // 高兴、开心 → 快乐（标准词）
        synonyms.put("高兴", "快乐");
        synonyms.put("开心", "快乐");
        // 悲伤、难过 → 伤心（标准词）
        synonyms.put("悲伤", "伤心");
        synonyms.put("难过", "伤心");
        // 计算机、电脑 → 计算机（标准词，按原配置保留重复）
        synonyms.put("计算机", "计算机");
        synonyms.put("电脑", "计算机");
        // 因特网、互联网 → 互联网（标准词，按原配置保留重复）
        synonyms.put("因特网", "互联网");
        synonyms.put("互联网", "互联网");
        // 星期一、周一 → 星期一（标准词）
        synonyms.put("星期一", "星期一");
        synonyms.put("周一", "星期一");
        // 星期二、周二 → 星期二（标准词）
        synonyms.put("星期二", "星期二");
        synonyms.put("周二", "星期二");
        // 星期三、周三 → 星期三（标准词）
        synonyms.put("星期三", "星期三");
        synonyms.put("周三", "星期三");
        // 星期四、周四 → 星期四（标准词）
        synonyms.put("星期四", "星期四");
        synonyms.put("周四", "星期四");
        // 星期五、周五 → 星期五（标准词）
        synonyms.put("星期五", "星期五");
        synonyms.put("周五", "星期五");
        // 星期六、周六 → 星期六（标准词）
        synonyms.put("星期六", "星期六");
        synonyms.put("周六", "星期六");
        // 拂晓、黎明 → 早晨（标准词）
        synonyms.put("拂晓", "早晨");
        synonyms.put("黎明", "早晨");
        // 傍晚、黄昏 → 晚上（标准词）
        synonyms.put("傍晚", "晚上");
        synonyms.put("黄昏", "晚上");
        // 大夫、医生 → 医生（标准词，按原配置保留重复）
        synonyms.put("大夫", "医生");
        synonyms.put("医生", "医生");
        // 孩童、孩子 → 儿童（标准词）
        synonyms.put("孩童", "儿童");
        synonyms.put("孩子", "儿童");
        // 马铃薯、土豆 → 土豆（标准词，按原配置保留重复）
        synonyms.put("马铃薯", "土豆");
        synonyms.put("土豆", "土豆");
        // 番茄、西红柿 → 西红柿（标准词，按原配置保留重复）
        synonyms.put("番茄", "西红柿");
        synonyms.put("西红柿", "西红柿");
        // 自行车、单车 → 自行车（标准词，按原配置保留重复）
        synonyms.put("自行车", "自行车");
        synonyms.put("单车", "自行车");

        // 3. 初始化组件（确保预处理、计算、文件操作组件配置一致）
        preprocessor = new TextPreprocessor(stopwords, synonyms);
        calculator = new SimilarityCalculator();
        fileAccessor = new FileAccessor();
    }
    // 测试1：完全相同的文本
    @Test
    public void testIdenticalTexts() {
        String text1 = "今天是星期天，天气晴，今天晚上我要去看电影。";
        String text2 = "今天是星期天，天气晴，今天晚上我要去看电影。";

        List<String> words1 = preprocessor.preprocess(text1);
        List<String> words2 = preprocessor.preprocess(text2);
        double similarity = calculator.calculateSimilarity(words1, words2);

        assertEquals(1.0, similarity, 0.01);
    }

    // 测试2：示例中的抄袭文本
    @Test
    public void testSamplePlagiarism() {
        String original = "今天是星期天，天气晴，今天晚上我要去看电影。";
        String plagiarized = "今天是周天，天气晴朗，我晚上要去看电影。";

        List<String> words1 = preprocessor.preprocess(original);
        List<String> words2 = preprocessor.preprocess(plagiarized);
        double similarity = calculator.calculateSimilarity(words1, words2);

        // 预期相似度较高，因为经过同义词替换后很多词相同
        assertTrue(similarity > 0.8);
    }

    // 测试3：完全不同的文本
    @Test
    public void testCompletelyDifferentTexts() {
        String text1 = "计算机科学是一门研究计算理论和实践的学科。";
        String text2 = "猫是一种常见的家庭宠物，喜欢吃鱼和老鼠。";

        List<String> words1 = preprocessor.preprocess(text1);
        List<String> words2 = preprocessor.preprocess(text2);
        double similarity = calculator.calculateSimilarity(words1, words2);

        assertEquals(0.0, similarity, 0.01);
    }

    // 测试4：其中一个文本为空
    @Test
    public void testEmptyText() {
        String text1 = "这是一个测试文本。";
        String text2 = "";

        List<String> words1 = preprocessor.preprocess(text1);
        List<String> words2 = preprocessor.preprocess(text2);
        double similarity = calculator.calculateSimilarity(words1, words2);

        assertEquals(0.0, similarity, 0.01);
    }

    // 测试5：短文本部分相似
    @Test
    public void testPartiallySimilarShortTexts() {
        String text1 = "苹果是一种水果，味道很甜。";
        String text2 = "苹果是一种水果，颜色有红色和绿色。";

        List<String> words1 = preprocessor.preprocess(text1);
        List<String> words2 = preprocessor.preprocess(text2);
        double similarity = calculator.calculateSimilarity(words1, words2);
        // 预期有一定相似度
        assertTrue(similarity > 0.4 && similarity < 0.8);
    }

    // 测试6：长文本部分抄袭
    @Test
    public void testPartiallyPlagiarizedLongText() {
        String text1 = "Java是一种广泛使用的编程语言，由Sun Microsystems开发，后来被Oracle收购。" +
                      "Java的特点是跨平台性，通过JVM实现一次编写，到处运行。它是一种面向对象的语言，" +
                      "具有垃圾回收机制，提高了开发效率。";

        String text2 = "Java编程语言由Sun公司开发，现在属于Oracle。它支持跨平台运行，这是通过JVM实现的。" +
                      "C++也是一种面向对象的语言，但没有自动垃圾回收功能。";

        List<String> words1 = preprocessor.preprocess(text1);
        List<String> words2 = preprocessor.preprocess(text2);
        double similarity = calculator.calculateSimilarity(words1, words2);
        // 预期有中度相似度
        assertTrue(similarity > 0.3 && similarity < 0.8);
    }

    // 测试7：标点符号处理
    @Test
    public void testPunctuationHandling() {
        String text1 = "Hello, world! This is a test.";
        String text2 = "Hello world. This is a test!";

        List<String> words1 = preprocessor.preprocess(text1);
        List<String> words2 = preprocessor.preprocess(text2);
        double similarity = calculator.calculateSimilarity(words1, words2);

        assertEquals(1.0, similarity, 0.01);
    }

    // 测试8：大小写处理
    @Test
    public void testCaseInsensitivity() {
        String text1 = "Java Python C++";
        String text2 = "java python c++";

        List<String> words1 = preprocessor.preprocess(text1);
        List<String> words2 = preprocessor.preprocess(text2);
        double similarity = calculator.calculateSimilarity(words1, words2);

        assertEquals(1.0, similarity, 0.01);
    }

    // 测试9：同义词替换效果
    @Test
    public void testSynonymReplacement() {
        String text1 = "今天是星期天，天气晴。";
        String text2 = "今天是周天，天气晴朗。";

        List<String> words1 = preprocessor.preprocess(text1);
        List<String> words2 = preprocessor.preprocess(text2);
        double similarity = calculator.calculateSimilarity(words1, words2);

        // 经过同义词替换后应该完全相同
        assertEquals(1.0, similarity, 0.01);
    }

    // 测试10：停用词过滤效果
    @Test
    public void testStopwordFiltering() {
        String text1 = "这是一个测试，的是在有和就不人都一。";
        String text2 = "这是测试。";

        List<String> words1 = preprocessor.preprocess(text1);
        List<String> words2 = preprocessor.preprocess(text2);
        double similarity = calculator.calculateSimilarity(words1, words2);

        // 停用词被过滤后应该完全相同
        assertEquals(1.0, similarity, 0.01);
    }

    // 测试11：文件读写功能
    @Test
    public void testFileOperations() throws IOException {
        // 创建临时文件
        File originalFile = File.createTempFile("original", ".txt");
        File plagiarizedFile = File.createTempFile("plagiarized", ".txt");
        File resultFile = File.createTempFile("result", ".txt");

        // 写入测试内容
        Files.write(originalFile.toPath(), "测试文件内容".getBytes());
        Files.write(plagiarizedFile.toPath(), "测试文件内容".getBytes());

        // 执行查重
        PlagiarismCheckerService service = new PlagiarismCheckerService(
                fileAccessor, preprocessor, calculator);
        service.checkPlagiarism(
                originalFile.getAbsolutePath(),
                plagiarizedFile.getAbsolutePath(),
                resultFile.getAbsolutePath());

        // 验证结果
        String result = new String(Files.readAllBytes(resultFile.toPath()));
        assertEquals("100.00", result);

        // 清理临时文件
        originalFile.deleteOnExit();
        plagiarizedFile.deleteOnExit();
        resultFile.deleteOnExit();
    }
}
