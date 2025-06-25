package com.nbenliogludev;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
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

    /* ====================  Limit controls  ==================== */

    private synchronized void acquirePermit() throws InterruptedException {
        while (true) {
            long now = System.currentTimeMillis();
            purge(now);
            if (requestTimestamps.size() < requestLimit) {
                requestTimestamps.addLast(now);
                return;
            }
            long sleep = windowMillis - (now - requestTimestamps.peekFirst());
            if (sleep > 0) this.wait(sleep);
        }
    }

    private void purge(long now) {
        while (!requestTimestamps.isEmpty() && now - requestTimestamps.peekFirst() >= windowMillis) {
            requestTimestamps.pollFirst();
        }
        if (requestTimestamps.size() < requestLimit) this.notifyAll();
    }

    /* ====================  DTOs  ==================== */

    public enum DocumentType {
        AGGREGATION_DOCUMENT,
        AGGREGATION_DOCUMENT_CSV,
        AGGREGATION_DOCUMENT_XML,
        DISAGGREGATION_DOCUMENT,
        DISAGGREGATION_DOCUMENT_CSV,
        DISAGGREGATION_DOCUMENT_XML,
        REAGGREGATION_DOCUMENT,
        REAGGREGATION_DOCUMENT_CSV,
        REAGGREGATION_DOCUMENT_XML,
        LP_INTRODUCE_GOODS,
        LP_SHIP_GOODS,
        LP_SHIP_GOODS_CSV,
        LP_SHIP_GOODS_XML,
        LP_INTRODUCE_GOODS_CSV,
        LP_INTRODUCE_GOODS_XML,
        LP_ACCEPT_GOODS,
        LP_ACCEPT_GOODS_XML,
        LK_REMARK,
        LK_REMARK_CSV,
        LK_REMARK_XML,
        LK_RECEIPT,
        LK_RECEIPT_XML,
        LK_RECEIPT_CSV,
        LP_GOODS_IMPORT,
        LP_GOODS_IMPORT_CSV,
        LP_GOODS_IMPORT_XML,
        LP_CANCEL_SHIPMENT,
        LP_CANCEL_SHIPMENT_CSV,
        LP_CANCEL_SHIPMENT_XML,
        LK_KM_CANCELLATION,
        LK_KM_CANCELLATION_CSV,
        LK_KM_CANCELLATION_XML,
        LK_APPLIED_KM_CANCELLATION,
        LK_APPLIED_KM_CANCELLATION_CSV,
        LK_APPLIED_KM_CANCELLATION_XML,
        LK_CONTRACT_COMMISSIONING,
        LK_CONTRACT_COMMISSIONING_CSV,
        LK_CONTRACT_COMMISSIONING_XML,
        LK_INDI_COMMISSIONING,
        LK_INDI_COMMISSIONING_CSV,
        LK_INDI_COMMISSIONING_XML,
        LP_SHIP_RECEIPT,
        LP_SHIP_RECEIPT_CSV,
        LP_SHIP_RECEIPT_XML,
        OST_DESCRIPTION,
        OST_DESCRIPTION_CSV,
        OST_DESCRIPTION_XML,
        CROSSBORDER,
        CROSSBORDER_CSV,
        CROSSBORDER_XML,
        LP_INTRODUCE_OST,
        LP_INTRODUCE_OST_CSV,
        LP_INTRODUCE_OST_XML,
        LP_RETURN,
        LP_RETURN_CSV,
        LP_RETURN_XML,
        LP_SHIP_GOODS_CROSSBORDER,
        LP_SHIP_GOODS_CROSSBORDER_CSV,
        LP_SHIP_GOODS_CROSSBORDER_XML,
        LP_CANCEL_SHIPMENT_CROSSBORDER,
        @JsonEnumDefaultValue UNKNOWN
    }

    public enum ProductGroup {
        CLOTHES,
        SHOES,
        TOBACCO,
        PERFUMERY,
        TIRES,
        ELECTRONICS,
        PHARMA,
        MILK,
        BICYCLE,
        WHEELCHAIRS,
        @JsonEnumDefaultValue UNKNOWN
    }

    public enum DocumentFormat { MANUAL, XML, CSV }

}
