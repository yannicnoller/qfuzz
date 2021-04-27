import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
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
        List<Character> values = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(args[0])) {
            byte[] bytes = new byte[Character.BYTES];
            int i = 0;
            while ((fis.read(bytes) != -1) && (i < max_password_length * numberOfVariables)) {
                char value = ByteBuffer.wrap(bytes).getChar();
                int val = (((int) value) % 58) + 65;
                values.add((char) val);
                i++;
            }
        } catch (IOException e) {
            System.err.println("Error reading input");
            e.printStackTrace();
            return;
        }
        if (values.size() < numberOfVariables) {
            throw new RuntimeException("Too less data...");
        }

        int m = values.size() / numberOfVariables;

        // Read public.
        List<Character> public_lst = new ArrayList<>();
        int z = 0;
        for (int i = 0; i < m; i++) {
          // if(values.get(i) >= 'a' && values.get(i) <= 'z')
          // {
          //   public_lst.add(values.get(i));
          //   z += 1;
          // }
          public_lst.add(values.get(i));
        }
        int pub_len = public_lst.size();
        char[] public_arr = new char[pub_len];
        for(int i = 0; i < pub_len; i++)
          public_arr[i] = public_lst.get(i);
        String s2 = new String(public_arr);
        System.out.println("public" + "=" + s2);

        List<String> s1 = new ArrayList<>();
        // Read secret1.
        for (int j=0; j<K; j++) {
          z = 0;
          List<Character> secret1_lst = new ArrayList<>();
          for (int i = 0; i < m; i++) {
            // if(values.get(i+m*(j+1)) >= 'a' && values.get(i+m*(j+1)) <= 'z')
            // {
            //   secret1_lst.add(values.get(i+m*(j+1)));
            //   z += 1;
            // }
            secret1_lst.add(values.get(i+m*(j+1)));
          }
          int sec_len = secret1_lst.size();
          char[] secret1_arr = new char[sec_len];
          for(int i = 0; i < sec_len; i++)
            secret1_arr[i] = secret1_lst.get(i);
          String secret = new String(secret1_arr);
          s1.add(secret);
          System.out.println("secret" + j + "=" + secret);
        }

        long[] observations = new long[K];
        Mem.clear(true);
        for (int i=0; i<K; i++) {
            Mem.clear(false);
            boolean result1 = Credential.stringEquals_original(s1.get(i), s2);
            observations[i] = Mem.instrCost;
        }
        System.out.println("observations: " + Arrays.toString(observations));

    		PartitionSet clusters = PartitionSet.createFromObservations(epsilon, observations, clusterAlgorithm);
    		Kelinci.setObserverdClusters(clusters.getClusterAverageValues(), clusters.getMinimumDeltaValue());

    		System.out.println("Done.");
    }

}
