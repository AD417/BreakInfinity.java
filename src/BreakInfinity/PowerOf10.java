package BreakInfinity;

/**
 * We need this lookup table because Math.pow(10, exponent)
 * when exponent's absolute value is large is slightly inaccurate.
 * You can fix it with the power of math... or just make a lookup table.
 * Faster AND simpler!
 */
class PowerOf10 {

    /**
     * Instantiate all the powers of 10. Only to be run at the beginning of the program.
     * @return an array containing every representable power of 10, from 1e-324 to 1e308.
     */
    private static double[] cache() {
        double[] out = new double[Constants.DOUBLE_EXP_MAX - Constants.DOUBLE_EXP_MIN];
        for (int i = Constants.DOUBLE_EXP_MIN + 1; i <= Constants.DOUBLE_EXP_MAX; i++) {
            out[i - Constants.DOUBLE_EXP_MIN - 1] = Double.parseDouble("1e" + i);
        }
        return out;
    }
    private static final double[] powersOf10 = cache();

    private static final int indexOf0InPowersOf10 = 323;

    public static double lookup(int power) {
        return powersOf10[power + indexOf0InPowersOf10];
    }

    public static void main(String[] args) {
        System.out.println(lookup(308));
        System.out.println(lookup(-323));
        System.out.println(lookup(0));
    }
}
