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

package com.example.kaushiknsanji.storeapp.ui.products.image;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.example.kaushiknsanji.storeapp.R;

/**
 * {@link DialogFragment} class that inflates the layout 'R.layout.dialog_product_image_picker'
 * to display a chooser dialog for Image Picking via Capture/Gallery.
 *
 * @author Kaushik N Sanji
 */
public class ProductImagePickerDialogFragment extends DialogFragment
        implements View.OnClickListener {

    //Constant used for logs
    private static final String LOG_TAG = ProductImagePickerDialogFragment.class.getSimpleName();

    //Constant used as an Identifier for the Fragment
    private static final String DIALOG_FRAGMENT_TAG = LOG_TAG;

    //Interface to deliver action events
    private ImagePickerOptionListener mImagePickerOptionListener;

    /**
     * Factory method to create an instance of the DialogFragment {@link ProductImagePickerDialogFragment}
     *
     * @return A new instance of the DialogFragment {@link ProductImagePickerDialogFragment}
     */
    public static ProductImagePickerDialogFragment newInstance() {
        //Returning the instance of this Fragment
        return new ProductImagePickerDialogFragment();
    }

    /**
     * Method that displays this Fragment as a Dialog.
     *
     * @param fragmentManager The instance of the {@link FragmentManager} to use
     *                        for managing the Fragments shown
     */
    public static void showDialog(FragmentManager fragmentManager) {
        //Finding the Fragment to see if it is already instantiated and shown
        ProductImagePickerDialogFragment imagePickerDialogFragment
                = (ProductImagePickerDialogFragment) fragmentManager
                .findFragmentByTag(DIALOG_FRAGMENT_TAG);

        if (imagePickerDialogFragment == null) {
            //Instantiating and displaying the fragment when the Fragment is not currently shown
            imagePickerDialogFragment = ProductImagePickerDialogFragment.newInstance();
            imagePickerDialogFragment.show(fragmentManager, DIALOG_FRAGMENT_TAG);
        }
    }

    /**
     * Called when a fragment is first attached to its context.
     * {@link #onCreate(Bundle)} will be called after this.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            //Trying to see if the Context of Fragment implements the required listener
            mImagePickerOptionListener = (ImagePickerOptionListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(getParentFragment() + " must implement ImagePickerOptionListener");
        }
    }

    /**
     * Method invoked by the system to create the Dialog to be shown.
     * The layout 'R.layout.dialog_product_image_picker'
     * will be inflated and returned as Dialog.
     *
     * @param savedInstanceState The last saved instance state of the Fragment,
     *                           or null if this is a freshly created Fragment.
     * @return Return a new Dialog instance to be displayed by the Fragment.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Initializing the AlertDialog Builder to build the Dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireActivity());

        //Inflating the layout for the dialog 'R.layout.dialog_product_image_picker'
        //Passing NULL as we are attaching the layout to the Dialog
        View rootView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_product_image_picker, null);

        //Finding the required views to set click listeners
        rootView.findViewById(R.id.view_picker_camera_overlay).setOnClickListener(this);
        rootView.findViewById(R.id.view_picker_gallery_overlay).setOnClickListener(this);
        rootView.findViewById(R.id.btn_picker_close).setOnClickListener(this);

        //Setting this prepared layout onto the dialog's builder
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
        //Making the background of the dialog transparent
        //since we are using CardView as the root view
        Window window = getDialog().getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param view The view that was clicked.
     */
    @Override
    public void onClick(View view) {
        //Taking action based on the Id of the view clicked
        switch (view.getId()) {
            case R.id.view_picker_camera_overlay:
                //For "Take Photo"

                //Dispatching the event to the listener
                mImagePickerOptionListener.onTakeNewPhoto();
                //Dismissing the dialog
                dismiss();
                break;

            case R.id.view_picker_gallery_overlay:
                //For "Pick from Gallery"

                //Dispatching the event to the listener
                mImagePickerOptionListener.onPickPhotoFromGallery();
                //Dismissing the dialog
                dismiss();
                break;

            case R.id.btn_picker_close:
                //For "Close" button

                //Dismiss this dialog
                dismiss();
                break;
        }
    }

    /**
     * Called when the fragment is no longer attached to its activity.  This
     * is called after {@link #onDestroy()}.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        //Clearing the reference to the listener to avoid leaks
        mImagePickerOptionListener = null;
    }

    /**
     * Activity/Fragment that creates an instance of this {@link ProductImagePickerDialogFragment}
     * needs to implement the interface to receive event callbacks.
     */
    interface ImagePickerOptionListener {
        /**
         * Callback method of {@link ProductImagePickerDialogFragment}
         * invoked when the user chooses the "Take Photo" option in the dialog.
         * This method should launch a Camera Activity that takes a Photo and saves it.
         */
        void onTakeNewPhoto();

        /**
         * Callback method of {@link ProductImagePickerDialogFragment}
         * invoked when the user chooses the "Pick from Gallery" option in the dialog.
         * This method should launch a Gallery like Activity to select the Photos.
         */
        void onPickPhotoFromGallery();
    }
}