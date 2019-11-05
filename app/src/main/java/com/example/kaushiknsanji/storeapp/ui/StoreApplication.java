/*
 * Copyright 2018 Kaushik N. Sanji
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
