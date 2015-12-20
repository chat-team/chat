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
        value = "/f2fchat",
        configurator = SessionConfig.class,
        encoders = { MessageEncoder.class },
        decoders = { MessageDecoder.class }
)

public class F2FChat {
    private static Map<String, HttpSession> sessionMap = new Hashtable<>();

    @OnOpen
    public void start(Session ws, EndpointConfig config){
        HttpSession httpSession = (HttpSession)config.getUserProperties().get(HttpSession.class.getName());
        sessionMap.put(ws.getId(), httpSession);
        String userid = (String) httpSession.getAttribute("userid");
        for (Message m: this.queryAllUnread(userid)) {
            this.reply(ws, m, userid);
        }
        System.out.println("User " + userid + " enter.");
    }

    @OnMessage
    public void process(Session ws, Message message) {
        HttpSession httpSession = sessionMap.get(ws.getId());
        String sender = (String)httpSession.getAttribute("userid");
        message.setSender(sender);
        message.insertToDB();
        Message response = message;
        this.makeRecord(message.getSender(), message.getTarget(), message.getID());
        if (sessionMap.containsKey(message.getTarget())) {
            this.reply(ws, response, message.getTarget());
            message.updateState(true);
        }
        System.out.println("log 2: " + message);
    }

    @OnClose
    public void end(Session ws){
        System.out.println("Colse " + sessionMap.get(ws.getId()).getAttribute("userid"));
        sessionMap.remove(ws.getId());
    }

    @OnError
    public void error(Session ws, java.lang.Throwable throwable) {
        System.err.println("User " + sessionMap.get(ws.getId()).getAttribute("userid") + " error: " + throwable.getMessage());
        end(ws);
    }

    private void makeRecord(String useraid, String userbid, int messageid) {
        DatabaseConnection dbConn = new DatabaseConnection();
        Connection conn = dbConn.getConnection();
        String sql = "insert into chat_record (useraid, userbid, messageid) value (?, ?, ?)";
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
        String sql = "select * from message where state=false and messageid in (select messageid from chat_record where userbid=?)";
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

    private void reply(Session ws, Message message, String targetid) {
        System.out.println("Reply to " + sessionMap.get(ws.getId()).getAttribute("userid"));
        RemoteEndpoint.Basic remote = ws.getBasicRemote();
        try {
            remote.sendObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (EncodeException e) {
            e.printStackTrace();
        }
    }
}

