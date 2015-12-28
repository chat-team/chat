package chat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by Dongfang on 2015/12/15.
 */
@WebServlet(name = "ModifyPass", urlPatterns = {"/modifypass"})
public class ModifyPass extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ReqReader reader = new ReqReader(request.getInputStream());
        ResWriter writer = new ResWriter(response.getOutputStream());

        String username;
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") != null) {
            username = (String)session.getAttribute("userid");
        }
        else {
            response.setHeader("Location", "/");
            response.setStatus(401);
            return; // no valid userid.
        }

        String password = reader.getString("passwd");
        String newpassword = reader.getString("newpasswd");

        if (username == "" || password == "") {
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
                    // DO NOTHING.
                }
                else {
                    writer.add("status", "failed");
                    writer.add("message", "no such user").write();
                    return;
                }
            }
            else {
                writer.add("status", "error");
                writer.add("message", "wrong old password").write();
                return;
            }
            newpassword = CipherUtil.encoderByMd5(newpassword);
            sql = "UPDATE user_info set passwd = ? WHERE userid = ?";
            ps = conn.prepareStatement(sql);
            ps.setObject(1, newpassword);
            ps.setObject(2, username);
            ps.executeUpdate();
            writer.add("status", "success").write();
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
