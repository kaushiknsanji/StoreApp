package com.example.kaushiknsanji.storeapp.data.local.contracts;

import android.net.Uri;

/**
 * Marker interface for the constants that identify the Content Provider
 *
 * @author Kaushik N Sanji
 */
public interface StoreContract {

    //The Authority constant of the content provider
    String CONTENT_AUTHORITY = "com.example.kaushiknsanji.storeapp.provider";

    //The Base URI constant to contact the content provider
    Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
}
