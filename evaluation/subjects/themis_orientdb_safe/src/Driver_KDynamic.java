import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.quantification.PartitionAlgorithm;
import edu.cmu.sv.kelinci.quantification.PartitionSet;
import edu.cmu.sv.kelinci.quantification.KDynamic;

/* Side-Channel not for password but to check whether username exists in system. */
public class Driver_KDynamic {

	/* Maximum number of different observations. */
	public final static int K = 100;

	/* Minimum distance between clusters. */
	public final static double epsilon = 1.0;

	/* Cluster Algorithm */
	public static PartitionAlgorithm clusterAlgorithm = new KDynamic();

	////////////////////////////////////////////////////////////////////////

	public static final int MAX_PASSWORD_LENGTH = 16; // bytes

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Expects file name as parameter");
			return;
		}

		String public_ihash;
		String[] secret_iPasswords = new String[K];

		// Read all inputs.
		try (FileInputStream fis = new FileInputStream(args[0])) {

			/* Read public value for public_actual */
			byte[] bytes = new byte[MAX_PASSWORD_LENGTH];
			if (fis.read(bytes) < 0) {
				throw new RuntimeException("Not enough input data...");
			}
			char[] tmp = new char[bytes.length];
			for (int i = 0; i < bytes.length; i++) {
				tmp[i] = (char) (bytes[i] % 128);
			}
			public_ihash = new String(tmp);

			/* Generate secrets. */
			for (int i = 0; i < K; i++) {

				bytes = new byte[MAX_PASSWORD_LENGTH];
				if (fis.read(bytes) < 0) {
					throw new RuntimeException("Not enough input data...");
				}
				tmp = new char[bytes.length];
				for (int j = 0; j < bytes.length; j++) {
					byte value = bytes[j];
					/* each char value must be between 0 and 127 and a printable character */
					char charValue = (char) (value % 128);
					tmp[j] = charValue;
				}
				secret_iPasswords[i] = new String(tmp);
			}

		} catch (IOException e) {
			System.err.println("Error reading input");
			e.printStackTrace();
			return;
		}

		System.out.println("public_ihash = " + public_ihash);
		for (int i = 0; i < K; i++) {
			System.out.println("secret_iPasswords " + i + " = " + secret_iPasswords[i]);
		}

		OSecurityManager manager = new OSecurityManager();
		long[] observations = new long[K];
		Mem.clear(true);
		for (int i = 0; i < K; i++) {
			Mem.clear(false);
			manager.checkPassword_safe(secret_iPasswords[i], public_ihash);
			observations[i] = Mem.instrCost;
		}
		System.out.println("observations: " + Arrays.toString(observations));

		PartitionSet clusters = PartitionSet.createFromObservations(epsilon, observations, clusterAlgorithm);
		Kelinci.setObserverdClusters(clusters.getClusterAverageValues(), clusters.getMinimumDeltaValue());

		System.out.println("Done.");
	}

}
