import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import com.acciente.oacc.PasswordCredentials;

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

	public static final int MAX_PASSWORD_LENGTH = 16; // characters

	static char[] validCharacters = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
			'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a',
			'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
			'w', 'x', 'y', 'z', '{', '}', '(', ')', '[', ']', '#', ':', ';', '^', '!', '|', '&', '_', '~', '@', '$',
			'%', '/' };

	public static String mapToLetterOrDigest(String stringValue) {
		char[] newCharValues = new char[stringValue.length()];
		for (int i = 0; i < newCharValues.length; i++) {
			newCharValues[i] = validCharacters[stringValue.charAt(i) % validCharacters.length];
		}
		return new String(newCharValues);
	}

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Expects file name as parameter");
			return;
		}

		PasswordCredentials public_credentials;
		PasswordCredentials[] secrets = new PasswordCredentials[K];

		// Read all inputs.
		try (FileInputStream fis = new FileInputStream(args[0])) {

			int minNumBytesToRead = (K + 1) * 1;
			int maxNumBytesToRead = (K + 1) * MAX_PASSWORD_LENGTH;

			int totalNumberOfBytesInFile = Math.toIntExact(fis.getChannel().size());

			if (totalNumberOfBytesInFile < minNumBytesToRead) {
				throw new RuntimeException("not enough data!");
			}

			int usedNumberOfBytes = Math.min(totalNumberOfBytesInFile, maxNumBytesToRead);
			byte[] allBytes = new byte[usedNumberOfBytes];
			fis.read(allBytes);

			int eachPasswordLength = usedNumberOfBytes / (K + 1);

			/* Read public. */
			int index = 0;
			byte[] temp = Arrays.copyOfRange(allBytes, index, index + eachPasswordLength);
			public_credentials = PasswordCredentials.newInstance(new String(temp).toCharArray());

			/* Read secrets. */
			for (int i = 0; i < K; i++) {
				index += eachPasswordLength;
				temp = Arrays.copyOfRange(allBytes, index, index + eachPasswordLength);
				secrets[i] = PasswordCredentials.newInstance(new String(temp).toCharArray());
			}

		} catch (IOException e) {
			System.err.println("Error reading input");
			e.printStackTrace();
			return;
		}

		System.out.println("public (password): " + Arrays.toString(public_credentials.getPassword()));
		for (int i = 0; i < K; i++) {
			System.out.println("secret[" + i + "]: " + Arrays.toString(secrets[i].getPassword()));
		}

		long[] observations = new long[K];
		Mem.clear(true);
		for (int i = 0; i < K; i++) {
			Mem.clear(false);
			public_credentials.equals(secrets[i]);
			observations[i] = Mem.instrCost;
		}
		System.out.println("observations: " + Arrays.toString(observations));

		PartitionSet clusters = PartitionSet.createFromObservations(epsilon, observations, clusterAlgorithm);
		Kelinci.setObserverdClusters(clusters.getClusterAverageValues(), clusters.getMinimumDeltaValue());

		System.out.println("Done.");
	}

}
