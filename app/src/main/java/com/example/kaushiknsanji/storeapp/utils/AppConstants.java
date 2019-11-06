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

import com.example.kaushiknsanji.storeapp.BuildConfig;

/**
 * Class that maintains Global App Constants
 *
 * @author Kaushik N Sanji
 */
public final class AppConstants {

    /**
     * Private Constructor to avoid direct instantiation of {@link AppConstants}
     */
    private AppConstants() {
        //Suppressing with an error to enforce noninstantiability
        throw new AssertionError("No " + this.getClass().getCanonicalName() + " instances for you!");
    }

    //Constant for Application ID
    public static final String APPLICATION_ID = BuildConfig.APPLICATION_ID;

    //Constant for Logging Cursor Queries
    public static final boolean LOG_CURSOR_QUERIES = BuildConfig.LOG_CURSOR_QUERIES;

    //Constant for Logging Stetho
    public static final boolean LOG_STETHO = BuildConfig.LOG_STETHO;

    //Constant used for the CursorLoader to load the list of Products from the database
    public static final int PRODUCTS_LOADER = 1;

    //Constant used for the CursorLoader to load the list of Suppliers from the database
    public static final int SUPPLIERS_LOADER = 2;

    //Constant used for the CursorLoader to load the list of Products for Selling from the database
    public static final int SALES_LOADER = 3;
}
