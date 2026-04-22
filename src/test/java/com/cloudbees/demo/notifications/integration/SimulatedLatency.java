package com.cloudbees.demo.notifications.integration;

/**
 * Small helper used by integration tests to model the wall-clock cost of
 * real I/O against provider sandboxes (SES sandbox, Twilio test creds,
 * webhook fixtures). The duration is tunable via the
 * {@code latency.scale} system property — set to e.g. {@code 0.1} to run
 * the suite quickly during local dev, default is {@code 1.0}.
 */
final class SimulatedLatency {

    private static final double SCALE =
            Double.parseDouble(System.getProperty("latency.scale", "1.0"));

    private SimulatedLatency() {}

    static void brief() { sleep(5_000); }

    static void standard() { sleep(15_000); }

    static void slow() { sleep(30_000); }

    private static void sleep(long baseMs) {
        long ms = Math.max(0L, (long) (baseMs * SCALE));
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
