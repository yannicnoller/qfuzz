import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Expects file name as parameter");
			return;
		}

		byte[][] secrets_digesta = null;
		byte[] public_digestb = null;

		/* Read all values. */
		List<Byte> values = new ArrayList<>();
		try (FileInputStream fis = new FileInputStream(args[0])) {
			byte[] bytes = new byte[1];
			while ((fis.read(bytes) != -1)) {// && (i < maxM * n)
				values.add(bytes[0]);
			}
		} catch (IOException e) {
			System.err.println("Error reading input...");
			e.printStackTrace();
			return;
		}

		if (values.size() < 3) {
			throw new RuntimeException("Too Less Data...");
		}

		int m = values.size() / (K + 1);

		// Read user public.
		public_digestb = new byte[m];
		for (int i = 0; i < m; i++) {
			public_digestb[i] = values.get(i);
		}

		secrets_digesta = new byte[K][m];
		for (int i = 0; i < K; i++) {
			for (int j = 0; i < m; i++) {
				secrets_digesta[i][j] = values.get(i + (K + 1) * m);
			}
		}

		System.out.println("public_digestb=" + Arrays.toString(public_digestb));
		for (int i = 0; i < K; i++) {
			System.out.println("secrets_digesta" + Arrays.toString(secrets_digesta[i]));
		}

		long[] observations = new long[K];
		Mem.clear(true);
		for (int i = 0; i < K; i++) {
			Mem.clear(false);
			MessageDigest.isEqual_unsafe(secrets_digesta[i], public_digestb);
			observations[i] = Mem.instrCost;
		}
		System.out.println("observations: " + Arrays.toString(observations));

		PartitionSet clusters = PartitionSet.createFromObservations(epsilon, observations, clusterAlgorithm);
		Kelinci.setObserverdClusters(clusters.getClusterAverageValues(), clusters.getMinimumDeltaValue());

		System.out.println("Done.");
	}

}
