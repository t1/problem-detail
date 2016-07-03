package com.github.t1.problem;

import javax.ws.rs.core.Response.Status;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Annotate your custom web exceptions to give them a different status type than {@link Status#BAD_REQUEST}.
 *
 * @see WebException
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface ReturnStatus {
    Status value();
}
