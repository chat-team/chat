package chat;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by Dongfang on 2015/12/15.
 */
public class DatabaseConnection {
    private String dbDriver = "com.mysql.jdbc.Driver";
    private String dbUrl = "jdbc:mysql://"
            + Config.getConfig("db.ip") + ":"
            + Config.getConfig("db.port") + "/"
            + Config.getConfig("db.name");
    private String dbUser = Config.getConfig("db.user");
    private String dbPassword = Config.getConfig("db.password");

    public Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName(dbDriver);
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

        } catch(Exception e) {
            e.printStackTrace();
        }
        return conn;
    }
}
