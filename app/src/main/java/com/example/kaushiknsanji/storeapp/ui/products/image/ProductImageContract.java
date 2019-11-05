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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.annotation.StringRes;

import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.ui.BasePresenter;
import com.example.kaushiknsanji.storeapp.ui.BaseView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * Contract Interface for the View {@link ProductImageActivityFragment} and its Presenter {@link ProductImagePresenter}.
 *
 * @author Kaushik N Sanji
 */
public interface ProductImageContract {

    //Request code for Image Capture
    int REQUEST_IMAGE_CAPTURE = 31;
    //Request code for Image Pick
    int REQUEST_IMAGE_PICK = 32;

    //Constants defining the PhotoGrid Modes for selection
    String MODE_SELECT = "PhotoGrid.SELECT_MODE";
    String MODE_DELETE = "PhotoGrid.DELETE_MODE";

    //Defining the Annotation interface for valid PhotoGrid Modes for selection
    //Enumerating the Annotation with valid PhotoGrid Modes for selection
    @StringDef({MODE_SELECT, MODE_DELETE})
    //Retaining Annotation till Compile Time
    @Retention(RetentionPolicy.SOURCE)
    @interface PhotoGridSelectModeDef {
    }

    /**
     * View Interface implemented by {@link ProductImageActivityFragment}
     */
    interface View extends BaseView<Presenter> {

        /**
         * Method invoked on click of the FAB in the Activity
         * to launch the Image Picker Dialog {@link ProductImagePickerDialogFragment}
         */
        void showImagePickerDialog();

        /**
         * Method that displays the Progress indicator
         *
         * @param statusTextId String resource for the status of the Progress to be shown.
         */
        void showProgressIndicator(@StringRes int statusTextId);

        /**
         * Method that hides the Progress indicator
         */
        void hideProgressIndicator();

        /**
         * Method invoked when an error is encountered during Product Image information
         * retrieval or save process.
         *
         * @param messageId String Resource of the error Message to be displayed
         * @param args      Variable number of arguments to replace the format specifiers
         *                  in the String resource if any
         */
        void showError(@StringRes int messageId, @Nullable Object... args);

        /**
         * Method invoked when the Image is already captured through the camera activity intent.
         */
        void onImageCaptured();

        /**
         * Method invoked to update the state of the items in the RecyclerView Grid of Photos
         * based on {@code imageSelectionTrackers}
         *
         * @param imageSelectionTrackers List of Selection Trackers {@link ImageSelectionTracker}
         *                               used for updating the state of the items in the RecyclerView
         *                               Grid of Photos, which are marked for select/delete
         */
        void updateGridItemsState(ArrayList<ImageSelectionTracker> imageSelectionTrackers);

        /**
         * Method that displays a message on success of deleting the Item images selected.
         */
        void showDeleteSuccess();

        /**
         * Method invoked to update the list of {@link ProductImage} objects to the RecyclerView
         * Grid of Photos
         *
         * @param productImages List of {@link ProductImage}, each of which holds the URI information
         *                      of the Image File.
         */
        void submitListToAdapter(ArrayList<ProductImage> productImages);

        /**
         * Method invoked by the Presenter to keep the View's {@code imageSelectionTrackers}
         * in-sync.
         *
         * @param imageSelectionTrackers List of Selection Trackers {@link ImageSelectionTracker}
         *                               used for updating the state of the items in the RecyclerView
         *                               Grid of Photos, which are marked for select/delete
         */
        void syncSelectionTrackers(ArrayList<ImageSelectionTracker> imageSelectionTrackers);

        /**
         * Method invoked by the Presenter to display the RecyclerView Grid of Photos
         */
        void showGridView();

        /**
         * Method invoked by the Presenter to hide the RecyclerView Grid of Photos, in order
         * to display a View to show a message requesting the User to add images.
         */
        void hideGridView();

        /**
         * Method invoked by the Presenter to display the Discard dialog,
         * requesting the User whether to keep/discard the changes
         */
        void showDiscardDialog();

        /**
         * Method that displays a message indicating that the Image being picked
         * was already picked and present in the list.
         */
        void showImageAlreadyPicked();
    }

    /**
     * Presenter Interface implemented by {@link ProductImagePresenter}
     */
    interface Presenter extends BasePresenter {

        /**
         * Method invoked on click of the FAB in the Activity
         * to open the Image Picker Dialog {@link ProductImagePickerDialogFragment}
         */
        void openImagePickerDialog();

        /**
         * Invoked from a previous call to
         * {@link android.support.v4.app.FragmentActivity#startActivityForResult(Intent, int)}.
         *
         * @param requestCode The integer request code originally supplied to
         *                    startActivityForResult(), allowing you to identify who this
         *                    result came from.
         * @param resultCode  The integer result code returned by the child activity
         *                    through its setResult().
         * @param data        An Intent, which can return result data to the caller
         *                    (various data can be attached to Intent "extras").
         */
        void onActivityResult(int requestCode, int resultCode, Intent data);

        /**
         * Method invoked to update the list of {@link ProductImage} objects to the RecyclerView
         * Grid of Photos
         *
         * @param productImages List of {@link ProductImage}, each of which holds the URI information
         *                      of the Image File.
         */
        void submitListToAdapter(ArrayList<ProductImage> productImages);

        /**
         * Method invoked when the Image is captured in a file pointed by the Content URI {@code tempCaptureImageUri}.
         * This method saves the temporary image to a file that generates a Content URI
         * for the new Image file.
         *
         * @param context             Context of the Activity/Fragment.
         * @param tempCaptureImageUri The Content URI of the temporary image file used
         *                            for capturing the image through the camera activity intent.
         */
        void saveImageToFile(Context context, @Nullable Uri tempCaptureImageUri);

        /**
         * Method invoked when the user clicks on an item in the RecyclerView Grid of Photos,
         * to select from.
         *
         * @param itemPosition The adapter position of the Item clicked
         * @param productImage The {@link ProductImage} associated with the Item clicked
         * @param gridMode     The mode of the Action as defined by {@link ProductImageContract.PhotoGridSelectModeDef}
         */
        void onItemImageClicked(int itemPosition, ProductImage productImage, @ProductImageContract.PhotoGridSelectModeDef String gridMode);

        /**
         * Method invoked when the user does a Long click on an item in the RecyclerView Grid of Photos,
         * to delete from. This should also trigger the contextual action mode for delete action.
         *
         * @param itemPosition The adapter position of the Item Long clicked
         * @param productImage The {@link ProductImage} associated with the Item Long clicked
         * @param gridMode     The mode of the Action as defined by {@link ProductImageContract.PhotoGridSelectModeDef}
         */
        void onItemImageLongClicked(int itemPosition, ProductImage productImage, @ProductImageContract.PhotoGridSelectModeDef String gridMode);

        /**
         * Method that calculates and reports the live number of Image Items
         * selected for Delete in DELETE Action Mode.
         */
        void showDeleteCount();

        /**
         * Method invoked when the user clicks on the Delete Contextual Action Menu
         * to delete the selected item photos.
         */
        void deleteSelection();

        /**
         * Method that unselects/resets all the selected Image Items in the adapter and the selection trackers
         * and then clears the trackers to prepare for next user action.
         */
        void clearSelectedItems();

        /**
         * Method invoked when the user exits from the Contextual Delete Action Mode
         * without taking any action on the selected items.
         */
        void onDeleteModeExit();

        /**
         * Method invoked by the View when the activity was restored, to restore the list of
         * Selection Trackers.
         *
         * @param imageSelectionTrackers List of Selection Trackers {@link ImageSelectionTracker}
         *                               used for updating the state of the items in the RecyclerView
         *                               Grid of Photos, which are marked for select/delete
         */
        void restoreSelectionTrackers(ArrayList<ImageSelectionTracker> imageSelectionTrackers);


        /**
         * Method invoked by the View when the activity was started/restored, to restore the list of
         * {@link ProductImage} objects.
         *
         * @param productImages List of {@link ProductImage}, each of which holds the URI information
         *                      of the Image File.
         */
        void restoreProductImages(ArrayList<ProductImage> productImages);

        /**
         * Method invoked by the View when an Item Image was selected by the User to be shown.
         *
         * @param productImage The {@link ProductImage} associated with the Item Image selected,
         *                     which contains the URI information of the Image File.
         */
        void syncLastChosenProductImage(ProductImage productImage);

        /**
         * Method invoked when the user clicks on the Select Menu Action,
         * or on the "Save" button, on the Unsaved changes dialog.
         */
        void onSelectAction();

        /**
         * Method invoked when the user clicks on the "Ignore" button, on the
         * Unsaved changes dialog.
         */
        void onIgnoreAction();

        /**
         * Method invoked when the user clicks on the android home/up button
         * or the back key is pressed
         */
        void onUpOrBackAction();

        /**
         * Method that sets the result and passes the information back to the calling Parent Activity
         */
        void doSetResult();

        /**
         * Method invoked to show the selected Image {@code bitmap} for the Product.
         *
         * @param bitmap   The {@link Bitmap} of the Image to be shown.
         * @param imageUri The String Content URI of the Image to be shown.
         */
        void showSelectedImage(Bitmap bitmap, String imageUri);
    }

}
