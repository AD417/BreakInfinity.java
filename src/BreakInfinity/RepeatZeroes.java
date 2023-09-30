package BreakInfinity;

import java.util.HashMap;

public class RepeatZeroes {
    private static final HashMap<Integer, String> cache = new HashMap<Integer, String>();

    public static String repeatZeroes(int count) {
        if (count <= 0) return "";

        if (!cache.containsKey(count)) {
            cache.put(count, "0".repeat(count));
        }

        return cache.get(count);
    }

    public static String trailZeroes(int places) {
        return places > 0 ? "." + repeatZeroes(places) : "";
    }
}
