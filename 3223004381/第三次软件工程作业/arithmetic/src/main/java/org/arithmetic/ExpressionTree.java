package org.arithmetic;

import java.util.Random;

/**
 * 表达式树类，用于表示和处理数学表达式的树形结构
 * 支持表达式的生成、计算、标准化和格式转换等功能
 */
public class ExpressionTree {
    private static final Random random = new Random();

    public String value;
    public ExpressionTree left;
    public ExpressionTree right;

    public ExpressionTree(String value) {
        this.value = value;
        this.left = null;
        this.right = null;
    }

    public ExpressionTree(String operator, ExpressionTree left, ExpressionTree right) {
        this.value = operator;
        this.left = left;
        this.right = right;
    }

    // 生成表达式树
    public static ExpressionTree generateTree(int maxOperators, int range) {
        if (maxOperators <= 0) {
            return new ExpressionTree(ExpressionUtils.generateNumber(range));
        }

        // 决定运算符
        char[] operators = {'+', '-', '*', '/'};
        char op = operators[random.nextInt(operators.length)];

        // 分配运算符数量
        int leftOps, rightOps;
        if (maxOperators == 1) {
            leftOps = 0;
            rightOps = 0;
        } else {
            leftOps = random.nextInt(maxOperators);
            rightOps = maxOperators - 1 - leftOps;
        }

        // 生成左右子树
        ExpressionTree leftTree = generateTree(leftOps, range);
        ExpressionTree rightTree = generateTree(rightOps, range);

        // 检查运算合法性
        if (!isOperationValid(op, leftTree, rightTree)) {
            // 如果运算不合法，重新生成
            return generateTree(maxOperators, range);
        }

        ExpressionTree result = new ExpressionTree(String.valueOf(op), leftTree, rightTree);

        // 额外验证：确保减法运算结果非负
        if (op == '-') {
            String leftValue = leftTree.calculate();
            String rightValue = rightTree.calculate();
            if (leftValue != null && rightValue != null) {
                Fraction leftFrac = ExpressionUtils.parseFraction(leftValue);
                Fraction rightFrac = ExpressionUtils.parseFraction(rightValue);
                if (leftFrac.compareTo(rightFrac) < 0) {
                    return generateTree(maxOperators, range); // 重新生成
                }
            }
        }

        return result;
    }

    // 运算合法性检查
    private static boolean isOperationValid(char op, ExpressionTree left, ExpressionTree right) {
        try {
            String leftValue = left.calculate();
            String rightValue = right.calculate();

            if (leftValue == null || rightValue == null) {
                return false;
            }

            Fraction leftFrac = ExpressionUtils.parseFraction(leftValue);
            Fraction rightFrac = ExpressionUtils.parseFraction(rightValue);

            if (op == '-') {
                return leftFrac.compareTo(rightFrac) >= 0;
            } else if (op == '/') {
                return ExpressionUtils.isProperDivision(leftFrac, rightFrac);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 转换为字符串表达式（带括号）
    public String toExpression() {
        if (isLeaf()) {
            return value;
        }

        String leftExpr = left.toExpression();
        String rightExpr = right.toExpression();

        // 根据运算符优先级决定是否添加括号
        if (left.needsParentheses(this)) {
            leftExpr = "(" + leftExpr + ")";
        }
        if (right.needsParentheses(this)) {
            rightExpr = "(" + rightExpr + ")";
        }

        // 将运算符转换为显示格式
        String displayOperator = value;
        if ("*".equals(value)) {
            displayOperator = "×";
        } else if ("/".equals(value)) {
            displayOperator = "÷";
        }

        return leftExpr + " " + displayOperator + " " + rightExpr;
    }

    // 判断是否需要括号
    private boolean needsParentheses(ExpressionTree parent) {
        if (isLeaf()) {
            return false;
        }

        int currentPriority = getOperatorPriority(value);
        int parentPriority = getOperatorPriority(parent.value);

        // 如果当前运算符优先级低于父运算符，需要括号
        if (currentPriority < parentPriority) {
            return true;
        }

        // 对于相同优先级但非交换运算的情况
        if (currentPriority == parentPriority && (!isCommutative() || this == parent.right)) {
            return true;
        }

        return false;
    }

    // 获取运算符优先级
    private int getOperatorPriority(String op) {
        switch (op) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
                return 2;
            default:
                return 0;
        }
    }

    // 判断运算符是否满足交换律
    private boolean isCommutative() {
        return value.equals("+") || value.equals("*");
    }

    // 计算表达式值
    public String calculate() {
        if (isLeaf()) {
            return value;
        }

        try {
            String leftValue = left.calculate();
            String rightValue = right.calculate();

            Fraction leftFrac = ExpressionUtils.parseFraction(leftValue);
            Fraction rightFrac = ExpressionUtils.parseFraction(rightValue);

            Fraction result;
            switch (value) {
                case "+":
                    result = leftFrac.add(rightFrac);
                    break;
                case "-":
                    result = leftFrac.subtract(rightFrac);
                    break;
                case "*":
                    result = leftFrac.multiply(rightFrac);
                    break;
                case "/":
                    result = leftFrac.divide(rightFrac);
                    break;
                default:
                    return null;
            }

            return result.toString();
        } catch (Exception e) {
            return null;
        }
    }

    // 标准化表达式（用于去重）
    public String normalize() {
        if (isLeaf()) {
            return value;
        }

        ExpressionTree normalized = normalizeTree();
        return normalized.toCanonicalForm();
    }

    // 标准化树结构
    private ExpressionTree normalizeTree() {
        if (isLeaf()) {
            return this;
        }

        ExpressionTree normLeft = left.normalizeTree();
        ExpressionTree normRight = right.normalizeTree();

        // 处理交换律
        if (isCommutative()) {
            // 对操作数排序
            if (normLeft.compareTo(normRight) > 0) {
                return new ExpressionTree(value, normRight, normLeft);
            }
        }

        return new ExpressionTree(value, normLeft, normRight);
    }

    // 转换为规范形式
    private String toCanonicalForm() {
        if (isLeaf()) {
            return value;
        }

        String leftCanonical = left.toCanonicalForm();
        String rightCanonical = right.toCanonicalForm();

        if (isCommutative()) {
            // 确保规范顺序
            if (leftCanonical.compareTo(rightCanonical) > 0) {
                return rightCanonical + " " + value + " " + leftCanonical;
            }
        }

        return leftCanonical + " " + value + " " + rightCanonical;
    }

    // 比较两个表达式树
    public int compareTo(ExpressionTree other) {
        if (isLeaf() && other.isLeaf()) {
            return value.compareTo(other.value);
        }

        if (isLeaf() != other.isLeaf()) {
            return Boolean.compare(isLeaf(), other.isLeaf());
        }

        int valueCompare = value.compareTo(other.value);
        if (valueCompare != 0) {
            return valueCompare;
        }

        int leftCompare = left.compareTo(other.left);
        if (leftCompare != 0) {
            return leftCompare;
        }

        return right.compareTo(other.right);
    }

    // 判断是否为叶子节点
    public boolean isLeaf() {
        return left == null && right == null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ExpressionTree other = (ExpressionTree) obj;
        return this.normalize().equals(other.normalize());
    }

    @Override
    public int hashCode() {
        return normalize().hashCode();
    }
}