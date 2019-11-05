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

package com.example.kaushiknsanji.storeapp.ui.inventory;

import android.content.Intent;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.DataRepository;
import com.example.kaushiknsanji.storeapp.data.StoreRepository;
import com.example.kaushiknsanji.storeapp.data.local.LoaderProvider;
import com.example.kaushiknsanji.storeapp.data.local.contracts.ProductContract;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SalesContract;
import com.example.kaushiknsanji.storeapp.data.local.contracts.StoreContract;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SupplierContract;
import com.example.kaushiknsanji.storeapp.data.local.models.SalesLite;
import com.example.kaushiknsanji.storeapp.ui.inventory.config.SalesConfigActivity;
import com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigActivity;
import com.example.kaushiknsanji.storeapp.utils.AppConstants;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Presenter Class that implements {@link SalesListContract.Presenter} on the lines of
 * Model-View-Presenter architecture. This Presenter interfaces with the App repository {@link StoreRepository}
 * and provides the list of Products with Sales information configured, to the View {@link SalesListFragment}
 * to load and display the same.
 *
 * @author Kaushik N Sanji
 */
public class SalesListPresenter implements SalesListContract.Presenter,
        LoaderManager.LoaderCallbacks<Cursor>, DataRepository.CursorDataLoaderCallback {

    //Constant used for logs
    private static final String LOG_TAG = SalesListPresenter.class.getSimpleName();
    //The Thread name of the Content Observer
    private static final String CONTENT_OBSERVER_THREAD_NAME = "ProductSalesContentObserverThread";
    //The View Interface of this Presenter
    @NonNull
    private final SalesListContract.View mSalesListView;
    //The LoaderProvider instance that provides the CursorLoader instance
    @NonNull
    private final LoaderProvider mLoaderProvider;
    //The LoaderManager instance
    @NonNull
    private final LoaderManager mLoaderManager;
    //Instance of the App Repository
    @NonNull
    private final StoreRepository mStoreRepository;
    //The Thread used by the Content Observer to observe and notify the changes
    private final HandlerThread mContentObserverHandlerThread;
    //Boolean to control multiple Content Observer notifications from being issued
    private final AtomicBoolean mDeliveredNotification = new AtomicBoolean(false);
    //The Content Observer to notify changes in the Product data
    private ProductSalesContentObserver mProductContentObserver;
    //The Content Observer to notify changes in the Supplier data
    private ProductSalesContentObserver mSupplierContentObserver;
    //The Content Observer to notify changes in the Price data
    private ProductSalesContentObserver mPriceContentObserver;
    //The Content Observer to notify changes in the Product Inventory
    private ProductSalesContentObserver mInventoryContentObserver;

    /**
     * Constructor of {@link SalesListPresenter}
     *
     * @param loaderProvider  Instance of {@link LoaderProvider} that provides the CursorLoader instance
     * @param loaderManager   Instance of {@link LoaderManager}
     * @param storeRepository Instance of {@link StoreRepository} for accessing/manipulating the data
     * @param salesListView   The View instance {@link SalesListContract.View} of this Presenter
     */
    public SalesListPresenter(@NonNull LoaderProvider loaderProvider,
                              @NonNull LoaderManager loaderManager,
                              @NonNull StoreRepository storeRepository,
                              @NonNull SalesListContract.View salesListView) {
        mLoaderProvider = loaderProvider;
        mLoaderManager = loaderManager;
        mStoreRepository = storeRepository;
        mSalesListView = salesListView;

        //Creating and starting the Content Observer Thread
        mContentObserverHandlerThread = new HandlerThread(CONTENT_OBSERVER_THREAD_NAME);
        mContentObserverHandlerThread.start();

        //Registering the View with the Presenter
        mSalesListView.setPresenter(this);
    }

    /**
     * Method that initiates the work of a Presenter which is invoked by the View
     * that implements the {@link com.example.kaushiknsanji.storeapp.ui.BaseView}
     */
    @Override
    public void start() {
        registerContentObservers();
        //Start downloading the Products with Sales Information from the database
        triggerProductSalesLoad(false);
    }

    /**
     * Method that registers the Content Observers to notify the changes in Products, Suppliers,
     * Product Price data and Product Inventory data
     */
    private void registerContentObservers() {
        if (mProductContentObserver == null) {
            //When Products Observer is not initialized

            //Create the Observer Instance
            mProductContentObserver = new ProductSalesContentObserver(ProductContract.Product.CONTENT_URI);
            //Register the Content Observer to monitor the Products URI and its descendants
            mStoreRepository.registerContentObserver(ProductContract.Product.CONTENT_URI,
                    true, mProductContentObserver);
        }

        if (mSupplierContentObserver == null) {
            //When Suppliers Observer is not initialized

            //Create the Observer Instance
            mSupplierContentObserver = new ProductSalesContentObserver(SupplierContract.Supplier.CONTENT_URI);
            //Register the Content Observer to monitor the Suppliers URI and its descendants
            mStoreRepository.registerContentObserver(SupplierContract.Supplier.CONTENT_URI,
                    true, mSupplierContentObserver);
        }

        if (mPriceContentObserver == null) {
            //When Product Price Observer is not initialized

            //Create the Observer Instance
            mPriceContentObserver = new ProductSalesContentObserver(SalesContract.ProductSupplierInfo.CONTENT_URI);
            //Register the Content Observer to monitor the ProductSupplierInfo URI and its descendants
            mStoreRepository.registerContentObserver(SalesContract.ProductSupplierInfo.CONTENT_URI,
                    true, mPriceContentObserver);
        }

        if (mInventoryContentObserver == null) {
            //When Product Inventory Observer is not initialized

            //Create the Observer Instance
            mInventoryContentObserver = new ProductSalesContentObserver(SalesContract.ProductSupplierInventory.CONTENT_URI);
            //Register the Content Observer to monitor the ProductSupplierInventory URI and its descendants
            mStoreRepository.registerContentObserver(SalesContract.ProductSupplierInventory.CONTENT_URI,
                    true, mInventoryContentObserver);
        }

        //Reset all observers to receive notifications
        resetObservers();
    }

    /**
     * Method that unregisters the Content Observers previously registered
     */
    private void unregisterContentObservers() {
        if (mProductContentObserver != null) {
            //When Products Observer is already initialized, unregister the same
            mStoreRepository.unregisterContentObserver(mProductContentObserver);
            mProductContentObserver = null;
        }

        if (mSupplierContentObserver != null) {
            //When Suppliers Observer is already initialized, unregister the same
            mStoreRepository.unregisterContentObserver(mSupplierContentObserver);
            mSupplierContentObserver = null;
        }

        if (mPriceContentObserver != null) {
            //When Product Price Observer is already initialized, unregister the same
            mStoreRepository.unregisterContentObserver(mPriceContentObserver);
            mPriceContentObserver = null;
        }

        if (mInventoryContentObserver != null) {
            //When Product Inventory Observer is already initialized, unregister the same
            mStoreRepository.unregisterContentObserver(mInventoryContentObserver);
            mInventoryContentObserver = null;
        }
    }

    /**
     * Method that resets the Content Observers to receive future notifications again
     */
    private void resetObservers() {
        //Resetting the observers' notification control boolean to FALSE,
        //to again trigger any new notification that occurs later.
        mDeliveredNotification.set(false);
    }

    /**
     * Method invoked by the {@link com.example.kaushiknsanji.storeapp.ui.MainActivity} displaying the ViewPager.
     * This is called when the User clicks on the Fab "+" button shown by the {@link com.example.kaushiknsanji.storeapp.ui.MainActivity}
     */
    @Override
    public void onFabAddClicked() {
        //No-op for this screen as there is no FAB
    }

    /**
     * Method invoked by the {@link com.example.kaushiknsanji.storeapp.ui.MainActivity} displaying the ViewPager.
     * This is called when the User clicks on the Refresh Menu icon shown by the {@link com.example.kaushiknsanji.storeapp.ui.MainActivity}
     */
    @Override
    public void onRefreshMenuClicked() {
        triggerProductSalesLoad(true);
    }

    /**
     * Method that triggers the CursorLoader to load the Products with Sales data from the database
     *
     * @param forceLoad Boolean value that controls the nature of the trigger
     *                  <br/><b>TRUE</b> to forcefully start a new load process
     *                  <br/><b>FALSE</b> to start a new/existing load process
     */
    @Override
    public void triggerProductSalesLoad(boolean forceLoad) {
        //Display the Progress Indicator
        mSalesListView.showProgressIndicator();
        if (forceLoad) {
            //When forcefully triggered, restart the loader
            mLoaderManager.restartLoader(AppConstants.SALES_LOADER, null, this);
        } else {
            //When triggered, start a new loader or load the existing loader
            mLoaderManager.initLoader(AppConstants.SALES_LOADER, null, this);
        }
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     * <p>
     * <p>This will always be called from the process's main thread.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        //Returning the Loader instance for the Sales List
        return mLoaderProvider.createCursorLoader(LoaderProvider.SALES_LIST_TYPE);
    }

    /**
     * Called when a previously created loader has finished its load.
     * <p>
     * <p>This will always be called from the process's main thread.
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            //When Cursor is NOT Null
            if (data.getCount() > 0) {
                //When we have data in the Cursor
                onDataLoaded(data);
            } else {
                //When there is no data in the Cursor
                onDataEmpty();
            }
        } else {
            //When Cursor is Null
            onDataNotAvailable();
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     * <p>
     * <p>This will always be called from the process's main thread.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        //When previous Loader was reset
        onDataReset();
    }

    /**
     * Callback Method of {@link DataRepository.CursorDataLoaderCallback} invoked when data is present
     * in the Cursor {@code data}
     *
     * @param data The {@link Cursor} generated for the query executed
     *             using a {@link android.support.v4.content.CursorLoader}
     */
    @Override
    public void onDataLoaded(Cursor data) {
        //Hide Empty View
        mSalesListView.hideEmptyView();
        //Initializing the ArrayList to load the SalesLite data from the Cursor
        ArrayList<SalesLite> salesList = new ArrayList<>();
        //Resetting the cursor position if pointing past the last row
        if (data.isAfterLast()) {
            data.moveToPosition(-1);
        }
        //Iterating over the Cursor data and building the list
        while (data.moveToNext()) {
            salesList.add(SalesLite.from(data));
        }
        //Updating the View with the new data
        mSalesListView.loadSalesList(salesList);
        //Hide the Progress Indicator
        mSalesListView.hideProgressIndicator();
    }

    /**
     * Callback Method of {@link DataRepository.CursorDataLoaderCallback} invoked when there is no data
     * in the {@link Cursor} returned for the query executed
     * using a {@link android.support.v4.content.CursorLoader}
     */
    @Override
    public void onDataEmpty() {
        //Hide the Progress Indicator
        mSalesListView.hideProgressIndicator();
        //Show empty view
        mSalesListView.showEmptyView();
    }

    /**
     * Callback Method of {@link DataRepository.CursorDataLoaderCallback} invoked when no {@link Cursor}
     * was generated for the query executed
     * using a {@link android.support.v4.content.CursorLoader}
     */
    @Override
    public void onDataNotAvailable() {
        //Hide the Progress Indicator
        mSalesListView.hideProgressIndicator();
        //Show error message
        mSalesListView.showError(R.string.sales_list_load_error);
    }

    /**
     * Callback Method of {@link DataRepository.CursorDataLoaderCallback} invoked when
     * the {@link android.support.v4.content.CursorLoader} was reset
     */
    @Override
    public void onDataReset() {
        //Updating the View with an empty list
        mSalesListView.loadSalesList(new ArrayList<>());
        //Show empty view
        mSalesListView.showEmptyView();
    }

    /**
     * Callback Method of {@link DataRepository.CursorDataLoaderCallback} invoked when
     * there is a change in the content loaded by the {@link android.support.v4.content.CursorLoader}
     */
    @Override
    public void onContentChange() {
        //Retrieving the Sales Cursor Loader
        Loader<Cursor> salesLoader = mLoaderManager.getLoader(AppConstants.SALES_LOADER);
        if (salesLoader != null) {
            //If Loader is already registered, restart by triggering a Content Change notification
            salesLoader.onContentChanged();
        } else {
            //If Loader not registered, then force restart the load
            triggerProductSalesLoad(true);
        }
    }

    /**
     * Method invoked when there is a change in the data pointed to by the Products URI
     * {@link ProductContract.Product#CONTENT_URI}
     */
    @Override
    public void onProductContentChange() {
        //Retrieving the Products Cursor Loader used by ProductListFragment
        Loader<Cursor> productsLoader = mLoaderManager.getLoader(AppConstants.PRODUCTS_LOADER);
        if (productsLoader != null) {
            //If Loader is already registered, restart by triggering a Content Change notification
            productsLoader.onContentChanged();
        }
    }

    /**
     * Method invoked when the View is about to be destroyed.
     * This method should release any critical resources held by the Presenter.
     */
    @Override
    public void releaseResources() {
        //Unregister any registered Content Observers
        unregisterContentObservers();
        //Stop the Content Observer Thread
        mContentObserverHandlerThread.quit();
    }

    /**
     * Method invoked when the user clicks on the "Delete Product" button.
     * This should delete the Product identified by {@code productId} from the database
     * along with its relationship with other tables in database.
     *
     * @param productId  The Product Id/Primary Key of the Product to be deleted.
     * @param productSku The Product SKU of the Product to be deleted.
     */
    @Override
    public void deleteProduct(int productId, String productSku) {
        //Display the Progress Indicator
        mSalesListView.showProgressIndicator();

        //Reset observers
        resetObservers();

        //Executing Product Deletion with the Repository
        mStoreRepository.deleteProductById(productId, new DataRepository.DataOperationsCallback() {
            /**
             * Method invoked when the database operations like insert/update/delete
             * was successful.
             */
            @Override
            public void onSuccess() {
                //Hide Progress Indicator
                mSalesListView.hideProgressIndicator();

                //Show the delete success message
                mSalesListView.showDeleteSuccess(productSku);
            }

            /**
             * Method invoked when the database operations like insert/update/delete
             * failed to complete.
             *
             * @param messageId The String resource of the error message
             *                  for the database operation failure
             * @param args Variable number of arguments to replace the format specifiers
             *             in the String resource if any
             */
            @Override
            public void onFailure(int messageId, @Nullable Object... args) {
                //Hide Progress Indicator
                mSalesListView.hideProgressIndicator();

                //Show the error message
                mSalesListView.showError(messageId, args);
            }
        });
    }

    /**
     * Method invoked when the user clicks on the "Sell 1" button.
     * This should decrease the Available Quantity from the
     * Top Supplier {@link SalesLite#mSupplierAvailableQuantity} by 1, indicating one Quantity
     * of the Product was sold/shipped.
     *
     * @param salesLite The {@link SalesLite} instance containing the Product Supplier
     *                  information with availability.
     */
    @Override
    public void sellOneQuantity(SalesLite salesLite) {
        //Display the Progress Indicator
        mSalesListView.showProgressIndicator();

        //Reset observers
        resetObservers();

        //Updating the Quantity via the Repository
        mStoreRepository.decreaseProductSupplierInventory(salesLite.getProductId(),
                salesLite.getProductSku(), salesLite.getSupplierId(), salesLite.getTopSupplierCode(),
                salesLite.getSupplierAvailableQuantity(), 1, new DataRepository.DataOperationsCallback() {
                    /**
                     * Method invoked when the database operations like insert/update/delete
                     * was successful.
                     */
                    @Override
                    public void onSuccess() {
                        //Hide Progress Indicator
                        mSalesListView.hideProgressIndicator();

                        //Show the success message
                        mSalesListView.showSellQuantitySuccess(salesLite.getProductSku(), salesLite.getTopSupplierCode());
                    }

                    /**
                     * Method invoked when the database operations like insert/update/delete
                     * failed to complete.
                     *
                     * @param messageId The String resource of the error message
                     *                  for the database operation failure
                     * @param args      Variable number of arguments to replace the format specifiers
                     *                  in the String resource if any
                     */
                    @Override
                    public void onFailure(int messageId, @Nullable Object... args) {
                        //Hide Progress Indicator
                        mSalesListView.hideProgressIndicator();

                        //Show the error message
                        mSalesListView.showError(messageId, args);
                    }
                });
    }

    /**
     * Invoked from a previous call to
     * {@link FragmentActivity#startActivityForResult(Intent, int)}.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode >= FragmentActivity.RESULT_FIRST_USER) {
            //When we have the custom results for the requests made

            if (requestCode == SalesConfigActivity.REQUEST_EDIT_SALES) {
                //For an Edit Sales request

                if (resultCode == SalesConfigActivity.RESULT_EDIT_SALES) {
                    //When the result is for the Edit Sales action
                    //Show the Update Inventory Success message
                    mSalesListView.showUpdateInventorySuccess(data.getStringExtra(SalesConfigActivity.EXTRA_RESULT_PRODUCT_SKU));

                } else if (resultCode == ProductConfigActivity.RESULT_DELETE_PRODUCT) {
                    //When the result is for the Delete Product action
                    //Show the Delete Success message
                    mSalesListView.showDeleteSuccess(data.getStringExtra(SalesConfigActivity.EXTRA_RESULT_PRODUCT_SKU));
                }

            }
        }
    }

    /**
     * Method invoked when the user clicks on the Item View itself. This should launch the
     * {@link SalesConfigActivity}
     * for editing the Sales data of the Product.
     *
     * @param productId             The Primary Key of the Product to be edited.
     * @param activityOptionsCompat Instance of {@link ActivityOptionsCompat} that has the
     *                              details for Shared Element Transition
     */
    @Override
    public void editProductSales(int productId, ActivityOptionsCompat activityOptionsCompat) {
        //Reset observers
        resetObservers();
        //Delegating to the View to launch the Activity
        mSalesListView.launchEditProductSales(productId, activityOptionsCompat);
    }

    /**
     * {@link ContentObserver} class that observes and notifies changes in the
     * 'item' table, 'item_image' table, 'item_supplier_info' table and 'item_supplier_inventory' table
     */
    private class ProductSalesContentObserver extends ContentObserver {
        //URI Matcher codes for identifying the URI of Product and its descendant relationships
        private static final int ITEM_ID = 10;
        private static final int ITEM_IMAGES_ID = 11;
        //URI Matcher codes for identifying the URI of Supplier and its descendant relationships
        private static final int SUPPLIER_ID = 20;
        //URI Matcher codes for identifying the URI of ProductSupplierInfo and its descendant relationships
        private static final int SUPPLIER_ITEMS_ID = 30;
        //URI Matcher codes for identifying the URI of ProductSupplierInventory and its descendant relationships
        private static final int SALES_INVENTORY_ITEM_ID = 40;
        //URI Matcher for matching the possible URI
        private final UriMatcher mUriMatcher = buildUriMatcher();
        //The URI observed by the Observer for changes
        private final Uri mObserverUri;
        //Main Thread Handler to dispatch the notifications to the CursorLoader on Main Thread
        private final Handler mMainThreadHandler;

        /**
         * Creates a content observer.
         *
         * @param observerUri The URI to be observed by the {@link ProductSalesContentObserver} instance
         */
        ProductSalesContentObserver(Uri observerUri) {
            //Using the custom Content Observer thread to receive notifications on
            super(new Handler(mContentObserverHandlerThread.getLooper()));
            //Instantiating the Main Thread Handler for dispatching notifications to the CursorLoader on Main Thread
            mMainThreadHandler = new Handler(Looper.getMainLooper());
            //Saving the URI being observed
            mObserverUri = observerUri;
        }

        /**
         * Method that returns the {@link UriMatcher} to be used
         * for matching the various possible Uri.
         *
         * @return {@link UriMatcher} instance to be used for matching the Uri
         */
        private UriMatcher buildUriMatcher() {
            //Constructs an empty UriMatcher for the root node
            UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
            //For "content://AUTHORITY/item/#" URI that references a record in 'item' table
            matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                    ProductContract.PATH_ITEM + "/#", ITEM_ID);
            //For "content://AUTHORITY/item/image/#" URI that references a set of records in 'item_image' table
            matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                    ProductContract.PATH_ITEM + "/" + ProductContract.PATH_ITEM_IMAGE + "/#",
                    ITEM_IMAGES_ID);
            //For "content://AUTHORITY/supplier/#" URI that references a record in 'supplier' table
            matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                    SupplierContract.PATH_SUPPLIER + "/#", SUPPLIER_ID);
            //For "content://AUTHORITY/salesinfo/supplier/#" URI that references a set of records in 'item_supplier_info' table
            //identified by 'supplier_id'
            matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                    SalesContract.PATH_ITEM_SUPPLIER_INFO + "/" + SupplierContract.PATH_SUPPLIER + "/#",
                    SUPPLIER_ITEMS_ID);
            //For "content://AUTHORITY/salesinventory/item/#" URI that references a set of records in 'item_supplier_inventory' table
            //identified by 'item_id'
            matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                    SalesContract.PATH_ITEM_SUPPLIER_INVENTORY + "/" + ProductContract.PATH_ITEM + "/#",
                    SALES_INVENTORY_ITEM_ID);
            //Returning the URI Matcher prepared
            return matcher;
        }

        /**
         * Returns true if this observer is interested receiving self-change notifications.
         * <p>
         * Subclasses should override this method to indicate whether the observer
         * is interested in receiving notifications for changes that it made to the
         * content itself.
         *
         * @return True if self-change notifications should be delivered to the observer.
         */
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        /**
         * This method is called when a content change occurs.
         * <p>
         * Subclasses should override this method to handle content changes.
         * </p>
         *
         * @param selfChange True if this is a self-change notification.
         */
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        /**
         * This method is called when a content change occurs.
         *
         * @param selfChange True if this is a self-change notification.
         * @param uri        The Uri of the changed content, or null if unknown.
         */
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri != null) {
                //When we have the URI, trigger notifications based on the URI

                //Finding the URI Match code
                int uriMatch = mUriMatcher.match(uri);

                if (mObserverUri.equals(ProductContract.Product.CONTENT_URI)) {
                    //For the URI of Product and its descendants

                    //Trigger notifications based on the URI
                    switch (uriMatch) {
                        case ITEM_ID:
                            triggerNotification(uri);
                            break;
                        case ITEM_IMAGES_ID:
                            triggerNotification(uri);
                            break;
                    }
                } else if (mObserverUri.equals(SupplierContract.Supplier.CONTENT_URI)) {
                    //For the URI of Supplier and its descendants

                    //Trigger notifications based on the URI
                    switch (uriMatch) {
                        case SUPPLIER_ID:
                            triggerNotification(uri);
                            break;
                    }
                } else if (mObserverUri.equals(SalesContract.ProductSupplierInfo.CONTENT_URI)) {
                    //For the URI of ProductSupplierInfo and its descendants

                    //Trigger notifications based on the URI
                    switch (uriMatch) {
                        case SUPPLIER_ITEMS_ID:
                            triggerNotification(uri);
                            break;
                    }
                } else if (mObserverUri.equals(SalesContract.ProductSupplierInventory.CONTENT_URI)) {
                    //For the URI of ProductSupplierInventory and its descendants

                    //Trigger notifications based on the URI
                    switch (uriMatch) {
                        case SALES_INVENTORY_ITEM_ID:
                            triggerNotification(uri);
                            break;
                    }
                }

            } else if (selfChange) {
                //When it is a self change notification, dispatch the content change
                //notification to Loader

                //Posting notification on Main Thread
                mMainThreadHandler.post(SalesListPresenter.this::onContentChange);
            }
        }

        /**
         * Method that triggers content change notification to the Loader.
         *
         * @param uri The Uri of the changed content.
         */
        private void triggerNotification(Uri uri) {
            if (mDeliveredNotification.compareAndSet(false, true)) {
                //When notification was not delivered previously, dispatch the notification and set to TRUE

                Log.i(LOG_TAG, "triggerNotification: Called for " + uri);

                //Posting notification on Main Thread
                mMainThreadHandler.post(SalesListPresenter.this::onContentChange);

                //Posting another notification specifically for any change in Products
                //to trigger a reload in the ProductListFragment
                if (mObserverUri.equals(ProductContract.Product.CONTENT_URI)) {
                    mMainThreadHandler.post(SalesListPresenter.this::onProductContentChange);
                }
            }
        }

    }

}
