package chat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by Dongfang on 2015/12/15.
 */
@WebServlet(name = "Login")
public class Login extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ReqReader reader = new ReqReader(request.getInputStream());
        ResWriter writer = new ResWriter(response.getOutputStream());
        String username  = reader.getString("userid");
        String password  = reader.getString("passwd");

        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(60);

        if (password == "" || username == "") {
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
                    session.setAttribute("userid", username);
                    writer.add("status", "success").write();
                }
                else {
                    writer.add("status", "failed").write();
                }
            }
            else {
                writer.add("status", "error").write();
            }
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
