package org.odata4j.format;

import java.io.Writer;

public interface FormatWriter<T> {
    public void write(String baseUri, Writer w, T target);
    public String getContentType();
}
