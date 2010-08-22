package org.odata4j.format;

import java.io.Writer;

import com.sun.jersey.api.core.ExtendedUriInfo;

public interface FormatWriter<T> {
    public void write(ExtendedUriInfo uriInfo, Writer w, T target);
    public String getContentType();
}
