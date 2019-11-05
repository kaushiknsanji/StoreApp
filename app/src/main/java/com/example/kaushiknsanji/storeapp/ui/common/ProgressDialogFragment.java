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

package com.example.kaushiknsanji.storeapp.ui.common;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.utils.OrientationUtility;

/**
 * {@link DialogFragment} class that inflates the layout 'R.layout.dialog_progress_circle'
 * to display the Indefinite Progress Dialog with a provided text for Progress Status.
 * <p>
 * User Interactivity will be blocked when this Dialog is displayed.
 * </p>
 *
 * @author Kaushik N Sanji
 */
public class ProgressDialogFragment extends DialogFragment {
    //Constant used for Dialog Fragment identifier
    private static final String DIALOG_FRAGMENT_TAG = ProgressDialogFragment.class.getSimpleName();

    //Bundle Argument Constant used to save the Progress status text
    private static final String ARGUMENT_PROGRESS_STATUS = "argument.ProgressStatus";
    //Default value for the Progress status text
    private static final String PROGRESS_STATUS_EMPTY_DEFAULT = "";

    /**
     * Static Factory Constructor of {@link ProgressDialogFragment}
     *
     * @param statusText String representing the status of the Progress
     * @return Instance of {@link ProgressDialogFragment}
     */
    public static ProgressDialogFragment newInstance(String statusText) {
        //Creating a new Instance of the DialogFragment
        ProgressDialogFragment progressDialogFragment = new ProgressDialogFragment();
        //Creating a Bundle to save the arguments passed
        Bundle args = new Bundle(1);
        args.putString(ARGUMENT_PROGRESS_STATUS, statusText);
        //Passing the arguments onto the Fragment instance
        progressDialogFragment.setArguments(args);
        //Returning the DialogFragment Instance
        return progressDialogFragment;
    }

    /**
     * Static Method that instantiates and shows a dialog of this {@link ProgressDialogFragment}.
     * This method also locks the screen orientation after the dialog is shown.
     *
     * @param fragmentManager The {@link FragmentManager} instance to manage the Fragment instances
     * @param statusText      String representing the status of the Progress
     */
    public static void showDialog(FragmentManager fragmentManager, String statusText) {
        //Looking for an existing instance of ProgressDialogFragment
        ProgressDialogFragment progressDialogFragment
                = (ProgressDialogFragment) fragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG);
        if (progressDialogFragment == null) {
            //If the Fragment is not yet initialized and shown

            //Create a New Instance of the DialogFragment
            progressDialogFragment = ProgressDialogFragment.newInstance(statusText);
            //Display the dialog immediately
            progressDialogFragment.showNow(fragmentManager, DIALOG_FRAGMENT_TAG);
        }
        //Locking the orientation after the dialog is shown
        OrientationUtility.lockCurrentScreenOrientation(progressDialogFragment.requireActivity());
    }

    /**
     * Static Method that dismisses the dialog shown for {@link ProgressDialogFragment} instance.
     * This method also releases the screen orientation lock that was applied when the dialog was shown.
     *
     * @param fragmentManager The {@link FragmentManager} instance to manage the Fragment instances
     */
    public static void dismissDialog(FragmentManager fragmentManager) {
        //Looking for an existing instance of ProgressDialogFragment
        ProgressDialogFragment progressDialogFragment
                = (ProgressDialogFragment) fragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG);
        if (progressDialogFragment != null && progressDialogFragment.getActivity() != null) {
            //Unlocking the orientation before being dismissed
            OrientationUtility.unlockScreenOrientation(progressDialogFragment.requireActivity());
            //Dismissing the dialog if initialized and shown
            progressDialogFragment.dismiss();
        }
    }

    /**
     * Method invoked by the system to create the Dialog to be shown.
     * The layout 'R.layout.dialog_progress_circle' for Progress
     * will be inflated and returned as Dialog.
     *
     * @param savedInstanceState The last saved instance state of the Fragment,
     *                           or null if this is a freshly created Fragment.
     * @return Return a new Dialog instance to be displayed by the Fragment.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Building the Dialog using the AlertDialog.Builder
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireActivity());

        //Inflating the Network Error Dialog Layout 'R.layout.dialog_progress_circle'
        //(Passing null as we are attaching the layout ourselves to a Dialog)
        View rootView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_progress_circle, null);

        //Looking up the Arguments if any
        Bundle arguments = getArguments();
        //Get the Progress status text from the Bundle arguments
        String progressStatus = PROGRESS_STATUS_EMPTY_DEFAULT;
        if (arguments != null) {
            progressStatus = arguments.getString(ARGUMENT_PROGRESS_STATUS, PROGRESS_STATUS_EMPTY_DEFAULT);
        }
        //Find the TextView for the Progress Text
        TextView textViewProgressStatus = rootView.findViewById(R.id.text_progress_status);
        if (progressStatus.equals(PROGRESS_STATUS_EMPTY_DEFAULT)) {
            //When the Progress Status Text is absent, hide the TextView
            textViewProgressStatus.setVisibility(View.GONE);
        } else {
            //When the Progress Status Text is present, show the TextView and set the Progress Text
            textViewProgressStatus.setText(progressStatus);
        }

        //Setting the View on the DialogBuilder
        dialogBuilder.setView(rootView);

        //Returning the Dialog instance built
        return dialogBuilder.create();
    }

    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * {@link #setRetainInstance(boolean)} to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after {@link #onCreateView}
     * and before {@link #onViewStateRestored(Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Get the current Dialog
        Dialog currentDialog = getDialog();
        //Making the background of the dialog transparent
        //since we are using CardView as the root view
        Window window = currentDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        //Preventing cancel on back key
        setCancelable(false);
    }

}
