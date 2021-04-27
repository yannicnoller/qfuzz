import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.ws.security.WSSecurityException;
import org.apache.wss4j.binding.wss10.PasswordString;

import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.quantification.PartitionAlgorithm;
import edu.cmu.sv.kelinci.quantification.PartitionSet;
import edu.cmu.sv.kelinci.quantification.KDynamic;
import edu.cmu.sv.kelinci.quantification.Greedy;

public class ClusterComparison {

	/* Maximum number of different observations. */
	public final static int K = 100;

	/* Minimum distance between clusters. */
	public final static double epsilon = 1.0;

	////////////////////////////////////////////////////////////////////////

	public static final int MAX_PASSWORD_LENGTH = 16; // bytes

	public static void main(String[] args) {
		
		String pathToApacheWSS4JGreedyResults = args[0]; 

		PartitionAlgorithm greedy = new Greedy(false);
		PartitionAlgorithm kdynamic = new KDynamic();
		long startTime;
		int rep = 1;

		int fileCounter = 0;
		List<Double> executionTimeGreedyList = new ArrayList<>();
		double executionTimeGreedy = 0L;
		List<Double> executionTimeKDynamicList = new ArrayList<>();
		double executionTimeKDynamic = 0L;

		for (int seed = 1; seed <= 5; seed++) {
			for (int run = 1; run <= 30; run++) {

//				String folder = "/Users/yannic/experiments/qfuzz-new-results/apache_wss4j_KGreedy_" + seed
//						+ "/fuzzer-out-" + run + "/afl/queue/";
				String folder = pathToApacheWSS4JGreedyResults + "/apache_wss4j_KGreedy_" + seed
						+ "/fuzzer-out-" + run + "/afl/queue/";
				File f = new File(folder);
				String[] inputFiles = f.list();

				for (String file : inputFiles) {

					String fileName = folder + file;
					if (fileName.endsWith(".state")) {
						continue;
					}
					
					
					System.out.println(fileName);
					fileCounter++;

					String validPassword_public;
					String[] storedPassword_secret = new String[K];

					// Read all inputs.
					try (FileInputStream fis = new FileInputStream(fileName)) {

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

					PasswordString passwordType = new PasswordString();
					passwordType.setValue(validPassword_public);

					long[] observations = new long[K];
					Mem.clear(true);
					for (int i = 0; i < K; i++) {
						Mem.clear(false);
						try {
							SimplifiedUsernameTokenValidatorImpl.verifyPlaintextPassword(passwordType,
									storedPassword_secret[i]);
						} catch (WSSecurityException e) {
						}
						observations[i] = Mem.instrCost;
					}

//					startTime = System.currentTimeMillis();
					startTime = System.nanoTime();
					for (int i=0; i<rep; i++) {
						PartitionSet.createFromObservations(epsilon, observations, greedy);
					}
//					double greedyTime = (System.currentTimeMillis() - startTime) / (double) rep;
//					double greedyTime = (System.currentTimeMillis() - startTime);
					double greedyTime = (System.nanoTime() - startTime);
					
					executionTimeGreedyList.add(greedyTime);
					executionTimeGreedy += greedyTime;

//					startTime = System.currentTimeMillis();
					startTime = System.nanoTime();
					for (int i=0; i<rep; i++) {
						PartitionSet.createFromObservations(epsilon, observations, kdynamic);	
					}
//					double dynamicTime = (System.currentTimeMillis() - startTime) / (double) rep;
//					double dynamicTime = (System.currentTimeMillis() - startTime);
					double dynamicTime = (System.nanoTime() - startTime);
					executionTimeKDynamicList.add(dynamicTime);
					executionTimeKDynamic += dynamicTime;

				}

			}

		}

		System.out.println();
		System.out.println("#files=" + fileCounter);
		System.out.println("rep=" + rep);
		System.out.println();
		System.out.println("timeGreedy=" + executionTimeGreedy);
		System.out.println("timeKDynamic=" + executionTimeKDynamic);
		System.out.println();
		System.out.println("avgTimeGreedy=" + (executionTimeGreedy / fileCounter));
		System.out.println("avgTimeKDynamic=" + (executionTimeKDynamic / fileCounter));
		System.out.println();
		System.out.println("executionTimeGreedyList=" + Arrays.toString(executionTimeGreedyList.toArray()));
		System.out.println("executionTimeKDynamicList=" + Arrays.toString(executionTimeKDynamicList.toArray()));
	}

}
