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
@WebServlet(name = "Register", urlPatterns = {"/register"})
public class Register extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ReqReader reader = new ReqReader(request.getInputStream());
        ResWriter writer = new ResWriter(response.getOutputStream());
        String password = reader.getString("passwd");
        String nickname = reader.getString("nickname");
        String email = reader.getString("email");

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
        String sql = "set @p0 = ?";
        ResultSet rs = null;
        PreparedStatement ps = null;
        int username = 0;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, nickname);
            ps.execute();
            sql = "set @p1 = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, password);
            ps.execute();
            sql = "set @p2 = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.execute();
            sql = "call add_user(@p0, @p1, @p2)";
            rs = ps.executeQuery();
            if (rs.next()) {
                username = rs.getInt("userid");
            }
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

}
