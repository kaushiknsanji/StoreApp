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

import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

/**
 * Utility class that acts as a wrapper for {@link Snackbar} extending its ease of usability.
 *
 * @author Kaushik N Sanji
 */
public final class SnackbarUtility {

    //Instance of the Snackbar injected
    private Snackbar mSnackbar;

    /**
     * Constructor of {@link SnackbarUtility}
     *
     * @param snackbar Instance of {@link Snackbar}
     */
    public SnackbarUtility(Snackbar snackbar) {
        mSnackbar = snackbar;
    }

    /**
     * Method that removes the MaxLines limit on Snackbar's TextView.
     * Out of the box, it is restricted to two text lines.
     *
     * @return Instance of {@link SnackbarUtility} to enable method chaining.
     */
    public SnackbarUtility revealCompleteMessage() {
        //Retrieving the View of the Snackbar
        View snackbarView = mSnackbar.getView();
        //Finding the TextView of the Snackbar
        TextView snackbarTextView = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        //Overriding MaxLines limit to Integer's Max value
        snackbarTextView.setMaxLines(Integer.MAX_VALUE);
        //Returning the instance of this for method chaining
        return this;
    }

    /**
     * Method that sets the Dismiss action on {@link Snackbar} with the action label as given by
     * {@code dismissActionLabelResId}. Since this is for Dismiss action, the duration of {@link Snackbar}
     * is set to {@link Snackbar#LENGTH_INDEFINITE}.
     *
     * @param dismissActionLabelResId The String resource of the Action Label for Dismiss action
     * @return Instance of {@link SnackbarUtility} to enable method chaining.
     */
    public SnackbarUtility setDismissAction(@StringRes int dismissActionLabelResId) {
        //Setting the duration to indefinite
        if (mSnackbar.getDuration() != Snackbar.LENGTH_INDEFINITE) {
            mSnackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
        }
        //Setting the dismiss action and returning the instance of this for method chaining
        return setAction(dismissActionLabelResId, (view) -> mSnackbar.dismiss());
    }

    /**
     * Method that sets the Dismiss action on {@link Snackbar} with the action label as given
     * by {@code dismissActionLabel}. Since this is for Dismiss action, the duration of {@link Snackbar}
     * is set to {@link Snackbar#LENGTH_INDEFINITE}.
     *
     * @param dismissActionLabel String containing the value of the Action Label for Dismiss action
     * @return Instance of {@link SnackbarUtility} to enable method chaining.
     */
    public SnackbarUtility setDismissAction(String dismissActionLabel) {
        //Setting the duration to indefinite
        if (mSnackbar.getDuration() != Snackbar.LENGTH_INDEFINITE) {
            mSnackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
        }
        //Setting the dismiss action and returning the instance of this for method chaining
        return setAction(dismissActionLabel, (view) -> mSnackbar.dismiss());
    }

    /**
     * Method that sets the Action with its label {@code actionLabelResId} and listener {@code listener}.
     *
     * @param actionLabelResId The String resource of the Action Label.
     * @param listener         The Listener for the Action.
     * @return Instance of {@link SnackbarUtility} to enable method chaining.
     */
    public SnackbarUtility setAction(@StringRes int actionLabelResId, final View.OnClickListener listener) {
        //Setting the Action Label and its Listener
        mSnackbar.setAction(actionLabelResId, listener);
        //Returning the instance of this for method chaining
        return this;
    }

    /**
     * Method that sets the Action with its label {@code actionLabel} and listener {@code listener}.
     *
     * @param actionLabel The String containing the value of the Action Label.
     * @param listener    The Listener for the Action.
     * @return Instance of {@link SnackbarUtility} to enable method chaining.
     */
    public SnackbarUtility setAction(String actionLabel, final View.OnClickListener listener) {
        //Setting the Action Label and its Listener
        mSnackbar.setAction(actionLabel, listener);
        //Returning the instance of this for method chaining
        return this;
    }

    /**
     * Terminal method of {@link SnackbarUtility} that shows the {@link Snackbar} prepared.
     */
    public void showSnack() {
        mSnackbar.show();
    }
}
