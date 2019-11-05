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

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.kaushiknsanji.storeapp.data.local.models.Product;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductLite;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductSupplierSales;
import com.example.kaushiknsanji.storeapp.data.local.models.Supplier;
import com.example.kaushiknsanji.storeapp.data.local.models.SupplierContact;

import java.util.ArrayList;
import java.util.List;

/**
 * The App Repository class that interfaces with {@link DataRepository}
 * and {@link FileRepository} to communicate with Database and Files respectively.
 *
 * @author Kaushik N Sanji
 */
public class StoreRepository implements DataRepository, FileRepository {

    //Constant used for logs
    private static final String LOG_TAG = StoreRepository.class.getSimpleName();

    //Singleton instance of StoreRepository
    private static volatile StoreRepository INSTANCE;

    //Instance of DataRepository to communicate with Database
    private final DataRepository mLocalDataSource;

    //Instance of FileRepository to communicate with Files
    private final FileRepository mLocalFileSource;

    /**
     * Private Constructor of {@link StoreRepository}
     *
     * @param localDataSource Instance of {@link DataRepository} to communicate with Database
     * @param localFileSource Instance of {@link FileRepository} to communicate with Files
     */
    private StoreRepository(@NonNull DataRepository localDataSource, @NonNull FileRepository localFileSource) {
        mLocalDataSource = localDataSource;
        mLocalFileSource = localFileSource;
    }

    /**
     * Static Singleton Constructor that creates a single instance of {@link StoreRepository}
     *
     * @param localDataSource Instance of {@link DataRepository} to communicate with Database
     * @param localFileSource Instance of {@link FileRepository} to communicate with Files
     * @return New or existing instance of {@link StoreRepository}
     */
    public static StoreRepository getInstance(@NonNull DataRepository localDataSource, @NonNull FileRepository localFileSource) {
        if (INSTANCE == null) {
            //When instance is not available
            synchronized (StoreRepository.class) {
                //Apply lock and check for the instance again
                if (INSTANCE == null) {
                    //When there is no instance, create a new one
                    INSTANCE = new StoreRepository(localDataSource, localFileSource);
                }
            }
        }
        //Returning the instance of StoreRepository
        return INSTANCE;
    }

    /**
     * Method that retrieves the Categories for configuring a Product.
     *
     * @param queryCallback The Callback to be implemented by the caller to receive the results
     */
    @Override
    public void getAllCategories(@NonNull GetQueryCallback<List<String>> queryCallback) {
        mLocalDataSource.getAllCategories(queryCallback);
    }

    /**
     * Method that retrieves the Category Id for the Category Name configured for the Product.
     *
     * @param categoryName  The Category Name selected for the Product
     * @param queryCallback The Callback to be implemented by the caller to receive the result.
     */
    @Override
    public void getCategoryByName(@NonNull String categoryName, @NonNull GetQueryCallback<Integer> queryCallback) {
        mLocalDataSource.getCategoryByName(categoryName, queryCallback);
    }

    /**
     * Method that retrieves the Product Details of Product identified by its Id.
     *
     * @param productId     The Integer Id of the Product to lookup for.
     * @param queryCallback The Callback to be implemented by the caller to receive the result.
     */
    @Override
    public void getProductDetailsById(int productId, @NonNull GetQueryCallback<Product> queryCallback) {
        mLocalDataSource.getProductDetailsById(productId, queryCallback);
    }

    /**
     * Method that checks and validates the uniqueness of the Product SKU {@code productSku} passed.
     *
     * @param productSku    The Product SKU of the Product to lookup for.
     * @param queryCallback The Callback to be implemented by the caller to receive the result.
     */
    @Override
    public void getProductSkuUniqueness(@NonNull String productSku, @NonNull GetQueryCallback<Boolean> queryCallback) {
        mLocalDataSource.getProductSkuUniqueness(productSku, queryCallback);
    }

    /**
     * Method that adds a New {@link Product} entry into the database.
     *
     * @param newProduct         The New {@link Product} to be added to the database
     * @param operationsCallback The Callback to be implemented by the caller to
     *                           receive the operation result.
     */
    @Override
    public void saveNewProduct(@NonNull Product newProduct, @NonNull DataOperationsCallback operationsCallback) {
        mLocalDataSource.saveNewProduct(newProduct, operationsCallback);
    }

    /**
     * Method that updates an existing {@link Product} entry in the database.
     *
     * @param existingProduct    The Existing Product details for figuring out the required
     *                           CRUD operations
     * @param newProduct         The New Updated Product details to be saved in the database.
     * @param operationsCallback The Callback to be implemented by the caller to
     *                           receive the operation result.
     */
    @Override
    public void saveUpdatedProduct(@NonNull Product existingProduct, @NonNull Product newProduct,
                                   @NonNull DataOperationsCallback operationsCallback) {
        mLocalDataSource.saveUpdatedProduct(existingProduct, newProduct, operationsCallback);
    }

    /**
     * Method that deletes a Product identified by its Id.
     * This also deletes any relationship data with the Product.
     *
     * @param productId          The Product Id of the Product to be deleted.
     * @param operationsCallback The Callback to be implemented by the caller to
     */
    @Override
    public void deleteProductById(int productId, @NonNull DataOperationsCallback operationsCallback) {
        mLocalDataSource.deleteProductById(productId, operationsCallback);
    }

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
     */
    @Override
    public void saveProductImages(@NonNull Product existingProduct, @NonNull ArrayList<ProductImage> productImages, @NonNull DataOperationsCallback operationsCallback) {
        mLocalDataSource.saveProductImages(existingProduct, productImages, operationsCallback);
    }

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
    @Override
    public void registerContentObserver(@NonNull Uri uri, boolean notifyForDescendants, @NonNull ContentObserver observer) {
        mLocalDataSource.registerContentObserver(uri, notifyForDescendants, observer);
    }

    /**
     * Method that unregisters a registered change observer {@link ContentObserver}.
     *
     * @param observer The previously registered {@link ContentObserver} that is no longer needed.
     */
    @Override
    public void unregisterContentObserver(@NonNull ContentObserver observer) {
        mLocalDataSource.unregisterContentObserver(observer);
    }

    /**
     * Method that retrieves the Supplier Details of Supplier identified by its Id.
     *
     * @param supplierId    The Integer Id of the Supplier to lookup for.
     * @param queryCallback The Callback to be implemented by the caller to receive the result.
     */
    @Override
    public void getSupplierDetailsById(int supplierId, @NonNull GetQueryCallback<Supplier> queryCallback) {
        mLocalDataSource.getSupplierDetailsById(supplierId, queryCallback);
    }

    /**
     * Method that retrieves the Contacts of a Supplier identified by its Id.
     *
     * @param supplierId    The Integer Id of the Supplier to retrieve the List of Supplier's Contacts.
     * @param queryCallback The Callback to be implemented by the caller to receive the result.
     */
    @Override
    public void getSupplierContactsById(int supplierId, @NonNull GetQueryCallback<List<SupplierContact>> queryCallback) {
        mLocalDataSource.getSupplierContactsById(supplierId, queryCallback);
    }

    /**
     * Method that checks and validates the uniqueness of the Supplier Code {@code supplierCode} passed.
     *
     * @param supplierCode  The Supplier Code of the Supplier to lookup for.
     * @param queryCallback The Callback to be implemented by the caller to receive the result.
     */
    @Override
    public void getSupplierCodeUniqueness(@NonNull String supplierCode, @NonNull GetQueryCallback<Boolean> queryCallback) {
        mLocalDataSource.getSupplierCodeUniqueness(supplierCode, queryCallback);
    }

    /**
     * Method that retrieves short information of the Products identified by its Ids {@code productIds}
     *
     * @param productIds    List of Ids of the Products whose information is required. When {@code null},
     *                      information for all the Products in the database is retrieved.
     * @param queryCallback The Callback to be implemented by the caller to receive the result.
     */
    @Override
    public void getShortProductInfoForProducts(@Nullable List<String> productIds, @NonNull GetQueryCallback<List<ProductLite>> queryCallback) {
        mLocalDataSource.getShortProductInfoForProducts(productIds, queryCallback);
    }

    /**
     * Method that adds a new {@link Supplier} entry into the database.
     *
     * @param newSupplier        The new {@link Supplier} to be added to the database.
     * @param operationsCallback The Callback to be implemented by the caller to
     */
    @Override
    public void saveNewSupplier(@NonNull Supplier newSupplier, @NonNull DataOperationsCallback operationsCallback) {
        mLocalDataSource.saveNewSupplier(newSupplier, operationsCallback);
    }

    /**
     * Method that updates an existing {@link Supplier} entry in the database.
     *
     * @param existingSupplier   The Existing Supplier details for figuring out the required
     *                           CRUD operations.
     * @param newSupplier        The New Supplier details to be saved in the database.
     * @param operationsCallback The Callback to be implemented by the caller to
     */
    @Override
    public void saveUpdatedSupplier(@NonNull Supplier existingSupplier, @NonNull Supplier newSupplier, @NonNull DataOperationsCallback operationsCallback) {
        mLocalDataSource.saveUpdatedSupplier(existingSupplier, newSupplier, operationsCallback);
    }

    /**
     * Method that deletes a Supplier identified by its Id.
     * This also deletes any relationship data with the Supplier.
     *
     * @param supplierId         The Supplier Id of the Supplier to be deleted.
     * @param operationsCallback The Callback to be implemented by the caller to
     */
    @Override
    public void deleteSupplierById(int supplierId, @NonNull DataOperationsCallback operationsCallback) {
        mLocalDataSource.deleteSupplierById(supplierId, operationsCallback);
    }

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
     */
    @Override
    public void decreaseProductSupplierInventory(int productId, String productSku,
                                                 int supplierId, String supplierCode,
                                                 int availableQuantity, int decreaseQuantityBy,
                                                 @NonNull DataOperationsCallback operationsCallback) {
        mLocalDataSource.decreaseProductSupplierInventory(productId, productSku, supplierId, supplierCode, availableQuantity, decreaseQuantityBy, operationsCallback);
    }

    /**
     * Method that retrieves the Suppliers' Inventory and Price details
     * for a Product identified by its id.
     *
     * @param productId     The Integer Id of the Product to lookup for.
     * @param queryCallback The Callback to be implemented by the caller to receive the result.
     */
    @Override
    public void getProductSuppliersSalesInfo(int productId, @NonNull GetQueryCallback<List<ProductSupplierSales>> queryCallback) {
        mLocalDataSource.getProductSuppliersSalesInfo(productId, queryCallback);
    }

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
     */
    @Override
    public void saveUpdatedProductSalesInfo(int productId, String productSku,
                                            @NonNull List<ProductSupplierSales> existingProductSupplierSales,
                                            @NonNull List<ProductSupplierSales> updatedProductSupplierSales,
                                            @NonNull DataOperationsCallback operationsCallback) {
        mLocalDataSource.saveUpdatedProductSalesInfo(productId, productSku,
                existingProductSupplierSales, updatedProductSupplierSales, operationsCallback);
    }

    /**
     * Method that saves the Image pointed to by the Content URI {@code fileContentUri}
     * in a file located at the app's private external storage path.
     *
     * @param context            The Context of the Activity/Fragment
     * @param fileContentUri     The Content Uri of the Temporary Image File
     * @param operationsCallback The Callback to be implemented by the caller to receive the result.
     */
    @Override
    public void saveImageToFile(Context context, Uri fileContentUri, FileOperationsCallback<Uri> operationsCallback) {
        mLocalFileSource.saveImageToFile(context, fileContentUri, operationsCallback);
    }

    /**
     * Method that persists the persistable URI permission grant that the system gives the app.
     * Applicable for devices with Android Kitkat (API level 19) and above.
     *
     * @param fileContentUri The Content Uri of a File returned by the Intent
     * @param intentFlags    The existing intent flags on the URI
     */
    @Override
    public void takePersistablePermissions(Uri fileContentUri, int intentFlags) {
        mLocalFileSource.takePersistablePermissions(fileContentUri, intentFlags);
    }

    /**
     * Method that deletes the Image files passed in {@code fileContentUriList}
     *
     * @param fileContentUriList List of String URIs (Content URIs) of the Image Files to be deleted.
     * @param operationsCallback The Callback to be implemented by the caller to receive the result.
     */
    @Override
    public void deleteImageFiles(List<String> fileContentUriList, FileOperationsCallback<Boolean> operationsCallback) {
        mLocalFileSource.deleteImageFiles(fileContentUriList, operationsCallback);
    }

    /**
     * Method that deletes the Image files passed in {@code fileContentUriList} silently
     * without reporting success/failure back to the caller
     *
     * @param fileContentUriList List of String URIs (Content URIs) of the Image Files to be deleted.
     */
    @Override
    public void deleteImageFilesSilently(List<String> fileContentUriList) {
        mLocalFileSource.deleteImageFiles(fileContentUriList, new FileRepository.FileOperationsCallback<Boolean>() {
            /**
             * Method invoked when the file operation was executed successfully.
             *
             * @param results The results of the operation in the generic type passed.
             */
            @Override
            public void onSuccess(Boolean results) {
                //no-op
                //Just logging the result
                Log.i(LOG_TAG, "onSuccess: deleteImageFilesSilently: All Image files deleted");
            }

            /**
             * Method invoked when the file operation failed to complete.
             *
             * @param messageId The String resource of the error message
             *                  for the file operation failure
             * @param args      Variable number of arguments to replace the format specifiers
             */
            @Override
            public void onFailure(int messageId, @Nullable Object... args) {
                //no-op
                //Just logging the failure
                Log.i(LOG_TAG, "onFailure: deleteImageFilesSilently: Some Image files were not deleted");
            }
        });
    }


}
