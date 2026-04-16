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
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * 数据采集模块 —— 模拟刷单兼职诈骗APP的隐蔽采集行为
 *
 * 采集内容：
 *   1. 设备信息（型号、IMEI、Android ID等）
 *   2. 用户注册的敏感身份信息（身份证、银行卡、手机号）
 *   3. 通讯录（用于后续威胁催收）
 *   4. 短信（用于窃取银行验证码）
 *   5. 精确定位
 *   6. 已安装应用列表
 *   7. SDK 上报链路元数据（友盟、个推、连连支付、阿里云实人认证）
 *   8. 任务系统中的充值诱导记录
 *
 * ⚠ 仅用于反诈培训教学演示，所有数据均为虚构。
 */
public final class DataCollector {

    private static final Random RNG = new Random();

    private DataCollector() {
    }

    // ────────────────────────────────────────────────────────────────────────
    // 主采集入口
    // ────────────────────────────────────────────────────────────────────────

    public static TelemetryReport collect(Context ctx, String actionName) throws JSONException {
        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date());
        int creditScore = 72 + RNG.nextInt(18);
        int junkMb      = 320 + RNG.nextInt(420);
        int memMb       = 90  + RNG.nextInt(180);

        JSONObject root = new JSONObject();

        root.put("app_name",     "众益淘");
        root.put("app_version",  "3.2.1");
        root.put("package_name", ctx.getPackageName());
        root.put("action_name",  actionName);
        root.put("generated_at", ts);

        root.put("sdk_info",           buildSdkInfo());
        root.put("device",             buildDevice(ctx));
        root.put("user_profile",       buildUserProfile());
        root.put("stolen_contacts",    buildContacts());
        root.put("stolen_sms",         buildSms());
        root.put("location",           buildLocation(ts));
        root.put("installed_apps",     buildInstalledApps());
        root.put("task_history",       buildTaskHistory());
        root.put("runtime_permissions",buildPermissions(ctx));

        JSONObject metrics = new JSONObject();
        metrics.put("credit_score",     creditScore);
        metrics.put("junk_size_mb",     junkMb);
        metrics.put("memory_boost_mb",  memMb);
        metrics.put("storage_avail_gb", getAvailableStorageGb());
        metrics.put("cpu_temp_hint",    33 + RNG.nextInt(6));
        root.put("optimization_metrics", metrics);

        root.put("risk_signals",  buildRiskSignals());
        root.put("c2_endpoint",  BuildConfig.C2_URL);

        JSONObject notes = new JSONObject();
        notes.put("operator",  "training-demo");
        notes.put("purpose",   "反诈培训教学演示用途");
        notes.put("remark",    "本数据包模拟刷单类诈骗APP窃取的用户敏感信息，所有个人信息均为虚构，仅供教学使用。");
        root.put("notes", notes);

        String deviceSummary = Build.BRAND + " " + Build.MODEL + " · Android " + Build.VERSION.RELEASE;
        String permSummary = "短信 " + sp(ctx, Manifest.permission.READ_SMS)
                + " / 通讯录 " + sp(ctx, Manifest.permission.READ_CONTACTS)
                + " / 定位 " + sp(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                + " / 通话记录 " + sp(ctx, Manifest.permission.READ_CALL_LOG);
        String headline = "检测到高风险权限组合与固定外联目标";

        return new TelemetryReport(actionName, ts, creditScore, junkMb, memMb,
                deviceSummary, permSummary, headline, root);
    }

    // ────────────────────────────────────────────────────────────────────────
    // SDK 供应商信息（ SDK调证关键证据 ）
    // ────────────────────────────────────────────────────────────────────────

    private static JSONObject buildSdkInfo() throws JSONException {
        JSONObject sdk = new JSONObject();

        JSONObject analytics = new JSONObject();
        analytics.put("name",    "友盟+统计SDK");
        analytics.put("vendor",  "北京锐讯灵通科技有限公司");
        analytics.put("appkey",  "6731a2d5e4b0f28e9a1c3d5f");
        analytics.put("version", "9.6.8");
        sdk.put("analytics", analytics);

        JSONObject push = new JSONObject();
        push.put("name",    "个推消息推送SDK");
        push.put("vendor",  "浙江每日互动网络科技股份有限公司");
        push.put("appid",   "gt_AbCdEf1234567890");
        push.put("version", "3.2.14");
        sdk.put("push", push);

        JSONObject payment = new JSONObject();
        payment.put("name",        "连连支付SDK");
        payment.put("vendor",      "连连银通电子支付有限公司");
        payment.put("merchant_id", "LL20240315001892");
        payment.put("version",     "5.1.2");
        sdk.put("payment", payment);

        JSONObject identity = new JSONObject();
        identity.put("name",     "阿里云实人认证SDK");
        identity.put("vendor",   "阿里云计算有限公司");
        identity.put("scene_id", "ic_aliyun_face_001");
        identity.put("version",  "2.4.6");
        sdk.put("identity_verify", identity);

        return sdk;
    }

    // ────────────────────────────────────────────────────────────────────────
    // 设备指纹
    // ────────────────────────────────────────────────────────────────────────

    private static JSONObject buildDevice(Context ctx) throws JSONException {
        JSONObject d = new JSONObject();
        d.put("brand",           Build.BRAND);
        d.put("model",           Build.MODEL);
        d.put("sdk_int",         Build.VERSION.SDK_INT);
        d.put("release",         Build.VERSION.RELEASE);
        d.put("android_id",      Settings.Secure.getString(
                ctx.getContentResolver(), Settings.Secure.ANDROID_ID));
        d.put("imei",            "86123456789" + (1000 + RNG.nextInt(9000)));
        d.put("battery_pct",     getBattery(ctx));
        d.put("network_type",    getNetType(ctx));
        d.put("screen_density",  ctx.getResources().getDisplayMetrics().densityDpi);
        d.put("language",        Locale.getDefault().toString());
        return d;
    }

    // ────────────────────────────────────────────────────────────────────────
    // 用户注册身份信息（注册时骗取）
    // ────────────────────────────────────────────────────────────────────────

    private static JSONObject buildUserProfile() throws JSONException {
        JSONObject u = new JSONObject();
        u.put("phone",           "138****2756");
        u.put("real_name",       "张某某");
        u.put("id_card_prefix",  "3301********");
        u.put("id_card_tail",    "****2019");
        u.put("bank_card_no",    "6222 **** **** 8832");
        u.put("bank_name",       "中国工商银行");
        u.put("bindcard_time",   "2026-04-10 09:15:22");
        u.put("alipay_bindid",   "zhangxx@163.com");
        u.put("wechat_openid",   "oXYZ_fake_openid_1234567");
        return u;
    }

    // ────────────────────────────────────────────────────────────────────────
    // 窃取的通讯录
    // ────────────────────────────────────────────────────────────────────────

    private static JSONArray buildContacts() throws JSONException {
        JSONArray arr = new JSONArray();
        arr.put(contact("老婆",    "139****1234", "2026-04-15"));
        arr.put(contact("爸爸",    "136****5678", "2026-03-20"));
        arr.put(contact("妈妈",    "137****4321", "2026-02-10"));
        arr.put(contact("王主任",  "158****9012", "2026-04-01"));
        arr.put(contact("张经理",  "131****3456", "2026-02-15"));
        arr.put(contact("李会计",  "155****7890", "2026-04-10"));
        arr.put(contact("赵老师",  "182****6543", "2026-01-05"));
        arr.put(contact("刘同事",  "176****2109", "2026-03-28"));
        return arr;
    }

    private static JSONObject contact(String name, String phone, String date) throws JSONException {
        JSONObject c = new JSONObject();
        c.put("name", name);
        c.put("phone", phone);
        c.put("last_updated", date);
        return c;
    }

    // ────────────────────────────────────────────────────────────────────────
    // 窃取的短信（银行通知 + 验证码）
    // ────────────────────────────────────────────────────────────────────────

    private static JSONArray buildSms() throws JSONException {
        JSONArray arr = new JSONArray();

        arr.put(sms("95588",
                "【工商银行】您尾号8832的账户于04月15日消费支出500.00元，余额12,350.28元。",
                "2026-04-15 18:23:45"));

        arr.put(sms("10086",
                "【中国移动】您的验证码为692831，5分钟内有效，请勿泄露给任何人。",
                "2026-04-16 10:05:12"));

        arr.put(sms("95533",
                "【建设银行】您的信用卡（尾号6621）04月账单金额2,180.00元，到期还款日05月15日。",
                "2026-04-12 09:00:00"));

        arr.put(sms("106902",
                "【众益淘】恭喜您完成新手任务，佣金3.50元已到账，点击 t.cn/xxxxx 查看提现进度。",
                "2026-04-15 14:30:00"));

        arr.put(sms("95555",
                "【招商银行】您的一网通账户于04月14日收入0.01元，活期余额8,762.19元。",
                "2026-04-14 16:10:33"));

        return arr;
    }

    private static JSONObject sms(String from, String body, String time) throws JSONException {
        JSONObject s = new JSONObject();
        s.put("sender", from);
        s.put("body", body);
        s.put("timestamp", time);
        return s;
    }

    // ────────────────────────────────────────────────────────────────────────
    // 精确定位
    // ────────────────────────────────────────────────────────────────────────

    private static JSONObject buildLocation(String ts) throws JSONException {
        JSONObject loc = new JSONObject();
        loc.put("latitude",     30.2741 + RNG.nextDouble() * 0.005);
        loc.put("longitude",    120.1551 + RNG.nextDouble() * 0.005);
        loc.put("accuracy_m",   10 + RNG.nextInt(20));
        loc.put("address_hint", "浙江省杭州市西湖区文三路XX号");
        loc.put("captured_at",  ts);
        return loc;
    }

    // ────────────────────────────────────────────────────────────────────────
    // 已安装应用列表
    // ────────────────────────────────────────────────────────────────────────

    private static JSONArray buildInstalledApps() {
        JSONArray arr = new JSONArray();
        arr.put("com.tencent.mm");
        arr.put("com.tencent.mobileqq");
        arr.put("com.eg.android.AlipayGphone");
        arr.put("com.taobao.taobao");
        arr.put("com.jingdong.app.mall");
        arr.put("com.sina.weibo");
        arr.put("com.ss.android.ugc.aweme");
        arr.put("com.icbc.im");
        arr.put("com.chinamworld.bocmbci");
        arr.put("com.cmbchina.ccd.pluto.cmbActivity");
        return arr;
    }

    // ────────────────────────────────────────────────────────────────────────
    // 任务与佣金记录 —— 典型刷单诈骗套路
    // 先小额返利建立信任，再诱导大额充值
    // ────────────────────────────────────────────────────────────────────────

    private static JSONArray buildTaskHistory() throws JSONException {
        JSONArray arr = new JSONArray();
        arr.put(task("T20260415001", "商品好评点赞",  "3.50",    "已完成", "佣金已到账"));
        arr.put(task("T20260415002", "店铺收藏关注",  "2.00",    "已完成", "佣金已到账"));
        arr.put(task("T20260415003", "浏览商品60秒",  "1.50",    "已完成", "佣金已到账"));
        arr.put(task("T20260416001", "组合充值任务",  "120.00",  "待支付", "需先充值120元解锁高级任务包"));
        arr.put(task("T20260416002", "VIP升级大礼包", "500.00",  "待支付", "充值500元可解锁日入300+通道"));
        arr.put(task("T20260416003", "连单冲量任务",  "2000.00", "待支付", "前期本金+佣金将一次性结算"));
        return arr;
    }

    private static JSONObject task(String id, String type, String amount,
                                    String status, String remark) throws JSONException {
        JSONObject t = new JSONObject();
        t.put("task_id", id);
        t.put("type", type);
        t.put("amount", amount);
        t.put("status", status);
        t.put("remark", remark);
        return t;
    }

    // ────────────────────────────────────────────────────────────────────────
    // 权限状态
    // ────────────────────────────────────────────────────────────────────────

    private static JSONObject buildPermissions(Context ctx) throws JSONException {
        JSONObject p = new JSONObject();
        p.put("read_sms",         ps(ctx, Manifest.permission.READ_SMS));
        p.put("read_contacts",    ps(ctx, Manifest.permission.READ_CONTACTS));
        p.put("fine_location",    ps(ctx, Manifest.permission.ACCESS_FINE_LOCATION));
        p.put("read_phone_state", ps(ctx, Manifest.permission.READ_PHONE_STATE));
        p.put("read_call_log",    ps(ctx, Manifest.permission.READ_CALL_LOG));
        p.put("camera",           ps(ctx, Manifest.permission.CAMERA));
        return p;
    }

    // ────────────────────────────────────────────────────────────────────────
    // 风险信号
    // ────────────────────────────────────────────────────────────────────────

    private static JSONArray buildRiskSignals() {
        JSONArray arr = new JSONArray();
        arr.put("大量敏感权限申请（短信、通讯录、定位、通话记录、相机）");
        arr.put("静默采集用户通讯录并上传至远端服务器");
        arr.put("读取短信内容，包含银行交易通知与验证码");
        arr.put("精确GPS定位信息持续上报");
        arr.put("注册流程骗取真实姓名、身份证号、银行卡号");
        arr.put("内嵌多个第三方SDK（友盟统计、个推推送、连连支付、阿里云实人认证）");
        arr.put("任务系统存在典型刷单诈骗模式：小额返利→大额充值诱导");
        arr.put("硬编码C2外联地址：" + BuildConfig.C2_URL);
        return arr;
    }

    // ── 工具方法 ──

    private static String ps(Context ctx, String perm) {
        return ContextCompat.checkSelfPermission(ctx, perm) == PackageManager.PERMISSION_GRANTED
                ? "granted" : "denied";
    }

    private static String sp(Context ctx, String perm) {
        return ContextCompat.checkSelfPermission(ctx, perm) == PackageManager.PERMISSION_GRANTED
                ? "已授权" : "未授权";
    }

    private static int getBattery(Context ctx) {
        BatteryManager bm = (BatteryManager) ctx.getSystemService(Context.BATTERY_SERVICE);
        return bm == null ? -1 : bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }

    private static String getNetType(Context ctx) {
        try {
            ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return "unknown";
            Network net = cm.getActiveNetwork();
            if (net == null) return "offline";
            NetworkCapabilities cap = cm.getNetworkCapabilities(net);
            if (cap == null) return "unknown";
            if (cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return "wifi";
            if (cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) return "cellular";
            return "other";
        } catch (SecurityException e) {
            return "restricted";
        }
    }

    private static double getAvailableStorageGb() {
        StatFs fs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        return Math.round((fs.getAvailableBytes() / 1024d / 1024d / 1024d) * 10d) / 10d;
    }
}
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
        try {
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
        } catch (SecurityException ex) {
            return "restricted";
        }
    }

    private static double getAvailableStorageGb() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        long availableBytes = statFs.getAvailableBytes();
        return Math.round((availableBytes / 1024d / 1024d / 1024d) * 10d) / 10d;
    }
}
