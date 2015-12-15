package chat;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by Dongfang on 2015/12/15.
 */
public class DatabaseConnection {
    private String dbDriver = "com.mysql.jdbc.Driver";
    private String dbUrl = "jdbc:mysql://172.31.124.197:3306/chat";
    private String dbUser = "vagrant";
    private String dbPassword = "vagrant";

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
