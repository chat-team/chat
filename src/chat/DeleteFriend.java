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

/**
 * Created by Dongfang on 2015/12/26.
 */
@WebServlet(name = "DeleteFriend", urlPatterns = {"/deletefriend"})
public class DeleteFriend extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ReqReader reader = new ReqReader(request.getInputStream());
        ResWriter writer = new ResWriter(response.getOutputStream());

        String username, targetid;
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") != null) {
            username = (String)session.getAttribute("userid");
        }
        else {
            response.setHeader("Location", "/");
            response.setStatus(401);
            return; // no valid userid.
        }

        targetid = reader.getString("targetid");

        if (username == "" || targetid == "" || username == targetid) {
            writer.add("status", "failed");
            writer.add("message", "invalid input").write();
            return;
        }

        String useraid = username.compareTo(targetid) > 0 ? targetid : username;
        String userbid = username.compareTo(targetid) > 0 ? username : targetid;

        DatabaseConnection dbConn = new DatabaseConnection();
        Connection conn = dbConn.getConnection();
        String sql = "DELETE FROM friend WHERE useraid = ? AND userbid = ?";
        PreparedStatement ps = null;
        int rs = 0;
        try {
            ps = conn.prepareStatement(sql);
            ps.setObject(1, useraid);
            ps.setObject(2, userbid);
            rs = ps.executeUpdate();
            if (rs == 0) {
                writer.add("status", "failed");
                writer.add("message", "no such user or already delete");
            }
            else {
                writer.add("status", "success");
            }
            writer.write();
        } catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
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
