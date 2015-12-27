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
 * Created by Dongfang on 2015/12/22.
 */
@WebServlet(name = "AskGroupRecord", urlPatterns = {"/askgrouprecord"})
public class AskGroupRecord extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ReqReader reader = new ReqReader(request.getInputStream());
        ResWriter writer = new ResWriter(response.getOutputStream());

        String username, groupid;
        HttpSession session = request.getSession();
        if (session.getAttribute("userid") != null) {
            username = (String)session.getAttribute("userid");
        }
        else {
            response.setHeader("Location", "/");
            response.setStatus(401);
            return; // no valid userid.
        }
        groupid = reader.getString("groupid");

        if (username == "" ) {
            writer.add("status", "failed").write();
            return;
        }

        DatabaseConnection dbConn = new DatabaseConnection();
        Connection conn = dbConn.getConnection();
        String sql = "SELECT ctime, content, userid FROM group_record, message WHERE group_record.messageid = message.messageid AND groupid = ?";
        PreparedStatement ps = null;
        ResultSet rs = null, rt = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, groupid);
            rs = ps.executeQuery();
            ArrayList<Map<String, String>> array = new ArrayList<>();
            while (rs.next()) {
                Map<String, String> m = new TreeMap<>();
                m.put("userid", rs.getString("userid"));
                m.put("ctime", rs.getString("ctime"));
                m.put("content", rs.getString("content"));

                sql = "select nickname from user_info where userid = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, rs.getString("userid"));
                rt = ps.executeQuery();
                if (rt.next()) {
                    m.put("nickname", rt.getString("nickname"));
                }

                array.add(m);
            }
            writer.add("status", "success");
            writer.add("grouprecord", array).write();
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
