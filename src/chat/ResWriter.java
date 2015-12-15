package chat;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import java.io.OutputStream;

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
}
