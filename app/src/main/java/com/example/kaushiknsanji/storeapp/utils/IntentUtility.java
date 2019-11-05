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
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

/**
 * Utility class that deals with the Common Intents used in the App.
 *
 * @author Kaushik N Sanji
 */
public class IntentUtility {

    //URI Scheme Constants
    private static final String URI_STR_TELEPHONE_SCHEME = "tel:";
    private static final String URI_STR_EMAIL_SCHEME = "mailto:";

    /**
     * Private constructor to avoid instantiating {@link IntentUtility}
     */
    private IntentUtility() {
        //Suppressing with an error to enforce noninstantiability
        throw new AssertionError("No " + this.getClass().getCanonicalName() + " instances for you!");
    }

    /**
     * Method that creates an Intent to the Phone Dialer to initiate a Phone Call.
     *
     * @param activity    The {@link FragmentActivity} instance initiating this.
     * @param phoneNumber The Phone Number to dial
     */
    public static void dialPhoneNumber(FragmentActivity activity, String phoneNumber) {
        //Creating a Phone Dialer Intent
        Intent intent = new Intent(Intent.ACTION_DIAL);
        //Setting the Phone number Uri
        intent.setData(Uri.parse(URI_STR_TELEPHONE_SCHEME + phoneNumber));
        //Checking for an Activity that can handle this Intent
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            //Starting the activity that handles the given Intent
            activity.startActivity(intent);
        }
    }

    /**
     * Method that creates an Email Intent with no attachments to compose an Email.
     *
     * @param activity    The {@link FragmentActivity} instance initiating this.
     * @param toAddresses String array of "TO" Addresses
     * @param ccAddresses String array of "CC" Addresses
     * @param subject     String containing the Subject of the Email
     * @param body        String containing the Body of the Email
     */
    public static void composeEmail(FragmentActivity activity, String[] toAddresses,
                                    @Nullable String[] ccAddresses, @Nullable String subject,
                                    @Nullable String body) {
        //Creating an Email Intent with no attachments
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        //Only email apps should handle this
        intent.setData(Uri.parse(URI_STR_EMAIL_SCHEME));
        //Passing To Addresses
        intent.putExtra(Intent.EXTRA_EMAIL, toAddresses);
        //Passing CC Addresses
        intent.putExtra(Intent.EXTRA_CC, ccAddresses);
        //Passing Subject
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        //Checking if the Body of the Email is passed
        if (!TextUtils.isEmpty(body)) {
            //Passing Email content with Html Formatted text
            intent.putExtra(Intent.EXTRA_TEXT, TextAppearanceUtility.getHtmlFormattedText(body));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                intent.putExtra(Intent.EXTRA_HTML_TEXT, TextAppearanceUtility.getHtmlFormattedText(body));
            }
        }

        //Checking for an Activity that can handle this Intent
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            //Starting the activity that handles the given Intent
            activity.startActivity(intent);
        }
    }

    /**
     * Method that opens a webpage for the URL passed
     *
     * @param context is the Context of the Calling Activity/Fragment
     * @param webUrl  is the String containing the URL of the Web Page to be launched
     */
    public static void openLink(Context context, String webUrl) {
        //Parsing the URL
        Uri webPageUri = Uri.parse(webUrl);
        //Creating an ACTION_VIEW Intent with the URI
        Intent webIntent = new Intent(Intent.ACTION_VIEW, webPageUri);
        //Checking if there is an Activity that accepts the Intent
        if (webIntent.resolveActivity(context.getPackageManager()) != null) {
            //Launching the corresponding Activity and passing it the Intent
            context.startActivity(webIntent);
        }
    }
}
