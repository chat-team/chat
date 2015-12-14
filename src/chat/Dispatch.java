package chat;

import java.io.IOException;

import java.util.*;
import javax.websocket.*;
import javax.websocket.server.*;

/**
 * Created by He Tao on 2015/12/13.
 */
@ServerEndpoint(value="/dispatch")
public class Dispatch  {

    //static final Logger logger = Logger.getLogger(ChatEndpoint.class);
    static Map<String, Session> sessionMap = new Hashtable<>();

    @OnOpen
    public void start(Session session){
        System.out.println("Guest"+session.getId()+" join");
        sessionMap.put(session.getId(), session);
        broadcast("Guest" + session.getId() + " join.");
    }

    @OnMessage
    public void process(Session session, String message){
        System.out.println(session.getId()+" say: " + message);
        broadcast("Guest"+session.getId()+" [say]: "+message);
    }


    @OnClose
    public void end(Session session){
        System.out.println("Guest"+session.getId()+" out.");
        sessionMap.remove(session.getId());
        broadcast("Guest"+session.getId()+ " out.");
    }

    @OnError
    public void error(Session session, java.lang.Throwable throwable){
        System.err.println("Guest" + session.getId() + " error: " + throwable);
        end(session);
    }

    void broadcast(String message){
        RemoteEndpoint.Basic remote = null;
        Set<Map.Entry<String,Session>> set = sessionMap.entrySet();
        for(Map.Entry<String,Session> i: set){
            remote = i.getValue().getBasicRemote();
            try {
                remote.sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }//static final Logger logger = Logger.getLogger(ChatEndpoint.class);
}
