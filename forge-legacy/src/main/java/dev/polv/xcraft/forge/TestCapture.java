package dev.polv.xcraft.forge;

/**
 * Collects what {@code /xcraft test} produces: every dropped item entity that joins the world while a scenario
 * runs (after xCraft multiplied it), summed per kind, and formats the result line the CI server test reads.
 */
public final class TestCapture {
	public static final int BEEF = 0, LEATHER = 1, OTHER = 2;
	private static int[] sums;

	private TestCapture() {
	}

	public static void begin() {
		sums = new int[3];
	}

	public static boolean active() {
		return sums != null;
	}

	public static void record(int kind, int count) {
		if (sums != null) sums[kind] += count;
	}

	public static int[] end() {
		int[] out = sums == null ? new int[3] : sums;
		sums = null;
		return out;
	}

	/** {@code xcraft-test furnace=<smelted> beef=<n> leather=<n> ore=<n> xp=<points> (orb <points>)} */
	public static String format(int smelted, int[] mob, int[] ore, int xp, int orb) {
		return "xcraft-test furnace=" + smelted + " beef=" + count(mob[BEEF]) + " leather=" + count(mob[LEATHER])
			+ " ore=" + count(ore[BEEF] + ore[LEATHER] + ore[OTHER]) + " xp=" + xp + " (orb " + orb + ")";
	}

	private static String count(int n) {
		return n == 0 ? "none" : String.valueOf(n);
	}
}
