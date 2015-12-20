package chat;

import com.sun.deploy.net.HttpRequest;

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

/**
 * Created by He Tao on 2015/12/20.
 */
@WebServlet(name = "MakeFriends", urlPatterns = {"/makefriends"})
public class MakeFriends extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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
        String sql = "INSERT INTO friend (useraid, userbid)\n" +
                "SELECT * from (select ?, ?) as tmp \n" +
                "where EXISTS (\n" +
                "SELECT * FROM user_info WHERE userid=?\n" +
                ")\n" +
                "and not exists (\n" +
                "SELECT * FROM friend where friend.useraid = ? and friend.userbid = ?\n" +
                ") LIMIT 1;";
        PreparedStatement ps = null;
        int rs = 0;
        try {
            ps = conn.prepareStatement(sql);
            ps.setObject(1, useraid);
            ps.setObject(2, userbid);
            ps.setObject(3, userbid);
            ps.setObject(4, useraid);
            ps.setObject(5, userbid);
            rs = ps.executeUpdate();
            if (rs == 0) {
                writer.add("status", "failed");
                writer.add("message", "no such user or already friends");
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
