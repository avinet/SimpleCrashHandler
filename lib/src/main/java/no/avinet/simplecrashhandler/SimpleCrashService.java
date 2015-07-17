package no.avinet.simplecrashhandler;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SimpleCrashService extends IntentService {
    public SimpleCrashService() {
        super("CrashService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String json = null;
        try {
            Bundle extras = intent.getExtras();

            if (extras.getBoolean("wakeup", false)) {
                processSavedCrashReports();
                return;
            }

            String packageName = getPackageName();
            PackageInfo packageInfo = getPackageManager().getPackageInfo(packageName, 0);
            Throwable ex = (Throwable) extras.getSerializable("ex");

            JSONObject crashReport = new JSONObject();
            crashReport.put("occurred", extras.getLong("occurred"));
            crashReport.put("packageName", packageName);
            crashReport.put("versionName", packageInfo.versionName);
            crashReport.put("versionCode", packageInfo.versionCode);
            crashReport.put("threadName", extras.getString("threadName"));
            crashReport.put("exceptionName", ex.getClass().getName());
            crashReport.put("exceptionMessage", ex.getMessage());
            crashReport.put("stackTrace", getStackTrace(ex));
            crashReport.put("buildID", Build.ID);
            crashReport.put("buildDisplay", Build.DISPLAY);
            crashReport.put("buildProduct", Build.PRODUCT);
            crashReport.put("buildDevice", Build.DEVICE);
            crashReport.put("buildBoard", Build.BOARD);
            crashReport.put("buildManufacturer", Build.MANUFACTURER);
            crashReport.put("buildBrand", Build.BRAND);
            crashReport.put("buildModel", Build.MODEL);
            crashReport.put("buildBootloader", Build.BOOTLOADER);
            crashReport.put("buildHardware", Build.HARDWARE);
            crashReport.put("buildSerial", Build.SERIAL);
            crashReport.put("buildType", Build.TYPE);
            crashReport.put("buildTags", Build.TAGS);
            crashReport.put("sdkVersion", Build.VERSION.SDK_INT);
            json = crashReport.toString(2);

            if (!isOnline() || !sendCrashReport(json))
                saveForLater(json);
        } catch (Exception e) {
            if (json != null)
                saveForLater(json);
        }
    }

    protected boolean isOnline() {
        if (checkCallingOrSelfPermission("android.permission.INTERNET") != PackageManager.PERMISSION_GRANTED)
            return false;

        if (checkCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE") != PackageManager.PERMISSION_GRANTED)
            return true; // Cannot determine, assume online

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        }

        return ni.isConnected();
    }

    protected String getStackTrace(Throwable ex) {
        String stackTrace = toStackTraceString(ex.getStackTrace());

        Throwable cause = ex.getCause();
        if (cause != null) {
            stackTrace += "Caused by: " + cause.getClass().getName() + ": " + cause.getMessage() + "\n";
            stackTrace += toStackTraceString(cause.getStackTrace());
        }

        return stackTrace;
    }

    protected String toStackTraceString(StackTraceElement[] stackTraceElements) {
        String stackTrace = "";
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            stackTrace += "\tat " + stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() +
                    "(" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + ")\n";
        }
        return stackTrace;
    }

    protected boolean sendCrashReport(String json) throws PackageManager.NameNotFoundException, IOException {
        ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        String crashUrl = (String) applicationInfo.metaData.get("no.avinet.simplecrashhandler.CRASH_URL");

        URL url = new URL(crashUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        // To allow filtering crash reports without parsing JSON payload
        connection.setRequestProperty("X-Crash-Package-Name", getPackageName());

        byte[] jsonBytes = json.getBytes();
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length", "" + jsonBytes.length);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(jsonBytes);
        outputStream.close();

        return connection.getResponseCode() == 200;
    }

    private void processSavedCrashReports() {
        if (!isOnline())
            return;

        SharedPreferences preferences = getSharedPreferences("SimpleCrashHandler", 0);
        String crash = preferences.getString("crash", null);
        int crashAttempts = preferences.getInt("crashAttempts", 1);
        if (crash != null) {
            try {
                if (!sendCrashReport(crash) && crashAttempts < 3)
                    throw new RuntimeException();

                preferences.edit()
                        .remove("crash")
                        .remove("crashAttempts")
                        .commit();
            } catch (Exception ex) {
                preferences.edit()
                        .putInt("crashAttempts", crashAttempts + 1)
                        .commit();
            }
        }
    }

    private void saveForLater(String json) {
        // TODO Support multiple cached crashes
        getSharedPreferences("SimpleCrashHandler", 0)
                .edit()
                .putString("crash", json)
                .putInt("crashAttempts", 1)
                .commit();
    }
}
