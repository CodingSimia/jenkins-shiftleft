package io.jenkins.plugins.shiftleft;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import hudson.util.Secret;
import java.io.PrintStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AuthHelperTest {
    @Mock
    private PrintStream mockLogger;

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockResponse;

    private AuthHelper authHelper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authHelper = new AuthHelper(mockLogger);
    }

    @Test
    void testGetAuthTokenSuccess() throws Exception {
        // Setup test data
        String uri = "example.com";
        String clientId = "testClientId";
        Secret clientSecret = Secret.fromString("testClientSecret");
        String accessToken = "testAccessToken";

        // Mock HTTP response
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"access_token\":\"" + accessToken + "\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        // Inject the mock HttpClient
        try (var ignored = mockStatic(HttpClient.class)) {
            when(HttpClient.newHttpClient()).thenReturn(mockHttpClient);

            // Test getAuthToken
            String token = authHelper.getAuthToken(uri, clientId, clientSecret);
            assertEquals(accessToken, token);
        }
    }

    @Test
    void testGetAuthTokenFailure() throws Exception {
        String uri = "example.com";
        String clientId = "testClientId";
        Secret clientSecret = Secret.fromString("testClientSecret");

        // Simulate an exception during HTTP request
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new RuntimeException("Request error"));

        // Inject the mock HttpClient
        try (var ignored = mockStatic(HttpClient.class)) {
            when(HttpClient.newHttpClient()).thenReturn(mockHttpClient);

            // Test getAuthToken and verify the error message
            // Attempt the export and verify error
            Exception exception =
                    assertThrows(RuntimeException.class, () -> authHelper.getAuthToken(uri, clientId, clientSecret));
            assertTrue(exception.getMessage().contains("Request error"));
        }
    }

    @Test
    void testExtractOrgIdFromJwtSuccess() {
        // JWT with payload containing "org_id"
        String authToken = "header.eyJvcmdfaWQiOiJ0ZXN0T3JnSWQifQ.signature";

        // Test extraction of orgId
        String orgId = authHelper.extractOrgIdFromJwt(authToken);
        assertEquals("testOrgId", orgId);
    }

    @Test
    void testExtractOrgIdFromJwtInvalidToken() {
        // Invalid JWT format
        String invalidAuthToken = "invalidToken";

        // Test extraction with invalid JWT
        String orgId = authHelper.extractOrgIdFromJwt(invalidAuthToken);
        assertNull(orgId);
        verify(mockLogger).println(contains("Error extracting orgId from token"));
    }

    @Test
    void testExtractOrgIdFromJwtInvalidPayload() {
        // JWT with invalid payload
        String authToken = "header.invalidPayload.signature";

        // Test extraction with an invalid payload
        String orgId = authHelper.extractOrgIdFromJwt(authToken);
        assertNull(orgId);
        verify(mockLogger).println(contains("Error extracting orgId from token"));
    }

    @Test
    void testParseTokenSuccess() {
        // JSON response with access token
        String responseBody = "{\"access_token\":\"testAccessToken\"}";

        // Test token parsing
        String token = authHelper.parseToken(responseBody);
        assertEquals("testAccessToken", token);
    }

    @Test
    void testParseTokenFailure() {
        // Malformed JSON response
        String malformedResponse = "invalid json";

        // Test parsing with malformed JSON
        String token = authHelper.parseToken(malformedResponse);
        assertEquals("", token);
        verify(mockLogger).println(contains("Error parsing token"));
    }

    @Test
    void testGetFormDataString() {
        Map<String, String> params = Map.of(
                "client_id", "testClientId",
                "client_secret", "testClientSecret",
                "grant_type", "client_credentials");

        // Test form data string generation
        String formData = AuthHelper.getFormDataString(params);
        assertTrue(formData.contains("client_id=testClientId"));
        assertTrue(formData.contains("client_secret=testClientSecret"));
        assertTrue(formData.contains("grant_type=client_credentials"));
    }
}
