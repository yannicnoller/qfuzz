import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.quantification.PartitionAlgorithm;
import edu.cmu.sv.kelinci.quantification.PartitionSet;
import edu.cmu.sv.kelinci.quantification.KDynamic;

/*
 * Created a H2 database called Tomcat with USERSm ROLES, and USERs_ROLES
 * tables. Check http://www.h2database.com/html/tutorial.html for creating a db.
 * To start db: cd h2/bin, and type: java -jar h2*.jar
 *
 */
public class Driver_KDynamic {

	/* Maximum number of different observations. */
	public final static int K = 100;

	/* Minimum distance between clusters. */
	public final static double epsilon = 1.0;

	/* Cluster Algorithm */
	public static PartitionAlgorithm clusterAlgorithm = new KDynamic();

	////////////////////////////////////////////////////////////////////////

	public static final int MAX_PASSWORD_LENGTH = 16; // bytes

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Expects file name as parameter");
			return;
		}

		String public_user;
		String[] secret_users = new String[K];
		String pw = "1234";

		// Read all inputs.
		try (FileInputStream fis = new FileInputStream(args[0])) {

			/* Read public value for public_actual */
			byte[] bytes = new byte[MAX_PASSWORD_LENGTH];
			if (fis.read(bytes) < 0) {
				throw new RuntimeException("Not enough input data...");
			}
			char[] tmp = new char[bytes.length];
			for (int i = 0; i < bytes.length; i++) {
				tmp[i] = (char) (bytes[i] % 128);
			}
			public_user = new String(tmp);

			/* Generate secrets. */
			for (int i = 0; i < K; i++) {

				bytes = new byte[MAX_PASSWORD_LENGTH];
				if (fis.read(bytes) < 0) {
					throw new RuntimeException("Not enough input data...");
				}
				tmp = new char[bytes.length];
				for (int j = 0; j < bytes.length; j++) {
					byte value = bytes[j];
					/* each char value must be between 0 and 127 and a printable character */
					char charValue = (char) (value % 128);
					tmp[j] = charValue;
				}
				secret_users[i] = new String(tmp);
			}

		} catch (IOException e) {
			System.err.println("Error reading input");
			e.printStackTrace();
			return;
		}

		System.out.println("public_actual = " + public_user);
		for (int i = 0; i < K; i++) {
			System.out.println("secrets_expected " + i + " = " + secret_users[i]);
		}
		
		DataSourceRealm DSR = new DataSourceRealm();
		/* Create Connection do database. */
		Connection dbConnection = null;

		// Ensure that we have an open database connection
		dbConnection = DSR.open();
		if (dbConnection == null) {
			// If the db connection open fails, return "not authenticated"
			System.out.println("DB connection failed...");
		}
		/* Prepare database. */
		Statement st;
		try {
			st = dbConnection.createStatement();
			st.execute("delete from users;");
			st.execute("insert into users (user_name, user_pass) values ('" + public_user + "', '" + pw + "');");
		} catch (Exception e) {
			System.out.println("Could not insert user in the table...");
			throw new RuntimeException(e);
		}

		long[] observations = new long[K];
		Mem.clear(true);
		for (int i = 0; i < K; i++) {
			Mem.clear(false);
			DSR.authenticate_safe(dbConnection, secret_users[i], pw);
			observations[i] = Mem.instrCost;
		}
		System.out.println("observations: " + Arrays.toString(observations));

		PartitionSet clusters = PartitionSet.createFromObservations(epsilon, observations, clusterAlgorithm);
		Kelinci.setObserverdClusters(clusters.getClusterAverageValues(), clusters.getMinimumDeltaValue());

		/* Clean database. */
		try {
			st.execute("delete from users;");
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			DSR.close(dbConnection);
		}

		System.out.println("Done.");
	}

}
