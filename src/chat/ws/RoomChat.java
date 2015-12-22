package chat.ws;

import chat.DatabaseConnection;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * Created by Dongfang on 2015/12/17.
 */
@ServerEndpoint(
        value = "/chat/room",
        configurator = SessionConfig.class,
        encoders = { MessageEncoder.class },
        decoders = { MessageDecoder.class }
)
public class RoomChat {

    private static Map<String, HttpSession> httpSessionMap = new Hashtable<>();     // <WebSecokSessionID, HttpSession>
    private static Map<String, Session> sessionMap = new Hashtable<>();             // <UserID, WebSecokSessionID>

    @OnOpen
    public void onOpen(Session ws, EndpointConfig config){
        HttpSession httpSession = (HttpSession)config.getUserProperties().get(HttpSession.class.getName());
        if (httpSession == null) {
            this.error(ws, new Exception("Not a valid WebSocket connection.")); // not a valid connection.
        }
        else {
            String userid = (String) httpSession.getAttribute("userid");
            httpSessionMap.put(ws.getId(), httpSession);
            sessionMap.put(userid, ws);
            System.out.println("User " + userid + " enter.");
        }
    }

    @OnMessage
    public void onMessage(Message message, Session ws)
            throws IOException, EncodeException {
        HttpSession httpSession = httpSessionMap.get(ws.getId());
        String sender = (String)httpSession.getAttribute("userid");
        message.setSender(sender);
        message.setRoom(message.getTarget());
        message.insertToDB();
        System.out.println("WebSocket get: " + message);
        try {
            broadcast(message, message.getTarget());
        }catch (Exception e) {
            e.printStackTrace();
        }
        if (message.getStatus().equals("enter")) {
            ChangeRoomStatus(sender, message.getTarget(), 0);
        }
        if (message.getStatus().equals("exit")) {
            ChangeRoomStatus(sender, message.getTarget(), 1);
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
    public void error(Session session, java.lang.Throwable throwable){
        System.err.println("Guest" + session.getId() + " error: " + throwable);
        onClose(session);
    }

    private void broadcast(Message message, String roomid) throws EncodeException{
        DatabaseConnection dbConn = new DatabaseConnection();
        Connection conn = dbConn.getConnection();
        String sql = "SELECT userid FROM room_status WHERE roomid = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, roomid);
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

    private void ChangeRoomStatus(String userid, String roomid, int status) {
        // status == 0 enter; status == 1 exit;
        DatabaseConnection dbConn = new DatabaseConnection();
        Connection conn = dbConn.getConnection();
        String sql;
        if (status == 0) {
            sql = "insert into room_status (userid, roomid) values (?, ?)";
        }
        else {
            sql = "DELETE FROM room_status WHERE userid = ? AND roomid = ?";
        }
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, userid);
            ps.setString(2, roomid);
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

}
