package com.nbenliogludev;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

/**
 * @author nbenliogludev
 */
public class CrptApi {

    private final int requestLimit;
    private final long windowMillis;
    private final Deque<Long> requestTimestamps = new ArrayDeque<>();

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        if (requestLimit <= 0) {
            throw new IllegalArgumentException("requestLimit must be positive");
        }

        this.requestLimit = requestLimit;
        this.windowMillis = timeUnit.toMillis(1);
    }

    public void createDocument(Document document, String signature) {}
}
