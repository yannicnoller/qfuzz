package edu.cmu.sv.kelinci.quantification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class PartitionAlgorithmByIndex implements PartitionAlgorithm {
	
	public abstract ArrayList<Integer>[] calClusters(long[] observations, int clusters);

	@Override
	public PartitionSet calculateClusters(double epsilon, long[] observations_orig) {
		
		Set<Long> targetA = new HashSet<Long>();
		for(int i = 0; i < observations_orig.length; i++)
		{
			targetA.add(observations_orig[i]);
		}
		int new_size = targetA.size();
		long[] observations = new long[new_size];
		int z = 0;
		for(long x: targetA)
		{
				observations[z] = x;
				z = z + 1;
		}
		Arrays.sort(observations);

		/* Identify pairs, which cannot be merged. */
		List<Pair> cantMerge = new ArrayList<>();
		for (int i = 0; i < observations.length; i++) {
			for (int j = i + 1; j < observations.length; j++) {
				if (Math.abs(observations[i] - observations[j]) > epsilon) {
					Pair notMergeable = new Pair(i, j);
					cantMerge.add(notMergeable);
				}
			}
		}

		/* Determine clusters. */
		int k = 1;
		ArrayList<Integer>[] res = calClusters(observations, k);

		while (k <= observations.length) {
			// int step = 1;
			// while (KmeanClust.cur_iteration == maxIterations && step >= 1000) {
			// 	KmeanClust = new KMeans();
			// 	res = KmeanClust.calClusters(observations, k, maxIterations);
			// 	step++;
			// }
			boolean isViolated = false;
			for (int i = 0; i < res.length; i++) {
				for (Pair P : cantMerge) {
					int p1 = P.index_1;
					int p2 = P.index_2;
					int counts = 0;
					for (int index : res[i]) {
						if (index == p1) {
							counts += 1;
						}
						if (index == p2) {
							counts += 1;
						}
					}
					if (counts == 2) {
						isViolated = true;
						break;
					}
				}
				if (isViolated)
					break;
			}
			if (!isViolated)
				break;
			k++;
			res = calClusters(observations, k);
		}

		/* Calculate the min, max and mean values for each cluster. */
		double[] values = new double[res.length];
		double[] min_vals = new double[res.length];
		double[] max_vals = new double[res.length];
		for (int i = 0; i < res.length; i++) {
			values[i] = 0.0;
			double min_val = Double.MAX_VALUE;
			double max_val = 0.0;
			for (int index : res[i]) {
				double currentVal = observations[index];
				values[i] += currentVal;
				min_val = Math.min(min_val, currentVal);
				max_val = Math.max(max_val, currentVal);
			}
			min_vals[i] = min_val;
			max_vals[i] = max_val;
			values[i] /= res[i].size();
		}

		/* Calculate the minimum difference (delta) between min/max values of all clusters. */
		double minDelta;
		if (k <= 1) {
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
		
		/* Construct ClusterSet object. */
		Partition[] clusters = new Partition[k];
		for (int i=0; i<k; i++) {
			Partition c = new Partition();
			for (Integer index : res[i]) {
				c.addValue(observations[index]);
			}
			clusters[i] = c;
		}
		return new PartitionSet(clusters, epsilon, values, minDelta);
	}

}
