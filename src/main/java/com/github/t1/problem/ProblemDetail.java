package com.github.t1.problem;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.json.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.*;
import javax.xml.bind.annotation.*;
import java.io.StringReader;
import java.net.URI;
import java.util.UUID;
import java.util.function.*;

import static lombok.AccessLevel.*;

/**
 * Defines a "problem detail" as a way to carry machine-readable details of errors in a HTTP response, to avoid the need
 * to invent new error response formats for HTTP APIs.
 *
 * @see <a href="https://tools.ietf.org/html/draft-ietf-appsawg-http-problem-01">IETF: Problem Details for HTTP APIs</a>
 */
@Slf4j
@Value
@Builder
@XmlRootElement
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE, force = true)
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
    @XmlElement
    private URI type;

    /**
     * A short, human-readable summary of the problem type. It SHOULD NOT change from occurrence
     * to occurrence of the problem, except for purposes of localization.
     */
    @XmlElement
    private String title;

    /** The HTTP status code ([RFC7231], Section 6) generated by the origin server for this occurrence of the problem. */
    @XmlElement
    private Integer status;

    /**
     * The full, human-readable explanation specific to this occurrence of the problem.
     * It MAY change from occurrence to occurrence of the problem.
     */
    @XmlElement
    private String detail;

    /**
     * A URI reference that identifies the specific occurrence of the problem.
     * It may or may not yield further information if dereferenced.
     */
    @XmlElement
    private URI instance;

    /**
     * The cause object for this problem. This is not defined in the problem spec.
     */
    @XmlElement
    ProblemDetail cause;


    public StatusType getStatusType() { return (status == null) ? null : Status.fromStatusCode(status); }

    public static class ProblemDetailBuilder {
        private URI instance = URI.create(URN_PROBLEM_INSTANCE_PREFIX + UUID.randomUUID());

        public ProblemDetailBuilder status(StatusType type) { return status(type.getStatusCode()); }

        public ProblemDetailBuilder status(int statusCode) {
            this.status = statusCode;
            return this;
        }

        private void set(JsonObject json, String field, Consumer<String> consumer) {
            set(json, field, consumer, Function.identity());
        }

        private <T> void set(JsonObject json, String field, Consumer<T> consumer, Function<String, T> map) {
            if (has(json, field))
                consumer.accept(map.apply(json.getString(field)));
        }
    }


    public static ProblemDetail from(Response response) {
        try {
            return ProblemDetail.fromJson(response.readEntity(String.class));
        } catch (RuntimeException e) {
            log.debug("can't read problem detail body", e);
            return null;
        }
    }

    public static ProblemDetail fromJson(String string) {
        JsonObject json = Json.createReader(new StringReader(string)).readObject();
        return fromJson(json);
    }

    private static ProblemDetail fromJson(JsonObject json) {
        ProblemDetailBuilder problem = builder();
        problem.set(json, "type", problem::type, URI::create);
        problem.set(json, "title", problem::title);
        if (has(json, "status"))
            problem.status(json.getInt("status"));
        problem.set(json, "detail", problem::detail);
        problem.set(json, "instance", problem::instance, URI::create);
        if (has(json, "cause"))
            problem.cause(fromJson(json.getJsonObject("cause")));
        return problem.build();
    }

    private static boolean has(JsonObject json, String field) {
        return json.containsKey(field) && !json.isNull(field);
    }


    @Override public String toString() {
        StringBuilder out = new StringBuilder();
        toString("", out);
        return out.toString();
    }

    private void toString(String indent, StringBuilder out) {
        append(out, indent, "type", type);
        append(out, indent, "title", title);
        append(out, indent, "status", status);
        append(out, indent, "detail", detail);
        append(out, indent, "instance", instance);
        if (cause != null) {
            out.append(indent).append("cause:\n");
            cause.toString(indent + "  ", out);
        }
    }

    private void append(StringBuilder out, String indent, String title, Object field) {
        if (field != null)
            out.append(indent).append(title).append(": ").append(field).append("\n");
    }
}
