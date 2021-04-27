import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		
        /* Read input. */
        String username = "username"; // irrelevant
        byte[] guess;
        byte[][] realpassword_secrets = new byte[K][];
        
		/* Read all values. */
        try (FileInputStream fis = new FileInputStream(args[0])) {

            /* Read all data. */
            int value;
            int count = 0;
            List<Byte> values = new ArrayList<>();
            while (((value = fis.read()) != -1) && (count < (K+1) * MAX_PASSWORD_LENGTH)) {
                values.add((byte) (value % 127));
                count++;
            }
            
            /* input must be non-empty */
            if (values.size() < 3) {
                throw new RuntimeException("not enough data!");
            }

            int eachSize = values.size() / (K+1);

            guess = new byte[eachSize];
            for (int j = 0; j < eachSize; j++) {
                guess[j] = values.get(j);
            }
            
            for (int i=0; i<K; i++) {
                realpassword_secrets[i] = new byte[eachSize];
                for (int j = 0; j < eachSize; j++) {
                	realpassword_secrets[i][j] = values.get(j + (i+1) * eachSize);
                }

            }

        } catch (IOException e) {
            System.err.println("Error reading input");
            e.printStackTrace();
            return;
        }

        System.out.println("username=" + username);
        System.out.println("password=" + Arrays.toString(guess));
        
        for (int i=0; i<K; i++) {
            System.out.println("secret" + i + "=" + Arrays.toString(realpassword_secrets[i]));
        }

		long[] observations = new long[K];
		Mem.clear(true);
		for (int i = 0; i < K; i++) {
			Mem.clear(false);
			Login.login_unsafe(realpassword_secrets[i], guess, username);
			observations[i] = Mem.instrCost;
		}
		System.out.println("observations: " + Arrays.toString(observations));

		PartitionSet clusters = PartitionSet.createFromObservations(epsilon, observations, clusterAlgorithm);
		Kelinci.setObserverdClusters(clusters.getClusterAverageValues(), clusters.getMinimumDeltaValue());

		System.out.println("Done.");
	}

}
