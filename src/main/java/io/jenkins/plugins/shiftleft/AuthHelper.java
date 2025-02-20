package io.jenkins.plugins.shiftleft;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.util.Secret;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

public class AuthHelper {
    private final PrintStream logger;

    public AuthHelper(PrintStream logger) {
        this.logger = logger;
    }

    public String getAuthToken(String uri, String clientId, Secret clientSecret)
            throws IOException, InterruptedException {
        // Map for request parameters
        Map<String, String> parameters = Map.of(
                "audience",
                "https://agent." + uri,
                "client_id",
                clientId,
                "client_secret",
                clientSecret.getPlainText(),
                "grant_type",
                "client_credentials");

        String requestBody = getFormDataString(parameters);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://oauth." + uri + "/oauth/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // Send the request and get the response
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the token from JSON response
        String responseBody = response.body();
        if (response.statusCode() == 200) {
            return parseToken(responseBody);
        } else {
            logger.printf("[❌] Failed to authenticate with %s: %s%n", uri, responseBody);
            return "";
        }
    }

    public String extractOrgIdFromJwt(String authToken) {
        try {
            // Split the JWT and get the payload part (second part)
            String[] jwtParts = authToken.split("\\.");
            if (jwtParts.length < 2) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }
            String payload = jwtParts[1];

            // Handle padding for base64 decoding
            int paddingLength = (4 - payload.length() % 4) % 4;
            if (paddingLength > 0) {
                payload += "=".repeat(paddingLength);
            }

            // Decode the Base64 payload
            byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
            String decodedPayload = new String(decodedBytes, StandardCharsets.UTF_8);

            // Parse JSON and extract `org_id`
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(decodedPayload);
            return jsonNode.path("org_id").asText();

        } catch (Exception e) {
            logger.println("[❌] Error extracting orgId from token: " + e);
            return null;
        }
    }

    // Method to parse token from the response JSON
    public String parseToken(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(responseBody);
            return jsonNode.path("access_token").asText();
        } catch (Exception e) {
            logger.println("[❌] Error parsing token: " + e);
            return "";
        }
    }

    // Helper method to encode parameters
    public static String getFormDataString(Map<String, String> params) {
        return params.entrySet().stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }
}
