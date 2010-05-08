package org.odata4j.format.json;

import java.io.IOException;
import java.io.Writer;

public class JsonWriter {

    private final Writer writer;
    public JsonWriter(Writer writer){
        this.writer = writer;
    }
    public void startCallback(String functionName) {
        try {
            writer.write(encode(functionName)+"(");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void endCallback() {
        try {
            writer.write(");");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }
    public void startObject() {
        try {
            writer.write("{\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void endObject() {
        try {
            writer.write("\n}");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void writeName(String name) {
        try {
            writer.write("\"" + encode(name) + "\" : ");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private String encode(String unencoded){
        return unencoded;
    }
    public void startArray() {
        try {
            writer.write("[\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void endArray() {
        try {
            writer.write("\n]");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void writeArraySeparator() {
        try {
            writer.write(", ");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void writeString(String name) {
        try {
            writer.write("\"" + encode(name) + "\"");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
