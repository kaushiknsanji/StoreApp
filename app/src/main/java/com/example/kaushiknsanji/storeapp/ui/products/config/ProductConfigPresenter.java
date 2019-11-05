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

package com.example.kaushiknsanji.storeapp.ui.products.config;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.DataRepository;
import com.example.kaushiknsanji.storeapp.data.StoreRepository;
import com.example.kaushiknsanji.storeapp.data.local.models.Product;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductAttribute;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.ui.products.image.ProductImageActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The Presenter class that implements {@link ProductConfigContract.Presenter} on the lines of
 * Model-View-Presenter architecture. This Presenter interfaces with the App repository {@link StoreRepository}
 * to create/modify the Product Entry in the database and updates the same to
 * the View {@link ProductConfigActivityFragment} to load and display it.
 *
 * @author Kaushik N Sanji
 */
public class ProductConfigPresenter implements ProductConfigContract.Presenter {

    //Constant used for Logs
    private static final String LOG_TAG = ProductConfigPresenter.class.getSimpleName();

    //The Product ID of an Existing Product, opened for editing/deleting the Product
    //Will be ProductConfigContract#NEW_PRODUCT_INT if the request is for a New Product Entry
    private final int mProductId;

    //The View Interface of this Presenter
    @NonNull
    private final ProductConfigContract.View mProductConfigView;

    //Instance of the App Repository
    @NonNull
    private final StoreRepository mStoreRepository;

    //Navigator that receives callbacks when navigating away from the Current Activity
    private final ProductConfigNavigator mProductConfigNavigator;

    //Listener that receives callbacks when there is a change in the default photo of the Product
    private final DefaultPhotoChangeListener mDefaultPhotoChangeListener;

    //Stores Existing Product details if this is an Edit request
    private Product mExistingProduct;

    //Stores the list of Categories downloaded from the database
    private ArrayList<String> mCategoriesDownloaded;

    //Stores the URI details of the Product Images
    private ArrayList<ProductImage> mProductImages;

    //Stores the state of Existing Product details restored,
    //to prevent updating the fields every time during System config change
    private boolean mIsExistingProductRestored;

    //Stores whether the Product SKU entered was valid or not
    private boolean mIsProductSkuValid;

    //Stores whether the Product Name was entered or not.
    //Used for monitoring unsaved progress
    private boolean mIsProductNameEntered;

    //Stores whether we received Product Images from the ProductImageActivity
    //to take action on it when the View is resumed
    private boolean mIsExtraProductImagesReceived;

    /**
     * Constructor of {@link ProductConfigPresenter}
     *
     * @param productId                  The integer value of the Product Id of an existing Product;
     *                                   or {@link ProductConfigContract#NEW_PRODUCT_INT} for a New Product Entry.
     * @param storeRepository            Instance of {@link StoreRepository} for accessing/manipulating the data
     * @param productConfigView          The View instance {@link ProductConfigContract.View} of this Presenter
     * @param productConfigNavigator     Instance of {@link ProductConfigNavigator} that receives callbacks
     *                                   when navigating away from the Current Activity
     * @param defaultPhotoChangeListener Instance of {@link DefaultPhotoChangeListener} that receives callbacks
     *                                   when there is a change in the default photo of the Product
     */
    ProductConfigPresenter(int productId,
                           @NonNull StoreRepository storeRepository,
                           @NonNull ProductConfigContract.View productConfigView,
                           @NonNull ProductConfigNavigator productConfigNavigator,
                           @NonNull DefaultPhotoChangeListener defaultPhotoChangeListener) {
        mProductId = productId;
        mStoreRepository = storeRepository;
        mProductConfigView = productConfigView;
        mProductConfigNavigator = productConfigNavigator;
        mDefaultPhotoChangeListener = defaultPhotoChangeListener;

        //Registering the View with the Presenter
        mProductConfigView.setPresenter(this);
    }

    /**
     * Invoked during the Fragment#onResume()
     */
    @Override
    public void start() {
        //Load an updated list of Categories
        loadCategories();

        if (mProductId == ProductConfigContract.NEW_PRODUCT_INT) {
            //When it is a New Product Entry

            if (mProductImages == null) {
                //When there are no Product Images yet

                //Delegating to the listener to show the Default Image
                mDefaultPhotoChangeListener.showDefaultImage();
            }

        } else {
            //When it is an Edit request, load existing product details
            loadExistingProduct();
        }

    }

    /**
     * Method that updates the state of "Existing Product details restored", and keeps it in sync with the View.
     *
     * @param isExistingProductRestored Boolean that indicates the state of Existing Product data restored.
     *                                  <b>TRUE</b> if it had been restored; <b>FALSE</b> otherwise.
     */
    @Override
    public void updateAndSyncExistingProductState(boolean isExistingProductRestored) {
        //Saving the state
        mIsExistingProductRestored = isExistingProductRestored;

        //Syncing the state to the View
        mProductConfigView.syncExistingProductState(mIsExistingProductRestored);
    }

    /**
     * Method that updates the state of "Product SKU Validity", and keeps it in sync with the View.
     * This method also informs the User to change the value if it is invalid.
     *
     * @param isProductSkuValid Boolean that indicates whether the Product SKU entered was valid or not.
     *                          <b>TRUE</b> if the Product SKU is valid; <b>FALSE</b> otherwise.
     */
    @Override
    public void updateAndSyncProductSkuValidity(boolean isProductSkuValid) {
        //Saving the state
        mIsProductSkuValid = isProductSkuValid;

        //Syncing the state to the View
        mProductConfigView.syncProductSkuValidity(mIsProductSkuValid);

        if (!mIsProductSkuValid) {
            //Informing the User to change the value if invalid
            mProductConfigView.showProductSkuConflictError();
        }
    }

    /**
     * Method that updates the state of "Product Name entered", and keeps it in sync with the View.
     * This is used for monitoring unsaved progress.
     *
     * @param isProductNameEntered Boolean that indicates whether the Product Name has been entered by the User or not.
     *                             <b>TRUE</b> if the Product Name is entered; <b>FALSE</b> otherwise.
     */
    @Override
    public void updateAndSyncProductNameEnteredState(boolean isProductNameEntered) {
        //Saving the state
        mIsProductNameEntered = isProductNameEntered;

        //Syncing the state to the View
        mProductConfigView.syncProductNameEnteredState(mIsProductNameEntered);
    }

    /**
     * Method that downloads the predefined list of categories,
     * to update the Product Config Category Spinner
     */
    private void loadCategories() {
        mStoreRepository.getAllCategories(new DataRepository.GetQueryCallback<List<String>>() {
            /**
             * Method invoked when the results are obtained
             * for the query executed.
             *
             * @param results The query results in the generic type passed
             */
            @Override
            public void onResults(List<String> results) {
                //Saving the list of categories retrieved
                mCategoriesDownloaded = new ArrayList<>();
                mCategoriesDownloaded.addAll(results);
                //Appending the 'Other' Category in the end
                results.add(ProductConfigContract.CATEGORY_OTHER);
                //Passing the categories retrieved, to the view
                mProductConfigView.updateCategories(results);
            }

            /**
             * Method invoked when there are no results
             * for the query executed.
             */
            @Override
            public void onEmpty() {
                List<String> categories = new ArrayList<>();
                //Appending the 'Other' Category in the end
                categories.add(ProductConfigContract.CATEGORY_OTHER);
                //Passing an empty list to the view
                mProductConfigView.updateCategories(categories);
            }
        });
    }

    /**
     * Method that downloads the existing product details when this is an Edit request,
     * to update the View Components with the product data.
     */
    private void loadExistingProduct() {
        //Display progress indicator
        mProductConfigView.showProgressIndicator(R.string.product_config_status_loading_existing_product);

        //Retrieving the Existing Product Details from the Repository
        mStoreRepository.getProductDetailsById(mProductId, new DataRepository.GetQueryCallback<Product>() {
            /**
             * Method invoked when the results are obtained
             * for the query executed.
             *
             * @param product The Product details loaded for the Product Id passed
             */
            @Override
            public void onResults(Product product) {
                //Update the Product details to the View
                if (!mIsExistingProductRestored) {
                    //When Product details have not been loaded into the views yet

                    //Update the Product Name field
                    updateProductNameField(product.getName());
                    //Update the Product SKU field
                    updateProductSkuField(product.getSku());
                    //Update the Product Description
                    updateProductDescriptionField(product.getDescription());
                    //Update the Product Category Selection
                    updateCategorySelection(product.getCategory(), null);
                    //Update the Product Attributes
                    updateProductAttributes(product.getProductAttributes());
                    //Update the Product Images
                    updateProductImages(product.getProductImages());

                    //Updating and syncing the state of existing Product data restore
                    updateAndSyncExistingProductState(true);

                    //Updating and syncing the state of Product SKU Validity,
                    //This is always true since this is an Existing Product Data
                    updateAndSyncProductSkuValidity(true);

                    //Updating and syncing the state of Product Name Entered,
                    //This is always true since this is an Existing Product Data
                    updateAndSyncProductNameEnteredState(true);
                }

                //Lock the Product SKU field from being edited, as this is an Existing Product
                mProductConfigView.lockProductSkuField();

                //Saving the Existing Product Details
                mExistingProduct = product;

                //Call the Method to save the Product Images when
                //they are received from ProductImageActivity
                if (mIsExtraProductImagesReceived) {
                    onExtraProductImagesReceived();
                }

                //Hide progress indicator
                mProductConfigView.hideProgressIndicator();
            }

            /**
             * Method invoked when the results could not be retrieved
             * for the query due to some error.
             *
             * @param messageId The String resource of the error message
             *                  for the query execution failure
             * @param args Variable number of arguments to replace the format specifiers
             *             in the String resource if any
             */
            @Override
            public void onFailure(@StringRes int messageId, @Nullable Object... args) {
                //Hide progress indicator
                mProductConfigView.hideProgressIndicator();

                //Show the error message
                mProductConfigView.showError(messageId, args);
            }

            /**
             * Method invoked when there are no results
             * for the query executed.
             */
            @Override
            public void onEmpty() {
                //No-op, not called for this implementation
            }
        });
    }

    /**
     * Method that updates the Product Category to the View.
     *
     * @param selectedCategory  The selected Category of the Product
     * @param categoryOtherText The Category OTHER EditText field value, in the case where the Product Category
     *                          was not available from the Predefined list of Categories.
     */
    @Override
    public void updateCategorySelection(String selectedCategory, @Nullable String categoryOtherText) {
        if (selectedCategory.equals(ProductConfigContract.CATEGORY_OTHER)) {
            //When the selected Category is Category OTHER
            if (TextUtils.isEmpty(categoryOtherText)) {
                //When Category Other Text is Empty,
                //just enable the other text field and set it to empty text
                mProductConfigView.showCategoryOtherEditTextField();
                mProductConfigView.clearCategoryOtherEditTextField();
                //Update the Category selected to the View
                mProductConfigView.updateCategorySelection(selectedCategory, null);
            } else {
                //When Category Other Text is NOT empty

                categoryOtherText = categoryOtherText.trim();
                //Check if this manually entered category already exists in the list
                if (mCategoriesDownloaded.contains(categoryOtherText)) {
                    //When the manually entered category already exists
                    //Select the corresponding category and hide the other text field
                    mProductConfigView.updateCategorySelection(categoryOtherText, null);
                    //Clearing the Category OTHER EditText Field
                    mProductConfigView.clearCategoryOtherEditTextField();
                    //Hiding the Category OTHER EditText Field
                    mProductConfigView.hideCategoryOtherEditTextField();
                } else {
                    //When the manually entered category does not exist
                    //Ensure that the other text field is enabled, and update the text
                    mProductConfigView.showCategoryOtherEditTextField();
                    mProductConfigView.updateCategorySelection(selectedCategory, categoryOtherText);
                }
            }
        } else {
            //When the selected Category is a predefined Category
            //Update the Category selected to the View
            mProductConfigView.updateCategorySelection(selectedCategory, null);
        }
    }

    /**
     * Method that updates the List of Product Images {@code productImages} to the View
     * and also loads the Selected Product Image to be shown if present.
     *
     * @param productImages The List of {@link ProductImage} of a Product.
     */
    @Override
    public void updateProductImages(ArrayList<ProductImage> productImages) {
        //Saving the list of Product Images
        mProductImages = productImages;

        //Updating the Product Images to the View
        mProductConfigView.updateProductImages(productImages);

        //Finding the Product Image to be shown if Present: START
        if (mProductImages != null && mProductImages.size() > 0) {
            //When we have Product Images

            //Saves the ProductImage to be shown
            ProductImage productImageToBeShown = null;

            //Iterating over the list of Images to find the selected Image
            for (ProductImage productImage : mProductImages) {
                if (productImage.isDefault()) {
                    //When selected, grab the ProductImage and bail out of the loop
                    productImageToBeShown = productImage;
                    break;
                }
            }

            if (productImageToBeShown != null) {
                //Delegating to the listener to show the selected Product Image by passing the String URI of the Image to the View
                mDefaultPhotoChangeListener.showSelectedProductImage(productImageToBeShown.getImageUri());
            } else {
                //This case should not occur, since at least one image needs to be selected.

                //Logging the error
                Log.e(LOG_TAG, "ERROR!!! updateProductImages: Product Images found but no default image");

                //Delegating to the listener to show the Default Image
                mDefaultPhotoChangeListener.showDefaultImage();
            }
        } else {
            //When there are no images, delegate to the listener to show the default image instead
            mDefaultPhotoChangeListener.showDefaultImage();
        }
        //Finding the Product Image to be shown if Present: END
    }

    /**
     * Method that updates the List of Product Attributes {@code productAttributes} to the View
     *
     * @param productAttributes The List of {@link ProductAttribute} of a Product
     */
    @Override
    public void updateProductAttributes(ArrayList<ProductAttribute> productAttributes) {
        mProductConfigView.updateProductAttributes(productAttributes);
    }

    /**
     * Method that updates the Product Description {@code description} to the View
     *
     * @param description The description of the Product
     */
    @Override
    public void updateProductDescriptionField(String description) {
        mProductConfigView.updateProductDescriptionField(description);
    }

    /**
     * Method that updates the Product SKU {@code sku} to the View
     *
     * @param sku The SKU of the Product
     */
    @Override
    public void updateProductSkuField(String sku) {
        mProductConfigView.updateProductSkuField(sku);
    }

    /**
     * Method that updates the Product Name {@code name} to the View
     *
     * @param name The Name of the Product
     */
    @Override
    public void updateProductNameField(String name) {
        mProductConfigView.updateProductNameField(name);
    }

    /**
     * Method invoked when a Category is selected by the user
     *
     * @param categoryName The Name of the Product Category selected by the user
     */
    @Override
    public void onCategorySelected(String categoryName) {
        if (categoryName.equals(ProductConfigContract.CATEGORY_OTHER)) {
            //Displaying the associated Edit field when 'Other' category is selected
            mProductConfigView.showCategoryOtherEditTextField();
        } else {
            //Hiding the Edit field of 'Other' category when a predefined category is selected
            mProductConfigView.hideCategoryOtherEditTextField();
        }
    }

    /**
     * Method invoked when the 'Save' Menu button is clicked.
     *
     * @param productName        The Name of the Product entered by the User
     * @param productSku         The SKU of the Product entered by the User
     * @param productDescription The Description of the Product entered by the User
     * @param categorySelected   The Category selected for the Product
     * @param categoryOtherText  The Custom Category entered by the User when not found in
     *                           predefined Categories
     * @param productAttributes  The Attributes describing the Product, which are optional
     */
    @Override
    public void onSave(String productName, String productSku, String productDescription,
                       String categorySelected, String categoryOtherText,
                       ArrayList<ProductAttribute> productAttributes) {

        //Display save progress indicator
        mProductConfigView.showProgressIndicator(R.string.product_config_status_saving);

        //Product SKU Empty/Uniqueness validation
        if (!mIsExistingProductRestored && !mIsProductSkuValid) {
            //If the Product SKU is still invalid/empty at the time of clicking Save button
            //(Not applicable when Existing Product is being edited, since the SKU is locked)

            //Hide Progress Indicator
            mProductConfigView.hideProgressIndicator();

            if (TextUtils.isEmpty(productSku)) {
                //When Product SKU is empty, show the empty error
                mProductConfigView.showProductSkuEmptyError();
            } else {
                //When Product SKU is present, show the invalid error
                mProductConfigView.showProductSkuConflictError();
            }
            return; //Exiting
        }

        //Empty Fields validation
        if (TextUtils.isEmpty(productName) || TextUtils.isEmpty(productSku) ||
                TextUtils.isEmpty(productDescription) || TextUtils.isEmpty(categorySelected) ||
                (categorySelected.equals(ProductConfigContract.CATEGORY_OTHER)
                        && TextUtils.isEmpty(categoryOtherText))) {
            //When required data is missing, report the same to the user and bail out
            //Hide Progress Indicator
            mProductConfigView.hideProgressIndicator();
            mProductConfigView.showEmptyFieldsValidationError();
            return; //Exiting
        }

        //Scanning Product Attributes for any Partially filled fields, Empty rows and duplicate Attribute Names : START
        Iterator<ProductAttribute> productAttributeIterator = productAttributes.iterator();
        //Stores a list of Product Attribute Names to check for any duplicate Attribute Names
        ArrayList<String> productAttributeNames = new ArrayList<>();
        while (productAttributeIterator.hasNext()) {
            //Retrieving the current ProductAttribute
            ProductAttribute productAttribute = productAttributeIterator.next();
            //Retrieving the ProductAttribute member values
            String attributeName = productAttribute.getAttributeName();
            String attributeValue = productAttribute.getAttributeValue();

            //Validating the ProductAttribute member values
            if (TextUtils.isEmpty(attributeName) && TextUtils.isEmpty(attributeValue)) {
                //Deleting the record when both Name and Value of the Attribute is empty
                productAttributeIterator.remove();
            } else if (TextUtils.isEmpty(attributeName) || TextUtils.isEmpty(attributeValue)) {
                //When either Name or Attribute is filled and not both, report the same to user and bail out
                //Hide Progress Indicator
                mProductConfigView.hideProgressIndicator();
                mProductConfigView.showAttributesPartialValidationError();
                return; //Exiting
            }

            //Checking for duplicate Product Attribute Name
            if (!TextUtils.isEmpty(attributeName)) {
                //When an Attribute Name is present

                if (productAttributeNames.contains(attributeName)) {
                    //If the list already contains the current Attribute Name,
                    //then report the same to the user and bail out
                    //Hide Progress Indicator
                    mProductConfigView.hideProgressIndicator();
                    mProductConfigView.showAttributeNameConflictError(attributeName);
                    return; //Exiting
                } else {
                    //If the current Attribute Name is unique,
                    //then update the same to the list for scanning duplicates
                    productAttributeNames.add(attributeName);
                }
            }
        }
        //Scanning Product Attributes for any Partially filled fields, Empty rows and duplicate Attribute Names : END

        //Create the New/Updated Product
        Product newProduct = createProductForUpdate(productName, productSku, productDescription,
                categorySelected, categoryOtherText, productAttributes, mProductImages);

        //Start saving to database
        if (mProductId > ProductConfigContract.NEW_PRODUCT_INT) {
            //For Existing Product
            saveUpdatedProduct(mExistingProduct, newProduct);
        } else {
            //For New Product
            saveNewProduct(newProduct);
        }

    }

    /**
     * Method invoked when the Product SKU entered by user needs to be validated for its uniqueness.
     *
     * @param productSku The SKU of the Product entered by the User
     */
    @Override
    public void validateProductSku(String productSku) {
        if (TextUtils.isEmpty(productSku)) {
            //When Product SKU is empty, show the empty error and bail out
            mProductConfigView.showProductSkuEmptyError();
            return;
        }

        //Checking the uniqueness of the Product SKU with the Repository
        mStoreRepository.getProductSkuUniqueness(productSku, new DataRepository.GetQueryCallback<Boolean>() {
            /**
             * Method invoked when the results are obtained
             * for the query executed.
             *
             * @param results The query results in the generic type passed.
             *                <b>TRUE</b> if the {@code productSku} is Unique;
             *                <b>FALSE</b> otherwise.
             */
            @Override
            public void onResults(Boolean results) {
                //Update and Sync the Product SKU Validity based on the "results" boolean
                updateAndSyncProductSkuValidity(results);
            }

            /**
             * Method invoked when there are no results
             * for the query executed.
             */
            @Override
            public void onEmpty() {
                //No-op, for this query (Never called)
            }
        });
    }

    /**
     * Method that creates and returns the {@link Product} for the details passed.
     *
     * @param productName        The Name of the Product entered by the User
     * @param productSku         The SKU of the Product entered by the User
     * @param productDescription The Description of the Product entered by the User
     * @param categorySelected   The Category selected for the Product
     * @param categoryOtherText  The Custom Category entered by the User when not found in
     *                           predefined Categories
     * @param productAttributes  The Attributes describing the Product, which are optional
     * @param productImages      The Images of the Product, which are optional.
     * @return Instance of {@link Product} containing the details passed
     */
    private Product createProductForUpdate(String productName, String productSku,
                                           String productDescription, String categorySelected,
                                           String categoryOtherText,
                                           ArrayList<ProductAttribute> productAttributes,
                                           ArrayList<ProductImage> productImages) {
        //Retrieving the Correct category
        String categoryName = categorySelected;
        if (categorySelected.equals(ProductConfigContract.CATEGORY_OTHER)) {
            //When Category selected is other than the predefined categories
            categoryName = categoryOtherText;
        }

        //Building and returning the Product built with the details
        return new Product.Builder()
                .setId(mProductId)
                .setName(productName)
                .setSku(productSku)
                .setDescription(productDescription)
                .setCategory(categoryName)
                .setProductAttributes(productAttributes)
                .setProductImages(productImages)
                .createProduct();
    }

    /**
     * Method that saves the Updated Product details to the database.
     *
     * @param existingProduct The Existing Product details for figuring out the required
     *                        CRUD operations
     * @param newProduct      The New Updated Product details to be saved.
     */
    private void saveUpdatedProduct(Product existingProduct, Product newProduct) {
        mStoreRepository.saveUpdatedProduct(existingProduct, newProduct,
                new DataRepository.DataOperationsCallback() {
                    /**
                     * Method invoked when the database operations like insert/update/delete
                     * was successful.
                     */
                    @Override
                    public void onSuccess() {
                        //Hide progress indicator
                        mProductConfigView.hideProgressIndicator();

                        //Set the result and finish on successful update
                        doSetResult(ProductConfigActivity.RESULT_EDIT_PRODUCT, newProduct.getId(), newProduct.getSku());
                    }

                    /**
                     * Method invoked when the database operations like insert/update/delete
                     * failed to complete.
                     *
                     * @param messageId The String resource of the error message
                     *                  for the database operation failure
                     * @param args      Variable number of arguments to replace the format specifiers
                     *                  in the String resource if any
                     */
                    @Override
                    public void onFailure(int messageId, @Nullable Object... args) {
                        //Hide progress indicator
                        mProductConfigView.hideProgressIndicator();

                        //Show message for Update Failure
                        mProductConfigView.showError(messageId, args);
                    }
                });
    }

    /**
     * Method that saves the New Product details to the database
     *
     * @param newProduct The New Product details to be saved.
     */
    private void saveNewProduct(Product newProduct) {
        mStoreRepository.saveNewProduct(newProduct, new DataRepository.DataOperationsCallback() {
            /**
             * Method invoked when the database operations like insert/update/delete
             * was successful.
             */
            @Override
            public void onSuccess() {
                //Hide progress indicator
                mProductConfigView.hideProgressIndicator();

                //Set the result and finish on successful insert
                doSetResult(ProductConfigActivity.RESULT_ADD_PRODUCT, newProduct.getId(), newProduct.getSku());
            }

            /**
             * Method invoked when the database operations like insert/update/delete
             * failed to complete.
             *
             * @param messageId The String resource of the error message
             *                  for the database operation failure
             * @param args      Variable number of arguments to replace the format specifiers
             *                  in the String resource if any
             */
            @Override
            public void onFailure(int messageId, @Nullable Object... args) {
                //Hide progress indicator
                mProductConfigView.hideProgressIndicator();

                //Show message for Insert Failure
                mProductConfigView.showError(messageId, args);
            }
        });
    }

    /**
     * Method invoked when Add Photo button (R.id.image_product_config_add_photo)
     * on the Product Image is clicked.
     */
    @Override
    public void openProductImages() {
        //Creating the ArrayList if not initialized
        if (mProductImages == null) {
            mProductImages = new ArrayList<>();
        }

        //Delegating to the Navigator to launch the ProductImageActivity
        mProductConfigNavigator.launchProductImagesView(mProductImages);
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

            if (requestCode == ProductImageActivity.REQUEST_PRODUCT_IMAGE) {
                //Request returned from ProductImageActivity

                if (data != null && data.hasExtra(ProductImageActivity.EXTRA_PRODUCT_IMAGES)) {
                    //When we have the new list of ProductImages, update the same to the View
                    ArrayList<ProductImage> productImages = data.getParcelableArrayListExtra(ProductImageActivity.EXTRA_PRODUCT_IMAGES);
                    updateProductImages(productImages);

                    //Set the boolean to True to indicate that we received
                    //Product Images from ProductImageActivity
                    mIsExtraProductImagesReceived = true;
                }
            }
        }
    }

    /**
     * Method invoked only when we have received Product Images from ProductImageActivity.
     * This is called when the View resumes, to force save the new
     * Product Images received for an Existing Product only.
     */
    private void onExtraProductImagesReceived() {
        //If this was received for an Existing Product, update it immediately
        //without the user's command
        if (mProductId != ProductConfigContract.NEW_PRODUCT_INT) {
            int noOfExistingProductImages = mExistingProduct.getProductImages().size();
            int noOfProductImagesReceived = mProductImages.size();

            if (!(noOfExistingProductImages == 0 && noOfProductImagesReceived == noOfExistingProductImages)) {
                //Updating the Images to the Database via the Repository when both are not 0
                //This check avoids unnecessary updates when both are 0.

                //Saving Product Images via the repository
                mStoreRepository.saveProductImages(mExistingProduct, mProductImages, new DataRepository.DataOperationsCallback() {
                    /**
                     * Method invoked when the database operations like insert/update/delete
                     * was successful.
                     */
                    @Override
                    public void onSuccess() {
                        //Show the Update success message
                        mProductConfigView.showUpdateImagesSuccess();
                    }

                    /**
                     * Method invoked when the database operations like insert/update/delete
                     * failed to complete.
                     *
                     * @param messageId The String resource of the error message
                     *                  for the database operation failure
                     * @param args      Variable number of arguments to replace the format specifiers
                     */
                    @Override
                    public void onFailure(int messageId, @Nullable Object... args) {
                        //Show the Failure message
                        mProductConfigView.showError(messageId, args);
                    }
                });
            }

        }
    }

    /**
     * Method invoked when the user clicks on the android home/up button
     * or the back key is pressed
     */
    @Override
    public void onUpOrBackAction() {
        if (mIsProductNameEntered) {
            //When the User has entered the Product Name, then we have some unsaved changes

            //Show the discard dialog to see if the user wants to keep editing/discard the changes
            mProductConfigView.showDiscardDialog();
        } else {
            //When the User has not yet entered the Product Name, then silently close the Activity
            finishActivity();
        }
    }

    /**
     * Method invoked when the user decides to exit without entering/saving any data
     */
    @Override
    public void finishActivity() {
        //For New Product Entry: Delete any uncommitted Image files if present
        deleteUncommittedImages();
        //Return back to the calling activity
        doCancel();
    }

    /**
     * Method invoked when the user clicks on the Delete Menu Action to delete the Product.
     * This should launch a Dialog for the user to reconfirm the request before proceeding
     * with the Delete Action.
     */
    @Override
    public void showDeleteProductDialog() {
        //Delegating to the View to show the dialog
        mProductConfigView.showDeleteProductDialog();
    }

    /**
     * Method that deletes Image files that are not committed
     * in case of a New Product Entry being discarded.
     */
    private void deleteUncommittedImages() {
        if (mProductId == ProductConfigContract.NEW_PRODUCT_INT && mProductImages != null && mProductImages.size() > 0) {
            //When this a New Product Entry and we have some unsaved images
            //that needs to be deleted when this Product Entry is being discarded

            //For building the list of Image File URIs to be deleted
            ArrayList<String> fileContentUriList = new ArrayList<>();

            //Reading the Product Images list and adding it to the list of URIs to be deleted
            for (ProductImage productImage : mProductImages) {
                fileContentUriList.add(productImage.getImageUri());
            }

            //Executing the delete operation silently on the URIs via the repository
            mStoreRepository.deleteImageFilesSilently(fileContentUriList);
        }
    }

    /**
     * Method invoked when the user clicks on the Delete Action Menu item.
     * This deletes the Product from the database along with its relationship data.
     * Applicable for an Existing Product. This option is not available for a New Product Entry.
     */
    @Override
    public void deleteProduct() {
        //Display the Progress Indicator
        mProductConfigView.showProgressIndicator(R.string.product_config_status_deleting);

        //For building the list of Image File URIs to be deleted
        ArrayList<String> fileContentUriList = new ArrayList<>();

        //Reading the Product Images list and adding it to the list of URIs to be deleted
        for (ProductImage productImage : mProductImages) {
            fileContentUriList.add(productImage.getImageUri());
        }

        //Executing Product Deletion via the Repository
        mStoreRepository.deleteProductById(mProductId, new DataRepository.DataOperationsCallback() {
            /**
             * Method invoked when the database operations like insert/update/delete
             * was successful.
             */
            @Override
            public void onSuccess() {
                //Hide Progress Indicator
                mProductConfigView.hideProgressIndicator();

                //Deleting Image files if any
                if (fileContentUriList.size() > 0) {
                    //Executing the delete operation silently on the URIs via the repository
                    mStoreRepository.deleteImageFilesSilently(fileContentUriList);
                }

                //Set the result and finish on successful delete
                doSetResult(ProductConfigActivity.RESULT_DELETE_PRODUCT, mExistingProduct.getId(), mExistingProduct.getSku());
            }

            /**
             * Method invoked when the database operations like insert/update/delete
             * failed to complete.
             *
             * @param messageId The String resource of the error message
             *                  for the database operation failure
             * @param args Variable number of arguments to replace the format specifiers
             *             in the String resource if any
             */
            @Override
            public void onFailure(int messageId, @Nullable Object... args) {
                //Hide Progress Indicator
                mProductConfigView.hideProgressIndicator();

                //Show the error message
                mProductConfigView.showError(messageId, args);
            }
        });
    }

    /**
     * Method invoked before save operation or screen orientation change to persist
     * any data held by the view that had focus and its listener registered.
     * This clears the focus held by the view to trigger the listener, causing to persist any unsaved data.
     */
    @Override
    public void triggerFocusLost() {
        mProductConfigView.triggerFocusLost();
    }

    /**
     * Method that updates the result {@code resultCode} to be sent back to the Calling Activity
     *
     * @param resultCode The integer result code to be returned to the Calling Activity.
     * @param productId  Integer containing the Id of the Product involved.
     * @param productSku String containing the SKU information of the Product involved.
     */
    @Override
    public void doSetResult(int resultCode, int productId, @NonNull String productSku) {
        mProductConfigNavigator.doSetResult(resultCode, productId, productSku);
    }

    /**
     * Method that updates the Calling Activity that the operation was aborted.
     */
    @Override
    public void doCancel() {
        mProductConfigNavigator.doCancel();
    }

}