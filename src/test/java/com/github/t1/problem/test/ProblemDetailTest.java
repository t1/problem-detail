package com.github.t1.problem.test;

import com.github.t1.problem.ProblemDetail;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.*;

import javax.xml.bind.JAXB;
import java.io.*;
import java.net.URI;
import java.util.Objects;

import static com.github.t1.problem.ProblemDetail.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.*;

class ProblemDetailTest {
    private static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
    private static final String XML_PATTERN = XML.replace("?", "\\?");

    @Nested
    @DisplayName("An empty ProblemDetail")
    class WhenEmpty {
        private ProblemDetail emptyDetail;

        @BeforeEach void create() { emptyDetail = ProblemDetail.builder().build(); }

        @Test void hasToString() { assertThat(emptyDetail.toString()).matches("instance: urn:problem-instance:.*\n"); }

        @Test void hasTypeNull() { assertThat(emptyDetail.getType()).isNull(); }

        @Test void hasTitleNull() { assertThat(emptyDetail.getTitle()).isNull(); }

        @Test void hasStatusNull() { assertThat(emptyDetail.getStatus()).isNull(); }

        @Test void hasDetailNull() { assertThat(emptyDetail.getDetail()).isNull(); }

        @Test void hasCauseNull() { assertThat(emptyDetail.getCause()).isNull(); }

        @Test void hasInstance() { assertThat(emptyDetail).has(instance()); }

        @Test void fromJson() { assertThat(ProblemDetail.fromJson("{}")).is(equalIgnoringInstance(emptyDetail)); }

        @Test void fromJsonWithExplicitNulls() {
            assertThat(ProblemDetail.fromJson(""
                    + "{"
                    + "\"type\":null,"
                    + "\"title\":null,"
                    + "\"status\":null,"
                    + "\"detail\":null,"
                    + "\"instance\":null,"
                    + "\"cause\":null"
                    + "}")).is(equalIgnoringInstance(emptyDetail));
        }

        @Test void fromXml() { assertThat(xml(XML + "<problemDetail/>\n")).is(equalIgnoringInstance(emptyDetail)); }

        @Test void toXml() {
            assertThat(xml(emptyDetail)).matches(XML_PATTERN
                    + "<problemDetail>\n"
                    + "    <instance>urn:problem-instance:.*</instance>\n"
                    + "</problemDetail>\n");
        }
    }


    @Nested
    @DisplayName("A full ProblemDetail")
    class WhenFull {
        private static final String FULL_DETAIL_JSON = ""
                + "{"
                + "\"type\":\"urn:problem:foo-type\","
                + "\"title\":\"foo-title\","
                + "\"status\":409,"
                + "\"detail\":\"foo-detail\","
                + "\"instance\":\"foo-instance\","
                + "\"cause\":{\"title\":\"cause-title\",\"instance\":\"cause-instance\"}"
                + "}";
        private static final String FULL_DETAIL_XML = XML + ""
                + "<problemDetail>\n"
                + "    <type>urn:problem:foo-type</type>\n"
                + "    <title>foo-title</title>\n"
                + "    <status>409</status>\n"
                + "    <detail>foo-detail</detail>\n"
                + "    <instance>foo-instance</instance>\n"
                + "    <cause>\n"
                + "        <title>cause-title</title>\n"
                + "        <instance>cause-instance</instance>\n"
                + "    </cause>\n"
                + "</problemDetail>\n";
        private ProblemDetail fullDetail;

        @BeforeEach void create() {
            fullDetail = ProblemDetail
                    .builder()
                    .type(URI.create("urn:problem:foo-type"))
                    .title("foo-title")
                    .status(CONFLICT)
                    .detail("foo-detail")
                    .instance(URI.create("foo-instance"))
                    .cause(ProblemDetail.builder().title("cause-title").instance(URI.create("cause-instance")).build())
                    .build();
        }

        @Test void hasToString() {
            assertThat(fullDetail.toString()).isEqualTo(""
                    + "type: urn:problem:foo-type\n"
                    + "title: foo-title\n"
                    + "status: 409\n"
                    + "detail: foo-detail\n"
                    + "instance: foo-instance\n"
                    + "cause:\n"
                    + "  title: cause-title\n"
                    + "  instance: cause-instance\n");
        }

        @Test void hasType() { assertThat(fullDetail.getType()).isEqualTo(URI.create(URN_PROBLEM_PREFIX + "foo-type"));}

        @Test void hasTitle() { assertThat(fullDetail.getTitle()).isEqualTo("foo-title"); }

        @Test void hasStatus() {
            assertThat(fullDetail.getStatus()).isEqualTo(CONFLICT.getStatusCode());
            assertThat(fullDetail.getStatusType()).isEqualTo(CONFLICT);
        }

        @Test void hasDetail() { assertThat(fullDetail.getDetail()).isEqualTo("foo-detail"); }

        @Test void hasInstance() { assertThat(fullDetail.getInstance()).isEqualTo(URI.create("foo-instance")); }

        @Test void fromJson() { assertThat(ProblemDetail.fromJson(FULL_DETAIL_JSON)).isEqualTo(fullDetail); }

        @Test void fromXml() { assertThat(xml(FULL_DETAIL_XML)).is(equalIgnoringInstance(fullDetail)); }

        @Test void toXml() { assertThat(xml(fullDetail)).isEqualTo(FULL_DETAIL_XML); }
    }

    @Nested
    @DisplayName("A doubly nested ProblemDetail")
    class DoublyNested {
        private static final String DOUBLY_DETAIL_JSON = ""
                + "{"
                + "\"type\":\"urn:problem:foo-type\","
                + "\"title\":\"foo-title\","
                + "\"status\":409,"
                + "\"detail\":\"foo-detail\","
                + "\"instance\":\"foo-instance\","
                + "\"cause\":{"
                + /**/"\"title\":\"cause-title\","
                + /**/"\"instance\":\"cause-instance\","
                + /**/"\"cause\":{"
                + /*    */"\"title\":\"doubly-title\","
                + /*    */"\"instance\":\"doubly-instance\""
                + /*    */"}"
                + /**/"}"
                + "}";
        private static final String DOUBLY_DETAIL_XML = XML + ""
                + "<problemDetail>\n"
                + "    <type>urn:problem:foo-type</type>\n"
                + "    <title>foo-title</title>\n"
                + "    <status>409</status>\n"
                + "    <detail>foo-detail</detail>\n"
                + "    <instance>foo-instance</instance>\n"
                + "    <cause>\n"
                + "        <title>cause-title</title>\n"
                + "        <instance>cause-instance</instance>\n"
                + "        <cause>\n"
                + "            <title>doubly-title</title>\n"
                + "            <instance>doubly-instance</instance>\n"
                + "        </cause>\n"
                + "    </cause>\n"
                + "</problemDetail>\n";
        private ProblemDetail doublyDetail;

        @BeforeEach void create() {
            doublyDetail = ProblemDetail
                    .builder()
                    .type(URI.create("urn:problem:foo-type"))
                    .title("foo-title")
                    .status(CONFLICT)
                    .detail("foo-detail")
                    .instance(URI.create("foo-instance"))
                    .cause(ProblemDetail
                            .builder()
                            .title("cause-title")
                            .instance(URI.create("cause-instance"))
                            .cause(ProblemDetail
                                    .builder()
                                    .title("doubly-title")
                                    .instance(URI.create("doubly-instance"))
                                    .build())
                            .build())
                    .build();
        }

        @Test void hasToString() {
            assertThat(doublyDetail.toString()).isEqualTo(""
                    + "type: urn:problem:foo-type\n"
                    + "title: foo-title\n"
                    + "status: 409\n"
                    + "detail: foo-detail\n"
                    + "instance: foo-instance\n"
                    + "cause:\n"
                    + "  title: cause-title\n"
                    + "  instance: cause-instance\n"
                    + "  cause:\n"
                    + "    title: doubly-title\n"
                    + "    instance: doubly-instance\n");
        }

        @Test void hasType() {
            assertThat(doublyDetail.getType()).isEqualTo(URI.create(URN_PROBLEM_PREFIX + "foo-type"));
        }

        @Test void hasTitle() { assertThat(doublyDetail.getTitle()).isEqualTo("foo-title"); }

        @Test void hasStatus() {
            assertThat(doublyDetail.getStatus()).isEqualTo(CONFLICT.getStatusCode());
            assertThat(doublyDetail.getStatusType()).isEqualTo(CONFLICT);
        }

        @Test void hasDetail() { assertThat(doublyDetail.getDetail()).isEqualTo("foo-detail"); }

        @Test void hasInstance() { assertThat(doublyDetail.getInstance()).isEqualTo(URI.create("foo-instance")); }

        @Test void fromJson() { assertThat(ProblemDetail.fromJson(DOUBLY_DETAIL_JSON)).isEqualTo(doublyDetail); }

        @Test void fromXml() { assertThat(xml(DOUBLY_DETAIL_XML)).is(equalIgnoringInstance(doublyDetail)); }

        @Test void toXml() { assertThat(xml(doublyDetail)).isEqualTo(DOUBLY_DETAIL_XML); }
    }

    private Condition<? super ProblemDetail> equalIgnoringInstance(ProblemDetail expected) {
        return new Condition<>(actual ->
                Objects.equals(expected.getType(), actual.getType()) &&
                        Objects.equals(expected.getTitle(), actual.getTitle()) &&
                        Objects.equals(expected.getStatus(), actual.getStatus()) &&
                        Objects.equals(expected.getDetail(), actual.getDetail()),
                "equal (ignoring instance) to>\n <%s>", expected);
    }

    private Condition<ProblemDetail> instance() {
        return new Condition<>(
                actual -> actual.getInstance().toString().startsWith(URN_PROBLEM_INSTANCE_PREFIX),
                "has instance field starting with " + URN_PROBLEM_INSTANCE_PREFIX);
    }

    private String xml(ProblemDetail detail) {
        StringWriter out = new StringWriter();
        JAXB.marshal(detail, out);
        return out.toString();
    }

    private ProblemDetail xml(String xml) {
        return JAXB.unmarshal(new StringReader(xml), ProblemDetail.class);
    }
}
