package com.appmeasurely.sdk;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.UUID;

/**
 * Automatically tracks session start and end using Activity lifecycle callbacks
 */
public class AMSession implements Application.ActivityLifecycleCallbacks {

    private static final int SESSION_TIMEOUT_MS = 30000; // 30 seconds

    private final AMTracker tracker;
    private String currentSessionId;
    private long sessionStartTime;
    private int activityCount = 0;
    private boolean sessionActive = false;

    public AMSession(Application app, AMTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public void onActivityStarted(Activity activity) {
        activityCount++;
        if (!sessionActive) {
            startSession();
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        activityCount--;
        if (activityCount == 0) {
            // App going to background — end session after timeout
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (activityCount == 0 && sessionActive) {
                    endSession();
                }
            }, SESSION_TIMEOUT_MS);
        }
    }

    private void startSession() {
        sessionActive = true;
        currentSessionId = UUID.randomUUID().toString();
        sessionStartTime = System.currentTimeMillis();
        tracker.trackSessionStart(currentSessionId);
    }

    private void endSession() {
        if (!sessionActive) return;
        sessionActive = false;
        long durationSeconds = (System.currentTimeMillis() - sessionStartTime) / 1000;
        tracker.trackSessionEnd(currentSessionId, durationSeconds);
    }

    @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
    @Override public void onActivityResumed(Activity activity) {}
    @Override public void onActivityPaused(Activity activity) {}
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
    @Override public void onActivityDestroyed(Activity activity) {}
}
