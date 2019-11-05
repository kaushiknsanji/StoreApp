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

package com.example.kaushiknsanji.storeapp.ui.products;

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
import com.example.kaushiknsanji.storeapp.data.local.contracts.StoreContract;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductLite;
import com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigActivity;
import com.example.kaushiknsanji.storeapp.utils.AppConstants;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Presenter Class that implements {@link ProductListContract.Presenter} on the lines of
 * Model-View-Presenter architecture. This Presenter interfaces with the App repository {@link StoreRepository}
 * and provides the list of Products configured to the View {@link ProductListFragment} to load and display the same.
 *
 * @author Kaushik N Sanji
 */
public class ProductListPresenter
        implements ProductListContract.Presenter,
        LoaderManager.LoaderCallbacks<Cursor>, DataRepository.CursorDataLoaderCallback {

    //Constant used for logs
    private static final String LOG_TAG = ProductListPresenter.class.getSimpleName();
    //The Thread name of the Content Observer
    private static final String CONTENT_OBSERVER_THREAD_NAME = "ProductListContentObserverThread";
    //The View Interface of this Presenter
    @NonNull
    private final ProductListContract.View mProductListView;
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
    //The Content Observer to notify changes in the Product data
    private ProductContentObserver mProductContentObserver;

    /**
     * Constructor of {@link ProductListPresenter}
     *
     * @param loaderProvider  Instance of {@link LoaderProvider} that provides the CursorLoader instance
     * @param loaderManager   Instance of {@link LoaderManager}
     * @param storeRepository Instance of {@link StoreRepository} for accessing/manipulating the data
     * @param productListView The View instance {@link ProductListContract.View} of this Presenter
     */
    public ProductListPresenter(@NonNull LoaderProvider loaderProvider,
                                @NonNull LoaderManager loaderManager,
                                @NonNull StoreRepository storeRepository,
                                @NonNull ProductListContract.View productListView) {
        mLoaderProvider = loaderProvider;
        mLoaderManager = loaderManager;
        mStoreRepository = storeRepository;
        mProductListView = productListView;

        //Creating and starting the Content Observer Thread
        mContentObserverHandlerThread = new HandlerThread(CONTENT_OBSERVER_THREAD_NAME);
        mContentObserverHandlerThread.start();

        //Registering the View with the Presenter
        mProductListView.setPresenter(this);
    }

    /**
     * Method that initiates the work of a Presenter which is invoked by the View
     * that implements the {@link com.example.kaushiknsanji.storeapp.ui.BaseView}
     */
    @Override
    public void start() {
        //Register the Content Observer
        registerContentObserver();
        //Start downloading the Product Information from the database
        triggerProductsLoad(false);
    }

    /**
     * Method invoked by the {@link com.example.kaushiknsanji.storeapp.ui.MainActivity} displaying the ViewPager.
     * This is called when the User clicks on the Fab "+" button shown by the {@link com.example.kaushiknsanji.storeapp.ui.MainActivity}
     */
    @Override
    public void onFabAddClicked() {
        addNewProduct();
    }

    /**
     * Method invoked by the {@link com.example.kaushiknsanji.storeapp.ui.MainActivity} displaying the ViewPager.
     * This is called when the User clicks on the Refresh Menu icon shown by the {@link com.example.kaushiknsanji.storeapp.ui.MainActivity}
     */
    @Override
    public void onRefreshMenuClicked() {
        triggerProductsLoad(true);
    }

    /**
     * Method that registers the Content Observer to notify the changes in Products data
     */
    private void registerContentObserver() {
        if (mProductContentObserver == null) {
            //When Observer is not initialized

            //Create the Observer Instance
            mProductContentObserver = new ProductContentObserver();
            //Register the Content Observer to monitor the Products URI and its descendants
            mStoreRepository.registerContentObserver(mProductContentObserver.OBSERVER_URI,
                    true,
                    mProductContentObserver
            );

        } else {
            //When Observer is already initialized, reset the observer to receive future notifications again
            mProductContentObserver.resetObserver();
        }
    }

    /**
     * Method that unregisters the Content Observer previously registered
     */
    private void unregisterContentObserver() {
        if (mProductContentObserver != null) {
            //When the Product Content Observer was already initialized, unregister the same
            mStoreRepository.unregisterContentObserver(mProductContentObserver);
            mProductContentObserver = null; //Invalidating..
        }
    }

    /**
     * Method that resets the Content Observers to receive future notifications again
     */
    private void resetObservers() {
        if (mProductContentObserver != null) {
            //When the Product Content Observer was already initialized, reset the same.
            mProductContentObserver.resetObserver();
        }
    }

    /**
     * Method that triggers the CursorLoader to load the Products from the database
     *
     * @param forceLoad Boolean value that controls the nature of the trigger
     *                  <br/><b>TRUE</b> to forcefully start a new load process
     *                  <br/><b>FALSE</b> to start a new/existing load process
     */
    @Override
    public void triggerProductsLoad(boolean forceLoad) {
        //Display the Progress Indicator
        mProductListView.showProgressIndicator();
        if (forceLoad) {
            //When forcefully triggered, restart the loader
            mLoaderManager.restartLoader(AppConstants.PRODUCTS_LOADER, null, this);
        } else {
            //When triggered, start a new loader or load the existing loader
            mLoaderManager.initLoader(AppConstants.PRODUCTS_LOADER, null, this);
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
        //Returning the Loader instance for the Product List
        return mLoaderProvider.createCursorLoader(LoaderProvider.PRODUCT_LIST_TYPE);
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
        mProductListView.hideEmptyView();
        //Initializing the ArrayList to load the ProductLite data from the Cursor
        ArrayList<ProductLite> productList = new ArrayList<>();
        //Resetting the cursor position if pointing past the last row
        if (data.isAfterLast()) {
            data.moveToPosition(-1);
        }
        //Iterating over the Cursor data and building the list
        while (data.moveToNext()) {
            productList.add(ProductLite.from(data));
        }
        //Updating the View with the new data
        mProductListView.loadProducts(productList);
        //Hide the Progress Indicator
        mProductListView.hideProgressIndicator();
    }

    /**
     * Callback Method of {@link DataRepository.CursorDataLoaderCallback} invoked when there is no data
     * in the {@link Cursor} returned for the query executed
     * using a {@link android.support.v4.content.CursorLoader}
     */
    @Override
    public void onDataEmpty() {
        //Hide the Progress Indicator
        mProductListView.hideProgressIndicator();
        //Show empty view
        mProductListView.showEmptyView();
    }

    /**
     * Callback Method of {@link DataRepository.CursorDataLoaderCallback} invoked when no {@link Cursor}
     * was generated for the query executed
     * using a {@link android.support.v4.content.CursorLoader}
     */
    @Override
    public void onDataNotAvailable() {
        //Hide the Progress Indicator
        mProductListView.hideProgressIndicator();
        //Show error message
        mProductListView.showError(R.string.product_list_load_error);
    }

    /**
     * Callback Method of {@link DataRepository.CursorDataLoaderCallback} invoked when
     * the {@link android.support.v4.content.CursorLoader} was reset
     */
    @Override
    public void onDataReset() {
        //Updating the View with an empty list
        mProductListView.loadProducts(new ArrayList<>());
        //Show empty view
        mProductListView.showEmptyView();
    }

    /**
     * Callback Method of {@link DataRepository.CursorDataLoaderCallback} invoked when
     * there is a change in the content loaded by the {@link android.support.v4.content.CursorLoader}
     */
    @Override
    public void onContentChange() {
        //Retrieving the Products Cursor Loader
        Loader<Cursor> productsLoader = mLoaderManager.getLoader(AppConstants.PRODUCTS_LOADER);
        if (productsLoader != null) {
            //If Loader is already registered, restart by triggering a Content Change notification
            productsLoader.onContentChanged();
        } else {
            //If Loader not registered, then force restart the load
            triggerProductsLoad(true);
        }

        //Retrieving the Sales Cursor Loader used by SalesListFragment
        Loader<Cursor> salesLoader = mLoaderManager.getLoader(AppConstants.SALES_LOADER);
        if (salesLoader != null) {
            //If Loader is already registered, restart by triggering a Content Change notification
            salesLoader.onContentChanged();
        }
    }

    /**
     * Method invoked when the user clicks on the "Edit" button on the Item View or the Item View itself
     * to edit the Product details. This should launch the {@link ProductConfigActivity}
     * for the Product to be edited.
     *
     * @param productId             The Primary Key of the Product to be edited.
     * @param activityOptionsCompat Instance of {@link ActivityOptionsCompat} that has the
     *                              details for Shared Element Transition
     */
    @Override
    public void editProduct(int productId, ActivityOptionsCompat activityOptionsCompat) {
        //Reset observers
        resetObservers();
        //Delegating to the view to launch the Activity for editing an Existing Product
        mProductListView.launchEditProduct(productId, activityOptionsCompat);
    }

    /**
     * Method invoked when the user clicks on the "Delete" button on the Item View
     * to delete the Product identified by {@link ProductLite#mId}
     *
     * @param product The {@link ProductLite} instance of the Product to be deleted.
     */
    @Override
    public void deleteProduct(ProductLite product) {
        //Display the Progress Indicator
        mProductListView.showProgressIndicator();

        //Reset observers
        resetObservers();

        //Executing Product Deletion with the Repository
        mStoreRepository.deleteProductById(product.getId(), new DataRepository.DataOperationsCallback() {
            /**
             * Method invoked when the database operations like insert/update/delete
             * was successful.
             */
            @Override
            public void onSuccess() {
                //Hide Progress Indicator
                mProductListView.hideProgressIndicator();

                //Show the delete success message
                mProductListView.showDeleteSuccess(product.getSku());
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
                mProductListView.hideProgressIndicator();

                //Show the error message
                mProductListView.showError(messageId, args);
            }
        });
    }

    /**
     * Method invoked when the user clicks on the FAB button to add a New Product
     * into the database.
     */
    @Override
    public void addNewProduct() {
        //Reset observers
        resetObservers();
        //Delegating to the view to launch the Activity for adding a New Product
        mProductListView.launchAddNewProduct();
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

            if (requestCode == ProductConfigActivity.REQUEST_EDIT_PRODUCT) {
                //For an Edit Product request
                if (resultCode == ProductConfigActivity.RESULT_EDIT_PRODUCT) {
                    //When the result is for the Edit action
                    //Show the Update Success message
                    mProductListView.showUpdateSuccess(data.getStringExtra(ProductConfigActivity.EXTRA_RESULT_PRODUCT_SKU));
                } else if (resultCode == ProductConfigActivity.RESULT_DELETE_PRODUCT) {
                    //When the result is for the Delete action
                    //Show the Delete Success message
                    mProductListView.showDeleteSuccess(data.getStringExtra(ProductConfigActivity.EXTRA_RESULT_PRODUCT_SKU));
                }
            } else if (requestCode == ProductConfigActivity.REQUEST_ADD_PRODUCT &&
                    resultCode == ProductConfigActivity.RESULT_ADD_PRODUCT) {
                //When the request and the result is for Add Product
                //Show the Add Success message
                mProductListView.showAddSuccess(data.getStringExtra(ProductConfigActivity.EXTRA_RESULT_PRODUCT_SKU));
            }
        }
    }

    /**
     * Method invoked when the View is about to be destroyed.
     * This method should release any critical resources held by the Presenter.
     */
    @Override
    public void releaseResources() {
        //Unregister any registered Content Observer
        unregisterContentObserver();
        //Stop the Content Observer Thread
        mContentObserverHandlerThread.quit();
    }

    /**
     * {@link ContentObserver} class that observes and notifies changes in the
     * 'item' table and 'item_image' table
     */
    private class ProductContentObserver extends ContentObserver {

        //URI Matcher codes for identifying the URI of Item and its descendant relationships
        private static final int ITEM_ID = 10;
        private static final int ITEM_IMAGES_ID = 11;
        //URI Matcher for matching the possible URI
        private final UriMatcher mUriMatcher = buildUriMatcher();
        //The URI to be observed which is the Product's URI
        private final Uri OBSERVER_URI = ProductContract.Product.CONTENT_URI;
        //Main Thread Handler to dispatch the notifications to the CursorLoader on Main Thread
        private final Handler mMainThreadHandler;

        //Boolean to control multiple notifications from being issued
        private final AtomicBoolean mDeliveredNotification = new AtomicBoolean(false);

        /**
         * Creates a content observer.
         */
        ProductContentObserver() {
            //Using the custom Content Observer thread to receive notifications on
            super(new Handler(mContentObserverHandlerThread.getLooper()));
            //Instantiating the Main Thread Handler for dispatching notifications to the CursorLoader on Main Thread
            mMainThreadHandler = new Handler(Looper.getMainLooper());
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
                switch (mUriMatcher.match(uri)) {
                    case ITEM_ID:
                        triggerNotification(uri);
                        break;
                    case ITEM_IMAGES_ID:
                        triggerNotification(uri);
                        break;
                }
            } else if (selfChange) {
                //When it is a self change notification, dispatch the content change
                //notification to Loader

                //Posting notification on Main Thread
                mMainThreadHandler.post(ProductListPresenter.this::onContentChange);
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
                mMainThreadHandler.post(ProductListPresenter.this::onContentChange);
            }
        }

        /**
         * Method that resets the observer's notification control boolean to FALSE,
         * to again trigger any new notification that occurs later.
         */
        private void resetObserver() {
            mDeliveredNotification.set(false);
        }
    }
}