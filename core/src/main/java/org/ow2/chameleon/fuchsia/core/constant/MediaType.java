package org.ow2.chameleon.fuchsia.core.constant;

/**
 * See http://www.iana.org/assignments/media-types/media-types.xhtml
 */
public final class MediaType {
    /**
     * The value of a type or subtype wildcard: "*"
     */
    public static final String MEDIA_TYPE_WILDCARD = "*";

    /**
     * "*&#47;*"
     */
    public final static String WILDCARD = "*/*";

    /**
     * "application/xml"
     */
    public final static String APPLICATION_XML = "application/xml";

    /**
     * "application/atom+xml"
     */
    public final static String APPLICATION_ATOM_XML = "application/atom+xml";

    /**
     * "application/xhtml+xml"
     */
    public final static String APPLICATION_XHTML_XML = "application/xhtml+xml";

    /**
     * "application/svg+xml"
     */
    public final static String APPLICATION_SVG_XML = "application/svg+xml";

    /**
     * "application/rss+xml"
     */
    public static final String APPLICATION_RSS_XML = "application/rss+xml";

    /**
     * "application/json"
     */
    public final static String APPLICATION_JSON = "application/json";

    /**
     * "application/x-www-form-urlencoded"
     */
    public final static String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";

    /**
     * "multipart/form-data"
     */
    public final static String MULTIPART_FORM_DATA = "multipart/form-data";

    /**
     * "application/octet-stream"
     */
    public final static String APPLICATION_OCTET_STREAM = "application/octet-stream";

    /**
     * "text/plain"
     */
    public final static String TEXT_PLAIN = "text/plain";

    /**
     * "text/xml"
     */
    public final static String TEXT_XML = "text/xml";

    /**
     * "text/html"
     */
    public final static String TEXT_HTML = "text/html";

    private MediaType() {
        // private constructor
    }
}
