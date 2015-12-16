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

/**
 * Created by Dongfang on 2015/12/15.
 */
@WebServlet(name = "Modify")
public class Modify extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ReqReader reader = new ReqReader(request.getInputStream());
        ResWriter writer = new ResWriter(response.getOutputStream());
        String username = reader.getString("userid");
        String password = reader.getString("passwd");
        String newpassword = reader.getString("newpasswd");
        String nickname = reader.getString("nickname");
        String email = reader.getString("email");

        if (username == "" || password == "" || nickname == "") {
            writer.add("status", "failed").write();
            return;
        }

        DatabaseConnection dbConn = new DatabaseConnection();
        Connection conn = dbConn.getConnection();
        String sql = "SELECT * FROM user_info WHERE userid = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setObject(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                if (CipherUtil.checkPassword(password, rs.getString("passwd"))) {
                    writer.add("status", "success");
                }
                else {
                    writer.add("status", "failed").write();
                    return;
                }
            }
            else {
                writer.add("status", "error").write();
                return;
            }
            if (newpassword.equals("")) {
                sql = "UPDATE user_info set nickname = ?, email = ? WHERE userid = ?";
                ps = conn.prepareStatement(sql);
                ps.setObject(3, username);
                ps.setObject(1, nickname);
                ps.setObject(2, email);
                ps.executeUpdate();
            }else{
                newpassword = CipherUtil.encoderByMd5(newpassword);
                sql = "UPDATE user_info set passwd = ?, nickname = ?, email = ? WHERE userid = ?";
                ps = conn.prepareStatement(sql);
                ps.setObject(4, username);
                ps.setObject(2, nickname);
                ps.setObject(1, newpassword);
                ps.setObject(3, email);
                ps.executeUpdate();
            }
            writer.add("modify", "success").write();
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
