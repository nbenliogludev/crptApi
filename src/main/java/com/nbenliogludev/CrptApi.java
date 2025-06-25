package com.nbenliogludev;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.net.http.HttpClient;
import java.time.Duration;

/**
 * @author nbenliogludev
 */
public class CrptApi {

    private final int requestLimit;
    private final long windowMillis;
    private final String apiBaseUrl;
    private final Deque<Long> requestTimestamps = new ArrayDeque<>();

    private final ObjectMapper mapper = new ObjectMapper();

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public CrptApi(TimeUnit timeUnit, int requestLimit, String apiBaseUrl) {
        if (requestLimit <= 0) {
            throw new IllegalArgumentException("requestLimit must be positive");
        }

        this.requestLimit = requestLimit;
        this.windowMillis = timeUnit.toMillis(1);
        this.apiBaseUrl = Objects.requireNonNull(apiBaseUrl);
    }

    public void createDocument(DocumentRequest req) {
        Objects.requireNonNull(req, "req is null");

        String url = String.format("%s/api/v3/lk/documents/create?pg=%s", apiBaseUrl, req.productGroup.name().toLowerCase());
        String body = mapper.writeValueAsString(req);

        HttpRequest httpReq = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = http.send(httpReq, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 != 2) {
            throw new IOException("API error " + resp.statusCode() + ": " + resp.body());
        }

        ResponseEnvelope envelope = mapper.readValue(resp.body(), ResponseEnvelope.class);
        return envelope.value;
    }

    private static class ResponseEnvelope { public String value; }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DocumentRequest {
        @JsonProperty("document_format")
        public DocumentFormat documentFormat;

        @JsonProperty("product_document")
        public String productDocument;

        @JsonProperty("product_group")
        public ProductGroup productGroup;

        public String signature;
        public DocumentType type;

        public DocumentRequest() { }

        public DocumentRequest(DocumentFormat fmt, String productDocB64, ProductGroup group,
                               String signatureB64, DocumentType type) {
            this.documentFormat = fmt;
            this.productDocument = productDocB64;
            this.productGroup = group;
            this.signature = signatureB64;
            this.type = type;
        }
    }

    public enum DocumentFormat { MANUAL, XML, CSV }

}
