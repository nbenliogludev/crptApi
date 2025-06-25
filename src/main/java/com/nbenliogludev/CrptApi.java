package com.nbenliogludev;

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

    public void createDocument(Document document, String signature) {}
}
