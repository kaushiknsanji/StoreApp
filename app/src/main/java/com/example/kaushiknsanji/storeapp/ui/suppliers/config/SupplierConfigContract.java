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

package com.example.kaushiknsanji.storeapp.ui.suppliers.config;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.SparseArray;

import com.example.kaushiknsanji.storeapp.data.local.models.ProductLite;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductSupplierInfo;
import com.example.kaushiknsanji.storeapp.data.local.models.SupplierContact;
import com.example.kaushiknsanji.storeapp.ui.BasePresenter;
import com.example.kaushiknsanji.storeapp.ui.BaseView;

import java.util.ArrayList;

/**
 * Contract Interface for the View {@link SupplierConfigActivityFragment} and its Presenter {@link SupplierConfigPresenter}.
 *
 * @author Kaushik N Sanji
 */
public interface SupplierConfigContract {

    //Integer Constant used as the Supplier ID for New Supplier Configuration
    int NEW_SUPPLIER_INT = -1;

    /**
     * The View Interface implemented by {@link SupplierConfigActivityFragment}
     */
    interface View extends BaseView<Presenter> {

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
         * Method invoked when an error is encountered during Supplier information
         * retrieval or save process.
         *
         * @param messageId String Resource of the error Message to be displayed
         * @param args      Variable number of arguments to replace the format specifiers
         *                  in the String resource if any
         */
        void showError(@StringRes int messageId, @Nullable Object... args);

        /**
         * Method that locks the Supplier Code field to prevent updates on this field.
         */
        void lockSupplierCodeField();

        /**
         * Method invoked to keep the state of "Existing Supplier details restored", in sync with the Presenter.
         *
         * @param isExistingSupplierRestored Boolean that indicates the state of Existing Supplier data restored.
         *                                   <b>TRUE</b> if it had been restored; <b>FALSE</b> otherwise.
         */
        void syncExistingSupplierState(boolean isExistingSupplierRestored);

        /**
         * Method invoked to keep the state of "Supplier Code Validity", in sync with the Presenter.
         *
         * @param isSupplierCodeValid Boolean that indicates whether the Supplier Code entered was valid or not.
         *                            <b>TRUE</b> if the Supplier Code is valid; <b>FALSE</b> otherwise.
         */
        void syncSupplierCodeValidity(boolean isSupplierCodeValid);

        /**
         * Method invoked to keep the state of "Supplier Name entered", in sync with the Presenter.
         * This is used for monitoring unsaved progress.
         *
         * @param isSupplierNameEntered Boolean that indicates whether the Supplier Name has been entered by the User or not.
         *                              <b>TRUE</b> if the Supplier Name is entered; <b>FALSE</b> otherwise.
         */
        void syncSupplierNameEnteredState(boolean isSupplierNameEntered);

        /**
         * Method invoked when the Supplier Code entered by the user is NOT Unique
         * causing the conflict.
         */
        void showSupplierCodeConflictError();

        /**
         * Method that updates the Supplier Name {@code supplierName} to the View
         *
         * @param supplierName The Name of the Supplier
         */
        void updateSupplierNameField(String supplierName);

        /**
         * Method that updates the Supplier Code {@code supplierCode} to the View
         *
         * @param supplierCode The Code of the Supplier
         */
        void updateSupplierCodeField(String supplierCode);

        /**
         * Method that updates the Supplier's Phone Contacts {@code phoneContacts} to the View
         *
         * @param phoneContacts List of {@link SupplierContact} of Phone Contact Type, of the Supplier
         */
        void updatePhoneContacts(ArrayList<SupplierContact> phoneContacts);

        /**
         * Method that updates the Supplier's Email Contacts {@code emailContacts} to the View
         *
         * @param emailContacts List of {@link SupplierContact} of Email Contact Type, of the Supplier
         */
        void updateEmailContacts(ArrayList<SupplierContact> emailContacts);

        /**
         * Method that updates the List of Products {@link ProductLite} sold
         * by the Supplier with Price information {@link ProductSupplierInfo}, to the View.
         *
         * @param productSupplierInfoList The List of {@link ProductSupplierInfo} which contains the
         *                                Products with Price details that is sold by the Supplier.
         * @param productLiteSparseArray  {@link SparseArray} of {@link ProductLite} which contains the
         *                                data of the Products sold by the Supplier.
         */
        void updateSupplierProducts(ArrayList<ProductSupplierInfo> productSupplierInfoList,
                                    @Nullable SparseArray<ProductLite> productLiteSparseArray);

        /**
         * Method invoked when No Supplier Code was entered by the user.
         */
        void showSupplierCodeEmptyError();

        /**
         * Method invoked before save operation or screen orientation change to persist
         * any data held by the view that had focus and its listener registered.
         * This clears the focus held by the view to trigger the listener, causing to persist any unsaved data.
         */
        void triggerFocusLost();

        /**
         * Method invoked when required fields are missing data, on click of 'Save' Menu button.
         */
        void showEmptyFieldsValidationError();

        /**
         * Method invoked when more than one {@link SupplierContact} is found to have
         * the same Contact value {@code contactValue}.
         *
         * @param conflictMessageResId The String resource of conflict error message to be shown.
         * @param contactValue         The Contact Value which is repeated causing the conflict.
         */
        void showSupplierContactConflictError(@StringRes int conflictMessageResId,
                                              String contactValue);

        /**
         * Method invoked when there is no contact information configured for the Supplier.
         */
        void showEmptyContactsError();

        /**
         * Method invoked by the Presenter to display the Discard dialog,
         * requesting the User whether to keep editing/discard the changes
         */
        void showDiscardDialog();

        /**
         * Method invoked when the user clicks on the Delete Menu Action to delete the Supplier.
         * This should launch a Dialog for the user to reconfirm the request before proceeding
         * with the Delete Action.
         */
        void showDeleteSupplierDialog();

        /**
         * Method invoked when the user swipes left/right any Item View of the Products sold by the Supplier
         * in order to remove it from the list. This should show a Snackbar with Action UNDO.
         *
         * @param productSku The Product SKU of the Product being swiped out.
         */
        void showSupplierProductSwiped(String productSku);

        /**
         * Method that displays a message on Success of Updating an Existing Product.
         *
         * @param productSku String containing the SKU of the Product that was updated successfully.
         */
        void showUpdateSuccess(String productSku);

        /**
         * Method that displays a message on Success of Deleting an Existing Product
         *
         * @param productSku String containing the SKU of the Product that was deleted successfully.
         */
        void showDeleteSuccess(String productSku);

        /**
         * Method invoked when one of the Supplier's Products were edited successfully and
         * returned through Activity result. This method should notify the Adapter to rebind
         * the data for the product with Id {@code productId}
         *
         * @param productId The Integer Id of the Product whose data needs to be rebound.
         */
        void notifyProductChanged(int productId);

        /**
         * Method invoked during save operation, when a {@link SupplierContact}
         * value is found to be still invalid.
         *
         * @param invalidMessageResId The String resource of the error message to be shown.
         */
        void showSupplierContactsInvalidError(@StringRes int invalidMessageResId);
    }

    /**
     * The Presenter Interface implemented by {@link SupplierConfigPresenter}
     */
    interface Presenter extends BasePresenter {

        /**
         * Method that updates the state of "Existing Supplier details restored", and keeps it in sync with the View.
         *
         * @param isExistingSupplierRestored Boolean that indicates the state of Existing Supplier data restored.
         *                                   <b>TRUE</b> if it had been restored; <b>FALSE</b> otherwise.
         */
        void updateAndSyncExistingSupplierState(boolean isExistingSupplierRestored);

        /**
         * Method that updates the state of "Supplier Code Validity", and keeps it in sync with the View.
         * This method also informs the User to change the value if it is invalid.
         *
         * @param isSupplierCodeValid Boolean that indicates whether the Supplier Code entered was valid or not.
         *                            <b>TRUE</b> if the Supplier Code is valid; <b>FALSE</b> otherwise.
         */
        void updateAndSyncSupplierCodeValidity(boolean isSupplierCodeValid);

        /**
         * Method that updates the state of "Supplier Name entered", and keeps it in sync with the View.
         * This is used for monitoring unsaved progress.
         *
         * @param isSupplierNameEntered Boolean that indicates whether the Supplier Name has been entered by the User or not.
         *                              <b>TRUE</b> if the Supplier Name is entered; <b>FALSE</b> otherwise.
         */
        void updateAndSyncSupplierNameEnteredState(boolean isSupplierNameEntered);

        /**
         * Method that updates the Supplier Name {@code supplierName} to the View
         *
         * @param supplierName The Name of the Supplier
         */
        void updateSupplierNameField(String supplierName);

        /**
         * Method that updates the Supplier Code {@code supplierCode} to the View
         *
         * @param supplierCode The Code of the Supplier
         */
        void updateSupplierCodeField(String supplierCode);

        /**
         * Method that updates the List of Supplier Contacts {@code supplierContacts} to the View.
         *
         * @param supplierContacts The List of {@link SupplierContact} of the Supplier.
         */
        void updateSupplierContacts(ArrayList<SupplierContact> supplierContacts);

        /**
         * Method that updates the List of Products {@link ProductLite} sold
         * by the Supplier with Price information {@link ProductSupplierInfo}, to the View.
         *
         * @param productSupplierInfoList The List of {@link ProductSupplierInfo} which contains the
         *                                Products with Price details that is sold by the Supplier.
         * @param productLiteSparseArray  {@link SparseArray} of {@link ProductLite} which contains the
         *                                data of the Products sold by the Supplier. When {@code null},
         *                                this data will be downloaded and rebuilt.
         */
        void updateSupplierProducts(ArrayList<ProductSupplierInfo> productSupplierInfoList,
                                    @Nullable SparseArray<ProductLite> productLiteSparseArray);

        /**
         * Method invoked when the Supplier Code entered by the user needs to be validated for its uniqueness.
         *
         * @param supplierCode The Supplier Code entered by the user.
         */
        void validateSupplierCode(String supplierCode);

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
         * Method invoked before save operation or screen orientation change to persist
         * any data held by the view that had focus and its listener registered.
         * This clears the focus held by the view to trigger the listener, causing to persist any unsaved data.
         */
        void triggerFocusLost();

        /**
         * Method invoked when the 'Save' Menu button is clicked.
         *
         * @param supplierName            The Name given to the Supplier
         * @param supplierCode            The Code of the Supplier
         * @param phoneContacts           List of Phone Contacts {@link SupplierContact} of the Supplier
         * @param emailContacts           List of Email Contacts {@link SupplierContact} of the Supplier
         * @param productSupplierInfoList List of Products sold by the Supplier with their Selling Price {@link ProductSupplierInfo}
         */
        void onSave(String supplierName,
                    String supplierCode,
                    ArrayList<SupplierContact> phoneContacts,
                    ArrayList<SupplierContact> emailContacts,
                    ArrayList<ProductSupplierInfo> productSupplierInfoList);

        /**
         * Method invoked when the user clicks on the Delete Menu Action to delete the Supplier.
         * This should launch a Dialog for the user to reconfirm the request before proceeding
         * with the Delete Action.
         */
        void showDeleteSupplierDialog();

        /**
         * Method invoked when the user clicks on the "Delete" Menu button.
         * This deletes the Supplier from the database along with its relationship data.
         * Applicable for an Existing Supplier. This Menu option is not available for a New Supplier Entry.
         */
        void deleteSupplier();

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
         * Method that updates the result {@code resultCode} to be sent back to the Calling Activity
         *
         * @param resultCode   The integer result code to be returned to the Calling Activity.
         * @param supplierId   Integer containing the Id of the Supplier involved.
         * @param supplierCode String containing the Supplier Code of the Supplier involved.
         */
        void doSetResult(final int resultCode, final int supplierId, @NonNull final String supplierCode);

        /**
         * Method that updates the Calling Activity that the operation was aborted.
         */
        void doCancel();

        /**
         * Method invoked when the user clicks on any Item View of the Products sold by the Supplier. This should
         * launch the {@link com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigActivity}
         * for the Product to be edited.
         *
         * @param productId             The Primary Key of the Product to be edited.
         * @param activityOptionsCompat Instance of {@link ActivityOptionsCompat} that has the
         *                              details for Shared Element Transition
         */
        void editProduct(int productId, ActivityOptionsCompat activityOptionsCompat);

        /**
         * Method invoked when the user swipes left/right any Item View of the Products sold by the Supplier
         * in order to remove it from the list. This should show a Snackbar with Action UNDO.
         *
         * @param productSku The Product SKU of the Product being swiped out.
         */
        void onSupplierProductSwiped(String productSku);

        /**
         * Method invoked when the user clicks on the "Add Item" button, present under "Supplier Items"
         * to add/link items to the Supplier. This should launch the
         * {@link com.example.kaushiknsanji.storeapp.ui.suppliers.product.SupplierProductPickerActivity}
         * to pick the Products for the Supplier to sell.
         *
         * @param productLiteList ArrayList of Products {@link ProductLite} already picked for the Supplier to sell.
         */
        void pickProducts(ArrayList<ProductLite> productLiteList);
    }

}
