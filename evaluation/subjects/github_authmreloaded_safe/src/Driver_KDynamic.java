import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.quantification.PartitionAlgorithm;
import edu.cmu.sv.kelinci.quantification.PartitionSet;
import edu.cmu.sv.kelinci.quantification.KDynamic;
import fr.xephi.authme.security.crypts.EncryptionMethod;
import fr.xephi.authme.security.crypts.HashedPassword;
import fr.xephi.authme.security.crypts.RoyalAuth;

public class Driver_KDynamic {

	/* Maximum number of different observations. */
	public final static int K = 100;

	/* Minimum distance between clusters. */
	public final static double epsilon = 1.0;

	/* Cluster Algorithm */
	public static PartitionAlgorithm clusterAlgorithm = new KDynamic();

	////////////////////////////////////////////////////////////////////////

	public static final int MAX_PASSWORD_LENGTH = 16; // characters

	public static final boolean SAFE_MODE = true;

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Expects file name as parameter");
			return;
		}

		EncryptionMethod encrMethod = new RoyalAuth(SAFE_MODE);

		/* Read input. */
		String username_public;
		String password_public;
		HashedPassword[] secrets = new HashedPassword[K];

		try (FileInputStream fis = new FileInputStream(args[0])) {

			/* Read public value. */
			int length = fis.read();
			if (length < 0) {
				throw new RuntimeException("Not enough input data...");
			}
			length = length % MAX_PASSWORD_LENGTH + 1;

			byte[] bytes = new byte[length];
			if (fis.read(bytes) < 0) {
				throw new RuntimeException("Not enough input data...");
			}
			char[] tmp = new char[length];
			for (int i = 0; i < tmp.length; i++) {
				byte value = bytes[i];
				char charValue = (char) (value % 128);
				tmp[i] = charValue;
			}
			username_public = new String(tmp);

			bytes = new byte[length];
			if (fis.read(bytes) < 0) {
				throw new RuntimeException("Not enough input data...");
			}
			tmp = new char[length];
			for (int i = 0; i < tmp.length; i++) {
				byte value = bytes[i];
				char charValue = (char) (value % 128);
				tmp[i] = charValue;
			}
			password_public = new String(tmp);

			/* Generate first secret. */
			secrets[0] = encrMethod.computeHash(password_public, username_public);

			for (int i = 1; i < secrets.length; i++) {

				/* Read another secret. */
				length = fis.read();
				if (length < 0) {
					throw new RuntimeException("Not enough input data...");
				}
				length = length % MAX_PASSWORD_LENGTH + 1;

				bytes = new byte[length];
				if (fis.read(bytes) < 0) {
					throw new RuntimeException("Not enough input data...");
				}
				tmp = new char[length];
				for (int j = 0; j < tmp.length; j++) {
					byte value = bytes[j];
					char charValue = (char) (value % 128);
					tmp[j] = charValue;
				}
				secrets[i] = encrMethod.computeHash(new String(tmp), username_public);
			}

		} catch (IOException e) {
			System.err.println("Error reading input");
			e.printStackTrace();
			return;
		}

		System.out.println("username_public=" + username_public);
		System.out.println("password_public=" + password_public);
		for (int i = 0; i < secrets.length; i++) {
			System.out.println("secret" + i + "=" + secrets[i].getHash());
		}

		long[] observations = new long[K];
		Mem.clear(true);
		for (int i = 0; i < K; i++) {
			Mem.clear(false);
			encrMethod.comparePassword(password_public, secrets[i], username_public);
			observations[i] = Mem.instrCost;
		}
		System.out.println("observations: " + Arrays.toString(observations));

		PartitionSet clusters = PartitionSet.createFromObservations(epsilon, observations, clusterAlgorithm);
		Kelinci.setObserverdClusters(clusters.getClusterAverageValues(), clusters.getMinimumDeltaValue());

		System.out.println("Done.");
	}

}
