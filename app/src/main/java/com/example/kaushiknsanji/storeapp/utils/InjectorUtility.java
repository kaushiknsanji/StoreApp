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

package com.example.kaushiknsanji.storeapp.utils;

import android.content.Context;

import com.example.kaushiknsanji.storeapp.data.StoreRepository;
import com.example.kaushiknsanji.storeapp.data.local.StoreFileRepository;
import com.example.kaushiknsanji.storeapp.data.local.StoreLocalRepository;

/**
 * Utility class that injects required dependencies into the Model-View-Presenter framework.
 *
 * @author Kaushik N Sanji
 */
public final class InjectorUtility {

    /**
     * Private Constructor to avoid instantiating {@link InjectorUtility}
     */
    private InjectorUtility() {
        //Suppressing with an error to enforce noninstantiability
        throw new AssertionError("No " + this.getClass().getCanonicalName() + " instances for you!");
    }

    /**
     * Method that provides/injects the {@link StoreLocalRepository} instance which
     * deals with the database.
     *
     * @param context A {@link Context} to derive the {@link android.content.ContentResolver} instance
     * @return Instance of {@link StoreLocalRepository}
     */
    private static StoreLocalRepository provideLocalRepository(Context context) {
        return StoreLocalRepository.getInstance(context.getContentResolver(), AppExecutors.getInstance());
    }

    /**
     * Method that provides/injects the {@link StoreFileRepository} instance which
     * deals with the Files.
     *
     * @param context A {@link Context} to derive the {@link android.content.ContentResolver} instance
     * @return Instance of {@link StoreFileRepository}
     */
    private static StoreFileRepository provideFileRepository(Context context) {
        return StoreFileRepository.getInstance(context.getContentResolver(), AppExecutors.getInstance());
    }

    /**
     * Method that provides/injects the {@link StoreRepository} instance which
     * interfaces with {@link StoreLocalRepository} and {@link StoreFileRepository}
     *
     * @param context A {@link Context} to derive the {@link android.content.ContentResolver} instance
     * @return Instance of {@link StoreRepository}
     */
    public static StoreRepository provideStoreRepository(Context context) {
        return StoreRepository.getInstance(provideLocalRepository(context), provideFileRepository(context));
    }

}
