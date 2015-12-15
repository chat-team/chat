package chat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

/**
 * Created by Dongfang on 2015/12/15.
 */
@WebServlet(name = "Register")
public class Register extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ReqReader reader = new ReqReader(request.getInputStream());
        ResWriter writer = new ResWriter(response.getOutputStream());
        String password = reader.getString("passwd");
        String nickname = reader.getString("nickname");
        String email = reader.getString("email");

        response.setContentType("text/html");

        if (password == "" || nickname == "") {
            writer.add("status", "failed").write();
            return;
        }

        try {
            password = CipherUtil.encoderByMd5(password);
        }catch (Exception e) {
            e.printStackTrace();
        }

        DatabaseConnection dbConn = new DatabaseConnection();
        Connection conn = dbConn.getConnection();
        String sql = "SELECT COUNT(*) as rowCount FROM user_info";
        ResultSet rs = null;
        PreparedStatement ps = null, ps1 = null;
        int username = 0;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                username = rs.getInt("rowCount");
            }
            sql = "INSERT INTO user_info (userid, nickname, passwd, email) VALUES (?,?,?,?)";
            ps1 = conn.prepareStatement(sql);
            ps1.setInt(1, ++username);
            ps1.setString(2, nickname);
            ps1.setString(3, password);
            ps1.setString(4, email);
            ps1.executeUpdate();
            ps1.close();
            writer.add("status", "success");
            writer.add("username", username).write();
        } catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
}
