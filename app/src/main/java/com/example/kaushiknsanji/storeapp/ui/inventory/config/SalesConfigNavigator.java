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

import android.support.annotation.NonNull;

import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductSupplierSales;

/**
 * Defines Navigation Actions that can be invoked from {@link SalesConfigActivity}
 *
 * @author Kaushik N Sanji
 */
public interface SalesConfigNavigator {

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

    /**
     * Method invoked when the user clicks on Edit button of the Product Details. This should
     * launch the {@link com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigActivity}
     * for the Product to be edited.
     *
     * @param productId The Primary Key of the Product to be edited.
     */
    void launchEditProduct(int productId);

    /**
     * Method invoked when the user clicks on the "Procure" button of the Item View of any Product's Suppliers.
     * This should launch the {@link com.example.kaushiknsanji.storeapp.ui.inventory.procure.SalesProcurementActivity}
     * for the User to place procurement for the Product.
     *
     * @param productSupplierSales  The {@link ProductSupplierSales} associated with the Item clicked.
     * @param productImageToBeShown The defaulted {@link ProductImage} of the Product.
     * @param productName           The Name of the Product.
     * @param productSku            The SKU of the Product.
     */
    void launchProcureProduct(ProductSupplierSales productSupplierSales, ProductImage productImageToBeShown, String productName, String productSku);

    /**
     * Method invoked when the user clicks on "Edit" button. This should
     * launch the {@link com.example.kaushiknsanji.storeapp.ui.suppliers.config.SupplierConfigActivity}
     * for the Supplier to be edited.
     *
     * @param supplierId The Primary key of the Supplier to be edited.
     */
    void launchEditSupplier(int supplierId);
}
