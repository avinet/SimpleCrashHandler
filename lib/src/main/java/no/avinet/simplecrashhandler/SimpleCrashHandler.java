package no.avinet.simplecrashhandler;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

public class SimpleCrashHandler {
    public static void init(Context context) {
        Context application = context.getApplicationContext();
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof SimpleUncaughtExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new SimpleUncaughtExceptionHandler(application));
        }

        Intent intent = new Intent(application, SimpleCrashService.class);
        intent.putExtra("wakeup", true);
        application.startService(intent);

        Log.d("SimpleCrashHandler", "Initialized");

        if (context.checkCallingOrSelfPermission("android.permission.INTERNET") != PackageManager.PERMISSION_GRANTED) {
            Log.w("SimpleCrashHandler", "Permission to use internet is not granted. No crashes will be uploaded.");
        }
    }
}
