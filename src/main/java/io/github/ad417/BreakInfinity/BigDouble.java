package io.github.ad417.BreakInfinity;

import org.jetbrains.annotations.NotNull;
import java.text.DecimalFormat;
import java.util.Objects;

/**
 * A BigDouble's value is simply mantissa * 10 ^ exponent.
 */
@SuppressWarnings("unused")
public final class BigDouble extends Number implements Comparable<BigDouble> {
    private final double mantissa;
    private final long exponent;

    private BigDouble(double mantissa, long exponent, PrivateConstructorArg unused) {
        this.mantissa = mantissa;
        this.exponent = exponent;
    }

    /**
     * Create a BigDouble by specifying the mantissa and exponent seperately.
     * @param mantissa A floating-point number between [1, 10) OR exactly 0.
     *                 Other values will be normalized to within this range.
     * @param exponent A long number of any value. Represents the exponent.
     */
    public BigDouble(double mantissa, long exponent) {
        BigDouble other = normalize(mantissa, exponent);
        this.mantissa = other.mantissa;
        this.exponent = other.exponent;
    }

    /**
     * Create a BigDouble from a number. Primitives may be passed.
     * @param value a number to convert to a BigDouble.
     */
    public BigDouble(Number value) {
        // Java hates direct assignment to "this". Fine.
        BigDouble other;
        if (value instanceof BigDouble) {
            other = (BigDouble) value;
        } else {
            double doubleValue = value.doubleValue();
            if (Double.isNaN(doubleValue)) {
                //SAFETY: Handle Infinity and NaN in a somewhat meaningful way.
                other = NaN;
            } else if (Double.isInfinite(doubleValue)) {
                if (doubleValue > 0) other = POSITIVE_INFINITY;
                else other = NEGATIVE_INFINITY;
            } else if (doubleValue == 0) {
                other = ZERO;
            } else {
                other = normalize(doubleValue, 0);
            }
        }
        this.mantissa = other.mantissa;
        this.exponent = other.exponent;
    }

    /**
     * Create a BigDouble from a properly formatted
     * @param value A String of the form X.XXeYYY, where
     *              X.XX is the mantissa and YYY is the exponent.
     */
    public BigDouble(String value) {
        this(BigDouble.parseBigDouble(value));
    }


    private static BigDouble normalize(double mantissa, long exponent) {
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


    private static BigDouble fromMantissaExponentNoNormalize(double mantissa, long exponent) {
        return new BigDouble(mantissa, exponent, new PrivateConstructorArg());
    }

    /**
     * The singular canonical value representing 0.0.
     */
    public static final BigDouble ZERO = fromMantissaExponentNoNormalize(0, 0);

    /**
     * The BigDouble value for 1.0.
     */
    public static final BigDouble ONE
            = fromMantissaExponentNoNormalize(1, 0);

    /**
     * The BigDouble value representing a numerical error in either parsing or mathematical operations.
     */
    public static final BigDouble NaN
            = fromMantissaExponentNoNormalize(Double.NaN, Long.MIN_VALUE);

    /**
     * Check if a BigDouble is Not a Number (NaN).
     * @param value A BigDouble to check.
     * @return Whether the value is NaN.
     */
    public static boolean isNaN(BigDouble value) {
        return Double.isNaN(value.mantissa);
    }

    /**
     * The singular canonical representation of a number too large to represent using a BigDouble.
     */
    public static final BigDouble POSITIVE_INFINITY
            = fromMantissaExponentNoNormalize(Double.POSITIVE_INFINITY, 0);

    /**
     * Determine if a BigDouble is Positive Infinity.
     * @param value A BigDouble to check.
     * @return Whether the value is Positive Infinity.
     */
    public static boolean isPositiveInfinity(BigDouble value) {
        return Double.isInfinite(value.mantissa) && value.mantissa > 0;
    }

    /**
     * The singular canonical representation of a negative number too small to represent using a BigDouble.
     */
    public static final BigDouble NEGATIVE_INFINITY
            = fromMantissaExponentNoNormalize(Double.NEGATIVE_INFINITY, 0);

    /**
     * Determine if a BigDouble is Negative Infinity.
     * @param value A BigDouble to check.
     * @return Whether the value is Negative Infinity.
     */
    public static boolean isNegativeInfinity(BigDouble value) {
        return Double.isInfinite(value.mantissa) && value.mantissa < 0;
    }

    /**
     * Determine if a BigDouble is Infinite.
     * @param value A BigDouble to check.
     * @return Whether the value is Infinite.
     */
    public static boolean isInfinite(BigDouble value) {
        return Double.isInfinite(value.mantissa);
    }

    /**
     * Determine if a BigDouble is not Infinite.
     * @param value A BigDouble to check.
     * @return Whether the value is not Infinite.
     */
    public static boolean isFinite(BigDouble value) {
        return !isInfinite(value);
    }

    /**
     * Parse a String that is either a valid Number or of the form X.XXeYYY
     * for some values X.XX and YYY.
     * @param value A string to parse into a BigDouble
     * @return A BigDouble equivalent to the value provided.
     * @throws RuntimeException if the string is malformed or invalid.
     */
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

    /**
     * Get this BigDouble's mantissa. A double with absolute value between [1, 10) OR exactly 0.
     * @return The mantissa.
     */
    public double getMantissa() {
        return mantissa;
    }

    /**
     * Get this BigDouble's exponent. A long value.
     * @return the exponent.
     */
    public long getExponent() {
        return exponent;
    }

    /**
     *
     * @return The Mantissa.
     * @see #getMantissa()
     */
    public double m() {
        return mantissa;
    }

    /**
     *
     * @return The Exponent
     * @see #getExponent()
     */
    public double e() {
        return exponent;
    }

    /**
     *
     * @return A positive BigDouble with equivalent magnitude to this BigDouble.
     */
    public BigDouble abs() {
        return fromMantissaExponentNoNormalize(Math.abs(mantissa), exponent);
    }

    /**
     *
     * @param value A value to take the absolute value of.
     * @return A positive BigDouble with equivalent magnitude to this value.
     * @see #abs() Delegates to abs() with proper conversion.
     */
    public static BigDouble abs(Number value) {
         return new BigDouble(value).abs();
    }

    /* TODO: The Original JS version uses a ton of typing shenanigans to avoid needing
     * to declare several methods. We don't have that luxury. I'll ignore it for now,
     * But when it becomes a sufficiently big problem I'll address it.
     */

    /**
     *
     * @return A BigDouble with equivalent magnitude to this value but the opposite sign.
     * value.neg().signum() == -value.signum()
     */
    public BigDouble neg() {
        return fromMantissaExponentNoNormalize(-mantissa, exponent);
    }
    /**
     *
     * @param value A value to negate.
     * @return A negated BigDouble.
     * @see #neg() Delegates to neg() with proper conversion.
     */
    public static BigDouble neg(Number value) {
        return new BigDouble(value).neg();
    }
    public BigDouble negate() {
        return neg();
    }
    /**
     * @param value A value to negate.
     * @return A negated BigDouble.
     * @see #neg() Delegates to neg()
     */
    public static BigDouble negate(Number value) {
        return new BigDouble(value).neg();
    }
    /**
     * @return A negated BigDouble.
     * @see #neg() Delegates to neg()
     */
    public BigDouble negated() {
        return neg();
    }
    /**
     * @param value A value to negate.
     * @return A negated BigDouble.
     * @see #neg() Delegates to neg() with proper conversion.
     */
    public static BigDouble negated(Number value) {
        return new BigDouble(value).neg();
    }


    /**
     * @return the signum function of the BigDouble; zero if the argument is zero,
     * 1.0 if the argument is greater than zero, -1.0 if the argument is less than zero.
     * Special Cases:
     * <ul><li>If the argument is NaN, then the result is NaN.</ul>
     */
    public double signum() {
        return Math.signum(mantissa);
    }
    /**
     * @param value a value to get the sign of.
     * @return the sign of this BigDouble.
     * @see #signum() Delegates to signum() with proper conversion.
     */
    public static double signum(Number value) {
        return new BigDouble(value).signum();
    }
    /**
     * @return the sign of this BigDouble.
     * @see #signum() Delegates to signum()
     */
    public double sign() {
        return signum();
    }
    /**
     * @param value a value to get the sign of.
     * @return the sign of this BigDouble.
     * @see #signum() Delegates to signum() with proper conversion.
     */
    public static double sign(Number value) {
        return new BigDouble(value).signum();
    }
    /**
     * @return the sign of this BigDouble.
     * @see #signum() Delegates to signum()
     */
    public double sgn() {
        return signum();
    }
    /**
     * @param value a value to get the sign of.
     * @return the sign of this BigDouble.
     * @see #signum() Delegates to signum() with proper conversion.
     */
    public static double sgn(Number value) {
        return new BigDouble(value).signum();
    }

    /**
     * Returns the closest long to the argument, with ties rounding to positive infinity.
     * Special cases:
     * <ul><li>If the argument is NaN, the result is NaN.
     * <li>If the argument is negative infinity or any value less than or equal to -1 * 10 ^ 17 the result is itself.
     * <li>If the argument is positive infinity or any value greater than or equal to 1 * 10 ^ 17 the result is itself.</ul>
     * @return the value of the BigDouble rounded to the nearest whole number.
     */
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
    /**
     * @see #round() Delegates to round() with proper conversion.
     */
    public static BigDouble round(Number value) {
        return new BigDouble(value).round();
    }

    /**
     * @return the largest (closest to positive infinity)
     * BigDouble value that is less than or equal to the
     * argument and is equal to a mathematical integer. Special cases:
     * <ul><li>If the argument value is already equal to a
     * mathematical integer, then the result is the same as the
     * argument.  <li>If the argument is NaN or an infinity,
     * then the result is the same as the argument.</ul>*/
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
    /**
     * @see #floor() Delgates to floor() with proper conversion.
     */
    public static BigDouble floor(Number value) {
        return new BigDouble(value).floor();
    }

    /**
     * @return the smallest (closest to negative infinity)
     * BigDouble value that is greater than or equal to the
     * argument and is equal to a mathematical integer. Special cases:
     * <ul><li>If the argument value is already equal to a
     * mathematical integer, then the result is the same as the
     * argument.  <li>If the argument is NaN or an infinity,
     * then the result is the same as the argument.
     * <li>If the argument value is less than zero but
     * greater than -1.0, then the result is zero.</ul> Note
     * that the value of {@code x.ceil()} is exactly the
     * value of {@code x.neg().floor().neg()}.
     */
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
    /**
     * @see #ceil() Delgates to ceil() with proper conversion.
     */
    public static BigDouble ceil(Number value) {
        return new BigDouble(value).ceil();
    }

    /**
     * Returns the smallest magnitude (closest to zero)
     * BigDouble value that is less than or equal to the
     * argument and is equal to a mathematical integer. Special cases:
     * <ul><li>If the argument value is already equal to a
     * mathematical integer, then the result is the same as the
     * argument.  <li>If the argument is NaN or an infinity,
     * then the result is the same as the argument.
     * <li>If the argument value is less than 1.0 but
     * greater than -1.0, then the result is zero.</ul>
     */
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
    /**
     * @see #trunc() Delegates to trunc() with proper conversion.
     */
    public static BigDouble trunc(Number value) {
        return new BigDouble(value).trunc();
    }
    /**
     * @see #trunc() Delegates to trunc()
     */
    public BigDouble truncate() {
        return trunc();
    }
    /**
     * @see #trunc() Delegates to trunc() with proper conversion.
     */
    public static BigDouble truncate(Number value) {
        return new BigDouble(value).trunc();
    }

    // TODO: STATIC

    /**
     * Adds two numbers together, returning the result as a BigDouble.
     * Note that BigDouble operations are not in-place, and a new BigDouble
     * instance is instantiated as the return value.
     * @param other a value, which may be a number, BigDouble, or valid String.
     * @return the sum of this BigDouble and the other value.
     */
    public BigDouble add(Number other) {
        BigDouble bd = new BigDouble(other);
        if (isInfinite(this)) return this;
        if (isInfinite(bd)) return bd;

        if (this.mantissa == 0) return bd;
        if (bd.mantissa == 0) return this;

        BigDouble bigger, smaller;

        if (this.exponent > bd.exponent) {
            bigger = this;
            smaller = bd;
        } else {
            // Not always true, but in such a case they're close enough that it doesn't matter.
            bigger = bd;
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
    /**
     * @see #add(Number) Delegates to add(BigDouble other)
     */
    public BigDouble plus(Number other) {
        return add(other);
    }

    // TODO: STATIC

    /**
     * Subtracts the provided value from this BigDouble, returning the result as a BigDouble.
     * Note that BigDouble operations are not in-place, and a new BigDouble
     * instance is instantiated as the return value.
     * @param other a value to subtract, which may be a number, BigDouble, or valid String.
     * @return the difference of this BigDouble and the other value.
     */
    public BigDouble sub(Number other) {
        return add(new BigDouble(other).neg());
    }
    /**
     * @see #sub(Number) Delegates to sub(BigDouble other)
     */
    public BigDouble subtract(Number other) {
        return sub(other);
    }
    /**
     * @see #sub(Number) Delegates to sub(BigDouble other)
     */
    public BigDouble minus(Number other) {
        return sub(other);
    }

    // TODO: STATIC

    /**
     * Multiply two numbers together, returning the result as a BigDouble.
     * Note that BigDouble operations are not in-place, and a new BigDouble
     * instance is instantiated as the return value.
     * @param other a value to multiply, which may be a number, BigDouble, or valid String.
     * @return the product of this BigDouble and the other value.
     */
    public BigDouble mul(Number other) {
        BigDouble bd = new BigDouble(other);
        return normalize(
                this.mantissa * bd.mantissa,
                this.exponent + bd.exponent
        );
    }
    /**
     * @see #mul(Number) Delegates to mul(BigDouble other).
     */
    public BigDouble multiply(Number other) {
        return mul(other);
    }
    /**
     * @see #mul(Number) Delegates to mul(BigDouble other)
     */
    public BigDouble times(Number other) {
        return mul(other);
    }

    // TODO: STATICS

    /**
     * Divides this BigDouble by the provided value, returning the result as a BigDouble.
     * Note that BigDouble operations are not in-place, and a new BigDouble
     * instance is instantiated as the return value. <p> Special cases: <ul><li>If the
     * provided value is 0.0, the result will be NaN.</ul>
     * @param other a value to subtract, which may be a number, BigDouble, or valid String.
     * @return the quotient of this BigDouble and the other value.
     */
    public BigDouble div(Number other) {
        return mul(new BigDouble(other).recip());
    }
    /**
     * @see #div(Number) Delegates to div(BigDouble other).
     */
    public BigDouble divide(Number other) {
        return div(other);
    }
    // NOTE: If we do add in all the things, divideBy and dividedBy don't get statics.
    /**
     * @see #div(Number) Delegates to div(BigDouble other)
     */
    public BigDouble divideBy(Number other) {
        return div(other);
    }
    /**
     * @see #div(Number) Delegates to div(BigDouble other).
     */
    public BigDouble dividedBy(Number other) {
        return div(other);
    }

    /**
     * Returns the reciprocal of this value. <p>Special cases: <ul><li>If the
     * provided value is 0.0, the result will be NaN.<li>If the provided value
     * is Infinite, the result will also be infinite.</ul>
     * @return the sum of this BigDouble and the other value.
     */
    public BigDouble recip() {
        return normalize(1 / mantissa, -exponent);
    }
    /**
     * @see #recip() Delegates to recip() with proper conversion.
     */
    public static BigDouble recip(Number value) {
        return new BigDouble(value).recip();
    }
    /**
     * @see #recip() Delegates to recip().
     */
    public BigDouble reciprocal() {
        return recip();
    }
    /**
     * @see #recip() Delegates to recip() with proper conversion.
     */
    public static BigDouble reciprocal(Number value) {
        return new BigDouble(value).reciprocal();
    }
    /**
     * @see #recip() Delegates to recip().
     */
    public BigDouble reciprocate() {
        return recip();
    }
    /**
     * @see #recip() Delegates to recip() with proper conversion.
     */
    public static BigDouble reciprocate(Number value) {
        return new BigDouble(value).reciprocate();
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

    public int cmp(Number other) {
        return compareTo(new BigDouble(other));
    }

    @Override
    public int hashCode() {
        return Objects.hash(mantissa, exponent);
    }

    /**
     * Indicates whether some value is "equal to" this one.
     * @param obj An object to check for equivalence.
     * @return Whether the object is equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (obj.getClass() != BigDouble.class) return false;
        return equals((BigDouble) obj);
    }

    // Due to object shenanigans with equals(Number), we have to do this one
    // the old way.

    /**
     * Determine if two BigDouble values are exactly equal to each other.
     * Two BigDoubles are equivalent if and only if both their mantissa and
     * exponent are the same.
     * @param other The other value to compare. Can be a String, Double, or BigDouble,
     *              and will be converted appropriately.
     * @return true if the numerical values of both BigDoubles are equal; false
     * otherwise.
     */
    public boolean equals(BigDouble other) {
        return this.exponent == other.exponent && this.mantissa == other.mantissa;
    }
    /**
     * @see #equals(BigDouble) Delegates to equals(BigDouble) with proper conversion.
     */
    public boolean equals(double other) {
        return this.equals(new BigDouble(other));
    }
    /**
     * @see #equals(BigDouble) Delegates to equals(BigDouble) with proper conversion.
     */
    public boolean equals(String other) {
        return this.equals(BigDouble.parseBigDouble(other));
    }
    /**
     * @see #equals(BigDouble) Delegates to equals(BigDouble) with proper conversion
     */
    public boolean eq(Number other) {
        return equals(new BigDouble(other));
    }

    /**
     * Returns the opposite of equals().
     * @see #equals(BigDouble)
     */
    public boolean neq(Number other) {
        return !equals(new BigDouble(other));
    }
    /**
     * @see #neq(Number) Delegates to neq(BigDouble).
     */
    public boolean notEquals(BigDouble other) {
        return !equals(other);
    }

    /**
     * Determine if this BigDouble is less than the provided value.
     * @param other The other value to compare. Can be a String, Double, or BigDouble,
     *              and will be converted appropriately.
     * @return true if and only if this BigDouble is less than the provided value, false otherwise.
     */
    public boolean lt(Number other) {
        return compareTo(new BigDouble(other)) < 0;
    }
    /**
     * @see #lt(Number) Delegates to lt(BigDouble).
     */
    public boolean lessThan(Number other) {
        return lt(other);
    }

    /**
     * Determine if this BigDouble is less than or equal to the provided value.
     * @param other The other value to compare. Can be a String, Double, or BigDouble,
     *              and will be converted appropriately.
     * @return true if and only if this BigDouble is less than or equal to
     * the provided value, false otherwise.
     */
    public boolean lte(Number other) {
        return compareTo(new BigDouble(other)) <= 0;
    }
    /**
     * @see #lte(Number) Delegates to lte(BigDouble).
     */
    public boolean lessThanOrEqualTo(Number other) {
        return lte(other);
    }

    /**
     * Determine if this BigDouble is greater than the provided value.
     * @param other The other value to compare. Can be a String, Double, or BigDouble,
     *              and will be converted appropriately.
     * @return true if and only if this BigDouble is greater than the provided value,
     * false otherwise.
     */
    public boolean gt(Number other) {
        return compareTo(new BigDouble(other)) > 0;
    }
    /**
     * @see #gt(Number) Delegates to gt().
     */
    public boolean greaterThan(Number other) {
        return gt(other);
    }

    /**
     * Determine if this BigDouble is greater than or equal to the provided value.
     * @param other The other value to compare. Can be a String, Double, or BigDouble,
     *              and will be converted appropriately.
     * @return true if and only if this BigDouble is greater than or equal to
     * the provided value, false otherwise.
     */
    public boolean gte(Number other) {
        return compareTo(new BigDouble(other)) >= 0;
    }
    /**
     * @see #gte(Number) Delegates to gte(BigDouble).
     */
    public boolean greaterThanOrEqualTo(Number other) {
        return gte(other);
    }

    /**
     * Returns the greater of this BigDouble or the other value.
     * @param other The value to compare this BigDouble to. Can be a String, Double,
     *              or BigDouble, and will be converted appropriately.
     * @return The greater value, as a BigDouble.
     */
    public BigDouble max(Number other) {
        BigDouble bd = new BigDouble(other);
        return compareTo(bd) > 0 ? this : bd;
    }

    /**
     * Returns the smallest of this BigDouble or the other value.
     * @param other The value to compare this BigDouble to. Can be a String, Double,
     *              or BigDouble, and will be converted appropriately.
     * @return The smaller value, as a BigDouble.
     */
    public BigDouble min(Number other) {
        BigDouble bd = new BigDouble(other);
        return compareTo(bd) < 0 ? this : bd;
    }

    /**
     * Determine if a value is within two bounds. If it is below or above those bounds,
     * return the value of lower or higher, respectively.
     * @param lower The lower bound that this BigDouble may be.
     * @param higher The upper bound that this BigDouble may be.
     * @return This BigDouble value, unless outside the bounds defined by lower and higher.
     */
    public BigDouble clamp(Number lower, Number higher) {
        BigDouble low = new BigDouble(lower), high = new BigDouble(higher);
        // Technically this means the bounds can be in whatever order.
        // You can't expect this all the time.
        if (low.gt(high)) {
            BigDouble tmp = low;
            low = high;
            high = tmp;
        }
        return max(low).min(high);
    }

    /**
     * Clamp a value such that it must be at least "value".
     * @param other A lower bound that this BigDouble should be above.
     * @return This BigDouble, unless less than "other", in which case "other" is returned.
     */
    public BigDouble clampMin(Number other) {
        return max(other);
    }

    /**
     * Clamp a value such that it must be at most "value".
     * @param other An upper bound that this BigDouble should be below.
     * @return This BigDouble, unless greater than "other", in which case "other" is returned.
     */
    public BigDouble clampMax(Number other) {
        return min(other);
    }

    /**
     * Compare this value with another value, with the assumption that if they
     * are within some amount, they are effectively equal.
     * @param other The value to compare this BigDouble with.
     *              Must be a BigDouble, number, or formatted String.
     * @param tolerance How close the values must be before we can say they are
     *                  equal.
     * @return 0 if the two are within tolerance of each other;
     * follows {@link #compareTo(BigDouble)} otherwise.
     */
    public int cmp_tolerance(Number other, Number tolerance) {
        return eq_tolerance(other, tolerance) ? 0 : cmp(other);
    }
    /**
     * @see #cmp_tolerance(Number, Number)
     * Delegates to cmp_tolerance(BigDouble, BigDouble).
     */
    public int compare_tolerance(Number other, Number tolerance) {
        return cmp_tolerance(other, tolerance);
    }

    /**
     * Determine if two values are reasonably close together. If the magnitude of the
     * difference between the two values is less than tolerance, then the values will
     * be treated as equal.
     * @param other The value to compare this BigDouble to.
     * @param tolerance The maximum amount that the values can differ by while being equivalent.
     * @return Whether the values are within tolerance of each other.
     */
    public boolean eq_tolerance(Number other, Number tolerance) {
        BigDouble bd = new BigDouble(other);
        BigDouble tol = new BigDouble(tolerance);
        return sub(bd).abs().lte(
                this.abs().max(bd.abs()).mul(tol)
        );
    }

    /**
     * @see #eq_tolerance(Number, Number)   Delegates to eq_tolerance(BigDouble, BigDouble).
     */
    public boolean equals_tolerance(Number other, Number tolerance) {
        return eq_tolerance(other, tolerance);
    }

    public boolean neq_tolerance(Number other, Number tolerance) {
        return !eq_tolerance(other, tolerance);
    }
    public boolean notEquals_tolerance(Number other, Number tolerance) {
        return neq_tolerance(other, tolerance);
    }

    public boolean lt_tolerance(Number other, Number tolerance) {
        return !eq_tolerance(other, tolerance) && lt(other);
    }

    public boolean lte_tolerance(Number other, Number tolerance) {
        return eq_tolerance(other, tolerance) || lt(other);
    }

    public boolean gt_tolerance(Number other, Number tolerance) {
        return !eq_tolerance(other, tolerance) && gt(other);
    }

    public boolean gte_tolerance(Number other, Number tolerance) {
        return eq_tolerance(other, tolerance) || gt(other);
    }

    /**
     * Returns the base 10 logarithm of this BigDouble value.
     * Special cases:
     *
     * <ul><li>If the argument is NaN or less than zero, then the result
     * is NaN.
     * <li>If the argument is positive infinity, then the result is
     * positive infinity.
     * <li>If the argument is positive zero or negative zero, then the
     * result is negative infinity.
     * <li>If the argument is equal to 10<sup><i>n</i></sup> for
     * integer <i>n</i>, then the result is <i>n</i>. In particular,
     * if the argument is {@code 1.0} (10<sup>0</sup>), then the
     * result is positive zero.
     * </ul>
     */
    public double log10() {
        return exponent + Math.log10(mantissa);
    }

    /**
     * @see #log10() Delegates to log10() with proper conversion.
     */
    public static double log10(Number value) {
        return new BigDouble(value).log10();
    }

    /**
     * Returns the base 10 logarithm of the magnitude of this BigDouble value.
     * Special cases:
     *
     * <ul><li>If the argument is NaN, then the result is NaN.
     * <li>If the argument is infinite, then the result is
     * positive infinity.
     * <li>If the argument is positive zero or negative zero, then the
     * result is negative infinity.
     * </ul>
     */
    public double absLog10() {
        return exponent + Math.log10(Math.abs(mantissa));
    }
    /**
     * @see #absLog10()  Delegates to absLog10().
     */
    public static double absLog10(Number value) {
        return new BigDouble(value).absLog10();
    }

    /**
     * Returns the base 10 logarithm of a BigDouble value, clamped to 0.
     * Special cases:
     *
     * <ul><li>If the argument is NaN, then the result
     * is NaN.
     * <li>If the argument is positive infinity, then the result is
     * positive infinity.
     * <li>If the argument is less than 1.0, the result is 0.0.
     * <li>If the argument is equal to 10<sup><i>n</i></sup> for
     * integer <i>n</i>, then the result is <i>n</i>. In particular,
     * if the argument is {@code 1.0} (10<sup>0</sup>), then the
     * result is positive zero.
     * </ul>
     */
    public double pLog10() {
        return mantissa <= 0 || exponent < 0 ? 0 : log10();
    }
    /**
     * @see #pLog10()  Delegates to pLog10().
     */
    public static double pLog10(Number value) {
        return new BigDouble(value).pLog10();
    }

    /**
     * Returns the natural logarithm of this BigDouble value.
     * Special cases:
     *
     * <ul><li>If the argument is NaN or less than zero, then the result
     * is NaN.
     * <li>If the argument is positive infinity, then the result is
     * positive infinity.
     * <li>If the argument is positive zero or negative zero, then the
     * result is negative infinity.
     * <li>If the argument is equal to 10<sup><i>n</i></sup> for
     * integer <i>n</i>, then the result is <i>n</i>. In particular,
     * if the argument is {@code 1.0} (10<sup>0</sup>), then the
     * result is positive zero.
     * </ul>
     */
    public double log() {
        return 2.302585092994046 * log10();
    }
    /**
     * @see #log()  Delegates to log().
     */
    public double logarithm() {
        return log();
    }
    /**
     * @see #log()  Delegates to log() with proper conversion.
     */
    public static double logarithm(Number value) {
        return new BigDouble(value).logarithm();
    }

    /**
     * Returns the logarithm of this BigDouble value, with the given base.
     * Special cases:
     *
     * <ul><li>If the argument is NaN or less than zero, then the result
     * is NaN.
     * <li>If the argument is positive infinity, then the result is
     * positive infinity.
     * <li>If the argument is positive zero or negative zero, then the
     * result is negative infinity.
     * <li>If the argument is equal to 10<sup><i>n</i></sup> for
     * integer <i>n</i>, then the result is <i>n</i>. In particular,
     * if the argument is {@code 1.0} (10<sup>0</sup>), then the
     * result is positive zero.
     * <li>If the base is less than 1, the result will equal
     * this.recip().log(1 / base).
     * </ul>
     * @param base the base for this logarithm.
     * @return the logarithm base B of this BigDouble.
     */
    public double log(double base) {
        // UN-SAFETY: Most incremental game cases are log(number := 1 or greater, base := 2 or greater).
        // We assume this to be true and thus only need to return a number, not a Decimal,
        // and don't do any other kind of error checking.

        // Also, Math.LN10 = 2.302585092994046. Dammit Java...
        return 2.302585092994046 / Math.log(base) * log10();
    }
    /**
     * @see #log(double)  Delegates to log(double).
     */
    public double logarithm(double base) {
        return log(base);
    }

    /**
     * Returns the base 10 logarithm of this BigDouble value.
     * Special cases:
     *
     * <ul><li>If the argument is NaN or less than zero, then the result
     * is NaN.
     * <li>If the argument is positive infinity, then the result is
     * positive infinity.
     * <li>If the argument is positive zero or negative zero, then the
     * result is negative infinity.
     * <li>If the argument is equal to 10<sup><i>n</i></sup> for
     * integer <i>n</i>, then the result is <i>n</i>. In particular,
     * if the argument is {@code 1.0} (10<sup>0</sup>), then the
     * result is positive zero.
     * </ul>
     */
    public double log2() {
        return 3.321928094887362 * log10();
    }

    /**
     * @see #log()  Delegates to log().
     */
    public double ln() {
        return log();
    }
    /**
     * @see #log()  Delegates to log() with proper conversion.
     */
    public static double ln(Number value) {
        return new BigDouble(value).ln();
    }

    /**
     * Create a new BigDouble that is 10 ^ value.
     * Equivalent to new BigDouble(1, value).
     * @param value the value 10 is being raised to.
     * @return A bigDouble of the form 1e+Value.
     */
    public static BigDouble pow10(long value) {
        return fromMantissaExponentNoNormalize(1, value);
    }

    /**
     * Create a new BigDouble value that is 10^value.
     * @param value The value that 10 is being raised to.
     * @return A BigDouble equal to 10^value.
     */
    public static BigDouble pow10(double value) {
        long valueAsLong = (long) value;
        // UN-SAFETY: if value is larger than a long, then the program will break anyway.
        double residual = value - valueAsLong;
        if (Math.abs(residual) < Constants.ROUND_TOLERANCE) {
            return fromMantissaExponentNoNormalize(1, valueAsLong);
        }
        return normalize(Math.pow(10, residual), valueAsLong);
    }
    /**
     * @see #pow10(double)  Delegates to pow10(double) with proper conversion.
     */
    public static BigDouble pow10(BigDouble value) {
        return pow10(value.toDouble());
    }

    /**
     * @see #pow(double)  Delegates to pow(double) with proper conversion.
     */
    public BigDouble pow(Number power) {
        // UN-SAFETY: if power > Double.MAX_VALUE,
        // anything raised to it is either 0 or infinite.

        return pow(new BigDouble(power).toDouble());
    }

    /**
     * Raise a BigDouble to the given power.
     * <p>Special cases:
     * <ul><li>If power is negative, and this BigDouble is not an integer,
     * returns NaN.
     * <li>If power is less than zero and not an integer, return NaN.
     * <li>If this BigDouble is negative, and power is not an integer,
     * return NaN.
     * <li>If this BigDouble is negative, and power is an odd integer,
     * the result will be negative.
     * </ul>
     * @param power the value to raise this BigDouble to.
     * @return The result as a BigDouble.
     */
    public BigDouble pow(double power) {
        // GUARD: 0 ^ Anything = 0, except 0.
        if (mantissa == 0) return power == 0 ? ONE : this;

        // GUARD: -XXX ^ 2.5 = NaN
        boolean powerIsInteger =
                Math.abs(power) < 9007199254740991L
                && Math.floor(power) == power;

        if (mantissa < 0 && !powerIsInteger) return NaN;

        // FAIL-FAST: 10 ^ x can be computed quickly.
        boolean is10 = exponent == 1 && mantissa - 1 < Double.MIN_VALUE;
        if (is10) return pow10(power);

        // FAST-TRACK: if (exponent * value) is an int and mantissa ^ value < e308,
        // Then we can do a very fast method.
        double temp = exponent * power;
        double newMantissa;
        if (Math.abs(temp) < 9007199254740991L && Math.floor(temp) == temp) {
            newMantissa = Math.pow(mantissa, power);
            if (Double.isFinite(newMantissa) && newMantissa != 0) {
                return normalize(newMantissa, (long)temp);
            }
        }

        long newExponent = (long) temp;
        double residue = temp - newExponent;
        newMantissa = Math.pow(10, power * Math.log10(mantissa) + residue);
        if (Double.isFinite(newMantissa) && newMantissa != 0) {
            return normalize(newMantissa, newExponent);
        }

        // Dumb math time: pow10(pow * this.log10())
        BigDouble result = BigDouble.pow10(power * this.absLog10());
        if (sign() == -1 && power % 2 == 1) {
            // We did NaN checking at the beginning.
            return result.neg();
        }
        return result;
    }

    /**
     * Returns Euler's number <i>e</i> raised to the power of a
     * {@code double} value.  Special cases:
     * <ul><li>If the argument is NaN, the result is NaN.
     * <li>If the argument is positive infinity, then the result is
     * positive infinity.
     * <li>If the argument is negative infinity, then the result is
     * positive zero.
     * <li>If the argument is zero, then the result is {@code 1.0}.
     * </ul>
     * @return The value e^a, where a is this BigDouble.
     */
    public BigDouble exp() {
        double x = toDouble();
        if (-706 < x && x < 709) return new BigDouble(Math.exp(x));
        return new BigDouble(Math.E).pow(this);
    }
    /**
     * @see #exp()  Delegates to exp() with proper conversion.
     */
    public static BigDouble exp(Number value) {
        return new BigDouble(value).exp();
    }

    /**
     * Squares this BigDouble.
     * @return the square of this BigDouble.
     */
    public BigDouble sqr() {
        return normalize(mantissa * mantissa, exponent * 2);
    }
    /**
     * @see #sqr()  Delegates to sqr() with proper conversion.
     */
    public static BigDouble sqr(Number value) {
        return new BigDouble(value).sqr();
    }

    /**
     * Returns the positive square root of this BigDouble value.
     * <p>Special cases:
     * <ul><li>If the argument is NaN or less than zero, then the result
     * is NaN.
     * <li>If the argument is positive infinity, then the result is positive
     * infinity.
     * <li>If the argument is positive zero or negative zero, then the
     * result is the same as the argument.</ul>
     * Otherwise, the result is a BigDouble approximately equal to
     * the true mathematical square root of this BigDouble's value.
     *
     * @return  the positive square root of this BigDouble.
     *          If the argument is NaN or less than zero, the result is NaN.
     */
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
    /**
     * @see #sqrt()  Delegates to sqrt() with proper conversion.
     */
    public static BigDouble sqrt(Number value) {
        return new BigDouble(value).sqrt();
    }

    /**
     * Cubes this BigDouble. The cube will have the same
     * sign as the original.
     * @return the cube of this BigDouble.
     */
    public BigDouble cube() {
        return normalize(
                mantissa * mantissa * mantissa,
                exponent * 3
        );
    }
    /**
     * @see #cube()  Delegates to cube() with proper conversion.
     */
    public static BigDouble cube(Number value) {
        return new BigDouble(value).cube();
    }

    /**
     * Returns the cube root of this BigDouble value.  For
     * positive finite {@code x}, {@code x.neg().cbrt() ==
     * x.cbrt().neg()}; that is, the cube root of a negative value is
     * the negative of the cube root of that value's magnitude.
     *
     * <p>Special cases:
     *
     * <ul>
     *
     * <li>If the argument is NaN, then the result is NaN.
     *
     * <li>If the argument is infinite, then the result is an infinity
     * with the same sign as the argument.
     *
     * <li>If the argument is zero, then the result is a zero with the
     * same sign as the argument.
     *
     * </ul>
     *
     * @return  the cube root of this BigDouble.
     */
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
    /**
     * @see #cbrt()  Delegates to cbrt() with proper conversion.
     */
    public static BigDouble cbrt(Number value) {
        return new BigDouble(value).cbrt();
    }

    /**
     * If you're willing to spend 'resourcesAvailable' and want to buy something
     * with exponentially increasing cost each purchase (start at priceStart,
     * multiply by priceRatio, already own currentOwned), how much of it can you buy?
     * Adapted from Trimps source code.
     */
    public static BigDouble affordGeometricSeries(
            Number resourcesAvailable,
            Number priceStart,
            Number priceRatio,
            long currentOwned
    ) {
        BigDouble avail = new BigDouble(resourcesAvailable);
        BigDouble start = new BigDouble(priceStart);
        BigDouble ratio = new BigDouble(priceRatio);

        BigDouble actualStart = start.mul(ratio.pow(currentOwned));

        return new BigDouble(Math.floor(
                avail.div(actualStart).mul(ratio.sub(ONE)).add(ONE).log10()
                / ratio.log10()
        ));
    }

    /**
     * How much resource would it cost to buy (numItems) items if you already have currentOwned,
     * the initial price is priceStart, and it multiplies by priceRatio each purchase?
     */
    public static BigDouble sumGeometricSeries(
            int numItems,
            Number priceStart,
            Number priceRatio,
            int currentOwned
    ) {
        BigDouble ratio = new BigDouble(priceRatio);
        return new BigDouble(priceStart)
                .mul(ratio.pow(currentOwned))
                .mul(ONE.sub(ratio.pow(numItems)))
                .div(ONE.sub(ratio));
    }

    /**
     * If you're willing to spend 'resourcesAvailable' and want to buy something with additively
     * increasing cost each purchase (start at priceStart, add by priceAdd, already own currentOwned),
     * how much of it can you buy?
     */
    public static BigDouble affordArithmeticSeries(
            Number resourcesAvailable,
            Number priceStart,
            Number priceAdd,
            int currentOwned
    ) {
        BigDouble avail = new BigDouble(resourcesAvailable);
        BigDouble start = new BigDouble(priceStart);
        BigDouble add = new BigDouble(priceAdd);

        BigDouble actualStart = start.add(add.mul(currentOwned));
        BigDouble b = actualStart.sub(add.div(2));
        BigDouble b2 = b.pow(2);

        return b.neg()
                .add(b2.add(add.mul(avail).mul(2)).sqrt())
                .div(add)
                .floor();
    }

    /**
     * How much resource would it cost to buy (numItems) items if you already have currentOwned,
     * the initial price is priceStart and it adds priceAdd each purchase?
     * Adapted from <a href="http://www.mathwords.com/a/arithmetic_series.htm">...</a>
     */
    public static BigDouble sumArithmeticSeries(
            int numItems,
            Number priceStart,
            Number priceAdd,
            int currentOwned
    ) {
        BigDouble start = new BigDouble(priceStart);
        BigDouble add = new BigDouble(priceAdd);
        BigDouble actualStart = start.add(add.mul(currentOwned));

        // (n/2)*(2*a+(n-1)*d)
        // numItems
        return new BigDouble(numItems)
                .div(2)
                .mul(actualStart.mul(2).plus(new BigDouble(numItems).sub(ONE).mul(add)));
    }

    /**
     * When comparing two purchases that cost (resource) and increase your resource/sec by (deltaRpS),
     * the lowest efficiency score is the better one to purchase.
     * From Frozen Cookies:
     * <a href="http://cookieclicker.wikia.com/wiki/Frozen_Cookies_(JavaScript_Add-on)#Efficiency.3F_What.27s_that.3F">...</a>
     */
    public static BigDouble efficiencyOfPurchase(
            Number cost, Number currentRpS, Number deltaRpS
    ) {
        BigDouble bdCost = new BigDouble(cost);
        return bdCost.div(currentRpS).add(bdCost.div(deltaRpS));
    }

    private static BigDouble randomDecimalForTesting(long absMaxExponent) {
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

    @Override
    public int intValue() {
        return (int) this.toDouble();
    }

    @Override
    public long longValue() {
        return (long) this.toDouble();
    }

    @Override
    public float floatValue() {
        return (float) this.toDouble();
    }

    @Override
    public double doubleValue() {
        return this.toDouble();
    }

    /**
     * Convert this value to a double.
     * Special cases:
     * <ul><li>If this value's magnitude is too large to convert to
     * a Double, return an infinity with the same sign as the argument.
     * <li>If this value is too small to convert to a double, return
     * 0.0.
     * <li>If this value is within ROUND_TOLERANCE of an integer,
     * return the value rounded be an exact integer.
     * </ul>
     * @return a double equal to this BigDouble's value.
     */
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

    /**
     * Get the current Mantissa, truncated to a specific number of decimal places.
     * Formatting always rounds towards 0.
     * Special cases:
     * <ul><li>If this BigDouble is infinite, NaN, or 0, no formatting is applied.
     * <li>Supplying a places value larger than 17 will simply return the mantissa.
     * </ul>
     * @param places The number of places to represent.
     * @return The mantissa, rounded to the specified number of places.
     */
    public double mantissaWithDecimalPlaces(int places) {
        if (isInfinite(this) || isNaN(this)) {
            return mantissa;
        }

        if (mantissa == 0) {
            return 0;
        }

        // TODO: would simple multiplication, rounding, and division be better?
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

    /**
     * Return a string representation of this BigDecimal, forcefully
     * formatted in scientific notation (X.XXeYYY, for some values X.XX and Y)
     * @param places The number of places in the mantissa.
     * @return a String representation of this BigDouble with the requested
     * number of places in the mantissa.
     */
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

        // TODO: wait, doesn't MantissaWithDecimalPlaces do this already?
        return df.format(rounded) + "e" + (exponent >= 0 ? "+" : "") + exponent;
    }

    /**
     * Return a string representation of this BigDecimal, formatted
     * with a specific number of places after the decimal point.
     * Will pad extra positions with zeroes.
     * @param places the number of places after the decimal point to show.
     * @return A string representation, with digits added or removed to reach
     * the specified number of places.
     */
    private String toFixed(int places) {
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
        // 2) exponent is 16 or less: use "basic" toFixed

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

    /**
     * Return a string representation of this BigDecimal, formatted
     * to have at least a specific number of places afer the decimal
     * point regardless of if it is big enough to be represented in
     * Scientific notation or not.
     * @param places The number of places after the decimal point to use.
     * @return A string representation of this BigDouble with the specified
     * number of decimal places.
     */
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
    public static BigDouble sinh(Number value) {
        return new BigDouble(value).sinh();
    }

    public BigDouble cosh() {
        return this.exp().add(this.neg().exp()).div(2);
    }
    public static BigDouble cosh(Number value) {
        return new BigDouble(value).cosh();
    }

    public BigDouble tanh() {
        return sinh().div(cosh());
    }
    public static BigDouble tanh(Number value) {
        return new BigDouble(value).tanh();
    }

    public double asinh() {
        return ln(this.add(sqr().add(ONE).sqrt()));
    }
    public static double asinh(Number value) {
        return new BigDouble(value).asinh();
    }

    public double acosh() {
        return add(ONE).div(ONE.sub(this)).ln() / 2;
    }
    public static double acosh(Number value) {
        return new BigDouble(value).acosh();
    }

    public double atanh() {
        if (this.abs().gte(1)) return Double.NaN;
        return ln(this.add(1).div(ONE.sub(this))) / 2;
    }
    public static double atanh(Number value) {
        return new BigDouble(value).atanh();
    }

    /**
     * Joke function from Realm Grinder
     * @return the Ascension Penalty for the number of
     * ascensions you have done.
     */
    public BigDouble ascensionPenalty(int ascensions) {
        if (ascensions == 0) {
            return this;
        }
        return this.pow(Math.pow(10, -ascensions));
    }

    /**
     * Joke function from Cookie Clicker. It's 'egg'
     * @return egg.
     */
    public BigDouble egg() {
        return this.add(9);
    }

    private static class PrivateConstructorArg { }

}