import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.ws.security.WSSecurityException;
import org.apache.wss4j.binding.wss10.PasswordString;

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

	public static final int MAX_PASSWORD_LENGTH = 16; // bytes

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Expects file name as parameter");
			return;
		}

		String validPassword_public;
		String[] storedPassword_secret = new String[K];

		// Read all inputs.
		try (FileInputStream fis = new FileInputStream(args[0])) {

			/* Read all data. */
			int i = 0;
			int value;
			List<Character> values = new ArrayList<>();
			while (((value = fis.read()) != -1) && (i < (K + 1) * MAX_PASSWORD_LENGTH)) {
				/* each char value must be between 0 and 127 and a printable character */
				value = value % 127;
				char charValue = (char) value;
				if (Character.isAlphabetic(charValue) || Character.isDigit(charValue)) {
					values.add(charValue);
					i++;
				}
			}
			/* input must be non-empty */
			int eachSize = values.size() / (K + 1);
			if (eachSize % Character.BYTES == 1) {
				eachSize--;
			}
			if (eachSize < 1) {
				throw new RuntimeException("not enough data!");
			}
			System.out.println("eachSize=" + eachSize);

			char[] tmp_array = new char[eachSize];
			for (i = 0; i < eachSize; i++) {
				tmp_array[i] = values.get(i);
			}
			validPassword_public = new String(tmp_array);

			for (int j = 0; j < K; j++) {
				tmp_array = new char[eachSize];
				for (i = 0; i < eachSize; i++) {
					tmp_array[i] = values.get(i + (j + 1) * eachSize);
				}
				storedPassword_secret[j] = new String(tmp_array);
			}

		} catch (IOException e) {
			System.err.println("Error reading input");
			e.printStackTrace();
			return;
		}

		System.out.println("password=" + validPassword_public);
		for (int i = 0; i < K; i++) {
			System.out.println("secret[" + i + "]=" + storedPassword_secret[i]);
		}

		PasswordString passwordType = new PasswordString();
		passwordType.setValue(validPassword_public);

		long[] observations = new long[K];
		Mem.clear(true);
		for (int i = 0; i < K; i++) {
			Mem.clear(false);
			try {
				SimplifiedUsernameTokenValidatorImpl.verifyPlaintextPassword(passwordType, storedPassword_secret[i]);
			} catch (WSSecurityException e) {
			}
			observations[i] = Mem.instrCost;
		}
		System.out.println("observations: " + Arrays.toString(observations));

		PartitionSet clusters = PartitionSet.createFromObservations(epsilon, observations, clusterAlgorithm);
		Kelinci.setObserverdClusters(clusters.getClusterAverageValues(), clusters.getMinimumDeltaValue());

		System.out.println("Done.");
	}

}
