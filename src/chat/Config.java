package chat;

import java.util.Objects;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * Created by He Tao on 2015/12/15.
 */
public class Config {
    private static Properties prop = new Properties();
    static {
        InputStream in = Config.class.getResourceAsStream("/chat.properities");
        try {
            prop.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Config() {

    }

    public static String getConfig(String key) {
        return prop.getProperty(key).trim();
    }
}
