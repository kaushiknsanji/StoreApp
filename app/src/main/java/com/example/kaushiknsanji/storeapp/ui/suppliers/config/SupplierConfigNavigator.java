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

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;

import com.example.kaushiknsanji.storeapp.data.local.models.ProductLite;

import java.util.ArrayList;

/**
 * Defines Navigation Actions that can be invoked from {@link SupplierConfigActivity}
 *
 * @author Kaushik N Sanji
 */
public interface SupplierConfigNavigator {

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
    void launchEditProduct(int productId, ActivityOptionsCompat activityOptionsCompat);

    /**
     * Method invoked when the user clicks on the "Add Item" button, present under "Supplier Items"
     * to add/link items to the Supplier. This should launch the
     * {@link com.example.kaushiknsanji.storeapp.ui.suppliers.product.SupplierProductPickerActivity}
     * to pick the Products for the Supplier to sell.
     *
     * @param productLiteList ArrayList of Products {@link ProductLite} already picked for the Supplier to sell.
     */
    void launchPickProducts(ArrayList<ProductLite> productLiteList);

}
