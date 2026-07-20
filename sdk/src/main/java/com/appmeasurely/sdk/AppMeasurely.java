package com.appmeasurely.sdk;

import android.app.Application;
import android.content.Context;

/**
 * AppMeasurely Android SDK
 * Mobile attribution and analytics tracking
 *
 * Usage:
 *   AppMeasurely.init(this, "YOUR_APP_KEY");
 */
public class AppMeasurely {

    private static AppMeasurely instance;
    private AMConfig config;
    private AMTracker tracker;
    private AMSession session;
    private boolean initialized = false;

    private AppMeasurely() {}

    /**
     * Initialize the SDK — call this in your Application.onCreate()
     */
    public static void init(Application app, String appKey) {
        if (instance == null) {
            instance = new AppMeasurely();
        }
        instance.config = new AMConfig(appKey);
        instance.tracker = new AMTracker(app, instance.config);
        instance.session = new AMSession(app, instance.tracker);
        instance.initialized = true;

        // Register lifecycle callbacks for automatic session tracking
        app.registerActivityLifecycleCallbacks(instance.session);

        // Track install or app open
        instance.tracker.trackInstallOrOpen(app);
    }

    /**
     * Initialize with custom config
     */
    public static void init(Application app, AMConfig config) {
        if (instance == null) {
            instance = new AppMeasurely();
        }
        instance.config = config;
        instance.tracker = new AMTracker(app, config);
        instance.session = new AMSession(app, instance.tracker);
        instance.initialized = true;

        app.registerActivityLifecycleCallbacks(instance.session);
        instance.tracker.trackInstallOrOpen(app);
    }

    /**
     * Get the singleton instance
     */
    public static AppMeasurely getInstance() {
        if (instance == null || !instance.initialized) {
            throw new IllegalStateException("AppMeasurely not initialized. Call AppMeasurely.init() first.");
        }
        return instance;
    }

    /**
     * Track a custom event
     */
    public static void trackEvent(String eventName) {
        trackEvent(eventName, null);
    }

    /**
     * Track a custom event with properties
     */
    public static void trackEvent(String eventName, java.util.Map<String, Object> properties) {
        if (instance == null || !instance.initialized) return;
        instance.tracker.trackEvent(eventName, properties);
    }

    /**
     * Track revenue
     */
    public static void trackRevenue(double amount, String currency) {
        trackRevenue(amount, currency, "purchase", null);
    }

    /**
     * Track revenue with event name and properties
     */
    public static void trackRevenue(double amount, String currency, String eventName, java.util.Map<String, Object> properties) {
        if (instance == null || !instance.initialized) return;
        instance.tracker.trackRevenue(amount, currency, eventName, properties);
    }

    /**
     * Set custom user properties
     */
    public static void setUserProperty(String key, Object value) {
        if (instance == null || !instance.initialized) return;
        instance.tracker.setUserProperty(key, value);
    }


    /**
     * Set customer user ID — links events across devices
     * Call this after user logs in with your internal user ID
     */
    public static void setCustomUserId(String userId) {
        if (instance == null || !instance.initialized) return;
        instance.tracker.setCustomUserId(userId);
    }

    /**
     * Stop tracking (e.g. when user opts out)
     */
    public static void stop() {
        if (instance != null) {
            instance.initialized = false;
        }
    }
}
