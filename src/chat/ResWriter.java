package chat;

import javax.json.*;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

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

    public  ResWriter add(String key, Map<String, String> elements) {
        JsonArrayBuilder arraybuilder;
        arraybuilder = Json.createArrayBuilder();
        for (Map.Entry<String, String> e: elements.entrySet()) {
            JsonObjectBuilder t  = Json.createObjectBuilder();
            t.add(e.getKey(), e.getValue());
            arraybuilder.add(t);
        }
        this.builder.add(key, arraybuilder);
        return this;
    }

    public  ResWriter add(String key, List<Map<String, String>> elements) {
        JsonArrayBuilder arraybuilder;
        arraybuilder = Json.createArrayBuilder();
        for (Map<String, String> m: elements) {
            JsonObjectBuilder t  = Json.createObjectBuilder();;
            for (Map.Entry<String, String> e: m.entrySet()) {
                t.add(e.getKey(), e.getValue());
            }
            arraybuilder.add(t);
        }
        this.builder.add(key, arraybuilder);
        return this;
    }
}
