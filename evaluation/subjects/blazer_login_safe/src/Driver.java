import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;

public class Driver {

    public static final int MAX_PASSWORD_LENGTH = 16; // bytes

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Expects file name as parameter");
            return;
        }

        /* Read input. */
        String username = "username"; // irrelevant
        byte[] guess;
        byte[] realpassword_secret1;
        byte[] realpassword_secret2;

        try (FileInputStream fis = new FileInputStream(args[0])) {

            /* Read all data. */
            int i = 0;
            int value;
            List<Byte> values = new ArrayList<>();
            while (((value = fis.read()) != -1) && (i < 3 * MAX_PASSWORD_LENGTH)) {
                values.add((byte) (value % 127));
                i++;
            }
            /* input must be non-empty */
            if (i < 3) {
                throw new RuntimeException("not enough data!");
            }

            int eachSize = values.size() / 3;

            guess = new byte[eachSize];
            for (i = 0; i < eachSize; i++) {
                guess[i] = values.get(i);
            }

            realpassword_secret1 = new byte[eachSize];
            for (i = 0; i < eachSize; i++) {
                realpassword_secret1[i] = values.get(i + eachSize);
            }

            realpassword_secret2 = new byte[eachSize];
            for (i = 0; i < eachSize; i++) {
                realpassword_secret2[i] = values.get(i + 2 * eachSize);
            }

        } catch (IOException e) {
            System.err.println("Error reading input");
            e.printStackTrace();
            return;
        }

        System.out.println("username=" + username);
        System.out.println("password=" + Arrays.toString(guess));
        System.out.println("secret1=" + Arrays.toString(realpassword_secret1));
        System.out.println("secret2=" + Arrays.toString(realpassword_secret2));

        Mem.clear(true);
        //boolean valid1 = Login.login_unsafe(realpassword_secret1, guess, username);
        boolean valid1 = Login.login_safe(realpassword_secret1, guess, username);
        long cost1 = Mem.instrCost;
        System.out.println("valid1=" + valid1);
        System.out.println("cost1=" + cost1);

        Mem.clear(false);
//        boolean valid2 = Login.login_unsafe(realpassword_secret2, guess, username);
        boolean valid2 = Login.login_safe(realpassword_secret2, guess, username);
        long cost2 = Mem.instrCost;
        System.out.println("valid2=" + valid2);
        System.out.println("cost2=" + cost2);

        long diff = Math.abs(cost1 - cost2);
        Kelinci.addCost(diff);
        System.out.println("diff=" + diff);

        System.out.println("Done.");
    }

}
