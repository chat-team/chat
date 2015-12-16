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
 * Created by Dongfang on 2015/12/16.
 */
@WebServlet(name = "ConstructChatroom")
public class ConstructChatroom extends HttpServlet {
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

        String roomname = reader.getString("roomname");
        String description = reader.getString("description");

        if (roomname == "") {
            writer.add("status", "failed").write();
            return;
        }

        DatabaseConnection dbConn = new DatabaseConnection();
        Connection conn = dbConn.getConnection();
        String sql = sql = "INSERT INTO chatroom (roomname, description) VALUES (?,?)";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, roomname);
            ps.setString(2, description);
            ps.executeUpdate();
            writer.add("status", "success").write();
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }
}
