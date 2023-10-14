package io.github.ad417.BreakInfinity;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class BigDoubleTest {
    static final Random Generator = new Random();

    public BigDouble randomValue() {
        return new BigDouble(
                Generator.nextDouble(1, 10),
                Generator.nextLong()
        );
    }

    public double randomMantissa() {
        return Generator.nextDouble(1, 10);
    }

    public long randomExponent() {
        return Generator.nextLong();
    }

    @Test
    void isNaN() {
        BigDouble x;
        for (int i = 0; i < 10000; i++) {
            x = randomValue();
            assertFalse(
                    BigDouble.isNaN(x),
                    String.format("Value '%s' is not NaN!", x)
            );
        }
        assertTrue(BigDouble.isNaN(BigDouble.NaN));
        assertFalse(BigDouble.isNaN(BigDouble.POSITIVE_INFINITY));
        assertFalse(BigDouble.isNaN(BigDouble.NEGATIVE_INFINITY));
    }

    @Test
    void isPositiveInfinity() {
        BigDouble x;
        for (int i = 0; i < 10000; i++) {
            x = randomValue();
            assertFalse(
                    BigDouble.isPositiveInfinity(x),
                    String.format("Value '%s' is not Infinite!", x)
            );
        }
        assertTrue(BigDouble.isPositiveInfinity(BigDouble.POSITIVE_INFINITY));
        assertFalse(BigDouble.isPositiveInfinity(BigDouble.NEGATIVE_INFINITY));
        assertFalse(BigDouble.isPositiveInfinity(BigDouble.NaN));
    }

    @Test
    void isNegativeInfinity() {
            BigDouble x;
            for (int i = 0; i < 10000; i++) {
                x = randomValue();
                assertFalse(
                        BigDouble.isNegativeInfinity(x),
                        String.format("Value '%s' is not Infinite!", x)
                );
            }
            assertTrue(BigDouble.isPositiveInfinity(BigDouble.POSITIVE_INFINITY));
            assertFalse(BigDouble.isPositiveInfinity(BigDouble.NEGATIVE_INFINITY));
            assertFalse(BigDouble.isPositiveInfinity(BigDouble.NaN));
    }

    @Test
    void isInfinite() {
            BigDouble x;
            for (int i = 0; i < 10000; i++) {
                x = randomValue();
                assertFalse(
                        BigDouble.isInfinite(x),
                        String.format("Value '%s' is not Infinite!", x)
                );
            }
            assertTrue(BigDouble.isInfinite(BigDouble.POSITIVE_INFINITY));
            assertTrue(BigDouble.isInfinite(BigDouble.NEGATIVE_INFINITY));
            assertFalse(BigDouble.isInfinite(BigDouble.NaN));
    }

    @Test
    void isFinite() {
            BigDouble x;
            for (int i = 0; i < 10000; i++) {
                x = randomValue();
                assertTrue(
                        BigDouble.isFinite(x),
                        String.format("Value '%s' is finite!", x)
                );
            }
            assertFalse(BigDouble.isFinite(BigDouble.POSITIVE_INFINITY));
            assertFalse(BigDouble.isFinite(BigDouble.NEGATIVE_INFINITY));
            // Probably irrelevant? NaN is definitionally not a number.
            assertTrue(BigDouble.isFinite(BigDouble.NaN));
    }

    @Test
    void parseBigDouble() {
        BigDouble x = BigDouble.parseBigDouble("1");
        assertTrue(BigDouble.ONE.equals(x));

        x = BigDouble.parseBigDouble("123456789");
        assertTrue(new BigDouble(123456789).equals(x));

        x = BigDouble.parseBigDouble("1e100");
        assertTrue(new BigDouble(1e100).equals(x));

        x = BigDouble.parseBigDouble("1e5000");
        assertTrue(new BigDouble(1, 5000).equals(x));
    }

    @Test
    void getMantissa() {
        double mantissa;
        BigDouble x;
        for (int i = 0; i < 1000; i++) {
            mantissa = randomMantissa();
            x = new BigDouble(mantissa, randomExponent());
            assertEquals(mantissa, x.getMantissa());
        }

        mantissa = 1.32764632e238;
        x = new BigDouble(mantissa);
        assertEquals(1.32764632, x.getMantissa());
    }

    @Test
    void getExponent() {
        long exponent;
        BigDouble x;
        for (int i = 0; i < 1000; i++) {
            exponent = randomExponent();
            x = new BigDouble(randomMantissa(), exponent);
            assertEquals(exponent, x.getExponent());
        }

        exponent = 10;
        x = new BigDouble(3e30, exponent);
        assertEquals(40, x.getExponent());
    }

    @Test
    void abs() {
        BigDouble x;
        for (int i = 0; i < 100; i++) {
            x = randomValue();
            assertTrue(x.equals(x.abs()));
        }

        for (int i = 0; i < 100; i++) {
            x = randomValue().neg();
            assertEquals(-x.getMantissa(), x.abs().getMantissa());
        }
    }

    @Test
    void negate() {
        BigDouble x;
        for (int i = 0; i < 1000; i++) {
            x = randomValue();
            assertEquals(-x.getMantissa(), x.neg().getMantissa());
        }
    }

    @Test
    void sign() {
        BigDouble x;
        for (int i = 0; i < 1000; i++) {
            x = randomValue();
            x = Generator.nextBoolean() ? x : x.neg();
            assertEquals(x.sign(), Math.signum(x.getMantissa()));
        }
    }

    @Test
    void round() {
        BigDouble x = new BigDouble(0.9);
        assertEquals(BigDouble.ONE, x.round());

        x = new BigDouble(1.1);
        assertEquals(BigDouble.ONE, x.round());

        x = new BigDouble(1.5);
        assertEquals(new BigDouble(2), x.round());

        x = BigDouble.ONE;
        assertEquals(x, x.round());
    }

    @Test
    void floor() {
        BigDouble x = new BigDouble(0.9);
        assertEquals(BigDouble.ZERO, x.floor());

        x = new BigDouble(1.1);
        assertEquals(BigDouble.ONE, x.floor());

        x = new BigDouble(1.5);
        assertEquals(BigDouble.ONE, x.floor());

        x = BigDouble.ONE;
        assertEquals(x, x.floor());
    }

    @Test
    void ceil() {
        BigDouble x = new BigDouble(0.9);
        assertEquals(BigDouble.ONE, x.ceil());

        x = new BigDouble(1.1);
        assertEquals(new BigDouble(2), x.ceil());

        x = new BigDouble(1.5);
        assertEquals(new BigDouble(2), x.ceil());

        x = BigDouble.ONE;
        assertEquals(x, x.ceil());
    }

    @Test
    void truncate() {
        BigDouble x = new BigDouble(0.9);
        assertEquals(BigDouble.ZERO, x.truncate());

        x = new BigDouble(1.1);
        assertEquals(BigDouble.ONE, x.truncate());

        x = new BigDouble(1.5);
        assertEquals(BigDouble.ONE, x.truncate());

        x = BigDouble.ONE;
        assertEquals(x, x.truncate());


        x = new BigDouble(-0.9);
        assertEquals(BigDouble.ZERO, x.truncate());

        x = new BigDouble(-1.1);
        assertEquals(BigDouble.ONE.neg(), x.truncate());

        x = new BigDouble(-1.5);
        assertEquals(BigDouble.ONE.neg(), x.truncate());

        x = BigDouble.ONE.neg();
        assertEquals(x, x.truncate());
    }

    @Test
    void add() {
        BigDouble x;
        BigDouble y;

        x = new BigDouble(1.2);
        y = new BigDouble(2.3);

        assertEquals(new BigDouble(3.5), x.add(y));

        y = y.neg();
        assertEquals(new BigDouble(-1.1), x.add(y));
    }

    @Test
    void sub() {
        BigDouble x;
        BigDouble y;

        x = new BigDouble(1.2);
        y = new BigDouble(2.3);

        assertEquals(new BigDouble(-1.1), x.sub(y));

        y = y.neg();
        assertEquals(new BigDouble(3.5), x.sub(y));
    }

    @Test
    void mul() {
        BigDouble x;
        BigDouble y;

        x = new BigDouble(1.2);
        y = new BigDouble(2.3);

        assertEquals(new BigDouble(2.76), x.mul(y));

        y = y.neg();
        assertEquals(new BigDouble(-2.76), x.mul(y));
    }

    @Test
    void div() {
        BigDouble x;
        BigDouble y;

        x = new BigDouble(1.2);
        y = new BigDouble(2.3);

        assertEquals(new BigDouble(0.5217391304347826), x.div(y));

        y = y.neg();
        assertEquals(new BigDouble(-0.5217391304347826), x.div(y));

        // Not sure if this is accurate.
        assertEquals(BigDouble.POSITIVE_INFINITY, x.div(0));
    }

    @Test
    void recip() {
        BigDouble x;
        BigDouble y;
        for (int i = 0; i < 1000; i++) {
            x = randomValue();
            y = x.recip();
            // God damn you, floating point errors.
            assertTrue(BigDouble.ONE.equals_tolerance(x.mul(y), new BigDouble(1e-12)));
        }
    }

    @Test
    void testEquals() {
        BigDouble x;
        for (int i = 0; i < 100; i++) {
            x = randomValue();
            assertTrue(x.equals(x));
            assertFalse(x.equals(x.mul(1.00000000000001)));
            assertFalse(x.equals(x.mul(0.99999999999999)));
        }
    }

    @Test
    void notEquals() {
        BigDouble x;
        for (int i = 0; i < 100; i++) {
            x = randomValue();
            assertFalse(x.neq(x));
            assertTrue(x.neq(x.mul(1.00000000000001)));
            assertTrue(x.neq(x.mul(0.99999999999999)));
        }
    }

    @Test
    void lessThan() {
        BigDouble x;
        for (int i = 0; i < 100; i++) {
            x = randomValue();
            assertFalse(x.lt(x));
            assertTrue(x.lt(x.mul(1.00000000000001)));
            assertFalse(x.lt(x.mul(0.99999999999999)));
        }
    }

    @Test
    void lessThanOrEqualTo() {
        BigDouble x;
        for (int i = 0; i < 100; i++) {
            x = randomValue();
            assertTrue(x.lte(x));
            assertTrue(x.lte(x.mul(1.00000000000001)));
            assertFalse(x.lte(x.mul(0.99999999999999)));
        }
    }

    @Test
    void greaterThan() {
        BigDouble x;
        for (int i = 0; i < 100; i++) {
            x = randomValue();
            assertFalse(x.gt(x));
            assertFalse(x.gt(x.mul(1.00000000000001)));
            assertTrue(x.gt(x.mul(0.99999999999999)));
        }
    }

    @Test
    void greaterThanOrEqualTo() {
        BigDouble x;
        for (int i = 0; i < 100; i++) {
            x = randomValue();
            assertTrue(x.gte(x));
            assertFalse(x.gte(x.mul(1.00000000000001)));
            assertTrue(x.gte(x.mul(0.99999999999999)));
        }
    }

    @Test
    void max() {
        BigDouble x, y;
        for (int i = 0; i < 100; i++) {
            x = randomValue();
            y = randomValue();
            if (x.gte(y)) {
                assertEquals(x, x.max(y));
                assertEquals(x, y.max(x));
            } else {
                assertEquals(y, x.max(y));
                assertEquals(y, y.max(x));
            }
        }
    }

    @Test
    void min() {
        BigDouble x, y;
        for (int i = 0; i < 100; i++) {
            x = randomValue();
            y = randomValue();
            if (x.lte(y)) {
                assertEquals(x, x.min(y));
                assertEquals(x, y.min(x));
            } else {
                assertEquals(y, x.min(y));
                assertEquals(y, y.min(x));
            }
        }
    }

    @Test
    void clamp() {
        // TODO
    }

    @Test
    void log10() {
        BigDouble x;
        for (int i = 0; i < 100; i++) {
            x = randomValue();
            assertEquals(x.e() + Math.log10(x.m()), x.log10());
        }

        assertEquals(Double.NEGATIVE_INFINITY, BigDouble.ZERO.log10());
        assertEquals(Double.NaN, BigDouble.ONE.neg().log10());
    }

    @Test
    void absLog10() {
        BigDouble x;
        for (int i = 0; i < 100; i++) {
            x = randomValue();
            assertEquals(x.e() + Math.log10(x.m()), x.absLog10());
        }

        for (int i = 0; i < 100; i++) {
            x = randomValue().neg();
            assertEquals(x.e() + Math.log10(x.abs().m()), x.absLog10());
        }

        assertEquals(Double.NEGATIVE_INFINITY, BigDouble.ZERO.absLog10());
    }

    @Test
    void pLog10() {
        BigDouble x;
        for (int i = 0; i < 100; i++) {
            x = randomValue();
            if (x.lt(1)) continue;

            assertEquals(x.e() + Math.log10(x.m()), x.pLog10());
        }

        for (int i = 0; i < 100; i++) {
            x = randomValue().neg();
            assertEquals(0, x.pLog10());
        }

        assertEquals(0.0, BigDouble.ZERO.pLog10());
    }

    @Test
    void log() {
        // TODO;
    }

    @Test
    void log2() {
        // TODO
    }

    @Test
    void ln() {
        // TODO
    }

    @Test
    void pow10() {
        BigDouble x;
        long exponent;
        for (int i = 0; i < 100; i++) {
            exponent = randomExponent();
            x = BigDouble.pow10(exponent);
            assertEquals(1, x.getMantissa());
            assertEquals(exponent, x.getExponent());
        }

        double dExp;
        double mantissa;
        for (int i = 0; i < 100; i++) {
            exponent = Generator.nextLong(1, 5000);
            mantissa = randomMantissa();
            dExp = exponent + Math.log10(mantissa);
            x = BigDouble.pow10(dExp);
            // Precision was never guaranteed.
            assertTrue(
                    x.getMantissa() - mantissa < 0.00000001,
                    String.format("10 ^ %s should have mantissa %s", dExp, mantissa)
            );
            assertEquals(exponent, x.getExponent());

        }
    }

    @Test
    void pow() {
        // Basic Pow operations.
        BigDouble TWO = new BigDouble(2);
        BigDouble tolerance = new BigDouble(1e-10);
        assertEquals(BigDouble.ONE, TWO.pow(0));
        assertEquals(TWO, TWO.pow(1));
        assertEquals(new BigDouble(4), TWO.pow(2));
        assertEquals(new BigDouble(1125899906842624L), TWO.pow(50));
        // Only accurate to like 8 decimal places.
        assertTrue(new BigDouble(1448.1546878700492).equals_tolerance(TWO.pow(10.5), tolerance));

        BigDouble N_TWO = TWO.neg();
        assertEquals(BigDouble.ONE, N_TWO.pow(0));
        assertTrue(BigDouble.isNaN(N_TWO.pow(1.5)));
    }

    @Test
    void exp() {
        // If pow is good, this one should be good too.
        // Feel free to yell at me later.
        BigDouble E = new BigDouble(Math.E);
        assertEquals(BigDouble.ONE, BigDouble.ZERO.exp());
        assertEquals(E, BigDouble.ONE.exp());
        assertEquals(new BigDouble(Math.exp(10)), BigDouble.exp(10));

        assertEquals(new BigDouble("1.645106699105499e+53616602"), new BigDouble(123456789).exp());

        int expPower;
        BigDouble result;
        BigDouble tolerance = new BigDouble(1e-8);
        for (int i = 0; i < 100; i++) {
            expPower = Generator.nextInt(1048576);
            // e^x / e^(x-1) = e
            result =  new BigDouble(expPower).exp().div(new BigDouble(expPower - 1).exp());
            assertTrue(
                    E.eq_tolerance(result, tolerance),
                    String.format("Expected value close to <%s> but was <%s>", E, result)
            );
        }
    }

    @Test
    void sqr() {
        assertEquals(BigDouble.ONE, BigDouble.ONE.sqr());
        assertEquals(BigDouble.ZERO, BigDouble.ZERO.sqr());

        BigDouble x;
        for (int i = 0; i < 100; i++) {
            x = randomValue();
            assertEquals(x.sqr(), x.neg().sqr());
        }

        assertEquals(new BigDouble("7.4649600000000005e+1975308643"), new BigDouble("8.64e987654321").sqr());
    }

    @Test
    void sqrt() {
        assertEquals(BigDouble.ONE, BigDouble.ONE.sqrt());
        assertEquals(BigDouble.ZERO, BigDouble.ZERO.sqrt());

        // Negative numbers have no sqrt.
        BigDouble x;
        for (int i = 0; i < 100; i++) {
            x = randomValue().neg();
            assertTrue(BigDouble.isNaN(x.sqrt()));
        }

        assertEquals(new BigDouble("2.0590774633315765e+448767117"), new BigDouble("4.2398e897534234").sqrt());
    }

    @Test
    void cube() {
        assertEquals(BigDouble.ONE, BigDouble.ONE.cube());
        assertEquals(BigDouble.ZERO, BigDouble.ZERO.cube());

        BigDouble x;
        for (int i = 0; i < 100; i++) {
            x = randomValue();
            assertEquals(x.cube().neg(), x.neg().cube());
        }

        assertEquals(new BigDouble("6.449725440000001e+2962962965"), new BigDouble("8.64e987654321").cube());
    }

    @Test
    void cbrt() {
        assertEquals(BigDouble.ONE, BigDouble.ONE.cbrt());
        assertEquals(BigDouble.ZERO, BigDouble.ZERO.cbrt());

        // Cbrt preserves sign.
        BigDouble x;
        for (int i = 0; i < 100; i++) {
            x = randomValue();
            assertEquals(x.cbrt().neg(), x.neg().cbrt());
        }

        assertEquals(new BigDouble("1.6185090178580044e+299178078"), new BigDouble("4.2398e897534234").cbrt());
    }

    @Test
    void toDouble() {
        assertEquals(0, BigDouble.ZERO.toDouble());
        assertEquals(-0, BigDouble.ZERO.toDouble());

        double x;
        BigDouble b;
        for (int i = 0; i < 100; i++) {
            x = Generator.nextDouble() * Math.pow(10, Generator.nextInt(-250, 250));
            b = new BigDouble(x);
            assertEquals(x, b.toDouble(), 1e-15 * x);
        }
    }
}