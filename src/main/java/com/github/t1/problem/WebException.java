package com.github.t1.problem;

import lombok.*;

import javax.ejb.ApplicationException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static com.github.t1.problem.WebExceptionBuilder.annotatedStatus;
import static javax.ws.rs.core.Response.Status.*;

/**
 * A {@link WebApplicationException} with a message (also pre JAX-RS 2.0).
 * Provides builders for WebExceptions with a ProblemDetail entity.
 *
 * Simple example:
 * `throw badRequest("you did it wrong");`
 *
 * Or using a builder:
 * `throw WebException.of(BAD_REQUEST).causedBy(e).text("you did it wrong").build();`
 *
 * Note that non-server-errors (< 500) are instances of {@link WebApplicationApplicationException},
 * annotated as {@link ApplicationException}, so the EJB container doesn't handle them, i.e. wraps them into an
 * EJBException, rollback transactions, destroy beans, etc.
 *
 * You can also subclass this exception to have a type that you can refer to. This is useful when you want to document
 * the return codes of a service for the clients to react to in a normal conditions. If return codes are only returned
 * when there's a bug, this won't be necessary; a good error description would be absolutely sufficient.
 *
 *      ReturnStatus(CONFLICT)
 *      public class YouDidItWrongException extends WebApplicationApplicationException {
 *          public YouDidItWrongException() {
 *              super("Next time, you'll do better");
 *          }
 *      }
 *
 * The problem detail entity would get these values:
 *
 *      type: urn:problem:java:mypackage.YouDidItWrong
 *      title: You did it wrong
 *      status: 409
 *      detail: Next time, you'll do better
 *      instance: fb4627b2-8479-4520-a645-d9e866c1d5ef
 */
public class WebException extends WebApplicationException {
    private static final long serialVersionUID = 1L;

    public static WebException badRequest(String detail) { return builderFor(BAD_REQUEST).detail(detail).build(); }

    public static WebException badGateway(String detail) { return builderFor(BAD_GATEWAY).detail(detail).build(); }

    public static WebException notFound(String detail) { return builderFor(NOT_FOUND).detail(detail).build(); }

    public static WebExceptionBuilder builderFor(Status status) { return new WebExceptionBuilder(status); }

    private String message;

    @Override public String getMessage() {
        return (message == null) ? getResponse().getEntity().toString() : message;
    }

    public WebException(String message, Response response, Throwable cause) {
        super(cause, response);
        this.message = message;
    }

    /** This constructor is for custom sub-types, which can be annotated as {@link ReturnStatus}. */
    protected WebException(String message) { super(buildResponse(message)); }

    private static Response buildResponse(String message) {
        Class<?> type = callingType();
        return Response
                .status(annotatedStatus(type))
                .entity(WebExceptionBuilder.from(type).detail(message).buildEntity())
                .build();
    }

    @SneakyThrows(ClassNotFoundException.class)
    private static Class<?> callingType() {
        return Class.forName(new RuntimeException().getStackTrace()[4].getClassName());
    }
}
