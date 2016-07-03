package com.github.t1.problem;

import lombok.*;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.StatusType;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.UUID;

/**
 * Defines a "problem detail" as a way to carry machine-readable details of errors in a HTTP response, to avoid the need
 * to invent new error response formats for HTTP APIs.
 *
 * @see <a href="https://tools.ietf.org/html/draft-ietf-appsawg-http-problem-01">IETF: Problem Details for HTTP APIs</a>
 */
@Value
@Builder
@XmlRootElement
public class ProblemDetail {
    /** The prefix for problem media types to be completed by <code>+json</code>, etc. */
    public static final String APPLICATION_PROBLEM_TYPE_PREFIX = "application/problem";

    /** The String Content-Type for {@link ProblemDetail}s in JSON */
    public static final String APPLICATION_PROBLEM_JSON = APPLICATION_PROBLEM_TYPE_PREFIX + "+json";
    /** The {@link MediaType} Content-Type for {@link ProblemDetail}s in JSON */
    public static final MediaType APPLICATION_PROBLEM_JSON_TYPE = MediaType.valueOf(APPLICATION_PROBLEM_JSON);

    /** The String Content-Type for {@link ProblemDetail}s in XML */
    public static final String APPLICATION_PROBLEM_XML = APPLICATION_PROBLEM_TYPE_PREFIX + "+xml";
    /** The {@link MediaType} Content-Type for {@link ProblemDetail}s in XML */
    public static final MediaType APPLICATION_PROBLEM_XML_TYPE = MediaType.valueOf(APPLICATION_PROBLEM_XML);

    /** The default {@link #type} URN scheme and namespace */
    public static final String URN_PROBLEM_PREFIX = "urn:problem:";
    /** The default {@link #type} URN scheme and namespace for java types */
    public static final String URN_PROBLEM_JAVA_PREFIX = URN_PROBLEM_PREFIX + "java:";

    /** The default {@link #instance} URN scheme and namespace */
    public static final String URN_PROBLEM_INSTANCE_PREFIX = "urn:problem-instance:";


    /**
     * A URI reference [RFC3986] that identifies the problem type. When dereferenced, it is encouraged to provide
     * human-readable documentation for the problem type (e.g., using HTML).
     *
     * Defaults to the {@link #URN_PROBLEM_PREFIX} + "java:" + fully qualified class name.
     */
    private URI type;

    /**
     * A short, human-readable summary of the problem type. It SHOULD NOT change from occurrence
     * to occurrence of the problem, except for purposes of localization.
     */
    private String title;

    /** The HTTP status code ([RFC7231], Section 6) generated by the origin server for this occurrence of the problem. */
    private StatusType status;

    /**
     * The full, human-readable explanation specific to this occurrence of the problem.
     * It MAY change from occurrence to occurrence of the problem.
     */
    private String detail;

    /**
     * A URI reference that identifies the specific occurrence of the problem.
     * It may or may not yield further information if dereferenced.
     */
    private URI instance = URI.create(URN_PROBLEM_INSTANCE_PREFIX + UUID.randomUUID());

    @Override public String toString() {
        StringBuilder out = new StringBuilder();
        append(out, "type", type);
        append(out, "title", title);
        append(out, "status", status.getStatusCode());
        append(out, "detail", detail);
        append(out, "instance", instance);
        return out.toString();
    }

    public void append(StringBuilder out, String title, Object field) {
        if (field != null)
            out.append(title).append(": ").append(field).append("\n");
    }
}
