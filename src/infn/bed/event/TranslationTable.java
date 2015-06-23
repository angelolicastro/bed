package infn.bed.event;

public interface TranslationTable {

	public static final int[][] bars = { { 0, 0, 0, 0 }, { 1, 0, 0, 0 },
			{ 2, 0, 0, 1 }, { 3, 0, 0, 1 }, { 4, 0, 0, 2 }, { 5, 0, 0, 2 },
			{ 6, 0, 1, 0 }, { 7, 0, 1, 0 }, { 8, 0, 1, 1 }, { 9, 0, 1, 1 },
			{ 10, 0, 1, 2 }, { 11, 0, 1, 2 }, { 12, 0, 2, 0 }, { 13, 0, 2, 0 },
			{ 14, 0, 2, 1 }, { 15, 0, 2, 1 }, { 16, 0, 2, 2 }, { 17, 0, 2, 2 } };

	public static final int[] vetoInner1 = { 18, 0, 0, 0 };

	public static final int[] vetoInner2 = { 19, 0, 0, 1 };

	public static final int[] vetoInner3 = { 20, 0, 0, 2 };

	public static final int[] vetoInner4 = { 21, 0, 0, 3 };

	public static final int[] vetoInner5 = { 22, 0, 0, 4 };

	public static final int[] vetoInner6 = { 23, 0, 0, 5 };

	public static final int[] vetoOuter1 = { 24, 0, 1, 0 };

	public static final int[] vetoOuter2 = { 25, 0, 1, 1 };

	public static final int[] vetoOuter3 = { 26, 0, 1, 2 };

	public static final int[] vetoOuter4 = { 27, 0, 1, 3 };

	public static final int[] vetoOuter5L = { 28, 0, 1, 4 };

	public static final int[] vetoOuter5R = { 29, 0, 1, 4 };

	public static final int[] vetoOuter6L = { 30, 0, 1, 5 };

	public static final int[] vetoOuter6R = { 31, 0, 1, 5 };

	public static final int[] vetoOuter7L = { 32, 0, 1, 6 };

	public static final int[] vetoOuter7R = { 33, 0, 1, 6 };

	public static final int[] vetoOuter8L = { 34, 0, 1, 7 };

	public static final int[] vetoOuter8R = { 35, 0, 1, 7 };

}
