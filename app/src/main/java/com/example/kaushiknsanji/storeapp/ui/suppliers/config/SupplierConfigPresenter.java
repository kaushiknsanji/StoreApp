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
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.SparseArray;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.DataRepository;
import com.example.kaushiknsanji.storeapp.data.StoreRepository;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SupplierContract;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductLite;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductSupplierInfo;
import com.example.kaushiknsanji.storeapp.data.local.models.Supplier;
import com.example.kaushiknsanji.storeapp.data.local.models.SupplierContact;
import com.example.kaushiknsanji.storeapp.ui.BaseView;
import com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigActivity;
import com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigContract;
import com.example.kaushiknsanji.storeapp.ui.suppliers.product.SupplierProductPickerActivity;
import com.example.kaushiknsanji.storeapp.utils.ContactUtility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The Presenter class that implements {@link SupplierConfigContract.Presenter} on the lines of
 * Model-View-Presenter architecture. This Presenter interfaces with the App repository {@link StoreRepository}
 * to create/modify the Supplier Entry in the database and updates the same to
 * the View {@link SupplierConfigActivityFragment} to load and display it.
 *
 * @author Kaushik N Sanji
 */
public class SupplierConfigPresenter implements SupplierConfigContract.Presenter {

    //Constant used for Logs
    private static final String LOG_TAG = SupplierConfigPresenter.class.getSimpleName();

    //The Supplier ID of an Existing Supplier, opened for editing/deleting the Supplier
    //Will be SupplierConfigContract#NEW_SUPPLIER_INT if the request is for a New Supplier Entry
    private final int mSupplierId;

    //The View Interface of this Presenter
    @NonNull
    private final SupplierConfigContract.View mSupplierConfigView;

    //Instance of the App Repository
    @NonNull
    private final StoreRepository mStoreRepository;

    //Navigator that receives callbacks when navigating away from the Current Activity
    private final SupplierConfigNavigator mSupplierConfigNavigator;

    //Stores Existing Supplier details if this is an Edit request
    private Supplier mExistingSupplier;

    //Stores the state of Existing Supplier details restored,
    //to prevent updating the fields every time during System config change
    private boolean mIsExistingSupplierRestored;

    //Stores whether the Supplier Code entered was valid or not
    private boolean mIsSupplierCodeValid;

    //Stores whether the Supplier Name was entered or not.
    //Used for monitoring unsaved progress.
    private boolean mIsSupplierNameEntered;

    //SparseArray of Products that stores the Product Details of Products
    private SparseArray<ProductLite> mProductLiteSparseArray;

    //List that stores the Price information of Products
    private ArrayList<ProductSupplierInfo> mProductSupplierInfoList;

    /**
     * Constructor of {@link SupplierConfigPresenter}
     *
     * @param supplierId              The integer value of the Supplier Id of an existing Supplier;
     *                                or {@link SupplierConfigContract#NEW_SUPPLIER_INT} if it is
     *                                for a New Supplier Entry.
     * @param storeRepository         Instance of {@link StoreRepository} for accessing/manipulating the data
     * @param supplierConfigView      The View instance {@link SupplierConfigContract.View} of this Presenter
     * @param supplierConfigNavigator Instance of {@link SupplierConfigNavigator} that receives callbacks
     *                                when navigating away from the Current Activity
     */
    SupplierConfigPresenter(int supplierId,
                            @NonNull StoreRepository storeRepository,
                            @NonNull SupplierConfigContract.View supplierConfigView,
                            @NonNull SupplierConfigNavigator supplierConfigNavigator) {
        mSupplierId = supplierId;
        mStoreRepository = storeRepository;
        mSupplierConfigView = supplierConfigView;
        mSupplierConfigNavigator = supplierConfigNavigator;

        //Registering the View with this Presenter
        mSupplierConfigView.setPresenter(this);
    }

    /**
     * Method that initiates the work of a Presenter which is invoked by the View
     * that implements the {@link BaseView}
     */
    @Override
    public void start() {
        //Load existing Supplier details if this is an Edit request
        loadExistingSupplier();
    }

    /**
     * Method that downloads the Existing Supplier details when this is an Edit request,
     * to update the view components with Supplier data.
     */
    private void loadExistingSupplier() {
        if (mSupplierId != SupplierConfigContract.NEW_SUPPLIER_INT) {
            //When it is an Edit request

            //Display progress indicator
            mSupplierConfigView.showProgressIndicator(R.string.supplier_config_status_loading_existing_supplier);

            //Retrieving the Existing Supplier details from the Repository
            mStoreRepository.getSupplierDetailsById(mSupplierId, new DataRepository.GetQueryCallback<Supplier>() {
                /**
                 * Method invoked when the results are obtained
                 * for the query executed.
                 *
                 * @param supplier The Supplier details loaded for the Supplier Id passed.
                 */
                @Override
                public void onResults(Supplier supplier) {
                    //Update the Supplier details to the View
                    if (!mIsExistingSupplierRestored) {
                        //When Supplier details have not been loaded into the views yet

                        //Update the Supplier Name field
                        updateSupplierNameField(supplier.getName());

                        //Update the Supplier Code field
                        updateSupplierCodeField(supplier.getCode());

                        //Update the Supplier Contacts
                        updateSupplierContacts(supplier.getContacts());

                        //Update the Supplier Products
                        updateSupplierProducts(supplier.getProductSupplierInfoList(), null);

                        //Updating and syncing the state of existing Supplier data restore
                        updateAndSyncExistingSupplierState(true);

                        //Updating and syncing the state of Supplier Code Validity,
                        //This is always true since this is an Existing Supplier Data
                        updateAndSyncSupplierCodeValidity(true);

                        //Updating and syncing the state of Supplier Name Entered,
                        //This is always true since this is an Existing Supplier Data
                        updateAndSyncSupplierNameEnteredState(true);
                    }

                    //Lock the Supplier Code field from being edited, as this is an Existing Supplier
                    mSupplierConfigView.lockSupplierCodeField();

                    //Saving the Existing Supplier details
                    mExistingSupplier = supplier;

                    //Hide progress indicator
                    mSupplierConfigView.hideProgressIndicator();
                }

                /**
                 * Method invoked when the results could not be retrieved
                 * for the query due to some error.
                 *
                 * @param messageId The String resource of the error message
                 *                  for the query execution failure
                 * @param args      Variable number of arguments to replace the format specifiers
                 */
                @Override
                public void onFailure(int messageId, @Nullable Object... args) {
                    //Hide progress indicator
                    mSupplierConfigView.hideProgressIndicator();

                    //Show the error message
                    mSupplierConfigView.showError(messageId, args);
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
    }

    /**
     * Method that updates the Supplier Name {@code supplierName} to the View
     *
     * @param supplierName The Name of the Supplier
     */
    @Override
    public void updateSupplierNameField(String supplierName) {
        mSupplierConfigView.updateSupplierNameField(supplierName);
    }

    /**
     * Method that updates the Supplier Code {@code supplierCode} to the View
     *
     * @param supplierCode The Code of the Supplier
     */
    @Override
    public void updateSupplierCodeField(String supplierCode) {
        mSupplierConfigView.updateSupplierCodeField(supplierCode);
    }

    /**
     * Method that updates the List of Supplier Contacts {@code supplierContacts} to the View.
     *
     * @param supplierContacts The List of {@link SupplierContact} of the Supplier.
     */
    @Override
    public void updateSupplierContacts(ArrayList<SupplierContact> supplierContacts) {
        //Supplier Contacts are of more than one type (Phone/Email).
        //So we need to iterate, make a separate list and update the same to the View

        //Stores a list of Phone Contacts
        ArrayList<SupplierContact> phoneContacts = new ArrayList<>();

        //Stores a list of Email Contacts
        ArrayList<SupplierContact> emailContacts = new ArrayList<>();

        if (supplierContacts != null && supplierContacts.size() > 0) {
            //When we have SupplierContacts, iterate and update the lists
            for (SupplierContact supplierContact : supplierContacts) {
                //Updating the lists with the SupplierContact based on their type
                switch (supplierContact.getType()) {
                    case SupplierContract.SupplierContactType.CONTACT_TYPE_PHONE:
                        //For Phone Contacts
                        phoneContacts.add((SupplierContact) supplierContact.clone());
                        break;
                    case SupplierContract.SupplierContactType.CONTACT_TYPE_EMAIL:
                        //For Email Contacts
                        emailContacts.add((SupplierContact) supplierContact.clone());
                        break;
                }
            }
        }

        //Updating Phone Contacts to the View
        mSupplierConfigView.updatePhoneContacts(phoneContacts);

        //Updating Email Contacts to the View
        mSupplierConfigView.updateEmailContacts(emailContacts);
    }

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
    @Override
    public void updateSupplierProducts(ArrayList<ProductSupplierInfo> productSupplierInfoList,
                                       @Nullable SparseArray<ProductLite> productLiteSparseArray) {
        if (productSupplierInfoList != null && productSupplierInfoList.size() > 0) {
            //When we have the Supplier's Products List

            //Make a Deep Copy of the new Supplier's Products List
            mProductSupplierInfoList = new ArrayList<>();
            for (ProductSupplierInfo productSupplierInfo : productSupplierInfoList) {
                mProductSupplierInfoList.add((ProductSupplierInfo) productSupplierInfo.clone());
            }

            if (productLiteSparseArray == null) {
                //When we do NOT have the details of the Products

                //Initializing the SparseArray of ProductLite objects
                if (mProductLiteSparseArray == null) {
                    mProductLiteSparseArray = new SparseArray<>();
                } else {
                    mProductLiteSparseArray.clear();
                }

                //Stores the list of Products' Ids
                ArrayList<String> productIds = new ArrayList<>();

                //Iterating over the list to get the Product Ids
                for (ProductSupplierInfo productSupplierInfo : mProductSupplierInfoList) {
                    productIds.add(String.valueOf(productSupplierInfo.getItemId()));
                }

                //Retrieving the Product information for the Product Ids via the Repository
                mStoreRepository.getShortProductInfoForProducts(productIds, new DataRepository.GetQueryCallback<List<ProductLite>>() {
                    /**
                     * Method invoked when the results are obtained
                     * for the query executed.
                     *
                     * @param products The List of Products {@link ProductLite} data for the product Ids passed.
                     */
                    @Override
                    public void onResults(List<ProductLite> products) {
                        //Iterating over the list to prepare the Map (SparseArray) of Product's Id with its data
                        for (ProductLite product : products) {
                            mProductLiteSparseArray.put(product.getId(), product);
                        }

                        //Updating to the View with the data downloaded
                        mSupplierConfigView.updateSupplierProducts(mProductSupplierInfoList, mProductLiteSparseArray);
                    }

                    /**
                     * Method invoked when there are no results
                     * for the query executed.
                     */
                    @Override
                    public void onEmpty() {
                        //Updating to the View with an empty ProductLiteSparseArray
                        mSupplierConfigView.updateSupplierProducts(mProductSupplierInfoList, mProductLiteSparseArray);
                    }
                });

            } else {
                //When we have the details of the Products

                //Replace the SparseArray of Products' details
                mProductLiteSparseArray = productLiteSparseArray;
                //Updating to the View with the same data passed
                mSupplierConfigView.updateSupplierProducts(mProductSupplierInfoList, mProductLiteSparseArray);
            }

        } else {
            //When we do NOT have the Supplier's Products List

            //Reset the list of ProductSupplierInfo objects
            if (mProductSupplierInfoList == null) {
                mProductSupplierInfoList = new ArrayList<>();
            } else {
                mProductSupplierInfoList.clear();
            }

            //Updating to the View without the SparseArray of Products' details
            mSupplierConfigView.updateSupplierProducts(mProductSupplierInfoList, null);
        }
    }

    /**
     * Method that updates the List of Products {@link ProductLite} sold by the Supplier
     * with Price information {@link ProductSupplierInfo}, to the View.
     *
     * @param productList The Updated List of Products {@link ProductLite} that includes a new list
     *                    of Products picked for selling.
     */
    private void updateSupplierProducts(ArrayList<ProductLite> productList) {
        if (productList != null && productList.size() > 0) {
            //When we have Products' details

            //Initializing a list of ProductSupplierInfo objects
            //when previously not initialized
            if (mProductSupplierInfoList == null) {
                mProductSupplierInfoList = new ArrayList<>();
            }

            //Initializing a SparseArray of Product Ids with ProductSupplierInfo objects
            SparseArray<ProductSupplierInfo> productSupplierInfoSparseArray = new SparseArray<>();

            //Building the SparseArray of Product Ids for the registered list of Products
            for (ProductSupplierInfo productSupplierInfo : mProductSupplierInfoList) {
                productSupplierInfoSparseArray.put(productSupplierInfo.getItemId(), productSupplierInfo);
            }

            //Initializing a SparseArray of Product Ids with Product details
            //when previously not initialized
            if (mProductLiteSparseArray == null) {
                mProductLiteSparseArray = new SparseArray<>();
            }

            //Iterating over the list of Products passed, to rebuild the current list and SparseArray
            //for including the new products just picked for selling
            for (ProductLite product : productList) {
                if (productSupplierInfoSparseArray.get(product.getId()) == null) {
                    //When the Product is a new Product selected for selling

                    //Create and Add a ProductSupplierInfo into the list
                    ProductSupplierInfo newProductSupplierInfo = new ProductSupplierInfo.Builder()
                            .setItemId(product.getId())
                            .setSupplierId(mSupplierId)
                            .createProductSupplierInfo();
                    mProductSupplierInfoList.add(newProductSupplierInfo);

                    //Add the SparseArray entry for the Product Id with its details
                    mProductLiteSparseArray.put(product.getId(), product);
                }
            }

            //Updating to the View with the updated list of Products and its details
            mSupplierConfigView.updateSupplierProducts(mProductSupplierInfoList, mProductLiteSparseArray);
        }
    }

    /**
     * Method invoked when the Supplier Code entered by the user needs to be validated for its uniqueness.
     *
     * @param supplierCode The Supplier Code entered by the user.
     */
    @Override
    public void validateSupplierCode(String supplierCode) {
        if (TextUtils.isEmpty(supplierCode)) {
            //When the Supplier Code is empty, show the empty error and bail out
            mSupplierConfigView.showSupplierCodeEmptyError();
            return;
        }

        //Checking the Uniqueness of the Supplier Code with the Repository
        mStoreRepository.getSupplierCodeUniqueness(supplierCode, new DataRepository.GetQueryCallback<Boolean>() {
            /**
             * Method invoked when the results are obtained
             * for the query executed.
             *
             * @param results The query results in the generic type passed.
             *                <b>TRUE</b> if the {@code supplierCode} is Unique;
             *                <b>FALSE</b> otherwise.
             */
            @Override
            public void onResults(Boolean results) {
                //Update and Sync the Supplier Code validity based on the "results" boolean
                updateAndSyncSupplierCodeValidity(results);
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
     * Invoked from a previous call to
     * {@link FragmentActivity#startActivityForResult(Intent, int)}.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode > FragmentActivity.RESULT_FIRST_USER) {
            //When we have a success result from an Activity

            if (requestCode == SupplierProductPickerActivity.REQUEST_SUPPLIER_PRODUCTS
                    && resultCode == SupplierProductPickerActivity.RESULT_SUPPLIER_PRODUCTS) {
                //When the result is from the SupplierProductPickerActivity for Supplier's Products

                if (data != null && data.hasExtra(SupplierProductPickerActivity.EXTRA_SUPPLIER_PRODUCTS)) {
                    //When we have the new updated list of Products picked for selling, update the same to the View
                    ArrayList<ProductLite> productList = data.getParcelableArrayListExtra(SupplierProductPickerActivity.EXTRA_SUPPLIER_PRODUCTS);
                    updateSupplierProducts(productList);
                }

            } else if (requestCode == ProductConfigActivity.REQUEST_EDIT_PRODUCT) {
                //For an Edit Product request
                if (resultCode == ProductConfigActivity.RESULT_EDIT_PRODUCT) {
                    //When the result is for the Edit Product action

                    //Retrieving the Product ID and its SKU from the activity result
                    String productSku = data.getStringExtra(ProductConfigActivity.EXTRA_RESULT_PRODUCT_SKU);
                    int productId = data.getIntExtra(ProductConfigActivity.EXTRA_RESULT_PRODUCT_ID, ProductConfigContract.NEW_PRODUCT_INT);

                    if (productId != ProductConfigContract.NEW_PRODUCT_INT) {
                        //This should be for an Edit request always, but still doing the check

                        //Stores the list of Products' Ids
                        ArrayList<String> productIds = new ArrayList<>();

                        //Adding the Product Id received, to the list
                        productIds.add(String.valueOf(productId));

                        //Retrieving the Product information for the Product Ids via the Repository
                        mStoreRepository.getShortProductInfoForProducts(productIds, new DataRepository.GetQueryCallback<List<ProductLite>>() {
                            /**
                             * Method invoked when the results are obtained
                             * for the query executed.
                             *
                             * @param products The List of Products {@link ProductLite} data for the product Ids passed.
                             */
                            @Override
                            public void onResults(List<ProductLite> products) {
                                //Iterating over the list to prepare the Map (SparseArray) of Product's Id with its data
                                for (ProductLite product : products) {
                                    mProductLiteSparseArray.put(product.getId(), product);
                                }

                                //Updating to the View with the data downloaded
                                mSupplierConfigView.updateSupplierProducts(mProductSupplierInfoList, mProductLiteSparseArray);

                                //Notify product data changed, to the View, in order to
                                //rebind the product data for the product updated
                                for (ProductLite product : products) {
                                    mSupplierConfigView.notifyProductChanged(product.getId());
                                }

                                //Show the Update Success message
                                mSupplierConfigView.showUpdateSuccess(productSku);
                            }

                            /**
                             * Method invoked when there are no results
                             * for the query executed.
                             */
                            @Override
                            public void onEmpty() {
                                //No-op, as this case will not occur
                            }
                        });
                    }

                } else if (resultCode == ProductConfigActivity.RESULT_DELETE_PRODUCT) {
                    //When the result is for the Delete Product action

                    //Retrieving the Product ID and its SKU from the activity result
                    String productSku = data.getStringExtra(ProductConfigActivity.EXTRA_RESULT_PRODUCT_SKU);
                    int productId = data.getIntExtra(ProductConfigActivity.EXTRA_RESULT_PRODUCT_ID, ProductConfigContract.NEW_PRODUCT_INT);

                    if (productId != ProductConfigContract.NEW_PRODUCT_INT) {
                        //This should be for an Edit request always, but still doing the check

                        //Removing the Product data from the Supplier Product list and the SparseArray data
                        mProductLiteSparseArray.remove(productId);
                        //Retrieving the iterator to Supplier Product list
                        Iterator<ProductSupplierInfo> productSupplierInfoIterator = mProductSupplierInfoList.iterator();
                        while (productSupplierInfoIterator.hasNext()) {
                            //Reading the next ProductSupplierInfo
                            ProductSupplierInfo productSupplierInfo = productSupplierInfoIterator.next();
                            if (productSupplierInfo.getItemId() == productId) {
                                //When the Product Id matches, delete the ProductSupplierInfo and bail out
                                productSupplierInfoIterator.remove();
                                break;
                            }
                        }

                        //Updating to the View with the data changed
                        mSupplierConfigView.updateSupplierProducts(mProductSupplierInfoList, mProductLiteSparseArray);

                        //Show the Delete Success message
                        mSupplierConfigView.showDeleteSuccess(productSku);
                    }

                }
            }

        }
    }

    /**
     * Method invoked before save operation or screen orientation change to persist
     * any data held by the view that had focus and its listener registered.
     * This clears the focus held by the view to trigger the listener, causing to persist any unsaved data.
     */
    @Override
    public void triggerFocusLost() {
        mSupplierConfigView.triggerFocusLost();
    }

    /**
     * Method invoked when the 'Save' Menu button is clicked.
     *
     * @param supplierName            The Name given to the Supplier
     * @param supplierCode            The Code of the Supplier
     * @param phoneContacts           List of Phone Contacts {@link SupplierContact} of the Supplier
     * @param emailContacts           List of Email Contacts {@link SupplierContact} of the Supplier
     * @param productSupplierInfoList List of Products sold by the Supplier with their Selling Price {@link ProductSupplierInfo}
     */
    @Override
    public void onSave(String supplierName,
                       String supplierCode,
                       ArrayList<SupplierContact> phoneContacts,
                       ArrayList<SupplierContact> emailContacts,
                       ArrayList<ProductSupplierInfo> productSupplierInfoList) {

        //Display save progress indicator
        mSupplierConfigView.showProgressIndicator(R.string.supplier_config_status_saving);

        //Supplier Code Empty/Uniqueness validation
        if (!mIsExistingSupplierRestored && !mIsSupplierCodeValid) {
            //If the Supplier Code is still invalid or empty at the time of clicking Save button
            //(Not applicable when Existing Supplier is being edited, since the Supplier Code is locked)

            //Hide Progress Indicator
            mSupplierConfigView.hideProgressIndicator();

            if (TextUtils.isEmpty(supplierCode)) {
                //When Supplier Code is empty, show the empty error
                mSupplierConfigView.showSupplierCodeEmptyError();
            } else {
                //When Supplier Code is present, show the invalid error
                mSupplierConfigView.showSupplierCodeConflictError();
            }
            return; //Exiting
        }

        //Empty Fields validation
        if (TextUtils.isEmpty(supplierName) || TextUtils.isEmpty(supplierCode)) {
            //When required data is missing, report the same to the user and bail out
            //Hide Progress Indicator
            mSupplierConfigView.hideProgressIndicator();
            mSupplierConfigView.showEmptyFieldsValidationError();
            return; //Exiting
        }

        //Phone Contacts Uniqueness validation and empty record removal
        if (!validateSupplierContactList(phoneContacts, R.string.supplier_config_phone_contact_conflict_error)) {
            //Bailing out when a conflicting contact is found
            return;
        }

        //Email Contacts Uniqueness validation and empty record removal
        if (!validateSupplierContactList(emailContacts, R.string.supplier_config_email_contact_conflict_error)) {
            //Bailing out when a conflicting contact is found
            return;
        }

        //Phone Number values validation
        if (!validateSupplierContactValues(SupplierContract.SupplierContactType.CONTACT_TYPE_PHONE,
                phoneContacts, R.string.supplier_config_phone_contacts_invalid_error)) {
            //Bailing out when an invalid Phone Number is found
            return;
        }

        //Email Contact values validation
        if (!validateSupplierContactValues(SupplierContract.SupplierContactType.CONTACT_TYPE_EMAIL,
                emailContacts, R.string.supplier_config_email_contacts_invalid_error)) {
            //Bailing out when an invalid Email is found
            return;
        }

        //Number of Contacts validation. One Contact (Email or Phone) should always be present
        if (phoneContacts.size() + emailContacts.size() == 0) {
            //When the number of contacts is less than 1, show the error message and bail out
            //Hide Progress Indicator
            mSupplierConfigView.hideProgressIndicator();
            mSupplierConfigView.showEmptyContactsError();
            return;
        }

        //No Validation is required for ProductSupplierInfo list since
        //duplication is prevented by showing only the remaining Products
        //to pick in the SupplierProductPickerActivity

        //Create the New/Updated Supplier
        Supplier newSupplier = createSupplierForUpdate(supplierName, supplierCode,
                phoneContacts, emailContacts, productSupplierInfoList);

        //Start saving to the Database
        if (mSupplierId > SupplierConfigContract.NEW_SUPPLIER_INT) {
            //For Existing Supplier
            saveUpdatedSupplier(mExistingSupplier, newSupplier);
        } else {
            //For New Supplier
            saveNewSupplier(newSupplier);
        }

    }

    /**
     * Method invoked when the user clicks on the Delete Menu Action to delete the Supplier.
     * This should launch a Dialog for the user to reconfirm the request before proceeding
     * with the Delete Action.
     */
    @Override
    public void showDeleteSupplierDialog() {
        //Delegating to the View to show the dialog
        mSupplierConfigView.showDeleteSupplierDialog();
    }

    /**
     * Method that checks the Contact Values in the list of {@link SupplierContact} passed,
     * against a pattern that identifies Phone Number or Email. If any contact does not match the
     * criteria for Phone/Email, an error message {@code invalidMessageResId} will be shown.
     *
     * @param contactType         The Contact Type of the Contact, which is Phone/Email.
     * @param supplierContacts    The List of {@link SupplierContact} to be validated.
     * @param invalidMessageResId The String resource of the Error message to be shown.
     * @return Returns <b>TRUE</b> when every record is valid; <b>FALSE</b> when an invalid contact is found.
     */
    private boolean validateSupplierContactValues(String contactType,
                                                  ArrayList<SupplierContact> supplierContacts,
                                                  @StringRes int invalidMessageResId) {
        //Iterating over the list of Contacts to check their validity
        for (SupplierContact supplierContact : supplierContacts) {
            if (contactType.equals(SupplierContract.SupplierContactType.CONTACT_TYPE_PHONE)) {
                //For "Phone" Contact Type, checking the validity of the Phone Number
                if (!ContactUtility.isValidPhoneNumber(supplierContact.getValue())) {
                    //When Phone Number is invalid

                    //Hide Progress Indicator
                    mSupplierConfigView.hideProgressIndicator();
                    mSupplierConfigView.showSupplierContactsInvalidError(invalidMessageResId);
                    //Returning False to indicate the error
                    return false;
                }
            } else if (contactType.equals(SupplierContract.SupplierContactType.CONTACT_TYPE_EMAIL)) {
                //For "Email" Contact Type, checking the validity of the Email
                if (!ContactUtility.isValidEmail(supplierContact.getValue())) {
                    //When Email is invalid

                    //Hide Progress Indicator
                    mSupplierConfigView.hideProgressIndicator();
                    mSupplierConfigView.showSupplierContactsInvalidError(invalidMessageResId);
                    //Returning False to indicate the error
                    return false;
                }
            }
        }
        //Returning True when everything is valid
        return true;
    }

    /**
     * Method that checks the Contact Values in the list of {@link SupplierContact} passed, for any
     * duplicates and empty records. If any empty record is found, the record will be silently
     * removed from the list and when a duplicate value is found, error message {@code conflictMessageResId}
     * will be shown.
     *
     * @param supplierContacts     The List of {@link SupplierContact} to be validated.
     * @param conflictMessageResId The String resource of the Error message to be shown.
     * @return Returns <b>TRUE</b> when every record is valid; <b>FALSE</b> when a conflict is found.
     */
    private boolean validateSupplierContactList(ArrayList<SupplierContact> supplierContacts,
                                                @StringRes int conflictMessageResId) {
        //Stores a list of Contact Values to check for any duplicates
        ArrayList<String> supplierContactValues = new ArrayList<>();
        //Retrieving the Iterator to Supplier Contacts
        Iterator<SupplierContact> supplierContactsIterator = supplierContacts.iterator();
        while (supplierContactsIterator.hasNext()) {
            //Retrieving the next Contact record
            SupplierContact contact = supplierContactsIterator.next();
            //Get the Contact Value
            String contactValue = contact.getValue();

            //Checking if the Contact Value is present
            if (TextUtils.isEmpty(contactValue)) {
                //Deleting the record when the value is found to be empty
                supplierContactsIterator.remove();
            } else {
                if (supplierContactValues.contains(contactValue)) {
                    //If the list already contains this Contact Value
                    //then report the same to the user and bail out
                    //Hide Progress Indicator
                    mSupplierConfigView.hideProgressIndicator();
                    mSupplierConfigView.showSupplierContactConflictError(conflictMessageResId, contactValue);
                    //Returning False to indicate the conflict
                    return false;
                } else {
                    //If the current Value is unique,
                    //then update the same to the list for scanning further duplicates
                    supplierContactValues.add(contactValue);
                }
            }
        }
        //Returning True when everything is valid
        return true;
    }

    /**
     * Method that creates and returns the {@link Supplier} for the details passed.
     *
     * @param supplierName            The Name given to the Supplier
     * @param supplierCode            The Code of the Supplier
     * @param phoneContacts           List of Phone Contacts {@link SupplierContact} of the Supplier
     * @param emailContacts           List of Email Contacts {@link SupplierContact} of the Supplier
     * @param productSupplierInfoList List of Products sold by the Supplier with their Selling Price {@link ProductSupplierInfo}
     * @return Instance of {@link Supplier} containing the details passed.
     */
    private Supplier createSupplierForUpdate(String supplierName,
                                             String supplierCode,
                                             ArrayList<SupplierContact> phoneContacts,
                                             ArrayList<SupplierContact> emailContacts,
                                             ArrayList<ProductSupplierInfo> productSupplierInfoList) {

        //Adding all emailContacts to phoneContacts since they belong to the same table
        phoneContacts.addAll(emailContacts);

        //Building and returning the Supplier built with the details
        return new Supplier.Builder()
                .setId(mSupplierId)
                .setName(supplierName)
                .setCode(supplierCode)
                .setContacts(phoneContacts)
                .setProductSupplierInfoList(productSupplierInfoList)
                .createSupplier();
    }

    /**
     * Method that saves the New Supplier details to the database.
     *
     * @param newSupplier The New Supplier details to be saved.
     */
    private void saveNewSupplier(Supplier newSupplier) {
        //Saving Supplier data via the Repository
        mStoreRepository.saveNewSupplier(newSupplier, new DataRepository.DataOperationsCallback() {
            /**
             * Method invoked when the database operations like insert/update/delete
             * was successful.
             */
            @Override
            public void onSuccess() {
                //Hide progress indicator
                mSupplierConfigView.hideProgressIndicator();

                //Set the result and finish on successful insert
                doSetResult(SupplierConfigActivity.RESULT_ADD_SUPPLIER, newSupplier.getId(), newSupplier.getCode());
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
                mSupplierConfigView.hideProgressIndicator();

                //Show message for Insert Failure
                mSupplierConfigView.showError(messageId, args);
            }
        });
    }

    /**
     * Method that saves the Updated Supplier details to the database.
     *
     * @param existingSupplier The Existing Supplier details for figuring out the required
     *                         CRUD operations.
     * @param newSupplier      The New Updated Supplier details to be saved.
     */
    private void saveUpdatedSupplier(Supplier existingSupplier, Supplier newSupplier) {
        //Updating Supplier data via the Repository
        mStoreRepository.saveUpdatedSupplier(existingSupplier, newSupplier, new DataRepository.DataOperationsCallback() {
            /**
             * Method invoked when the database operations like insert/update/delete
             * was successful.
             */
            @Override
            public void onSuccess() {
                //Hide progress indicator
                mSupplierConfigView.hideProgressIndicator();

                //Set the result and finish on successful update
                doSetResult(SupplierConfigActivity.RESULT_EDIT_SUPPLIER, newSupplier.getId(), newSupplier.getCode());
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
                mSupplierConfigView.hideProgressIndicator();

                //Show message for Update Failure
                mSupplierConfigView.showError(messageId, args);
            }
        });
    }

    /**
     * Method invoked when the user clicks on the "Delete" Menu button.
     * This deletes the Supplier from the database along with its relationship data.
     * Applicable for an Existing Supplier. This Menu option is not available for a New Supplier Entry.
     */
    @Override
    public void deleteSupplier() {
        //Display the Progress Indicator
        mSupplierConfigView.showProgressIndicator(R.string.supplier_config_status_deleting);

        //Executing Supplier Deletion via the Repository
        mStoreRepository.deleteSupplierById(mSupplierId, new DataRepository.DataOperationsCallback() {
            /**
             * Method invoked when the database operations like insert/update/delete
             * was successful.
             */
            @Override
            public void onSuccess() {
                //Hide Progress Indicator
                mSupplierConfigView.hideProgressIndicator();

                //Set the result and finish on successful delete
                doSetResult(SupplierConfigActivity.RESULT_DELETE_SUPPLIER, mExistingSupplier.getId(), mExistingSupplier.getCode());
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
                mSupplierConfigView.hideProgressIndicator();

                //Show the error message
                mSupplierConfigView.showError(messageId, args);
            }
        });
    }

    /**
     * Method invoked when the user clicks on the android home/up button
     * or the back key is pressed
     */
    @Override
    public void onUpOrBackAction() {
        if (mIsSupplierNameEntered) {
            //When the User has entered the Supplier Name, then we have some unsaved changes

            //Show the discard dialog to see if the user wants to keep editing/discard the changes
            mSupplierConfigView.showDiscardDialog();
        } else {
            //When the User has not yet entered the Supplier Name, then silently close the Activity
            finishActivity();
        }
    }

    /**
     * Method invoked when the user decides to exit without entering/saving any data
     */
    @Override
    public void finishActivity() {
        //Return back to the calling activity
        doCancel();
    }

    /**
     * Method that updates the result {@code resultCode} to be sent back to the Calling Activity
     *
     * @param resultCode   The integer result code to be returned to the Calling Activity.
     * @param supplierId   Integer containing the Id of the Supplier involved.
     * @param supplierCode String containing the Supplier Code of the Supplier involved.
     */
    @Override
    public void doSetResult(int resultCode, int supplierId, @NonNull String supplierCode) {
        //Delegating to the Navigator
        mSupplierConfigNavigator.doSetResult(resultCode, supplierId, supplierCode);
    }

    /**
     * Method that updates the Calling Activity that the operation was aborted.
     */
    @Override
    public void doCancel() {
        //Delegating to the Navigator
        mSupplierConfigNavigator.doCancel();
    }

    /**
     * Method invoked when the user clicks on any Item View of the Products sold by the Supplier. This should
     * launch the {@link com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigActivity}
     * for the Product to be edited.
     *
     * @param productId             The Primary Key of the Product to be edited.
     * @param activityOptionsCompat Instance of {@link ActivityOptionsCompat} that has the
     *                              details for Shared Element Transition
     */
    @Override
    public void editProduct(int productId, ActivityOptionsCompat activityOptionsCompat) {
        //Delegating to the Navigator to launch the Activity for editing the Product
        mSupplierConfigNavigator.launchEditProduct(productId, activityOptionsCompat);
    }

    /**
     * Method invoked when the user swipes left/right any Item View of the Products sold by the Supplier
     * in order to remove it from the list. This should show a Snackbar with Action UNDO.
     *
     * @param productSku The Product SKU of the Product being swiped out.
     */
    @Override
    public void onSupplierProductSwiped(String productSku) {
        mSupplierConfigView.showSupplierProductSwiped(productSku);
    }

    /**
     * Method invoked when the user clicks on the "Add Item" button, present under "Supplier Items"
     * to add/link items to the Supplier. This should launch the
     * {@link SupplierProductPickerActivity}
     * to pick the Products for the Supplier to sell.
     *
     * @param productLiteList ArrayList of Products {@link ProductLite} already picked for the Supplier to sell.
     */
    @Override
    public void pickProducts(ArrayList<ProductLite> productLiteList) {
        //Delegating to the Navigator to launch the Activity for picking Products
        mSupplierConfigNavigator.launchPickProducts(productLiteList);
    }

    /**
     * Method that updates the state of "Existing Supplier details restored", and keeps it in sync with the View.
     *
     * @param isExistingSupplierRestored Boolean that indicates the state of Existing Supplier data restored.
     *                                   <b>TRUE</b> if it had been restored; <b>FALSE</b> otherwise.
     */
    @Override
    public void updateAndSyncExistingSupplierState(boolean isExistingSupplierRestored) {
        //Saving the state
        mIsExistingSupplierRestored = isExistingSupplierRestored;

        //Syncing the state to the View
        mSupplierConfigView.syncExistingSupplierState(mIsExistingSupplierRestored);
    }

    /**
     * Method that updates the state of "Supplier Code Validity", and keeps it in sync with the View.
     * This method also informs the User to change the value if it is invalid.
     *
     * @param isSupplierCodeValid Boolean that indicates whether the Supplier Code entered was valid or not.
     *                            <b>TRUE</b> if the Supplier Code is valid; <b>FALSE</b> otherwise.
     */
    @Override
    public void updateAndSyncSupplierCodeValidity(boolean isSupplierCodeValid) {
        //Saving the state
        mIsSupplierCodeValid = isSupplierCodeValid;

        //Syncing the state to the View
        mSupplierConfigView.syncSupplierCodeValidity(mIsSupplierCodeValid);

        if (!mIsSupplierCodeValid) {
            //Informing the User to change the value if invalid
            mSupplierConfigView.showSupplierCodeConflictError();
        }
    }

    /**
     * Method that updates the state of "Supplier Name entered", and keeps it in sync with the View.
     * This is used for monitoring unsaved progress.
     *
     * @param isSupplierNameEntered Boolean that indicates whether the Supplier Name has been entered by the User or not.
     *                              <b>TRUE</b> if the Supplier Name is entered; <b>FALSE</b> otherwise.
     */
    @Override
    public void updateAndSyncSupplierNameEnteredState(boolean isSupplierNameEntered) {
        //Saving the state
        mIsSupplierNameEntered = isSupplierNameEntered;

        //Syncing the state to the View
        mSupplierConfigView.syncSupplierNameEnteredState(mIsSupplierNameEntered);
    }
}
