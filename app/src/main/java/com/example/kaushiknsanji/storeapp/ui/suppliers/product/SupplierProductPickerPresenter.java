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

package com.example.kaushiknsanji.storeapp.ui.suppliers.product;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.DataRepository;
import com.example.kaushiknsanji.storeapp.data.StoreRepository;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductLite;
import com.example.kaushiknsanji.storeapp.ui.BaseView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Presenter class that implements {@link SupplierProductPickerContract.Presenter} on the lines of
 * Model-View-Presenter architecture. This Presenter interfaces with the App repository {@link StoreRepository}
 * to load a list of Products available to be picked for the Supplier to sell, and updates the same to
 * the View {@link SupplierProductPickerActivityFragment} to load and display it.
 *
 * @author Kaushik N Sanji
 */
public class SupplierProductPickerPresenter implements SupplierProductPickerContract.Presenter {

    //Constant used for Logs
    private static final String LOG_TAG = SupplierProductPickerPresenter.class.getSimpleName();

    //Instance of the App Repository
    @NonNull
    private final StoreRepository mStoreRepository;

    //The View Interface of this Presenter
    @NonNull
    private final SupplierProductPickerContract.View mSupplierProductPickerView;

    //Navigator that receives callbacks when navigating away from the Current Activity
    private final SupplierProductPickerNavigator mSupplierProductPickerNavigator;

    //Listener that receives callbacks for the User select/pick actions on the RecyclerView list of Products
    private final SupplierProductPickerMultiSelectListener mSupplierProductPickerMultiSelectListener;

    //Listener that receives callbacks for the User search actions on the RecyclerView list of Products
    private final SupplierProductPickerSearchActionsListener mSupplierProductPickerSearchActionsListener;

    //Boolean used to prevent invoking the products load when already triggered
    private AtomicBoolean mIsLoadProductsCalled = new AtomicBoolean(false);

    //Stores the number of products that were currently selected for the Supplier to sell
    private int mCountOfProductsSelected;

    /**
     * Constructor of {@link SupplierProductPickerPresenter}
     *
     * @param storeRepository                Instance of {@link StoreRepository} for accessing/manipulating the data
     * @param supplierProductPickerView      The View Instance {@link SupplierProductPickerContract.View} of this Presenter
     * @param supplierProductPickerNavigator Instance of {@link SupplierProductPickerNavigator} that receives callbacks
     *                                       when navigating away from the Current Activity
     * @param multiSelectListener            Instance of {@link SupplierProductPickerMultiSelectListener} that receives callbacks for
     *                                       the User select/pick actions on the RecyclerView list of Products
     * @param searchActionsListener          Instance of {@link SupplierProductPickerSearchActionsListener} that receives callbacks for
     *                                       the User search actions on the RecyclerView list of Products
     */
    SupplierProductPickerPresenter(@NonNull StoreRepository storeRepository,
                                   @NonNull SupplierProductPickerContract.View supplierProductPickerView,
                                   @NonNull SupplierProductPickerNavigator supplierProductPickerNavigator,
                                   @NonNull SupplierProductPickerMultiSelectListener multiSelectListener,
                                   @NonNull SupplierProductPickerSearchActionsListener searchActionsListener) {
        mStoreRepository = storeRepository;
        mSupplierProductPickerView = supplierProductPickerView;
        mSupplierProductPickerNavigator = supplierProductPickerNavigator;
        mSupplierProductPickerMultiSelectListener = multiSelectListener;
        mSupplierProductPickerSearchActionsListener = searchActionsListener;

        //Registering the View with this Presenter
        mSupplierProductPickerView.setPresenter(this);
    }

    /**
     * Method that initiates the work of a Presenter which is invoked by the View
     * that implements the {@link BaseView}
     */
    @Override
    public void start() {
        //no-op
    }

    /**
     * Method that filters the Product List shown, for the Product Name/SKU/Category
     * passed in the Search Query {@code searchQueryStr}
     *
     * @param searchQueryStr The Product Name/SKU/Category to filter in the Product List
     */
    @Override
    public void filterResults(String searchQueryStr) {
        //Delegating to the Adapter to filter the Product List
        mSupplierProductPickerView.filterAdapterData(searchQueryStr, (count) -> {
            if (count > 0) {
                //When we have the records, ensure the empty view is hidden
                mSupplierProductPickerView.hideEmptyView();
            } else {
                //When we do not have the records for the search executed,
                //show the empty view with an appropriate message.
                mSupplierProductPickerView.showEmptyView(R.string.supplier_product_picker_empty_search_result, searchQueryStr);
            }
        });
    }

    /**
     * Method that clears the filter applied on the Product List shown.
     */
    @Override
    public void clearFilter() {
        mSupplierProductPickerView.clearAdapterFilter((count) -> {
            if (count > 0) {
                //When we have the records, ensure the empty view is hidden
                mSupplierProductPickerView.hideEmptyView();
            }
        });
    }

    /**
     * Method that loads a list of Products {@link ProductLite} available in the database
     * and publishes the remaining products list to the View that can be picked
     * based on the already picked products {@code registeredProducts} if any.
     *
     * @param registeredProducts List of Products {@link ProductLite} already
     *                           picked by the Supplier for selling.
     * @param remainingProducts  List of remaining Products {@link ProductLite} that can
     *                           be picked. When {@code null} products will be loaded from the
     *                           database and the remaining list will be obtained accordingly.
     * @param selectedProducts   List of Products {@link ProductLite} that were currently selected if any.
     */
    @Override
    public void loadProductsToPick(ArrayList<ProductLite> registeredProducts,
                                   @Nullable ArrayList<ProductLite> remainingProducts,
                                   @Nullable ArrayList<ProductLite> selectedProducts) {

        if (mIsLoadProductsCalled.compareAndSet(false, true)) {
            //Triggering only once

            if (remainingProducts != null) {
                //When we have the remaining products list

                //check remaining products list size and then submit
                if (remainingProducts.size() > 0) {
                    //When we previously had determined some products available to pick

                    //Hide the Empty View and submit the data to the Adapter of the RecyclerView
                    mSupplierProductPickerView.hideEmptyView();
                    mSupplierProductPickerView.submitDataToAdapter(remainingProducts, selectedProducts);

                    //Updating the count of selected products if any
                    if (selectedProducts != null) {
                        updateSelectedProductCount(selectedProducts.size());
                    }
                } else {
                    //When we previously had no products to pick

                    //Show the Empty View with the appropriate reason
                    mSupplierProductPickerView.showEmptyView(R.string.supplier_product_picker_list_empty_all_picked);

                    //Delegate to the listener to disable the Search
                    mSupplierProductPickerSearchActionsListener.disableSearch();
                }

            } else {
                //When remaining products list was not previously initialized

                //Display progress indicator
                mSupplierProductPickerView.showProgressIndicator(R.string.supplier_product_picker_status_loading_products);

                //Retrieving the entire list of Products (along with their details) configured via the Repository
                mStoreRepository.getShortProductInfoForProducts(null, new DataRepository.GetQueryCallback<List<ProductLite>>() {
                    /**
                     * Method invoked when the results are obtained
                     * for the query executed.
                     *
                     * @param products The Entire List of Products {@link ProductLite} data configured in the database.
                     */
                    @Override
                    public void onResults(List<ProductLite> products) {
                        //Load the Products list
                        ArrayList<ProductLite> allProducts = new ArrayList<>(products);
                        //Remove those that are already in the registered list of Products,
                        //to get the remaining list of products available to pick
                        allProducts.removeAll(registeredProducts);

                        //check remaining products size and then submit
                        if (allProducts.size() > 0) {
                            //When we have found some products available to pick

                            //Hide the Empty View and submit the data to the Adapter of the RecyclerView
                            mSupplierProductPickerView.hideEmptyView();
                            mSupplierProductPickerView.submitDataToAdapter(allProducts, null);
                        } else {
                            //When all products are already picked/registered for sell

                            //Show the Empty View with the appropriate reason
                            mSupplierProductPickerView.showEmptyView(R.string.supplier_product_picker_list_empty_all_picked);

                            //Delegate to the listener to disable the Search
                            mSupplierProductPickerSearchActionsListener.disableSearch();
                        }

                        //Hide progress indicator
                        mSupplierProductPickerView.hideProgressIndicator();
                    }

                    /**
                     * Method invoked when there are no results
                     * for the query executed.
                     */
                    @Override
                    public void onEmpty() {
                        //Hide progress indicator
                        mSupplierProductPickerView.hideProgressIndicator();

                        //Show the Empty View with a message to indicate the user to configure the Products first
                        mSupplierProductPickerView.showEmptyView(R.string.supplier_product_picker_list_empty_no_product);
                    }
                });
            }
        }

    }

    /**
     * Method that displays the number of Products {@code countOfProductsSelected} selected
     * for the Supplier to sell.
     *
     * @param countOfProductsSelected The Number of Products selected/picked for the Supplier to sell.
     */
    @Override
    public void updateSelectedProductCount(int countOfProductsSelected) {
        //Saving the count
        mCountOfProductsSelected = countOfProductsSelected;
        //Delegating to the listener to update the count
        mSupplierProductPickerMultiSelectListener.showSelectedCount(countOfProductsSelected);
    }

    /**
     * Method invoked when the 'Save' Menu button is clicked.
     *
     * @param registeredProducts List of Products {@link ProductLite} already
     *                           picked by the Supplier for selling.
     * @param selectedProducts   List of Products {@link ProductLite} that were
     *                           currently selected/picked for the Supplier to sell.
     */
    @Override
    public void onSave(@NonNull ArrayList<ProductLite> registeredProducts,
                       @NonNull ArrayList<ProductLite> selectedProducts) {
        //Initializing a new List to club both the list of products passed
        ArrayList<ProductLite> productsToSell = new ArrayList<>();
        productsToSell.addAll(registeredProducts);
        productsToSell.addAll(selectedProducts);
        //Set the result and finish the activity
        doSetResult(productsToSell);
    }

    /**
     * Method invoked when the user clicks on the android home/up button
     * or the back key is pressed
     */
    @Override
    public void onUpOrBackAction() {
        if (mCountOfProductsSelected > 0) {
            //When we have some products selected, then we have some unsaved changes

            //Show the discard dialog to see if the user wants to stay here/discard the changes
            mSupplierProductPickerView.showDiscardDialog();
        } else {
            //When no products are selected yet, then silently close the Activity
            finishActivity();
        }
    }

    /**
     * Method invoked when the user decides to exit without entering/saving any data
     */
    @Override
    public void finishActivity() {
        //Return back to the calling activity
        doCancel();
    }

    /**
     * Method that updates the result {@code productsToSell} to be sent back to the Calling activity.
     *
     * @param productsToSell List of Products {@link ProductLite} selected by the Supplier
     *                       for selling.
     */
    @Override
    public void doSetResult(ArrayList<ProductLite> productsToSell) {
        //Delegating to the Navigator to set the result and finish the activity
        mSupplierProductPickerNavigator.doSetResult(productsToSell);
    }

    /**
     * Method that updates the Calling Activity that the operation was aborted.
     */
    @Override
    public void doCancel() {
        //Delegating to the Navigator to abort
        mSupplierProductPickerNavigator.doCancel();
    }
}
