package org.odata4j.format;

import javax.ws.rs.core.MediaType;

import org.odata4j.format.json.JsonEntryFormatParser;
import org.odata4j.format.json.JsonFeedFormatParser;
import org.odata4j.format.xml.AtomEntryFormatParser;
import org.odata4j.format.xml.AtomFeedFormatParser;

public class FormatParserFactory {

	private FormatParserFactory() {}
	
    private static interface FormatParsers {
        public FormatParser<Feed> getFeedFormatParser(Settings settings);
        public FormatParser<Entry> getEntryFormatParser(Settings settings);
    }

    @SuppressWarnings("unchecked")
	public static <T> FormatParser<T> getParser(Class<T> targetType,
			FormatType type, Settings settings) {
		FormatParsers formatParsers = type.equals(FormatType.JSON)
        	? new JsonParsers()
        	: new AtomParsers();
        
        if (Feed.class.isAssignableFrom(targetType)) {
            return (FormatParser<T>)formatParsers.getFeedFormatParser(settings);
        } 
        if (Entry.class.isAssignableFrom(targetType)) {
            return (FormatParser<T>)formatParsers.getEntryFormatParser(settings);
        } 
        throw new IllegalArgumentException("Unable to locate format parser for " + targetType.getName() + " and format " + type);
	}

    public static <T> FormatParser<T> getParser(Class<T> targetType, MediaType contentType, Settings settings) {

    	FormatType type;
    	if (contentType.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
    		type = FormatType.JSON;
    	} else if (contentType.isCompatible(MediaType.APPLICATION_ATOM_XML_TYPE)) {
    		type = FormatType.ATOM;
    	} else {
    		throw new IllegalArgumentException("Unknown content type " + contentType);
    	}

        return getParser(targetType, type, settings);
    }

    public static class JsonParsers implements FormatParsers {

        @Override
        public FormatParser<Feed> getFeedFormatParser(Settings settings) {
            return new JsonFeedFormatParser(settings);
        }

        @Override
        public FormatParser<Entry> getEntryFormatParser(Settings settings) {
            return new JsonEntryFormatParser(settings);
        }
        
    }
    
    public static class AtomParsers implements FormatParsers {

        @Override
        public FormatParser<Feed> getFeedFormatParser(Settings settings) {
            return new AtomFeedFormatParser(settings.metadata, settings.entitySetName, settings.entityKey, settings.fcMapping);
        }

        @Override
        public FormatParser<Entry> getEntryFormatParser(Settings settings) {
            return new AtomEntryFormatParser(settings.metadata, settings.entitySetName, settings.entityKey, settings.fcMapping);
        }
        
    }
}
