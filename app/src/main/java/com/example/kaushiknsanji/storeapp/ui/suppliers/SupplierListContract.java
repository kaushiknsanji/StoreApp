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

package com.example.kaushiknsanji.storeapp.ui.suppliers;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.example.kaushiknsanji.storeapp.data.local.models.SupplierLite;
import com.example.kaushiknsanji.storeapp.ui.PagerPresenter;
import com.example.kaushiknsanji.storeapp.ui.PagerView;

import java.util.ArrayList;

/**
 * Contract Interface for the View {@link SupplierListFragment} and its Presenter {@link SupplierListPresenter}
 *
 * @author Kaushik N Sanji
 */
public interface SupplierListContract {

    /**
     * View Interface implemented by {@link SupplierListFragment}
     */
    interface View extends PagerView<Presenter> {

        /**
         * Method that displays the Progress indicator
         */
        void showProgressIndicator();

        /**
         * Method that hides the Progress indicator
         */
        void hideProgressIndicator();

        /**
         * Method invoked when an error is encountered during Supplier information
         * retrieval or delete process.
         *
         * @param messageId String Resource of the error Message to be displayed
         * @param args      Variable number of arguments to replace the format specifiers
         *                  in the String resource if any
         */
        void showError(@StringRes int messageId, @Nullable Object... args);

        /**
         * Method invoked when the Supplier List is empty. This should show a TextView with a
         * Text that suggests Users to add Suppliers into the database.
         */
        void showEmptyView();

        /**
         * Method invoked when we have the Supplier List. This should show the Supplier List and
         * hide the Empty List TextView and Step Number Drawable.
         */
        void hideEmptyView();

        /**
         * Method that updates the RecyclerView's Adapter with new {@code supplierList} data.
         *
         * @param supplierList List of Suppliers defined by {@link SupplierLite}
         *                     loaded from the database.
         */
        void loadSuppliers(ArrayList<SupplierLite> supplierList);

        /**
         * Method invoked when the user clicks on the FAB button to add a New Supplier
         * into the database. This should
         * launch the {@link com.example.kaushiknsanji.storeapp.ui.suppliers.config.SupplierConfigActivity}
         * for configuring a New Supplier.
         */
        void launchAddNewSupplier();

        /**
         * Method invoked when the user clicks on "Edit" button or the Item View itself. This should
         * launch the {@link com.example.kaushiknsanji.storeapp.ui.suppliers.config.SupplierConfigActivity}
         * for the Supplier to be edited.
         *
         * @param supplierId The Primary key of the Supplier to be edited.
         */
        void launchEditSupplier(int supplierId);

        /**
         * Method that displays a message on Success of adding a New Supplier.
         *
         * @param supplierCode String containing the code of the Supplier that was added successfully.
         */
        void showAddSuccess(String supplierCode);

        /**
         * Method that displays a message on Success of updating an Existing Supplier.
         *
         * @param supplierCode String containing the code of the Supplier that was updated successfully.
         */
        void showUpdateSuccess(String supplierCode);

        /**
         * Method that displays a message on Success of Deleting an Existing Supplier.
         *
         * @param supplierCode String containing the code of the Supplier that was deleted successfully.
         */
        void showDeleteSuccess(String supplierCode);

        /**
         * Method invoked when the user clicks on the Phone button. This should launch the Phone
         * dialer passing in the phone number {@code phoneNumber}
         *
         * @param phoneNumber The Phone Number to dial.
         */
        void dialPhoneNumber(String phoneNumber);

        /**
         * Method invoked when the user clicks on the Email button. This should launch an Email
         * activity passing in the "TO" Address {@code toEmailAddress}
         *
         * @param toEmailAddress The "TO" Address to send an email to.
         */
        void composeEmail(String toEmailAddress);

    }

    /**
     * Presenter Interface implemented by {@link SupplierListPresenter}
     */
    interface Presenter extends PagerPresenter {

        /**
         * Method that triggers the CursorLoader to load the Suppliers from the database.
         *
         * @param forceLoad Boolean value that controls the nature of the trigger
         *                  <br/><b>TRUE</b> to forcefully start a new load process
         *                  <br/><b>FALSE</b> to start a new/existing load process
         */
        void triggerSuppliersLoad(boolean forceLoad);

        /**
         * Method invoked when the user clicks on "Edit" button or the Item View itself. This should
         * launch the {@link com.example.kaushiknsanji.storeapp.ui.suppliers.config.SupplierConfigActivity}
         * for the Supplier to be edited.
         *
         * @param supplierId The Primary key of the Supplier to be edited.
         */
        void editSupplier(int supplierId);

        /**
         * Method invoked when the user clicks on "Delete" button. This should delete the Supplier
         * identified by {@link SupplierLite#mId}, from the database.
         *
         * @param supplier The {@link SupplierLite} data of the Supplier to be deleted.
         */
        void deleteSupplier(SupplierLite supplier);

        /**
         * Method invoked when the user clicks on the FAB button to add a New Supplier
         * into the database.
         */
        void addNewSupplier();

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
         * Method invoked when the View is about to be destroyed.
         * This method should release any critical resources held by the Presenter.
         */
        void releaseResources();

        /**
         * Method invoked when the user clicks on the Phone button. This should launch the Phone
         * dialer passing in the phone number {@code phoneNumber}
         *
         * @param phoneNumber The Phone Number to dial.
         */
        void defaultPhoneClicked(String phoneNumber);

        /**
         * Method invoked when the user clicks on the Email button. This should launch an Email
         * activity passing in the "TO" Address {@code toEmailAddress}
         *
         * @param toEmailAddress The "TO" Address to send an email to.
         */
        void defaultEmailClicked(String toEmailAddress);
    }
}
