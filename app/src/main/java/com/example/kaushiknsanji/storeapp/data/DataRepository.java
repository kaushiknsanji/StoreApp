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

package com.example.kaushiknsanji.storeapp.data;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.example.kaushiknsanji.storeapp.data.local.models.Product;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductLite;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductSupplierSales;
import com.example.kaushiknsanji.storeapp.data.local.models.Supplier;
import com.example.kaushiknsanji.storeapp.data.local.models.SupplierContact;

import java.util.ArrayList;
import java.util.List;

/**
 * Contract Interface for the App's Database management. Database communication is
 * implemented by {@link StoreRepository}
 * and {@link com.example.kaushiknsanji.storeapp.data.local.StoreLocalRepository}
 *
 * @author Kaushik N Sanji
 */
public interface DataRepository {

    /**
     * Method that retrieves the Categories for configuring a Product.
     *
     * @param queryCallback The Callback to be implemented by the caller to receive the results
     */
    void getAllCategories(@NonNull GetQueryCallback<List<String>> queryCallback);

    /**
     * Method that retrieves the Category Id for the Category Name configured for the Product.
     *
     * @param categoryName  The Category Name selected for the Product
     * @param queryCallback The Callback to be implemented by the caller to receive the result.
     */
    void getCategoryByName(@NonNull String categoryName, @NonNull GetQueryCallback<Integer> queryCallback);

    /**
     * Method that retrieves the Product Details of Product identified by its Id.
     *
     * @param productId     The Integer Id of the Product to lookup for.
     * @param queryCallback The Callback to be implemented by the caller to receive the result.
     */
    void getProductDetailsById(int productId, @NonNull GetQueryCallback<Product> queryCallback);

    /**
     * Method that checks and validates the uniqueness of the Product SKU {@code productSku} passed.
     *
     * @param productSku    The Product SKU of the Product to lookup for.
     * @param queryCallback The Callback to be implemented by the caller to receive the result.
     */
    void getProductSkuUniqueness(@NonNull String productSku, @NonNull GetQueryCallback<Boolean> queryCallback);

    /**
     * Method that adds a New {@link Product} entry into the database.
     *
     * @param newProduct         The New {@link Product} to be added to the database
     * @param operationsCallback The Callback to be implemented by the caller to
     *                           receive the operation result.
     */
    void saveNewProduct(@NonNull Product newProduct, @NonNull DataOperationsCallback operationsCallback);

    /**
     * Method that updates an existing {@link Product} entry in the database.
     *
     * @param existingProduct    The Existing Product details for figuring out the required
     *                           CRUD operations
     * @param newProduct         The New Updated Product details to be saved in the database.
     * @param operationsCallback The Callback to be implemented by the caller to
     *                           receive the operation result.
     */
    void saveUpdatedProduct(@NonNull Product existingProduct, @NonNull Product newProduct,
                            @NonNull DataOperationsCallback operationsCallback);

    /**
     * Method that deletes a Product identified by its Id.
     * This also deletes any relationship data with the Product.
     *
     * @param productId          The Product Id of the Product to be deleted.
     * @param operationsCallback The Callback to be implemented by the caller to
     *                           receive the operation result.
     */
    void deleteProductById(int productId, @NonNull DataOperationsCallback operationsCallback);

    /**
     * Method that updates the list of {@link ProductImage} details for the
     * existing Product {@code existingProduct} into the database table 'item_image'.
     * Performs a silent delete operation of all the Images previously
     * configured for the Product if any when the {@code productImages} passed is empty.
     * (Applicable for Existing Product only)
     *
     * @param existingProduct    The Existing Product for which the Product Images are to be updated.
     * @param productImages      The New List of {@link ProductImage} details to be updated to the database
     * @param operationsCallback The Callback to be implemented by the caller to
     *                           receive the operation result.
     */
    void saveProductImages(@NonNull Product existingProduct, @NonNull ArrayList<ProductImage> productImages, @NonNull DataOperationsCallback operationsCallback);

    /**
     * Method that registers a {@link ContentObserver} class that receives callbacks
     * when the data identified by a given content URI changes.
     *
     * @param uri                  The URI to watch for changes. This can be a specific row URI,
     *                             or a base URI for a whole class of content.
     * @param notifyForDescendants When false, the observer will be notified
     *                             whenever a change occurs to the exact URI specified by
     *                             <code>uri</code> or to one of the URI's ancestors in the path
     *                             hierarchy. When true, the observer will also be notified
     *                             whenever a change occurs to the URI's descendants in the path
     *                             hierarchy.
     * @param observer             The {@link ContentObserver} object that receives callbacks when changes occur.
     */
    void registerContentObserver(@NonNull Uri uri, boolean notifyForDescendants,
                                 @NonNull ContentObserver observer);

    /**
     * Method that unregisters a registered change observer {@link ContentObserver}.
     *
     * @param observer The previously registered {@link ContentObserver} that is no longer needed.
     */
    void unregisterContentObserver(@NonNull ContentObserver observer);

    /**
     * Method that retrieves the Supplier Details of Supplier identified by its Id.
     *
     * @param supplierId    The Integer Id of the Supplier to lookup for.
     * @param queryCallback The Callback to be implemented by the caller to receive the result.
     */
    void getSupplierDetailsById(int supplierId, @NonNull GetQueryCallback<Supplier> queryCallback);

    /**
     * Method that retrieves the Contacts of a Supplier identified by its Id.
     *
     * @param supplierId    The Integer Id of the Supplier to retrieve the List of Supplier's Contacts.
     * @param queryCallback The Callback to be implemented by the caller to receive the result.
     */
    void getSupplierContactsById(int supplierId, @NonNull GetQueryCallback<List<SupplierContact>> queryCallback);

    /**
     * Method that checks and validates the uniqueness of the Supplier Code {@code supplierCode} passed.
     *
     * @param supplierCode  The Supplier Code of the Supplier to lookup for.
     * @param queryCallback The Callback to be implemented by the caller to receive the result.
     */
    void getSupplierCodeUniqueness(@NonNull String supplierCode, @NonNull GetQueryCallback<Boolean> queryCallback);

    /**
     * Method that retrieves short information of the Products identified by its Ids {@code productIds}
     *
     * @param productIds    List of Ids of the Products whose information is required. When {@code null},
     *                      information for all the Products in the database is retrieved.
     * @param queryCallback The Callback to be implemented by the caller to receive the result.
     */
    void getShortProductInfoForProducts(@Nullable List<String> productIds, @NonNull GetQueryCallback<List<ProductLite>> queryCallback);

    /**
     * Method that adds a new {@link Supplier} entry into the database.
     *
     * @param newSupplier        The new {@link Supplier} to be added to the database.
     * @param operationsCallback The Callback to be implemented by the caller to
     *                           receive the operation result.
     */
    void saveNewSupplier(@NonNull Supplier newSupplier, @NonNull DataOperationsCallback operationsCallback);

    /**
     * Method that updates an existing {@link Supplier} entry in the database.
     *
     * @param existingSupplier   The Existing Supplier details for figuring out the required
     *                           CRUD operations.
     * @param newSupplier        The New Supplier details to be saved in the database.
     * @param operationsCallback The Callback to be implemented by the caller to
     *                           receive the operation result.
     */
    void saveUpdatedSupplier(@NonNull Supplier existingSupplier, @NonNull Supplier newSupplier,
                             @NonNull DataOperationsCallback operationsCallback);

    /**
     * Method that deletes a Supplier identified by its Id.
     * This also deletes any relationship data with the Supplier.
     *
     * @param supplierId         The Supplier Id of the Supplier to be deleted.
     * @param operationsCallback The Callback to be implemented by the caller to
     *                           receive the operation result.
     */
    void deleteSupplierById(int supplierId, @NonNull DataOperationsCallback operationsCallback);

    /**
     * Method that decreases the available quantity {@code availableQuantity} of a Product sold
     * by the Supplier, by the specified quantity {@code decreaseQuantityBy}.
     *
     * @param productId          The Product Id of the Product.
     * @param productSku         The Product SKU of the Product.
     * @param supplierId         The Supplier Id of the Supplier for the Product.
     * @param supplierCode       The Supplier Code of the Supplier for the Product.
     * @param availableQuantity  The current available Quantity of the Product at the Supplier.
     * @param decreaseQuantityBy The amount to decrease the available quantity by.
     * @param operationsCallback The Callback to be implemented by the caller to
     *                           receive the operation result.
     */
    void decreaseProductSupplierInventory(int productId, String productSku, int supplierId, String supplierCode, int availableQuantity,
                                          int decreaseQuantityBy, @NonNull DataOperationsCallback operationsCallback);

    /**
     * Method that retrieves the Suppliers' Inventory and Price details
     * for a Product identified by its id.
     *
     * @param productId     The Integer Id of the Product to lookup for.
     * @param queryCallback The Callback to be implemented by the caller to receive the result.
     */
    void getProductSuppliersSalesInfo(int productId, @NonNull GetQueryCallback<List<ProductSupplierSales>> queryCallback);

    /**
     * Method that updates the inventory of the Product identified by the Product Id {@code productId}
     * at all its registered suppliers.
     *
     * @param productId                    The Product Id of the Product whose inventory is being updated.
     * @param productSku                   The Product SKU of the Product.
     * @param existingProductSupplierSales List of Product's Suppliers with Sales information
     *                                     currently persisted in the database.
     * @param updatedProductSupplierSales  List of Product's Suppliers with updated Sales information
     * @param operationsCallback           The Callback to be implemented by the caller to
     *                                     receive the operation result.
     */
    void saveUpdatedProductSalesInfo(int productId, String productSku, @NonNull List<ProductSupplierSales> existingProductSupplierSales,
                                     @NonNull List<ProductSupplierSales> updatedProductSupplierSales,
                                     @NonNull DataOperationsCallback operationsCallback);

    /**
     * Callback Interface for Database query requests
     *
     * @param <T> The type of the results expected for the query being executed.
     */
    interface GetQueryCallback<T> {
        /**
         * Method invoked when the results are obtained
         * for the query executed.
         *
         * @param results The query results in the generic type passed
         */
        void onResults(T results);

        /**
         * Method invoked when there are no results
         * for the query executed.
         */
        void onEmpty();

        /**
         * Method invoked when the results could not be retrieved
         * for the query due to some error.
         *
         * @param messageId The String resource of the error message
         *                  for the query execution failure
         * @param args      Variable number of arguments to replace the format specifiers
         *                  in the String resource if any
         */
        default void onFailure(@StringRes int messageId, @Nullable Object... args) {
        }
    }

    /**
     * Callback Interface for the Database CRUD Operations like insert/update/delete
     */
    interface DataOperationsCallback {
        /**
         * Method invoked when the database operations like insert/update/delete
         * was successful.
         */
        void onSuccess();

        /**
         * Method invoked when the database operations like insert/update/delete
         * failed to complete.
         *
         * @param messageId The String resource of the error message
         *                  for the database operation failure
         * @param args      Variable number of arguments to replace the format specifiers
         *                  in the String resource if any
         */
        void onFailure(@StringRes int messageId, @Nullable Object... args);
    }

    /**
     * Callback interface for the {@link android.support.v4.content.CursorLoader} operations
     */
    interface CursorDataLoaderCallback {
        /**
         * Callback Method of {@link CursorDataLoaderCallback} invoked when data is present
         * in the Cursor {@code data}
         *
         * @param data The {@link Cursor} generated for the query executed
         *             using a {@link android.support.v4.content.CursorLoader}
         */
        void onDataLoaded(Cursor data);

        /**
         * Callback Method of {@link CursorDataLoaderCallback} invoked when there is no data
         * in the {@link Cursor} returned for the query executed
         * using a {@link android.support.v4.content.CursorLoader}
         */
        void onDataEmpty();

        /**
         * Callback Method of {@link CursorDataLoaderCallback} invoked when no {@link Cursor}
         * was generated for the query executed
         * using a {@link android.support.v4.content.CursorLoader}
         */
        void onDataNotAvailable();

        /**
         * Callback Method of {@link CursorDataLoaderCallback} invoked when
         * the {@link android.support.v4.content.CursorLoader} was reset
         */
        void onDataReset();

        /**
         * Callback Method of {@link CursorDataLoaderCallback} invoked when
         * there is a change in the content loaded by the {@link android.support.v4.content.CursorLoader}
         */
        void onContentChange();
    }
}
