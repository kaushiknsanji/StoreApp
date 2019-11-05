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

package com.example.kaushiknsanji.storeapp.ui.inventory.procure;

import android.support.annotation.NonNull;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.DataRepository;
import com.example.kaushiknsanji.storeapp.data.StoreRepository;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SupplierContract;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductSupplierSales;
import com.example.kaushiknsanji.storeapp.data.local.models.SupplierContact;
import com.example.kaushiknsanji.storeapp.ui.BaseView;
import com.example.kaushiknsanji.storeapp.ui.products.config.DefaultPhotoChangeListener;

import java.util.ArrayList;
import java.util.List;

/**
 * The Presenter class that implements {@link SalesProcurementContract.Presenter} on the lines of
 * Model-View-Presenter architecture. This Presenter interfaces with the App repository {@link StoreRepository}
 * to download the availability of the Product at the Supplier and Supplier's Contacts for procurement
 * and updates the same to the View {@link SalesProcurementActivityFragment} to load and display it.
 */
public class SalesProcurementPresenter implements SalesProcurementContract.Presenter {

    //Constant used for Logs
    private static final String LOG_TAG = SalesProcurementPresenter.class.getSimpleName();

    //The View Interface of this Presenter
    @NonNull
    private final SalesProcurementContract.View mSalesProcurementView;

    //Instance of the App Repository
    @NonNull
    private final StoreRepository mStoreRepository;

    //Navigator that receives callbacks when navigating away from the Current Activity
    private final SalesProcurementNavigator mSalesProcurementNavigator;

    //Listener that receives callbacks when there is a change in the default photo of the Product
    private final DefaultPhotoChangeListener mDefaultPhotoChangeListener;

    //The Name of the Product
    private String mProductName;

    //The SKU of the Product
    private String mProductSku;

    //The defaulted ProductImage of the Product.
    private ProductImage mProductImage;

    //The Sales details of the Product
    private ProductSupplierSales mProductSupplierSales;

    //Stores the state of Supplier Contacts restored,
    //to prevent updating the fields every time during System config change
    private boolean mAreSupplierContactsRestored;

    /**
     * Constructor of {@link SalesProcurementPresenter}
     *
     * @param storeRepository            Instance of {@link StoreRepository} for accessing the data
     * @param salesProcurementView       The View instance {@link SalesProcurementContract.View} of this Presenter.
     * @param productName                The Name of the Product to procure.
     * @param productSku                 The SKU of the Product to procure.
     * @param productImage               The defaulted {@link ProductImage} of the Product.
     * @param productSupplierSales       The Sales details {@link ProductSupplierSales} of the Product.
     * @param salesProcurementNavigator  Instance of {@link SalesProcurementNavigator} that receives
     *                                   callbacks when navigating away from the Current Activity
     * @param defaultPhotoChangeListener Instance of {@link DefaultPhotoChangeListener} that receives
     *                                   callbacks when there is a change in the default photo of the Product
     */
    SalesProcurementPresenter(@NonNull StoreRepository storeRepository,
                              @NonNull SalesProcurementContract.View salesProcurementView,
                              String productName, String productSku,
                              ProductImage productImage,
                              ProductSupplierSales productSupplierSales,
                              SalesProcurementNavigator salesProcurementNavigator,
                              @NonNull DefaultPhotoChangeListener defaultPhotoChangeListener) {
        mStoreRepository = storeRepository;
        mSalesProcurementView = salesProcurementView;
        mProductName = productName;
        mProductSku = productSku;
        mProductImage = productImage;
        mProductSupplierSales = productSupplierSales;
        mSalesProcurementNavigator = salesProcurementNavigator;
        mDefaultPhotoChangeListener = defaultPhotoChangeListener;

        //Registering the View with the Presenter
        mSalesProcurementView.setPresenter(this);
    }


    /**
     * Method that initiates the work of a Presenter which is invoked by the View
     * that implements the {@link BaseView}
     */
    @Override
    public void start() {

        //Update the Product Supplier Title
        updateProductSupplierTitle();

        //Update the Product Supplier Availability
        updateProductSupplierAvailability();

        //Update the Product Image
        updateProductImage();

        //Download the Supplier Contacts for the Supplier
        loadSupplierContacts();

    }

    /**
     * Method that updates the state of "Supplier's Contacts restored", and keeps it in sync with the View.
     *
     * @param areSupplierContactsRestored Boolean that indicates the state of Supplier's Contacts restored.
     *                                    <b>TRUE</b> if it had been restored; <b>FALSE</b> otherwise.
     */
    @Override
    public void updateAndSyncContactsState(boolean areSupplierContactsRestored) {
        //Saving the state
        mAreSupplierContactsRestored = areSupplierContactsRestored;

        //Syncing the state to the View
        mSalesProcurementView.syncContactsState(mAreSupplierContactsRestored);
    }

    /**
     * Method that downloads the Supplier's Contacts to update the view components with the
     * contact information
     */
    private void loadSupplierContacts() {
        if (!mAreSupplierContactsRestored) {
            //When Supplier's Contacts was not previously downloaded

            //Display progress indicator
            mSalesProcurementView.showProgressIndicator(R.string.sales_procurement_status_loading_contacts);

            //Retrieving the Supplier's Contacts from the Repository
            mStoreRepository.getSupplierContactsById(mProductSupplierSales.getSupplierId(), new DataRepository.GetQueryCallback<List<SupplierContact>>() {
                /**
                 * Method invoked when the results are obtained
                 * for the query executed.
                 *
                 * @param supplierContacts The Supplier's Contact List loaded for the Supplier Id passed
                 */
                @Override
                public void onResults(List<SupplierContact> supplierContacts) {
                    //Update the Supplier Contacts
                    updateSupplierContacts(supplierContacts);

                    //Marking as downloaded/restored
                    updateAndSyncContactsState(true);

                    //Hide progress indicator
                    mSalesProcurementView.hideProgressIndicator();
                }

                /**
                 * Method invoked when there are no results
                 * for the query executed.
                 */
                @Override
                public void onEmpty() {
                    //When we do not have any contacts, show the view with the message for NO Contacts available
                    mSalesProcurementView.hidePhoneContacts();
                    mSalesProcurementView.hideEmailContacts();
                    mSalesProcurementView.showEmptyContactsView();

                    //Hide progress indicator
                    mSalesProcurementView.hideProgressIndicator();
                }
            });
        }
    }

    /**
     * Method that updates the List of Supplier Contacts {@code supplierContacts} to the View.
     *
     * @param supplierContacts The List of {@link SupplierContact} of the Supplier.
     */
    @Override
    public void updateSupplierContacts(List<SupplierContact> supplierContacts) {
        //Supplier Contacts are of more than one type (Phone/Email).
        //So we need to iterate, make a separate list and update the same to the View

        //Stores a list of Phone Contacts
        ArrayList<SupplierContact> phoneContacts = new ArrayList<>();

        //Stores a list of Email Contacts
        ArrayList<SupplierContact> emailContacts = new ArrayList<>();

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

        if (phoneContacts.size() > 0 || emailContacts.size() > 0) {
            //When we have any of the contacts

            if (phoneContacts.size() > 0) {
                //When we have phone contacts, update the same to the view
                mSalesProcurementView.updatePhoneContacts(phoneContacts);
            } else {
                //When we do not have any phone contacts, hide the corresponding View
                mSalesProcurementView.hidePhoneContacts();
            }

            if (emailContacts.size() > 0) {
                //When we have email contacts, update the same to the view
                mSalesProcurementView.updateEmailContacts(emailContacts);
            } else {
                //When we do not have any email contacts, hide the corresponding View
                mSalesProcurementView.hideEmailContacts();
            }

        } else {
            //When we do not have any contacts, show the view with the message for NO Contacts available
            mSalesProcurementView.hidePhoneContacts();
            mSalesProcurementView.hideEmailContacts();
            mSalesProcurementView.showEmptyContactsView();
        }
    }

    /**
     * Method that updates the Product Supplier Title to the View
     */
    private void updateProductSupplierTitle() {
        mSalesProcurementView.updateProductSupplierTitle(R.string.sales_procurement_title_product_supplier,
                mProductName, mProductSku, mProductSupplierSales.getSupplierName(), mProductSupplierSales.getSupplierCode());
    }

    /**
     * Method that updates the Product Supplier's Availability to the View
     */
    private void updateProductSupplierAvailability() {
        int supplierAvailableQuantity = mProductSupplierSales.getAvailableQuantity();

        if (supplierAvailableQuantity > 0) {
            //When there is quantity available to sell at the Supplier

            //Show the total to the View
            mSalesProcurementView.updateProductSupplierAvailability(supplierAvailableQuantity);

        } else {
            //When there is NO quantity available to sell

            //Show the Out of Stock Alert to the View
            mSalesProcurementView.showOutOfStockAlert();
        }

    }

    /**
     * Method that updates the Product Image to be shown, to the View
     */
    private void updateProductImage() {
        if (mProductImage != null) {
            //Delegating to the listener to show the selected Product Image by passing the String URI of the Image to the View
            mDefaultPhotoChangeListener.showSelectedProductImage(mProductImage.getImageUri());
        } else {
            //When there is no Image for the Product, delegate to the listener to show the default image instead
            mDefaultPhotoChangeListener.showDefaultImage();
        }
    }

    /**
     * Method invoked when the user clicks on any of the Phone Contacts shown. This should
     * launch an Intent to start the Phone Activity passing in
     * the number {@link SupplierContact#mValue}
     *
     * @param supplierContact The {@link SupplierContact} data of the Phone Contact clicked.
     */
    @Override
    public void phoneClicked(SupplierContact supplierContact) {
        //Delegating to the View to launch the Phone Dialer
        mSalesProcurementView.dialPhoneNumber(supplierContact.getValue());
    }

    /**
     * Method invoked when the user clicks on the "Send Mail" Image Button. This should
     * dispatch an Email to the Supplier's Contacts for procuring more quantity of the Product.
     *
     * @param requiredQuantityStr String containing the quantity of Product required.
     * @param emailContacts       List of {@link SupplierContact} of Email Contact Type, of the Supplier.
     */
    @Override
    public void sendMailClicked(String requiredQuantityStr, ArrayList<SupplierContact> emailContacts) {
        //Stores the TO Email Address which is the defaulted contact
        String toEmailAddress = "";
        //Stores the CC Email Addresses which are not defaulted contacts
        ArrayList<String> ccAddressList = new ArrayList<>();

        //Iterating over the Email Addresses to find the TO and CC Addresses
        for (SupplierContact emailContact : emailContacts) {
            if (emailContact.isDefault()) {
                //Assigning to TO Email Address when it is a defaulted Email Contact
                toEmailAddress = emailContact.getValue();
            } else {
                //Adding to the CC Address List when it is NOT a defaulted Email Contact
                ccAddressList.add(emailContact.getValue());
            }
        }

        //Preparing the Subject Arguments
        String[] subjectArgs = new String[3];
        subjectArgs[0] = requiredQuantityStr;
        subjectArgs[1] = mProductName;
        subjectArgs[2] = mProductSku;

        //Preparing the Body Arguments
        String[] bodyArgs = new String[6];
        bodyArgs[0] = mProductSupplierSales.getSupplierName();
        bodyArgs[1] = mProductSupplierSales.getSupplierCode();
        bodyArgs[2] = String.valueOf(mProductSupplierSales.getAvailableQuantity());
        bodyArgs[3] = mProductName;
        bodyArgs[4] = mProductSku;
        bodyArgs[5] = requiredQuantityStr;

        //Delegating to the View, to Compose and Launch a template Email for procurement
        mSalesProcurementView.composeEmail(toEmailAddress,
                ccAddressList.toArray(new String[0]),
                R.string.sales_procurement_email_subject,
                subjectArgs,
                R.string.sales_procurement_email_body,
                bodyArgs
        );
    }

    /**
     * Method that finishes the current activity and returns back to the calling activity
     */
    @Override
    public void doFinish() {
        //Delegating to the Navigator to finish the Activity
        mSalesProcurementNavigator.doFinish();
    }

}