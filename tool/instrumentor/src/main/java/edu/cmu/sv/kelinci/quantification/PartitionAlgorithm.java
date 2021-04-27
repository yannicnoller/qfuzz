package edu.cmu.sv.kelinci.quantification;

public interface PartitionAlgorithm {

	public PartitionSet calculateClusters(double epsilon, long[] observations);

}
