package edu.cmu.sv.kelinci.quantification;

public class PartitionSet {

	// minimum difference between clusters, used to identify clusters
	private double epsilon;
	private Partition[] clusters;
	private double[] clusterAverageValues;
	private double minDelta;

	PartitionSet(Partition[] clusters, double epsilon, double[] clusterAverageValues, double minDelta) {
		this.epsilon = epsilon;
		this.clusters = clusters;
		this.clusterAverageValues = clusterAverageValues;
		this.minDelta = minDelta;
	}

	public double[] getClusterAverageValues() {
		return clusterAverageValues;
	}

	public double getEpsilon() {
		return epsilon;
	}

	public double getMinimumDeltaValue() {
		return minDelta;
	}

	public int getNumClusters() {
		return this.clusters.length;
	}

	public Partition[] getClusters() {
		return this.clusters;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (int i=0; i<this.clusters.length; i++) {
			sb.append(this.clusters[i]);
			if (i<this.clusters.length-1) {
				sb.append(", ");
			}
		}
		sb.append("}");
		return sb.toString();
	}

	public static PartitionSet createFromObservations(double epsilon, long[] observations,
			PartitionAlgorithm clusterAlgorithm) {
		return clusterAlgorithm.calculateClusters(epsilon, observations);
	}

	public static void main(String[] args) {
		// long[] observations = {83, 81, 82, 81, 85, 85, 86, 86, 85, 86, 85, 85,
		// 85, 85, 85, 85, 85, 85, 85, 85, 85, 85, 86, 85,
		// 85, 85, 86, 85, 85, 80, 82, 81, 81, 81, 81, 81,
		// 89, 85, 85, 85, 85, 87, 87, 85, 85, 85, 85, 85,
		// 85, 85, 85, 85, 85, 85, 85, 85, 85, 77, 79, 81,
		// 88, 85, 84, 81, 81, 81, 82, 81, 81, 82, 81, 81,
		// 82, 88, 88, 89, 89, 87, 85, 86, 85, 86, 85, 85,
		// 85, 85, 86, 91, 86, 85, 85, 85, 86, 85, 85, 85,
		// 76, 85, 87, 89};
		// long[] observations = {4, 4, 42, 4, 4, 4, 4, 41, 4, 4, 4, 41, 4, 4, 40, 4, 4,
		// 4, 41, 4, 4, 40, 4, 4, 4, 4, 4, 41, 39, 4, 4, 4, 4, 4, 41, 39, 4, 4, 4, 4, 4,
		// 4, 42, 4, 4, 4, 4, 4, 4, 42, 4, 4, 4, 4, 41, 4, 4, 4, 41, 4, 4, 40, 4, 4, 4,
		// 41, 4, 4, 40, 4, 4, 4, 4, 4, 4, 4, 4, 42, 4, 41, 4, 4, 4, 4, 42, 4, 41, 4,
		// 36, 4, 4, 41, 4, 41, 4, 4, 4, 4, 41, 4};
		long[] observations = { 4, 4, 4, 4, 4, 4, 37, 4, 4, 4, 32, 4, 4, 34, 4, 4, 4, 4, 4, 4, 34, 4, 33, 4, 4, 4, 4,
				34, 4, 4, 4, 4, 36, 4, 4, 4, 4, 4, 4, 34, 4, 33, 4, 4, 4, 4, 37, 4, 4, 4, 4, 4, 4, 37, 4, 4, 4, 4, 35,
				4, 4, 4, 33, 4, 4, 36, 4, 4, 4, 4, 4, 4, 37, 37, 4, 4, 37, 4, 35, 4, 4, 4, 33, 4, 4, 35, 4, 4, 4, 33, 4,
				4, 36, 4, 37, 4, 4, 37, 4, 4 };
		PartitionSet clustering = createFromObservations(2.0, observations, new KDynamic());
		for (int i = 0; i < clustering.getClusterAverageValues().length; i++) {
			System.out.print(clustering.getClusterAverageValues()[i]);
			System.out.print(" , ");
		}
		System.out.println();
		System.out.println(clustering.getMinimumDeltaValue());
		System.out.println(clustering.getNumClusters());
	}

}
