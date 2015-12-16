package chat;

import javax.json.*;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by He Tao on 2015/12/15.
 */
public final class ResWriter {
    private JsonWriter writer;
    private JsonObjectBuilder builder;

    public ResWriter(OutputStream output) {
        this.writer = Json.createWriter(output);
        this.builder = Json.createObjectBuilder();
    }

    public void write() {
        writer.writeObject(builder.build());
    }

    public ResWriter add(String key, String value) {
        this.builder.add(key, value);
        return this;
    }

    public ResWriter add(String key, int value) {
        this.builder.add(key, value);
        return this;
    }

    public  ResWriter add(String key, List<String> element) {
        JsonArrayBuilder arraybuilder;
        arraybuilder = Json.createArrayBuilder();
        for (int i = 0; i < element.size(); i += 2) {
            JsonObjectBuilder t  = Json.createObjectBuilder();;
            t.add("groupid", element.get(i));
            t.add("groupname", element.get(i + 1));
            arraybuilder.add(t);
        }
        this.builder.add(key, arraybuilder);
        return this;
    }

}
