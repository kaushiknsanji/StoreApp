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

import com.example.kaushiknsanji.storeapp.data.local.models.ProductAttribute;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.ui.BasePresenter;
import com.example.kaushiknsanji.storeapp.ui.BaseView;

import java.util.ArrayList;
import java.util.List;

/**
 * Contract Interface for the View {@link ProductConfigActivityFragment} and its Presenter {@link ProductConfigPresenter}.
 *
 * @author Kaushik N Sanji
 */
public interface ProductConfigContract {

    //Integer Constant used as the Product ID for New Product Configuration
    int NEW_PRODUCT_INT = -1;
    //Constant for custom Category "Other" option
    String CATEGORY_OTHER = "Other";

    /**
     * View Interface implemented by {@link ProductConfigActivityFragment}
     */
    interface View extends BaseView<Presenter> {

        /**
         * Method that updates the categories list to the spinner for configuration
         *
         * @param categories The list of categories available
         */
        void updateCategories(List<String> categories);

        /**
         * Method that displays the EditText Field associated with the 'Other' Category
         * selected by the user.
         */
        void showCategoryOtherEditTextField();

        /**
         * Method that hides the EditText Field of 'Other' Category when a predefined category
         * is selected by the user.
         */
        void hideCategoryOtherEditTextField();

        /**
         * Method that clears the EditText Field associated with the 'Other' Category
         */
        void clearCategoryOtherEditTextField();

        /**
         * Method invoked when required fields are missing data, on click of 'Save' Menu button.
         */
        void showEmptyFieldsValidationError();

        /**
         * Method invoked when either fields that make the {@link ProductAttribute} is missing.
         */
        void showAttributesPartialValidationError();

        /**
         * Method invoked when the {@link ProductAttribute} identified by the Attribute Name {@code attributeName}
         * is already defined.
         *
         * @param attributeName The Attribute Name of the {@link ProductAttribute} that has been repeated.
         */
        void showAttributeNameConflictError(String attributeName);

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
         * Method invoked when an error is encountered during Product information
         * retrieval or save process.
         *
         * @param messageId String Resource of the error Message to be displayed
         * @param args      Variable number of arguments to replace the format specifiers
         *                  in the String resource if any
         */
        void showError(@StringRes int messageId, @Nullable Object... args);

        /**
         * Method invoked when the Product SKU entered by the user is NOT Unique
         * causing the conflict.
         */
        void showProductSkuConflictError();

        /**
         * Method invoked when NO Product SKU was entered by the user.
         */
        void showProductSkuEmptyError();

        /**
         * Method that updates the Product Name {@code name} to the View
         *
         * @param name The Name of the Product
         */
        void updateProductNameField(String name);

        /**
         * Method that updates the Product SKU {@code sku} to the View
         *
         * @param sku The SKU of the Product
         */
        void updateProductSkuField(String sku);

        /**
         * Method that locks the Product SKU field to prevent updates on this field
         */
        void lockProductSkuField();

        /**
         * Method that updates the Product Description {@code description} to the View
         *
         * @param description The description of the Product
         */
        void updateProductDescriptionField(String description);

        /**
         * Method that updates the List of Product Attributes {@code productAttributes} to the View
         *
         * @param productAttributes The List of {@link ProductAttribute} of a Product
         */
        void updateProductAttributes(ArrayList<ProductAttribute> productAttributes);

        /**
         * Method that updates the List of Product Images {@code productImages} to the View
         *
         * @param productImages The List of {@link ProductImage} of a Product.
         */
        void updateProductImages(ArrayList<ProductImage> productImages);

        /**
         * Method that updates the Product Category to the View.
         *
         * @param selectedCategory  The selected Category of the Product
         * @param categoryOtherText The Category OTHER EditText field value, in the case where the Product Category
         *                          was not available from the Predefined list of Categories.
         */
        void updateCategorySelection(String selectedCategory, @Nullable String categoryOtherText);

        /**
         * Method invoked to keep the state of "Existing Product details restored", in sync with the Presenter.
         *
         * @param isExistingProductRestored Boolean that indicates the state of Existing Product data restored.
         *                                  <b>TRUE</b> if it had been restored; <b>FALSE</b> otherwise.
         */
        void syncExistingProductState(boolean isExistingProductRestored);

        /**
         * Method invoked to keep the state of "Product SKU Validity", in sync with the Presenter.
         *
         * @param isProductSkuValid Boolean that indicates whether the Product SKU entered was valid or not.
         *                          <b>TRUE</b> if the Product SKU is valid; <b>FALSE</b> otherwise.
         */
        void syncProductSkuValidity(boolean isProductSkuValid);

        /**
         * Method invoked to keep the state of "Product Name entered", in sync with the Presenter.
         * This is used for monitoring unsaved progress.
         *
         * @param isProductNameEntered Boolean that indicates whether the Product Name has been entered by the User or not.
         *                             <b>TRUE</b> if the Product Name is entered; <b>FALSE</b> otherwise.
         */
        void syncProductNameEnteredState(boolean isProductNameEntered);

        /**
         * Method invoked by the Presenter to display the Discard dialog,
         * requesting the User whether to keep editing/discard the changes
         */
        void showDiscardDialog();

        /**
         * Method invoked when the user clicks on the Delete Menu Action to delete the Product.
         * This should launch a Dialog for the user to reconfirm the request before proceeding
         * with the Delete Action.
         */
        void showDeleteProductDialog();

        /**
         * Method invoked before save operation or screen orientation change to persist
         * any data held by the view that had focus and its listener registered.
         * This clears the focus held by the view to trigger the listener, causing to persist any unsaved data.
         */
        void triggerFocusLost();

        /**
         * Method invoked to display a message on successful update/erase of the {@link ProductImage}s list.
         * This method will be invoked when the ProductImages are changed for an Existing Product,
         * immediately after returning from
         * the {@link com.example.kaushiknsanji.storeapp.ui.products.image.ProductImageActivity}
         */
        void showUpdateImagesSuccess();
    }

    /**
     * Presenter Interface implemented by {@link ProductConfigPresenter}
     */
    interface Presenter extends BasePresenter {

        /**
         * Method that updates the state of "Existing Product details restored", and keeps it in sync with the View.
         *
         * @param isExistingProductRestored Boolean that indicates the state of Existing Product data restored.
         *                                  <b>TRUE</b> if it had been restored; <b>FALSE</b> otherwise.
         */
        void updateAndSyncExistingProductState(boolean isExistingProductRestored);

        /**
         * Method that updates the state of "Product SKU Validity", and keeps it in sync with the View.
         * This method also informs the User to change the value if it is invalid.
         *
         * @param isProductSkuValid Boolean that indicates whether the Product SKU entered was valid or not.
         *                          <b>TRUE</b> if the Product SKU is valid; <b>FALSE</b> otherwise.
         */
        void updateAndSyncProductSkuValidity(boolean isProductSkuValid);

        /**
         * Method that updates the state of "Product Name entered", and keeps it in sync with the View.
         * This is used for monitoring unsaved progress.
         *
         * @param isProductNameEntered Boolean that indicates whether the Product Name has been entered by the User or not.
         *                             <b>TRUE</b> if the Product Name is entered; <b>FALSE</b> otherwise.
         */
        void updateAndSyncProductNameEnteredState(boolean isProductNameEntered);

        /**
         * Method invoked when a Category is selected by the user
         *
         * @param categoryName The Name of the Product Category selected by the user
         */
        void onCategorySelected(String categoryName);

        /**
         * Method that updates the Product Name {@code name} to the View
         *
         * @param name The Name of the Product
         */
        void updateProductNameField(String name);

        /**
         * Method that updates the Product SKU {@code sku} to the View
         *
         * @param sku The SKU of the Product
         */
        void updateProductSkuField(String sku);

        /**
         * Method that updates the Product Description {@code description} to the View
         *
         * @param description The description of the Product
         */
        void updateProductDescriptionField(String description);

        /**
         * Method that updates the Product Category to the View.
         *
         * @param selectedCategory  The selected Category of the Product
         * @param categoryOtherText The Category OTHER EditText field value, in the case where the Product Category
         *                          was not available from the Predefined list of Categories.
         */
        void updateCategorySelection(String selectedCategory, @Nullable String categoryOtherText);

        /**
         * Method that updates the List of Product Attributes {@code productAttributes} to the View
         *
         * @param productAttributes The List of {@link ProductAttribute} of a Product
         */
        void updateProductAttributes(ArrayList<ProductAttribute> productAttributes);

        /**
         * Method that updates the List of Product Images {@code productImages} to the View
         * and also loads the Selected Product Image to be shown if present.
         *
         * @param productImages The List of {@link ProductImage} of a Product.
         */
        void updateProductImages(ArrayList<ProductImage> productImages);

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
        void onSave(String productName, String productSku, String productDescription,
                    String categorySelected, String categoryOtherText,
                    ArrayList<ProductAttribute> productAttributes);

        /**
         * Method invoked when the Product SKU entered by user needs to be validated for its uniqueness.
         *
         * @param productSku The SKU of the Product entered by the User
         */
        void validateProductSku(String productSku);

        /**
         * Method invoked when the Add Photo button (R.id.image_product_config_add_photo)
         * on the Product Image is clicked.
         */
        void openProductImages();

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
         * Method invoked when the user clicks on the android home/up button
         * or the back key is pressed
         */
        void onUpOrBackAction();

        /**
         * Method invoked when the user decides to exit without entering/saving any data
         */
        void finishActivity();

        /**
         * Method invoked when the user clicks on the Delete Menu Action to delete the Product.
         * This should launch a Dialog for the user to reconfirm the request before proceeding
         * with the Delete Action.
         */
        void showDeleteProductDialog();

        /**
         * Method invoked when the user clicks on the Delete Action Menu item.
         * This deletes the Product from the database along with its relationship data.
         * Applicable for an Existing Product. This option is not available for a New Product Entry.
         */
        void deleteProduct();

        /**
         * Method invoked before save operation or screen orientation change to persist
         * any data held by the view that had focus and its listener registered.
         * This clears the focus held by the view to trigger the listener, causing to persist any unsaved data.
         */
        void triggerFocusLost();

        /**
         * Method that updates the result {@code resultCode} to be sent back to the Calling Activity
         *
         * @param resultCode The integer result code to be returned to the Calling Activity.
         * @param productId  Integer containing the Id of the Product involved.
         * @param productSku String containing the SKU information of the Product involved.
         */
        void doSetResult(final int resultCode, final int productId, @NonNull final String productSku);

        /**
         * Method that updates the Calling Activity that the operation was aborted.
         */
        void doCancel();
    }
}
