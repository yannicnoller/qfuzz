package edu.cmu.sv.kelinci.quantification;

/**
 * Stores the index of two observations, which cannot be merged in the same cluster.
 */
public class Pair {
	int index_1;
	int index_2;

	public Pair(int i1, int i2) {
		index_1 = i1;
		index_2 = i2;
	}
}
