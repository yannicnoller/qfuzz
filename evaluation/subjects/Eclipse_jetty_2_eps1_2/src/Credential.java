import java.io.Serializable;

public class Credential {

    public static boolean stringEquals_original(String s1, String s2) {
      if (s1 == s2)
        return true;
      if (s1 == null || s2 == null || s1.length() != s2.length())
        return false;
      boolean result = true;
      for (int i = 0; i < s1.length(); ++i)
        result &= (s1.charAt(i) == s2.charAt(i));
      return result;
    }
}
