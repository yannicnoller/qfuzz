import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.quantification.PartitionAlgorithm;
import edu.cmu.sv.kelinci.quantification.PartitionSet;
import edu.cmu.sv.kelinci.quantification.Greedy;

public class Driver_Greedy {

	/* Maximum number of different observations. */
	public final static int K = 100;

	/* Minimum distance between clusters. */
	public final static double epsilon = 1.0;

	/* Cluster Algorithm */
	public static PartitionAlgorithm clusterAlgorithm = new Greedy(false);

	////////////////////////////////////////////////////////////////////////

	private static final int MAX_INT_VALUE = 100;

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Expects file name as parameter");
			return;
		}

		int startRow, rowsCount; // public
		int[] usersLength = new int[K]; // secret

		/* Read input file. */
		try (FileInputStream fis = new FileInputStream(args[0])) {
			int value;

			if ((value = fis.read()) == -1) {
				throw new RuntimeException("Not enough data!");
			}
			startRow = Math.abs(value) % MAX_INT_VALUE;

			if ((value = fis.read()) == -1) {
				throw new RuntimeException("Not enough data!");
			}
			rowsCount = Math.abs(value) % MAX_INT_VALUE;

			for (int i = 0; i < K; i++) {
				if ((value = fis.read()) == -1) {
					throw new RuntimeException("Not enough data!");
				}
				usersLength[i] = Math.abs(value) % MAX_INT_VALUE;
			}

		} catch (IOException e) {
			System.err.println("Error reading input");
			e.printStackTrace();
			return;
		}

		System.out.println("startRow=" + startRow);
		System.out.println("rowsCount=" + rowsCount);
		for (int i = 0; i < K; i++) {
			System.out.println("usersLength[" + i + "]=" + usersLength[i]);
		}

		long[] observations = new long[K];
		Mem.clear(true);
		for (int i = 0; i < K; i++) {
			UsersTableModelServiceImpl ser = new UsersTableModelServiceImpl(usersLength[i]);
			long cost = 0L;
			Mem.clear(false);
	        try {
	            String[][] res1 = ser.getRows(startRow, rowsCount, null, null, true);
	            cost = res1.length;
	        } catch (IndexOutOfBoundsException e) {
	            // ignore;
	        }
			observations[i] = cost;
		}
		System.out.println("observations: " + Arrays.toString(observations));

		PartitionSet clusters = PartitionSet.createFromObservations(epsilon, observations, clusterAlgorithm);
		Kelinci.setObserverdClusters(clusters.getClusterAverageValues(), clusters.getMinimumDeltaValue());

		System.out.println("Done.");
	}

}
