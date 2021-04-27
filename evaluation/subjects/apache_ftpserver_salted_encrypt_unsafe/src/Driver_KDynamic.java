import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor;

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

	public static final int MAX_PASSWORD_LENGTH = 16; // characters

	public static final boolean SAFE_MODE = false;

	/* retrieved from application */
	private static final int SALT_LENGTH = 1; /* Integer number */
	private static final int MAXIMUM_SALT_VALUE = 99999999;

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Expects file name as parameter");
			return;
		}

		String public_guess;
		String[] secrets = new String[K];

		SaltedPasswordEncryptor pe = new SaltedPasswordEncryptor(SAFE_MODE);

		try (FileInputStream fis = new FileInputStream(args[0])) {

			int minNumBytesToRead = 1 + K * SALT_LENGTH * Integer.BYTES;
			int maxNumBytesToRead = MAX_PASSWORD_LENGTH + K * SALT_LENGTH * Integer.BYTES;
			System.out.println("minNumBytesToRead: " + minNumBytesToRead);
			System.out.println("maxNumBytesToRead: " + maxNumBytesToRead);

			int totalNumberOfBytesInFile = Math.toIntExact(fis.getChannel().size());

			if (totalNumberOfBytesInFile < minNumBytesToRead) {
				throw new RuntimeException("not enough data!");
			}

			int usedNumberOfBytes = Math.min(totalNumberOfBytesInFile, maxNumBytesToRead);
			byte[] allBytes = new byte[usedNumberOfBytes];
			fis.read(allBytes);

			/* Read public. */
			int passwordByteLength = usedNumberOfBytes - (K * SALT_LENGTH * Integer.BYTES);
			int index = 0;
			byte[] temp = Arrays.copyOfRange(allBytes, index, index + passwordByteLength);
			index += passwordByteLength;
			public_guess = new String(temp);

			/* Read secrets. */
			for (int i = 0; i < secrets.length; i++) {

				/* Read salt. */
				temp = Arrays.copyOfRange(allBytes, index, index + SALT_LENGTH * Integer.BYTES);
				index += SALT_LENGTH * Integer.BYTES;
				int salt = Math.abs(ByteBuffer.wrap(temp).getInt() % (MAXIMUM_SALT_VALUE));
				secrets[i] = Integer.toString(salt);

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
			pe.encrypt(public_guess, secrets[i]);
			observations[i] = Mem.instrCost;
		}
		System.out.println("observations: " + Arrays.toString(observations));

		PartitionSet clusters = PartitionSet.createFromObservations(epsilon, observations, clusterAlgorithm);
		Kelinci.setObserverdClusters(clusters.getClusterAverageValues(), clusters.getMinimumDeltaValue());

		System.out.println("Done.");
	}

}
