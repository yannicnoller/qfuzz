import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcConnectionPool;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.quantification.PartitionAlgorithm;
import edu.cmu.sv.kelinci.quantification.PartitionSet;
import edu.cmu.sv.kelinci.quantification.Greedy;

/*
* Username might be in database or not Encoding/Hashing of given password is expensive. The unsafe version does
* encoding only if username is in database.
*
* Password is actually not relevant here, but we would use the same password for both executions.
*/
public class Driver_Greedy {

	/* Maximum number of different observations. */
	public final static int K = 100;

	/* Minimum distance between clusters. */
	public final static double epsilon = 1.0;

	/* Cluster Algorithm */
	public static PartitionAlgorithm clusterAlgorithm = new Greedy(false);

	////////////////////////////////////////////////////////////////////////

    private static final int USERNAME_MAX_LENGTH = 5; // # of characters
    private static final int PASSWORD_MAX_LENGTH = 20;

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Expects file name as parameter");
			return;
		}

		String public_user;
		String public_pw;
		String[] secret_user = new String[K];

		// Read all inputs.
		try (FileInputStream fis = new FileInputStream(args[0])) {

			/* Read public value for public_user */
			byte[] bytes = new byte[USERNAME_MAX_LENGTH];
			if (fis.read(bytes) < 0) {
				throw new RuntimeException("Not enough input data...");
			}
			char[] tmp = new char[bytes.length];
			for (int i = 0; i < bytes.length; i++) {
				tmp[i] = (char) (bytes[i] % 128);
			}
			public_user = new String(tmp);
			
			/* Read public value for public_user */
			bytes = new byte[PASSWORD_MAX_LENGTH];
			if (fis.read(bytes) < 0) {
				throw new RuntimeException("Not enough input data...");
			}
			tmp = new char[bytes.length];
			for (int i = 0; i < bytes.length; i++) {
				tmp[i] = (char) (bytes[i] % 128);
			}
			public_pw = new String(tmp);

			/* Generate secrets. */
			for (int i = 0; i < K; i++) {

				bytes = new byte[USERNAME_MAX_LENGTH];
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
				secret_user[i] = new String(tmp);
			}

		} catch (IOException e) {
			System.err.println("Error reading input");
			e.printStackTrace();
			return;
		}

		System.out.println("public_user = " + public_user);
		System.out.println("public_pw = " + public_pw);
		for (int i = 0; i < K; i++) {
			System.out.println("secret_user " + i + " = " + secret_user[i]);
		}
		
        /* Create Connection to database. */
        DataSource ds = JdbcConnectionPool.create("jdbc:h2:~/pac4j-fuzz", "sa", "");
        DBI dbi = new DBI(ds);

        DbAuthenticator dbAuth = new DbAuthenticator();
        dbAuth.dbi = dbi;
        
        /* Prepare database. */
        Handle h = dbi.open();
        try {
            String processedPW = dbAuth.getPasswordEncoder().encode(public_pw);
            h.execute("delete from users");
            h.execute("insert into users (id, username, password) values (1, ?, ?)", public_user, processedPW); // add user
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            h.close();
        }

		long[] observations = new long[K];
		Mem.clear(true);
		for (int i = 0; i < K; i++) {
			UsernamePasswordCredentials cred_secr = new UsernamePasswordCredentials(secret_user[i], public_pw, ""); // public info
			Mem.clear(false);
			try {
				dbAuth.validate_unsafe(cred_secr);
			} catch (Exception e) {
			}
			observations[i] = Mem.instrCost;
		}
		System.out.println("observations: " + Arrays.toString(observations));

		PartitionSet clusters = PartitionSet.createFromObservations(epsilon, observations, clusterAlgorithm);
		Kelinci.setObserverdClusters(clusters.getClusterAverageValues(), clusters.getMinimumDeltaValue());

        /* Clean database. */
        h = dbi.open();
        try {
            h.execute("delete from users");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            h.close();
        }

		System.out.println("Done.");
	}

}
