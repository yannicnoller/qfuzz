import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.quantification.PartitionAlgorithm;
import edu.cmu.sv.kelinci.quantification.PartitionSet;
import edu.cmu.sv.kelinci.quantification.Greedy;

/* Side-Channel not for password but to check whether username exists in system. */
public class Driver_KGreedy {

	/* Maximum number of different observations. */
	public final static int K = 5;

	/* Minimum distance between clusters. */
	public final static double epsilon = 1.0;

	/* Cluster Algorithm */
	public static PartitionAlgorithm clusterAlgorithm = new Greedy(false);

	////////////////////////////////////////////////////////////////////////

	public static final int MAX_PASSWORD_LENGTH = 16; // bytes

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Expects file name as parameter");
			return;
		}

		String username;
		String password;
		String[] username_secrets = new String[K];
		String[] password_secrets = new String[K];

		// Read all inputs.
		try (FileInputStream fis = new FileInputStream(args[0])) {

			/* Read public value for username and password. */
			byte[] bytes = new byte[MAX_PASSWORD_LENGTH];
			if (fis.read(bytes) < 0) {
				throw new RuntimeException("Not enough input data...");
			}
			char[] tmp = new char[bytes.length];
			for (int i = 0; i < bytes.length; i++) {
				tmp[i] = (char) (bytes[i] % 128);
			}
			username = new String(tmp);

			bytes = new byte[MAX_PASSWORD_LENGTH];
			if (fis.read(bytes) < 0) {
				throw new RuntimeException("Not enough input data...");
			}
			tmp = new char[bytes.length];
			for (int i = 0; i < bytes.length; i++) {
				tmp[i] = (char) (bytes[i] % 128);
			}
			password = new String(tmp);

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
				username_secrets[i] = new String(tmp);

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
				password_secrets[i] = new String(tmp);
			}

		} catch (IOException e) {
			System.err.println("Error reading input");
			e.printStackTrace();
			return;
		}

		System.out.println("public username = " + username);
		System.out.println("public password = " + password);
		for (int i = 0; i < K; i++) {
			System.out.println("secret username " + i + " = " + username_secrets[i]);
			System.out.println("secret password " + i + " = " + password_secrets[i]);
		}
		
		long[] observations = new long[K];
		Mem.clear(true);
		for (int i = 0; i < K; i++) {
			Timing.resetMap(username_secrets[i], password_secrets[i]);
			Mem.clear(false);
			Timing.login_safe(username, password);
			observations[i] = Mem.instrCost;
		}
		System.out.println("observations: " + Arrays.toString(observations));

		PartitionSet clusters = PartitionSet.createFromObservations(epsilon, observations, clusterAlgorithm);
		Kelinci.setObserverdClusters(clusters.getClusterAverageValues(), clusters.getMinimumDeltaValue());

		System.out.println("Done.");
	}

}
