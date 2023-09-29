package BreakInfinity;


import org.jetbrains.annotations.NotNull;

public class BigDouble {
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





    private static class PrivateConstructorArg { }

    public static void main(String[] args) {
        BigDouble x = new BigDouble(932472349832742932L);
        System.out.print(x.mantissa);
        System.out.print('e');
        System.out.println(x.exponent);
    }

}