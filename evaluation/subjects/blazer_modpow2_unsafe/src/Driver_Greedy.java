import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
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

	/* Cluster Algorithm */
	public static PartitionAlgorithm clusterAlgorithm = new Greedy(false);

	////////////////////////////////////////////////////////////////////////

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Expects file name as parameter");
			return;
		}
		
		BigInteger public_base = null;
        BigInteger public_modulus = null;
        BigInteger[] secret_exponents = new BigInteger[K];
		
        byte[][] secret_bytes = new byte[K][];
        byte[] public_base_bytes = null;
        byte[] public_modulus_bytes = null;
        
        int maxNumVal = (K+2) * Integer.BYTES;
		
		/* Read all values. */
        byte[] bytes;
        try (FileInputStream fis = new FileInputStream(args[0])) {

            // Determine size of byte array.
            try {
                int fileSize = Math.toIntExact(fis.getChannel().size());
                bytes = new byte[Math.min(fileSize, maxNumVal)];
            } catch (ArithmeticException e) {
                bytes = new byte[maxNumVal];
            }

            if (bytes.length < (K+2)) {
                throw new RuntimeException("too less data");
            } else {
                fis.read(bytes);
            }
        } catch (IOException e) {
            System.err.println("Error reading input");
            e.printStackTrace();
            return;
        }
        
        int m = bytes.length / (K+2);

        public_base_bytes = Arrays.copyOfRange(bytes, 0, m);
        public_modulus_bytes = Arrays.copyOfRange(bytes, m, 2 * m);
        for (int i=0; i<K; i++) {
        	secret_bytes[i] = Arrays.copyOfRange(bytes, (i+2) * m, (i+2+1) * m); 
        }
        
        /* Use only positive values, first value determines the signum. */
        if (public_base_bytes[0] < 0) {
            public_base_bytes[0] = (byte) (public_base_bytes[0] * (-1) - 1);
        }
        if (public_modulus_bytes[0] < 0) {
            public_modulus_bytes[0] = (byte) (public_modulus_bytes[0] * (-1) - 1);
        }
        for (int i=0; i<K; i++) {
            if (secret_bytes[i][0] < 0) {
            	secret_bytes[i][0] = (byte) (secret_bytes[i][0] * (-1) - 1);
            }
        }
        
        /* We do not care about the bit length of the public values. */
        public_base = new BigInteger(public_base_bytes);
        public_modulus = new BigInteger(public_modulus_bytes);
        // Ensure that modulus is not zero.
        if (public_modulus.equals(BigInteger.ZERO)) {
            public_modulus = BigInteger.ONE;
        }
        
        /* Ensure secrets have same bit length */
        int smallestBitLength = Integer.MAX_VALUE;
        for (int i=0; i<K; i++) {
        	secret_exponents[i] = new BigInteger(secret_bytes[i]);
        	
        	/* Determine smallest bitlength. */
        	int bitLength = (secret_exponents[i].equals(BigInteger.ZERO) ? 1 : secret_exponents[i].bitLength());
        	if (bitLength < smallestBitLength) {
        		smallestBitLength = bitLength;
        	}
        }
        for (int i=0; i<K; i++) {
        	int bitLength = (secret_exponents[i].equals(BigInteger.ZERO) ? 1 : secret_exponents[i].bitLength());
        	
            if (bitLength != smallestBitLength) {
                /*
                 * Trim bigger number to smaller bit length and ensure there is the 1 in the beginning of the bit
                 * representation, otherwise the zero would be trimmed again by the BigInteger constructor and hence it
                 * would have a smaller bit length.
                 */
                String bitStr = secret_exponents[i].toString(2);
                bitStr = "1" + bitStr.substring(bitLength - smallestBitLength + 1);
                secret_exponents[i] = new BigInteger(bitStr, 2);
            }
        }
        
        System.out.println("public_base=" + public_base);
        System.out.println("public_base.bitlength=" + public_base.bitLength());
        System.out.println("public_modulus=" + public_modulus);
        System.out.println("public_modulus.bitlength=" + public_modulus.bitLength());
        
        for (int i=0; i<K; i++) {
            System.out.println("secret" + i + "_exponent=" + secret_exponents[i]);
            System.out.println("secret" + i + "_exponent.bitlength=" + secret_exponents[i].bitLength());
            System.out.println("secret" + i + "_exponent=" + secret_exponents[i].toString(2));
        }

		long[] observations = new long[K];
		Mem.clear(true);
		for (int i = 0; i < K; i++) {
			int bitLength = (secret_exponents[i].equals(BigInteger.ZERO) ? 1 : secret_exponents[i].bitLength());
			Mem.clear(false);
			ModPow2.modPow2_unsafe(public_base, secret_exponents[i], public_modulus, bitLength);
			observations[i] = Mem.instrCost;
		}
		System.out.println("observations: " + Arrays.toString(observations));

		PartitionSet clusters = PartitionSet.createFromObservations(epsilon, observations, clusterAlgorithm);
		Kelinci.setObserverdClusters(clusters.getClusterAverageValues(), clusters.getMinimumDeltaValue());

		System.out.println("Done.");
	}

}
