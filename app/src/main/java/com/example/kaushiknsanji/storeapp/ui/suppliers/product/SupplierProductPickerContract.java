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
import android.support.annotation.StringRes;
import android.widget.Filter;

import com.example.kaushiknsanji.storeapp.data.local.models.ProductLite;
import com.example.kaushiknsanji.storeapp.ui.BasePresenter;
import com.example.kaushiknsanji.storeapp.ui.BaseView;

import java.util.ArrayList;

/**
 * Contract Interface for the View {@link SupplierProductPickerActivityFragment} and its Presenter {@link SupplierProductPickerPresenter}.
 *
 * @author Kaushik N Sanji
 */
public interface SupplierProductPickerContract {

    /**
     * The View Interface implemented by {@link SupplierProductPickerActivityFragment}
     */
    interface View extends BaseView<Presenter> {

        /**
         * Method that displays the Progress indicator
         *
         * @param statusTextId String resource for the status of the Progress to be shown.
         */
        void showProgressIndicator(@StringRes int statusTextId);

        /**
         * Method that hides the Progress indicator
         */
        void hideProgressIndicator();

        /**
         * Method invoked when an error is encountered during Supplier information
         * retrieval or save process.
         *
         * @param messageId String Resource of the error Message to be displayed
         * @param args      Variable number of arguments to replace the format specifiers
         *                  in the String resource if any
         */
        void showError(@StringRes int messageId, @Nullable Object... args);

        /**
         * Method invoked when we have the Product List. This should show the Product List and
         * hide the Empty List TextView.
         */
        void hideEmptyView();

        /**
         * Method invoked when the Product List is empty. This should show a TextView with a
         * Text {@code emptyTextResId} that tells the reason why the list is empty.
         *
         * @param emptyTextResId The String Resource of the empty message to be shown.
         * @param args           Variable number of arguments to replace the format specifiers
         *                       in the String resource if any
         */
        void showEmptyView(@StringRes int emptyTextResId, @Nullable Object... args);

        /**
         * Method invoked to update the list of Products {@link ProductLite} displayed by the Adapter
         * of the RecyclerView.
         *
         * @param remainingProducts List of remaining Products {@link ProductLite} that can
         *                          be picked, which is the data displayed by the Adapter.
         * @param selectedProducts  List of Products {@link ProductLite} that were currently selected if any.
         */
        void submitDataToAdapter(ArrayList<ProductLite> remainingProducts,
                                 @Nullable ArrayList<ProductLite> selectedProducts);

        /**
         * Method invoked to filter the Product List shown by the Adapter, for the Product Name/SKU/Category
         * passed in the Search Query {@code searchQueryStr}
         *
         * @param searchQueryStr The Product Name/SKU/Category to filter in the Product List
         * @param filterListener A listener notified upon completion of the operation
         */
        void filterAdapterData(String searchQueryStr, Filter.FilterListener filterListener);

        /**
         * Method invoked to clear the filter applied on the Adapter of the RecyclerView
         *
         * @param filterListener A listener notified upon completion of the operation
         */
        void clearAdapterFilter(Filter.FilterListener filterListener);

        /**
         * Method invoked by the Presenter to display the Discard dialog,
         * requesting the User whether to stay here/discard the changes
         */
        void showDiscardDialog();
    }

    /**
     * The Presenter Interface implemented by {@link SupplierProductPickerPresenter}
     */
    interface Presenter extends BasePresenter {

        /**
         * Method that filters the Product List shown, for the Product Name/SKU/Category
         * passed in the Search Query {@code searchQueryStr}
         *
         * @param searchQueryStr The Product Name/SKU/Category to filter in the Product List
         */
        void filterResults(String searchQueryStr);

        /**
         * Method that clears the filter applied on the Product List shown.
         */
        void clearFilter();

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
        void loadProductsToPick(ArrayList<ProductLite> registeredProducts,
                                @Nullable ArrayList<ProductLite> remainingProducts,
                                @Nullable ArrayList<ProductLite> selectedProducts);

        /**
         * Method that displays the number of Products {@code countOfProductsSelected} selected
         * for the Supplier to sell.
         *
         * @param countOfProductsSelected The Number of Products selected/picked for the Supplier to sell.
         */
        void updateSelectedProductCount(int countOfProductsSelected);

        /**
         * Method invoked when the 'Save' Menu button is clicked.
         *
         * @param registeredProducts List of Products {@link ProductLite} already
         *                           picked by the Supplier for selling.
         * @param selectedProducts   List of Products {@link ProductLite} that were
         *                           currently selected/picked for the Supplier to sell.
         */
        void onSave(@NonNull ArrayList<ProductLite> registeredProducts, @NonNull ArrayList<ProductLite> selectedProducts);

        /**
         * Method invoked when the user clicks on the android home/up button
         * or the back key is pressed
         */
        void onUpOrBackAction();

        /**
         * Method invoked when the user decides to exit without entering/saving any data
         */
        void finishActivity();

        /**
         * Method that updates the result {@code productsToSell} to be sent back to the Calling activity.
         *
         * @param productsToSell List of Products {@link ProductLite} selected by the Supplier
         *                       for selling.
         */
        void doSetResult(ArrayList<ProductLite> productsToSell);

        /**
         * Method that updates the Calling Activity that the operation was aborted.
         */
        void doCancel();
    }
}
