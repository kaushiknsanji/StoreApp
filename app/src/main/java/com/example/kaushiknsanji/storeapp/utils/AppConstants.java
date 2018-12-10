package com.example.kaushiknsanji.storeapp.utils;

import com.example.kaushiknsanji.storeapp.BuildConfig;

/**
 * Class that maintains Global App Constants
 *
 * @author Kaushik N Sanji
 */
public class AppConstants {

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
