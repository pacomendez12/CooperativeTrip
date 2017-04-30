package mx.iteso.pam2017.a705164.cooperativetrip;

/**
 * Created by paco on 29/04/2017.
 */

public class AuthenticationManager {
    private static String key = "2143c0cb1fb4d3849400eade3bfc37re";

    public static String getKey() { return key; }
    public static void setKey(String key) { AuthenticationManager.key = key; }
}
