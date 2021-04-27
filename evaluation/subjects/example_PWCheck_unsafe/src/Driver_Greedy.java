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

    public final static boolean SAFE_MODE = false;
    
    /* Maximum number of different observations. */
    public final static int K = 10;
    public final static double epsilon = 1.0;
    
	/* Cluster Algorithm */
	public static PartitionAlgorithm clusterAlgorithm = new Greedy(false);

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Expects file name as parameter");
            return;
        }
        
        int numberOfVariables = K + 1; // how many variables
        int max_password_length = 16; // bytes
        
        // Read all inputs.
        List<Byte> values = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(args[0])) {
            byte[] bytes = new byte[1];
            int i = 0;
            while ((fis.read(bytes) != -1) && (i < max_password_length * numberOfVariables)) {
                values.add(bytes[0]);
                i++;
            }
        } catch (IOException e) {
            System.err.println("Error reading input");
            e.printStackTrace();
            return;
        }

        List<byte[]> secrets = new ArrayList<>();
        byte[] public_guess;

        if (values.size() < numberOfVariables) {
            throw new RuntimeException("Not enough input data...");
        }

        int passwordLength = values.size() / numberOfVariables;
        System.out.println("length=" + passwordLength);
        
        
        // Read public.
        public_guess = new byte[passwordLength];
        for (int i = 0; i < passwordLength; i++) {
        	public_guess[i] = values.get(i);
        }
        
        System.out.println("public_guess=" + Arrays.toString(public_guess));
        
        // Read secrets.
        for (int i=0; i<K; i++) {
            byte[] secret_pw = new byte[passwordLength];
            for (int j = 0; j < passwordLength; j++) {
            	secret_pw[j] = values.get(j + i * passwordLength);
            }
            secrets.add(secret_pw);
            System.out.println("secret" + i + "=" + Arrays.toString(secret_pw));
        }

        long[] observations = new long[K];
        Mem.clear(true);
        for (int i=0; i<K; i++) {
            long cost;
            if (SAFE_MODE) {
                Mem.clear(false);
                PWCheck.pwcheck3_safe(public_guess, secrets.get(i));
                cost = Mem.instrCost;
            } else {
                Mem.clear(false);
                PWCheck.pwcheck1_unsafe(public_guess, secrets.get(i));
                cost = Mem.instrCost;
            }
            observations[i] = cost;
            //System.out.println("cost" + i + ": " + cost);
        }
        System.out.println("observations: " + Arrays.toString(observations));
        
		PartitionSet clusters = PartitionSet.createFromObservations(epsilon, observations, clusterAlgorithm);
		Kelinci.setObserverdClusters(clusters.getClusterAverageValues(), clusters.getMinimumDeltaValue());
        
        System.out.println("Done.");
    }

}
