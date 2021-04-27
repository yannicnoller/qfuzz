import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.ftpserver.util.StringUtils;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.quantification.PartitionAlgorithm;
import edu.cmu.sv.kelinci.quantification.PartitionSet;
import edu.cmu.sv.kelinci.quantification.KDynamic;

public class Driver_KDynamic {

	/* Maximum number of different observations. */
	public final static int K = 100;

	/* Minimum distance between clusters. */
	public final static double epsilon = 1.0;
	
	/* Cluster Algorithm */
	public static PartitionAlgorithm clusterAlgorithm = new KDynamic();

	////////////////////////////////////////////////////////////////////////

	public static final int MAX_USERNAME_LENGTH = 16; // characters

	public static final boolean SAFE_MODE = false;

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Expects file name as parameter");
			return;
		}

		String[] secrets = new String[K];

		try (FileInputStream fis = new FileInputStream(args[0])) {

			/* Read secrets. */
			for (int i = 0; i < secrets.length; i++) {

				/* Read username length. */
				int userNameLength = fis.read();
				if (userNameLength == -1) {
					throw new RuntimeException("not enough data!");
				}
				userNameLength = userNameLength % (MAX_USERNAME_LENGTH+1);

				secrets[i] = "";
				for (int j = 0; j < userNameLength; j++) {
					secrets[i] += "X";
				}

			}

		} catch (IOException e) {
			System.err.println("Error reading input");
			e.printStackTrace();
			return;
		}

		for (int i = 0; i < secrets.length; i++) {
			System.out.println("secret" + i + "=" + secrets[i]);
		}

		StringUtils.safeMode = SAFE_MODE;

		long[] observations = new long[K];
		Mem.clear(true);
		for (int i = 0; i < K; i++) {
			Mem.clear(false);
			StringUtils.pad(secrets[i], ' ', true, MAX_USERNAME_LENGTH);
			observations[i] = Mem.instrCost;
		}
		System.out.println("observations: " + Arrays.toString(observations));

		PartitionSet clusters = PartitionSet.createFromObservations(epsilon, observations, clusterAlgorithm);
		Kelinci.setObserverdClusters(clusters.getClusterAverageValues(), clusters.getMinimumDeltaValue());

		System.out.println("Done.");
	}

}
