package chat.ws;

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.HandshakeResponse;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * Created by He Tao on 2015/12/20.
 */
public class SessionConfig extends ServerEndpointConfig.Configurator  {
    @Override
    public void modifyHandshake(ServerEndpointConfig config,
                                HandshakeRequest request,
                                HandshakeResponse response) {
        System.out.println("request : " + request + "  httpSession: " + request.getHttpSession());
        config.getUserProperties().put(HttpSession.class.getName(), (HttpSession)request.getHttpSession());
    }
}
