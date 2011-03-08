package org.odata4j.format;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.odata4j.format.json.JsonEntryFormatParser;
import org.odata4j.format.json.JsonFeedFormatParser;
import org.odata4j.format.json.JsonFeedFormatParser.JsonEntry;
import org.odata4j.format.json.JsonFeedFormatParser.JsonFeed;
import org.odata4j.format.xml.AtomEntryFormatParser;
import org.odata4j.format.xml.AtomFeedFormatParser;
import org.odata4j.format.xml.AtomFeedFormatParser.AtomEntry;
import org.odata4j.format.xml.AtomFeedFormatParser.AtomFeed;

public class FormatParserFactory {

    private static interface FormatParsers<F extends Feed<E>, E extends Entry> {
        public FormatParser<F> getFeedFormatParser();
        public FormatParser<E> getEntryFormatParser();
    }

    @SuppressWarnings("unchecked")
	public static <T> FormatParser<T> getParser(Class<T> targetType,
			FormatType type) {
		FormatParsers<?,?> formatParsers = type.equals(FormatType.JSON)
        	? new JsonParsers()
        	: new AtomParsers();
        
        if (Feed.class.isAssignableFrom(targetType)) {
            return (FormatParser<T>)formatParsers.getFeedFormatParser();
        } 
        if (Entry.class.isAssignableFrom(targetType)) {
            return (FormatParser<T>)formatParsers.getEntryFormatParser();
        } 
        throw new IllegalArgumentException("Unable to locate format writer for " + targetType.getName() + " and format " + type);
	}
    
    public static <T> FormatParser<T> getParser(Class<T> targetType, List<MediaType> acceptTypes, String format){
        
        FormatType type = null;
        
        // if format is explicitly specified, use that
        if (format != null){
            type = FormatType.parse(format);
        }
        
        // if header accepts json, use that
        if (type==null && acceptTypes != null) {
            for(MediaType acceptType : acceptTypes){
                if (acceptType.equals(MediaType.APPLICATION_JSON_TYPE)){
                    type = FormatType.JSON;
                    break;
                }
            }
        }
        
        // else default to atom
        if (type==null)
            type = FormatType.ATOM;

        return getParser(targetType, type);
    }

    public static class JsonParsers implements FormatParsers<JsonFeed, JsonEntry> {

        @Override
        public FormatParser<JsonFeed> getFeedFormatParser() {
            return new JsonFeedFormatParser();
        }

        @Override
        public FormatParser<JsonEntry> getEntryFormatParser() {
            return new JsonEntryFormatParser();
        }
        
    }
    
    public static class AtomParsers implements FormatParsers<AtomFeed, AtomEntry> {

        @Override
        public FormatParser<AtomFeed> getFeedFormatParser() {
            return new AtomFeedFormatParser();
        }

        @Override
        public FormatParser<AtomEntry> getEntryFormatParser() {
            return new AtomEntryFormatParser();
        }
        
    }
}
