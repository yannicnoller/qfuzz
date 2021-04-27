import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
//
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

	public static final int PASSWORD_LENGTH = 128; // characters

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Expects file name as parameter");
			return;
		}

		byte[] public_guess;
		byte[][] secrets = new byte[K][PASSWORD_LENGTH];

		// Read all inputs.
		List<Byte> public_value_list = new ArrayList<>();
		try (FileInputStream fis = new FileInputStream(args[0])) {
			
			// First k * 128 bytes for the secrets.
			for (int i = 0; i < secrets.length; i++) {

				/* Read another secret. */
				secrets[i] = new byte[PASSWORD_LENGTH];
				if (fis.read(secrets[i]) < 0) {
					throw new RuntimeException("Not enough input data...");
				}
				
			}
			
			// Then read the remaining bytes for the public image.
            byte[] bytes = new byte[1];
            while ((fis.read(bytes) != -1)) {
            	public_value_list.add(bytes[0]);
            }
			if (public_value_list.size() < 1) {
				throw new RuntimeException("Not enough input data...");
			}
            public_guess = new byte[public_value_list.size()];
            for (int i = 0; i < public_guess.length; i++) {
                public_guess[i] = public_value_list.get(i);
            }

		} catch (IOException e) {
			System.err.println("Error reading input");
			e.printStackTrace();
			return;
		}

		System.out.println("public_guess=" + Arrays.toString(public_guess));
		for (int i = 0; i < secrets.length; i++) {
			System.out.println("secret" + i + "=" + Arrays.toString(secrets[i]));
		}

		long[] observations = new long[K];
		Mem.clear(true);
		for (int i = 0; i < K; i++) {
			Mem.clear(false);
			ImageMatcherWorker.test(public_guess, secrets[i]);
			observations[i] = Mem.instrCost;
		}
		System.out.println("observations: " + Arrays.toString(observations));

		PartitionSet clusters = PartitionSet.createFromObservations(epsilon, observations, clusterAlgorithm);
		Kelinci.setObserverdClusters(clusters.getClusterAverageValues(), clusters.getMinimumDeltaValue());

		System.out.println("Done.");

	}

	public static byte[] extractBytes(String ImageName) {
		try {
			byte[] imageInByte;
			BufferedImage originalImage = ImageIO.read(new File(ImageName));

			// convert BufferedImage to byte array
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(originalImage, "jpg", baos);
			baos.flush();
			imageInByte = baos.toByteArray();
			baos.close();
			return (imageInByte);
		} catch (IOException e) {
			e.printStackTrace();
			return (null);
		}
	}
	
	public static byte[] createImagePasscode(byte[] image) {
		try {
			System.out.println("Creating passcode");
			ScalrApplyTest b = new ScalrApplyTest();
			ScalrApplyTest.setup(image);
			BufferedImage p = b.testApply1();
			int r = p.getWidth();
			int h = p.getHeight();
			int[] imageDataBuff = p.getRGB(0, 0, r, h, (int[]) null, 0, r);
			ByteBuffer byteBuffer = ByteBuffer.allocate(imageDataBuff.length * 4);
			IntBuffer intBuffer = byteBuffer.asIntBuffer();
			intBuffer.put(imageDataBuff);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.write(byteBuffer.array());
			baos.flush();
			baos.close();
			System.out.println("Image Done");
			ScalrApplyTest.tearDown();
			byte[] pcodetest = new byte[128];
			int csize = imageDataBuff.length / 128;
			int ii = 0;

			for (int i1 = 0; i1 < csize * 128; i1 += csize) {
				pcodetest[ii] = (byte) (imageDataBuff[i1] % 2);
				++ii;
			}
			return (pcodetest);
		} catch (Exception var15) {
			System.out.println("worker ended, error: " + var15.getMessage());
			return (null);
		}
	}

	public static void firstInput(String ImageName) {
		byte[] image = extractBytes(ImageName);

		byte[] passcode = new byte[128];
		new Random().nextBytes(passcode); // createImagePasscode(image);
		byte[] passcode2 = new byte[128];
		new Random().nextBytes(passcode2);
		try (FileOutputStream fos = new FileOutputStream("in_dir/example.txt")) {
			fos.write(passcode);
			fos.write(passcode2);
			fos.write(image);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	public static void main(String[] args) {
//		firstInput("image.jpeg");
//	}
	
}
