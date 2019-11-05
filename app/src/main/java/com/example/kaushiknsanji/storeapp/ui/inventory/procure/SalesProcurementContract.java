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

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.example.kaushiknsanji.storeapp.data.local.models.SupplierContact;
import com.example.kaushiknsanji.storeapp.ui.BasePresenter;
import com.example.kaushiknsanji.storeapp.ui.BaseView;

import java.util.ArrayList;
import java.util.List;

/**
 * Contract Interface for the View {@link SalesProcurementActivityFragment} and its Presenter {@link SalesProcurementPresenter}.
 *
 * @author Kaushik N Sanji
 */
public interface SalesProcurementContract {

    /**
     * The View Interface implemented by {@link SalesProcurementActivityFragment}
     */
    interface View extends BaseView<Presenter> {

        /**
         * Method invoked to keep the state of "Supplier's Contacts restored", in sync with the Presenter.
         *
         * @param areSupplierContactsRestored Boolean that indicates the state of Supplier's Contacts restored.
         *                                    <b>TRUE</b> if it had been restored; <b>FALSE</b> otherwise.
         */
        void syncContactsState(boolean areSupplierContactsRestored);

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
         * Method invoked when an error is encountered during Product/Suppliers information
         * retrieval or save process.
         *
         * @param messageId String Resource of the error Message to be displayed
         * @param args      Variable number of arguments to replace the format specifiers
         *                  in the String resource if any
         */
        void showError(@StringRes int messageId, @Nullable Object... args);

        /**
         * Method that updates the Supplier's Phone Contacts {@code phoneContacts} to the View
         *
         * @param phoneContacts List of {@link SupplierContact} of Phone Contact Type, of the Supplier
         */
        void updatePhoneContacts(ArrayList<SupplierContact> phoneContacts);

        /**
         * Method invoked to hide the "Procure via Phone Call" View when there are No Phone Contacts to show
         */
        void hidePhoneContacts();

        /**
         * Method that updates the Supplier's Email Contacts {@code emailContacts} to the View
         *
         * @param emailContacts List of {@link SupplierContact} of Email Contact Type, of the Supplier
         */
        void updateEmailContacts(ArrayList<SupplierContact> emailContacts);

        /**
         * Method invoked to hide the "Procure via Email" View when there are No Email Contacts available
         */
        void hideEmailContacts();

        /**
         * Method invoked to display a TextView with a message when there are No contacts available
         * for procuring the Product from the Supplier
         */
        void showEmptyContactsView();

        /**
         * Method that updates the Product Supplier Title to the View.
         *
         * @param titleResId   The String resource of the Title to be used
         * @param productName  The Name of the Product being procured
         * @param productSku   The SKU of the Product
         * @param supplierName The Name of the Supplier being procured from
         * @param supplierCode The Code of the Supplier
         */
        void updateProductSupplierTitle(@StringRes int titleResId, String productName, String productSku, String supplierName, String supplierCode);

        /**
         * Method that updates the quantity available to Sell at the Supplier to the View
         *
         * @param availableQuantity Integer value of the available quantity of the Product at the Supplier
         */
        void updateProductSupplierAvailability(int availableQuantity);

        /**
         * Method invoked to show the "Out Of Stock!" alert when the quantity available to Sell
         * for the Product at the Supplier is 0.
         */
        void showOutOfStockAlert();

        /**
         * Method invoked when the user clicks on any of the Phone Contacts shown. This should launch the Phone
         * dialer passing in the phone number {@code phoneNumber}
         *
         * @param phoneNumber The Phone Number to dial.
         */
        void dialPhoneNumber(String phoneNumber);

        /**
         * Method invoked when the user clicks on the "Send Mail" Image Button. This should launch the Email
         * Activity passing in the data for Email address, subject and body of the Email.
         *
         * @param toEmailAddress The "TO" Address to send the email to.
         * @param ccAddresses    Array of "CC" Addresses to include.
         * @param subjectResId   The String resource for the Subject of the Email.
         * @param subjectArgs    The Arguments of the Subject String Resource.
         * @param bodyResId      The String resource for the Body of the Email.
         * @param bodyArgs       The Arguments of the Body String Resource.
         */
        void composeEmail(String toEmailAddress, String[] ccAddresses, @StringRes int subjectResId,
                          Object[] subjectArgs, @StringRes int bodyResId, Object[] bodyArgs);
    }

    /**
     * The Presenter Interface implemented by {@link SalesProcurementPresenter}
     */
    interface Presenter extends BasePresenter {

        /**
         * Method that updates the state of "Supplier's Contacts restored", and keeps it in sync with the View.
         *
         * @param areSupplierContactsRestored Boolean that indicates the state of Supplier's Contacts restored.
         *                                    <b>TRUE</b> if it had been restored; <b>FALSE</b> otherwise.
         */
        void updateAndSyncContactsState(boolean areSupplierContactsRestored);

        /**
         * Method that updates the List of Supplier Contacts {@code supplierContacts} to the View.
         *
         * @param supplierContacts The List of {@link SupplierContact} of the Supplier.
         */
        void updateSupplierContacts(List<SupplierContact> supplierContacts);

        /**
         * Method invoked when the user clicks on any of the Phone Contacts shown. This should
         * launch an Intent to start the Phone Activity passing in
         * the number {@link SupplierContact#mValue}
         *
         * @param supplierContact The {@link SupplierContact} data of the Phone Contact clicked.
         */
        void phoneClicked(SupplierContact supplierContact);

        /**
         * Method invoked when the user clicks on the "Send Mail" Image Button. This should
         * dispatch an Email to the Supplier's Contacts for procuring more quantity of the Product.
         *
         * @param requiredQuantityStr String containing the quantity of Product required.
         * @param emailContacts       List of {@link SupplierContact} of Email Contact Type, of the Supplier.
         */
        void sendMailClicked(String requiredQuantityStr, ArrayList<SupplierContact> emailContacts);

        /**
         * Method that finishes the current activity and returns back to the calling activity
         */
        void doFinish();

    }

}
