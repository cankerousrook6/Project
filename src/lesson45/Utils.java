package lesson45;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    public static Map<String, String> parseUrlEncoded(String raw) {

        Map<String, String> result = new HashMap<>();
        String delimiter = raw.contains("&") ? "&" : ";";
        String[] pairs = raw.split(delimiter);

        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);

            if (parts.length == 2) {
                String key = decode(parts[0].trim());
                String value = decode(parts[1].trim());
                result.put(key, value);

            }

        }
        return result;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}