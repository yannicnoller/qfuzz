package edu.cmu.sv.kelinci.quantification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Greedy implements PartitionAlgorithm {

	private boolean optimized;

	public Greedy(boolean optimized) {
		this.optimized = optimized;
	}

	@Override
	public PartitionSet calculateClusters(double epsilon, long[] observations) {

		/* Sort observations. */
		Arrays.sort(observations);

		/* Remove duplicates. */
		int purifiedPointer = 0;
		for (int i = 1; i < observations.length; i++) {
			if (observations[purifiedPointer] != observations[i]) {
				purifiedPointer++;
				observations[purifiedPointer] = observations[i];
			}
		}
		observations = Arrays.copyOf(observations, purifiedPointer + 1);

		/* Initial greedy clustering. */
		List<Partition> clusterList = new ArrayList<>();
		Partition currentCluster = new Partition();
		for (long value : observations) {
			if (currentCluster.canTake(value, epsilon)) {
				currentCluster.addValue(value);
			} else {
				clusterList.add(currentCluster);

				currentCluster = new Partition();
				currentCluster.addValue(value);
			}
		}
		clusterList.add(currentCluster);

		Partition[] clusters;
		if (optimized) {
			/* Make another pass to split 2-pairs to 3-pairs if possible */
			List<Partition> updatedClusterList = new ArrayList<>();
			for (int i = clusterList.size() - 1; i > 0; i--) {

				Partition c2 = clusterList.get(i);
				Partition c1 = clusterList.get(i - 1);

				/* Skip if no splitting is possible. */
				if (c1.size() <= 1 || c2.size() <= 1) {
					updatedClusterList.add(0, c2);
					continue;
				}

				/*
				 * Check whether we can squeeze in another cluster between the two current
				 * clusters. Take the Max from c1 and the Min from c2. Check if both cannot be
				 * added to the others cluster. If yes create a new cluster c_new with these two
				 * elements that fits between c1 and c2.
				 *
				 * Please note that if we create c_new we know the following two facts.
				 *
				 * (1) c1 is still valid: it cannot be joined with c_new because the max in
				 * c_new (earlier Min from c2) cannot be joined with c1 AND c1 cannot be joined
				 * with c0 because we followed the greedy algorithm and this depends on the min
				 * in c1 and we have not touched this value.
				 *
				 * (2) c2 might be still valid: it cannot be joined with c_new because the min
				 * in c_new (earlier Max from c1) cannot be joined with c2. However, we do not
				 * know whether c2 without its min must not be joined with a potential c3.
				 * Therefore, we need here an additional check (see below).
				 *
				 */
				if (!c1.canTake(c2.getMin(), epsilon) && !c2.canTake(c1.getMax(), epsilon)
						&& Math.abs(c2.getMin() - c1.getMax()) <= epsilon) {

					/*
					 * I know tht c1 min alone cannot be part of c0 because this is why we generated
					 * a new cluster in the first palce
					 */

					/*
					 * Additional Check whether c2 is still valid, needs no join with c3 (if
					 * existent).
					 */
					if (i + 1 < clusterList.size() - 1) {
						Partition c3 = clusterList.get(i + 1);

						/*
						 * c2.getValues().get(1) would be the next smallest element if min is removed.
						 */
						if (Math.abs(c3.getMax() - c2.getValues().get(1)) <= epsilon) {
							/*
							 * Note it could be still okay to follow the splitting, but we would have to
							 * check more cluster 3++. But we stop here to be still efficient.
							 */
							updatedClusterList.add(0, c2);
							continue;
						}
					}

					Partition newCluster = new Partition();
					newCluster.addValue(c1.getMax());
					newCluster.addValue(c2.getMin());

					c1.removeMax();
					c2.removeMin();

					updatedClusterList.add(0, c2);
					updatedClusterList.add(0, newCluster);
				} else {
					updatedClusterList.add(0, c2);
				}

			}
			updatedClusterList.add(0, clusterList.get(0));

			clusters = updatedClusterList.toArray(new Partition[updatedClusterList.size()]);

		} else {
			clusters = clusterList.toArray(new Partition[clusterList.size()]);
		}

		/* Calculate the min, max and mean values for each cluster. */
		double[] averageValues = new double[clusters.length];
		double[] min_vals = new double[clusters.length];
		double[] max_vals = new double[clusters.length];
		for (int i = 0; i < clusters.length; i++) {
			averageValues[i] = 0.0;
			double min_val = Double.MAX_VALUE;
			double max_val = 0.0;
			for (double currentVal : clusters[i].getValues()) {
				averageValues[i] += currentVal;
				min_val = Math.min(min_val, currentVal);
				max_val = Math.max(max_val, currentVal);
			}
			min_vals[i] = min_val;
			max_vals[i] = max_val;
			averageValues[i] /= clusters[i].size();
		}

		/*
		 * Calculate the minimum difference (delta) between min/max values of all
		 * clusters.
		 */
		double minDelta;
		if (clusters.length <= 1) {
			minDelta = 0;
		} else {
			double min_overall = Double.MAX_VALUE;
			for (int i = 0; i < min_vals.length; i++) {
				for (int j = 0; j < max_vals.length; j++) {
					if (i == j)
						continue;
					min_overall = Math.min(Math.abs(min_vals[i] - max_vals[j]), min_overall);
				}
			}
			minDelta = min_overall;
		}

		PartitionSet cs = new PartitionSet(clusters, epsilon, averageValues, minDelta);
		return cs;
	}

	public static void main(String[] args) {

		List<Long> timeNeededGreedy = new ArrayList<>();
		List<Long> timeNeededKDynamic = new ArrayList<>();

		PartitionSet clusterSet;

		boolean optimizedGreedy = false;

		double epsilon = 1.0;
		System.out.println("epsilon=" + epsilon);

		int N = 10000;
		System.out.println("N=" + N);

		long[] observations1 = { 83, 81, 82, 81, 85, 85, 86, 86, 85, 86, 85, 85, 85, 85, 85, 85, 85, 85, 85, 85, 85, 85,
				86, 85, 85, 85, 86, 85, 85, 80, 82, 81, 81, 81, 81, 81, 89, 85, 85, 85, 85, 87, 87, 85, 85, 85, 85, 85,
				85, 85, 85, 85, 85, 85, 85, 85, 85, 77, 79, 81, 88, 85, 84, 81, 81, 81, 82, 81, 81, 82, 81, 81, 82, 88,
				88, 89, 89, 87, 85, 86, 85, 86, 85, 85, 85, 85, 86, 91, 86, 85, 85, 85, 86, 85, 85, 85, 76, 85, 87,
				89 };

		System.out.println(Arrays.toString(observations1));

		long startTime;
		for (int i = 0; i < N; i++) {

			startTime = System.currentTimeMillis();
			clusterSet = new Greedy(optimizedGreedy).calculateClusters(epsilon, observations1);
			timeNeededGreedy.add(System.currentTimeMillis() - startTime);

			startTime = System.currentTimeMillis();
			clusterSet = new KDynamic().calculateClusters(epsilon, observations1);
			timeNeededKDynamic.add(System.currentTimeMillis() - startTime);

		}

		double avgTimeGreedy = 0.0;
		for (long time : timeNeededGreedy) {
			avgTimeGreedy += time;
		}
		avgTimeGreedy = avgTimeGreedy / N;
		clusterSet = new Greedy(optimizedGreedy).calculateClusters(epsilon, observations1);
		System.out.println("greedy: " + clusterSet.getNumClusters() + ", " + clusterSet + ", " + avgTimeGreedy + "ms");

		double avgTimeKDynamic = 0.0;
		for (long time : timeNeededKDynamic) {
			avgTimeKDynamic += time;
		}
		avgTimeKDynamic = avgTimeKDynamic / N;
		clusterSet = new KDynamic().calculateClusters(epsilon, observations1);
		System.out.println(
				"kDynamic: " + clusterSet.getNumClusters() + ", " + clusterSet + ", " + avgTimeKDynamic + "ms");

		/////////////////////////////////////////////////

		System.out.println();
		long[] observations2 = { 64, 23, 17, 17, 17, 64, 17, 17, 17, 17, 17, 17, 17, 17, 17, 64, 17, 17, 17, 17, 17, 17,
				17, 17, 17, 17, 47, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 29, 17, 17, 17, 17, 17, 17,
				17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 20, 17, 17,
				17, 17, 17, 59, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17,
				17 };

		System.out.println(Arrays.toString(observations2));

		for (int i = 0; i < N; i++) {

			startTime = System.currentTimeMillis();
			clusterSet = new Greedy(optimizedGreedy).calculateClusters(epsilon, observations2);
			timeNeededGreedy.add(System.currentTimeMillis() - startTime);

			startTime = System.currentTimeMillis();
			clusterSet = new KDynamic().calculateClusters(epsilon, observations2);
			timeNeededKDynamic.add(System.currentTimeMillis() - startTime);

		}

		avgTimeGreedy = 0.0;
		for (long time : timeNeededGreedy) {
			avgTimeGreedy += time;
		}
		avgTimeGreedy = avgTimeGreedy / N;
		clusterSet = new Greedy(optimizedGreedy).calculateClusters(epsilon, observations2);
		System.out.println("greedy: " + clusterSet.getNumClusters() + ", " + clusterSet + ", " + avgTimeGreedy + "ms");

		avgTimeKDynamic = 0.0;
		for (long time : timeNeededKDynamic) {
			avgTimeKDynamic += time;
		}
		avgTimeKDynamic = avgTimeKDynamic / N;
		clusterSet = new KDynamic().calculateClusters(epsilon, observations2);
		System.out.println(
				"kDynamic: " + clusterSet.getNumClusters() + ", " + clusterSet + ", " + avgTimeKDynamic + "ms");

		/////////////////////////////////////////////////

		System.out.println();
		long[] observations3 = { 64, 64, 64, 29, 64, 20, 64, 20, 38, 64, 64, 64, 64, 50, 44, 64, 38, 50, 17, 38, 35, 64,
				38, 64, 64, 64, 53, 32, 17, 62, 17, 47, 64, 64, 47, 64, 32, 64, 64, 20, 64, 38, 44, 64, 20, 64, 64, 29,
				64, 64, 47, 64, 64, 20, 29, 64, 17, 64, 64, 64, 64, 59, 64, 64, 23, 64, 64, 64, 50, 64, 64, 29, 26, 64,
				41, 44, 38, 64, 29, 56, 64, 64, 20, 64, 64, 59, 29, 64, 64, 64, 62, 17, 64, 29, 29, 64, 41, 64, 64,
				64 };

		System.out.println(Arrays.toString(observations3));

		for (int i = 0; i < N; i++) {

			startTime = System.currentTimeMillis();
			clusterSet = new Greedy(optimizedGreedy).calculateClusters(epsilon, observations3);
			timeNeededGreedy.add(System.currentTimeMillis() - startTime);

			startTime = System.currentTimeMillis();
			clusterSet = new KDynamic().calculateClusters(epsilon, observations3);
			timeNeededKDynamic.add(System.currentTimeMillis() - startTime);

		}

		avgTimeGreedy = 0.0;
		for (long time : timeNeededGreedy) {
			avgTimeGreedy += time;
		}
		avgTimeGreedy = avgTimeGreedy / N;
		clusterSet = new Greedy(optimizedGreedy).calculateClusters(epsilon, observations3);
		System.out.println("greedy: " + clusterSet.getNumClusters() + ", " + clusterSet + ", " + avgTimeGreedy + "ms");

		avgTimeKDynamic = 0.0;
		for (long time : timeNeededKDynamic) {
			avgTimeKDynamic += time;
		}
		avgTimeKDynamic = avgTimeKDynamic / N;
		clusterSet = new KDynamic().calculateClusters(epsilon, observations3);
		System.out.println(
				"kDynamic: " + clusterSet.getNumClusters() + ", " + clusterSet + ", " + avgTimeKDynamic + "ms");

		/////////////////////////////////////////////////

		System.out.println();
		long[] observations4 = { 3, 76, 77, 78, 79, 80, 80, 81, 82, 83, 84, 85, 86, 87, 88, 90, 91 };
		System.out.println(Arrays.toString(observations4));

		epsilon = 1.0;
		System.out.println("epsilon=" + epsilon);

		for (int i = 0; i < N; i++) {

			startTime = System.currentTimeMillis();
			clusterSet = new Greedy(optimizedGreedy).calculateClusters(epsilon, observations4);
			timeNeededGreedy.add(System.currentTimeMillis() - startTime);

			startTime = System.currentTimeMillis();
			clusterSet = new KDynamic().calculateClusters(epsilon, observations4);
			timeNeededKDynamic.add(System.currentTimeMillis() - startTime);

		}

		avgTimeGreedy = 0.0;
		for (long time : timeNeededGreedy) {
			avgTimeGreedy += time;
		}
		avgTimeGreedy = avgTimeGreedy / N;
		clusterSet = new Greedy(optimizedGreedy).calculateClusters(epsilon, observations4);
		System.out.println("greedy: " + clusterSet.getNumClusters() + ", " + clusterSet + ", " + avgTimeGreedy + "ms");

		avgTimeKDynamic = 0.0;
		for (long time : timeNeededKDynamic) {
			avgTimeKDynamic += time;
		}
		avgTimeKDynamic = avgTimeKDynamic / N;
		clusterSet = new KDynamic().calculateClusters(epsilon, observations4);
		System.out.println(
				"kDynamic: " + clusterSet.getNumClusters() + ", " + clusterSet + ", " + avgTimeKDynamic + "ms");

	}

}
