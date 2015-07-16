package no.avinet.simplecrashhandler;

import android.content.Context;
import android.content.Intent;

public class SimpleUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    protected Context application;
    protected Thread.UncaughtExceptionHandler defaultUEH;

    public SimpleUncaughtExceptionHandler(Context application) {
        this.application = application;
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (application != null) {
            Intent intent = new Intent(application, SimpleCrashService.class);
            intent.putExtra("occurred", System.currentTimeMillis());
            intent.putExtra("ex", ex);
            intent.putExtra("threadName", thread.getName());
            intent.putExtra("threadStackTrace", thread.getStackTrace());
            application.startService(intent);
        }

        if (defaultUEH != null) {
            defaultUEH.uncaughtException(thread, ex);
        }
    }
}
