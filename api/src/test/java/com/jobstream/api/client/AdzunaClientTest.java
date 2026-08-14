package com.jobstream.api.client;

import com.jobstream.api.exception.ExternalApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AdzunaClientTest {

    private static final String BASE_URL = "https://api.test.com";

    private AdzunaClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new AdzunaClient(builder, "testAppId", "testAppKey", BASE_URL, "ch");
    }

    @Test
    void callAdzunaApi_shouldReturnResponseBody() {
        server.expect(requestTo(BASE_URL + "/jobs/ch/search/1"
                        + "?app_id=testAppId&app_key=testAppKey&results_per_page=20"
                        + "&what=java&content-type=application/json"))
                .andRespond(withSuccess("""
                        {"count":1,"results":[{"id":"job_1","title":"Java Dev","company":"Acme","location":"Zurich"}]}
                        """, MediaType.APPLICATION_JSON));

        Map<String, Object> result = client.callAdzunaApi("java", 1, 20, null);

        assertThat(result).isNotNull();
        assertThat(result.get("count")).isEqualTo(1);
        assertThat(result.get("results")).isInstanceOf(List.class);
        server.verify();
    }

    @Test
    void callAdzunaApi_shouldUseDefaultsWhenPageAndLimitAreNull() {
        server.expect(requestTo(BASE_URL + "/jobs/ch/search/1"
                        + "?app_id=testAppId&app_key=testAppKey&results_per_page=20"
                        + "&what=java&content-type=application/json"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        client.callAdzunaApi("java", null, null, null);

        server.verify();
    }

    @Test
    void callAdzunaApi_shouldIncludeLocationWhenProvided() {
        server.expect(requestTo(BASE_URL + "/jobs/ch/search/1"
                        + "?app_id=testAppId&app_key=testAppKey&results_per_page=20"
                        + "&what=java&content-type=application/json&where=paris"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        client.callAdzunaApi("java", 1, 20, "paris");

        server.verify();
    }

    @Test
    void callAdzunaApi_shouldExcludeLocationWhenBlank() {
        server.expect(requestTo(BASE_URL + "/jobs/ch/search/1"
                        + "?app_id=testAppId&app_key=testAppKey&results_per_page=20"
                        + "&what=java&content-type=application/json"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        client.callAdzunaApi("java", 1, 20, "   ");

        server.verify();
    }

    @Test
    void callAdzunaApi_shouldThrowExternalApiExceptionOnServerError() {
        server.expect(requestTo(BASE_URL + "/jobs/ch/search/1"
                        + "?app_id=testAppId&app_key=testAppKey&results_per_page=20"
                        + "&what=java&content-type=application/json"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR).body("{}"));

        assertThatThrownBy(() -> client.callAdzunaApi("java", 1, 20, null))
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("Erreur lors de la recherche d'offres")
                .hasCauseInstanceOf(Exception.class);
    }
}