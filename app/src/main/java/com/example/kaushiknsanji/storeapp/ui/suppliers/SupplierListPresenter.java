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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.DataRepository;
import com.example.kaushiknsanji.storeapp.data.StoreRepository;
import com.example.kaushiknsanji.storeapp.data.local.LoaderProvider;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SalesContract;
import com.example.kaushiknsanji.storeapp.data.local.contracts.StoreContract;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SupplierContract;
import com.example.kaushiknsanji.storeapp.data.local.models.SupplierLite;
import com.example.kaushiknsanji.storeapp.ui.suppliers.config.SupplierConfigActivity;
import com.example.kaushiknsanji.storeapp.utils.AppConstants;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Presenter Class that implements {@link SupplierListContract.Presenter} on the lines of
 * Model-View-Presenter architecture. This Presenter interfaces with the App repository {@link StoreRepository}
 * and provides the list of Suppliers configured to the View {@link SupplierListFragment} to load and display the same.
 *
 * @author Kaushik N Sanji
 */
public class SupplierListPresenter implements SupplierListContract.Presenter,
        LoaderManager.LoaderCallbacks<Cursor>, DataRepository.CursorDataLoaderCallback {

    //Constant used for logs
    private static final String LOG_TAG = SupplierListPresenter.class.getSimpleName();
    //The Thread name of the Content Observer
    private static final String CONTENT_OBSERVER_THREAD_NAME = "SupplierListContentObserverThread";
    //The View Interface of this Presenter
    @NonNull
    private final SupplierListContract.View mSupplierListView;
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
    //The Content Observer to notify changes in the Supplier data
    private SupplierContentObserver mSupplierContentObserver;
    //The Content Observer to notify changes in the Supplier Product Price data
    private SupplierContentObserver mPriceContentObserver;

    /**
     * Constructor of {@link SupplierListPresenter}
     *
     * @param loaderProvider   Instance of {@link LoaderProvider} that provides the CursorLoader instance
     * @param loaderManager    Instance of {@link LoaderManager}
     * @param storeRepository  Instance of {@link StoreRepository} for accessing/manipulating the data
     * @param supplierListView The View instance {@link SupplierListContract.View} of this Presenter
     */
    public SupplierListPresenter(@NonNull LoaderProvider loaderProvider,
                                 @NonNull LoaderManager loaderManager,
                                 @NonNull StoreRepository storeRepository,
                                 @NonNull SupplierListContract.View supplierListView) {
        mLoaderProvider = loaderProvider;
        mLoaderManager = loaderManager;
        mStoreRepository = storeRepository;
        mSupplierListView = supplierListView;

        //Creating and starting the Content Observer Thread
        mContentObserverHandlerThread = new HandlerThread(CONTENT_OBSERVER_THREAD_NAME);
        mContentObserverHandlerThread.start();

        //Registering the View with the Presenter
        mSupplierListView.setPresenter(this);
    }

    /**
     * Method that initiates the work of a Presenter which is invoked by the View
     * that implements the {@link com.example.kaushiknsanji.storeapp.ui.BaseView}
     */
    @Override
    public void start() {
        //Register the Content Observers
        registerContentObservers();
        //Start downloading the Supplier Information from the database
        triggerSuppliersLoad(false);
    }

    /**
     * Method that registers the Content Observers to notify the changes in Suppliers and Product Price data
     */
    private void registerContentObservers() {
        if (mSupplierContentObserver == null) {
            //When Suppliers Observer is not initialized

            //Create the Observer Instance
            mSupplierContentObserver = new SupplierContentObserver(SupplierContract.Supplier.CONTENT_URI);
            //Register the Content Observer to monitor the Suppliers URI and its descendants
            mStoreRepository.registerContentObserver(SupplierContract.Supplier.CONTENT_URI,
                    true,
                    mSupplierContentObserver
            );
        }

        if (mPriceContentObserver == null) {
            //When Product Price Observer is not initialized

            //Create the Observer Instance
            mPriceContentObserver = new SupplierContentObserver(SalesContract.ProductSupplierInfo.CONTENT_URI);
            //Register the Content Observer to monitor the ProductSupplierInfo URI and its descendants
            mStoreRepository.registerContentObserver(SalesContract.ProductSupplierInfo.CONTENT_URI,
                    true,
                    mPriceContentObserver
            );
        }

        //Reset all observers to receive notifications
        resetObservers();
    }

    /**
     * Method that unregisters the Content Observers previously registered
     */
    private void unregisterContentObservers() {
        if (mSupplierContentObserver != null) {
            //When Suppliers Observer is already initialized, unregister the same
            mStoreRepository.unregisterContentObserver(mSupplierContentObserver);
            mSupplierContentObserver = null; //Invalidating..
        }

        if (mPriceContentObserver != null) {
            //When Product Price Observer is already initialized, unregister the same
            mStoreRepository.unregisterContentObserver(mPriceContentObserver);
            mPriceContentObserver = null; //Invalidating..
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
     * Method that triggers the CursorLoader to load the Suppliers from the database
     *
     * @param forceLoad Boolean value that controls the nature of the trigger
     *                  <br/><b>TRUE</b> to forcefully start a new load process
     *                  <br/><b>FALSE</b> to start a new/existing load process
     */
    @Override
    public void triggerSuppliersLoad(boolean forceLoad) {
        //Display the Progress Indicator
        mSupplierListView.showProgressIndicator();
        if (forceLoad) {
            //When forcefully triggered, restart the loader
            mLoaderManager.restartLoader(AppConstants.SUPPLIERS_LOADER, null, this);
        } else {
            //When triggered, start a new loader or load the existing loader
            mLoaderManager.initLoader(AppConstants.SUPPLIERS_LOADER, null, this);
        }
    }

    /**
     * Method invoked when the user clicks on "Edit" button or the Item View itself. This should
     * launch the {@link SupplierConfigActivity}
     * for the Supplier to be edited.
     *
     * @param supplierId The Primary key of the Supplier to be edited.
     */
    @Override
    public void editSupplier(int supplierId) {
        //Reset observers
        resetObservers();
        //Delegating to the view to launch the Activity for editing an Existing Supplier
        mSupplierListView.launchEditSupplier(supplierId);
    }

    /**
     * Method invoked when the user clicks on "Delete" button. This should delete the Supplier
     * identified by {@link SupplierLite#mId}, from the database.
     *
     * @param supplier The {@link SupplierLite} data of the Supplier to be deleted.
     */
    @Override
    public void deleteSupplier(SupplierLite supplier) {
        //Display the Progress Indicator
        mSupplierListView.showProgressIndicator();

        //Reset observers
        resetObservers();

        //Executing Supplier Deletion via the Repository
        mStoreRepository.deleteSupplierById(supplier.getId(), new DataRepository.DataOperationsCallback() {
            /**
             * Method invoked when the database operations like insert/update/delete
             * was successful.
             */
            @Override
            public void onSuccess() {
                //Hide Progress Indicator
                mSupplierListView.hideProgressIndicator();

                //Show the delete success message
                mSupplierListView.showDeleteSuccess(supplier.getCode());
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
                mSupplierListView.hideProgressIndicator();

                //Show the error message
                mSupplierListView.showError(messageId, args);
            }
        });
    }

    /**
     * Method invoked when the user clicks on the FAB button to add a New Supplier
     * into the database.
     */
    @Override
    public void addNewSupplier() {
        //Reset observers
        resetObservers();
        //Delegating to the view to launch the Activity for adding a New Supplier
        mSupplierListView.launchAddNewSupplier();
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

            if (requestCode == SupplierConfigActivity.REQUEST_EDIT_SUPPLIER) {
                //For an Edit Supplier request
                if (resultCode == SupplierConfigActivity.RESULT_EDIT_SUPPLIER) {
                    //When the result is for the Edit action
                    //Show the Update Success message
                    mSupplierListView.showUpdateSuccess(data.getStringExtra(SupplierConfigActivity.EXTRA_RESULT_SUPPLIER_CODE));
                } else if (resultCode == SupplierConfigActivity.RESULT_DELETE_SUPPLIER) {
                    //When the result is for the Delete action
                    //Show the Delete Success message
                    mSupplierListView.showDeleteSuccess(data.getStringExtra(SupplierConfigActivity.EXTRA_RESULT_SUPPLIER_CODE));
                }
            } else if (requestCode == SupplierConfigActivity.REQUEST_ADD_SUPPLIER &&
                    resultCode == SupplierConfigActivity.RESULT_ADD_SUPPLIER) {
                //When the request and the result is for Add Supplier
                //Show the Add Success message
                mSupplierListView.showAddSuccess(data.getStringExtra(SupplierConfigActivity.EXTRA_RESULT_SUPPLIER_CODE));
            }
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
     * Method invoked when the user clicks on the Phone button. This should launch the Phone
     * dialer passing in the phone number {@code phoneNumber}
     *
     * @param phoneNumber The Phone Number to dial.
     */
    @Override
    public void defaultPhoneClicked(String phoneNumber) {
        //Delegating to the View to launch the Phone Dialer
        mSupplierListView.dialPhoneNumber(phoneNumber);
    }

    /**
     * Method invoked when the user clicks on the Email button. This should launch an Email
     * activity passing in the "TO" Address {@code toEmailAddress}
     *
     * @param toEmailAddress The "TO" Address to send an email to.
     */
    @Override
    public void defaultEmailClicked(String toEmailAddress) {
        //Delegating to the View to launch an Email Activity
        mSupplierListView.composeEmail(toEmailAddress);
    }

    /**
     * Method invoked by the {@link com.example.kaushiknsanji.storeapp.ui.MainActivity} displaying the ViewPager.
     * This is called when the User clicks on the Fab "+" button shown by the {@link com.example.kaushiknsanji.storeapp.ui.MainActivity}
     */
    @Override
    public void onFabAddClicked() {
        addNewSupplier();
    }

    /**
     * Method invoked by the {@link com.example.kaushiknsanji.storeapp.ui.MainActivity} displaying the ViewPager.
     * This is called when the User clicks on the Refresh Menu icon shown by the {@link com.example.kaushiknsanji.storeapp.ui.MainActivity}
     */
    @Override
    public void onRefreshMenuClicked() {
        triggerSuppliersLoad(true);
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
        //Returning the Loader instance for the Supplier List
        return mLoaderProvider.createCursorLoader(LoaderProvider.SUPPLIER_LIST_TYPE);
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
        mSupplierListView.hideEmptyView();
        //Initializing the ArrayList to load the SupplierLite data from the Cursor
        ArrayList<SupplierLite> supplierList = new ArrayList<>();
        //Resetting the cursor position if pointing past the last row
        if (data.isAfterLast()) {
            data.moveToPosition(-1);
        }
        //Iterating over the Cursor data and building the list
        while (data.moveToNext()) {
            supplierList.add(SupplierLite.from(data));
        }
        //Updating the View with the new data
        mSupplierListView.loadSuppliers(supplierList);
        //Hide the Progress Indicator
        mSupplierListView.hideProgressIndicator();
    }

    /**
     * Callback Method of {@link DataRepository.CursorDataLoaderCallback} invoked when there is no data
     * in the {@link Cursor} returned for the query executed
     * using a {@link android.support.v4.content.CursorLoader}
     */
    @Override
    public void onDataEmpty() {
        //Hide the Progress Indicator
        mSupplierListView.hideProgressIndicator();
        //Show empty view
        mSupplierListView.showEmptyView();
    }

    /**
     * Callback Method of {@link DataRepository.CursorDataLoaderCallback} invoked when no {@link Cursor}
     * was generated for the query executed
     * using a {@link android.support.v4.content.CursorLoader}
     */
    @Override
    public void onDataNotAvailable() {
        //Hide the Progress Indicator
        mSupplierListView.hideProgressIndicator();
        //Show error message
        mSupplierListView.showError(R.string.supplier_list_load_error);
    }

    /**
     * Callback Method of {@link DataRepository.CursorDataLoaderCallback} invoked when
     * the {@link android.support.v4.content.CursorLoader} was reset
     */
    @Override
    public void onDataReset() {
        //Updating the View with an empty list
        mSupplierListView.loadSuppliers(new ArrayList<>());
        //Show empty view
        mSupplierListView.showEmptyView();
    }

    /**
     * Callback Method of {@link DataRepository.CursorDataLoaderCallback} invoked when
     * there is a change in the content loaded by the {@link android.support.v4.content.CursorLoader}
     */
    @Override
    public void onContentChange() {
        //Retrieving the Suppliers Cursor Loader
        Loader<Cursor> suppliersLoader = mLoaderManager.getLoader(AppConstants.SUPPLIERS_LOADER);
        if (suppliersLoader != null) {
            //If Loader is already registered, restart by triggering a Content Change notification
            suppliersLoader.onContentChanged();
        } else {
            //If Loader not registered, then force restart the load
            triggerSuppliersLoad(true);
        }
    }

    /**
     * {@link ContentObserver} class that observes and notifies changes in the
     * 'supplier' table, 'supplier_contact' table and 'item_supplier_info' table
     */
    private class SupplierContentObserver extends ContentObserver {
        //URI Matcher codes for identifying the URI of Supplier and its descendant relationships
        private static final int SUPPLIER_ID = 10;
        private static final int SUPPLIER_CONTACTS_ID = 11;
        //URI Matcher codes for identifying the URI of ProductSupplierInfo and its descendant relationships
        private static final int SUPPLIER_ITEMS_ID = 20;
        //URI Matcher for matching the possible URI
        private final UriMatcher mUriMatcher = buildUriMatcher();
        //The URI observed by the Observer for changes
        private final Uri mObserverUri;
        //Main Thread Handler to dispatch the notifications to the CursorLoader on Main Thread
        private final Handler mMainThreadHandler;

        /**
         * Creates a content observer.
         *
         * @param observerUri The URI to be observed by the {@link SupplierContentObserver} instance
         */
        SupplierContentObserver(Uri observerUri) {
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

            //For "content://AUTHORITY/supplier/#" URI that references a record in 'supplier' table
            matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                    SupplierContract.PATH_SUPPLIER + "/#", SUPPLIER_ID);

            //For "content://AUTHORITY/supplier/contact/#" URI that references a set of records in 'supplier_contact' table
            //identified by 'supplier_id'
            matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                    SupplierContract.PATH_SUPPLIER + "/" + SupplierContract.PATH_SUPPLIER_CONTACT + "/#",
                    SUPPLIER_CONTACTS_ID);

            //For "content://AUTHORITY/salesinfo/supplier/#" URI that references a set of records in 'item_supplier_info' table
            //identified by 'supplier_id'
            matcher.addURI(StoreContract.CONTENT_AUTHORITY,
                    SalesContract.PATH_ITEM_SUPPLIER_INFO + "/" + SupplierContract.PATH_SUPPLIER + "/#",
                    SUPPLIER_ITEMS_ID);

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

                if (mObserverUri.equals(SupplierContract.Supplier.CONTENT_URI)) {
                    //For the URI of Supplier and its descendants

                    //Trigger notifications based on the URI
                    switch (uriMatch) {
                        case SUPPLIER_ID:
                            triggerNotification(uri);
                            break;
                        case SUPPLIER_CONTACTS_ID:
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
                }
            } else if (selfChange) {
                //When it is a self change notification, dispatch the content change
                //notification to Loader

                //Posting notification on Main Thread
                mMainThreadHandler.post(SupplierListPresenter.this::onContentChange);
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
                mMainThreadHandler.post(SupplierListPresenter.this::onContentChange);
            }
        }
    }
}
