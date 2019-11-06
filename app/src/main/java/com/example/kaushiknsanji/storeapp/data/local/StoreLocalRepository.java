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

package com.example.kaushiknsanji.storeapp.data.local;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.util.Pair;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.DataRepository;
import com.example.kaushiknsanji.storeapp.data.local.contracts.ProductContract;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SalesContract;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SupplierContract;
import com.example.kaushiknsanji.storeapp.data.local.models.Product;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductAttribute;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductLite;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductSupplierInfo;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductSupplierSales;
import com.example.kaushiknsanji.storeapp.data.local.models.Supplier;
import com.example.kaushiknsanji.storeapp.data.local.models.SupplierContact;
import com.example.kaushiknsanji.storeapp.data.local.utils.QueryArgsUtility;
import com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility;
import com.example.kaushiknsanji.storeapp.utils.AppExecutors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.AND;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.EQUALS;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.PLACEHOLDER;

/**
 * The Database Repository class that implements {@link DataRepository} interface
 * to manage communication with the Database of the App.
 *
 * @author Kaushik N Sanji
 */
public class StoreLocalRepository implements DataRepository {

    //Constant used for logs
    private static final String LOG_TAG = StoreLocalRepository.class.getSimpleName();

    //Singleton instance of StoreLocalRepository
    private static volatile StoreLocalRepository INSTANCE;

    //The ContentResolver instance to communicate with the Database
    private final ContentResolver mContentResolver;

    //AppExecutors instance for threading requests
    private final AppExecutors mAppExecutors;

    /**
     * Private Constructor of {@link StoreLocalRepository}
     *
     * @param contentResolver The {@link ContentResolver} instance to communicate with the Database
     * @param appExecutors    {@link AppExecutors} instance for threading requests
     */
    private StoreLocalRepository(@NonNull ContentResolver contentResolver, @NonNull AppExecutors appExecutors) {
        mContentResolver = contentResolver;
        mAppExecutors = appExecutors;
    }

    /**
     * Singleton Constructor that creates a single instance of {@link StoreLocalRepository}
     *
     * @param contentResolver The {@link ContentResolver} instance to communicate with the Database
     * @param appExecutors    {@link AppExecutors} instance for threading requests
     * @return New or existing instance of {@link StoreLocalRepository}
     */
    public static StoreLocalRepository getInstance(@NonNull ContentResolver contentResolver, @NonNull AppExecutors appExecutors) {
        if (INSTANCE == null) {
            //When instance is not available
            synchronized (StoreLocalRepository.class) {
                //Apply lock and check for the instance again
                if (INSTANCE == null) {
                    //When there is no instance, create a new one
                    INSTANCE = new StoreLocalRepository(contentResolver, appExecutors);
                }
            }
        }
        //Returning the instance of StoreLocalRepository
        return INSTANCE;
    }

    /**
     * Method that retrieves the Categories for configuring a Product.
     *
     * @param queryCallback The Callback to be implemented by the caller to receive the results
     */
    @Override
    public void getAllCategories(@NonNull GetQueryCallback<List<String>> queryCallback) {
        //Executing on Disk Thread
        mAppExecutors.getDiskIO().execute(() -> {
            //Retrieving the cursor to the records, sorted by "category_name" column
            Cursor cursor = mContentResolver.query(
                    ProductContract.ProductCategory.CONTENT_URI,
                    QueryArgsUtility.CategoriesQuery.getProjection(),
                    null,
                    null,
                    ProductContract.ProductCategory.COLUMN_ITEM_CATEGORY_NAME
            );

            //Iterating over the cursor records to build the list of categories
            ArrayList<String> categoryList = new ArrayList<>();
            try {
                if (cursor != null && cursor.getCount() > 0) {
                    //When the query returned results

                    //Iterating over the cursor results and building the list of categories
                    while (cursor.moveToNext()) {
                        categoryList.add(cursor.getString(QueryArgsUtility.CategoriesQuery.COLUMN_ITEM_CATEGORY_NAME_INDEX));
                    }
                }
            } finally {
                //Closing the cursor to release its resources
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }

            //Executing on the Main Thread
            mAppExecutors.getMainThread().execute(() -> {
                if (categoryList.size() > 0) {
                    //Pass the results to the callback
                    queryCallback.onResults(categoryList);
                } else {
                    //Return to the caller when there are no results
                    queryCallback.onEmpty();
                }
            });

        });
    }

    /**
     * Method that retrieves the Category Id for the Category Name configured for the Product.
     *
     * @param categoryName  The Category Name selected for the Product
     * @param queryCallback The Callback to be implemented by the caller to receive the result.
     */
    @Override
    public void getCategoryByName(@NonNull String categoryName, @NonNull GetQueryCallback<Integer> queryCallback) {
        //Executing on Disk Thread
        mAppExecutors.getDiskIO().execute(() -> {
            //Retrieving the cursor to the record
            Cursor cursor = mContentResolver.query(
                    ProductContract.ProductCategory.buildCategoryNameUri(categoryName),
                    QueryArgsUtility.CategoryByNameQuery.getProjection(),
                    null,
                    null,
                    null
            );

            //Retrieving the Category Id from the cursor
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    //When Cursor is not Null and there is a record for the Category being queried

                    //Read the Category ID column
                    final Integer categoryId = cursor.getInt(QueryArgsUtility.CategoryByNameQuery.COLUMN_ITEM_CATEGORY_ID_INDEX);

                    //Checking if the value in the column is NULL
                    final boolean cursorNull = cursor.isNull(QueryArgsUtility.CategoryByNameQuery.COLUMN_ITEM_CATEGORY_ID_INDEX);

                    //Executing on the Main Thread
                    mAppExecutors.getMainThread().execute(() -> {
                        if (cursorNull) {
                            //Category ID cannot be Null. Pass the error to the callback.
                            queryCallback.onFailure(R.string.product_config_category_null_error, categoryName);
                        } else {
                            //When Category ID is not Null, pass the results to the callback
                            queryCallback.onResults(categoryId);
                        }
                    });

                } else {
                    //When Cursor is Null, the Category being queried is not found.
                    //Pass the empty result to the callback in this case.
                    queryCallback.onEmpty();
                }

            } finally {
                //Closing the cursor to release its resources
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }

        });
    }

    /**
     * Method that retrieves the Product Details of Product identified by its Id.
     *
     * @param productId     The Integer Id of the Product to lookup for.
     * @param queryCallback The Callback to be implemented by the caller to receive the result.
     */
    @Override
    public void getProductDetailsById(int productId, @NonNull GetQueryCallback<Product> queryCallback) {
        //Retrieving the Product details for the Product ID passed
        //Executing on Disk Thread
        mAppExecutors.getDiskIO().execute(() -> {
            //Retrieving the cursor to the record
            Cursor cursor = mContentResolver.query(
                    ContentUris.withAppendedId(ProductContract.Product.CONTENT_URI, productId),
                    QueryArgsUtility.ItemByIdQuery.getProjection(),
                    null,
                    null,
                    null
            );

            //Retrieving item details from the cursor
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    //When Cursor is not Null and there is a record for the Product being queried

                    //Retrieving the details of the Item
                    int itemId = cursor.getInt(QueryArgsUtility.ItemByIdQuery.COLUMN_ITEM_ID_INDEX);
                    String itemName = cursor.getString(QueryArgsUtility.ItemByIdQuery.COLUMN_ITEM_NAME_INDEX);
                    String itemSku = cursor.getString(QueryArgsUtility.ItemByIdQuery.COLUMN_ITEM_SKU_INDEX);
                    String itemDescription = cursor.getString(QueryArgsUtility.ItemByIdQuery.COLUMN_ITEM_DESCRIPTION_INDEX);
                    String itemCategoryName = cursor.getString(QueryArgsUtility.ItemByIdQuery.COLUMN_ITEM_CATEGORY_NAME_INDEX);

                    //Retrieving the Hang-off table data: START
                    //Retrieving the ProductAttribute for the Product ID passed
                    ArrayList<ProductAttribute> productAttributes = getProductAttributesById(productId);

                    //Retrieving the ProductImage for the Product ID passed
                    ArrayList<ProductImage> productImages = getProductImagesById(productId);
                    //Retrieving the Hang-off table data: END

                    //Building the Product details
                    final Product product = new Product.Builder()
                            .setId(itemId)
                            .setName(itemName)
                            .setSku(itemSku)
                            .setDescription(itemDescription)
                            .setCategory(itemCategoryName)
                            .setProductAttributes(productAttributes)
                            .setProductImages(productImages)
                            .createProduct();

                    //Executing on the Main Thread
                    mAppExecutors.getMainThread().execute(() -> {
                        //Pass the Product data to the callback
                        queryCallback.onResults(product);
                    });
                } else {
                    //When Cursor is Null, the Product being queried is not found

                    //Pass the Product Not Found error to the callback in this case.
                    queryCallback.onFailure(R.string.product_config_no_product_found_error, productId);
                }

            } finally {
                //Closing the cursor to release its resources
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }

        });
    }

    /**
     * Method that retrieves the list of {@link ProductImage}s for the Product identified by its Id.
     * This is called on the Disk Thread.
     *
     * @param productId The Integer Id of the Product to lookup for.
     * @return List of {@link ProductImage}s for the Product identified by its Id.
     */
    @WorkerThread
    private ArrayList<ProductImage> getProductImagesById(int productId) {
        //Retrieving the cursor to the records
        Cursor cursor = mContentResolver.query(
                ContentUris.withAppendedId(ProductContract.ProductImage.CONTENT_URI, productId),
                QueryArgsUtility.ItemImagesQuery.getProjection(),
                null,
                null,
                null
        );

        //Retrieving item images from the cursor
        ArrayList<ProductImage> productImages = new ArrayList<>();
        try {
            if (cursor != null && cursor.getCount() > 0) {
                //When the query returned results
                while (cursor.moveToNext()) {
                    //Retrieving the values from the record pointed to by the Cursor
                    String imageUri = cursor.getString(QueryArgsUtility.ItemImagesQuery.COLUMN_ITEM_IMAGE_URI_INDEX);
                    int defaultImageId = cursor.getInt(QueryArgsUtility.ItemImagesQuery.COLUMN_ITEM_IMAGE_DEFAULT_INDEX);

                    //Preparing the ProductImage
                    ProductImage productImage = new ProductImage.Builder()
                            .setImageUri(imageUri)
                            .setIsDefault(defaultImageId == ProductContract.ProductImage.ITEM_IMAGE_DEFAULT)
                            .createProductImage();

                    //Adding to the list of ProductImages
                    productImages.add(productImage);
                }
            }
        } finally {
            //Closing the cursor to release its resources
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        //Returning the ProductImages retrieved for the Product ID passed
        return productImages;
    }

    /**
     * Method that retrieves the list of {@link ProductAttribute}s for the Product identified by its Id.
     * This is called on the Disk Thread.
     *
     * @param productId The Integer Id of the Product to lookup for.
     * @return List of {@link ProductAttribute}s for the Product identified by its Id.
     */
    @WorkerThread
    private ArrayList<ProductAttribute> getProductAttributesById(int productId) {
        //Retrieving the cursor to the records
        Cursor cursor = mContentResolver.query(
                ContentUris.withAppendedId(ProductContract.ProductAttribute.CONTENT_URI, productId),
                QueryArgsUtility.ItemAttributesQuery.getProjection(),
                null,
                null,
                null

        );

        //Retrieving item attributes from the cursor
        ArrayList<ProductAttribute> productAttributes = new ArrayList<>();
        try {
            if (cursor != null && cursor.getCount() > 0) {
                //When the query returned results
                while (cursor.moveToNext()) {
                    //Retrieving the values from the record pointed to by the Cursor
                    String attrName = cursor.getString(QueryArgsUtility.ItemAttributesQuery.COLUMN_ITEM_ATTR_NAME_INDEX);
                    String attrValue = cursor.getString(QueryArgsUtility.ItemAttributesQuery.COLUMN_ITEM_ATTR_VALUE_INDEX);

                    //Preparing the ProductAttribute
                    ProductAttribute productAttribute = new ProductAttribute.Builder()
                            .setAttributeName(attrName)
                            .setAttributeValue(attrValue)
                            .createProductAttribute();

                    //Adding to the list of ProductAttributes
                    productAttributes.add(productAttribute);
                }
            }
        } finally {
            //Closing the cursor to release its resources
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        //Returning the ProductAttributes retrieved for the Product ID passed
        return productAttributes;
    }

    /**
     * Method that checks and validates the uniqueness of the Product SKU {@code productSku} passed.
     *
     * @param productSku    The Product SKU of the Product to lookup for.
     * @param queryCallback The Callback to be implemented by the caller to receive the result.
     */
    @Override
    public void getProductSkuUniqueness(@NonNull String productSku, @NonNull GetQueryCallback<Boolean> queryCallback) {
        //Executing on Disk Thread
        mAppExecutors.getDiskIO().execute(() -> {
            //Retrieving the cursor to the record
            Cursor cursor = mContentResolver.query(
                    ProductContract.Product.buildItemSkuUri(productSku),
                    QueryArgsUtility.ItemBySkuQuery.getProjection(),
                    null,
                    null,
                    null
            );

            //Retrieving item from the cursor
            try {
                //Records the uniqueness result of the SKU passed
                boolean isSkuUnique;

                if (cursor != null && cursor.moveToFirst()) {
                    //When cursor is NOT NULL

                    //Retrieving the Item Key for the SKU
                    int itemId = cursor.getInt(QueryArgsUtility.ItemBySkuQuery.COLUMN_ITEM_ID_INDEX);

                    //SKU is unique if the Item Key detected is null or negative
                    isSkuUnique = (cursor.isNull(QueryArgsUtility.ItemBySkuQuery.COLUMN_ITEM_ID_INDEX)
                            || itemId < 0);

                } else {
                    //When cursor is NULL

                    //SKU is unique if the query resulted in no records found
                    isSkuUnique = true;
                }

                //Recording the final data to pass to the caller
                final Boolean isSkuUniqueFinal = isSkuUnique;
                //Executing on the Main Thread
                mAppExecutors.getMainThread().execute(() -> {
                    //Passing the result evaluated to the caller
                    queryCallback.onResults(isSkuUniqueFinal);
                });
            } finally {
                //Closing the cursor to release its resources
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }

        });
    }

    /**
     * Method that adds a New {@link Product} entry into the database table 'item'.
     *
     * @param newProduct         The New {@link Product} to be added to the database
     * @param operationsCallback The Callback to be implemented by the caller to
     *                           receive the operation result.
     */
    @Override
    public void saveNewProduct(@NonNull Product newProduct, @NonNull DataOperationsCallback operationsCallback) {
        //Get the Category Name
        String categoryName = newProduct.getCategory();

        //Check if the Category exists (Executes on Disk Thread)
        getCategoryByName(categoryName, new GetQueryCallback<Integer>() {
            /**
             * Method invoked when the results are obtained
             * for the query executed.
             *
             * @param categoryId The integer Category Id found for the Category Name queried
             */
            @MainThread
            @Override
            public void onResults(Integer categoryId) {
                //When Category was found

                //Propagating the result to #proceedToSaveProduct
                //Executing on Disk Thread
                mAppExecutors.getDiskIO().execute(() -> proceedToSaveProduct(categoryId));
            }

            /**
             * Method invoked when there are no results
             * for the query executed.
             */
            @MainThread
            @Override
            public void onEmpty() {
                //When Category does not exist, we insert the new Category to get the new record Id

                //Executing on Disk Thread
                mAppExecutors.getDiskIO().execute(() -> {
                    //Loading the Category Name for insert
                    ContentValues categoryContentValues = new ContentValues();
                    categoryContentValues.put(ProductContract.ProductCategory.COLUMN_ITEM_CATEGORY_NAME, categoryName);

                    //Executing insert
                    Uri categoryInsertUri = mContentResolver.insert(
                            ProductContract.ProductCategory.CONTENT_URI,
                            categoryContentValues
                    );

                    //Checking the result URI
                    if (categoryInsertUri == null) {
                        //When Null, pass the error message
                        //Executing on Main Thread
                        mAppExecutors.getMainThread().execute(() -> operationsCallback.onFailure(R.string.product_config_insert_category_error, categoryName));
                    } else {
                        //When NOT Null, parse for the Category Id

                        //Parsing the Category Id and propagating the result to #proceedToSaveProduct
                        proceedToSaveProduct((int) ContentUris.parseId(categoryInsertUri));
                    }
                });

            }

            /**
             * Method invoked when the results could not be retrieved
             * for the query due to some error.
             *
             * @param messageId The String resource of the error message
             *                  for the query execution failure
             * @param args      Variable number of arguments to replace the format specifiers
             */
            @MainThread
            @Override
            public void onFailure(int messageId, @Nullable Object... args) {
                //Pass the Failure to the callback
                operationsCallback.onFailure(messageId, args);
            }

            /**
             * Method that saves the Product details after the {@code categoryId}
             * for the Product's category was determined.
             *
             * @param categoryId The Id of the Category determined
             */
            @WorkerThread
            private void proceedToSaveProduct(final int categoryId) {
                //Begin to save Product data

                //Loading the values for 'item' table
                ContentValues itemContentValues = new ContentValues();
                itemContentValues.put(ProductContract.Product.COLUMN_ITEM_NAME, newProduct.getName());
                itemContentValues.put(ProductContract.Product.COLUMN_ITEM_SKU, newProduct.getSku());
                itemContentValues.put(ProductContract.Product.COLUMN_ITEM_DESCRIPTION, newProduct.getDescription());
                itemContentValues.put(ProductContract.Product.COLUMN_ITEM_CATEGORY_ID, categoryId);

                //Executing insert
                Uri itemInsertUri = mContentResolver.insert(
                        ProductContract.Product.CONTENT_URI,
                        itemContentValues
                );

                //Checking the result URI
                if (itemInsertUri == null) {
                    //When Null, pass the error message
                    //Executing on Main Thread
                    mAppExecutors.getMainThread().execute(() -> operationsCallback.onFailure(R.string.product_config_insert_item_error, newProduct.getName()));
                } else {
                    //When NOT Null, parse for the Item Id

                    //Parsing the Uri for the Item Id
                    int itemId = (int) ContentUris.parseId(itemInsertUri);

                    //bulk insert Product Attributes
                    int noOfProductAttrsInserted = 0;
                    int noOfProductAttrsPresent = newProduct.getProductAttributes().size();
                    if (noOfProductAttrsPresent > 0) {
                        noOfProductAttrsInserted = saveProductAttributes(itemId, newProduct.getProductAttributes());
                    }

                    //bulk insert Product Images
                    int noOfProductImagesInserted = 0;
                    int noOfProductImagesPresent = newProduct.getProductImages().size();
                    if (noOfProductImagesPresent > 0) {
                        noOfProductImagesInserted = saveProductImages(itemId, newProduct.getProductImages());
                    }

                    if (noOfProductAttrsPresent == noOfProductAttrsInserted
                            && noOfProductImagesPresent == noOfProductImagesInserted) {
                        //When the Product Hang off details were inserted successfully,
                        //call the operation as successful

                        //Executing on Main Thread
                        mAppExecutors.getMainThread().execute(operationsCallback::onSuccess);
                    } else {
                        //When the Product Hang off details were not inserted, pass the error message

                        //Executing on Main Thread
                        mAppExecutors.getMainThread().execute(() -> operationsCallback.onFailure(R.string.product_config_insert_item_addtnl_dtls_error, newProduct.getName(), newProduct.getSku()));
                    }
                }
            }

        });

    }

    /**
     * Method that updates an existing {@link Product} entry into the database table 'item'.
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

        //Get the Category Name
        String newCategoryName = newProduct.getCategory();

        //Check if the Category exists (Executes on Disk Thread)
        getCategoryByName(newCategoryName, new GetQueryCallback<Integer>() {
            /**
             * Method invoked when the results are obtained
             * for the query executed.
             *
             * @param categoryId The integer Category Id found for the Category Name queried
             */
            @MainThread
            @Override
            public void onResults(Integer categoryId) {
                //When Category was found

                //Propagating the result to #proceedToUpdateProduct
                //Executing on Disk Thread
                mAppExecutors.getDiskIO().execute(() -> proceedToUpdateProduct(categoryId));
            }

            /**
             * Method invoked when there are no results
             * for the query executed.
             */
            @MainThread
            @Override
            public void onEmpty() {
                //When Category does not exist, we insert the new Category to get the new record Id

                //Executing on Disk Thread
                mAppExecutors.getDiskIO().execute(() -> {
                    //Loading the Category Name for insert
                    ContentValues categoryContentValues = new ContentValues();
                    categoryContentValues.put(ProductContract.ProductCategory.COLUMN_ITEM_CATEGORY_NAME, newCategoryName);

                    //Executing insert
                    Uri categoryInsertUri = mContentResolver.insert(
                            ProductContract.ProductCategory.CONTENT_URI,
                            categoryContentValues
                    );

                    //Checking the result URI
                    if (categoryInsertUri == null) {
                        //When Null, pass the error message
                        //Executing on Main Thread
                        mAppExecutors.getMainThread().execute(() -> operationsCallback.onFailure(R.string.product_config_update_category_error, existingProduct.getSku(), newCategoryName));
                    } else {
                        //When NOT Null, parse for the Category Id

                        //Parsing the Category Id and propagating the result to #proceedToUpdateProduct
                        proceedToUpdateProduct((int) ContentUris.parseId(categoryInsertUri));
                    }
                });
            }

            /**
             * Method invoked when the results could not be retrieved
             * for the query due to some error.
             *
             * @param messageId The String resource of the error message
             *                  for the query execution failure
             * @param args      Variable number of arguments to replace the format specifiers
             */
            @MainThread
            @Override
            public void onFailure(int messageId, @Nullable Object... args) {
                //Pass the Failure to the callback
                operationsCallback.onFailure(messageId, args);
            }

            /**
             * Method that updates the existing Product details after the {@code categoryId}
             * for the Product's category was determined.
             *
             * @param categoryId The Id of the Category determined
             */
            @WorkerThread
            private void proceedToUpdateProduct(final int categoryId) {
                //Update the Product Hang off details by inserting them completely
                //(The Content Provider deletes any existing data before inserting new data)

                //Get the Item Id to update
                int itemId = existingProduct.getId();

                //bulk insert Product Attributes
                int noOfProductAttrsInserted = 0;
                int noOfProductAttrsPresent = newProduct.getProductAttributes().size();
                if (noOfProductAttrsPresent > 0) {
                    noOfProductAttrsInserted = saveProductAttributes(itemId, newProduct.getProductAttributes());
                } else {
                    mContentResolver.delete(
                            ContentUris.withAppendedId(ProductContract.ProductAttribute.CONTENT_URI, existingProduct.getId()),
                            null,
                            null
                    );
                }

                //bulk insert Product Images
                int noOfProductImagesInserted = 0;
                int noOfProductImagesPresent = newProduct.getProductImages().size();
                if (noOfProductImagesPresent > 0) {
                    noOfProductImagesInserted = saveProductImages(itemId, newProduct.getProductImages());
                } else {
                    mContentResolver.delete(
                            ContentUris.withAppendedId(ProductContract.ProductImage.CONTENT_URI, existingProduct.getId()),
                            null,
                            null
                    );
                }

                if (noOfProductAttrsPresent == noOfProductAttrsInserted
                        && noOfProductImagesPresent == noOfProductImagesInserted) {
                    //When the Product Hang off details were inserted successfully,
                    //begin with the Product update

                    //Compare the product details for update
                    boolean isNameChanged = !newProduct.getName().equals(existingProduct.getName());
                    boolean isSkuChanged = !newProduct.getSku().equals(existingProduct.getSku());
                    boolean isDescriptionChanged = !newProduct.getDescription().equals(existingProduct.getDescription());
                    boolean isCategoryChanged = !newProduct.getCategory().equals(existingProduct.getCategory());

                    if (isNameChanged || isSkuChanged || isDescriptionChanged || isCategoryChanged) {
                        //If any of the product details are changed, then we need to update the Product

                        //Loading the values for 'item' table
                        ContentValues itemContentValues = new ContentValues();
                        itemContentValues.put(ProductContract.Product.COLUMN_ITEM_NAME, newProduct.getName());
                        itemContentValues.put(ProductContract.Product.COLUMN_ITEM_SKU, newProduct.getSku());
                        itemContentValues.put(ProductContract.Product.COLUMN_ITEM_DESCRIPTION, newProduct.getDescription());
                        itemContentValues.put(ProductContract.Product.COLUMN_ITEM_CATEGORY_ID, categoryId);

                        Uri contentUri = ContentUris.withAppendedId(ProductContract.Product.CONTENT_URI, itemId);

                        //Executing update
                        int noOfItemRecordsUpdated = mContentResolver.update(
                                contentUri,
                                itemContentValues,
                                null,
                                null
                        );

                        //Checking the number of records updated
                        if (noOfItemRecordsUpdated == 0) {
                            //When 0 rows were affected, pass the error message
                            //Executing on Main Thread
                            mAppExecutors.getMainThread().execute(() -> operationsCallback.onFailure(R.string.product_config_update_item_error, existingProduct.getSku()));
                        } else if (noOfItemRecordsUpdated == 1) {
                            //When 1 row was affected, call the update operation as successful
                            //Executing on Main Thread
                            mAppExecutors.getMainThread().execute(operationsCallback::onSuccess);
                        } else if (noOfItemRecordsUpdated > 1) {
                            //When more than 1 row were affected, pass the error message
                            //Executing on Main Thread
                            mAppExecutors.getMainThread().execute(() -> operationsCallback.onFailure(R.string.product_config_update_item_inconsistent_error, existingProduct.getSku()));
                        }
                    } else {
                        //When none of the Product details were changed, call the update operation as successful
                        //Executing on Main Thread
                        mAppExecutors.getMainThread().execute(operationsCallback::onSuccess);
                    }

                } else {
                    //When the Product Hang off details were not inserted, pass the error message
                    //Executing on Main Thread
                    mAppExecutors.getMainThread().execute(() -> operationsCallback.onFailure(R.string.product_config_update_item_addtnl_dtls_error, existingProduct.getSku()));
                }
            }
        });

    }

    /**
     * Method that saves the {@link ProductAttribute} details for the Product
     * identified by the {@code productId} into the database table 'item_attr'.
     *
     * @param productId         The Product Id for which the Product Attributes are to be added.
     * @param productAttributes The New {@link ProductAttribute} details to be added to the database
     * @return The Number of records inserted.
     */
    @WorkerThread
    private int saveProductAttributes(int productId, ArrayList<ProductAttribute> productAttributes) {
        //Number of Product Attributes to be inserted
        int noOfProductAttrsPresent = productAttributes.size();

        //Preparing an array of Content values for bulk insert
        ContentValues[] valuesArray = new ContentValues[noOfProductAttrsPresent];
        for (int index = 0; index < noOfProductAttrsPresent; index++) {
            ProductAttribute productAttribute = productAttributes.get(index);
            ContentValues contentValues = new ContentValues();
            contentValues.put(ProductContract.ProductAttribute.COLUMN_ITEM_ATTR_NAME, productAttribute.getAttributeName());
            contentValues.put(ProductContract.ProductAttribute.COLUMN_ITEM_ATTR_VALUE, productAttribute.getAttributeValue());
            valuesArray[index] = contentValues;
        }

        //Executing the bulk insert
        return mContentResolver.bulkInsert(
                ContentUris.withAppendedId(ProductContract.ProductAttribute.CONTENT_URI, productId),
                valuesArray
        );
    }

    /**
     * Method that saves the {@link ProductImage} details for the Product
     * identified by the {@code productId} into the database table 'item_image'.
     *
     * @param productId     The Product Id for which the Product Images are to be added.
     * @param productImages The New {@link ProductImage} details to be added to the database
     * @return The Number of records inserted.
     */
    @WorkerThread
    private int saveProductImages(int productId, ArrayList<ProductImage> productImages) {
        //Number of Product Images to be inserted
        int noOfProductImagesPresent = productImages.size();

        //Preparing an array of Content values for bulk insert
        ContentValues[] valuesArray = new ContentValues[noOfProductImagesPresent];
        for (int index = 0; index < noOfProductImagesPresent; index++) {
            ProductImage productImage = productImages.get(index);
            ContentValues contentValues = new ContentValues();
            contentValues.put(ProductContract.ProductImage.COLUMN_ITEM_IMAGE_URI, productImage.getImageUri());
            contentValues.put(ProductContract.ProductImage.COLUMN_ITEM_IMAGE_DEFAULT,
                    productImage.isDefault() ? ProductContract.ProductImage.ITEM_IMAGE_DEFAULT : ProductContract.ProductImage.ITEM_IMAGE_NON_DEFAULT);
            valuesArray[index] = contentValues;
        }

        //Executing the bulk insert
        return mContentResolver.bulkInsert(
                ContentUris.withAppendedId(ProductContract.ProductImage.CONTENT_URI, productId),
                valuesArray
        );
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
     *                           receive the operation result.
     */
    @Override
    public void saveProductImages(@NonNull Product existingProduct, @NonNull ArrayList<ProductImage> productImages, @NonNull DataOperationsCallback operationsCallback) {
        //Executing on Disk Thread
        mAppExecutors.getDiskIO().execute(() -> {
            //Bulk insert Product Images
            int noOfProductImagesInserted = 0;
            int noOfProductImagesPresent = productImages.size();
            if (noOfProductImagesPresent > 0) {
                noOfProductImagesInserted = saveProductImages(existingProduct.getId(), productImages);
            } else {
                mContentResolver.delete(
                        ContentUris.withAppendedId(ProductContract.ProductImage.CONTENT_URI, existingProduct.getId()),
                        null,
                        null
                );
            }

            if (noOfProductImagesPresent == noOfProductImagesInserted) {
                //When all the Images information were updated successfully
                //Executing on Main Thread
                mAppExecutors.getMainThread().execute(operationsCallback::onSuccess);
            } else {
                //When the Images information were NOT updated successfully
                //Executing on Main Thread
                mAppExecutors.getMainThread().execute(() -> {
                    //Passing the error message to the callback
                    operationsCallback.onFailure(R.string.product_config_update_item_images_error, existingProduct.getSku());
                });
            }
        });
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
        //Executing on Disk Thread
        mAppExecutors.getDiskIO().execute(() -> {
            //Executing Product Deletion for the Product ID passed
            final int noOfRecordsDeleted = mContentResolver.delete(
                    ContentUris.withAppendedId(ProductContract.Product.CONTENT_URI, productId),
                    null,
                    null
            );

            //Executing on Main Thread
            mAppExecutors.getMainThread().execute(() -> {
                if (noOfRecordsDeleted > 0) {
                    //When some records were deleted successfully, call the delete operation as successful
                    operationsCallback.onSuccess();
                } else {
                    //When no record was deleted, pass the error message
                    operationsCallback.onFailure(R.string.product_config_delete_item_error, productId);
                }
            });
        });
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
        mContentResolver.registerContentObserver(uri, notifyForDescendants, observer);
    }

    /**
     * Method that unregisters a registered change observer {@link ContentObserver}.
     *
     * @param observer The previously registered {@link ContentObserver} that is no longer needed.
     */
    @Override
    public void unregisterContentObserver(@NonNull ContentObserver observer) {
        mContentResolver.unregisterContentObserver(observer);
    }

    /**
     * Method that retrieves the Supplier Details of Supplier identified by its Id.
     *
     * @param supplierId    The Integer Id of the Supplier to lookup for.
     * @param queryCallback The Callback to be implemented by the caller to receive the result.
     */
    @Override
    public void getSupplierDetailsById(int supplierId, @NonNull GetQueryCallback<Supplier> queryCallback) {
        //Retrieving the Supplier details for the Supplier ID passed
        //Executing on Disk Thread
        mAppExecutors.getDiskIO().execute(() -> {
            //Retrieving the cursor to the record
            Cursor cursor = mContentResolver.query(
                    ContentUris.withAppendedId(SupplierContract.Supplier.CONTENT_URI, supplierId),
                    QueryArgsUtility.SupplierByIdQuery.getProjection(),
                    null,
                    null,
                    null
            );

            //Retrieving the details from the Cursor
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    //When the Cursor is NOT Null and there is a record for the Supplier being queried

                    //Retrieving the details of the Supplier
                    String name = cursor.getString(QueryArgsUtility.SupplierByIdQuery.COLUMN_SUPPLIER_NAME_INDEX);
                    String code = cursor.getString(QueryArgsUtility.SupplierByIdQuery.COLUMN_SUPPLIER_CODE_INDEX);

                    //Retrieving the Supplier's Contacts
                    ArrayList<SupplierContact> supplierContacts = getSupplierContacts(supplierId);

                    //Retrieving the Supplier's Items
                    ArrayList<ProductSupplierInfo> productSupplierInfoList = getProductSupplierInfoList(supplierId);

                    //Building the Supplier details
                    final Supplier supplier = new Supplier.Builder()
                            .setId(supplierId)
                            .setName(name)
                            .setCode(code)
                            .setContacts(supplierContacts)
                            .setProductSupplierInfoList(productSupplierInfoList)
                            .createSupplier();

                    //Executing on the Main Thread
                    mAppExecutors.getMainThread().execute(() -> {
                        //Pass the Supplier data to the callback
                        queryCallback.onResults(supplier);
                    });

                } else {
                    //When Cursor is Null, the Supplier being queried is not found

                    //Pass the Supplier Not found error to the callback in this case.
                    queryCallback.onFailure(R.string.supplier_config_no_supplier_found_error, supplierId);
                }
            } finally {
                //Closing the cursor to release its resources
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        });
    }

    /**
     * Method that retrieves the Contacts of a Supplier identified by its Id.
     *
     * @param supplierId    The Integer Id of the Supplier to retrieve the List of Supplier's Contacts.
     * @param queryCallback The Callback to be implemented by the caller to receive the result.
     */
    @Override
    public void getSupplierContactsById(int supplierId, @NonNull GetQueryCallback<List<SupplierContact>> queryCallback) {
        //Retrieving the Supplier's contacts for the Supplier ID passed
        //Executing on Disk Thread
        mAppExecutors.getDiskIO().execute(() -> {
            //Retrieving the Supplier's Contacts
            ArrayList<SupplierContact> supplierContacts = getSupplierContacts(supplierId);

            if (supplierContacts != null && supplierContacts.size() > 0) {
                //When we have the Supplier's Contacts, pass the data to the callback
                //Executing on the Main Thread
                mAppExecutors.getMainThread().execute(() -> queryCallback.onResults(supplierContacts));
            } else {
                //When we have no Contacts, pass the empty result to the callback
                //Executing on the Main Thread
                mAppExecutors.getMainThread().execute(queryCallback::onEmpty);
            }

        });
    }

    /**
     * Method that retrieves the list of {@link ProductSupplierInfo} for the Supplier identified by its id.
     * This is called on Disk Thread.
     *
     * @param supplierId The Integer Id of the Supplier to lookup for.
     * @return List of {@link ProductSupplierInfo} for the Supplier identified by its id.
     */
    @WorkerThread
    private ArrayList<ProductSupplierInfo> getProductSupplierInfoList(int supplierId) {
        //Retrieving the cursor to the records
        Cursor cursor = mContentResolver.query(
                ContentUris.withAppendedId(SalesContract.ProductSupplierInfo.CONTENT_URI_SUPPLIER_ITEMS, supplierId),
                QueryArgsUtility.SupplierItemsQuery.getProjection(),
                null,
                null,
                null
        );

        //Retrieving the Supplier Items from the Cursor
        ArrayList<ProductSupplierInfo> productSupplierInfoList = new ArrayList<>();
        try {
            if (cursor != null && cursor.getCount() > 0) {
                //When the query returned results
                while (cursor.moveToNext()) {
                    //Retrieving the values from the record pointed to by the Cursor
                    int itemId = cursor.getInt(QueryArgsUtility.SupplierItemsQuery.COLUMN_ITEM_ID_INDEX);
                    float unitPrice = cursor.getFloat(QueryArgsUtility.SupplierItemsQuery.COLUMN_ITEM_UNIT_PRICE_INDEX);

                    //Preparing the ProductSupplierInfo
                    ProductSupplierInfo productSupplierInfo = new ProductSupplierInfo.Builder()
                            .setItemId(itemId)
                            .setSupplierId(supplierId)
                            .setUnitPrice(unitPrice)
                            .createProductSupplierInfo();

                    //Adding to the list of ProductSupplierInfo
                    productSupplierInfoList.add(productSupplierInfo);
                }
            }
        } finally {
            //Closing the cursor to release its resources
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        //Returning the Supplier Items (ProductSupplierInfo) retrieved for the Supplier ID passed
        return productSupplierInfoList;
    }

    /**
     * Method that retrieves the list of {@link SupplierContact} for the Supplier identified by its id.
     * This is called on Disk Thread.
     *
     * @param supplierId The Integer Id of the Supplier to lookup for.
     * @return List of {@link SupplierContact} for the Supplier identified by its id.
     */
    @WorkerThread
    private ArrayList<SupplierContact> getSupplierContacts(int supplierId) {
        //Retrieving the cursor to the records
        Cursor cursor = mContentResolver.query(
                ContentUris.withAppendedId(SupplierContract.SupplierContact.CONTENT_URI, supplierId),
                QueryArgsUtility.SupplierContactsQuery.getProjection(),
                null,
                null,
                null
        );

        //Retrieving the Supplier Contacts from the Cursor
        ArrayList<SupplierContact> supplierContacts = new ArrayList<>();
        try {
            if (cursor != null && cursor.getCount() > 0) {
                //When the query returned results
                while (cursor.moveToNext()) {
                    //Retrieving the values from the record pointed to by the Cursor
                    int contactTypeId = cursor.getInt(QueryArgsUtility.SupplierContactsQuery.COLUMN_SUPPLIER_CONTACT_TYPE_ID_INDEX);
                    String contactValue = cursor.getString(QueryArgsUtility.SupplierContactsQuery.COLUMN_SUPPLIER_CONTACT_VALUE_INDEX);
                    int defaultContactIndex = cursor.getInt(QueryArgsUtility.SupplierContactsQuery.COLUMN_SUPPLIER_CONTACT_DEFAULT_INDEX);

                    //Preparing the SupplierContact
                    SupplierContact supplierContact = new SupplierContact.Builder()
                            .setType(SupplierContract.SupplierContactType.getPreloadedContactTypes()[contactTypeId])
                            .setValue(contactValue)
                            .setIsDefault(defaultContactIndex == SupplierContract.SupplierContact.SUPPLIER_CONTACT_DEFAULT)
                            .createSupplierContact();

                    //Adding to the list of SupplierContacts
                    supplierContacts.add(supplierContact);
                }
            }
        } finally {
            //Closing the cursor to release its resources
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        //Returning the Supplier Contacts retrieved for the Supplier ID passed
        return supplierContacts;
    }

    /**
     * Method that checks and validates the uniqueness of the Supplier Code {@code supplierCode} passed.
     *
     * @param supplierCode  The Supplier Code of the Supplier to lookup for.
     * @param queryCallback The Callback to be implemented by the caller to receive the result.
     */
    @Override
    public void getSupplierCodeUniqueness(@NonNull String supplierCode, @NonNull GetQueryCallback<Boolean> queryCallback) {
        //Executing on Disk Thread
        mAppExecutors.getDiskIO().execute(() -> {
            //Retrieving the cursor to the record
            Cursor cursor = mContentResolver.query(
                    SupplierContract.Supplier.buildSupplierCodeUri(supplierCode),
                    QueryArgsUtility.SupplierByCodeQuery.getProjection(),
                    null,
                    null,
                    null
            );

            //Retrieving Supplier from the cursor
            try {
                //Records the uniqueness result of the Supplier Code passed
                boolean isSupplierCodeUnique;

                if (cursor != null && cursor.moveToFirst()) {
                    //When cursor is NOT NULL

                    //Retrieving the Supplier Id for the Supplier Code
                    int supplierId = cursor.getInt(QueryArgsUtility.SupplierByCodeQuery.COLUMN_SUPPLIER_ID_INDEX);

                    //Supplier Code is unique if the Supplier Id detected is null or negative
                    isSupplierCodeUnique = (cursor.isNull(QueryArgsUtility.SupplierByCodeQuery.COLUMN_SUPPLIER_ID_INDEX)
                            || supplierId < 0);
                } else {
                    //When cursor is NULL

                    //Supplier Code is unique if the query resulted in no records found
                    isSupplierCodeUnique = true;
                }

                //Recording the final data to pass to the caller
                final Boolean isSupplierCodeUniqueFinal = isSupplierCodeUnique;
                //Executing on the Main Thread
                mAppExecutors.getMainThread().execute(() -> {
                    //Passing the result evaluated to the caller
                    queryCallback.onResults(isSupplierCodeUniqueFinal);
                });

            } finally {
                //Closing the cursor to release its resources
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        });
    }

    /**
     * Method that retrieves short information of the Products identified by its Ids {@code productIds}
     *
     * @param productIds    List of Ids of the Products whose information is required. When {@code null},
     *                      information for all the Products in the database is retrieved.
     * @param queryCallback The Callback to be implemented by the caller to receive the result.
     */
    @Override
    public void getShortProductInfoForProducts(@Nullable List<String> productIds,
                                               @NonNull GetQueryCallback<List<ProductLite>> queryCallback) {
        //Executing on the Disk Thread
        mAppExecutors.getDiskIO().execute(() -> {
            //Building the Selection and its Arguments Pair for the list of Products passed
            Pair<String, String[]> selectionPairs = SqliteUtility.makeSelectionForInClause(
                    ProductContract.Product.getQualifiedColumnName(ProductContract.Product._ID),
                    productIds
            );

            //Retrieving the Cursor to the records
            Cursor cursor = mContentResolver.query(
                    ProductContract.Product.CONTENT_URI_SHORT_INFO,
                    QueryArgsUtility.ItemsShortInfoQuery.getProjection(),
                    selectionPairs == null ? null : selectionPairs.first,
                    selectionPairs == null ? null : selectionPairs.second,
                    ProductContract.Product.getQualifiedColumnName(ProductContract.Product.COLUMN_ITEM_SKU)
            );

            //Retrieving Products from the cursor
            ArrayList<ProductLite> productList = new ArrayList<>();
            try {
                if (cursor != null && cursor.getCount() > 0) {
                    //When the query returned results
                    while (cursor.moveToNext()) {
                        //Retrieving the values from the record pointed to by the Cursor
                        ProductLite productLite = ProductLite.from(cursor);
                        //Adding to the list of Products
                        productList.add(productLite);
                    }
                }

            } finally {
                //Closing the cursor to release its resources
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }

            //Executing on the Main Thread
            mAppExecutors.getMainThread().execute(() -> {
                if (productList.size() > 0) {
                    //Pass the results to the callback
                    queryCallback.onResults(productList);
                } else {
                    //Return to the caller when there are no results
                    queryCallback.onEmpty();
                }
            });

        });
    }

    /**
     * Method that adds a new {@link Supplier} entry into the database.
     *
     * @param newSupplier        The new {@link Supplier} to be added to the database.
     * @param operationsCallback The Callback to be implemented by the caller to
     */
    @Override
    public void saveNewSupplier(@NonNull Supplier newSupplier, @NonNull DataOperationsCallback operationsCallback) {
        //Executing on Disk Thread
        mAppExecutors.getDiskIO().execute(() -> {
            //Loading values for 'supplier' table
            ContentValues supplierContentValues = new ContentValues();
            supplierContentValues.put(SupplierContract.Supplier.COLUMN_SUPPLIER_NAME, newSupplier.getName());
            supplierContentValues.put(SupplierContract.Supplier.COLUMN_SUPPLIER_CODE, newSupplier.getCode());

            //Executing insert
            Uri supplierInsertUri = mContentResolver.insert(
                    SupplierContract.Supplier.CONTENT_URI,
                    supplierContentValues
            );

            //Checking the result URI
            if (supplierInsertUri == null) {
                //When Null, pass the error message
                //Executing on Main Thread
                mAppExecutors.getMainThread().execute(() -> operationsCallback.onFailure(R.string.supplier_config_insert_supplier_error, newSupplier.getName()));
            } else {
                //When NOT Null, parse for the Supplier Id

                //Parsing the Uri for the Supplier Id
                int supplierId = (int) ContentUris.parseId(supplierInsertUri);

                //After retrieving the Supplier Id, insert other relationship data

                //Bulk insert Supplier Contacts
                int noOfSupplierContactsInserted = 0;
                int noOfSupplierContactsPresent = newSupplier.getContacts().size();
                if (noOfSupplierContactsPresent > 0) {
                    noOfSupplierContactsInserted = saveSupplierContacts(supplierId, newSupplier.getContacts());
                }

                //Bulk insert Supplier Items and their Price info
                int noOfSupplierItemsInserted = 0;
                int noOfSupplierItemsPresent = newSupplier.getProductSupplierInfoList().size();
                if (noOfSupplierItemsPresent > 0) {
                    noOfSupplierItemsInserted = saveSupplierItems(supplierId, newSupplier.getProductSupplierInfoList());
                }

                //Bulk insert Zero inventory for the Supplier Items
                int noOfSupplierItemsInventoryInserted = 0;
                if (noOfSupplierItemsPresent > 0) {
                    noOfSupplierItemsInventoryInserted = insertZeroSupplierInventoryForItems(supplierId, newSupplier.getProductSupplierInfoList());
                }

                if (noOfSupplierContactsInserted == noOfSupplierContactsPresent
                        && noOfSupplierItemsInserted == noOfSupplierItemsPresent
                        && noOfSupplierItemsInventoryInserted == noOfSupplierItemsPresent) {
                    //When the Hang off details were inserted successfully,
                    //call the operation as successful

                    //Executing on Main Thread
                    mAppExecutors.getMainThread().execute(operationsCallback::onSuccess);
                } else {
                    //When the Hang off details were not inserted, pass the error message

                    //Executing on Main Thread
                    mAppExecutors.getMainThread().execute(() -> operationsCallback.onFailure(R.string.supplier_config_insert_supplier_addtnl_dtls_error, newSupplier.getName(), newSupplier.getCode()));
                }
            }
        });
    }

    /**
     * Method that saves the Supplier's Contacts {@link SupplierContact} details
     * of the Supplier identified by the {@code supplierId} into the database table 'supplier_contact'.
     *
     * @param supplierId The Supplier Id for which the Contacts are to be added.
     * @param contacts   The Supplier's Contacts {@link SupplierContact} to be added/updated to the database.
     * @return The Number of records inserted.
     */
    @WorkerThread
    private int saveSupplierContacts(int supplierId, ArrayList<SupplierContact> contacts) {
        //Number of Supplier Contacts to be inserted
        int noOfSupplierContactsPresent = contacts.size();

        //Preparing an array of Content values for bulk insert
        ContentValues[] valuesArray = new ContentValues[noOfSupplierContactsPresent];
        for (int index = 0; index < noOfSupplierContactsPresent; index++) {
            //Retrieving the SupplierContact at the index
            SupplierContact supplierContact = contacts.get(index);
            //Building the ContentValue for the SupplierContact
            ContentValues contentValues = new ContentValues();
            contentValues.put(SupplierContract.SupplierContact.COLUMN_SUPPLIER_CONTACT_TYPE_ID,
                    supplierContact.getType().equals(
                            SupplierContract.SupplierContactType.CONTACT_TYPE_PHONE) ?
                            SupplierContract.SupplierContactType.CONTACT_TYPE_ID_PHONE :
                            SupplierContract.SupplierContactType.CONTACT_TYPE_ID_EMAIL);
            contentValues.put(SupplierContract.SupplierContact.COLUMN_SUPPLIER_CONTACT_VALUE,
                    supplierContact.getValue());
            contentValues.put(SupplierContract.SupplierContact.COLUMN_SUPPLIER_CONTACT_DEFAULT,
                    supplierContact.isDefault() ?
                            SupplierContract.SupplierContact.SUPPLIER_CONTACT_DEFAULT :
                            SupplierContract.SupplierContact.SUPPLIER_CONTACT_NON_DEFAULT);
            //Loading into the array of ContentValues
            valuesArray[index] = contentValues;
        }

        //Executing the bulk insert
        return mContentResolver.bulkInsert(
                ContentUris.withAppendedId(SupplierContract.SupplierContact.CONTENT_URI, supplierId),
                valuesArray
        );
    }

    /**
     * Method that saves the Supplier's Items with their Price info {@link ProductSupplierInfo}
     * into the database table 'item_supplier_info'.
     *
     * @param supplierId              THe Supplier Id for which the Items with Price info are to be added/updated.
     * @param productSupplierInfoList List of Supplier's Items with their Price info
     *                                {@link ProductSupplierInfo} to be added/updated to the database.
     * @return The Number of records inserted.
     */
    @WorkerThread
    private int saveSupplierItems(int supplierId, ArrayList<ProductSupplierInfo> productSupplierInfoList) {
        //Number of Supplier Items to be inserted
        int noOfSupplierItemsPresent = productSupplierInfoList.size();

        //Preparing an array of Content values for bulk insert
        ContentValues[] valuesArray = new ContentValues[noOfSupplierItemsPresent];
        for (int index = 0; index < noOfSupplierItemsPresent; index++) {
            //Retrieving the ProductSupplierInfo at the index
            ProductSupplierInfo productSupplierInfo = productSupplierInfoList.get(index);
            //Building Content Value for the ProductSupplierInfo
            ContentValues contentValues = new ContentValues();
            contentValues.put(SalesContract.ProductSupplierInfo.COLUMN_ITEM_ID, productSupplierInfo.getItemId());
            contentValues.put(SalesContract.ProductSupplierInfo.COLUMN_ITEM_UNIT_PRICE, productSupplierInfo.getUnitPrice());
            //Loading into the array of ContentValues
            valuesArray[index] = contentValues;
        }

        //Executing the bulk insert
        return mContentResolver.bulkInsert(
                ContentUris.withAppendedId(SalesContract.ProductSupplierInfo.CONTENT_URI_SUPPLIER_ITEMS, supplierId),
                valuesArray
        );
    }

    /**
     * Method that inserts Zero Inventory record for the Supplier's Items {@link ProductSupplierInfo}
     * into the database table 'item_supplier_inventory'.
     *
     * @param supplierId              The Supplier Id of the Supplier whose items inventory are to be inserted with 0.
     * @param productSupplierInfoList List of Supplier's Items {@link ProductSupplierInfo}
     *                                whose inventory record are to be inserted with 0.
     * @return The Number of records inserted.
     */
    @WorkerThread
    private int insertZeroSupplierInventoryForItems(int supplierId, ArrayList<ProductSupplierInfo> productSupplierInfoList) {
        //Number of Supplier Items inventory to be inserted
        int noOfSupplierItemsPresent = productSupplierInfoList.size();

        //Preparing an array of Content values for bulk insert
        ContentValues[] valuesArray = new ContentValues[noOfSupplierItemsPresent];
        for (int index = 0; index < noOfSupplierItemsPresent; index++) {
            //Retrieving the ProductSupplierInfo at the index
            ProductSupplierInfo productSupplierInfo = productSupplierInfoList.get(index);
            //Building Content Value for the ProductSupplierInfo data
            ContentValues contentValues = new ContentValues();
            contentValues.put(SalesContract.ProductSupplierInventory.COLUMN_ITEM_ID, productSupplierInfo.getItemId());
            //Loading into the array of ContentValues
            valuesArray[index] = contentValues;
        }

        //Executing the bulk insert
        return mContentResolver.bulkInsert(
                ContentUris.withAppendedId(SalesContract.ProductSupplierInventory.CONTENT_URI_INV_SUPPLIER, supplierId),
                valuesArray
        );
    }

    /**
     * Method that saves/updates the Item's Inventory provided by its Suppliers {@link ProductSupplierSales}
     * into the database table 'item_supplier_inventory'.
     *
     * @param productId                The Product Id of the Product whose inventory is to be inserted/updated.
     * @param productSupplierSalesList List of the Product's Suppliers inventory{@link ProductSupplierSales}
     *                                 to be added/updated to the database.
     * @return The Number of inventory records inserted for the Item {@code productId}
     */
    @WorkerThread
    private int saveItemSuppliersInventory(int productId, List<ProductSupplierSales> productSupplierSalesList) {
        //Number of Suppliers Inventory records to be inserted for the Item
        int noOfItemSuppliersInventoryPresent = productSupplierSalesList.size();

        //Preparing an array of Content values for bulk insert
        ContentValues[] valuesArray = new ContentValues[noOfItemSuppliersInventoryPresent];
        for (int index = 0; index < noOfItemSuppliersInventoryPresent; index++) {
            //Retrieving the ProductSupplierSales at the index
            ProductSupplierSales productSupplierSales = productSupplierSalesList.get(index);
            //Building Content Value for the ProductSupplierSales data
            ContentValues contentValues = new ContentValues();
            contentValues.put(SalesContract.ProductSupplierInventory.COLUMN_SUPPLIER_ID, productSupplierSales.getSupplierId());
            contentValues.put(SalesContract.ProductSupplierInventory.COLUMN_ITEM_AVAIL_QUANTITY, productSupplierSales.getAvailableQuantity());
            //Loading into the array of ContentValues
            valuesArray[index] = contentValues;
        }

        //Executing the bulk insert
        return mContentResolver.bulkInsert(
                ContentUris.withAppendedId(SalesContract.ProductSupplierInventory.CONTENT_URI_INV_ITEM, productId),
                valuesArray
        );
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
    public void saveUpdatedSupplier(@NonNull Supplier existingSupplier,
                                    @NonNull Supplier newSupplier,
                                    @NonNull DataOperationsCallback operationsCallback) {
        //Executing on Disk Thread
        mAppExecutors.getDiskIO().execute(() -> {
            //Get the Supplier Id to update
            int supplierId = existingSupplier.getId();

            //Retrieving the Supplier Contacts
            ArrayList<SupplierContact> existingSupplierContacts = existingSupplier.getContacts();
            ArrayList<SupplierContact> newSupplierContacts = newSupplier.getContacts();

            //Evaluating for the Supplier Contacts that were removed in order to issue delete
            //Adding all existing first
            ArrayList<SupplierContact> removedSupplierContacts = new ArrayList<>(existingSupplierContacts);
            //Removing what is present in the new list
            removeSimilarSupplierContacts(removedSupplierContacts, newSupplierContacts);

            //Stores the number of Contacts deleted
            int noOfSupplierContactsDeleted = 0;
            //Number of Contacts to be deleted
            int noOfSupplierContactsToDelete = removedSupplierContacts.size();
            if (noOfSupplierContactsToDelete > 0) {
                //When there are contacts to be deleted, delete them
                noOfSupplierContactsDeleted = deleteSupplierContacts(supplierId, removedSupplierContacts);
            }

            //Stores the number of Contacts inserted
            int noOfSupplierContactsInserted = 0;
            int noOfSupplierContactsToInsert = newSupplierContacts.size();
            if (noOfSupplierContactsToInsert > 0) {
                //When there are Contacts to be inserted, bulk insert the contacts
                //(Existing Contacts data will be updated because of CONFLICT REPLACE Strategy)
                noOfSupplierContactsInserted = saveSupplierContacts(supplierId, newSupplierContacts);
            }

            //Retrieving the Supplier Items with their Price info
            ArrayList<ProductSupplierInfo> existingProductSupplierInfoList = existingSupplier.getProductSupplierInfoList();
            ArrayList<ProductSupplierInfo> newProductSupplierInfoList = newSupplier.getProductSupplierInfoList();

            //Evaluating the Supplier Items that were removed in order to issue delete
            //Adding all existing Supplier Items first
            ArrayList<ProductSupplierInfo> removedProductSupplierInfoList = new ArrayList<>(existingProductSupplierInfoList);
            //Removing what is present in the new list
            removeSimilarProductSupplierInfo(removedProductSupplierInfoList, newProductSupplierInfoList);

            //Stores the number of Supplier Items removed from the Supplier
            int noOfSupplierItemsRemoved = 0;
            int noOfSupplierItemsToRemove = removedProductSupplierInfoList.size();
            if (noOfSupplierItemsToRemove > 0) {
                //When there are Supplier Items to be removed, unlink them from the Supplier
                noOfSupplierItemsRemoved = unlinkSupplierItems(supplierId, removedProductSupplierInfoList);
            }

            //Stores the number of Supplier Items inventory removed from the Supplier
            int noOfSupplierItemsInventoryRemoved = 0;
            if (noOfSupplierItemsToRemove > 0) {
                //When there are Supplier Items to be removed, unlink their inventory from the Supplier
                noOfSupplierItemsInventoryRemoved = unlinkSupplierItemsInventory(supplierId, removedProductSupplierInfoList);
            }

            //Evaluating the Supplier Items that were newly added in order to insert Zero Inventory record
            //Adding all new Supplier Items first
            ArrayList<ProductSupplierInfo> addedProductSupplierInfoList = new ArrayList<>(newProductSupplierInfoList);
            //Removing what is present in the existing list
            removeSimilarProductSupplierInfo(addedProductSupplierInfoList, existingProductSupplierInfoList);

            //Stores the number of Supplier Items inventory added to the Supplier
            int noOfSupplierItemsInventoryInserted = 0;
            int noOfSupplierItemsInventoryToInsert = addedProductSupplierInfoList.size();
            if (noOfSupplierItemsInventoryToInsert > 0) {
                //When there are new Items for the Supplier added, insert the Zero Inventory record for those Items
                noOfSupplierItemsInventoryInserted = insertZeroSupplierInventoryForItems(supplierId, addedProductSupplierInfoList);
            }

            //Stores the number of Supplier Items inserted
            int noOfSupplierItemsInserted = 0;
            int noOfSupplierItemsToInsert = newProductSupplierInfoList.size();
            if (noOfSupplierItemsToInsert > 0) {
                //When there are Supplier Items to be inserted, bulk insert the Supplier Items
                //(Existing Supplier Items data will be updated because of CONFLICT REPLACE Strategy)
                noOfSupplierItemsInserted = saveSupplierItems(supplierId, newProductSupplierInfoList);
            }

            if (noOfSupplierContactsDeleted == noOfSupplierContactsToDelete
                    && noOfSupplierContactsInserted == noOfSupplierContactsToInsert
                    && noOfSupplierItemsRemoved == noOfSupplierItemsToRemove
                    && noOfSupplierItemsInventoryRemoved == noOfSupplierItemsToRemove
                    && noOfSupplierItemsInventoryInserted == noOfSupplierItemsInventoryToInsert
                    && noOfSupplierItemsInserted == noOfSupplierItemsToInsert) {
                //When the Hang off details were inserted/deleted successfully,
                //begin with the Supplier update

                //Compare the Supplier details for Update
                boolean isNameChanged = !newSupplier.getName().equals(existingSupplier.getName());
                boolean isCodeChanged = !newSupplier.getCode().equals(existingSupplier.getCode());

                if (isNameChanged || isCodeChanged) {
                    //If any of the Supplier details are changed, then we need to update the Supplier

                    //Loading the values for the 'supplier' table update
                    ContentValues supplierContentValues = new ContentValues();
                    supplierContentValues.put(SupplierContract.Supplier.COLUMN_SUPPLIER_NAME, newSupplier.getName());
                    supplierContentValues.put(SupplierContract.Supplier.COLUMN_SUPPLIER_CODE, newSupplier.getCode());

                    //Executing update
                    int noOfSupplierRecordsUpdated = mContentResolver.update(
                            ContentUris.withAppendedId(SupplierContract.Supplier.CONTENT_URI, supplierId),
                            supplierContentValues,
                            null,
                            null
                    );

                    //Checking the number of records updated
                    if (noOfSupplierRecordsUpdated == 0) {
                        //When 0 rows were affected, pass the error message
                        //Executing on Main Thread
                        mAppExecutors.getMainThread().execute(() -> operationsCallback.onFailure(R.string.supplier_config_update_supplier_error, existingSupplier.getCode()));
                    } else if (noOfSupplierRecordsUpdated == 1) {
                        //When 1 row was affected, call the update operation as successful
                        //Executing on Main Thread
                        mAppExecutors.getMainThread().execute(operationsCallback::onSuccess);
                    } else if (noOfSupplierRecordsUpdated > 1) {
                        //When more than 1 row were affected, pass the error message
                        //Executing on Main Thread
                        mAppExecutors.getMainThread().execute(() -> operationsCallback.onFailure(R.string.supplier_config_update_supplier_inconsistent_error, existingSupplier.getCode()));
                    }

                } else {
                    //When none of the Supplier details were changed, call the update operation as successful
                    //Executing on Main Thread
                    mAppExecutors.getMainThread().execute(operationsCallback::onSuccess);
                }

            } else {
                //When the Hang off details were not inserted/deleted, pass the error message
                //Executing on Main Thread
                mAppExecutors.getMainThread().execute(() -> operationsCallback.onFailure(R.string.supplier_config_update_supplier_addtnl_dtls_error, existingSupplier.getCode()));
            }

        });

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
        //Executing on Disk Thread
        mAppExecutors.getDiskIO().execute(() -> {
            //Executing Supplier Deletion for the Supplier ID passed
            final int noOfRecordsDeleted = mContentResolver.delete(
                    ContentUris.withAppendedId(SupplierContract.Supplier.CONTENT_URI, supplierId),
                    null,
                    null
            );

            //Executing on Main Thread
            mAppExecutors.getMainThread().execute(() -> {
                if (noOfRecordsDeleted > 0) {
                    //When some records were deleted successfully, call the delete operation as successful
                    operationsCallback.onSuccess();
                } else {
                    //When no record was deleted, pass the error message
                    operationsCallback.onFailure(R.string.supplier_config_delete_supplier_error, supplierId);
                }
            });
        });
    }

    /**
     * Method that removes the Inventory records for the Items of the given Supplier identified by its Id.
     *
     * @param supplierId                     The Supplier Id of the Supplier whose Items inventory is to be removed
     * @param removedProductSupplierInfoList List of {@link ProductSupplierInfo} that contains the Item Ids
     *                                       of the Items whose Inventory records needs to be removed.
     * @return The number of Supplier Items inventory records removed
     */
    @WorkerThread
    private int unlinkSupplierItemsInventory(int supplierId, ArrayList<ProductSupplierInfo> removedProductSupplierInfoList) {
        //Stores the number of Supplier Items inventory removed from the Supplier
        int noOfSupplierItemsInventoryRemoved = 0;

        //Iterating over the Supplier Items to remove their inventory
        for (ProductSupplierInfo productSupplierInfo : removedProductSupplierInfoList) {
            //Specifying the WHERE Clause filter
            String selection = SalesContract.ProductSupplierInventory.COLUMN_ITEM_ID + EQUALS + PLACEHOLDER;
            //Specifying the values of the columns involved in the WHERE Clause filter
            String[] selectionArgs = new String[]{String.valueOf(productSupplierInfo.getItemId())};
            //Executing delete
            noOfSupplierItemsInventoryRemoved += mContentResolver.delete(
                    ContentUris.withAppendedId(SalesContract.ProductSupplierInventory.CONTENT_URI_INV_SUPPLIER, supplierId),
                    selection,
                    selectionArgs
            );
        }

        //Returning the number of Supplier Items inventory removed
        return noOfSupplierItemsInventoryRemoved;
    }

    /**
     * Method that removes the Inventory records for the Suppliers of the given Item identified by its Id.
     *
     * @param productId                       The Product Id of the Product whose inventory from the Suppliers are to be removed.
     * @param removedProductSupplierSalesList List of {@link ProductSupplierSales} that contains the Supplier Ids
     *                                        of the Suppliers whose Inventory records with the Item {@code productId}
     *                                        needs to be removed.
     * @return The number of Suppliers' inventory removed for the Item.
     */
    @WorkerThread
    private int unlinkItemSuppliersInventory(int productId, List<ProductSupplierSales> removedProductSupplierSalesList) {
        //Stores the number of Suppliers' inventory removed for the Item
        int noOfItemSuppliersInventoryRemoved = 0;

        //Iterating over the Item's Suppliers to remove their inventory
        for (ProductSupplierSales productSupplierSales : removedProductSupplierSalesList) {
            //Specifying the WHERE Clause filter
            String selection = SalesContract.ProductSupplierInventory.COLUMN_SUPPLIER_ID + EQUALS + PLACEHOLDER;
            //Specifying the values of the columns involved in the WHERE Clause filter
            String[] selectionArgs = new String[]{String.valueOf(productSupplierSales.getSupplierId())};
            //Executing delete
            noOfItemSuppliersInventoryRemoved += mContentResolver.delete(
                    ContentUris.withAppendedId(SalesContract.ProductSupplierInventory.CONTENT_URI_INV_ITEM, productId),
                    selection,
                    selectionArgs
            );
        }

        //Returning the number of Suppliers' inventory removed
        return noOfItemSuppliersInventoryRemoved;
    }

    /**
     * Method that removes the link between Supplier and its items for the
     * items mentioned in {@code removedProductSupplierInfoList}.
     *
     * @param supplierId                     The Supplier Id of the Supplier whose link with the items are to be removed
     * @param removedProductSupplierInfoList List of {@link ProductSupplierInfo} that contains
     *                                       the Item Ids of the Items for which the link needs to be removed.
     * @return The Number of Supplier Items removed.
     */
    @WorkerThread
    private int unlinkSupplierItems(int supplierId, ArrayList<ProductSupplierInfo> removedProductSupplierInfoList) {
        //Stores the number of Supplier Items removed from the Supplier
        int noOfSupplierItemsRemoved = 0;

        //Iterating over the Supplier Items to unlink them
        for (ProductSupplierInfo productSupplierInfo : removedProductSupplierInfoList) {
            //Specifying the WHERE Clause filter
            String selection = SalesContract.ProductSupplierInfo.COLUMN_ITEM_ID + EQUALS + PLACEHOLDER;
            //Specifying the values of the columns involved in the WHERE Clause filter
            String[] selectionArgs = new String[]{String.valueOf(productSupplierInfo.getItemId())};
            //Executing delete
            noOfSupplierItemsRemoved += mContentResolver.delete(
                    ContentUris.withAppendedId(SalesContract.ProductSupplierInfo.CONTENT_URI_SUPPLIER_ITEMS, supplierId),
                    selection,
                    selectionArgs
            );
        }

        //Returning the Number of Supplier Items removed
        return noOfSupplierItemsRemoved;
    }

    /**
     * Method that removes the link between Item and its Suppliers for the Suppliers
     * mentioned in {@code removedProductSupplierSalesList}.
     *
     * @param productId                       The Product Id of the Product whose link with the Suppliers are to be removed.
     * @param removedProductSupplierSalesList List of {@link ProductSupplierSales} that contains
     *                                        the Supplier Ids of the Suppliers for which the link needs to be removed.
     * @return The Number of Item's Suppliers removed.
     */
    @WorkerThread
    private int unlinkItemSuppliers(int productId, List<ProductSupplierSales> removedProductSupplierSalesList) {
        //Stores the number of Suppliers removed from the item
        int noOfItemSuppliersRemoved = 0;

        //Iterating over the Item's Suppliers to unlink them
        for (ProductSupplierSales productSupplierSales : removedProductSupplierSalesList) {
            //Specifying the WHERE Clause filter
            String selection = SalesContract.ProductSupplierInfo.COLUMN_SUPPLIER_ID + EQUALS + PLACEHOLDER;
            //Specifying the values of the columns involved in the WHERE Clause filter
            String[] selectionArgs = new String[]{String.valueOf(productSupplierSales.getSupplierId())};
            //Executing delete
            noOfItemSuppliersRemoved += mContentResolver.delete(
                    ContentUris.withAppendedId(SalesContract.ProductSupplierInfo.CONTENT_URI_ITEM_SUPPLIERS, productId),
                    selection,
                    selectionArgs
            );
        }

        //Returning the number of Item's Suppliers removed
        return noOfItemSuppliersRemoved;
    }

    /**
     * Method that deletes the Contacts of the given Supplier and Supplier Contact Values.
     *
     * @param supplierId              The Supplier Id of the Supplier whose contacts are to be deleted.
     * @param removedSupplierContacts List of {@link SupplierContact} that needs to be deleted.
     * @return The Number of Supplier Contacts deleted.
     */
    @WorkerThread
    private int deleteSupplierContacts(int supplierId, ArrayList<SupplierContact> removedSupplierContacts) {
        //Stores the number of Records deleted
        int noOfRecordsDeleted = 0;

        //Iterating over the Supplier Contacts to delete them
        for (SupplierContact supplierContact : removedSupplierContacts) {
            //Specifying the WHERE Clause filter
            String selection = SupplierContract.SupplierContact.COLUMN_SUPPLIER_CONTACT_TYPE_ID + EQUALS + PLACEHOLDER
                    + AND + SupplierContract.SupplierContact.COLUMN_SUPPLIER_CONTACT_VALUE + EQUALS + PLACEHOLDER;
            //Retrieving the Contact Type Id
            int contactTypeId = supplierContact.getType().equals(
                    SupplierContract.SupplierContactType.CONTACT_TYPE_PHONE) ?
                    SupplierContract.SupplierContactType.CONTACT_TYPE_ID_PHONE :
                    SupplierContract.SupplierContactType.CONTACT_TYPE_ID_EMAIL;
            //Specifying the values of the columns involved in the WHERE Clause filter
            String[] selectionArgs = new String[]{String.valueOf(contactTypeId), supplierContact.getValue()};
            //Executing delete
            noOfRecordsDeleted += mContentResolver.delete(
                    ContentUris.withAppendedId(SupplierContract.SupplierContact.CONTENT_URI, supplierId),
                    selection,
                    selectionArgs
            );
        }

        //Returning the Number of Supplier Contacts deleted
        return noOfRecordsDeleted;
    }

    /**
     * Method that removes similar {@link SupplierContact} from {@code sourceSupplierContacts}
     * by comparing only the contact value.
     *
     * @param sourceSupplierContacts   The Source List of {@link SupplierContact} to remove from.
     * @param supplierContactsToRemove The List of {@link SupplierContact} to be removed.
     */
    @WorkerThread
    private void removeSimilarSupplierContacts(ArrayList<SupplierContact> sourceSupplierContacts,
                                               ArrayList<SupplierContact> supplierContactsToRemove) {
        //Creating a list to store the SupplierContact Values that needs to be removed
        ArrayList<String> contactsToRemove = new ArrayList<>();
        for (SupplierContact supplierContact : supplierContactsToRemove) {
            contactsToRemove.add(supplierContact.getValue());
        }

        //Iterating over the source list to find and remove the Supplier Contact
        Iterator<SupplierContact> sourceSupplierContactsIterator = sourceSupplierContacts.iterator();
        while (sourceSupplierContactsIterator.hasNext()) {
            //Retrieving the SupplierContact from the source list
            SupplierContact supplierContact = sourceSupplierContactsIterator.next();
            if (contactsToRemove.contains(supplierContact.getValue())) {
                //When the Contact value is present in the list to be removed, then remove from the source
                sourceSupplierContactsIterator.remove();
            }
        }
    }

    /**
     * Method that removes similar {@link ProductSupplierInfo} from {@code sourceProductSupplierInfoList}
     * by comparing the ItemId-SupplierId pair.
     *
     * @param sourceProductSupplierInfoList   The Source List of {@link ProductSupplierInfo} to remove from.
     * @param productSupplierInfoListToRemove The List of {@link ProductSupplierInfo} to be removed.
     */
    @WorkerThread
    private void removeSimilarProductSupplierInfo(ArrayList<ProductSupplierInfo> sourceProductSupplierInfoList,
                                                  ArrayList<ProductSupplierInfo> productSupplierInfoListToRemove) {
        //Creating a List to store the Pairs of ItemId-SupplierId for the list of ProductSupplierInfos to be removed.
        ArrayList<Pair<Integer, Integer>> productSupplierPairsToRemove = new ArrayList<>();
        for (ProductSupplierInfo productSupplierInfo : productSupplierInfoListToRemove) {
            productSupplierPairsToRemove.add(Pair.create(productSupplierInfo.getItemId(), productSupplierInfo.getSupplierId()));
        }

        //Iterating over the source list to find and remove the ProductSupplierInfos
        Iterator<ProductSupplierInfo> sourceProductSupplierInfoIterator = sourceProductSupplierInfoList.iterator();
        while (sourceProductSupplierInfoIterator.hasNext()) {
            //Retrieving the ProductSupplierInfo from the source list
            ProductSupplierInfo productSupplierInfo = sourceProductSupplierInfoIterator.next();
            //Creating the ItemId-SupplierId pair for the current ProductSupplierInfo read
            Pair<Integer, Integer> productSupplierPair = Pair.create(productSupplierInfo.getItemId(), productSupplierInfo.getSupplierId());
            if (productSupplierPairsToRemove.contains(productSupplierPair)) {
                //If the Pair exists in the list of ItemId-SupplierId pairs to be removed, then remove from the source
                sourceProductSupplierInfoIterator.remove();
            }
        }
    }

    /**
     * Method that removes similar {@link ProductSupplierSales} from {@code sourceProductSupplierSalesList}
     * by comparing the ItemId-SupplierId pair.
     *
     * @param sourceProductSupplierSalesList   The Source List of {@link ProductSupplierSales} to remove from.
     * @param productSupplierSalesListToRemove The List of {@link ProductSupplierSales} to be removed.
     */
    @WorkerThread
    private void removeSimilarProductSupplierSales(List<ProductSupplierSales> sourceProductSupplierSalesList,
                                                   List<ProductSupplierSales> productSupplierSalesListToRemove) {
        //Creating a List to store the Pairs of ItemId-SupplierId for the list of ProductSupplierSales to be removed.
        ArrayList<Pair<Integer, Integer>> productSupplierPairsToRemove = new ArrayList<>();
        for (ProductSupplierSales productSupplierSales : productSupplierSalesListToRemove) {
            productSupplierPairsToRemove.add(Pair.create(productSupplierSales.getItemId(), productSupplierSales.getSupplierId()));
        }

        //Iterating over the source list to find and remove the ProductSupplierSales
        Iterator<ProductSupplierSales> sourceProductSupplierSalesIterator = sourceProductSupplierSalesList.iterator();
        while (sourceProductSupplierSalesIterator.hasNext()) {
            //Retrieving the ProductSupplierSales from the source list
            ProductSupplierSales productSupplierSales = sourceProductSupplierSalesIterator.next();
            //Creating the ItemId-SupplierId pair for the current ProductSupplierSales read
            Pair<Integer, Integer> productSupplierPair = Pair.create(productSupplierSales.getItemId(), productSupplierSales.getSupplierId());
            if (productSupplierPairsToRemove.contains(productSupplierPair)) {
                //If the Pair exists in the list of ItemId-SupplierId pairs to be removed, then remove from the source
                sourceProductSupplierSalesIterator.remove();
            }
        }
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
        //Executing on Disk Thread
        mAppExecutors.getDiskIO().execute(() -> {
            //Preparing the ContentValue to update the available quantity
            ContentValues contentValues = new ContentValues();
            contentValues.put(SalesContract.ProductSupplierInventory.COLUMN_ITEM_AVAIL_QUANTITY, availableQuantity - decreaseQuantityBy);

            //Preparing the WHERE Clause to pass the Supplier Id
            String selection = SalesContract.ProductSupplierInventory.COLUMN_SUPPLIER_ID + EQUALS + PLACEHOLDER;
            String[] selectionArgs = new String[]{String.valueOf(supplierId)};

            //Executing update for the Product Id passed
            final int noOfRecordsUpdated = mContentResolver.update(
                    ContentUris.withAppendedId(SalesContract.ProductSupplierInventory.CONTENT_URI_INV_ITEM, productId),
                    contentValues,
                    selection,
                    selectionArgs
            );

            //Executing on Main Thread
            mAppExecutors.getMainThread().execute(() -> {
                if (noOfRecordsUpdated > 0) {
                    //When some records were updated successfully, call the update operation as successful
                    operationsCallback.onSuccess();
                } else {
                    //When no record was updated, pass the error message
                    operationsCallback.onFailure(R.string.sales_list_item_decrease_availability_error, productSku, supplierCode);
                }
            });
        });
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
        //Executing on the Disk Thread
        mAppExecutors.getDiskIO().execute(() -> {
            //Retrieving the cursor to the records
            Cursor cursor = mContentResolver.query(
                    ContentUris.withAppendedId(SalesContract.ProductSupplierInventory.CONTENT_URI_INV_ITEM, productId),
                    QueryArgsUtility.ItemSuppliersSalesQuery.getProjection(),
                    null,
                    null,
                    SupplierContract.Supplier.getQualifiedColumnName(SupplierContract.Supplier.COLUMN_SUPPLIER_CODE)
            );

            //Retrieving List of ProductSupplierSales from the Cursor
            ArrayList<ProductSupplierSales> productSupplierSalesList = new ArrayList<>();
            try {
                if (cursor != null && cursor.getCount() > 0) {
                    //When the query returned results
                    while (cursor.moveToNext()) {
                        //Preparing the ProductSupplierSales from the record pointed to by the Cursor
                        ProductSupplierSales productSupplierSales
                                = new ProductSupplierSales.Builder()
                                .setItemId(cursor.getInt(QueryArgsUtility.ItemSuppliersSalesQuery.COLUMN_ITEM_ID_INDEX))
                                .setSupplierId(cursor.getInt(QueryArgsUtility.ItemSuppliersSalesQuery.COLUMN_SUPPLIER_ID_INDEX))
                                .setSupplierName(cursor.getString(QueryArgsUtility.ItemSuppliersSalesQuery.COLUMN_SUPPLIER_NAME_INDEX))
                                .setSupplierCode(cursor.getString(QueryArgsUtility.ItemSuppliersSalesQuery.COLUMN_SUPPLIER_CODE_INDEX))
                                .setUnitPrice(cursor.getFloat(QueryArgsUtility.ItemSuppliersSalesQuery.COLUMN_ITEM_UNIT_PRICE_INDEX))
                                .setAvailableQuantity(cursor.getInt(QueryArgsUtility.ItemSuppliersSalesQuery.COLUMN_AVAIL_QUANTITY_INDEX))
                                .createProductSupplierSales();

                        //Adding to the list of ProductSupplierSales
                        productSupplierSalesList.add(productSupplierSales);
                    }
                }
            } finally {
                //Closing the cursor to release its resources
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }

            //Executing on the Main Thread
            mAppExecutors.getMainThread().execute(() -> {
                if (productSupplierSalesList.size() > 0) {
                    //Pass the results to the callback
                    queryCallback.onResults(productSupplierSalesList);
                } else {
                    //Return to the caller when there are no results
                    queryCallback.onEmpty();
                }
            });

        });
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
     *                                     receive the operation result.
     */
    @Override
    public void saveUpdatedProductSalesInfo(int productId, String productSku,
                                            @NonNull List<ProductSupplierSales> existingProductSupplierSales,
                                            @NonNull List<ProductSupplierSales> updatedProductSupplierSales,
                                            @NonNull DataOperationsCallback operationsCallback) {
        //Executing on Disk Thread
        mAppExecutors.getDiskIO().execute(() -> {
            //Evaluating the Item's Suppliers that were removed in order to
            //unlink the Item from the Supplier and also its Price and Inventory details
            //Adding the existing list first
            ArrayList<ProductSupplierSales> removedProductSupplierSalesList = new ArrayList<>(existingProductSupplierSales);
            //Removing what is present in the new updated list to find the Suppliers unlinked from the Item
            removeSimilarProductSupplierSales(removedProductSupplierSalesList, updatedProductSupplierSales);

            //Stores the number of Item's Suppliers removed
            int noOfItemSuppliersRemoved = 0;
            int noOfItemSuppliersToRemove = removedProductSupplierSalesList.size();
            if (noOfItemSuppliersToRemove > 0) {
                //When there are some Item's Suppliers removed, unlink from the Item
                noOfItemSuppliersRemoved = unlinkItemSuppliers(productId, removedProductSupplierSalesList);
            }

            //Stores the number of Item's Suppliers inventory removed
            int noOfItemSuppliersInventoryRemoved = 0;
            if (noOfItemSuppliersToRemove > 0) {
                //When there are some Item's Suppliers removed, remove their inventory records
                noOfItemSuppliersInventoryRemoved = unlinkItemSuppliersInventory(productId, removedProductSupplierSalesList);
            }

            //Stores the number of Item's Suppliers inventory records to be inserted/updated
            int noOfItemSuppliersInventoryInserted = 0;
            int noOfItemSuppliersInventoryToInsert = updatedProductSupplierSales.size();
            if (noOfItemSuppliersInventoryToInsert > 0) {
                //When there are Item's Suppliers inventory records, bulk insert to add/update them to the database
                //(Existing inventory records will be updated because of CONFLICT REPLACE Strategy)
                noOfItemSuppliersInventoryInserted = saveItemSuppliersInventory(productId, updatedProductSupplierSales);
            }

            //Evaluating the records inserted/deleted successfully
            if (noOfItemSuppliersRemoved == noOfItemSuppliersToRemove
                    && noOfItemSuppliersInventoryRemoved == noOfItemSuppliersToRemove
                    && noOfItemSuppliersInventoryInserted == noOfItemSuppliersInventoryToInsert) {
                //When all records have been deleted/inserted successfully
                //Call the operation as successful

                //Executing on Main Thread
                mAppExecutors.getMainThread().execute(operationsCallback::onSuccess);

            } else {
                //When some of the records were not deleted/inserted successfully
                //Pass the error message

                //Executing on Main Thread
                mAppExecutors.getMainThread().execute(() -> operationsCallback.onFailure(R.string.sales_config_inventory_update_error, productSku));
            }

        });

    }

}