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
@WebServlet(name = "MemberOfRoom", urlPatterns = {"/memberofroom"})
public class MemberOfRoom extends HttpServlet {
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

        if (username == "" ) {
            writer.add("status", "failed").write();
            return;
        }

        targetid = reader.getString("targetid");

        DatabaseConnection dbConn = new DatabaseConnection();
        Connection conn = dbConn.getConnection();
        String sql = "SELECT room_status.userid, user_info.nickname FROM room_status, user_info WHERE room_status.userid = user_info.userid and roomid = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setObject(1,targetid );
            rs = ps.executeQuery();
            ArrayList<Map<String, String>> array = new ArrayList<>();
            while (rs.next()) {
                Map<String, String> m = new TreeMap<>();
                m.put("userid", rs.getString("userid"));
                m.put("nickname", rs.getString("nickname"));
                array.add(m);
            }
            writer.add("status", "success");
            writer.add("member", array).write();
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
