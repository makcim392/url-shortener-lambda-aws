package com.rocketseat.createURLShortner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final S3Client s3Client = S3Client.create();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Raw input: " + input);

        try {
            // Get the body from input
            Object rawBody = input.get("body");
            logger.log("Raw body: " + rawBody);

            Map<String, Object> bodyMap;
            if (rawBody instanceof String) {
                // If body is a string, parse it
                bodyMap = objectMapper.readValue((String) rawBody, Map.class);
            } else if (rawBody instanceof Map) {
                // If body is already a map
                bodyMap = (Map<String, Object>) rawBody;
            } else {
                throw new IllegalArgumentException("Invalid body format");
            }

            // If bodyMap contains a nested 'body' object, use that instead
            if (bodyMap.containsKey("body")) {
                Object nestedBody = bodyMap.get("body");
                if (nestedBody instanceof String) {
                    bodyMap = objectMapper.readValue((String) nestedBody, Map.class);
                } else if (nestedBody instanceof Map) {
                    bodyMap = (Map<String, Object>) nestedBody;
                }
            }

            logger.log("Final bodyMap: " + bodyMap);

            // Extract and validate originalUrl
            String originalUrl = (String) bodyMap.get("originalUrl");
            if (originalUrl == null || originalUrl.trim().isEmpty()) {
                return createResponse(400, Map.of("error", "originalUrl is required"));
            }

            // Handle expiration time
            String expirationTime = (String) bodyMap.get("expirationTime");
            long expirationTimeInSeconds;

            if (expirationTime == null || expirationTime.trim().isEmpty()) {
                expirationTimeInSeconds = 24 * 3600; // 24 hours default
            } else {
                try {
                    Instant expiration = Instant.parse(expirationTime);
                    Instant now = Instant.now();
                    expirationTimeInSeconds = ChronoUnit.SECONDS.between(now, expiration);

                    if (expirationTimeInSeconds <= 0) {
                        return createResponse(400, Map.of("error", "Expiration time must be in the future"));
                    }
                } catch (Exception e) {
                    try {
                        expirationTimeInSeconds = Long.parseLong(expirationTime) * 3600;
                    } catch (NumberFormatException ne) {
                        return createResponse(400, Map.of("error",
                                "Invalid expirationTime format. Please provide either an ISO-8601 timestamp or number of hours"));
                    }
                }
            }

            // Generate short URL code
            String shortURLCode = UUID.randomUUID().toString().substring(0, 8);
            UrlData urlData = new UrlData(originalUrl, expirationTimeInSeconds);

            // Save to S3
            try {
                String urlDataJSON = objectMapper.writeValueAsString(urlData);
                logger.log("Writing to S3: " + urlDataJSON);

                PutObjectRequest request = PutObjectRequest.builder()
                        .bucket("url-shortener-storage-lambda-example")
                        .key(shortURLCode + ".json")
                        .build();

                s3Client.putObject(request, RequestBody.fromString(urlDataJSON));

                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("code", shortURLCode);
                successResponse.put("originalUrl", originalUrl);
                successResponse.put("expirationTime", expirationTime);

                return createResponse(200, successResponse);

            } catch (JsonProcessingException e) {
                logger.log("Error saving to S3: " + e.getMessage());
                return createResponse(500, Map.of("error", "Error saving data to S3: " + e.getMessage()));
            }

        } catch (Exception e) {
            logger.log("Error: " + e.getMessage());
            return createResponse(500, Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    private Map<String, Object> createResponse(int statusCode, Object body) {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", statusCode);
        response.put("headers", Map.of(
                "Content-Type", "application/json",
                "Access-Control-Allow-Origin", "*",
                "Access-Control-Allow-Methods", "POST, OPTIONS",
                "Access-Control-Allow-Headers", "Content-Type"
        ));

        try {
            String jsonBody = (body instanceof String) ? (String) body : objectMapper.writeValueAsString(body);
            response.put("body", jsonBody);
        } catch (JsonProcessingException e) {
            response.put("body", "{\"error\": \"Error processing response\"}");
        }

        return response;
    }
}