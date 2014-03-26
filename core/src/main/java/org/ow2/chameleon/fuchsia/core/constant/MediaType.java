package org.ow2.chameleon.fuchsia.core.constant;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Core
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * See http://www.iana.org/assignments/media-types/media-types.xhtml
 */
public final class MediaType {
    /**
     * The value of a type or subtype wildcard: "*".
     */
    public static final String MEDIA_TYPE_WILDCARD = "*";

    /**
     * "*&#47;*".
     */
    public static final String WILDCARD = "*/*";

    /**
     * "application/xml".
     */
    public static final String APPLICATION_XML = "application/xml";

    /**
     * "application/atom+xml".
     */
    public static final String APPLICATION_ATOM_XML = "application/atom+xml";

    /**
     * "application/xhtml+xml".
     */
    public static final String APPLICATION_XHTML_XML = "application/xhtml+xml";

    /**
     * "application/svg+xml".
     */
    public static final String APPLICATION_SVG_XML = "application/svg+xml";

    /**
     * "application/rss+xml".
     */
    public static final String APPLICATION_RSS_XML = "application/rss+xml";

    /**
     * "application/json".
     */
    public static final String APPLICATION_JSON = "application/json";

    /**
     * "application/x-www-form-urlencoded".
     */
    public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";

    /**
     * "multipart/form-data".
     */
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";

    /**
     * "application/octet-stream".
     */
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    /**
     * "text/plain".
     */
    public static final String TEXT_PLAIN = "text/plain";

    /**
     * "text/xml".
     */
    public static final String TEXT_XML = "text/xml";

    /**
     * "text/html".
     */
    public static final String TEXT_HTML = "text/html";

    private MediaType() {
        // private constructor
    }
}
