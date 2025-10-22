package org.arithmetic;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表达式工具类，提供表达式生成、解析、计算及验证等功能
 * 支持自然数、真分数、带分数的四则运算表达式处理
 */
public class ExpressionUtils {
    private static final char[] OPERATORS = {'+', '-', '*', '/'};
    private static final Random random = new Random();

    // 运算符优先级
    private static final Map<String, Integer> OPERATOR_PRIORITY = new HashMap<>();
    static {
        OPERATOR_PRIORITY.put("+", 1);
        OPERATOR_PRIORITY.put("-", 1);
        OPERATOR_PRIORITY.put("*", 2);
        OPERATOR_PRIORITY.put("/", 2);
    }

    // 生成表达式树
    public static ExpressionTree generateExpressionTree(int maxOperators, int range) {
        return ExpressionTree.generateTree(maxOperators, range);
    }

    // 表达式树构建
    public static ExpressionTree buildExpressionTree(String expression) {
        try {
            // 统一运算符格式：将显示符号转换为计算符号
            expression = expression.replace('×', '*').replace('÷', '/');

            // 移除最外层的括号（如果有）
            expression = removeOuterParentheses(expression.trim());

            // 首先检查是否是分数形式（包括带分数）
            if (isStandaloneFraction(expression)) {
                return new ExpressionTree(expression);
            }

            // 查找优先级最低的运算符（作为根节点）
            int splitIndex = findLowestPriorityOperator(expression);

            if (splitIndex == -1) {
                // 没有运算符，直接返回数字节点
                return new ExpressionTree(expression);
            }

            // 分割表达式
            String leftExpr = expression.substring(0, splitIndex).trim();
            String operator = String.valueOf(expression.charAt(splitIndex));
            String rightExpr = expression.substring(splitIndex + 1).trim();

            // 递归构建左右子树
            ExpressionTree leftTree = buildExpressionTree(leftExpr);
            ExpressionTree rightTree = buildExpressionTree(rightExpr);

            if (leftTree == null || rightTree == null) {
                return null;
            }

            return new ExpressionTree(operator, leftTree, rightTree);
        } catch (Exception e) {
            System.err.println("构建表达式树失败: " + expression);
            return null;
        }
    }

    // 检查是否是独立的分数（包括带分数）
    private static boolean isStandaloneFraction(String s) {
        s = s.trim();
        // 检查是否是 "数字/数字" 格式
        if (s.matches("\\d+/\\d+")) {
            return true;
        }
        // 检查是否是带分数格式 "数字'数字/数字"
        if (s.matches("\\d+'\\d+/\\d+")) {
            return true;
        }
        return false;
    }

    // 处理带分数，确保正确解析和计算
    private static String processMixedFractions(String expr) {
        // 匹配带分数模式：数字'数字/数字
        Pattern mixedFractionPattern = Pattern.compile("(\\d+)'(\\d+)/(\\d+)");
        Matcher matcher = mixedFractionPattern.matcher(expr);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            int integerPart = Integer.parseInt(matcher.group(1));
            int numerator = Integer.parseInt(matcher.group(2));
            int denominator = Integer.parseInt(matcher.group(3));

            // 将带分数转换为假分数：integerPart + numerator/denominator
            String replacement = "(" + integerPart + " + " + numerator + "/" + denominator + ")";
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    // 表达式计算，确保运算顺序正确
    public static String calculateExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return null;
        }

        try {
            // 统一运算符格式：将显示符号转换为计算符号
            expression = expression.replace('×', '*').replace('÷', '/');

            // 首先处理带分数
            expression = processMixedFractions(expression);

            // 使用表达式树来计算带括号的表达式
            ExpressionTree tree = buildExpressionTree(expression);
            if (tree != null) {
                String result = tree.calculate();
                if (result != null) {
                    return result;
                }
            }

            // 如果表达式树构建失败，回退到改进的简单计算
            return calculateSimpleExpression(expression);
        } catch (Exception e) {
            System.err.println("计算表达式失败: " + expression + ", 错误: " + e.getMessage());
            return null;
        }
    }

    // 简化的表达式计算
    private static String calculateSimpleExpression(String expression) {
        try {
            // 处理带分数
            expression = processMixedFractions(expression);

            // 使用改进的优先级计算
            Fraction result = evaluateWithPriorityImproved(expression);
            return result.toString();
        } catch (Exception e) {
            return null;
        }
    }

    // 带优先级计算
    private static Fraction evaluateWithPriorityImproved(String expr) {
        // 分割token，考虑括号
        List<String> tokens = tokenizeExpression(expr);

        // 先处理括号
        tokens = evaluateParentheses(tokens);

        // 处理乘除
        tokens = evaluateOperators(tokens, Arrays.asList("*", "/"));

        // 处理加减
        tokens = evaluateOperators(tokens, Arrays.asList("+", "-"));

        if (tokens.size() != 1) {
            throw new ArithmeticException("表达式计算错误");
        }

        return parseFraction(tokens.get(0));
    }

    // 分词器，处理数字和运算符
    private static List<String> tokenizeExpression(String expr) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (char c : expr.toCharArray()) {
            if (c == ' ') {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else if (c == '(' || c == ')' || c == '+' || c == '-' || c == '*' || c == '/') {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
                tokens.add(String.valueOf(c));
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0) {
            tokens.add(current.toString());
        }

        return tokens;
    }

    // 处理括号
    private static List<String> evaluateParentheses(List<String> tokens) {
        List<String> result = new ArrayList<>();
        Stack<List<String>> stack = new Stack<>();

        for (String token : tokens) {
            if (token.equals("(")) {
                stack.push(new ArrayList<>());
            } else if (token.equals(")")) {
                if (stack.isEmpty()) {
                    throw new ArithmeticException("括号不匹配");
                }
                List<String> innerTokens = stack.pop();
                Fraction innerResult = evaluateWithPriorityImproved(String.join(" ", innerTokens));
                if (stack.isEmpty()) {
                    result.add(innerResult.toComputableString());
                } else {
                    stack.peek().add(innerResult.toComputableString());
                }
            } else {
                if (stack.isEmpty()) {
                    result.add(token);
                } else {
                    stack.peek().add(token);
                }
            }
        }

        if (!stack.isEmpty()) {
            throw new ArithmeticException("括号不匹配");
        }

        return result;
    }

    // 处理运算符
    private static List<String> evaluateOperators(List<String> tokens, List<String> operators) {
        List<String> result = new ArrayList<>();
        int i = 0;

        while (i < tokens.size()) {
            if (i + 2 < tokens.size() && operators.contains(tokens.get(i + 1))) {
                Fraction left = parseFraction(tokens.get(i));
                String operator = tokens.get(i + 1);
                Fraction right = parseFraction(tokens.get(i + 2));
                Fraction operationResult;

                switch (operator) {
                    case "+":
                        operationResult = left.add(right);
                        break;
                    case "-":
                        if (left.compareTo(right) < 0) {
                            throw new ArithmeticException("减法结果为负");
                        }
                        operationResult = left.subtract(right);
                        break;
                    case "*":
                        operationResult = left.multiply(right);
                        break;
                    case "/":
                        if (!isValidDivision(left, right)) {
                            throw new ArithmeticException("除法不合法");
                        }
                        operationResult = left.divide(right);
                        break;
                    default:
                        throw new ArithmeticException("未知运算符: " + operator);
                }

                result.add(operationResult.toComputableString());
                i += 3;
            } else {
                result.add(tokens.get(i));
                i++;
            }
        }

        return result;
    }

    // 生成数字（自然数、真分数或带分数）
    public static String generateNumber(int range) {
        // 50%概率生成分数或带分数，确保题目中包含足够的分数
        if (random.nextDouble() < 0.5) {
            int denominator = random.nextInt(range - 1) + 2;
            int numerator = random.nextInt(denominator - 1) + 1;

            // 30%概率生成带分数
            if (random.nextDouble() < 0.3 && numerator * 2 > denominator) {
                // 生成带分数，整数部分范围适当控制
                int integerPart = random.nextInt(Math.max(1, range / 3)) + 1;
                return integerPart + "'" + numerator + "/" + denominator;
            } else {
                // 生成真分数
                return numerator + "/" + denominator;
            }
        } else {
            // 50%概率生成自然数
            int num = random.nextInt(range - 1) + 1; // 避免0
            return String.valueOf(num);
        }
    }

    // 解析分数字符串为Fraction对象
    public static Fraction parseFraction(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException("输入字符串为空");
        }

        s = s.trim();

        // 带整数部分的分数（如 "2'3/5"）
        if (s.contains("'")) {
            String[] parts = s.split("'");
            int integerPart = Integer.parseInt(parts[0]);
            String[] fracParts = parts[1].split("/");
            int numerator = Integer.parseInt(fracParts[0]);
            int denominator = Integer.parseInt(fracParts[1]);
            return new Fraction(integerPart * denominator + numerator, denominator);
        }
        // 普通分数
        else if (s.contains("/")) {
            String[] parts = s.split("/");
            int numerator = Integer.parseInt(parts[0]);
            int denominator = Integer.parseInt(parts[1]);
            return new Fraction(numerator, denominator);
        }
        // 整数
        else {
            return new Fraction(Integer.parseInt(s), 1);
        }
    }

    // 移除最外层括号
    private static String removeOuterParentheses(String expr) {
        while (expr.startsWith("(") && expr.endsWith(")") && isBalancedParentheses(expr.substring(1, expr.length() - 1))) {
            expr = expr.substring(1, expr.length() - 1).trim();
        }
        return expr;
    }

    // 检查括号是否平衡
    private static boolean isBalancedParentheses(String expr) {
        int balance = 0;
        for (char c : expr.toCharArray()) {
            if (c == '(') balance++;
            if (c == ')') balance--;
            if (balance < 0) return false;
        }
        return balance == 0;
    }

    // 查找优先级最低的运算符位置
    private static int findLowestPriorityOperator(String expression) {
        int lowestPriority = Integer.MAX_VALUE;
        int lowestIndex = -1;
        int parenthesesLevel = 0;

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (c == '(') {
                parenthesesLevel++;
            } else if (c == ')') {
                parenthesesLevel--;
            } else if (parenthesesLevel == 0 && isOperator(c)) {
                // 检查是否在分数中（如 "1/2" 中的 / 不是运算符）
                if (isPartOfFraction(expression, i)) {
                    continue;
                }

                int priority = OPERATOR_PRIORITY.get(String.valueOf(c));
                if (priority <= lowestPriority) {
                    lowestPriority = priority;
                    lowestIndex = i;
                }
            }
        }

        return lowestIndex;
    }

    // 检查字符是否是分数的一部分
    private static boolean isPartOfFraction(String expression, int index) {
        if (expression.charAt(index) != '/') {
            return false;
        }

        // 检查前后字符是否是数字或 '
        if (index > 0 && index < expression.length() - 1) {
            char before = expression.charAt(index - 1);
            char after = expression.charAt(index + 1);
            return (Character.isDigit(before) || before == '\'') && Character.isDigit(after);
        }

        return false;
    }

    // 判断字符是否为运算符
    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    // 检查除法是否合法且结果为真分数
    public static boolean isProperDivision(Fraction a, Fraction b) {
        try {
            Fraction result = a.divide(b);
            // 检查结果是否为真分数（分子小于分母且为正数）
            return result.getNumerator() >= 0 &&
                    Math.abs(result.getNumerator()) < Math.abs(result.getDenominator());
        } catch (ArithmeticException e) {
            return false;
        }
    }

    // 检查除法是否合法
    public static boolean isValidDivision(Fraction a, Fraction b) {
        try {
            Fraction result = a.divide(b);
            return result.isValid();
        } catch (ArithmeticException e) {
            return false;
        }
    }

    // 统计表达式中的运算符数量
    public static int countOperators(String expr) {
        if (expr == null || expr.trim().isEmpty()) {
            return 0;
        }

        int count = 0;
        for (char c : expr.toCharArray()) {
            if (c == '+' || c == '-' || c == '*' || c == '/') {
                count++;
            }
        }
        return count;
    }

    // 验证表达式树中减法运算结果非负
    public static boolean verifyAllSubtractionsNonNegative(ExpressionTree tree) {
        // 空树直接返回false
        if (tree == null) {
            return false;
        }

        // 计算整个表达式的最终结果
        String resultStr = tree.calculate();

        // 计算失败时视为验证不通过
        if (resultStr == null) {
            return false;
        }

        try {
            // 解析结果为Fraction对象
            Fraction result = parseFraction(resultStr);
            // 验证结果是否非负（分子 >= 0，因为分数已约分，分母恒为正）
            return result.getNumerator() >= 0;
        } catch (Exception e) {
            // 解析失败时视为验证不通过
            return false;
        }
    }

    // 验证表达式树中所有除法运算结果为真分数
    public static boolean verifyAllDivisionsProperFraction(ExpressionTree tree) {
        if (tree == null || tree.isLeaf()) {
            return true;
        }

        // 检查当前节点是否为除法
        if (tree.value.equals("/")) {
            String leftValue = tree.left.calculate();
            String rightValue = tree.right.calculate();

            if (leftValue == null || rightValue == null) {
                return false;
            }

            Fraction leftFrac = parseFraction(leftValue);
            Fraction rightFrac = parseFraction(rightValue);
            Fraction result = leftFrac.divide(rightFrac);
            // 真分数判定
            return result.getNumerator() < result.getDenominator();
        }

        // 递归检查左右子树
        return verifyAllDivisionsProperFraction(tree.left) &&
                verifyAllDivisionsProperFraction(tree.right);
    }
}