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

import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.constraint.Group;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.ui.common.ProgressDialogFragment;
import com.example.kaushiknsanji.storeapp.utils.FileStorageUtility;
import com.example.kaushiknsanji.storeapp.utils.ImageStorageUtility;
import com.example.kaushiknsanji.storeapp.utils.OrientationUtility;
import com.example.kaushiknsanji.storeapp.utils.SnackbarUtility;
import com.example.kaushiknsanji.storeapp.workers.ImageDownloaderFragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Content Fragment of {@link ProductImageActivity} that inflates the layout 'R.layout.fragment_product_image'
 * to capture/record/display the Images of the Product. This implements the
 * {@link ProductImageContract.View} on the lines of Model-View-Presenter architecture.
 *
 * @author Kaushik N Sanji
 */
public class ProductImageActivityFragment extends Fragment
        implements ProductImageContract.View, ProductImagePickerDialogFragment.ImagePickerOptionListener {

    //The Bundle argument constant of this Fragment
    public static final String ARGUMENT_LIST_PRODUCT_IMAGES = "argument.PRODUCT_IMAGES";
    //Constant used for logs
    private static final String LOG_TAG = ProductImageActivityFragment.class.getSimpleName();
    //Bundle constants for persisting the data through System config changes
    private static final String BUNDLE_CAPTURE_IMAGE_URI_KEY = "ProductImage.TempCaptureImageUri";
    private static final String BUNDLE_PRODUCT_IMAGES_KEY = "ProductImage.Images";
    private static final String BUNDLE_SELECTION_TRACKERS_KEY = "ProductImage.ImageSelectionTrackers";

    //The Presenter for this View
    private ProductImageContract.Presenter mPresenter;

    //Stores the instance of the View components required
    private RecyclerView mRecyclerViewPhotoGrid;
    private TextView mTextViewEmptyGrid;

    //Stores the Adapter of the RecyclerView used for displaying Photo Grid
    private ProductImagePhotosGridAdapter mPhotosGridAdapter;

    //Stores the URI details of the Product Images
    private ArrayList<ProductImage> mProductImages;

    //Stores the URI of the Temporary file used for storing the image captured through camera
    private Uri mTempCaptureImageUri;

    //Selection Trackers used for updating the state of the items
    //in the RecyclerView Grid of Photos, which are marked for select/delete
    private ArrayList<ImageSelectionTracker> mImageSelectionTrackers;
    /**
     * The {@link AlertDialog} Click Listener for the Unsaved changes dialog
     */
    private DialogInterface.OnClickListener mUnsavedDialogOnClickListener = new DialogInterface.OnClickListener() {
        /**
         * This method will be invoked when a button in the dialog is clicked.
         *
         * @param dialog the dialog that received the click
         * @param which  the button that was clicked (ex.
         *               {@link DialogInterface#BUTTON_POSITIVE}) or the position
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //Taking action based on the button clicked
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //For "Save" button

                    //Dismiss the dialog
                    dialog.dismiss();
                    //Unlock orientation
                    OrientationUtility.unlockScreenOrientation(requireActivity());
                    //Make the Image shown as the default selected image
                    mPresenter.onSelectAction();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //For "Ignore" button

                    //Dismiss the dialog
                    dialog.dismiss();
                    //Unlock orientation
                    OrientationUtility.unlockScreenOrientation(requireActivity());
                    //Delegate to the Presenter to check the integrity of ProductImages
                    //prior to passing the information back to the parent activity
                    mPresenter.onIgnoreAction();
                    break;
            }
        }
    };

    /**
     * Mandatory Empty Constructor of {@link ProductImageActivityFragment}
     * This is required by the {@link android.support.v4.app.FragmentManager} to instantiate
     * the fragment (e.g. upon screen orientation changes).
     */
    public ProductImageActivityFragment() {
    }

    /**
     * Factory method that creates an instance of {@link ProductImageActivityFragment}
     *
     * @param productImages List of {@link ProductImage} that stores the URI details of the Product Images
     * @return Instance of {@link ProductImageActivityFragment}
     */
    public static ProductImageActivityFragment newInstance(ArrayList<ProductImage> productImages) {
        //Saving the arguments passed, in a Bundle: START
        Bundle args = new Bundle(1);
        args.putParcelableArrayList(ARGUMENT_LIST_PRODUCT_IMAGES, productImages);
        //Saving the arguments passed, in a Bundle: END

        //Instantiating the Fragment
        ProductImageActivityFragment fragment = new ProductImageActivityFragment();
        //Passing the Bundle as Arguments to this Fragment
        fragment.setArguments(args);

        //Returning the fragment instance
        return fragment;
    }

    /**
     * Called to do initial creation of a fragment.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Indicating that this fragment has menu options to show
        setHasOptionsMenu(true);
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null (which
     * is the default implementation).  This will be called between
     * {@link #onCreate(Bundle)} and {@link #onActivityCreated(Bundle)}.
     * <p>
     * <p>If you return a View from here, you will later be called in
     * {@link #onDestroyView} when the view is being released.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Returns the View for the fragment's UI ('R.layout.fragment_product_image') prepared.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //Inflate the layout 'R.layout.fragment_product_image' for this fragment
        //Passing false as we are attaching the layout ourselves
        View rootView = inflater.inflate(R.layout.fragment_product_image, container, false);

        //Find the views to initialize
        mRecyclerViewPhotoGrid = rootView.findViewById(R.id.recyclerview_product_image_photo_grid);
        mTextViewEmptyGrid = rootView.findViewById(R.id.text_product_image_empty_grid);

        if (savedInstanceState == null) {
            //Retrieving the ProductImages from the Arguments Bundle
            //when there is no instance state saved
            Bundle arguments = getArguments();
            if (arguments != null) {
                mProductImages = arguments.getParcelableArrayList(ARGUMENT_LIST_PRODUCT_IMAGES);
            }
        } else {
            //On Subsequent launch
            //Retrieving the ProductImages from the Instance state when saved
            //NOTE: This list is delivered to the Adapter when RecyclerView is setup
            mProductImages = savedInstanceState.getParcelableArrayList(BUNDLE_PRODUCT_IMAGES_KEY);
        }

        //Initialize RecyclerView for the Product Images
        setupPhotoGridRecyclerView();

        //Returning the prepared view
        return rootView;
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            //On Subsequent launch

            //Restoring the URI of the Temporary file used for storing the image captured through camera
            mTempCaptureImageUri = savedInstanceState.getParcelable(BUNDLE_CAPTURE_IMAGE_URI_KEY);
            //Restoring the state of Selection Trackers
            mPresenter.restoreSelectionTrackers(savedInstanceState.getParcelableArrayList(BUNDLE_SELECTION_TRACKERS_KEY));
        }
    }

    /**
     * Called to ask the fragment to save its current dynamic state, so it
     * can later be reconstructed in a new instance of its process is
     * restarted.  If a new instance of the fragment later needs to be
     * created, the data you place in the Bundle here will be available
     * in the Bundle given to {@link #onCreate(Bundle)},
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}, and
     * {@link #onActivityCreated(Bundle)}.
     * <p>
     * <p>This corresponds to {@link android.support.v4.app.FragmentActivity#onSaveInstanceState(Bundle)
     * Activity.onSaveInstanceState(Bundle)} and most of the discussion there
     * applies here as well.
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //Saving the URI of the Temporary file used for storing the image captured through camera
        outState.putParcelable(BUNDLE_CAPTURE_IMAGE_URI_KEY, mTempCaptureImageUri);
        //Saving the list of Selection Trackers
        outState.putParcelableArrayList(BUNDLE_SELECTION_TRACKERS_KEY, mImageSelectionTrackers);
        //Saving the list of ProductImage objects
        outState.putParcelableArrayList(BUNDLE_PRODUCT_IMAGES_KEY, mProductImages);
    }

    /**
     * Initialize the contents of the Fragment host's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     *
     * @param menu     The options menu in which you place your items.
     * @param inflater The LayoutInflater object that can be used to inflate the Menu options
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //Inflating the Menu options from 'R.menu.menu_fragment_product_image'
        inflater.inflate(R.menu.menu_fragment_product_image, menu);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handling based on the Menu item selected
        switch (item.getItemId()) {
            case R.id.action_select:
                //On click of Select Menu

                //Propagating the call to the Presenter to do the required action
                mPresenter.onSelectAction();
                return true;
            default:
                //On other cases, do the default menu handling
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Method that initializes the RecyclerView of Product Photos with its Adapter.
     */
    private void setupPhotoGridRecyclerView() {
        //Creating a GridLayoutManager
        GridLayoutManager gridLayoutManager
                = new GridLayoutManager(getContext(),
                getResources().getInteger(R.integer.product_image_grid_span_count),
                GridLayoutManager.VERTICAL,
                false
        );

        //Assigning the GridLayoutManager to RecyclerView
        mRecyclerViewPhotoGrid.setLayoutManager(gridLayoutManager);

        //Creating and assigning the adapter
        mPhotosGridAdapter = new ProductImagePhotosGridAdapter(new UserActionsListener());
        mRecyclerViewPhotoGrid.setAdapter(mPhotosGridAdapter);

        //Restore the Product Images to Presenter and the RecyclerView Adapter
        mPresenter.restoreProductImages(mProductImages);

        //Setting the Item Decoration for Grid Item Spacing
        mRecyclerViewPhotoGrid.addItemDecoration(new GridSpacingItemDecoration(getResources().getDimensionPixelSize(R.dimen.image_item_photo_grid_spacing)));
    }

    /**
     * Method that registers the Presenter {@code presenter} with the View implementing {@link com.example.kaushiknsanji.storeapp.ui.BaseView}
     *
     * @param presenter Presenter instance implementing the {@link com.example.kaushiknsanji.storeapp.ui.BasePresenter}
     */
    @Override
    public void setPresenter(ProductImageContract.Presenter presenter) {
        mPresenter = presenter;
    }

    /**
     * Method invoked on click of the FAB in the Activity
     * to launch the Image Picker Dialog {@link ProductImagePickerDialogFragment}
     */
    @Override
    public void showImagePickerDialog() {
        ProductImagePickerDialogFragment.showDialog(getChildFragmentManager());
    }

    /**
     * Method that displays the Progress indicator
     *
     * @param statusTextId String resource for the status of the Progress to be shown.
     */
    @Override
    public void showProgressIndicator(@StringRes int statusTextId) {
        ProgressDialogFragment.showDialog(getChildFragmentManager(), getString(statusTextId));
    }

    /**
     * Method that hides the Progress indicator
     */
    @Override
    public void hideProgressIndicator() {
        ProgressDialogFragment.dismissDialog(getChildFragmentManager());
    }

    /**
     * Method invoked when an error is encountered during Product Image information
     * retrieval or save process.
     *
     * @param messageId String Resource of the error Message to be displayed
     * @param args      Variable number of arguments to replace the format specifiers
     */
    @Override
    public void showError(@StringRes int messageId, @Nullable Object... args) {
        if (getView() != null) {
            //When we have the root view

            //Evaluating the message to be shown
            String messageToBeShown;
            if (args != null && args.length > 0) {
                //For the String Resource with args
                messageToBeShown = getString(messageId, args);
            } else {
                //For the String Resource without args
                messageToBeShown = getString(messageId);
            }

            if (!TextUtils.isEmpty(messageToBeShown)) {
                //Displaying the Snackbar message of indefinite time length
                //when we have the error message to be shown

                new SnackbarUtility(Snackbar.make(getView(), messageToBeShown, Snackbar.LENGTH_INDEFINITE))
                        .revealCompleteMessage() //Removes the limit on max lines
                        .setDismissAction(R.string.snackbar_action_ok) //For the Dismiss "OK" action
                        .showSnack();
            }
        }
    }

    /**
     * Method invoked when the Image is already captured through the camera activity intent.
     */
    @Override
    public void onImageCaptured() {
        //Save the Temporary image to a file
        mPresenter.saveImageToFile(requireContext(), mTempCaptureImageUri);
    }

    /**
     * Method invoked to update the state of the items in the RecyclerView Grid of Photos
     * based on {@code imageSelectionTrackers}
     *
     * @param imageSelectionTrackers List of Selection Trackers {@link ImageSelectionTracker}
     *                               used for updating the state of the items in the RecyclerView
     *                               Grid of Photos, which are marked for select/delete
     */
    @Override
    public void updateGridItemsState(ArrayList<ImageSelectionTracker> imageSelectionTrackers) {
        //Dispatch to the Adapter to update
        mPhotosGridAdapter.refreshItemsState(imageSelectionTrackers);
    }

    /**
     * Method that displays a message on success of deleting the Item images selected.
     */
    @Override
    public void showDeleteSuccess() {
        if (getView() != null) {
            Snackbar.make(getView(), R.string.product_image_delete_success, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Method invoked to update the list of {@link ProductImage} objects to the RecyclerView
     * Grid of Photos
     *
     * @param productImages List of {@link ProductImage}, each of which holds the URI information
     *                      of the Image File.
     */
    @Override
    public void submitListToAdapter(ArrayList<ProductImage> productImages) {
        //Saving the list
        mProductImages = productImages;
        //Updating to the RecyclerView Adapter
        mPhotosGridAdapter.submitList(productImages);
    }

    /**
     * Method invoked by the Presenter to keep the View's {@code imageSelectionTrackers}
     * in-sync.
     *
     * @param imageSelectionTrackers List of Selection Trackers {@link ImageSelectionTracker}
     *                               used for updating the state of the items in the RecyclerView
     *                               Grid of Photos, which are marked for select/delete
     */
    @Override
    public void syncSelectionTrackers(ArrayList<ImageSelectionTracker> imageSelectionTrackers) {
        //Sync the trackers
        mImageSelectionTrackers = imageSelectionTrackers;
    }

    /**
     * Method invoked by the Presenter to display the RecyclerView Grid of Photos
     */
    @Override
    public void showGridView() {
        //Show the Grid RecyclerView
        mRecyclerViewPhotoGrid.setVisibility(View.VISIBLE);
        //Hide the Empty Grid TextView
        mTextViewEmptyGrid.setVisibility(View.GONE);
    }

    /**
     * Method invoked by the Presenter to hide the RecyclerView Grid of Photos, in order
     * to display a View to show a message requesting the User to add images.
     */
    @Override
    public void hideGridView() {
        //Hide the Grid RecyclerView
        mRecyclerViewPhotoGrid.setVisibility(View.INVISIBLE);
        //Show the Empty Grid TextView
        mTextViewEmptyGrid.setVisibility(View.VISIBLE);
    }

    /**
     * Method invoked by the Presenter to display the Discard dialog,
     * requesting the User whether to keep/discard the changes
     */
    @Override
    public void showDiscardDialog() {
        //Creating an AlertDialog with a message, and listeners for the positive and negative buttons
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        //Set the Message
        builder.setMessage(R.string.product_image_unsaved_changes_dialog_message);
        //Set the Positive Button and its listener
        builder.setPositiveButton(R.string.product_image_unsaved_changes_dialog_positive_text, mUnsavedDialogOnClickListener);
        //Set the Negative Button and its listener
        builder.setNegativeButton(R.string.product_image_unsaved_changes_dialog_negative_text, mUnsavedDialogOnClickListener);
        //Lock the Orientation
        OrientationUtility.lockCurrentScreenOrientation(requireActivity());
        //Create and display the AlertDialog
        builder.create().show();
    }

    /**
     * Method that displays a message indicating that the Image being picked
     * was already picked and present in the list.
     */
    @Override
    public void showImageAlreadyPicked() {
        if (getView() != null) {
            Snackbar.make(getView(), R.string.product_image_already_picked_message, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Callback method of {@link ProductImagePickerDialogFragment}
     * invoked when the user chooses the "Take Photo" option in the dialog.
     * This method should launch a Camera Activity that takes a Photo and saves it.
     */
    @Override
    public void onTakeNewPhoto() {
        //Check if the device has the Camera feature
        if (requireContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            //When the device has Camera, proceed to capture a photo
            dispatchTakePictureIntent();
        } else {
            //When the device has NO Camera, display a message
            showError(R.string.product_image_no_camera_hardware_error);
        }
    }

    /**
     * Callback method of {@link ProductImagePickerDialogFragment}
     * invoked when the user chooses the "Pick from Gallery" option in the dialog.
     * This method should launch a Gallery like Activity to select the Photos.
     */
    @Override
    public void onPickPhotoFromGallery() {
        Intent pickerIntent;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            //For devices with Android version less than Kitkat
            pickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            //For devices with Android version Kitkat and above
            pickerIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            //Add Persistable permission to URI
            //(this exists only from Kitkat)
            pickerIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        }

        //Add read/write permissions to URI
        pickerIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        //Filter results that can be streamed like files (this excludes stuff like timezones and contacts)
        pickerIntent.addCategory(Intent.CATEGORY_OPENABLE);

        //Filter only for images
        pickerIntent.setType("image/*");

        //Allow multiple pick
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            pickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }

        //Launch the Picker Activity with the request code and intent chooser
        startActivityForResult(Intent.createChooser(pickerIntent, getString(R.string.product_image_picker_chooser_title)), ProductImageContract.REQUEST_IMAGE_PICK);
    }

    /**
     * Method invoked to create an Intent for taking a Picture
     */
    private void dispatchTakePictureIntent() {
        //Creating an Image capture intent
        Intent capturePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Ensuring that there is an Activity to handle this intent
        if (capturePictureIntent.resolveActivity(requireContext().getPackageManager()) != null
                && FileStorageUtility.isExternalStorageMounted()) {
            //Create a temp file for capturing the photo
            File tempPhotoFile = null;
            try {
                tempPhotoFile = ImageStorageUtility.createTempImageFile(requireContext());
            } catch (IOException e) {
                Log.e(LOG_TAG, "dispatchTakePictureIntent: Error occurred while creating a temp file ", e);
            }

            //When the Temp file is created
            if (tempPhotoFile != null && tempPhotoFile.exists()) {
                //Get the Content URI for the file (to avoid android.os.FileUriExposedException)
                mTempCaptureImageUri = ImageStorageUtility.getContentUriForImageFile(requireContext(), tempPhotoFile);

                //Add the URI to Intent EXTRA to store the image that will be taken
                capturePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mTempCaptureImageUri);

                //Grant read/write permissions to the URI
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                        && Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    //On devices with Android Jellybean (API Level 16) till Lollipop (API Level 21),
                    //no code is present to migrate the MediaStore.EXTRA_OUTPUT to ClipData,
                    //which implicitly grants read/write permission to the URI in the
                    //hidden method Intent#migrateExtraStreamToClipData(). Hence adding this fix.
                    capturePictureIntent.setClipData(ClipData.newRawUri("", mTempCaptureImageUri));
                    capturePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } else {
                    //For devices with Android ICS (level 15) and lower, we grant the permissions to
                    //all the app packages that can handle this intent.
                    //(This is the only approach since we do not have ClipData
                    //and Intent#migrateExtraStreamToClipData() method in the versions 15 and below)

                    //Retrieving the list of default activities that can handle this intent
                    List<ResolveInfo> resolveInfoList = requireContext().getPackageManager().queryIntentActivities(capturePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resolveInfoList) {
                        //Reading the Package Name of the resolved activity
                        String packageName = resolveInfo.activityInfo.packageName;
                        //Granting read/write permissions to the URI for the package
                        requireContext().grantUriPermission(packageName, mTempCaptureImageUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }
                }

                //Launch the Camera Activity with the request code
                startActivityForResult(capturePictureIntent, ProductImageContract.REQUEST_IMAGE_CAPTURE);
            }
        } else if (!FileStorageUtility.isExternalStorageMounted()) {
            //When the external storage was not accessible, display a message
            showError(R.string.product_image_disk_not_mounted_save_error);
        }
    }

    /**
     * Receive the result from a previous call to
     * {@link #startActivityForResult(Intent, int)}.  This follows the
     * related Activity API as described there in
     * {@link android.support.v4.app.FragmentActivity#onActivityResult(int, int, Intent)}.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Delegating to the Presenter to handle
        mPresenter.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * RecyclerView {@link ListAdapter} to load the list of Product Images {@link ProductImage} to be displayed.
     */
    private static class ProductImagePhotosGridAdapter extends ListAdapter<ProductImage, ProductImagePhotosGridAdapter.ViewHolder> {

        //Payload constant to partial rebind the selection state of the Product Image Item in the Grid
        private static final String PAYLOAD_TRACKER = "Payload.SelectionTracker";
        /**
         * {@link DiffUtil.ItemCallback} for calculating the difference between two
         * {@link ProductImage} objects
         */
        private static DiffUtil.ItemCallback<ProductImage> DIFF_IMAGES
                = new DiffUtil.ItemCallback<ProductImage>() {
            /**
             * Called to check whether two objects represent the same item.
             * <p>
             * For example, if your items have unique ids, this method should check their id equality.
             *
             * @param oldItem The item in the old list.
             * @param newItem The item in the new list.
             * @return True if the two items represent the same object or false if they are different.
             *
             * @see DiffUtil.Callback#areItemsTheSame(int, int)
             */
            @Override
            public boolean areItemsTheSame(ProductImage oldItem, ProductImage newItem) {
                //Returning the comparison of ProductImage URI
                return oldItem.getImageUri().equals(newItem.getImageUri());
            }

            /**
             * Called to check whether two items have the same data.
             * <p>
             * This information is used to detect if the contents of an item have changed.
             * <p>
             * This method to check equality instead of {@link Object#equals(Object)} so that you can
             * change its behavior depending on your UI.
             * <p>
             * For example, if you are using DiffUtil with a
             * {@link android.support.v7.widget.RecyclerView.Adapter RecyclerView.Adapter}, you should
             * return whether the items' visual representations are the same.
             * <p>
             * This method is called only if {@link #areItemsTheSame(ProductImage, ProductImage)} returns {@code true} for
             * these items.
             *
             * @param oldItem The item in the old list.
             * @param newItem The item in the new list.
             * @return True if the contents of the items are the same or false if they are different.
             *
             * @see DiffUtil.Callback#areContentsTheSame(int, int)
             */
            @Override
            public boolean areContentsTheSame(ProductImage oldItem, ProductImage newItem) {
                //Returning the comparison of the Boolean Default in ProductImage
                return oldItem.isDefault() == newItem.isDefault();
            }
        };
        //Listener for the User Actions on the Grid Items
        private PhotoGridUserActionsListener mActionsListener;

        //The Mode of Action (Select/Delete) on the RecyclerView Grid Items
        @ProductImageContract.PhotoGridSelectModeDef
        private String mGridMode;

        //SparseArray of Selection Trackers used for updating the state of the items which are marked for select/delete
        private SparseArray<ImageSelectionTracker> mImageSelectionTrackerMap;

        /**
         * Constructor of {@link ProductImagePhotosGridAdapter}
         *
         * @param userActionsListener Instance of {@link PhotoGridUserActionsListener} to receive
         *                            event callbacks for User actions on Item Views
         */
        ProductImagePhotosGridAdapter(PhotoGridUserActionsListener userActionsListener) {
            super(DIFF_IMAGES);
            //Registering the User Actions Listener
            mActionsListener = userActionsListener;
        }

        /**
         * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
         * an item.
         *
         * @param parent   The ViewGroup into which the new View will be added after it is bound to
         *                 an adapter position.
         * @param viewType The view type of the new View.
         * @return A new ViewHolder that holds a View of the given view type.
         */
        @NonNull
        @Override
        public ProductImagePhotosGridAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Inflating the Item View 'R.layout.item_product_image'
            //Passing false as we are attaching the view ourselves
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_image, parent, false);

            //Returning the ViewHolder for the ItemView
            return new ViewHolder(itemView);
        }

        /**
         * Called by RecyclerView to display the data at the specified position. This method should
         * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
         * position.
         *
         * @param holder   The ViewHolder which should be updated to represent the contents of the
         *                 item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        @Override
        public void onBindViewHolder(@NonNull ProductImagePhotosGridAdapter.ViewHolder holder, int position) {
            //Get the data at the item position
            ProductImage itemProductImage = getItem(position);

            //Get the View Context
            Context context = holder.itemView.getContext();

            //Load the Image for the item
            ImageDownloaderFragment.newInstance(((FragmentActivity) context).getSupportFragmentManager(), position)
                    .setOnSuccessListener(bitmap -> {
                        //Checking if any selections were made to update the item state
                        if (mImageSelectionTrackerMap != null && mImageSelectionTrackerMap.size() > 0 && mGridMode.equals(ProductImageContract.MODE_SELECT)) {
                            //Lookup the tracker for the current item to update the state
                            ImageSelectionTracker currentItemTracker = mImageSelectionTrackerMap.get(position);
                            //Checking if the adapter's mode is SELECT and the current item tracker is selected to proceed
                            if (currentItemTracker != null && currentItemTracker.getPhotoGridMode().equals(mGridMode)
                                    && currentItemTracker.isSelected()) {
                                //When the Mode is SELECT and the current item tracker is selected,
                                //update the downloaded bitmap to ImageView 'R.id.image_product_selected_item_photo'
                                mActionsListener.showSelectedImage(bitmap, itemProductImage);
                            }
                        }
                    })
                    .executeAndUpdate(holder.mImageViewItemPhoto,
                            itemProductImage.getImageUri(),
                            position);


            //Checking if any selections were made to update the item state
            if (mImageSelectionTrackerMap != null && mImageSelectionTrackerMap.size() > 0) {
                //Load the state for the item

                //Lookup the tracker for the current item to update the state
                ImageSelectionTracker currentItemTracker = mImageSelectionTrackerMap.get(position);
                if (currentItemTracker != null) {
                    //When there is selection for the item, update its state accordingly
                    holder.updateItemSelectionState(currentItemTracker.getPhotoGridMode(), currentItemTracker.isSelected());
                }
            }
        }

        /**
         * Called by RecyclerView to display the data at the specified position. This method
         * should update the contents of the {@link ViewHolder#itemView} to reflect the item at
         * the given position.
         * <p>
         * Note that unlike {@link ListView}, RecyclerView will not call this method
         * again if the position of the item changes in the data set unless the item itself is
         * invalidated or the new position cannot be determined. For this reason, you should only
         * use the <code>position</code> parameter while acquiring the related data item inside
         * this method and should not keep a copy of it. If you need the position of an item later
         * on (e.g. in a click listener), use {@link ViewHolder#getAdapterPosition()} which will
         * have the updated adapter position.
         * <p>
         * Partial bind vs full bind:
         * <p>
         * The payloads parameter is a merge list from {@link #notifyItemChanged(int, Object)} or
         * {@link #notifyItemRangeChanged(int, int, Object)}.  If the payloads list is not empty,
         * the ViewHolder is currently bound to old data and Adapter may run an efficient partial
         * update using the payload info.  If the payload is empty,  Adapter must run a full bind.
         * Adapter should not assume that the payload passed in notify methods will be received by
         * onBindViewHolder().  For example when the view is not attached to the screen, the
         * payload in notifyItemChange() will be simply dropped.
         *
         * @param holder   The ViewHolder which should be updated to represent the contents of the
         *                 item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         * @param payloads A non-null list of merged payloads. Can be empty list if requires full
         */
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (payloads.isEmpty()) {
                //When there is no payload, return to super
                super.onBindViewHolder(holder, position, payloads);
            } else {
                //When we have some payloads for partial bind

                //Get the Bundle from the first payload (we are sending only one payload)
                Bundle bundle = (Bundle) payloads.get(0);
                //Iterating over the Bundle Keys to bind accordingly
                for (String keyStr : bundle.keySet()) {
                    switch (keyStr) {
                        case PAYLOAD_TRACKER:
                            //For the selected tracker update

                            //Retrieving the latest tracker update for the item tracked in the payload
                            ImageSelectionTracker currentItemTracker = getLatestSelectionTracker(bundle.getParcelable(keyStr));
                            if (currentItemTracker != null && currentItemTracker.getPosition() == position) {
                                //When the tracker is for the current item position

                                if (mGridMode.equals(ProductImageContract.MODE_SELECT)) {
                                    //When the Grid Mode is SELECT

                                    //Get the data at the item position
                                    ProductImage itemProductImage = getItem(position);
                                    //Get the View Context
                                    Context context = holder.itemView.getContext();
                                    //Get the ImageView of the adapter item
                                    ImageView imageViewItemPhoto = holder.mImageViewItemPhoto;

                                    if (currentItemTracker.getPhotoGridMode().equals(mGridMode)
                                            && currentItemTracker.isSelected()) {
                                        //When the tracker's mode matches with the current mode of the Adapter(SELECT)
                                        //and the tracker item is marked as selected

                                        if (imageViewItemPhoto.getDrawable() instanceof BitmapDrawable) {
                                            //Update the bitmap of the adapter's item to the ImageView 'R.id.image_product_selected_item_photo'
                                            //if the bitmap is already loaded for the adapter item
                                            BitmapDrawable bitmapDrawable = (BitmapDrawable) imageViewItemPhoto.getDrawable();
                                            mActionsListener.showSelectedImage(bitmapDrawable.getBitmap(), itemProductImage);
                                        } else {
                                            //When the ImageView of the adapter item does not have the bitmap yet, load the Image for the item
                                            ImageDownloaderFragment.newInstance(((FragmentActivity) context).getSupportFragmentManager(), position)
                                                    .setOnSuccessListener(bitmap -> {
                                                        //Update the downloaded bitmap to ImageView 'R.id.image_product_selected_item_photo'
                                                        mActionsListener.showSelectedImage(bitmap, itemProductImage);
                                                    })
                                                    .executeAndUpdate(imageViewItemPhoto,
                                                            itemProductImage.getImageUri(),
                                                            position);
                                        }
                                    }
                                }

                                //When there is selection for the item, update its state accordingly
                                holder.updateItemSelectionState(currentItemTracker.getPhotoGridMode(), currentItemTracker.isSelected());
                            }
                            break;

                    }
                }

            }
        }

        /**
         * Method that retrieves the latest {@link ImageSelectionTracker} for the {@code imageSelectionTracker}
         * passed based on the value of {@link ImageSelectionTracker#mPosition}.
         *
         * @param imageSelectionTracker The {@link ImageSelectionTracker} whose latest tracker is required.
         * @return Instance of {@link ImageSelectionTracker} which is the updated {@code imageSelectionTracker}
         * if present in the updated mImageSelectionTrackerMap; else the same {@code imageSelectionTracker}
         * when there is no latest update.
         */
        private ImageSelectionTracker getLatestSelectionTracker(ImageSelectionTracker imageSelectionTracker) {
            if (mImageSelectionTrackerMap != null && mImageSelectionTrackerMap.size() > 0) {
                //When we trackers in the map

                //Lookup for the latest tracker based on the position value
                ImageSelectionTracker latestSelectionTracker = mImageSelectionTrackerMap.get(imageSelectionTracker.getPosition());
                if (latestSelectionTracker != null) {
                    //If present, then return the same
                    return latestSelectionTracker;
                }
            }
            //Return the tracker passed when there is no updated tracker for the same
            return imageSelectionTracker;
        }

        /**
         * Method that triggers an update to refresh the state of the items based on {@code imageSelectionTrackers}
         *
         * @param imageSelectionTrackers List of Selection Trackers {@link ImageSelectionTracker}
         *                               used for updating the state of the items which
         *                               are marked for select/delete
         */
        void refreshItemsState(List<ImageSelectionTracker> imageSelectionTrackers) {
            //Build and Replace the SparseArray of trackers
            buildSparseArrayOfTrackers(imageSelectionTrackers);

            //Number of Items marked for Select and/or Delete, to restore/correct the #mGridMode
            int noOfItemsMarkedForSelect = 0;
            int noOfItemsMarkedForDelete = 0;

            //Read the trackers to correct/restore the mode
            for (ImageSelectionTracker selectionTracker : imageSelectionTrackers) {
                //Increment the count of the items marked for Select and/or Delete
                if (selectionTracker.getPhotoGridMode().equals(ProductImageContract.MODE_SELECT)) {
                    noOfItemsMarkedForSelect++;
                } else if (selectionTracker.getPhotoGridMode().equals(ProductImageContract.MODE_DELETE)) {
                    noOfItemsMarkedForDelete++;
                }
            }

            //Restore the mode based on the count
            if (noOfItemsMarkedForDelete + noOfItemsMarkedForSelect == 0) {
                //When both are zero, clear the mode
                mGridMode = "";
            } else if (noOfItemsMarkedForDelete > noOfItemsMarkedForSelect) {
                //When we have more items marked for DELETE, set the mode to DELETE
                mGridMode = ProductImageContract.MODE_DELETE;
            } else {
                //When we have more items marked for SELECT, set the mode to SELECT
                mGridMode = ProductImageContract.MODE_SELECT;
            }

            //Read the trackers and trigger notify for the item positions stored
            for (ImageSelectionTracker selectionTracker : imageSelectionTrackers) {
                //Notifying item state changes only when the Tracker's Mode matches the current
                if (selectionTracker.getPhotoGridMode().equals(mGridMode)) {
                    //Create the Payload Bundle for item state update
                    Bundle payloadBundle = new Bundle(1);
                    payloadBundle.putParcelable(PAYLOAD_TRACKER, selectionTracker);
                    //Trigger notify for the item position passing in the payload to
                    //partial bind the selection state of the item
                    notifyItemChanged(selectionTracker.getPosition(), payloadBundle);
                }
            }

        }

        /**
         * Method that prepares and replaces the SparseArray of {@link ImageSelectionTracker}
         *
         * @param imageSelectionTrackers List of Selection Trackers {@link ImageSelectionTracker}
         *                               used for updating the state of the items which
         *                               are marked for select/delete
         */
        private void buildSparseArrayOfTrackers(List<ImageSelectionTracker> imageSelectionTrackers) {
            //Initialize the Map for loading the trackers
            if (mImageSelectionTrackerMap != null && mImageSelectionTrackerMap.size() > 0) {
                //Clearing the map before use if it was previously initialized
                mImageSelectionTrackerMap.clear();
            } else {
                //Initializing the map when not yet initialized
                mImageSelectionTrackerMap = new SparseArray<>();
            }

            //Reading the trackers for item position and building the Map
            for (ImageSelectionTracker selectionTracker : imageSelectionTrackers) {
                mImageSelectionTrackerMap.append(selectionTracker.getPosition(), selectionTracker);
            }
        }

        /**
         * ViewHolder class for caching View components of the template item view
         * 'R.layout.item_product_image'
         */
        public class ViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener, View.OnLongClickListener {
            //The ImageView in the Grid to show the Product Photo
            private final ImageView mImageViewItemPhoto;
            //Group which controls the visibility for SELECT border and overlay
            private final Group mGroupItemPhotoSelect;
            //Group which controls the visibility for DELETE border and overlay
            private final Group mGroupItemPhotoDelete;

            /**
             * Constructor of the ViewHolder.
             *
             * @param itemView The inflated item layout View passed
             *                 for caching its View components
             */
            ViewHolder(View itemView) {
                super(itemView);

                //Finding the views to be cached
                mImageViewItemPhoto = itemView.findViewById(R.id.image_item_product_photo);
                mGroupItemPhotoSelect = itemView.findViewById(R.id.group_item_product_photo_select);
                mGroupItemPhotoDelete = itemView.findViewById(R.id.group_item_product_photo_delete);

                //Registering the Click Listener and Long Click Listener on Item Product Photo
                mImageViewItemPhoto.setOnClickListener(this);
                mImageViewItemPhoto.setOnLongClickListener(this);
            }

            /**
             * Called when a view has been clicked.
             *
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                //Get the adapter position
                int itemPosition = getAdapterPosition();
                if (itemPosition > RecyclerView.NO_POSITION) {
                    //Proceed when the item position is valid

                    //Get the item at the position
                    ProductImage productImage = getItem(itemPosition);

                    //Take action based on the view being clicked
                    switch (view.getId()) {
                        case R.id.image_item_product_photo:
                            //For the Product Image Photo view

                            if (TextUtils.isEmpty(mGridMode)) {
                                //When the mode is undefined still, update to SELECT mode
                                mGridMode = ProductImageContract.MODE_SELECT;
                            }

                            //Dispatch the action to the actions listener
                            mActionsListener.onItemClicked(itemPosition,
                                    productImage,
                                    mGridMode);
                            break;
                    }
                }
            }

            /**
             * Called when a view has been clicked and held.
             *
             * @param view The view that was clicked and held.
             * @return true if the callback consumed the long click, false otherwise.
             */
            @Override
            public boolean onLongClick(View view) {
                //Get the adapter position
                int itemPosition = getAdapterPosition();
                if (itemPosition > RecyclerView.NO_POSITION) {
                    //Proceed when the item position is valid

                    //Get the item at the position
                    ProductImage productImage = getItem(itemPosition);

                    //Take action based on the view being clicked
                    switch (view.getId()) {
                        case R.id.image_item_product_photo:
                            //For the Product Image Photo view

                            if (TextUtils.isEmpty(mGridMode)
                                    || !mGridMode.equals(ProductImageContract.MODE_DELETE)) {
                                //When the mode is undefined or not set to DELETE yet,
                                //then update it to DELETE mode
                                mGridMode = ProductImageContract.MODE_DELETE;
                            }

                            //Dispatch the action to the actions listener
                            mActionsListener.onItemLongClicked(itemPosition,
                                    productImage,
                                    mGridMode);
                            //Returning TRUE as we consumed the click
                            return true;
                    }
                }
                //On all else, returning FALSE since we have NOT consumed the action
                return false;
            }

            /**
             * Method that updates the state of the item to reflect the mode of action {@code photoGridMode}
             *
             * @param photoGridMode The Mode of Action (Select/Delete) on the RecyclerView Grid Item,
             *                      which is one of the values of
             *                      {@link com.example.kaushiknsanji.storeapp.ui.products.image.ProductImageContract.PhotoGridSelectModeDef}
             * @param isSelected    Boolean to indicate whether the item was selected/unselected.
             *                      <b>TRUE</b> for selected state. <b>FALSE</b> otherwise
             */
            void updateItemSelectionState(@ProductImageContract.PhotoGridSelectModeDef String photoGridMode, boolean isSelected) {
                //Evaluating based on mode first
                if (photoGridMode.equals(ProductImageContract.MODE_SELECT)) {
                    //Action mode is for SELECT

                    //Hide the delete border and overlay
                    mGroupItemPhotoDelete.setVisibility(View.INVISIBLE);

                    if (isSelected) {
                        //When Item is selected

                        //Show the select border and overlay
                        mGroupItemPhotoSelect.setVisibility(View.VISIBLE);

                    } else {
                        //When Item is unselected

                        //Hide the select border and overlay
                        mGroupItemPhotoSelect.setVisibility(View.INVISIBLE);
                    }


                } else if (photoGridMode.equals(ProductImageContract.MODE_DELETE)) {
                    //Action mode is for DELETE

                    //Hide the select border and overlay
                    mGroupItemPhotoSelect.setVisibility(View.INVISIBLE);

                    if (isSelected) {
                        //When Item is selected

                        //Show the delete border and overlay
                        mGroupItemPhotoDelete.setVisibility(View.VISIBLE);

                    } else {
                        //When Item is unselected

                        //Hide the delete border and overlay
                        mGroupItemPhotoDelete.setVisibility(View.INVISIBLE);
                    }
                }

                //Relayout the Groups to show the change
                mGroupItemPhotoSelect.requestLayout();
                mGroupItemPhotoDelete.requestLayout();

            }

        }
    }

    /**
     * RecyclerView {@link android.support.v7.widget.RecyclerView.ItemDecoration} class
     * to add spacing between the items in the Grid Layout, managed by {@link GridLayoutManager}
     */
    private static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        //Stores the spacing to be applied between the items
        private final int mOffsetSize;

        /**
         * Constructor of {@link GridSpacingItemDecoration}
         *
         * @param offsetSize The spacing in Pixels to be applied between the items
         */
        GridSpacingItemDecoration(int offsetSize) {
            mOffsetSize = offsetSize;
        }

        /**
         * Retrieve any offsets for the given item. Each field of <code>outRect</code> specifies
         * the number of pixels that the item view should be inset by, similar to padding or margin.
         * The default implementation sets the bounds of outRect to 0 and returns.
         *
         * @param outRect Rect to receive the output.
         * @param view    The child view to decorate
         * @param parent  RecyclerView this ItemDecoration is decorating
         * @param state   The current state of RecyclerView.
         */
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            //Get the Grid span from the layout manager
            int spanCount = ((GridLayoutManager) parent.getLayoutManager()).getSpanCount();
            //Get the Child View position in the adapter
            int position = parent.getChildAdapterPosition(view);
            //Calculate the column index of the item position
            int column = position % spanCount;

            //Evaluates to first column when the column index is 0
            boolean isFirstColumn = (column == 0);
            //Evaluates to last column when the column index is (spanCount - 1)
            boolean isLastColumn = (column + 1 == spanCount);
            //Evaluates to first row when the item position is less than the spanCount
            boolean isFirstRow = (position < spanCount);

            if (isFirstColumn) {
                //Set full spacing to left when it is the first column item
                outRect.left = mOffsetSize;
            } else {
                //Set half spacing to left when it is other than the first column item
                outRect.left = mOffsetSize / 2;
            }

            if (isLastColumn) {
                //Set full spacing to right when it is the last column item
                outRect.right = mOffsetSize;
            } else {
                //Set half spacing to right when it is other than the last column item
                outRect.right = mOffsetSize / 2;
            }

            if (isFirstRow) {
                //Set full spacing to top when it is the item of the first row
                outRect.top = mOffsetSize;
            } else {
                //Set 0 spacing to top when it is the item of row other than the first row,
                //since spacing will be taken care by defining the bottom
                outRect.top = 0;
            }

            //Set full spacing to bottom
            outRect.bottom = mOffsetSize;
        }
    }

    /**
     * Listener that implements {@link PhotoGridUserActionsListener} to receive
     * event callbacks for User actions on RecyclerView Grid of Product photos
     */
    private class UserActionsListener implements PhotoGridUserActionsListener {

        /**
         * Callback Method of {@link PhotoGridUserActionsListener} invoked when
         * the user clicks on an item in the RecyclerView that displays a Grid of Photos,
         * to select from.
         *
         * @param itemPosition The adapter position of the Item clicked
         * @param productImage The {@link ProductImage} associated with the Item clicked
         * @param gridMode     The mode of the Action as defined by {@link ProductImageContract.PhotoGridSelectModeDef}
         */
        @Override
        public void onItemClicked(int itemPosition, ProductImage productImage, @ProductImageContract.PhotoGridSelectModeDef String gridMode) {
            //Propagating the call to Presenter
            mPresenter.onItemImageClicked(itemPosition, productImage, gridMode);
        }

        /**
         * Callback Method of {@link PhotoGridUserActionsListener} invoked when
         * the user does a Long click on an item in the RecyclerView that displays a Grid of Photos,
         * to delete from. This should also trigger the contextual action mode for delete action.
         *
         * @param itemPosition The adapter position of the Item Long clicked
         * @param productImage The {@link ProductImage} associated with the Item Long clicked
         * @param gridMode     The mode of the Action as defined by {@link ProductImageContract.PhotoGridSelectModeDef}
         */
        @Override
        public void onItemLongClicked(int itemPosition, ProductImage productImage, @ProductImageContract.PhotoGridSelectModeDef String gridMode) {
            //Propagating the call to Presenter
            mPresenter.onItemImageLongClicked(itemPosition, productImage, gridMode);
        }

        /**
         * Callback Method of {@link PhotoGridUserActionsListener} invoked when
         * the selected item's image is to be shown in the Main ImageView 'R.id.image_product_selected_item_photo'
         *
         * @param bitmap       The {@link Bitmap} of the Image to be shown.
         * @param productImage The {@link ProductImage} associated with the Item Image to be shown.
         */
        @Override
        public void showSelectedImage(Bitmap bitmap, ProductImage productImage) {
            //Delegating to the Presenter to show the selected Image
            mPresenter.showSelectedImage(bitmap, productImage.getImageUri());
            //Sync up the selected ProductImage with the Presenter
            mPresenter.syncLastChosenProductImage(productImage);
        }
    }
}
