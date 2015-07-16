package no.avinet.simplecrashhandler.sample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import no.avinet.simplecrashhandler.SimpleCrashHandler;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SimpleCrashHandler.init(this);

        setContentView(R.layout.activity_main);
        findViewById(R.id.crash_now).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                throw new RuntimeException("This is a sample crash");
            }
        });
    }

}
