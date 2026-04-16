package com.demo.phonehelper.data;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public final class StorageManager {
    private StorageManager() {
    }

    public static String save(Context context, String jsonData) throws Exception {
        File reportsDir = new File(context.getFilesDir(), "reports");
        if (!reportsDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            reportsDir.mkdirs();
        }

        File output = new File(reportsDir, "device_info_" + System.currentTimeMillis() + ".json");
        try (FileOutputStream fos = new FileOutputStream(output)) {
            fos.write(jsonData.getBytes(StandardCharsets.UTF_8));
            fos.flush();
        }
        return output.getAbsolutePath();
    }
}
