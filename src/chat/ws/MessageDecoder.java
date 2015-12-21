package chat.ws;

import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.io.StringReader;

/**
 * Created by Dongfang on 2015/12/17.
 */
public class MessageDecoder implements Decoder.Text<Message> {

    public Message decode(String message) throws DecodeException {
        JsonObject jsonObject = Json.createReader(new StringReader(message)).readObject();
        return new Message(jsonObject.getString("target"), jsonObject.getString("content"));
    }

    public void init(EndpointConfig ec) {

    }

    public boolean willDecode(String jsonMessage) {

        try {
            Json.createReader(new StringReader(jsonMessage)).readObject();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void destroy() {

    }
}
