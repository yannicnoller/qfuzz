import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

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
	
	public static PartitionAlgorithm clusterAlgorithm = new Greedy(false);

	////////////////////////////////////////////////////////////////////////

	public static void main(String[] args) throws IOException {

		byte[] public_value; // low values
		byte[][] secrets; // high values

		// Read all inputs.
		byte[] bytes;
		try (FileInputStream fis = new FileInputStream(args[0])) {
			// Determine size of byte array.
			int fileSize = Math.toIntExact(fis.getChannel().size());
			bytes = new byte[fileSize];
			if (bytes.length < (K + 1)) {
				throw new RuntimeException("too less data");
			} else {
				fis.read(bytes);
			}
		} catch (IOException e) {
			System.err.println("Error reading input");
			e.printStackTrace();
			return;
		}
		int SIZE = bytes.length / (K + 1);

		public_value = Arrays.copyOfRange(bytes, 0, SIZE);
		secrets = new byte[K][SIZE];
		for (int i = 0; i < secrets.length; i++) {
			secrets[i] = Arrays.copyOfRange(bytes, SIZE * i, SIZE * (i + 1));
		}

		System.out.println("public=" + Arrays.toString(public_value));
		for (int i = 0; i < secrets.length; i++) {
			System.out.println("secret" + i + "=" + Arrays.toString(secrets[i]));
		}

		byte[] cookie = { 'c', 'o', 'o', 'k', 'i', 'e' };
		
		long[] observations = new long[K];
		Mem.clear(true);
		for (int i = 0; i < K; i++) {
			Mem.clear(false);
			
			byte[] secret_cookie = Arrays.copyOf(secrets[i], secrets[i].length + cookie.length);
			byte[] public_cookie = Arrays.copyOf(public_value, public_value.length + cookie.length);
			System.arraycopy(cookie, 0, secret_cookie, secrets[i].length, cookie.length);
			System.arraycopy(cookie, 0, public_cookie, public_value.length, cookie.length);
			final byte[] all1 = Arrays.copyOf(secret_cookie, secret_cookie.length + public_cookie.length);
			System.arraycopy(public_cookie, 0, all1, secret_cookie.length, public_cookie.length);

			final byte[] compressed = LZ77T.compress(all1);
			
			observations[i] = compressed.length;
		}
		System.out.println("observations: " + Arrays.toString(observations));

		PartitionSet clusters = PartitionSet.createFromObservations(epsilon, observations, clusterAlgorithm);
		Kelinci.setObserverdClusters(clusters.getClusterAverageValues(), clusters.getMinimumDeltaValue());

		System.out.println("Done.");
	}

}