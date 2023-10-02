package io.github.ad417.BreakInfinity;

import org.jetbrains.annotations.NotNull;
import java.text.DecimalFormat;
import java.util.Objects;


@SuppressWarnings("unused" )
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

    public BigDouble(String value) {
        this(BigDouble.parseBigDouble(value));
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

    public static final BigDouble ZERO = fromMantissaExponentNoNormalize(0, 0);

    public static final BigDouble ONE
            = fromMantissaExponentNoNormalize(1, 0);

    public static final BigDouble NaN
            = fromMantissaExponentNoNormalize(Double.NaN, Long.MIN_VALUE);

    public static boolean isNaN(BigDouble value) {
        return Double.isNaN(value.mantissa);
    }

    public static final BigDouble POSITIVE_INFINITY
            = fromMantissaExponentNoNormalize(Double.POSITIVE_INFINITY, 0);

    public static boolean isPositiveInfinity(BigDouble value) {
        return Double.isInfinite(value.mantissa) && value.mantissa > 0;
    }

    public static final BigDouble NEGATIVE_INFINITY
            = fromMantissaExponentNoNormalize(Double.NEGATIVE_INFINITY, 0);

    public static boolean isNegativeInfinity(BigDouble value) {
        return Double.isInfinite(value.mantissa) && value.mantissa < 0;
    }

    public static boolean isInfinite(BigDouble value) {
        return Double.isInfinite(value.mantissa);
    }

    public static boolean isFinite(BigDouble value) {
        return !isInfinite(value);
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

    public static BigDouble abs(BigDouble value) {
         return value.abs();
    }
    public static BigDouble abs(double value) {
        return new BigDouble(value).abs();
    }
    public static BigDouble abs(String value) {
        return BigDouble.parseBigDouble(value).abs();
    }

    /* TODO: The Original JS version uses a ton of typing shenanigans to avoid needing
     * to declare several methods. We don't have that luxury. I'll ignore it for now,
     * But when it becomes a sufficiently big problem I'll address it.
     */

    public BigDouble neg() {
        return fromMantissaExponentNoNormalize(-mantissa, exponent);
    }
    public static BigDouble neg(BigDouble value) {
        return value.neg();
    }
    public static BigDouble neg(double value) {
        return new BigDouble(value).neg();
    }
    public static BigDouble neg(String value) {
        return BigDouble.parseBigDouble(value).neg();
    }
    public BigDouble negate() {
        return neg();
    }
    public static BigDouble negate(BigDouble value) {
        return value.neg();
    }
    public static BigDouble negate(double value) {
        return new BigDouble(value).neg();
    }
    public static BigDouble negate(String value) {
        return BigDouble.parseBigDouble(value).neg();
    }
    public BigDouble negated() {
        return neg();
    }
    public static BigDouble negated(BigDouble value) {
        return value.neg();
    }
    public static BigDouble negated(double value) {
        return new BigDouble(value).neg();
    }
    public static BigDouble negated(String value) {
        return BigDouble.parseBigDouble(value).neg();
    }


    public double signum() {
        return Math.signum(mantissa);
    }
    public static double signum(BigDouble value) {
        return value.signum();
    }
    public static double signum(double value) {
        return new BigDouble(value).signum();
    }
    public static double signum(String value) {
        return BigDouble.parseBigDouble(value).signum();
    }
    public double sign() {
        return signum();
    }
    public static double sign(BigDouble value) {
        return value.signum();
    }
    public static double sign(double value) {
        return new BigDouble(value).signum();
    }
    public static double sign(String value) {
        return BigDouble.parseBigDouble(value).signum();
    }
    public double sgn() {
        return signum();
    }
    public static double sgn(BigDouble value) {
        return value.signum();
    }
    public static double sgn(double value) {
        return new BigDouble(value).signum();
    }
    public static double sgn(String value) {
        return BigDouble.parseBigDouble(value).signum();
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
    public static BigDouble round(BigDouble value) {
        return value.round();
    }
    public static BigDouble round(double value) {
        return new BigDouble(value).round();
    }
    public static BigDouble round(String value) {
        return BigDouble.parseBigDouble(value).round();
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
    public static BigDouble floor(BigDouble value) {
        return value.floor();
    }
    public static BigDouble floor(double value) {
        return new BigDouble(value).floor();
    }
    public static BigDouble floor(String value) {
        return BigDouble.parseBigDouble(value).floor();
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
    public static BigDouble ceil(BigDouble value) {
        return value.ceil();
    }
    public static BigDouble ceil(double value) {
        return new BigDouble(value).ceil();
    }
    public static BigDouble ceil(String value) {
        return BigDouble.parseBigDouble(value).ceil();
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
    public static BigDouble trunc(BigDouble value) {
        return value.trunc();
    }
    public static BigDouble trunc(double value) {
        return new BigDouble(value).trunc();
    }
    public static BigDouble trunc(String value) {
        return BigDouble.parseBigDouble(value).trunc();
    }
    public BigDouble truncate() {
        return trunc();
    }
    public static BigDouble truncate(BigDouble value) {
        return value.trunc();
    }
    public static BigDouble truncate(double value) {
        return new BigDouble(value).trunc();
    }
    public static BigDouble truncate(String value) {
        return BigDouble.parseBigDouble(value).trunc();
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
    public BigDouble add(double other) {
        return this.add(new BigDouble(other));
    }
    public BigDouble add(String other) {
        return this.add(BigDouble.parseBigDouble(other));
    }
    public BigDouble plus(BigDouble other) {
        return add(other);
    }
    public BigDouble plus(double other) {
        return this.plus(new BigDouble(other));
    }
    public BigDouble plus(String other) {
        return this.plus(BigDouble.parseBigDouble(other));
    }

    public BigDouble sub(BigDouble other) {
        return add(other.neg());
    }
    public BigDouble sub(double other) {
        return this.sub(new BigDouble(other));
    }
    public BigDouble sub(String other) {
        return this.sub(BigDouble.parseBigDouble(other));
    }
    public BigDouble subtract(BigDouble other) {
        return sub(other);
    }
    public BigDouble subtract(double other) {
        return this.subtract(new BigDouble(other));
    }
    public BigDouble subtract(String other) {
        return this.subtract(BigDouble.parseBigDouble(other));
    }
    public BigDouble minus(BigDouble other) {
        return sub(other);
    }
    public BigDouble minus(double other) {
        return this.minus(new BigDouble(other));
    }
    public BigDouble minus(String other) {
        return this.minus(BigDouble.parseBigDouble(other));
    }

    public BigDouble mul(BigDouble other) {
        return normalize(
                this.mantissa * other.mantissa,
                this.exponent + other.exponent
        );
    }
    public BigDouble mul(double other) {
        return this.mul(new BigDouble(other));
    }
    public BigDouble mul(String other) {
        return this.mul(BigDouble.parseBigDouble(other));
    }
    public BigDouble multiply(BigDouble other) {
        return mul(other);
    }
    public BigDouble multiply(double other) {
        return this.multiply(new BigDouble(other));
    }
    public BigDouble multiply(String other) {
        return this.multiply(BigDouble.parseBigDouble(other));
    }
    public BigDouble times(BigDouble other) {
        return mul(other);
    }
    public BigDouble times(double other) {
        return this.times(new BigDouble(other));
    }
    public BigDouble times(String other) {
        return this.times(BigDouble.parseBigDouble(other));
    }

    public BigDouble div(BigDouble other) {
        return mul(other.recip());
    }
    public BigDouble div(double other) {
        return this.div(new BigDouble(other));
    }
    public BigDouble div(String other) {
        return this.div(BigDouble.parseBigDouble(other));
    }
    public BigDouble divide(BigDouble other) {
        return div(other);
    }
    public BigDouble divide(double other) {
        return this.divide(new BigDouble(other));
    }
    public BigDouble divide(String other) {
        return this.divide(BigDouble.parseBigDouble(other));
    }
    // NOTE: If we do add in all the things, divideBy and dividedBy don't get statics.
    public BigDouble divideBy(BigDouble other) {
        return div(other);
    }
    public BigDouble divideBy(double other) {
        return this.divideBy(new BigDouble(other));
    }
    public BigDouble divideBy(String other) {
        return this.divideBy(BigDouble.parseBigDouble(other));
    }
    public BigDouble dividedBy(BigDouble other) {
        return div(other);
    }
    public BigDouble dividedBy(double other) {
        return this.dividedBy(new BigDouble(other));
    }
    public BigDouble dividedBy(String other) {
        return this.dividedBy(BigDouble.parseBigDouble(other));
    }

    public BigDouble recip() {
        return normalize(1 / mantissa, -exponent);
    }
    public static BigDouble recip(double value) {
        return new BigDouble(value).recip();
    }
    public static BigDouble recip(String value) {
        return BigDouble.parseBigDouble(value).recip();
    }
    public BigDouble reciprocal() {
        return recip();
    }
    public static BigDouble reciprocal(double value) {
        return new BigDouble(value).reciprocal();
    }
    public static BigDouble reciprocal(String value) {
        return BigDouble.parseBigDouble(value).reciprocal();
    }
    public BigDouble reciprocate() {
        return recip();
    }
    public static BigDouble reciprocate(double value) {
        return new BigDouble(value).reciprocate();
    }
    public static BigDouble reciprocate(String value) {
        return BigDouble.parseBigDouble(value).reciprocate();
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
    public boolean equals(double other) {
        return this.equals(new BigDouble(other));
    }
    public boolean equals(String other) {
        return this.equals(BigDouble.parseBigDouble(other));
    }
    public boolean eq(BigDouble other) {
        return equals(other);
    }
    public boolean eq(double other) {
        return this.eq(new BigDouble(other));
    }
    public boolean eq(String other) {
        return this.eq(BigDouble.parseBigDouble(other));
    }

    public boolean neq(BigDouble other) {
        return !equals(other);
    }
    public boolean neq(double other) {
        return this.neq(new BigDouble(other));
    }
    public boolean neq(String other) {
        return this.neq(BigDouble.parseBigDouble(other));
    }
    public boolean notEquals(BigDouble other) {
        return !equals(other);
    }
    public boolean notEquals(double other) {
        return this.notEquals(new BigDouble(other));
    }
    public boolean notEquals(String other) {
        return this.notEquals(BigDouble.parseBigDouble(other));
    }

    // NOTE: maybe I could get away with the extant CompareTo method doing the work for me.
    public boolean lt(BigDouble other) {
        return compareTo(other) < 0;
    }
    public boolean lt(double other) {
        return this.lt(new BigDouble(other));
    }
    public boolean lt(String other) {
        return this.lt(BigDouble.parseBigDouble(other));
    }
    public boolean lessThan(BigDouble other) {
        return lt(other);
    }
    public boolean lessThan(double other) {
        return this.lessThan(new BigDouble(other));
    }
    public boolean lessThan(String other) {
        return this.lessThan(BigDouble.parseBigDouble(other));
    }

    public boolean lte(BigDouble other) {
        return compareTo(other) <= 0;
    }
    public boolean lte(double other) {
        return this.lte(new BigDouble(other));
    }
    public boolean lte(String other) {
        return this.lte(BigDouble.parseBigDouble(other));
    }
    public boolean lessThanOrEqualTo(BigDouble other) {
        return lte(other);
    }
    public boolean lessThanOrEqualTo(double other) {
        return this.lessThanOrEqualTo(new BigDouble(other));
    }
    public boolean lessThanOrEqualTo(String other) {
        return this.lessThanOrEqualTo(BigDouble.parseBigDouble(other));
    }

    public boolean gt(BigDouble other) {
        return compareTo(other) > 0;
    }
    public boolean gt(double other) {
        return this.gt(new BigDouble(other));
    }
    public boolean gt(String other) {
        return this.gt(BigDouble.parseBigDouble(other));
    }
    public boolean greaterThan(BigDouble other) {
        return gt(other);
    }
    public boolean greaterThan(double other) {
        return this.greaterThan(new BigDouble(other));
    }
    public boolean greaterThan(String other) {
        return this.greaterThan(BigDouble.parseBigDouble(other));
    }

    public boolean gte(BigDouble other) {
        return compareTo(other) >= 0;
    }
    public boolean gte(double other) {
        return this.gte(new BigDouble(other));
    }
    public boolean gte(String other) {
        return this.gte(BigDouble.parseBigDouble(other));
    }
    public boolean greaterThanOrEqualTo(BigDouble other) {
        return gte(other);
    }
    public boolean greaterThanOrEqualTo(double other) {
        return this.greaterThanOrEqualTo(new BigDouble(other));
    }
    public boolean greaterThanOrEqualTo(String other) {
        return this.greaterThanOrEqualTo(BigDouble.parseBigDouble(other));
    }

    public BigDouble max(BigDouble other) {
        return compareTo(other) > 0 ? this : other;
    }
    public BigDouble max(double other) {
        return this.max(new BigDouble(other));
    }
    public BigDouble max(String other) {
        return this.max(BigDouble.parseBigDouble(other));
    }

    public BigDouble min(BigDouble other) {
        return compareTo(other) < 0 ? this : other;
    }
    public BigDouble min(double other) {
        return this.min(new BigDouble(other));
    }
    public BigDouble min(String other) {
        return this.min(BigDouble.parseBigDouble(other));
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

    public double log10() {
        return exponent + Math.log10(mantissa);
    }
    public static double log10(BigDouble value) {
        return value.log10();
    }
    public static double log10(double value) {
        return new BigDouble(value).log10();
    }
    public static double log10(String value) {
        return BigDouble.parseBigDouble(value).log10();
    }

    public double absLog10() {
        return exponent + Math.log10(Math.abs(mantissa));
    }
    public static double absLog10(BigDouble value) {
        return value.absLog10();
    }
    public static double absLog10(double value) {
        return new BigDouble(value).absLog10();
    }
    public static double absLog10(String value) {
        return BigDouble.parseBigDouble(value).absLog10();
    }

    public double pLog10() {
        return mantissa <= 0 || exponent < 0 ? 0 : log10();
    }
    public static double pLog10(BigDouble value) {
        return value.pLog10();
    }
    public static double pLog10(double value) {
        return new BigDouble(value).pLog10();
    }
    public static double pLog10(String value) {
        return BigDouble.parseBigDouble(value).pLog10();
    }

    public double log() {
        return 2.302585092994046 * log10();
    }
    public double logarithm() {
        return log();
    }
    public static double logarithm(BigDouble value) {
        return value.logarithm();
    }

    public double log(double base) {
        // UN-SAFETY: Most incremental game cases are log(number := 1 or greater, base := 2 or greater).
        // We assume this to be true and thus only need to return a number, not a Decimal,
        // and don't do any other kind of error checking.

        // Also, Math.LN10 = 2.302585092994046
        return 2.302585092994046 / Math.log(base) * log10();
    }
    public double logarithm(double base) {
        return log(base);
    }

    public double log2() {
        return 3.321928094887362 * log10();
    }

    public double ln() {
        return log();
    }
    public static double ln(BigDouble value) {
        return value.ln();
    }
    public static double ln(double value) {
        return new BigDouble(value).ln();
    }
    public static double ln(String value) {
        return BigDouble.parseBigDouble(value).ln();
    }

    public static BigDouble pow10(long value) {
        return fromMantissaExponentNoNormalize(1, value);
    }
    public static BigDouble pow10(double value) {
        long valueAsLong = (long) value;
        // UN-SAFETY: if value is larger than a long, then the program will break anyway.
        double residual = value - valueAsLong;
        if (residual < Constants.ROUND_TOLERANCE) {
            return fromMantissaExponentNoNormalize(1, valueAsLong);
        }
        return normalize(Math.pow(10, residual), valueAsLong);
    }
    public static BigDouble pow10(BigDouble value) {
        return pow10(value.toDouble());
    }

    public BigDouble pow(BigDouble power) {
        // UN-SAFETY: if power > Double.MAX_VALUE,
        // anything raised to it is either 0 or infinite.

        return pow(power.toDouble());
    }
    public BigDouble pow(double power) {
        boolean powerIsInteger = Math.abs(power % 1) < Double.MIN_VALUE;
        if (power < 0 && !powerIsInteger) return NaN;

        boolean is10 = exponent == 1 && mantissa - 1 < Double.MIN_VALUE;
        return is10 ? pow10(power) : powInternal(power);
    }
    private BigDouble powInternal(double other) {
        //UN-SAFETY: Accuracy not guaranteed beyond ~9~11 decimal places.

        //TODO: Fast track seems about neutral for performance. It might become faster if an integer pow is implemented, or it might not be worth doing (see https://github.com/Patashu/break_infinity.js/issues/4 )

        //Fast track: If (this.exponent*value) is an integer and mantissa^value fits in a Number, we can do a very fast method.
        var temp = exponent * other;
        double newMantissa;
        if (Math.abs(temp % 1) < Double.MIN_VALUE && Double.isFinite(temp) && Math.abs(temp) < Constants.EXP_LIMIT)
        {
            newMantissa = Math.pow(mantissa, other);
            if (Double.isFinite(newMantissa))
            {
                return normalize(newMantissa, (long) temp);
            }
        }

        //Same speed and usually more accurate. (An arbitrary-precision version of this calculation is used in break_break_infinity.js, sacrificing performance for utter accuracy.)

        var newExponent = (long) temp;
        var residue = temp - newExponent;
        newMantissa = Math.pow(10, other * Math.log10(mantissa) + residue);
        if (Double.isFinite(newMantissa))
        {
            return normalize(newMantissa, newExponent);
        }

        //UN-SAFETY: This should return NaN when mantissa is negative and value is noninteger.
        var result = pow10(other * absLog10()); //this is 2x faster and gives same values AFAIK
        if (signum() == -1 && other % 2 == 1)
        {
            return result.neg();
        }

        return result;
    }

    public BigDouble exp() {
        double x = toDouble();
        if (-706 < x && x < 709) return new BigDouble(Math.exp(x));
        return pow(Math.E);
    }
    public static BigDouble exp(BigDouble value) {
        return value.exp();
    }
    public static BigDouble exp(double value) {
        return new BigDouble(value).exp();
    }
    public static BigDouble exp(String value) {
        return BigDouble.parseBigDouble(value).exp();
    }

    public BigDouble sqr() {
        return normalize(mantissa * mantissa, exponent * 2);
    }
    public static BigDouble sqr(BigDouble value) {
        return value.sqr();
    }
    public static BigDouble sqr(double value) {
        return new BigDouble(value).sqr();
    }
    public static BigDouble sqr(String value) {
        return BigDouble.parseBigDouble(value).sqr();
    }

    public BigDouble sqrt() {
        if (mantissa < 0) return NaN;
        if (exponent % 2 != 0) {
            // Mod of a negative number is negative, so != could be +1 or -1.
            return normalize(
                    Math.sqrt(mantissa) * 3.16227766016838,
                    (exponent - 1) / 2
            );
        }
        return normalize(Math.sqrt(mantissa), exponent / 2);
    }
    public static BigDouble sqrt(BigDouble value) {
        return value.sqrt();
    }
    public static BigDouble sqrt(double value) {
        return new BigDouble(Math.sqrt(value));
    }
    public static BigDouble sqrt(String value) {
        return BigDouble.parseBigDouble(value).sqrt();
    }

    public BigDouble cube() {
        return normalize(
                mantissa * mantissa * mantissa,
                exponent * 3
        );
    }
    public static BigDouble cube(BigDouble value) {
        return value.cube();
    }
    public static BigDouble cube(double value) {
        return new BigDouble(value).cube();
    }
    public static BigDouble cube(String value) {
        return BigDouble.parseBigDouble(value).cube();
    }

    public BigDouble cbrt() {
        int sign = mantissa > 0 ? 1 : -1;
        double newMantissa = Math.cbrt(mantissa);

        return switch ((int) (exponent % 3)) {
            case 1, -2 -> normalize(
                    newMantissa * 2.154434690031883,
                    (long) Math.floor(exponent / 3.0)
            );
            case 2, -1 -> normalize(
                    newMantissa * 4.641588833612778,
                    (long) Math.floor(exponent / 3.0)
            );
            default -> // 0
                    normalize(newMantissa, exponent / 3);
        };
    }
    public static BigDouble cbrt(BigDouble value) {
        return value.cbrt();
    }
    public static BigDouble cbrt(double value) {
        return new BigDouble(Math.cbrt(value));
    }
    public static BigDouble cbrt(String value) {
        return BigDouble.parseBigDouble(value).cbrt();
    }

    /**
     * If you're willing to spend 'resourcesAvailable' and want to buy something
     * with exponentially increasing cost each purchase (start at priceStart,
     * multiply by priceRatio, already own currentOwned), how much of it can you buy?
     * Adapted from Trimps source code.
     */
    public static BigDouble affordGeometricSeries(
            // Thanks, I hate that this has 4
            BigDouble resourcesAvailable,
            BigDouble priceStart,
            BigDouble priceRatio,
            long currentOwned
    ) {
        BigDouble actualStart = priceStart.mul(priceRatio.pow(currentOwned));

        return new BigDouble(Math.floor(
                resourcesAvailable.div(actualStart).mul(priceRatio.sub(ONE)).add(ONE).log10()
                / priceRatio.log10()
        ));
    }
    public static BigDouble affordGeometricSeries(
            BigDouble resourcesAvailable,
            double priceStart,
            double priceRatio,
            long currentOwned
    ) {
        return affordGeometricSeries(
                resourcesAvailable,
                new BigDouble(priceStart),
                new BigDouble(priceRatio),
                currentOwned
        );
    }

    /**
     * How much resource would it cost to buy (numItems) items if you already have currentOwned,
     * the initial price is priceStart and it multiplies by priceRatio each purchase?
     */
    public static BigDouble sumGeometricSeries(
            int numItems,
            BigDouble priceStart,
            BigDouble priceRatio,
            int currentOwned
    ) {
        return priceStart
                .mul(priceRatio.pow(currentOwned))
                .mul(ONE.sub(priceRatio.pow(numItems)))
                .div(ONE.sub(priceRatio));
    }

    /**
     * If you're willing to spend 'resourcesAvailable' and want to buy something with additively
     * increasing cost each purchase (start at priceStart, add by priceAdd, already own currentOwned),
     * how much of it can you buy?
     */
    public static BigDouble affordArithmeticSeries(
            BigDouble resourcesAvailable,
            BigDouble priceStart,
            BigDouble priceAdd,
            int currentOwned
    ) {
        BigDouble actualStart = priceStart.add(priceAdd.mul(currentOwned));
        BigDouble b = actualStart.sub(priceAdd.div(2));
        BigDouble b2 = b.pow(2);

        return b.neg()
                .add(b2.add(priceAdd.mul(resourcesAvailable).mul(2)).sqrt())
                .div(priceAdd)
                .floor();
    }

    /**
     * How much resource would it cost to buy (numItems) items if you already have currentOwned,
     * the initial price is priceStart and it adds priceAdd each purchase?
     * Adapted from <a href="http://www.mathwords.com/a/arithmetic_series.htm">...</a>
     */
    public static BigDouble sumArithmeticSeries(
            int numItems,
            BigDouble priceStart,
            BigDouble priceAdd,
            int currentOwned
    ) {
        BigDouble actualStart = priceStart.add(priceAdd.mul(currentOwned));

        // (n/2)*(2*a+(n-1)*d)
        // numItems
        return new BigDouble(numItems)
                .div(2)
                .mul(actualStart.mul(2).plus(new BigDouble(numItems).sub(ONE).mul(priceAdd)));
    }

    /**
     * When comparing two purchases that cost (resource) and increase your resource/sec by (deltaRpS),
     * the lowest efficiency score is the better one to purchase.
     * From Frozen Cookies:
     * <a href="http://cookieclicker.wikia.com/wiki/Frozen_Cookies_(JavaScript_Add-on)#Efficiency.3F_What.27s_that.3F">...</a>
     */
    public static BigDouble efficiencyOfPurchase(
            BigDouble cost, BigDouble currentRpS, BigDouble deltaRpS
    ) {
        return cost.div(currentRpS).add(cost.div(deltaRpS));
    }

    public static BigDouble randomDecimalForTesting(long absMaxExponent) {
        // NOTE: This doesn't follow any kind of sane random distribution, so use this for testing purposes only.
        // 5% of the time, have a mantissa of 0
        if (Math.random() * 20 < 1) {
            return fromMantissaExponentNoNormalize(0, 0);
        }
        double mantissa = Math.random() * 10;
        // 10% of the time, have a simple mantissa
        if (Math.random() * 10 < 1) {
            mantissa = Math.round(mantissa);
        }
        mantissa *= Math.signum(Math.random() * 2 - 1);
        long exponent = (long) Math.floor(Math.random() * absMaxExponent * 2) - absMaxExponent;
        return normalize(mantissa, exponent);

        /*
          Examples:
          randomly test pow:
          var a = Decimal.randomDecimalForTesting(1000);
          var pow = Math.random()*20-10;
          if (Math.random()*2 < 1) { pow = Math.round(pow); }
          var result = Decimal.pow(a, pow);
          ["(" + a.toString() + ")^" + pow.toString(), result.toString()]
          randomly test add:
          var a = Decimal.randomDecimalForTesting(1000);
          var b = Decimal.randomDecimalForTesting(17);
          var c = a.mul(b);
          var result = a.add(c);
          [a.toString() + "+" + c.toString(), result.toString()]
        */
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

    public double mantissaWithDecimalPlaces(int places) {
        if (isInfinite(this)) {
            return mantissa;
        }

        if (mantissa == 0) {
            return 0;
        }

        // Create a DecimalFormat instance with the desired pattern
        DecimalFormat df = new DecimalFormat("#." + "0".repeat(places));

        // Use the format() method to round and format the double
        String formattedValue = df.format(mantissa);
        return Double.parseDouble(formattedValue);
    }

    @Override
    public String toString() {
        if (isInfinite(this)) return Double.toString(mantissa);
        if (exponent <= -Constants.EXP_LIMIT) return "0";

        if (exponent < 21 && exponent > -7) {
            return Double.toString(toDouble());
        }
        return mantissa + "e" + (exponent >= 0 ? "+" : "") + exponent;
    }

    public String toExponential(int places) {
        if (isInfinite(this)) return Double.toString(mantissa);

        if (mantissa == 0 || exponent < -Constants.EXP_LIMIT) {
            return "0" + RepeatZeroes.trailZeroes(places) + "e+0";
        }

        // One case: we have to do it all ourselves!
        // Sorry, no toExponential in Double.

        int len = places + 1;
        int numDigits = (int) Math.max(1, Math.ceil(Math.log10(Math.abs(mantissa))));
        double rounded = Math.round(mantissa * Math.pow(10, len - numDigits)) * Math.pow(10, numDigits - len);

        // Create a DecimalFormat instance with the desired pattern
        DecimalFormat df = new DecimalFormat("#." + "0".repeat((Math.max(len - numDigits, 0))));


        return df.format(rounded) + "e" + (exponent >= 0 ? "+" : "") + exponent;
    }

    private String toFixed(int places)
    {
        if (places < 0) {
            places = Constants.MAX_SIGNIFICANT_DIGITS;
        }
        if (exponent <= -Constants.EXP_LIMIT || mantissa == 0) {
            return "0" + (
                    places > 0 ?
                    RepeatZeroes.padRight(".", places) :
                    ""
            );
        }

        // two cases:
        // 1) exponent is 17 or greater: just print out mantissa with the appropriate number of zeroes after it
        // 2) exponent is 16 or less: use basic toFixed

        if (exponent >= Constants.MAX_SIGNIFICANT_DIGITS)
        {
            // TODO: StringBuilder-optimizable, and frankly just bad in general.
            String out =  Double.toString(mantissa)
                    .replace(".", "");
            out = RepeatZeroes.padRight(out, (int)exponent + 1)
                    + (places > 0 ? RepeatZeroes.padRight(".", places+1) : "");
        }

        long multiplier = (long) Math.pow(10, places);
        double roundedValue = Math.round(this.toDouble() * multiplier) / (double) multiplier;

        // Not malformed. I think.
        return String.format("%." + places + "f", roundedValue);
    }

    public String toPrecision(int places) {
        if (exponent <= -7) {
            return toExponential(places - 1);
        }
        if (places > exponent) {
            return this.toFixed(places - (int)exponent - 1);
        }
        return this.toExponential(places - 1);
    }

    public BigDouble sinh() {
        return this.exp().sub(this.neg().exp()).div(2);
    }
    public static BigDouble sinh(BigDouble value) {
        return value.sinh();
    }
    public static BigDouble sinh(double value) {
        return new BigDouble(value).sinh();
    }
    public static BigDouble sinh(String value) {
        return BigDouble.parseBigDouble(value).sinh();
    }

    public BigDouble cosh() {
        return this.exp().add(this.neg().exp()).div(2);
    }
    public static BigDouble cosh(BigDouble value) {
        return value.cosh();
    }
    public static BigDouble cosh(double value) {
        return new BigDouble(value).cosh();
    }
    public static BigDouble cosh(String value) {
        return BigDouble.parseBigDouble(value).cosh();
    }

    public BigDouble tanh() {
        return sinh().div(cosh());
    }
    public static BigDouble tanh(BigDouble value) {
        return value.tanh();
    }
    public static BigDouble tanh(double value) {
        return new BigDouble(value).tanh();
    }
    public static BigDouble tanh(String value) {
        return BigDouble.parseBigDouble(value).tanh();
    }

    public double asinh() {
        return ln(this.add(sqr().add(ONE).sqrt()));
    }
    public static double asinh(BigDouble value) {
        return value.asinh();
    }
    public static double asinh(double value) {
        return new BigDouble(value).asinh();
    }
    public static double asinh(String value) {
        return BigDouble.parseBigDouble(value).asinh();
    }

    public double acosh() {
        return add(ONE).div(ONE.sub(this)).ln() / 2;
    }
    public static double acosh(BigDouble value) {
        return value.acosh();
    }
    public static double acosh(double value) {
        return new BigDouble(value).acosh();
    }
    public static double acosh(String value) {
        return BigDouble.parseBigDouble(value).acosh();
    }

    public double atanh() {
        if (this.abs().gte(1)) return Double.NaN;
        return ln(this.add(1).div(ONE.sub(this))) / 2;
    }
    public static double atanh(BigDouble value) {
        return value.atanh();
    }
    public static double atanh(double value) {
        return new BigDouble(value).atanh();
    }
    public static double atanh(String value) {
        return BigDouble.parseBigDouble(value).atanh();
    }

    /**
     * Joke function from Realm Grinder
     */
    public BigDouble ascensionPenalty(int ascensions) {
        if (ascensions == 0) {
            return this;
        }
        return this.pow(Math.pow(10, -ascensions));
    }

    /**
     * Joke function from Cookie Clicker. It's 'egg'
     */
    public BigDouble egg() {
        return this.add(9);
    }

    private static class PrivateConstructorArg { }

    public static void main(String[] args) {
        BigDouble x = new BigDouble(3).add(2);
        for (int i = 0; i < 10000; i++) {
            BigDouble y = BigDouble.randomDecimalForTesting(Long.MAX_VALUE >> 1);
            String yStr = y.toPrecision(5);
            System.out.println();
            System.out.println(y);
            System.out.println(yStr);
            x = x.add(yStr);
            System.out.println(x);
        }
    }

}