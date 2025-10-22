package org.arithmetic;

/**
 * 分数类，用于处理自然数、真分数及带分数的四则运算
 * 支持分数的加、减、乘、除、比较等操作，并提供分数的规范化表示
 */
public class Fraction {
    private int numerator;
    private int denominator;

    // 在 Fraction 类中添加改进的解析方法

    /**
     * 从字符串创建分数，支持整数、真分数和带分数
     */
    public static Fraction fromString(String s) {
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
        // 普通分数（如 "3/5"）
        else if (s.contains("/")) {
            String[] parts = s.split("/");
            int numerator = Integer.parseInt(parts[0]);
            int denominator = Integer.parseInt(parts[1]);
            return new Fraction(numerator, denominator);
        }
        // 整数（如 "5"）
        else {
            return new Fraction(Integer.parseInt(s), 1);
        }
    }

    // 修改构造函数，添加对带分数的支持
    public Fraction(int numerator, int denominator) {
        if (denominator == 0) {
            throw new IllegalArgumentException("分母不能为0");
        }

        if (denominator < 0) {
            numerator = -numerator;
            denominator = -denominator;
        }

        int gcd = gcd(Math.abs(numerator), Math.abs(denominator));
        this.numerator = numerator / gcd;
        this.denominator = denominator / gcd;

        // 确保分数是标准形式
        normalize();
    }

    // 标准化分数表示
    private void normalize() {
        if (denominator < 0) {
            numerator = -numerator;
            denominator = -denominator;
        }

        int gcd = gcd(Math.abs(numerator), Math.abs(denominator));
        numerator /= gcd;
        denominator /= gcd;
    }


    private int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    public Fraction add(Fraction other) {
        int newNumerator = this.numerator * other.denominator +
                this.denominator * other.numerator;
        int newDenominator = this.denominator * other.denominator;
        return new Fraction(newNumerator, newDenominator);
    }

    public Fraction subtract(Fraction other) {
        int newNumerator = this.numerator * other.denominator -
                this.denominator * other.numerator;
        int newDenominator = this.denominator * other.denominator;
        return new Fraction(newNumerator, newDenominator);
    }

    public Fraction multiply(Fraction other) {
        int newNumerator = this.numerator * other.numerator;
        int newDenominator = this.denominator * other.denominator;
        return new Fraction(newNumerator, newDenominator);
    }

    public Fraction divide(Fraction other) {
        if (other.numerator == 0) {
            throw new ArithmeticException("除数不能为0");
        }
        return this.multiply(other.reciprocal());
    }

    public Fraction reciprocal() {
        if (numerator == 0) {
            throw new ArithmeticException("不能计算0的倒数");
        }
        return new Fraction(denominator, numerator);
    }

    public int compareTo(Fraction other) {
        long thisVal = (long) this.numerator * other.denominator;
        long otherVal = (long) other.numerator * this.denominator;
        return Long.compare(thisVal, otherVal);
    }

    public boolean isValid() {
        return numerator >= 0 && denominator > 0;
    }

    public boolean isProperFraction() {
        return Math.abs(numerator) < denominator && numerator >= 0;
    }

    public String toComputableString() {
        if (denominator == 1) {
            return String.valueOf(numerator);
        } else {
            return numerator + "/" + denominator;
        }
    }

    @Override
    public String toString() {
        if (numerator == 0) {
            return "0";
        }

        if (denominator == 1) {
            return String.valueOf(numerator);
        }

        // 确保分子分母都是正数
        int num = Math.abs(numerator);
        int den = Math.abs(denominator);

        if (num >= den) {
            int integerPart = numerator / denominator;
            int newNumerator = num % den;
            if (newNumerator == 0) {
                return String.valueOf(integerPart);
            }
            return integerPart + "'" + newNumerator + "/" + den;
        }

        return (numerator < 0 ? "-" : "") + num + "/" + den;
    }

    public int getNumerator() {
        return numerator;
    }

    public int getDenominator() {
        return denominator;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Fraction other = (Fraction) obj;
        return numerator == other.numerator && denominator == other.denominator;
    }

    @Override
    public int hashCode() {
        return 31 * numerator + denominator;
    }
}