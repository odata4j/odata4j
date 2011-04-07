package org.odata4j.format;

import java.io.Writer;

import com.sun.jersey.api.core.ExtendedUriInfo;

/** Write entities to an HTTP stream in a particular format
 * 
 * @param <T> the type of the entities to be written
 */
public interface FormatWriter<T> {
	/** Write an object to the formatted version of the stream
	 * 
	 * @param uriInfo the base uri of the entity documents
	 * @param w the underlying "stream" to write to
	 * @param target the object to be written
	 */
    public void write(ExtendedUriInfo uriInfo, Writer w, T target);
    
    /** Recover the MIME content type for the stream
     * @return the MIME content type to be used for the content of this stream
     */
    public String getContentType();
}
