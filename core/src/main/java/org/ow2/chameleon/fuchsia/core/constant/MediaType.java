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
    public static final String WILDCARD = "*/*";

    /**
     * "application/xml"
     */
    public static final String APPLICATION_XML = "application/xml";

    /**
     * "application/atom+xml"
     */
    public static final String APPLICATION_ATOM_XML = "application/atom+xml";

    /**
     * "application/xhtml+xml"
     */
    public static final String APPLICATION_XHTML_XML = "application/xhtml+xml";

    /**
     * "application/svg+xml"
     */
    public static final String APPLICATION_SVG_XML = "application/svg+xml";

    /**
     * "application/rss+xml"
     */
    public static final String APPLICATION_RSS_XML = "application/rss+xml";

    /**
     * "application/json"
     */
    public static final String APPLICATION_JSON = "application/json";

    /**
     * "application/x-www-form-urlencoded"
     */
    public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";

    /**
     * "multipart/form-data"
     */
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";

    /**
     * "application/octet-stream"
     */
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    /**
     * "text/plain"
     */
    public static final String TEXT_PLAIN = "text/plain";

    /**
     * "text/xml"
     */
    public static final String TEXT_XML = "text/xml";

    /**
     * "text/html"
     */
    public static final String TEXT_HTML = "text/html";

    private MediaType() {
        // private constructor
    }
}
