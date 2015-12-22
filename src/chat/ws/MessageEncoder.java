package chat.ws;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 * Created by Dongfang on 2015/12/17.
 */
public class MessageEncoder implements Encoder.Text<Message> {

    public String encode(Message message) throws EncodeException {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("sender", message.getSender())
                .add("content", message.getContent())
                .add("ctime", message.getCtime());
        if (message.getGroup() != null) {
            builder.add("group", message.getGroup());
        }
        if (message.getRoom() != null) {
            builder.add("room", message.getRoom());
        }
        return builder.build().toString();
    }

    public void init(EndpointConfig ec) {

    }

    public void destroy() {

    }

}
