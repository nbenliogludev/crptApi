package com.nbenliogludev;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * @author nbenliogludev
 */
public class Main {
    public static void main(String[] args) {
        String baseUrl = "https://ismp.crpt.ru";

        String bearerToken = // Replace with your JWT token
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."
                        + "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0."
                        + "KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30";

        String signatureB64= "signature_base64_string_here"; // Replace with your CMS signature in Base64

        CrptApi api = new CrptApi(TimeUnit.SECONDS, 100, baseUrl);

        String jsonDoc = "{\n" +
                "  \"description\": {\"participantInn\": \"123456789012\"},\n" +
                "  \"doc_id\": \"123e4567-e89b-12d3-a456-426614174000\",\n" +
                "  \"doc_type\": \"LP_INTRODUCE_GOODS\"\n" +
                "}";

        CrptApi.DocumentRequest request = new CrptApi.DocumentRequest(
                CrptApi.DocumentFormat.MANUAL,
                Base64.getEncoder().encodeToString(jsonDoc.getBytes()),
                CrptApi.ProductGroup.MILK,
                signatureB64,
                CrptApi.DocumentType.LP_INTRODUCE_GOODS
        );

        try {
            String uuid = api.createDocument(request, bearerToken);
            System.out.println("Document accepted, UUID: " + uuid);
        } catch (Exception e) {
            System.err.println("Error creating document: " + e.getMessage());
            e.printStackTrace();
        }
    }
}