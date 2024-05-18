import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Config {
    private static String token;
    private static String channel;

    public static void loadConfig(String filename) throws IOException {
        try (FileInputStream fis = new FileInputStream(filename)) {
            JSONTokener tokener = new JSONTokener(fis);
            JSONObject jsonObject = new JSONObject(tokener);

            token = jsonObject.getString("token");
            channel = jsonObject.getString("channel");

        }
    }

    public static String getToken() {
        return token;
    }

    public static String getChannel() {
        return channel;
    }
}

