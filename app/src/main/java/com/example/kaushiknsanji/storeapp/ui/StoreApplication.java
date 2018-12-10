package com.example.kaushiknsanji.storeapp.ui;

import android.app.Application;
import android.content.Context;

import com.example.kaushiknsanji.storeapp.utils.AppConstants;
import com.facebook.stetho.Stetho;

/**
 * Custom {@link Application} of the App that is used
 * for initializing Stetho in Debug mode.
 *
 * @author Kaushik N Sanji
 */
public class StoreApplication extends Application {

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.
     * If you override this method, be sure to call super.onCreate().
     */
    @Override
    public void onCreate() {
        super.onCreate();

        if (AppConstants.LOG_STETHO) {
            //When Stetho Logging is enabled (Debug mode only), initialise Stetho
            initializeStetho(this);
        }
    }

    /**
     * Method that Initializes Stetho logging
     *
     * @param context {@link Context} used to initialize Stetho
     */
    private void initializeStetho(final Context context) {
        //Initializing with all Defaults
        Stetho.initializeWithDefaults(context);
    }
}
