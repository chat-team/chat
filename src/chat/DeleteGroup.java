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
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Dongfang on 2015/12/26.
 */
@WebServlet(name = "DeleteGroup", urlPatterns = {"/deletegroup"})
public class DeleteGroup extends HttpServlet {
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
        DatabaseConnection dbConn = new DatabaseConnection();
        Connection conn = dbConn.getConnection();
        String sql = "SELECT admin FROM group_info WHERE groupid = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        String admin = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setObject(1, targetid);
            rs = ps.executeQuery();
            if (rs.next()) {
                admin = rs.getString("admin");
            }
            if (username.equals(admin)) {
                sql = "DELETE FROM group_info WHERE groupid = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, targetid);
                ps.executeUpdate();
                writer.add("status", "success");
            }
            else {
                sql = "DELETE FROM group_belong WHERE userid = ? AND groupid = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, username);
                ps.setString(2, targetid);
                ps.executeUpdate();
                writer.add("status", "success");
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
