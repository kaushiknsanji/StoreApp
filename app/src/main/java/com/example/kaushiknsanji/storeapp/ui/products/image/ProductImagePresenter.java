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
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.FileRepository;
import com.example.kaushiknsanji.storeapp.data.StoreRepository;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * The Presenter class that implements {@link ProductImageContract.Presenter} on the lines of
 * Model-View-Presenter architecture. This Presenter interfaces with the App repository {@link StoreRepository}
 * to record/manage the Image Files' Content URIs of the Product in the database, and updates the images
 * to the View {@link ProductImageActivityFragment} to load and display it.
 *
 * @author Kaushik N Sanji
 */
public class ProductImagePresenter implements ProductImageContract.Presenter {

    //Constant used for Logs
    private static final String LOG_TAG = ProductImagePresenter.class.getSimpleName();

    //The View Interface of this Presenter
    @NonNull
    private final ProductImageContract.View mProductImageView;

    //Instance of the App Repository
    @NonNull
    private final StoreRepository mStoreRepository;

    //Listener that receives callbacks on Grid Mode when it changes to Delete action
    private final PhotoGridDeleteModeListener mPhotoGridDeleteModeListener;

    //Navigator that receives callbacks when navigating away from the Current Activity
    private final ProductImageNavigator mProductImageNavigator;

    //Listener that receives callbacks for User selection actions on RecyclerView Grid of Product photos
    private final SelectedPhotoActionsListener mSelectedPhotoActionsListener;

    //Selection Trackers used for updating the state of the items
    //in the RecyclerView Grid of Photos, which are marked for select/delete
    private ArrayList<ImageSelectionTracker> mImageSelectionTrackers;

    //Stores the URI details of the Product Images
    private ArrayList<ProductImage> mProductImages;

    //Stores a list of ProductImage URIs that helps in avoiding duplicates
    //(Should be maintained to have a unique list of URIs)
    private ArrayList<String> mProductImageUris;

    //Stores the ProductImage of the Image that was last chosen by the User to be shown
    //while selecting from the list of Images available
    private ProductImage mLastChosenProductImage;

    //The Mode of Action (Select/Delete) of the RecyclerView Grid Items
    @ProductImageContract.PhotoGridSelectModeDef
    private String mGridMode;

    /**
     * Constructor of {@link ProductImagePresenter}
     *
     * @param storeRepository              Instance of {@link StoreRepository} for accessing/manipulating the data
     * @param productImageView             The View Instance {@link ProductImageContract.View} of this Presenter
     * @param photoGridDeleteModeListener  Instance of {@link PhotoGridDeleteModeListener} that
     *                                     receives callbacks on Grid Mode when it changes to Delete action
     * @param productImageNavigator        Instance of {@link ProductImageNavigator} that receives callbacks
     *                                     when navigating away from the Current Activity
     * @param selectedPhotoActionsListener Instance of {@link SelectedPhotoActionsListener} that
     *                                     receives callbacks for User selection actions on
     *                                     RecyclerView Grid of Product photos
     */
    ProductImagePresenter(@NonNull StoreRepository storeRepository,
                          @NonNull ProductImageContract.View productImageView,
                          @NonNull PhotoGridDeleteModeListener photoGridDeleteModeListener,
                          @NonNull ProductImageNavigator productImageNavigator,
                          @NonNull SelectedPhotoActionsListener selectedPhotoActionsListener) {
        mStoreRepository = storeRepository;
        mProductImageView = productImageView;
        mPhotoGridDeleteModeListener = photoGridDeleteModeListener;
        mProductImageNavigator = productImageNavigator;
        mSelectedPhotoActionsListener = selectedPhotoActionsListener;

        //Initializing the Trackers
        mImageSelectionTrackers = new ArrayList<>();

        //Registering the View with the Presenter
        mProductImageView.setPresenter(this);
    }

    /**
     * Method that initiates the work of a Presenter which is invoked by the View
     * that implements the {@link com.example.kaushiknsanji.storeapp.ui.BaseView}
     */
    @Override
    public void start() {
        //no-op
    }

    /**
     * Method that generates a deep copy of the List of {@link ProductImage} passed.
     *
     * @param productImages The source list of {@link ProductImage} that needs to be deep copied.
     * @return New List of {@link ProductImage} which is a copy of {@code productImages} passed.
     */
    private ArrayList<ProductImage> deepCopyProductImages(ArrayList<ProductImage> productImages) {
        //Creating a new list for the copy
        ArrayList<ProductImage> newProductImages = new ArrayList<>();
        //Iterating over the source to build the copy
        for (ProductImage productImage : productImages) {
            //Adding the clone of ProductImage read, to the new list
            newProductImages.add((ProductImage) productImage.clone());
        }
        //Returning the deep copy list of ProductImage
        return newProductImages;
    }

    /**
     * Method that generates a deep copy of the List of {@link ImageSelectionTracker} passed.
     *
     * @param imageSelectionTrackers The source list of {@link ImageSelectionTracker} that needs to be deep copied.
     * @return New List of {@link ImageSelectionTracker} which is a copy of {@code imageSelectionTrackers} passed.
     */
    private ArrayList<ImageSelectionTracker> deepCopyImageSelectionTrackers(ArrayList<ImageSelectionTracker> imageSelectionTrackers) {
        //Creating a new list for the copy
        ArrayList<ImageSelectionTracker> newImageSelectionTrackers = new ArrayList<>();
        //Iterating over the source to build the copy
        for (ImageSelectionTracker selectionTracker : imageSelectionTrackers) {
            //Adding the clone of ImageSelectionTracker read, to the new list
            newImageSelectionTrackers.add((ImageSelectionTracker) selectionTracker.clone());
        }
        //Returning the deep copy list of ImageSelectionTracker
        return newImageSelectionTrackers;
    }

    /**
     * Method invoked on click of the FAB in the Activity
     * to open the Image Picker Dialog {@link ProductImagePickerDialogFragment}
     */
    @Override
    public void openImagePickerDialog() {
        mProductImageView.showImagePickerDialog();
    }

    /**
     * Invoked from a previous call to
     * {@link FragmentActivity#startActivityForResult(Intent, int)}.
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
        if (resultCode == FragmentActivity.RESULT_OK) {
            //When we have a success result from the Activity

            if (requestCode == ProductImageContract.REQUEST_IMAGE_PICK) {
                //Request is for Image Pick (which can be single or multiple)

                if (data.getData() != null) {
                    //For a single Image Pick

                    //Show the progress indicator
                    mProductImageView.showProgressIndicator(R.string.product_image_status_loading_picked_single);

                    //Get the URI for the Image Picked
                    Uri fileContentUri = data.getData();

                    //Take Persistable permissions on the URI
                    mStoreRepository.takePersistablePermissions(fileContentUri, data.getFlags());

                    //Get the String URI of the URI
                    String fileContentUriStr = fileContentUri.toString();

                    if (!mProductImageUris.contains(fileContentUriStr)) {
                        //If the Image file was not part of the list previously,
                        //then add to the list and update the view

                        //Add the URI to the list
                        mProductImageUris.add(fileContentUriStr);

                        //Prepare the new ProductImage
                        ProductImage newProductImage
                                = new ProductImage.Builder()
                                .setImageUri(fileContentUriStr)
                                .createProductImage();

                        //Update the List of ProductImages
                        mProductImages.add(newProductImage);

                        //Ensure that we are displaying the Images Grid
                        mProductImageView.showGridView();

                        //Submit the new list to the RecyclerView's Adapter
                        submitListToAdapter(mProductImages);

                        //Add the current Image Item to be shown as selected
                        mGridMode = ProductImageContract.MODE_SELECT;
                        showProductImageAsSelected(mProductImages.size() - 1, newProductImage, mGridMode);

                    } else {
                        //When the same Image was picked again

                        //Show the message indicating that the image was already picked
                        mProductImageView.showImageAlreadyPicked();
                    }

                    //Hide the Progress indicator
                    mProductImageView.hideProgressIndicator();

                } else {
                    //For Multiple Image Pick, applicable for API Level 18+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        if (data.getClipData() != null) {
                            //Get the Clip data and its count
                            ClipData clipData = data.getClipData();
                            int noOfClips = clipData.getItemCount();

                            if (noOfClips > 0) {
                                //Show the progress indicator
                                mProductImageView.showProgressIndicator(R.string.product_image_status_loading_picked_multiple);

                                //Iterating over the clip data to extract the URI of the Images picked
                                for (int index = 0; index < noOfClips; index++) {
                                    //Get the Item at index
                                    ClipData.Item clipItem = clipData.getItemAt(index);
                                    //Get the URI
                                    Uri fileContentUri = clipItem.getUri();
                                    //Take Persistable permissions on the URI
                                    mStoreRepository.takePersistablePermissions(fileContentUri, data.getFlags());

                                    //Get the String URI of the URI
                                    String fileContentUriStr = fileContentUri.toString();

                                    if (!mProductImageUris.contains(fileContentUriStr)) {
                                        //If the Image file was not part of the list previously,
                                        //then add to the lists

                                        //Add the URI to the list
                                        mProductImageUris.add(fileContentUriStr);

                                        //Prepare the ProductImage for the URI retrieved
                                        ProductImage newProductImage = new ProductImage.Builder()
                                                .setImageUri(fileContentUri.toString())
                                                .createProductImage();

                                        //Update the List of ProductImages
                                        mProductImages.add(newProductImage);
                                    } else {
                                        //When the same Image was picked again

                                        //Show the message indicating that the image was already picked
                                        mProductImageView.showImageAlreadyPicked();
                                    }

                                }

                                //Submit the new list to the RecyclerView's Adapter
                                submitListToAdapter(mProductImages);

                                if (mProductImages.size() > 0) {
                                    //Ensure that we are displaying the Images Grid
                                    mProductImageView.showGridView();

                                    //Add the last Image Item to be shown as selected
                                    mGridMode = ProductImageContract.MODE_SELECT;
                                    int lastItemPosition = mProductImages.size() - 1;
                                    showProductImageAsSelected(lastItemPosition, mProductImages.get(lastItemPosition), mGridMode);
                                }

                                //Hide the Progress indicator
                                mProductImageView.hideProgressIndicator();
                            }

                        }
                    }
                }

            } else if (requestCode == ProductImageContract.REQUEST_IMAGE_CAPTURE) {
                //Request is for Image capture

                //Passing the call to the View when the image is captured successfully
                mProductImageView.onImageCaptured();
            }
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
        mProductImageView.submitListToAdapter(deepCopyProductImages(productImages));
    }

    /**
     * Method invoked when the Image is captured in a file pointed by the Content URI {@code tempCaptureImageUri}.
     * This method saves the temporary image to a file that generates a Content URI
     * for the new Image file.
     *
     * @param context             Context of the Activity/Fragment.
     * @param tempCaptureImageUri The Content URI of the temporary image file used
     *                            for capturing the image through the camera activity intent.
     */
    @Override
    public void saveImageToFile(Context context, @Nullable Uri tempCaptureImageUri) {
        //Return when the URI of the temporary image file is not available
        if (tempCaptureImageUri == null) {
            return;
        }

        //Show the progress indicator
        mProductImageView.showProgressIndicator(R.string.product_image_status_saving_capture);

        //Execute save operation
        mStoreRepository.saveImageToFile(context, tempCaptureImageUri, new FileRepository.FileOperationsCallback<Uri>() {
            /**
             * Method invoked when the file operation was executed successfully.
             *
             * @param results The results of the operation in the generic type passed.
             */
            @Override
            public void onSuccess(Uri results) {
                //Get the String URI of the URI
                String fileContentUriStr = results.toString();

                //Add the URI to the list
                mProductImageUris.add(fileContentUriStr);

                //Prepare the ProductImage for the new URI
                ProductImage newProductImage = new ProductImage.Builder()
                        .setImageUri(fileContentUriStr)
                        .createProductImage();

                //Update the List of ProductImages
                mProductImages.add(newProductImage);

                //Ensure that we are displaying the Images Grid
                mProductImageView.showGridView();

                //Submit the new list to the RecyclerView's Adapter
                submitListToAdapter(mProductImages);

                //Update the main ImageView to show the new Photo as the selected Photo
                mGridMode = ProductImageContract.MODE_SELECT;
                showProductImageAsSelected(mProductImages.size() - 1, newProductImage, mGridMode);

                //Hide the Progress indicator
                mProductImageView.hideProgressIndicator();

                //NOTE: No need to delete the temporary file pointed by tempCaptureImageUri
                //as the file is deleted when the image is saved. This is taken care by
                //com.example.kaushiknsanji.storeapp.utils.ImageStorageUtility#saveImage
            }

            /**
             * Method invoked when the file operation failed to complete.
             *
             * @param messageId The String resource of the error message
             *                  for the file operation failure
             * @param args      Variable number of arguments to replace the format specifiers
             */
            @Override
            public void onFailure(int messageId, @Nullable Object... args) {
                //Hide the Progress Indicator
                mProductImageView.hideProgressIndicator();

                //Pass to the View to show the error message
                mProductImageView.showError(messageId, args);
            }
        });
    }

    /**
     * Method invoked when the user clicks on an item in the RecyclerView Grid of Photos,
     * to select from.
     *
     * @param itemPosition The adapter position of the Item clicked
     * @param productImage The {@link ProductImage} associated with the Item clicked
     * @param gridMode     The mode of the Action as defined by {@link ProductImageContract.PhotoGridSelectModeDef}
     */
    @Override
    public void onItemImageClicked(int itemPosition, ProductImage productImage,
                                   @ProductImageContract.PhotoGridSelectModeDef String gridMode) {
        //Save the Grid mode passed
        mGridMode = gridMode;

        //Saving the current list of Selection Trackers if we
        //needed to restore in case of the same item image being selected in SELECT mode
        ArrayList<ImageSelectionTracker> tempImageSelectionTrackers = deepCopyImageSelectionTrackers(mImageSelectionTrackers);

        //Check and update if the same Image item was clicked
        //This unselects the Image item when it was selected previously
        //(applicable for both the Grid modes)
        boolean isSameItemClicked = onSameItemImageClicked(productImage);

        if (mGridMode.equals(ProductImageContract.MODE_SELECT)) {
            //When the Grid Mode Action is Select

            if (!isSameItemClicked) {
                //When the Item Image clicked is a new Item

                //Mark the old trackers as unselected, and delete the previously unselected trackers
                //(SELECT mode always should select only one item)
                Iterator<ImageSelectionTracker> trackerIterator = mImageSelectionTrackers.iterator();
                while (trackerIterator.hasNext()) {
                    //Reading the current tracker
                    ImageSelectionTracker tracker = trackerIterator.next();
                    //(Ideally only SELECT entries needs to be present, but still not making any assumption)
                    if (tracker.getPhotoGridMode().equals(mGridMode)) {
                        //When the tracker is for SELECT

                        //Checking the selection state
                        if (tracker.isSelected()) {
                            //When the tracker was previously marked as selected, make it unselected
                            //(to update the same to the view later)
                            tracker.setSelected(false);
                        } else {
                            //When previously unselected, remove it from the list
                            trackerIterator.remove();
                        }
                    } else {
                        //When we find tracker of some other mode, delete it..
                        trackerIterator.remove();
                    }
                }
            } else {
                //When same Item Image is selected again

                //Restoring the previous list of Selection Trackers
                //(The method #onSameItemImageClicked would have marked the image item as unselected)
                mImageSelectionTrackers = tempImageSelectionTrackers;

                //Exiting..
                return;
            }

        } else if (mGridMode.equals(ProductImageContract.MODE_DELETE)) {
            //When the Grid Mode Action is Delete

            //Irrespective of whether a new or same Image item was marked for DELETE,
            //we need to delete the tracker that was previously unselected.
            Iterator<ImageSelectionTracker> trackerIterator = mImageSelectionTrackers.iterator();
            while (trackerIterator.hasNext()) {
                //Reading the current tracker
                ImageSelectionTracker tracker = trackerIterator.next();
                //(Ideally only DELETE entries needs to be present, but still not making any assumption)
                if (tracker.getPhotoGridMode().equals(mGridMode)) {
                    //When the tracker is for DELETE

                    if (!tracker.getImageContentUri().equals(productImage.getImageUri())
                            && !tracker.isSelected()) {
                        //When the tracker is not for the same Image item that was currently
                        //unselected from DELETE and was previously marked unselected from DELETE

                        //Remove the tracker from the list
                        trackerIterator.remove();
                    }

                } else {
                    //When we find tracker of some other mode, delete it..
                    trackerIterator.remove();
                }
            }

        }

        //Following steps applies for both Grid Modes (SELECT/DELETE)
        //(DELETE mode starts when the mode had changed for the previous long click action)

        if (!isSameItemClicked) {
            //When the Item Image clicked is a new Item

            //Build and Add the current item to the Tracker
            addItemImageToSelectionTracker(itemPosition, productImage, mGridMode);
        }

        //Update and sync the items' selection state to the View
        updateSelectionStateAndSyncTrackers();

        if (mGridMode.equals(ProductImageContract.MODE_DELETE)) {
            //When the Grid Mode Action is Delete

            //Show the live count of images selected for delete
            showDeleteCount();
        }
    }

    /**
     * Method invoked when the user does a Long click on an item in the RecyclerView Grid of Photos,
     * to delete from. This should also trigger the contextual action mode for delete action.
     *
     * @param itemPosition The adapter position of the Item Long clicked
     * @param productImage The {@link ProductImage} associated with the Item Long clicked
     * @param gridMode     The mode of the Action as defined by {@link ProductImageContract.PhotoGridSelectModeDef}
     */
    @Override
    public void onItemImageLongClicked(int itemPosition, ProductImage productImage,
                                       @ProductImageContract.PhotoGridSelectModeDef String gridMode) {
        if (gridMode.equals(ProductImageContract.MODE_DELETE)
                && !gridMode.equals(mGridMode)) {
            //When the Grid Mode Action just changed to DELETE

            //Reset and clear the selected state
            clearSelectedItems();

            //Save the Grid mode passed
            mGridMode = gridMode;

            //Notify the listener about the mode change to DELETE
            mPhotoGridDeleteModeListener.onGridItemDeleteMode();
        }

        //Following steps applies for only DELETE mode since Long click is for DELETE mode only

        //Check if the Item Image was previously selected or not, to make it unselected if present
        if (!onSameItemImageClicked(productImage)) {
            //When the Item Image clicked is a new Item

            //Build and Add the current item to the Tracker
            addItemImageToSelectionTracker(itemPosition, productImage, mGridMode);
        }

        //Update and sync the items' selection state to the View
        updateSelectionStateAndSyncTrackers();

        //Show the live count of images selected for delete
        showDeleteCount();
    }

    /**
     * Method that calculates and reports the live number of Image Items
     * selected for Delete in DELETE Action Mode.
     */
    @Override
    public void showDeleteCount() {
        if (mImageSelectionTrackers != null && mImageSelectionTrackers.size() > 0 && mGridMode.equals(ProductImageContract.MODE_DELETE)) {
            //When we have the selection trackers and the Grid Mode is in DELETE mode

            //Stores the number of image items selected for Delete action
            int liveDeleteItemCount = 0;

            //Iterating over the selection trackers to count the number of items
            //selected for Delete
            for (ImageSelectionTracker selectionTracker : mImageSelectionTrackers) {
                if (selectionTracker.getPhotoGridMode().equals(mGridMode)
                        && selectionTracker.isSelected()) {
                    //Incrementing when the current selection tracker's mode is Delete Mode
                    //and is selected for Delete
                    liveDeleteItemCount++;
                }
            }

            //Reporting the live count back to the listener to display the same
            mPhotoGridDeleteModeListener.showSelectedItemCount(liveDeleteItemCount);
        }
    }

    /**
     * Method that looks up the list of Selection Trackers {@code mImageSelectionTrackers}
     * to see if the Item Image was previously selected or not. Updates to unselected when found
     * and reports the same.
     *
     * @param productImage The {@link ProductImage} associated with the Item clicked / Long clicked
     * @return Boolean that indicates if the item was previously selected or not.
     * <b>TRUE</b> if it was previously selected; <b>FALSE</b> otherwise.
     */
    private boolean onSameItemImageClicked(ProductImage productImage) {
        //Saves if the Item Image was previously selected
        boolean previouslySelected = false;

        //Iterating over the list of trackers to see if the same Image item was selected previously
        for (ImageSelectionTracker selectionTracker : mImageSelectionTrackers) {
            if (selectionTracker.getImageContentUri().equals(productImage.getImageUri())
                    && selectionTracker.isSelected()) {
                //When the item was previously selected and present in the trackers list

                //Mark the item as unselected
                selectionTracker.setSelected(false);
                //Update as True to indicate that the item was previously selected
                previouslySelected = true;
                //Bail out as we have found and updated the existing tracker
                break;
            }
        }

        //Return the updated boolean
        return previouslySelected;
    }

    /**
     * Method that builds and adds an {@link ImageSelectionTracker} to the list of {@code mImageSelectionTrackers}
     * for the Item clicked / Long clicked.
     *
     * @param itemPosition The adapter position of the Item clicked / Long clicked
     * @param productImage The {@link ProductImage} associated with the Item clicked / Long clicked
     * @param gridMode     The mode of the Action as defined by {@link ProductImageContract.PhotoGridSelectModeDef}
     */
    private void addItemImageToSelectionTracker(int itemPosition, ProductImage productImage,
                                                @ProductImageContract.PhotoGridSelectModeDef String gridMode) {
        //Building the selection tracker using its Builder
        ImageSelectionTracker tracker = new ImageSelectionTracker.Builder()
                .setPosition(itemPosition)
                .setImageContentUri(productImage.getImageUri())
                .setPhotoGridMode(gridMode)
                .setSelected(true)
                .createTracker();
        //Adding the selection tracker built, to the list of selection trackers
        mImageSelectionTrackers.add(tracker);
    }

    /**
     * Method that builds and adds an {@link ImageSelectionTracker} to the list of {@code mImageSelectionTrackers}
     * for the Product Image {@code productImage} to be shown as selected, and updates the same to the View
     * immediately.
     *
     * @param itemPosition The adapter position of the Item to be shown as selected
     * @param productImage The {@link ProductImage} to be shown as selected that resides at the position {@code itemPosition}
     * @param gridMode     The mode of the Action as defined by {@link ProductImageContract.PhotoGridSelectModeDef}
     */
    private void showProductImageAsSelected(int itemPosition, ProductImage productImage,
                                            @ProductImageContract.PhotoGridSelectModeDef String gridMode) {
        //Reset and clear the selected state
        clearSelectedItems();
        //Add the current Image Item to be shown as selected
        addItemImageToSelectionTracker(itemPosition, productImage, gridMode);
        //Update and sync the items' selection state to the View
        updateSelectionStateAndSyncTrackers();
    }

    /**
     * Method that updates the Items' Selection State and syncs the Selection Trackers with the View.
     */
    private void updateSelectionStateAndSyncTrackers() {
        //Update the items state to the View
        mProductImageView.updateGridItemsState(mImageSelectionTrackers);
        //Sync the trackers with the View
        mProductImageView.syncSelectionTrackers(mImageSelectionTrackers);
    }

    /**
     * Method invoked when the user clicks on the Delete Contextual Action Menu
     * to delete the selected item photos.
     */
    @Override
    public void deleteSelection() {
        //Show the Progress Indicator
        mProductImageView.showProgressIndicator(R.string.product_image_status_deleting);

        //For building the list of Image File URIs to be deleted
        ArrayList<String> fileContentUriList = new ArrayList<>();
        //Reading the trackers for the URI and adding it to the list of URIs
        for (ImageSelectionTracker selectionTracker : mImageSelectionTrackers) {
            fileContentUriList.add(selectionTracker.getImageContentUri());
        }

        //Executing the delete operation on the URIs
        mStoreRepository.deleteImageFiles(fileContentUriList, new FileRepository.FileOperationsCallback<Boolean>() {
            /**
             * Method invoked when the file operation was executed successfully.
             *
             * @param results The results of the operation in the generic type passed.
             */
            @Override
            public void onSuccess(Boolean results) {
                //Delete the ProductImage Items selected for delete, from the view
                startDeleteItems();

                //Show delete success message
                mProductImageView.showDeleteSuccess();
            }

            /**
             * Method invoked when the file operation failed to complete.
             *
             * @param messageId The String resource of the error message
             *                  for the file operation failure
             * @param args      Variable number of arguments to replace the format specifiers
             */
            @Override
            public void onFailure(int messageId, @Nullable Object... args) {
                //Delete the ProductImage Items selected for delete, from the view
                startDeleteItems();

                //Show the failure message
                mProductImageView.showError(messageId, args);
            }

            /**
             * Method that starts the delete operation on {@link ProductImage} Items
             * displayed by the RecyclerView adapter
             */
            private void startDeleteItems() {
                //Iterating over the ProductImages List to delete the file URIs deleted
                Iterator<ProductImage> productImagesIterator = mProductImages.iterator();
                while (productImagesIterator.hasNext()) {
                    //Get the ProductImage
                    ProductImage currentProductImage = productImagesIterator.next();
                    //Check if the URI of ProductImage exists in the list of file URIs deleted
                    if (fileContentUriList.contains(currentProductImage.getImageUri())) {
                        //Delete this item from the ProductImage list as the file URI is deleted
                        productImagesIterator.remove();
                    }
                }

                //Clear the trackers first, and then resync with the View: START
                mImageSelectionTrackers.clear();

                //Update and sync the items' selection state to the View
                //(Required for clearing the selection tracker map in the RecyclerView's Adapter)
                updateSelectionStateAndSyncTrackers();
                //Clear the trackers first, and then resync with the View: END

                //Remove the file URIs deleted, from the list of Image URIs saved for avoiding duplicates
                mProductImageUris.removeAll(fileContentUriList);

                //Submit the updated list to the RecyclerView's Adapter
                submitListToAdapter(mProductImages);

                //Restore the last chosen ProductImage if present
                restoreLastChosenProductImage();

                //Hide the Progress indicator
                mProductImageView.hideProgressIndicator();
            }
        });
    }

    /**
     * Method that unselects/resets all the selected Image Items in the adapter and the selection trackers
     * and then clears the trackers to prepare for next user action.
     */
    @Override
    public void clearSelectedItems() {
        //Proceed only when we have the trackers
        if (mImageSelectionTrackers.size() > 0) {
            //Unselect all the selected ones in the trackers
            for (ImageSelectionTracker selectionTracker : mImageSelectionTrackers) {
                if (selectionTracker.isSelected()) {
                    selectionTracker.setSelected(false);
                }
            }

            //Clear/Reset all the selected items state in the adapter
            mProductImageView.updateGridItemsState(mImageSelectionTrackers);

            //Clear the tracker after resetting the selected items
            mImageSelectionTrackers.clear();

            //Update and sync the items' selection state to the View
            //(Required for clearing the selection tracker map in the RecyclerView's Adapter)
            updateSelectionStateAndSyncTrackers();
        }
    }

    /**
     * Method invoked when the user exits from the Contextual Delete Action Mode
     * without taking any action on the selected items.
     */
    @Override
    public void onDeleteModeExit() {
        //Reset and clear the selected state
        clearSelectedItems();
        //Restore the last chosen image item if present
        restoreLastChosenProductImage();
    }

    /**
     * Method invoked by the View when the activity was restored, to restore the list of
     * Selection Trackers.
     *
     * @param imageSelectionTrackers List of Selection Trackers {@link ImageSelectionTracker}
     *                               used for updating the state of the items in the RecyclerView
     *                               Grid of Photos, which are marked for select/delete
     */
    @Override
    public void restoreSelectionTrackers(ArrayList<ImageSelectionTracker> imageSelectionTrackers) {
        if (imageSelectionTrackers != null && imageSelectionTrackers.size() > 0) {
            //When we have the previous data of Selection Trackers,
            //restore them to the Presenter and the View
            mImageSelectionTrackers = imageSelectionTrackers;

            //Update and sync the items' selection state to the View
            updateSelectionStateAndSyncTrackers();

            //Restore the #mGridMode based on the number of items marked for SELECT and/or DELETE
            restoreMode();
        }
    }

    /**
     * Method that restores the {@link #mGridMode} based on the number of items
     * marked for Select and/or Delete in the selection trackers {@link #mImageSelectionTrackers}
     */
    private void restoreMode() {
        //Number of Items marked for Select and/or Delete, to restore/correct the #mGridMode
        int noOfItemsMarkedForSelect = 0;
        int noOfItemsMarkedForDelete = 0;
        int noOfItemsMarkedAndSelectedForDelete = 0;

        //Read the trackers to restore the mode
        for (ImageSelectionTracker selectionTracker : mImageSelectionTrackers) {
            //Increment the count of the items marked for Select and/or Delete
            if (selectionTracker.getPhotoGridMode().equals(ProductImageContract.MODE_SELECT)) {
                noOfItemsMarkedForSelect++;
            } else if (selectionTracker.getPhotoGridMode().equals(ProductImageContract.MODE_DELETE)) {
                noOfItemsMarkedForDelete++;
                if (selectionTracker.isSelected()) {
                    noOfItemsMarkedAndSelectedForDelete++;
                }
            }
        }

        //Restore the mode based on the count
        if (noOfItemsMarkedForDelete + noOfItemsMarkedForSelect == 0) {
            //When both are zero, clear the mode
            mGridMode = "";
        } else if (noOfItemsMarkedForDelete > noOfItemsMarkedForSelect) {
            //When we have more items marked for DELETE, set the mode to DELETE
            mGridMode = ProductImageContract.MODE_DELETE;
            //Delegating to the listener to show the DELETE mode action
            mPhotoGridDeleteModeListener.onGridItemDeleteMode();
            //Restore the delete item count to be shown
            mPhotoGridDeleteModeListener.showSelectedItemCount(noOfItemsMarkedAndSelectedForDelete);
        } else {
            //When we have more items marked for SELECT, set the mode to SELECT
            mGridMode = ProductImageContract.MODE_SELECT;
        }
    }

    /**
     * Method invoked by the View when the activity was started/restored, to restore the list of
     * {@link ProductImage} objects.
     *
     * @param productImages List of {@link ProductImage}, each of which holds the URI information
     *                      of the Image File.
     */
    @Override
    public void restoreProductImages(ArrayList<ProductImage> productImages) {
        //Save the list in the Presenter
        mProductImages = productImages;

        //Send the list to the RecyclerView's Adapter to display
        submitListToAdapter(mProductImages);

        //Restore the preselected Product Image to be shown
        restorePreselectedProductImage();

        //Building a list of Image URIs to keep a track of duplicate images
        mProductImageUris = new ArrayList<>();
        for (ProductImage productImage : mProductImages) {
            mProductImageUris.add(productImage.getImageUri());
        }
    }

    /**
     * Method that displays a Preselected {@link ProductImage} if present. When not present,
     * a default image will be shown. When no {@link ProductImage}s are found, a default image
     * will be shown and the Grid View will be hidden to display a message requesting
     * the user to add images for the Product.
     */
    private void restorePreselectedProductImage() {
        //The number of ProductImages present
        int noOfProductImages = mProductImages.size();

        //When we have ProductImages
        if (noOfProductImages > 0) {
            //Show the Grid View when there are images
            mProductImageView.showGridView();

            //Saves the ProductImage to be shown
            ProductImage productImageToBeShown = null;
            //Saves the Position of the ProductImage to be shown
            int itemPosition = -1;

            //Iterating over the list to find the ProductImage to be shown
            for (int index = 0; index < noOfProductImages; index++) {
                //Get the current index item
                ProductImage productImage = mProductImages.get(index);
                //Looking for IsDefault set to True
                if (productImage.isDefault()) {
                    //When found, save the ProductImage and break
                    productImageToBeShown = productImage;
                    itemPosition = index;
                    break;
                }
            }

            if (productImageToBeShown == null) {
                //When we do not have the ProductImage to be shown,
                //Use the first Image Item in the list

                itemPosition = 0;
                productImageToBeShown = mProductImages.get(itemPosition);
            }

            //Add the current Image Item to be shown as selected
            mGridMode = ProductImageContract.MODE_SELECT;
            showProductImageAsSelected(itemPosition, productImageToBeShown, mGridMode);

            //NOTE: mLastChosenProductImage will be updated automatically by the View
            //when the Image Item is shown as selected, via a call to #syncLastChosenProductImage

        } else {
            //Hide the Grid View when there are no images
            mProductImageView.hideGridView();

            //Delegating to the listener to show the default image
            mSelectedPhotoActionsListener.showDefaultImage();
        }
    }

    /**
     * Method invoked by the View when an Item Image was selected by the User to be shown.
     *
     * @param productImage The {@link ProductImage} associated with the Item Image selected,
     *                     which contains the URI information of the Image File.
     */
    @Override
    public void syncLastChosenProductImage(ProductImage productImage) {
        //Save the ProductImage info
        mLastChosenProductImage = productImage;
    }

    /**
     * Method that restores the last chosen {@link ProductImage} to be shown as selected if present;
     * otherwise shows the preselected {@link ProductImage} if present. When no {@link ProductImage}s
     * are present, a default image will be shown.
     */
    private void restoreLastChosenProductImage() {
        if (mLastChosenProductImage != null && mProductImages != null && mProductImages.size() > 0) {
            //When we have all the data required to restore the last chosen Product Image

            //Saves the position of the item last chosen if present
            int itemPosition = -1;

            //The number of ProductImages present
            int noOfProductImages = mProductImages.size();

            //Check to see if the last chosen ProductImage is currently present in the list
            for (int index = 0; index < noOfProductImages; index++) {
                //Get the current index ProductImage
                ProductImage productImage = mProductImages.get(index);
                //Compare the URI string to see if it is present
                if (productImage.getImageUri().equals(mLastChosenProductImage.getImageUri())) {
                    //When found, save the position and bail out of the loop
                    itemPosition = index;
                }
            }

            if (itemPosition > -1) {
                //When the Item Image was found in the list, restore the item selection

                //Add the current Image Item to be shown as selected
                mGridMode = ProductImageContract.MODE_SELECT;
                showProductImageAsSelected(itemPosition, mLastChosenProductImage, mGridMode);

            } else {
                //When the Item Image was not found in the list, set to Null
                mLastChosenProductImage = null;

                //Restore the preselected Product Image to be shown
                restorePreselectedProductImage();
            }

        } else if (mProductImages != null && mProductImages.size() > 0) {
            //When we do not have the last chosen image to be shown

            //Restore the preselected Product Image to be shown
            restorePreselectedProductImage();
        } else {
            //When we do not have any images

            //Hide the Grid View
            mProductImageView.hideGridView();

            //Delegating to the listener to show the default image
            mSelectedPhotoActionsListener.showDefaultImage();
        }
    }

    /**
     * Method invoked when the user clicks on the Select Menu Action,
     * or on the "Save" button, on the Unsaved changes dialog.
     */
    @Override
    public void onSelectAction() {
        //Make changes to the list of ProductImages to update the state of current selected Image if any
        if (mLastChosenProductImage != null && mProductImages != null && mProductImages.size() > 0) {
            //When we have an Image that was last chosen to be shown as selected

            //Tracks whether the Preselected Product Image was unselected
            boolean oldItemUnSelected = false;
            //Tracks whether the new Product Image was selected
            boolean newItemSelected = false;

            if (mProductImages.size() == 1) {
                //When there is only one image in the list, update 'oldItemUnSelected' to true
                //as there will be no item to unselect
                oldItemUnSelected = true;
            }

            //Iterate over the list of ProductImages to update the
            //selected state of preselected Product Image to unselected, and select the new one
            for (ProductImage productImage : mProductImages) {
                //When preselected ProductImage is found, unselect the same and update the boolean
                if (!oldItemUnSelected && productImage.isDefault()) {
                    productImage.setDefault(false); //Unselecting..
                    oldItemUnSelected = true;
                }

                //When the last chosen ProductImage is found, select the same and update the boolean
                if (!newItemSelected && productImage.getImageUri().equals(mLastChosenProductImage.getImageUri())) {
                    productImage.setDefault(true); //Selecting as Default
                    newItemSelected = true;
                }

                //Bail out when the work is done
                if (oldItemUnSelected && newItemSelected) {
                    break;
                }
            }
        }

        //After the required changes are done in the list of ProductImages,
        //make a call to the Navigator's doSetResult() to pass the information back to the parent activity
        doSetResult();
    }

    /**
     * Method invoked when the user clicks on the "Ignore" button, on the
     * Unsaved changes dialog.
     */
    @Override
    public void onIgnoreAction() {
        if (mProductImages != null && mProductImages.size() > 0) {
            //When we have Product Images

            //Check to see if we have at least one image set as default: START
            //Tracks whether any Product Image was marked as selected/default
            boolean anyItemSelected = false;

            //Iterating over the list of Product Images to see if any Image has been set as default
            for (ProductImage productImage : mProductImages) {
                if (productImage.isDefault()) {
                    //When the current Product Image is marked as default,
                    //update the flag to true and bail out
                    anyItemSelected = true;
                    break;
                }
            }
            //Check to see if we have at least one image set as default: END

            if (!anyItemSelected) {
                //When none of the Images were marked as default

                //NOTE: At least one Image should be set as default. Otherwise, the Product List
                //will not show up the Product entry even if it exists.

                if (mLastChosenProductImage != null) {
                    //If we have a previously chosen Product Image, set that as default

                    //Iterating over the list of Product Images to lookup the last chosen
                    for (ProductImage productImage : mProductImages) {
                        if (productImage.getImageUri().equals(mLastChosenProductImage.getImageUri())) {
                            //When we have found the last chosen Product Image, update it as default
                            productImage.setDefault(true);
                            //update the flag to true
                            anyItemSelected = true;
                        }
                    }

                    if (!anyItemSelected) {
                        //If we still do not have any item selected,
                        //then it means that the last chosen image was not found in the list.

                        //In this case, set the first Image as default
                        mProductImages.get(0).setDefault(true);
                    }

                } else {
                    //If we do not have any previously chosen Product Image, set the first Image as default
                    mProductImages.get(0).setDefault(true);
                }
            }
        }

        //After the required changes are done in the list of ProductImages,
        //make a call to the Navigator's doSetResult() to pass the information back to the parent activity
        doSetResult();
    }

    /**
     * Method invoked when the user clicks on the android home/up button
     * or the back key is pressed
     */
    @Override
    public void onUpOrBackAction() {
        if (mLastChosenProductImage != null) {
            //When we have an Image that was last chosen to be shown as selected

            //Show the discard dialog to see if the user wants to keep/discard the changes
            mProductImageView.showDiscardDialog();
        } else {
            //When we do not have an Image that was last chosen to be shown as selected

            //Make a call to the Navigator's doSetResult() to pass the information back to the parent activity
            doSetResult();
        }
    }

    /**
     * Method that sets the result and passes the information back to the calling Parent Activity
     */
    @Override
    public void doSetResult() {
        mProductImageNavigator.doSetResult(mProductImages);
    }

    /**
     * Method invoked to show the selected Image {@code bitmap} for the Product.
     *
     * @param bitmap   The {@link Bitmap} of the Image to be shown.
     * @param imageUri The String Content URI of the Image to be shown.
     */
    @Override
    public void showSelectedImage(Bitmap bitmap, String imageUri) {
        //Delegating the call to the listener to show the selected Photo
        mSelectedPhotoActionsListener.showSelectedImage(bitmap, imageUri);
    }

}