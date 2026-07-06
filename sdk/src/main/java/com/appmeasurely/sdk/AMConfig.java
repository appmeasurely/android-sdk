package com.appmeasurely.sdk;

/**
 * Configuration for AppMeasurely SDK
 */
public class AMConfig {

    private static final String DEFAULT_ENDPOINT = "https://uqvknwgcpptxnbmsubkc.supabase.co/functions/v1/track-mobile";

    private String appKey;
    private String endpoint;
    private boolean debugMode;
    private boolean trackSessions;
    private boolean trackInstalls;
    private int sendIntervalSeconds;
    private int maxQueueSize;

    /**
     * Basic config with just app key
     */
    public AMConfig(String appKey) {
        this.appKey = appKey;
        this.endpoint = DEFAULT_ENDPOINT;
        this.debugMode = false;
        this.trackSessions = true;
        this.trackInstalls = true;
        this.sendIntervalSeconds = 30;
        this.maxQueueSize = 100;
    }

    // Builder pattern
    public AMConfig setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public AMConfig setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        return this;
    }

    public AMConfig setTrackSessions(boolean trackSessions) {
        this.trackSessions = trackSessions;
        return this;
    }

    public AMConfig setTrackInstalls(boolean trackInstalls) {
        this.trackInstalls = trackInstalls;
        return this;
    }

    public AMConfig setSendInterval(int seconds) {
        this.sendIntervalSeconds = seconds;
        return this;
    }

    public AMConfig setMaxQueueSize(int size) {
        this.maxQueueSize = size;
        return this;
    }

    // Getters
    public String getAppKey() { return appKey; }
    public String getEndpoint() { return endpoint; }
    public boolean isDebugMode() { return debugMode; }
    public boolean isTrackSessions() { return trackSessions; }
    public boolean isTrackInstalls() { return trackInstalls; }
    public int getSendIntervalSeconds() { return sendIntervalSeconds; }
    public int getMaxQueueSize() { return maxQueueSize; }
}
