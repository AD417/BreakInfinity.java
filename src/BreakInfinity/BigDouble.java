package BreakInfinity;


import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BigDouble implements Comparable<BigDouble> {
    private final double mantissa;
    private final long exponent;

    private BigDouble(double mantissa, long exponent, PrivateConstructorArg unused) {
        this.mantissa = mantissa;
        this.exponent = exponent;
    }

    public BigDouble(double mantissa, long exponent) {
        BigDouble other = normalize(mantissa, exponent);
        this.mantissa = other.mantissa;
        this.exponent = other.exponent;
    }

    public BigDouble(@NotNull BigDouble other) {
        mantissa = other.mantissa;
        exponent = other.exponent;
    }

    public BigDouble(double value) {
        // Java hates direct assignment to this. Fine.
        BigDouble other;
        //SAFETY: Handle Infinity and NaN in a somewhat meaningful way.
        if (Double.isNaN(value)) {
            other = NaN;
        } else if (Double.isInfinite(value)) {
            if (value > 0) other = POSITIVE_INFINITY;
            else other = NEGATIVE_INFINITY;
        } else if (value == 0) {
            other = ZERO;
        } else {
            other = normalize(value, 0);
        }
        this.mantissa = other.mantissa;
        this.exponent = other.exponent;
    }

    public static BigDouble normalize(double mantissa, long exponent) {
        if (mantissa >= 1 && mantissa < 10 || !Double.isFinite(mantissa)) {
            return fromMantissaExponentNoNormalize(mantissa, exponent);
        }
        if (mantissa == 0.0) {
            return ZERO;
        }

        int tempExponent = (int) Math.floor(Math.log10(Math.abs(mantissa)));
        //SAFETY: handle 5e-324, -5e-324 separately
        if (tempExponent == Constants.DOUBLE_EXP_MIN) {
            mantissa = mantissa * 10 / 1e-323;
        } else {
            mantissa = mantissa / PowerOf10.lookup(tempExponent);
        }

        return fromMantissaExponentNoNormalize(mantissa, exponent + tempExponent);
    }


    public static BigDouble fromMantissaExponentNoNormalize(double mantissa, long exponent) {
        return new BigDouble(mantissa, exponent, new PrivateConstructorArg());
    }

    public static BigDouble ZERO = fromMantissaExponentNoNormalize(0, 0);

    public static BigDouble ONE
            = fromMantissaExponentNoNormalize(1, 0);

    public static BigDouble NaN
            = fromMantissaExponentNoNormalize(Double.NaN, Long.MIN_VALUE);

    public static boolean isNaN(BigDouble value) {
        return Double.isNaN(value.mantissa);
    }

    public static BigDouble POSITIVE_INFINITY
            = fromMantissaExponentNoNormalize(Double.POSITIVE_INFINITY, 0);

    public static boolean isPositiveInfinity(BigDouble value) {
        return Double.isInfinite(value.mantissa) && value.mantissa > 0;
    }

    public static BigDouble NEGATIVE_INFINITY
            = fromMantissaExponentNoNormalize(Double.NEGATIVE_INFINITY, 0);

    public static boolean isNegativeInfinity(BigDouble value) {
        return Double.isInfinite(value.mantissa) && value.mantissa < 0;
    }

    public static boolean isInfinite(BigDouble value) {
        return Double.isInfinite(value.mantissa);
    }

    public static BigDouble parseBigDouble(String value) {
        if (value.indexOf('e') != -1) {
            var parts = value.split("e" );
            var mantissa = Double.parseDouble(parts[0]);
            var exponent = Long.parseLong(parts[1]);
            return normalize(mantissa, exponent);
        }

        if (value.equals("NaN" )) {
            return NaN;
        }

        BigDouble result = new BigDouble(Double.parseDouble(value));
        if (isNaN(result)) {
            throw new RuntimeException("Invalid argument: " + value);
        }

        return result;
    }

    // TS stuffs begins here. I stole the constructor and whatnot from BI.CS
    public double getMantissa() {
        return mantissa;
    }

    public long getExponent() {
        return exponent;
    }

    public double m() {
        return mantissa;
    }

    public double e() {
        return exponent;
    }

    public BigDouble abs() {
        return fromMantissaExponentNoNormalize(Math.abs(mantissa), exponent);
    }

    // public static BigDouble abs(BigDouble value) {
    //      return value.abs();
    // }

    /* TODO: The Original JS version uses a ton of typing shenanigans to avoid needing
     * to declare several methods. We don't have that luxury. I'll ignore it for now,
     * But when it becomes a sufficiently big problem I'll address it.
     */

    public BigDouble neg() {
        return fromMantissaExponentNoNormalize(-mantissa, exponent);
    }
    public BigDouble negate() {
        return neg();
    }
    public BigDouble negated() {
        return neg();
    }


    public double signum() {
        return Math.signum(mantissa);
    }
    public double sign() {
        return signum();
    }
    public double sgn() {
        return signum();
    }

    public BigDouble round() {
        if (exponent < -1) {
            return ZERO;
        }
        if (exponent < Constants.MAX_SIGNIFICANT_DIGITS) {
            // Let Math deal with it.
            return new BigDouble(Math.round(toDouble()));
        }
        return this;
    }

    public BigDouble floor() {
        if (isInfinite(this)) return this;

        if (exponent < -1) {
            return Math.signum(mantissa) >= 0 ? ZERO : ONE.neg();
        }
        if (exponent < Constants.MAX_SIGNIFICANT_DIGITS) {
            return new BigDouble(Math.floor(toDouble()));
        }
        return this;
    }

    public BigDouble ceil() {
        if (isInfinite(this)) return this;

        if (exponent < -1) {
            return Math.signum(mantissa) > 0 ? ONE : ZERO;
        }
        if (exponent < Constants.MAX_SIGNIFICANT_DIGITS) {
            return new BigDouble(Math.ceil(toDouble()));
        }
        return this;
    }

    public BigDouble trunc() {
        if (exponent < 0) return ZERO;

        if (exponent < Constants.MAX_SIGNIFICANT_DIGITS) {
            // Math.trunc doesn't exist.
            double value = toDouble();
            if (value > 0) return new BigDouble(Math.floor(value));
            return new BigDouble(Math.ceil(value));
        }
        return this;
    }
    public BigDouble truncate() {
        return trunc();
    }

    public BigDouble add(BigDouble other) {
        if (isInfinite(this)) return this;
        if (isInfinite(other)) return other;

        if (this.mantissa == 0) return other;
        if (other.mantissa == 0) return this;

        BigDouble bigger, smaller;

        if (this.exponent > other.exponent) {
            bigger = this;
            smaller = other;
        } else {
            // Not always true, but in such a case they're close enough that it doesn't matter.
            bigger = other;
            smaller = this;
        }

        if (bigger.exponent - smaller.exponent > Constants.MAX_SIGNIFICANT_DIGITS) {
            return bigger;
        }

        // Have to do this because adding numbers that were once integers but scaled down is imprecise.
        // Example: 299 + 18
        double mantissa = Math.round(
                1e14 * bigger.mantissa +
                1e14 * smaller.mantissa * PowerOf10.lookup(smaller.exponent - bigger.exponent)
        );
        return new BigDouble(mantissa, bigger.exponent - 14);
    }
    public BigDouble plus(BigDouble other) {
        return add(other);
    }

    public BigDouble sub(BigDouble other) {
        return add(other.neg());
    }
    public BigDouble subtract(BigDouble other) {
        return sub(other);
    }
    public BigDouble minus(BigDouble other) {
        return sub(other);
    }

    public BigDouble mul(BigDouble other) {
        return normalize(
                this.mantissa * other.mantissa,
                this.exponent + other.exponent
        );
    }
    public BigDouble multiply(BigDouble other) {
        return mul(other);
    }
    public BigDouble times(BigDouble other) {
        return mul(other);
    }

    public BigDouble div(BigDouble other) {
        return mul(other.recip());
    }
    public BigDouble divide(BigDouble other) {
        return div(other);
    }
    // NOTE: If we do add in all the things, divideBy and dividedBy don't get statics.
    public BigDouble divideBy(BigDouble other) {
        return div(other);
    }
    public BigDouble dividedBy(BigDouble other) {
        return div(other);
    }

    public BigDouble recip() {
        return normalize(1 / mantissa, -exponent);
    }
    public BigDouble reciprocal() {
        return recip();
    }
    public BigDouble reciprocate() {
        return recip();
    }

    @Override
    public int compareTo(@NotNull BigDouble other) {
        if (isNaN(this)) {
            if (isNaN(other)) return 0;
            return -1;
        }
        if (isNaN(other)) return 1;

        if (this.mantissa == 0) {
            if (other.mantissa == 0) return 0;
            if (other.mantissa < 0) return 1;
            return -1;
        }
        if (other.mantissa == 0) {
            if (this.mantissa < 0) return -1;
            return 1;
        }

        if (this.mantissa > 0) {
            if (other.mantissa < 0) return 1;
            if (this.exponent > other.exponent) return 1;
            if (this.exponent < other.exponent) return -1;
            return Double.compare(this.mantissa, other.mantissa);
        }

        if (other.mantissa > 0) return -1;
        if (this.exponent > other.exponent) return -1;
        if (this.exponent < other.exponent) return -1;
        return Double.compare(this.mantissa, other.mantissa);
    }
    public int cmp(BigDouble other) {
        return compareTo(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mantissa, exponent);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (obj.getClass() != BigDouble.class) return false;
        return equals((BigDouble) obj);
    }
    public boolean equals(BigDouble other) {
        return this.exponent == other.exponent && this.mantissa == other.mantissa;
    }
    public boolean eq(BigDouble other) {
        return equals(other);
    }

    public boolean neq(BigDouble other) {
        return !equals(other);
    }
    public boolean notEquals(BigDouble other) {
        return !equals(other);
    }

    // NOTE: maybe I could get away with the extant CompareTo method doing the work for me.
    public boolean lt(BigDouble other) {
        return compareTo(other) < 0;
    }
    public boolean lessThan(BigDouble other) {
        return lt(other);
    }

    public boolean lte(BigDouble other) {
        return compareTo(other) <= 0;
    }
    public boolean lessThanOrEqualTo(BigDouble other) {
        return lte(other);
    }

    public boolean gt(BigDouble other) {
        return compareTo(other) > 0;
    }
    public boolean greaterThan(BigDouble other) {
        return gt(other);
    }

    public boolean gte(BigDouble other) {
        return compareTo(other) >= 0;
    }
    public boolean greaterThanOrEqualTo(BigDouble other) {
        return gte(other);
    }

    public BigDouble max(BigDouble other) {
        return compareTo(other) > 0 ? this : other;
    }

    public BigDouble min(BigDouble other) {
        return compareTo(other) < 0 ? this : other;
    }

    public BigDouble clamp(BigDouble lower, BigDouble higher) {
        return max(lower).min(higher);
    }

    public BigDouble clampMin(BigDouble other) {
        return max(other);
    }

    public BigDouble clampMax(BigDouble other) {
        return min(other);
    }

    // It's operators like this one that make me realize how much of a pain it will be
    // to properly overload everything later. 9 methods each.
    public int cmp_tolerance(BigDouble other, BigDouble tolerance) {
        return eq_tolerance(other, tolerance) ? 0 : cmp(other);
    }
    public int compare_tolerance(BigDouble other, BigDouble tolerance) {
        return cmp_tolerance(other, tolerance);
    }

    public boolean eq_tolerance(BigDouble other, BigDouble tolerance) {
        return sub(other).abs().lte(
                this.abs().max(other.abs()).mul(tolerance)
        );
    }
    public boolean equals_tolerance(BigDouble other, BigDouble tolerance) {
        return eq_tolerance(other, tolerance);
    }

    public boolean neq_tolerance(BigDouble other, BigDouble tolerance) {
        return !eq_tolerance(other, tolerance);
    }
    public boolean notEquals_tolerance(BigDouble other, BigDouble tolerance) {
        return neq_tolerance(other, tolerance);
    }

    public boolean lt_tolerance(BigDouble other, BigDouble tolerance) {
        return !eq_tolerance(other, tolerance) && lt(other);
    }

    public boolean lte_tolerance(BigDouble other, BigDouble tolerance) {
        return eq_tolerance(other, tolerance) || lt(other);
    }

    public boolean gt_tolerance(BigDouble other, BigDouble tolerance) {
        return !eq_tolerance(other, tolerance) && gt(other);
    }

    public boolean gte_tolerance(BigDouble other, BigDouble tolerance) {
        return eq_tolerance(other, tolerance) || gt(other);
    }



    public double toDouble() {
        // Problem: in JS, new Decimal(116).toNumber() returns 115.99999999999999.
        // TODO: How to fix in general case? It's clear that if toNumber() is
        //  VERY close to an integer, we want exactly the integer.
        //  But it's not clear how to specifically write that.
        //  So I'll just settle with 'exponent >= 0 and difference between rounded
        //  and not rounded < 1e-9' as a quick fix.

        // UN-SAFETY: It still eventually fails.
        // Since there's no way to know for sure we started with an integer,
        // all we can do is decide what tradeoff we want between 'yeah I think
        // this used to be an integer' and 'pfft, who needs THAT many decimal
        // places tracked' by changing ROUND_TOLERANCE.
        // https://github.com/Patashu/break_infinity.js/issues/52
        // Currently starts failing at 800002. Workaround is to do .Round()
        // AFTER toNumber() if you are confident you started with an integer.

        // var result = this.m*Math.pow(10, this.e);

        if (isInfinite(this)) {
            return this.mantissa;
        }

        if (exponent > Constants.DOUBLE_EXP_MAX) {
            return mantissa > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }
        if (exponent < Constants.DOUBLE_EXP_MIN) {
            return 0;
        }
        // SAFETY: again, handle 5e-324, -5e-324 separately
        if (exponent == Constants.DOUBLE_EXP_MIN) {
            return mantissa > 0 ? 5e-324 : -5e-324;
        }

        double result = mantissa * PowerOf10.lookup(exponent);
        if (exponent < 0 || Double.isInfinite(result)) {
            return result;
        }
        double resultRounded = Math.round(result);
        if (Math.abs(resultRounded - result) < Constants.ROUND_TOLERANCE) {
            return resultRounded;
        }
        return result;
    }


    private static class PrivateConstructorArg { }

    public static void main(String[] args) {
        BigDouble x = BigDouble.parseBigDouble("7");
        BigDouble y = BigDouble.parseBigDouble("7.0000001");
        BigDouble tol = new BigDouble(0.0001);
        System.out.println(x.toDouble());
        System.out.println(y.toDouble());
        System.out.println(x.equals(y));
        System.out.println(x.eq_tolerance(y, tol));
    }

}