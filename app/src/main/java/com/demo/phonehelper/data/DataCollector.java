package com.demo.phonehelper.data;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;

import androidx.core.content.ContextCompat;

import com.demo.phonehelper.BuildConfig;
import com.demo.phonehelper.model.TelemetryReport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class DataCollector {
    private static final Random RANDOM = new Random();
    private static final List<String> DEMO_PACKAGES = Arrays.asList(
            "com.tencent.mobileqq",
            "com.tencent.mm",
            "com.eg.android.AlipayGphone",
            "com.android.chrome",
            "com.ss.android.ugc.aweme"
    );

    private DataCollector() {
    }

    public static TelemetryReport collect(Context context, String actionName) throws JSONException {
        String generatedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date());
        int securityScore = 82 + RANDOM.nextInt(14);
        int junkSizeMb = 320 + RANDOM.nextInt(420);
        int memoryFreedMb = 90 + RANDOM.nextInt(180);

        JSONObject root = new JSONObject();
        root.put("app_name", "手机助手 Pro");
        root.put("package_name", context.getPackageName());
        root.put("action_name", actionName);
        root.put("generated_at", generatedAt);
        root.put("safe_training_mode", BuildConfig.SAFE_TRAINING_MODE);

        JSONObject device = new JSONObject();
        device.put("brand", Build.BRAND);
        device.put("model", Build.MODEL);
        device.put("sdk", Build.VERSION.SDK_INT);
        device.put("release", Build.VERSION.RELEASE);
        device.put("android_id", Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ANDROID_ID
        ));
        device.put("battery_percent", getBatteryPercent(context));
        device.put("network_type", getNetworkType(context));
        root.put("device", device);

        JSONObject permissions = new JSONObject();
        permissions.put("read_sms", permissionState(context, Manifest.permission.READ_SMS));
        permissions.put("read_contacts", permissionState(context, Manifest.permission.READ_CONTACTS));
        permissions.put("fine_location", permissionState(context, Manifest.permission.ACCESS_FINE_LOCATION));
        permissions.put("read_phone_state", permissionState(context, Manifest.permission.READ_PHONE_STATE));
        root.put("runtime_permissions", permissions);

        JSONObject metrics = new JSONObject();
        metrics.put("security_score", securityScore);
        metrics.put("junk_size_mb", junkSizeMb);
        metrics.put("memory_boost_mb", memoryFreedMb);
        metrics.put("storage_available_gb", getAvailableStorageGb());
        metrics.put("cpu_temperature_hint", 33 + RANDOM.nextInt(6));
        metrics.put("network_sessions_detected", 2 + RANDOM.nextInt(5));
        root.put("optimization_metrics", metrics);

        JSONArray observedPackages = new JSONArray();
        for (String packageName : DEMO_PACKAGES) {
            observedPackages.put(packageName);
        }
        root.put("observed_packages_sample", observedPackages);

        JSONArray riskSignals = new JSONArray();
        riskSignals.put("manifest_contains_high_risk_permissions");
        riskSignals.put("hardcoded_destination=" + BuildConfig.C2_URL);
        riskSignals.put("local_report_cache_enabled");
        riskSignals.put("proxy_visible_http_post");
        root.put("risk_signals", riskSignals);

        JSONObject notes = new JSONObject();
        notes.put("operator", "training-demo");
        notes.put("capture_mode", "ui_triggered_background_sync");
        notes.put("remark", "safe training payload; no real SMS or contact bodies are uploaded");
        root.put("notes", notes);

        String deviceSummary = Build.BRAND + " " + Build.MODEL + " · Android " + Build.VERSION.RELEASE;
        String permissionSummary = "短信 " + shortPermission(context, Manifest.permission.READ_SMS)
                + " / 通讯录 " + shortPermission(context, Manifest.permission.READ_CONTACTS)
                + " / 定位 " + shortPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        String riskHeadline = "检测到高风险权限组合与固定外联目标";

        return new TelemetryReport(
                actionName,
                generatedAt,
                securityScore,
                junkSizeMb,
                memoryFreedMb,
                deviceSummary,
                permissionSummary,
                riskHeadline,
                root
        );
    }

    private static String permissionState(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                ? "granted"
                : "denied";
    }

    private static String shortPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                ? "已授权"
                : "未授权";
    }

    private static int getBatteryPercent(Context context) {
        BatteryManager manager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        if (manager == null) {
            return -1;
        }
        return manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }

    private static String getNetworkType(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return "unknown";
        }
        Network network = manager.getActiveNetwork();
        if (network == null) {
            return "offline";
        }
        NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);
        if (capabilities == null) {
            return "unknown";
        }
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return "wifi";
        }
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return "cellular";
        }
        return "other";
    }

    private static double getAvailableStorageGb() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        long availableBytes = statFs.getAvailableBytes();
        return Math.round((availableBytes / 1024d / 1024d / 1024d) * 10d) / 10d;
    }
}
