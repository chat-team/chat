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
@WebServlet(name = "QueryGroupInfo", urlPatterns = {"/querygroupinfo"})
public class QueryGroupInfo extends HttpServlet {
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
        String sql = "SELECT * FROM group_info WHERE groupid = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        String admin = null, groupid = null, description = null, groupname = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, targetid);
            rs = ps.executeQuery();
            if (rs.next()) {
                groupid = rs.getString("groupid");
                description = rs.getString("description");
                admin = rs.getString("admin");
                groupname = rs.getString("groupname");
            }
            writer.add("status", "success");
            writer.add("groupid", groupid);
            writer.add("admin", admin);
            writer.add("groupname", groupname);
            writer.add("description", description).write();
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
