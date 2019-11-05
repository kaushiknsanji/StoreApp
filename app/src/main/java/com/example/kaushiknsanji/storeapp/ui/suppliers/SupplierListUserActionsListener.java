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

import com.example.kaushiknsanji.storeapp.data.local.models.SupplierLite;

/**
 * Interface to be implemented by {@link SupplierListFragment}
 * to receive callback events for User actions on RecyclerView list of Suppliers
 *
 * @author Kaushik N Sanji
 */
public interface SupplierListUserActionsListener {

    /**
     * Callback Method of {@link SupplierListUserActionsListener} invoked when
     * the user clicks on "Edit" button or the Item View itself. This should
     * launch the {@link com.example.kaushiknsanji.storeapp.ui.suppliers.config.SupplierConfigActivity}
     * for the Supplier to be edited.
     *
     * @param itemPosition The adapter position of the Item View clicked
     * @param supplier     The {@link SupplierLite} associated with the Item View clicked.
     */
    void onEditSupplier(final int itemPosition, SupplierLite supplier);

    /**
     * Callback Method of {@link SupplierListUserActionsListener} invoked when
     * the user clicks on "Delete" button. This should delete the Supplier
     * identified by {@link SupplierLite#mId}, from the database.
     *
     * @param itemPosition The adapter position of the Item View clicked
     * @param supplier     The {@link SupplierLite} associated with the Item View clicked.
     */
    void onDeleteSupplier(final int itemPosition, SupplierLite supplier);

    /**
     * Callback Method of {@link SupplierListUserActionsListener} invoked when
     * the user clicks on the default Phone shown. This should launch an Intent
     * to start the Phone Activity passing in the number {@link SupplierLite#mDefaultPhone}.
     *
     * @param itemPosition The adapter position of the Item View clicked
     * @param supplier     The {@link SupplierLite} associated with the Item View clicked.
     */
    void onDefaultPhoneClicked(final int itemPosition, SupplierLite supplier);

    /**
     * Callback Method of {@link SupplierListUserActionsListener} invoked when
     * the user clicks on the default Email shown. This should launch an Intent
     * to start an Email Activity passing in the To address {@link SupplierLite#mDefaultEmail}.
     *
     * @param itemPosition The adapter position of the Item View clicked
     * @param supplier     The {@link SupplierLite} associated with the Item View clicked.
     */
    void onDefaultEmailClicked(final int itemPosition, SupplierLite supplier);
}
