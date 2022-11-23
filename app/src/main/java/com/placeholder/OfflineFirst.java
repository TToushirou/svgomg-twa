package com.placeholder;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.widget.Button;

import com.google.androidbrowserhelper.trusted.LauncherActivity;

public class OfflineFirst extends LauncherActivity {
    @Override
    protected boolean shouldLaunchImmediately() {
        // launchImmediately() returns `false` so we can check connection
        // and then render a fallback page or launch the Trusted Web Activity with `launchTwa()`.
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tryLaunchTwa();
    }

    private void tryLaunchTwa() {
        // If TWA has already launched successfully, launch TWA immediately.
        // Otherwise, check connection status. If online, launch the Trusted Web Activity with `launchTwa()`.
        // Otherwise, if offline, render the offline fallback screen.
        if (hasTwaLaunchedSuccessfully()) {
            launchTwa();
        } else if (isOnline()) {
            firstTimeLaunchTwa();
        } else {
            renderOfflineFallback();
        }
    }

    private boolean isOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean hasTwaLaunchedSuccessfully() {
        // Return `true` if the preference "twa_launched_successfully" has already been set.
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.twa_offline_first_preferences_file_key), Context.MODE_PRIVATE);
        return sharedPref.getBoolean(getString(R.string.twa_launched_successfully), false);
    }

    private void renderOfflineFallback() {
        setContentView(R.layout.activity_offline_first);

        Button retryBtn = this.findViewById(R.id.retry_btn);
        retryBtn.setOnClickListener(v -> {
            // Check connection status. If online, launch the Trusted Web Activity for the first
            // time.
            if (isOnline()) {
                firstTimeLaunchTwa();
            }
        });
    }

    private void firstTimeLaunchTwa() {
        // Launch the TWA and set the preference "twa_launched_successfully" to true, to indicate
        // that it has launched successfully, at least, once.
        launchTwa();

        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.twa_offline_first_preferences_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.twa_launched_successfully), true);
        editor.apply();
    }

}

