import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

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

	public static final int MAX_USERNAME_LENGTH = 5; // bytes

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Expects file name as parameter");
			return;
		}

		String userBytesStringPublic;
		int[] invalidCharacterIndex = new int[K-1];

		// Read all inputs.
		try (FileInputStream fis = new FileInputStream(args[0])) {

			/* Read public value for public_actual */
			byte[] bytes = new byte[MAX_USERNAME_LENGTH];
			if (fis.read(bytes) < 0) {
				throw new RuntimeException("Not enough input data...");
			}
			char[] tmp = new char[bytes.length];
			for (int i = 0; i < bytes.length; i++) {
				tmp[i] = (char) (bytes[i] % 128);
			}
			userBytesStringPublic = new String(tmp);

			/* Generate secrets. */
			for (int i = 0; i < K - 1; i++) {
				invalidCharacterIndex[i] = fis.read();
			}

		} catch (IOException e) {
			System.err.println("Error reading input");
			e.printStackTrace();
			return;
		}

		System.out.println("userBytesStringPublic = " + userBytesStringPublic);
		for (int i = 0; i < K - 1; i++) {
			System.out.println("invalidCharacterIndex " + i + " = " + invalidCharacterIndex[i]);
		}

		byte[] secretKey = { 15, 23, -12, 17, 3 }; // YN just random, but fixed for all experiments
		TokenHandler th = new TokenHandler(secretKey, false);

		byte[] validHash = th.hmac.doFinal(DatatypeConverter.parseBase64Binary(userBytesStringPublic));
		String hashByteStringValid = DatatypeConverter.printBase64Binary(validHash);
		String userTokenValid = userBytesStringPublic + TokenHandler.SEPARATOR + hashByteStringValid;

		String[] userTokenInvalid = new String[K - 1];
		for (int i = 0; i < K - 1; i++) {
			
			/* Generate a hash with same size but the wrong content. */
	        invalidCharacterIndex[i] = invalidCharacterIndex[i] % validHash.length;
	        byte[] invalidHash = new byte[validHash.length];
	        for (int j = 0; j < invalidHash.length; j++) {
	            if (j == invalidCharacterIndex[i]) {
	                invalidHash[j] = (byte) ((validHash[j] == 42) ? 21 : 42);
	            } else {
	                invalidHash[j] = validHash[j];
	            }
	        }
	        String hashByteStringInvalid = DatatypeConverter.printBase64Binary(invalidHash);
	        userTokenInvalid[i] = userBytesStringPublic + TokenHandler.SEPARATOR + hashByteStringInvalid;
	        
		}
		
        
		long[] observations = new long[K];
		Mem.clear(true);
		th.parseUserFromToken(userTokenValid);
		observations[0] = Mem.instrCost;

		Mem.clear(true);
		for (int i = 0; i < K - 1; i++) {
			Mem.clear(false);
			th.parseUserFromToken(userTokenInvalid[i]);
			observations[i + 1] = Mem.instrCost;
		}
		System.out.println("observations: " + Arrays.toString(observations));

		PartitionSet clusters = PartitionSet.createFromObservations(epsilon, observations, clusterAlgorithm);
		Kelinci.setObserverdClusters(clusters.getClusterAverageValues(), clusters.getMinimumDeltaValue());

		System.out.println("Done.");
	}

}
