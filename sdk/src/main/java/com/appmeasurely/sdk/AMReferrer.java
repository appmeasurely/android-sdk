package com.appmeasurely.sdk;

import android.content.Context;
import android.net.Uri;

/**
 * Parses Android install referrer for attribution
 * Extracts media source, campaign, ad set from Google Play install referrer
 */
public class AMReferrer {

    /**
     * Get the raw install referrer string
     * Note: For full referrer support, add play-install-referrer library
     * This uses SharedPreferences fallback
     */
    public static String getReferrer(Context context) {
        return context.getSharedPreferences("am_prefs", Context.MODE_PRIVATE)
            .getString("am_referrer", null);
    }

    /**
     * Store referrer (call this from your BroadcastReceiver)
     */
    public static void setReferrer(Context context, String referrer) {
        context.getSharedPreferences("am_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("am_referrer", referrer)
            .apply();
    }

    /**
     * Parse media source from referrer URL
     * Supports UTM parameters: utm_source, af_pid, network
     */
    public static String parseMediaSource(String referrer) {
        if (referrer == null) return null;
        try {
            Uri uri = Uri.parse("?" + referrer);
            String source = uri.getQueryParameter("utm_source");
            if (source != null) return source;
            source = uri.getQueryParameter("af_pid");
            if (source != null) return source;
            source = uri.getQueryParameter("network");
            return source;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse campaign name from referrer URL
     */
    public static String parseCampaign(String referrer) {
        if (referrer == null) return null;
        try {
            Uri uri = Uri.parse("?" + referrer);
            String campaign = uri.getQueryParameter("utm_campaign");
            if (campaign != null) return campaign;
            campaign = uri.getQueryParameter("af_c");
            return campaign;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse ad set from referrer URL
     */
    public static String parseAdSet(String referrer) {
        if (referrer == null) return null;
        try {
            Uri uri = Uri.parse("?" + referrer);
            String adSet = uri.getQueryParameter("utm_term");
            if (adSet != null) return adSet;
            adSet = uri.getQueryParameter("af_adset");
            return adSet;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse click ID for attribution matching
     */
    public static String parseClickId(String referrer) {
        if (referrer == null) return null;
        try {
            Uri uri = Uri.parse("?" + referrer);
            String clickId = uri.getQueryParameter("af_click_lookback");
            if (clickId != null) return clickId;
            clickId = uri.getQueryParameter("click_id");
            return clickId;
        } catch (Exception e) {
            return null;
        }
    }
}
