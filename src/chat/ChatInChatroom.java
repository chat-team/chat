package chat;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * Created by Dongfang on 2015/12/17.
 */
@ServerEndpoint(
        value = "/chatroom",
        encoders = { MessageEncoder.class },
        decoders = { MessageDecoder.class }
)
public class ChatInChatroom {

    static Map<String, Session> sessionMap = new Hashtable<>();

    @OnMessage
    public void onMessage(Message message, Session session)
        throws IOException, EncodeException {

        Message response = new Message();
        response.setSubject(message.getSubject());
        response.setContent(message.getContent());
        broadcast(response);
    }

    @OnOpen
    public void onOpen(Session session) {
        sessionMap.put(session.getId(), session);
        // room_status
    }

    @OnClose
    public void onClose(Session session) {
        sessionMap.remove(session.getId());
        // room_status
    }

    @OnError
    public void error(Session session, java.lang.Throwable throwable){
        System.err.println("Guest" + session.getId() + " error: " + throwable);
        onClose(session);
    }

    void broadcast(Message message) throws EncodeException{
        RemoteEndpoint.Basic remote = null;
        Set<Map.Entry<String,Session>> set = sessionMap.entrySet();
        for(Map.Entry<String,Session> i: set){
            remote = i.getValue().getBasicRemote();
            try {
                remote.sendObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
