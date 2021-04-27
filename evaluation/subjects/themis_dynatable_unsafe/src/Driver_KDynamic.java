import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import com.google.gwt.sample.dynatable.client.Person;
import com.google.gwt.sample.dynatable.client.SchoolCalendarService;

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

	private static final int MAX_INT_VALUE = 100;

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Expects file name as parameter");
			return;
		}

		int startIndex, maxCount; // public
		int[] peopleLength = new int[K]; // secret

        /* Read input file. */
        try (FileInputStream fis = new FileInputStream(args[0])) {
            int value;

            if ((value = fis.read()) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            startIndex = Math.abs(value) % MAX_INT_VALUE;

            if ((value = fis.read()) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            maxCount = Math.abs(value) % MAX_INT_VALUE;

            for (int i=0; i<K; i++) {
                if ((value = fis.read()) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                peopleLength[i] = Math.abs(value) % MAX_INT_VALUE;
            }

        } catch (IOException e) {
            System.err.println("Error reading input");
            e.printStackTrace();
            return;
        }
        
        System.out.println("startIndex=" + startIndex);
        System.out.println("maxCount=" + maxCount);
        for (int i=0; i<K; i++) {
        	System.out.println("peopleLength[" + i + "]=" + peopleLength[i]);	
        }

		long[] observations = new long[K];
		Mem.clear(true);
		for (int i = 0; i < K; i++) {
			SchoolCalendarService ser = new SchoolCalendarServiceImpl(peopleLength[i]);
			Mem.clear(false);
			Person[] res = ser.getPeople(startIndex, maxCount);
			observations[i] = res.length;
		}
		System.out.println("observations: " + Arrays.toString(observations));

		PartitionSet clusters = PartitionSet.createFromObservations(epsilon, observations, clusterAlgorithm);
		Kelinci.setObserverdClusters(clusters.getClusterAverageValues(), clusters.getMinimumDeltaValue());

		System.out.println("Done.");
	}

}
