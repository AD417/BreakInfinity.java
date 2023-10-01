package BreakInfinity;

import java.text.DecimalFormat;
import org.jetbrains.annotations.NotNull;

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

    public double log10() {
        return exponent + Math.log10(mantissa);
    }

    public double absLog10() {
        return exponent + Math.log10(Math.abs(mantissa));
    }

    public double pLog10() {
        return mantissa <= 0 || exponent < 0 ? 0 : log10();
    }

    public double log() {
        return 2.302585092994046 * log10();
    }
    public double logarithm() {
        return log();
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

    public BigDouble sqr() {
        return normalize(mantissa * mantissa, exponent * 2);
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

    public BigDouble cube() {
        return normalize(
                mantissa * mantissa * mantissa,
                exponent * 3
        );
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
        // TODO: numItems
        return priceStart
                .mul(priceRatio.pow(currentOwned))
                .mul(ONE.sub(priceRatio.pow(new BigDouble(numItems))))
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
        // TODO: currentOwned, 2, 2
        BigDouble actualStart = priceStart.add(new BigDouble(currentOwned).mul(priceAdd));
        BigDouble b = actualStart.sub(priceAdd.div(new BigDouble(2)));
        BigDouble b2 = b.pow(new BigDouble(2));

        return b.neg()
                .add(b2.add(priceAdd.mul(resourcesAvailable).mul(new BigDouble(2))).sqrt())
                .div(priceAdd)
                .floor();
    }

    /**
     * How much resource would it cost to buy (numItems) items if you already have currentOwned,
     * the initial price is priceStart and it adds priceAdd each purchase?
     * Adapted from http://www.mathwords.com/a/arithmetic_series.htm
     */
    public static BigDouble sumArithmeticSeries(
            int numItems,
            BigDouble priceStart,
            BigDouble priceAdd,
            int currentOwned
    ) {
        // TODO: currentOwned.
        BigDouble actualStart = priceStart.add(new BigDouble(currentOwned).mul(priceAdd));

        // (n/2)*(2*a+(n-1)*d)
        // TODO: 2, 2, numItems
        return new BigDouble(numItems)
                .div(new BigDouble(2))
                .mul(actualStart
                        .mul(new BigDouble(2))
                        .plus(new BigDouble(numItems).sub(ONE).mul(priceAdd))
                );
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

    public String toFixed(int places) {
        if (isInfinite(this)) return Double.toString(mantissa);
        if (mantissa == 0 || exponent < -Constants.EXP_LIMIT) {
            return "0" + RepeatZeroes.trailZeroes(places);
        }

        String mantissaStr = Double.toString(mantissa).replace(".", "");//, this.e + 1, "0");
        String mantissaZeroes = RepeatZeroes.repeatZeroes(
                (int)exponent - mantissaStr.length() + 1
        );
        return mantissa + mantissaZeroes + RepeatZeroes.trailZeroes(places);
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
        // TODO: 2
        return this.exp().sub(this.neg().exp()).div(new BigDouble(2));
    }

    public BigDouble cosh() {
        // TODO: 2
        return this.exp().add(this.neg().exp()).div(new BigDouble(2));
    }

    public BigDouble tanh() {
        return sinh().div(cosh());
    }

    public double asinh() {
        // TODO: ln
        return add(sqr().add(ONE).sqrt()).ln();
    }

    public double acosh() {
        return add(ONE).div(ONE.sub(this)).ln() / 2;
    }

    public double atanh() {
        // TODO: 1
        if (this.abs().gte(ONE)) return Double.NaN;
        return this.add(ONE).div(new BigDouble(1).sub(this)).ln() / 2;
    }

    /**
     * Joke function from Realm Grinder
     */
    public BigDouble egg() {
        // TODO: 9
        return this.add(new BigDouble(9));
    }

    private static class PrivateConstructorArg { }

    public static void main(String[] args) {
        String x;
        for (int i = 0; i < 100; i++) {
            x = BigDouble.randomDecimalForTesting(1000).toPrecision(5);
            System.out.println(x);
        }
    }

}
