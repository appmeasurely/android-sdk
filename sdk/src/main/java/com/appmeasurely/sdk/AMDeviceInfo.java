package com.appmeasurely.sdk;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Collects device information automatically
 */
public class AMDeviceInfo {

    private final Context context;

    public AMDeviceInfo(Context context) {
        this.context = context.getApplicationContext();
    }

    public String getDeviceId() {
        return Settings.Secure.getString(
            context.getContentResolver(),
            Settings.Secure.ANDROID_ID
        );
    }

    public String getOsVersion() {
        return Build.VERSION.RELEASE;
    }

    public String getDeviceModel() {
        return Build.MANUFACTURER + " " + Build.MODEL;
    }

    public String getAppVersion() {
        try {
            PackageInfo info = context.getPackageManager()
                .getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "unknown";
        }
    }

    public String getCarrier() {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                String carrier = tm.getNetworkOperatorName();
                return carrier != null && !carrier.isEmpty() ? carrier : null;
            }
        } catch (Exception e) {
            // Permission not granted
        }
        return null;
    }

    public String getLanguage() {
        return java.util.Locale.getDefault().getLanguage();
    }

    public int[] getScreenSize() {
        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);
            return new int[]{metrics.widthPixels, metrics.heightPixels};
        } catch (Exception e) {
            return new int[]{0, 0};
        }
    }

    public boolean isFirstLaunch() {
        return !context.getSharedPreferences("am_prefs", Context.MODE_PRIVATE)
            .getBoolean("am_launched", false);
    }

    public void markLaunched() {
        context.getSharedPreferences("am_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("am_launched", true)
            .apply();
    }
}
