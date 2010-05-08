package org.odata4j.format.xml;

import org.joda.time.DateTime;

public class XmlFormatWriter {

    protected static String edmx = "http://schemas.microsoft.com/ado/2007/06/edmx";
    protected static String d = "http://schemas.microsoft.com/ado/2007/08/dataservices";
    protected static String m = "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata";
    protected static String edm = "http://schemas.microsoft.com/ado/2006/04/edm";

    protected static String atom = "http://www.w3.org/2005/Atom";
    protected static String app = "http://www.w3.org/2007/app";

    protected static final String scheme = "http://schemas.microsoft.com/ado/2007/08/dataservices/scheme";
    protected static final String related = "http://schemas.microsoft.com/ado/2007/08/dataservices/related/";

    protected static String toString(DateTime utc) {
        return utc.toString("yyyy-MM-dd'T'HH:mm:ss'Z'");
    }
}
