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
 * Created by Dongfang on 2015/12/28.
 */
@WebServlet(name = "QueryRoomInfo", urlPatterns = {"/queryroominfo"})
public class QueryRoomInfo extends HttpServlet {
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

        if (username == "" || targetid == "") {
            writer.add("status", "failed").write();
            return;
        }

        DatabaseConnection dbConn = new DatabaseConnection();
        Connection conn = dbConn.getConnection();
        String sql = "SELECT * FROM chatroom WHERE roomid = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        String roomid = null, discription = null, roomname = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, targetid);
            rs = ps.executeQuery();
            if (rs.next()) {
                roomid = rs.getString("roomid");
                discription = rs.getString("discription");
                roomname = rs.getString("roomname");
            }
            writer.add("status", "success");
            writer.add("roomid", roomid);
            writer.add("roomname", roomname);
            writer.add("discription", discription).write();
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
