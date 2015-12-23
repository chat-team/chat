package chat.ws;

import chat.DatabaseConnection;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * Created by Dongfang on 2015/12/21.
 */

@ServerEndpoint(
        value = "/chat/group",
        configurator = SessionConfig.class,
        encoders = { MessageEncoder.class },
        decoders = { MessageDecoder.class }
)
public class GroupChat {

    private static Map<String, HttpSession> httpSessionMap = new Hashtable<>();     // <WebSecokSessionID, HttpSession>
    private static Map<String, Session> sessionMap = new Hashtable<>();             // <UserID, WebSecokSessionID>

    @OnOpen
    public void onOpen(Session ws, EndpointConfig config){
        HttpSession httpSession = (HttpSession)config.getUserProperties().get(HttpSession.class.getName());
        if (httpSession == null) {
            this.error(ws, new Exception("Not a valid WebSocket connection.")); // not a valid connection.
        }
        else {
            ws.setMaxTextMessageBufferSize(600);
            String userid = (String) httpSession.getAttribute("userid");
            httpSessionMap.put(ws.getId(), httpSession);
            sessionMap.put(userid, ws);
            System.out.println("User " + userid + " enter.");
        }
    }

    @OnMessage
    public void onMessage(Session ws, Message message) {
        HttpSession httpSession = httpSessionMap.get(ws.getId());
        String sender = (String)httpSession.getAttribute("userid");
        message.setSender(sender);
        message.setGroup(message.getTarget());
        message.insertToDB();
        this.makeRecord(message.getTarget(), message.getID());
        System.out.println("WebSocket get: " + message);
        try {
            broadcast(message, message.getTarget());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session ws) {
        String userid = (String)httpSessionMap.get(ws.getId()).getAttribute("userid");
        System.out.println("User " + userid + " leave");
        httpSessionMap.remove(ws.getId());
        sessionMap.remove(userid);
    }

    @OnError
    public void error(Session ws, java.lang.Throwable throwable) {
        String userid = (String)httpSessionMap.get(ws.getId()).getAttribute("userid");
        System.err.println("User " + userid + " error: " + throwable.getMessage());
        this.onClose(ws);
    }

    private void makeRecord(String groupid, int messageid) {
        DatabaseConnection dbConn = new DatabaseConnection();
        Connection conn = dbConn.getConnection();
        String sql = "insert into group_record (groupid, messageid) values (?, ?)";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, groupid);
            ps.setInt(2, messageid);
            ps.executeUpdate();
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

    private void broadcast(Message message, String groupid) throws EncodeException{
        DatabaseConnection dbConn = new DatabaseConnection();
        Connection conn = dbConn.getConnection();
        String sql = "SELECT userid FROM group_belong WHERE groupid = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, groupid);
            rs = ps.executeQuery();
            while (rs.next()) {
                String user = rs.getString("userid");
                if (sessionMap.containsKey(user)) {
                    this.reply(sessionMap.get(user), message, user);
                    //message.updateState(true);
                }
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

    private void reply(Session response, Message message, String targetid) {
        System.out.println("Reply to: " + targetid + " " + message);
        RemoteEndpoint.Basic remote = response.getBasicRemote();
        try {
            remote.sendObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (EncodeException e) {
            e.printStackTrace();
        }
    }

}
