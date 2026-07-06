package com.appmeasurely.sdk;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Offline event queue — stores events locally and sends when network is available
 */
public class AMQueue {

    private static final String PREF_NAME = "am_event_queue";
    private static final String PREF_KEY = "queued_events";

    private final Context context;
    private final int maxSize;
    private final Queue<JSONObject> memoryQueue;

    public AMQueue(Context context, int maxSize) {
        this.context = context.getApplicationContext();
        this.maxSize = maxSize;
        this.memoryQueue = new LinkedList<>();
        loadFromDisk();
    }

    public synchronized void add(JSONObject event) {
        if (memoryQueue.size() >= maxSize) {
            memoryQueue.poll(); // Remove oldest
        }
        memoryQueue.add(event);
        saveToDisk();
    }

    public synchronized JSONObject poll() {
        JSONObject event = memoryQueue.poll();
        if (event != null) saveToDisk();
        return event;
    }

    public synchronized int size() {
        return memoryQueue.size();
    }

    public synchronized boolean isEmpty() {
        return memoryQueue.isEmpty();
    }

    public synchronized void clear() {
        memoryQueue.clear();
        saveToDisk();
    }

    private void saveToDisk() {
        try {
            JSONArray array = new JSONArray();
            for (JSONObject event : memoryQueue) {
                array.put(event);
            }
            SharedPreferences.Editor editor = context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit();
            editor.putString(PREF_KEY, array.toString());
            editor.apply();
        } catch (Exception e) {
            // Silent fail
        }
    }

    private void loadFromDisk() {
        try {
            String stored = context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(PREF_KEY, null);
            if (stored != null) {
                JSONArray array = new JSONArray(stored);
                for (int i = 0; i < array.length(); i++) {
                    memoryQueue.add(array.getJSONObject(i));
                }
            }
        } catch (Exception e) {
            // Silent fail
        }
    }
}
