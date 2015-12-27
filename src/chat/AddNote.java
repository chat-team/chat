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
 * Created by Dongfang on 2015/12/26.
 */
@WebServlet(name = "AddNote", urlPatterns = {"/addnote"})
public class AddNote extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ReqReader reader = new ReqReader(request.getInputStream());
        ResWriter writer = new ResWriter(response.getOutputStream());

        String username, targetid, content;
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
        content = reader.getString("content");

        if (username == "" || targetid == "") {
            writer.add("status", "failed");
            writer.add("message", "invalid input").write();
            return;
        }

        DatabaseConnection dbConn = new DatabaseConnection();
        Connection conn = dbConn.getConnection();
        String sql = "SET @p0 = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        int noteid;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, content);
            ps.execute();
            sql = "SET  @p1 = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.execute();
            sql = "CALL add_note(@p0, @p1)";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                noteid = rs.getInt("noteid");
            }
            else {
                writer.add("status", "failed").write();
                return;
            }
            sql = "INSERT INTO note_belong(boardid, noteid) VALUES (?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, targetid);
            ps.setInt(2, noteid);
            ps.executeUpdate();
            writer.add("status", "success");
            writer.add("noteid", noteid).write();
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
