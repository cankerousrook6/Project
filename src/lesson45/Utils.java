package lesson45;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    public static Map<String, String> parseUrlEncoded(String raw) {

        Map<String, String> result = new HashMap<>();
        String[] pairs = raw.split("&");

        for (String pair : pairs) {
            String[] parts = pair.split("=");

            if (parts.length == 2) {
                String key = decode(parts[0]);
                String value = decode(parts[1]);
                result.put(key, value);

            }

        }
        return result;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}