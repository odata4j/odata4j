package org.odata4j.format;

import java.io.Reader;

import org.odata4j.core.OEntity;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.internal.FeedCustomizationMapping;

/**
 * Deals with parsing the resulting stream into a <code>Entry</code> or
 * <code>Feed</code> and converting it to a <code>OEntity</code>. The
 * implementation depends on the format Atom or Json.
 * 
 * @param <T>            Atom or json
 * @see Entry
 * @see Feed
 * @see OEntity
 */
public interface FormatParser<T> {

	public T parse(Reader reader);

	public <E> E toOEntity(Entry entry, Class<E> entityType,
			EdmDataServices metadata,
			EdmEntitySet entitySet,
			FeedCustomizationMapping fcMapping);
}
