import java.io.Serializable;

public class Credential {

    public static boolean stringEquals_original(String known, String unknown)
    {
        if (known == unknown)
            return true;
        if (known == null || unknown == null)
            return false;
        boolean result = true;
        int l1 = known.length();
        int l2 = unknown.length();
        for (int i = 0; i < l2; ++i)
            result &= known.charAt(i%l1) == unknown.charAt(i);
        return result && l1 == l2;
    }
}
