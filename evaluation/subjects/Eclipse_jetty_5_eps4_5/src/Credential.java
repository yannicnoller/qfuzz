import java.io.Serializable;

public class Credential {

	public static boolean stringEquals_original(String known, String unknown) {
		byte[] digesta = known.getBytes();
		byte[] digestb = unknown.getBytes();

		if (digesta == digestb)
			return true;
		if (digesta == null || digestb == null) {
			return false;
		}

		int lenA = digesta.length;
		int lenB = digestb.length;

		if (lenB == 0) {
			return lenA == 0;
		}

		int result = 0;
		result |= lenA - lenB;

		// time-constant comparison
		for (int i = 0; i < lenA; i++) {
			// If i >= lenB, indexB is 0; otherwise, i.
			int indexB = ((i - lenB) >>> 31) * i;
			result |= digesta[i] ^ digestb[indexB];
		}
		return result == 0;
	}
}
