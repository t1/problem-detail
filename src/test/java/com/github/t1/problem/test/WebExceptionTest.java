package com.github.t1.problem.test;

import com.github.t1.problem.*;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import java.net.URI;

import static com.github.t1.problem.ProblemDetail.APPLICATION_PROBLEM_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("A WebException")
class WebExceptionTest {
    private Condition<Throwable> status(Response.Status status) {
        return new Condition<>(exception -> status.equals(response(exception).getStatusInfo()), "status %s", status);
    }

    private Condition<Throwable> contentType(MediaType mediaType) {
        return new Condition<>(exception ->
                mediaType.equals(response(exception).getMediaType()), "media type %s", mediaType);
    }

    private Response response(Throwable throwable) { return ((WebApplicationException) throwable).getResponse(); }

    private URI instanceUri(Throwable exception) {
        return ((ProblemDetail) response(exception).getEntity()).getInstance();
    }

    @Test void shouldBuildSimpleBadRequest() {
        WebException exception = WebException.badRequest("foo");

        assertThat(exception)
                .isInstanceOf(WebApplicationApplicationException.class)
                .hasMessage(""
                        + "status: 400\n"
                        + "detail: foo\n"
                        + "instance: " + instanceUri(exception) + "\n")
                .has(status(BAD_REQUEST))
                .has(contentType(APPLICATION_PROBLEM_JSON_TYPE));
    }

    @Test void shouldBuildFull() {
        WebException exception = WebException
                .builderFor(CONFLICT)
                .type(URI.create("urn:problem:failed.status.check"))
                .title("failed status check")
                .detail("foo is not allowed when bar")
                .causedBy(new IllegalArgumentException("foo"))
                .build();

        assertThat(exception)
                .isInstanceOf(WebApplicationApplicationException.class)
                .hasMessage(""
                        + "type: urn:problem:failed.status.check\n"
                        + "title: failed status check\n"
                        + "status: 409\n"
                        + "detail: foo is not allowed when bar\n"
                        + "instance: " + instanceUri(exception) + "\n")
                .has(status(CONFLICT))
                .has(contentType(APPLICATION_PROBLEM_JSON_TYPE));
    }

    @ReturnStatus(FORBIDDEN)
    private static class YouDidItWrongException extends WebApplicationApplicationException {
        private YouDidItWrongException(String message) {
            super(message);
        }
    }

    @Test void shouldBuildFromSubException() {
        String message = "Next time, you'll do better";
        YouDidItWrongException exception = new YouDidItWrongException(message);

        assertThat(exception)
                .isInstanceOf(WebApplicationApplicationException.class)
                .hasMessage(""
                        + "type: urn:problem:java:" + YouDidItWrongException.class.getName() + "\n"
                        + "title: you did it wrong\n"
                        + "status: 403\n"
                        + "detail: " + message + "\n"
                        + "instance: " + instanceUri(exception) + "\n")
                .has(status(FORBIDDEN));
    }
}
