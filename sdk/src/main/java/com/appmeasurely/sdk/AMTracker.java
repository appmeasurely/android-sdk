package com.appmeasurely.sdk;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Core tracker — builds payloads and sends events to AppMeasurely
 */
public class AMTracker {

    private static final String TAG = "AppMeasurely";
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 2000;

    private final AMConfig config;
    private final AMDeviceInfo deviceInfo;
    private final AMQueue queue;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;
    private final Map<String, Object> userProperties;

    public AMTracker(Context context, AMConfig config) {
        this.config = config;
        this.deviceInfo = new AMDeviceInfo(context);
        this.queue = new AMQueue(context, config.getMaxQueueSize());
        this.executor = Executors.newSingleThreadExecutor();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.userProperties = new HashMap<>();

        // Start periodic flush
        scheduler.scheduleWithFixedDelay(
            this::flushQueue,
            config.getSendIntervalSeconds(),
            config.getSendIntervalSeconds(),
            TimeUnit.SECONDS
        );
    }

    public void trackInstallOrOpen(Context context) {
        boolean isFirst = deviceInfo.isFirstLaunch();
        String eventName = isFirst ? "install" : "app_open";

        JSONObject payload = buildBasePayload(eventName);
        try {
            payload.put("is_first_launch", isFirst);

            // Get install referrer if available
            String referrer = AMReferrer.getReferrer(context);
            if (referrer != null) {
                payload.put("install_referrer", referrer);
                // Parse media source from referrer
                String mediaSource = AMReferrer.parseMediaSource(referrer);
                if (mediaSource != null) payload.put("media_source", mediaSource);
                String campaign = AMReferrer.parseCampaign(referrer);
                if (campaign != null) payload.put("campaign", campaign);
            }
        } catch (Exception e) {
            log("Error building install payload: " + e.getMessage());
        }

        if (isFirst) {
            deviceInfo.markLaunched();
        }

        sendEvent(payload);
    }

    public void trackSessionStart(String sessionId) {
        JSONObject payload = buildBasePayload("session_start");
        try {
            payload.put("session_id", sessionId);
        } catch (Exception e) {
            log("Error building session_start payload");
        }
        sendEvent(payload);
    }

    public void trackSessionEnd(String sessionId, long durationSeconds) {
        JSONObject payload = buildBasePayload("session_end");
        try {
            payload.put("session_id", sessionId);
            payload.put("session_duration", durationSeconds);
        } catch (Exception e) {
            log("Error building session_end payload");
        }
        sendEvent(payload);
    }

    public void trackEvent(String eventName, Map<String, Object> properties) {
        JSONObject payload = buildBasePayload(eventName);
        try {
            if (properties != null && !properties.isEmpty()) {
                JSONObject props = new JSONObject();
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    props.put(entry.getKey(), entry.getValue());
                }
                // Merge user properties
                for (Map.Entry<String, Object> entry : userProperties.entrySet()) {
                    props.put(entry.getKey(), entry.getValue());
                }
                payload.put("properties", props);
            } else if (!userProperties.isEmpty()) {
                JSONObject props = new JSONObject();
                for (Map.Entry<String, Object> entry : userProperties.entrySet()) {
                    props.put(entry.getKey(), entry.getValue());
                }
                payload.put("properties", props);
            }
        } catch (Exception e) {
            log("Error building event payload: " + e.getMessage());
        }
        sendEvent(payload);
    }

    public void trackRevenue(double amount, String currency, String eventName, Map<String, Object> properties) {
        JSONObject payload = buildBasePayload(eventName != null ? eventName : "purchase");
        try {
            payload.put("revenue", amount);
            payload.put("currency", currency != null ? currency : "USD");
            if (properties != null && !properties.isEmpty()) {
                JSONObject props = new JSONObject();
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    props.put(entry.getKey(), entry.getValue());
                }
                payload.put("properties", props);
            }
        } catch (Exception e) {
            log("Error building revenue payload: " + e.getMessage());
        }
        sendEvent(payload);
    }

    public void setUserProperty(String key, Object value) {
        userProperties.put(key, value);
    }

    private JSONObject buildBasePayload(String eventName) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("app_key", config.getAppKey());
            payload.put("event_name", eventName);
            payload.put("device_id", deviceInfo.getDeviceId());
            payload.put("device_type", "android");
            payload.put("os_version", deviceInfo.getOsVersion());
            payload.put("device_model", deviceInfo.getDeviceModel());
            payload.put("app_version", deviceInfo.getAppVersion());
            payload.put("language", deviceInfo.getLanguage());
            payload.put("timestamp", new java.text.SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                java.util.Locale.US
            ).format(new java.util.Date()));

            String carrier = deviceInfo.getCarrier();
            if (carrier != null) payload.put("carrier", carrier);

            int[] screen = deviceInfo.getScreenSize();
            if (screen[0] > 0) {
                payload.put("screen_width", screen[0]);
                payload.put("screen_height", screen[1]);
            }
        } catch (Exception e) {
            log("Error building base payload: " + e.getMessage());
        }
        return payload;
    }

    private void sendEvent(JSONObject payload) {
        // Add to queue first for offline support
        queue.add(payload);
        // Try to flush immediately
        executor.execute(this::flushQueue);
    }

    private void flushQueue() {
        while (!queue.isEmpty()) {
            JSONObject event = queue.poll();
            if (event == null) break;
            boolean success = sendWithRetry(event, MAX_RETRIES);
            if (!success) {
                // Put back in queue for later
                queue.add(event);
                break;
            }
        }
    }

    private boolean sendWithRetry(JSONObject payload, int retriesLeft) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(config.getEndpoint());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("apikey", config.getAppKey());
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            byte[] body = payload.toString().getBytes("UTF-8");
            conn.setFixedLengthStreamingMode(body.length);
            OutputStream os = conn.getOutputStream();
            os.write(body);
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            log("Event sent: " + payload.optString("event_name") + " → " + responseCode);

            if (responseCode == 429) {
                // Rate limited — stop sending
                log("Rate limited — will retry later");
                return false;
            }

            return responseCode >= 200 && responseCode < 300;

        } catch (Exception e) {
            log("Send error: " + e.getMessage());
            if (retriesLeft > 0) {
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                return sendWithRetry(payload, retriesLeft - 1);
            }
            return false;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private void log(String message) {
        if (config.isDebugMode()) {
            Log.d(TAG, message);
        }
    }
}
