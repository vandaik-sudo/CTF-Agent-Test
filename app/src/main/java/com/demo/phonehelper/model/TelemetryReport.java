package com.demo.phonehelper.model;

import org.json.JSONObject;

public class TelemetryReport {
    private final String actionName;
    private final String generatedAt;
    private final int securityScore;
    private final int junkSizeMb;
    private final int memoryFreedMb;
    private final String deviceSummary;
    private final String permissionSummary;
    private final String riskHeadline;
    private final JSONObject payload;

    public TelemetryReport(
            String actionName,
            String generatedAt,
            int securityScore,
            int junkSizeMb,
            int memoryFreedMb,
            String deviceSummary,
            String permissionSummary,
            String riskHeadline,
            JSONObject payload
    ) {
        this.actionName = actionName;
        this.generatedAt = generatedAt;
        this.securityScore = securityScore;
        this.junkSizeMb = junkSizeMb;
        this.memoryFreedMb = memoryFreedMb;
        this.deviceSummary = deviceSummary;
        this.permissionSummary = permissionSummary;
        this.riskHeadline = riskHeadline;
        this.payload = payload;
    }

    public String getActionName() {
        return actionName;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public int getSecurityScore() {
        return securityScore;
    }

    public int getJunkSizeMb() {
        return junkSizeMb;
    }

    public int getMemoryFreedMb() {
        return memoryFreedMb;
    }

    public String getDeviceSummary() {
        return deviceSummary;
    }

    public String getPermissionSummary() {
        return permissionSummary;
    }

    public String getRiskHeadline() {
        return riskHeadline;
    }

    public JSONObject getPayload() {
        return payload;
    }

    public String toJsonString() {
        return payload.toString();
    }
}
