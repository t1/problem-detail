package com.github.t1.problem;

import com.github.t1.problem.ProblemDetail.ProblemDetailBuilder;
import lombok.*;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.net.URI;

import static com.github.t1.problem.ProblemDetail.*;
import static com.github.t1.problem.WebException.builderFor;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.Family.*;

/** @see WebException */
public class WebExceptionBuilder {
    private static final String EXCEPTION = "Exception";

    public static WebExceptionBuilder from(Class<?> type) {
        return builderFor(annotatedStatus(type))
                .type(URI.create(URN_PROBLEM_JAVA_PREFIX + type.getName()))
                .title(title(type));
    }

    public static Status annotatedStatus(Class<?> type) {
        ReturnStatus returnStatus = type.getAnnotation(ReturnStatus.class);
        return (returnStatus == null) ? BAD_REQUEST : returnStatus.value();
    }

    private static String title(Class<?> type) {
        String name = type.getSimpleName();
        if (name.endsWith(EXCEPTION))
            name = name.substring(0, name.length() - EXCEPTION.length());
        return camelToWords(name);
    }

    private static String camelToWords(String in) {
        StringBuilder out = new StringBuilder();
        for (char c : in.toCharArray()) {
            if (out.length() > 0 && Character.isUpperCase(c))
                out.append(' ');
            out.append(Character.toLowerCase(c));
        }
        return out.toString();
    }


    private final ProblemDetailBuilder entity;
    @NonNull private final Status status;
    private Throwable cause;

    public WebExceptionBuilder(Status status) {
        this.entity = ProblemDetail.builder().status(status);
        this.status = status;
    }

    public WebExceptionBuilder type(URI type) {
        entity.type(type);
        return this;
    }

    public WebExceptionBuilder title(@NonNull String title) {
        entity.title(title);
        return this;
    }

    public WebExceptionBuilder detail(@NonNull String detail) {
        entity.detail(detail);
        return this;
    }

    public WebExceptionBuilder causedBy(@NonNull Throwable cause) {
        this.cause = cause;
        return this;
    }

    public WebException build() {
        ProblemDetail detail = buildEntity();
        Response response = buildResponse(detail);
        return isServerError()
                ? new WebException(detail.toString(), response, cause)
                : new WebApplicationApplicationException(detail.toString(), response, cause);
    }

    public ProblemDetail buildEntity() {return entity.build();}

    private boolean isServerError() { return status.getFamily() == SERVER_ERROR; }

    private Response buildResponse(ProblemDetail detail) {
        return Response
                .status(status)
                .entity(detail)
                .type(APPLICATION_PROBLEM_JSON_TYPE)
                .build();
    }
}
