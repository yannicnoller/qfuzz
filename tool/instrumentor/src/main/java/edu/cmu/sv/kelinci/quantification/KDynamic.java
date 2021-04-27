package edu.cmu.sv.kelinci.quantification;

// Java program to find minimum cost
// k partitions of array.
// https://www.geeksforgeeks.org/clusteringpartitioning-an-array-such-that-sum-of-square-differences-is-minimum/
// import java.io.*;
// import java.util.ArrayList;
import java.util.*;

public class KDynamic extends PartitionAlgorithmByIndex {
	static long inf = 1000000000;
	
	// Returns minimum cost of partitioning
	// a[] in k clusters.
	@Override
	public ArrayList<Integer>[] calClusters(long[] a, int k) {
		// Create a dp[][] table and initialize
		// all values as infinite. dp[i][j] is
		// going to store optimal partition cost
		// for arr[0..i-1] and j partitions
		// Set<Long> targetA = new HashSet<Long>();
		// for(int i = 0; i < n; i++)
		// {
		// targetA.add(a1[i]);
		// }
		// n = targetA.size();
		// long[] a = new long[n];
		// int z = 0;
		// for(long x: targetA)
		// {
		// a[z] = x;
		// z = z + 1;
		// }
		
		int n = a.length;

		long dp[][] = new long[n + 1][k + 1];
		int[][] cluster_ind = new int[n + 1][k + 1];
		for (int i = 0; i <= n; i++)
			for (int j = 0; j <= k; j++)
				dp[i][j] = inf;

		// Fill dp[][] in bottom up manner
		dp[0][0] = 0;

		// Current ending position (After i-th
		// iteration result for a[0..i-1] is computed.
		ArrayList<Integer>[] res = new ArrayList[k];
		for (int j = 0; j < k; j++) {
			res[j] = new ArrayList<Integer>();
		}

		for (int i = 1; i <= n; i++)

			// j is number of partitions
			// int best_j = 1;
			for (int j = 1; j <= k; j++) {
				// Picking previous partition for
				// current i.
				int best_m = -1;
				for (int m = i - 1; m >= 0; m--) {
					if (dp[i][j] > dp[m][j - 1] + (a[i - 1] - a[m]) * (a[i - 1] - a[m])) {
						dp[i][j] = dp[m][j - 1] + (a[i - 1] - a[m]) * (a[i - 1] - a[m]);
						best_m = m;
					}
				}
				cluster_ind[i][j] = best_m;
			}

		int cur_k = 0;
		int last_ind = -1;
		int clust_ind = k;
		for (int i = n; i > 0;) {
			res[cur_k].add(i - 1);
			int next_ind = cluster_ind[i][clust_ind];
			int j = i - 1;
			clust_ind = clust_ind - 1;
			while (j > next_ind) {
				res[cur_k].add(j - 1);
				j = j - 1;
			}
			i = j;
			cur_k += 1;
		}
		// displayOutput_2(res,a);
		// displayOutput(dp);
		// displayOutput_3(cluster_ind);
		return res;
	}

	static void displayOutput(long[][] clusterList) {
		for (int i = 0; i < clusterList.length; i++) {
			for (int j = 0; j < clusterList[0].length; j++) {
				System.out.print(clusterList[i][j]);
				System.out.print(",");
			}
			System.out.println();
		}
	}

	static void displayOutput_2(ArrayList<Integer>[] clusterList, long[] points) {
		for (int i = 0; i < clusterList.length; i++) {
			String clusterOutput = "\n\n[";
			for (int index : clusterList[i])
				clusterOutput += "(" + points[index] + "), ";
			System.out.println(clusterOutput.substring(0, clusterOutput.length() - 2) + "]");
		}
	}

	static void displayOutput_3(int[][] clusterList) {
		for (int i = 0; i < clusterList.length; i++) {
			for (int j = 0; j < clusterList[0].length; j++) {
				System.out.print(clusterList[i][j]);
				System.out.print(",");
			}
			System.out.println();
		}
	}
	
	// Driver code
	public static void main(String[] args) {
		int k = 6;
		// long a1[] = {76, 76, 77, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 91};
		// long a1[] = {1, 2, 3, 4, 5, 6, 7, 8, 9};
		// long a1[] = {1, 3, 4, 6, 7, 9, 10, 11, 12};
		long a1[] = { 83, 81, 82, 81, 85, 85, 86, 86, 85, 86, 85, 85, 85, 85, 85, 85, 85, 85, 85, 85, 85, 85, 86, 85,
				85, 85, 86, 85, 85, 80, 82, 81, 81, 81, 81, 81, 89, 85, 85, 85, 85, 87, 87, 85, 85, 85, 85, 85, 85, 85,
				85, 85, 85, 85, 85, 85, 85, 77, 79, 81, 88, 85, 84, 81, 81, 81, 82, 81, 81, 82, 81, 81, 82, 88, 88, 89,
				89, 87, 85, 86, 85, 86, 85, 85, 85, 85, 86, 91, 86, 85, 85, 85, 86, 85, 85, 85, 76, 85, 87, 89 };
		// Arrays.sort(a1);
		KDynamic KClust = new KDynamic();
		KClust.calClusters(a1, k);
	}


}

// This code is contributed by vt_m.
