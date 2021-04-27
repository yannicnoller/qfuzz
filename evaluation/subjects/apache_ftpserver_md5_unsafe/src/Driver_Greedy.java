import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.ftpserver.usermanager.Md5PasswordEncryptor;
import org.apache.ftpserver.usermanager.PasswordEncryptor;

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

	public static final int PASSWORD_LENGTH = 16; // characters

	public static final boolean SAFE_MODE = false;
	
	/*
	 *	Strategy for selecting the inputs:
	 *	1. select public input with given length
	 *	2. generate secret based on public input via the PasswordEncryptor, which represents the exact match.
	 *	3. select k-1 more secrets that have the same length as the generated secret
	 * 
	 */

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Expects file name as parameter");
			return;
		}

		String public_guess;
		String[] secrets = new String[K];

		PasswordEncryptor pe = new Md5PasswordEncryptor(SAFE_MODE);

		// Read all inputs.
		try (FileInputStream fis = new FileInputStream(args[0])) {
			
			/* Read public value. */
			byte[] bytes = new byte[PASSWORD_LENGTH];
			if (fis.read(bytes) < 0) {
				throw new RuntimeException("Not enough input data...");
			}
			char[] tmp = new char[bytes.length];
			for (int i=0; i<bytes.length; i++) {
				byte value = bytes[i];
				/* each char value must be between 0 and 127 and a printable character */
                char charValue = (char) (value % 128);
                tmp[i] = charValue;
			}
			public_guess = new String(tmp);
			System.out.println(public_guess);

			/* Generate first secret. */
			secrets[0] = new String(pe.encrypt(public_guess));
			
			for (int i = 1; i < secrets.length; i++) {
				
				/* Read another secret. */
				bytes = new byte[secrets[0].length()];
				if (fis.read(bytes) < 0) {
					throw new RuntimeException("Not enough input data...");
				}
				tmp = new char[bytes.length];
				for (int j=0; j<bytes.length; j++) {
					byte value = bytes[j];
					/* each char value must be between 0 and 127 and a printable character */
	                char charValue = (char) (value % 128);
	                tmp[j] = charValue;
				}
				secrets[i] = new String(tmp);
				
			}

		} catch (IOException e) {
			System.err.println("Error reading input");
			e.printStackTrace();
			return;
		}

		System.out.println("public_guess=" + public_guess);
		for (int i = 0; i < secrets.length; i++) {
			System.out.println("secret" + i + "=" + secrets[i]);
		}

		long[] observations = new long[K];
		Mem.clear(true);
		for (int i = 0; i < K; i++) {
			Mem.clear(false);
			pe.matches(public_guess, secrets[i]);
			observations[i] = Mem.instrCost;
		}
		System.out.println("observations: " + Arrays.toString(observations));

		PartitionSet clusters = PartitionSet.createFromObservations(epsilon, observations, clusterAlgorithm);
		Kelinci.setObserverdClusters(clusters.getClusterAverageValues(), clusters.getMinimumDeltaValue());

		System.out.println("Done.");
	}

}
