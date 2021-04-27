import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
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

		int public_a;
		int[] secret_taints = new int[K];

		List<Integer> values = new ArrayList<>();

		/* Read all values. */
		try (FileInputStream fis = new FileInputStream(args[0])) {
			byte[] bytes = new byte[Integer.BYTES];
			while ((fis.read(bytes) != -1) && (values.size() < K+1)) {
				values.add(ByteBuffer.wrap(bytes).getInt());
			}
		} catch (IOException e) {
			System.err.println("Error reading input");
			e.printStackTrace();
			return;
		}
		if (values.size() < K + 1) {
			throw new RuntimeException("Too less data!");
		}

		/* Parse public value. */
		public_a = values.get(0);

		/* Parse secret values. */
		for (int i = 0; i < K; i++) {
			secret_taints[i] = values.get(i+1);
		}

		System.out.println("public=" + public_a);
		for (int i = 0; i < secret_taints.length; i++) {
			System.out.println("secret" + i + "=" + secret_taints[i]);
		}

		long[] observations = new long[K];
		Mem.clear(true);
		for (int i = 0; i < K; i++) {
			Mem.clear(false);
			MoreSanity.loopAndbranch_safe(public_a, secret_taints[i]);
			observations[i] = Mem.instrCost;
		}
		System.out.println("observations: " + Arrays.toString(observations));

		PartitionSet clusters = PartitionSet.createFromObservations(epsilon, observations, clusterAlgorithm);
		Kelinci.setObserverdClusters(clusters.getClusterAverageValues(), clusters.getMinimumDeltaValue());

		System.out.println("Done.");
	}

}
