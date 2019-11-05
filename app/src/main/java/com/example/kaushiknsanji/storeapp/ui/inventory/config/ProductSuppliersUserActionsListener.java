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

package com.example.kaushiknsanji.storeapp.ui.inventory.config;

import com.example.kaushiknsanji.storeapp.data.local.models.ProductSupplierSales;

/**
 * Interface to be implemented by {@link SalesConfigActivityFragment}
 * to receive callback events for User actions on RecyclerView list of Product's Suppliers
 *
 * @author Kaushik N Sanji
 */
public interface ProductSuppliersUserActionsListener {

    /**
     * Callback Method of {@link ProductSuppliersUserActionsListener} invoked when
     * the user clicks on the "Edit" button. This should launch the
     * {@link com.example.kaushiknsanji.storeapp.ui.suppliers.config.SupplierConfigActivity}
     * for the Supplier to be edited.
     *
     * @param itemPosition         The adapter position of the Item clicked.
     * @param productSupplierSales The {@link ProductSupplierSales} associated with the Item clicked.
     */
    void onEditSupplier(final int itemPosition, ProductSupplierSales productSupplierSales);

    /**
     * Callback Method of {@link ProductSuppliersUserActionsListener} invoked when
     * the user swipes the Item View to remove the Product-Supplier link.
     *
     * @param itemPosition         The adapter position of the Item clicked.
     * @param productSupplierSales The {@link ProductSupplierSales} associated with the Item swiped.
     */
    void onSwiped(final int itemPosition, ProductSupplierSales productSupplierSales);

    /**
     * Callback Method of {@link ProductSuppliersUserActionsListener} invoked when
     * the user clicks on the "Procure" button. This should launch the
     * {@link com.example.kaushiknsanji.storeapp.ui.inventory.procure.SalesProcurementActivity}
     * for the User to place procurement for the Product.
     *
     * @param itemPosition         The adapter position of the Item clicked.
     * @param productSupplierSales The {@link ProductSupplierSales} associated with the Item clicked.
     */
    void onProcure(final int itemPosition, ProductSupplierSales productSupplierSales);

    /**
     * Callback Method of {@link ProductSuppliersUserActionsListener} invoked when
     * the total available quantity of the Product has been recalculated.
     *
     * @param totalAvailableQuantity Integer value of the Total Available quantity of the Product.
     */
    void onUpdatedAvailability(final int totalAvailableQuantity);

    /**
     * Callback Method of {@link ProductSuppliersUserActionsListener} invoked when
     * there is a change to the total available quantity of the Product.
     *
     * @param changeInAvailableQuantity Integer value of the change in the Total Available
     *                                  quantity of the Product with respect to the last
     *                                  Updated Availability. Can be negative to indicate
     *                                  the decrease in Available Quantity.
     */
    void onChangeInAvailability(final int changeInAvailableQuantity);
}
