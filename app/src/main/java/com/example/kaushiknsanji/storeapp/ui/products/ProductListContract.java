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
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityOptionsCompat;

import com.example.kaushiknsanji.storeapp.data.local.models.ProductLite;
import com.example.kaushiknsanji.storeapp.ui.PagerPresenter;
import com.example.kaushiknsanji.storeapp.ui.PagerView;

import java.util.ArrayList;

/**
 * Contract Interface for the View {@link ProductListFragment} and its Presenter {@link ProductListPresenter}
 *
 * @author Kaushik N Sanji
 */
public interface ProductListContract {

    /**
     * View interface implemented by {@link ProductListFragment}
     */
    interface View extends PagerView<Presenter> {

        /**
         * Method that displays the Progress indicator
         */
        void showProgressIndicator();

        /**
         * Method that hides the Progress indicator
         */
        void hideProgressIndicator();

        /**
         * Method that updates the RecyclerView's Adapter with new {@code productList} data.
         *
         * @param productList List of Products defined by {@link ProductLite},
         *                    loaded from the database
         */
        void loadProducts(ArrayList<ProductLite> productList);

        /**
         * Method invoked when the user clicks on the "Edit" button on the Item View or the Item View itself
         * to edit the Product details. This should
         * launch the {@link com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigActivity}
         * for the Product to be edited.
         *
         * @param productId             The Primary Key of the Product to be edited.
         * @param activityOptionsCompat Instance of {@link ActivityOptionsCompat} that has the
         *                              details for Shared Element Transition
         */
        void launchEditProduct(int productId, ActivityOptionsCompat activityOptionsCompat);

        /**
         * Method invoked when an error is encountered during Product information
         * retrieval or delete process.
         *
         * @param messageId String Resource of the error Message to be displayed
         * @param args      Variable number of arguments to replace the format specifiers
         *                  in the String resource if any
         */
        void showError(@StringRes int messageId, @Nullable Object... args);

        /**
         * Method that displays a message on Success of adding a New Product.
         *
         * @param productSku String containing the SKU of the Product that was added successfully.
         */
        void showAddSuccess(String productSku);

        /**
         * Method that displays a message on Success of Updating an Existing Product.
         *
         * @param productSku String containing the SKU of the Product that was updated successfully.
         */
        void showUpdateSuccess(String productSku);

        /**
         * Method that displays a message on Success of Deleting an Existing Product
         *
         * @param productSku String containing the SKU of the Product that was deleted successfully.
         */
        void showDeleteSuccess(String productSku);

        /**
         * Method invoked when the Product List is empty. This should show a TextView with a
         * Text that suggests Users to add Products into the database.
         */
        void showEmptyView();

        /**
         * Method invoked when we have the Product List. This should show the Product List and
         * hide the Empty List TextView and Step Number drawable.
         */
        void hideEmptyView();

        /**
         * Method invoked when the user clicks on the FAB button to add a New Product
         * into the database. This should
         * launch the {@link com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigActivity}
         * for configuring a New Product.
         */
        void launchAddNewProduct();

    }

    /**
     * Presenter Interface implemented by {@link ProductListPresenter}
     */
    interface Presenter extends PagerPresenter {

        /**
         * Method that triggers the CursorLoader to load the Products from the database
         *
         * @param forceLoad Boolean value that controls the nature of the trigger
         *                  <br/><b>TRUE</b> to forcefully start a new load process
         *                  <br/><b>FALSE</b> to start a new/existing load process
         */
        void triggerProductsLoad(boolean forceLoad);

        /**
         * Method invoked when the user clicks on the "Edit" button on the Item View or the Item View itself
         * to edit the Product details. This should
         * launch the {@link com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigActivity}
         * for the Product to be edited.
         *
         * @param productId             The Primary Key of the Product to be edited.
         * @param activityOptionsCompat Instance of {@link ActivityOptionsCompat} that has the
         *                              details for Shared Element Transition
         */
        void editProduct(int productId, ActivityOptionsCompat activityOptionsCompat);

        /**
         * Method invoked when the user clicks on the "Delete" button on the Item View
         * to delete the Product identified by {@link ProductLite#mId}
         *
         * @param product The {@link ProductLite} instance of the Product to be deleted.
         */
        void deleteProduct(ProductLite product);

        /**
         * Method invoked when the user clicks on the FAB button to add a New Product
         * into the database.
         */
        void addNewProduct();

        /**
         * Invoked from a previous call to
         * {@link android.support.v4.app.FragmentActivity#startActivityForResult(Intent, int)}.
         *
         * @param requestCode The integer request code originally supplied to
         *                    startActivityForResult(), allowing you to identify who this
         *                    result came from.
         * @param resultCode  The integer result code returned by the child activity
         *                    through its setResult().
         * @param data        An Intent, which can return result data to the caller
         *                    (various data can be attached to Intent "extras").
         */
        void onActivityResult(int requestCode, int resultCode, Intent data);

        /**
         * Method invoked when the View is about to be destroyed.
         * This method should release any critical resources held by the Presenter.
         */
        void releaseResources();
    }

}
