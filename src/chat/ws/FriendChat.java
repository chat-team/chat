package chat.ws;

import chat.DatabaseConnection;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by He Tao on 2015/12/20.
 */
@ServerEndpoint(
        value = "/chat/friend",
        configurator = SessionConfig.class,
        encoders = { MessageEncoder.class },
        decoders = { MessageDecoder.class }
)

public class FriendChat {
    private static Map<String, HttpSession> httpSessionMap = new Hashtable<>();     // <WebSecokSessionID, HttpSession>
    private static Map<String, Session> sessionMap = new Hashtable<>();             // <UserID, WebSecokSessionID>

    @OnOpen
    public void start(Session ws, EndpointConfig config){
        HttpSession httpSession = (HttpSession)config.getUserProperties().get(HttpSession.class.getName());
        if (httpSession == null) {
            this.error(ws, new Exception("Not a valid WebSocket connection.")); // not a valid connection.
        }
        else {
            ws.setMaxTextMessageBufferSize(600);
            String userid = (String) httpSession.getAttribute("userid");
            httpSessionMap.put(ws.getId(), httpSession);
            sessionMap.put(userid, ws);
            for (Message m: this.queryAllUnread(userid)) {
                this.reply(ws, m, userid);
            }
            System.out.println("User " + userid + " enter.");
        }
    }

    @OnMessage
    public void process(Session ws, Message message) {
        HttpSession httpSession = httpSessionMap.get(ws.getId());
        String sender = (String)httpSession.getAttribute("userid");
        message.setSender(sender);
        message.insertToDB();
        this.makeRecord(message.getSender(), message.getTarget(), message.getID());
        System.out.println("WebSocket get: " + message);
        if (sessionMap.containsKey(message.getTarget())) {
            this.reply(sessionMap.get(message.getTarget()), message, message.getTarget());
            message.updateState(true);
        }
    }

    @OnClose
    public void end(Session ws){
        String userid = (String)httpSessionMap.get(ws.getId()).getAttribute("userid");
        System.out.println("User " + userid + " leave");
        httpSessionMap.remove(ws.getId());
        sessionMap.remove(userid);
    }

    @OnError
    public void error(Session ws, java.lang.Throwable throwable) {
        String userid = (String)httpSessionMap.get(ws.getId()).getAttribute("userid");
        System.err.println("User " + userid + " error: " + throwable.getMessage());
        this.end(ws);
    }

    private void makeRecord(String useraid, String userbid, int messageid) {
        DatabaseConnection dbConn = new DatabaseConnection();
        Connection conn = dbConn.getConnection();
        String sql = "insert into chat_record (useraid, userbid, messageid) values (?, ?, ?)";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, useraid);
            ps.setString(2, userbid);
            ps.setInt(3, messageid);
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

    private List<Message> queryAllUnread(String targetid) {
        List<Message> unreads = new ArrayList<>();
        DatabaseConnection dbConn = new DatabaseConnection();
        Connection conn = dbConn.getConnection();
        String sql = "select * from message where\n" +
                "state=false\n" +
                "and\n" +
                "messageid in (select messageid from chat_record where userbid=?) order by ctime";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, targetid);
            rs = ps.executeQuery();
            while (rs.next()) {
                Message m = new Message(targetid, rs.getString("content"));
                m.setSender(rs.getString("userid"));
                m.setCtime(rs.getString("ctime"));
                m.setMessageID(rs.getInt("messageid"));
                unreads.add(m);
            }
            // mark all message as read.
            for (Message m: unreads) {
                m.updateState(true);
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
        return unreads;
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

