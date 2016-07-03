package com.github.t1.problem;

import javax.ejb.ApplicationException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * A {@link WebApplicationException} for non-server errors, annotated as {@link ApplicationException}, so the EJB
 * container doesn't handle them, i.e. wraps them into an EJBException, rollback transactions, destroy beans, etc.
 *
 * @see WebException
 */
@ApplicationException
public class WebApplicationApplicationException extends WebException {
    private static final long serialVersionUID = 1L;

    public WebApplicationApplicationException(String message, Response response, Throwable cause) {
        super(message, response, cause);
    }

    /** This constructor is for custom sub-types, which can be annotated as {@link ReturnStatus}. */
    protected WebApplicationApplicationException(String message) { super(message); }
}
