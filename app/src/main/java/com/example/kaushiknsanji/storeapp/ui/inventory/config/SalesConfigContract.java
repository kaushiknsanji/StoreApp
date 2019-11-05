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

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.example.kaushiknsanji.storeapp.data.local.models.ProductAttribute;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductSupplierSales;
import com.example.kaushiknsanji.storeapp.ui.BasePresenter;
import com.example.kaushiknsanji.storeapp.ui.BaseView;

import java.util.ArrayList;
import java.util.List;

/**
 * Contract Interface for the View {@link SalesConfigActivityFragment} and its Presenter {@link SalesConfigPresenter}.
 *
 * @author Kaushik N Sanji
 */
public interface SalesConfigContract {

    /**
     * The View Interface implemented by {@link SalesConfigActivityFragment}
     */
    interface View extends BaseView<Presenter> {

        /**
         * Method invoked to keep the state of "Product details restored", in sync with the Presenter.
         *
         * @param isProductRestored Boolean that indicates the state of Product data restored.
         *                          <b>TRUE</b> if it had been restored; <b>FALSE</b> otherwise.
         */
        void syncProductState(boolean isProductRestored);

        /**
         * Method invoked to keep the state of "Suppliers data restored", in sync with the Presenter.
         *
         * @param areSuppliersRestored Boolean that indicates the state of Suppliers data restored.
         *                             <b>TRUE</b> if it had been restored; <b>FALSE</b> otherwise.
         */
        void syncSuppliersState(boolean areSuppliersRestored);

        /**
         * Method invoked to keep the original Total available quantity of the Product,
         * in sync with the Presenter.
         *
         * @param oldTotalAvailableQuantity Integer value of the original Total available
         *                                  quantity of the Product.
         */
        void syncOldTotalAvailability(int oldTotalAvailableQuantity);

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
         * Method invoked when an error is encountered during Product/Suppliers information
         * retrieval or save process.
         *
         * @param messageId String Resource of the error Message to be displayed
         * @param args      Variable number of arguments to replace the format specifiers
         *                  in the String resource if any
         */
        void showError(@StringRes int messageId, @Nullable Object... args);

        /**
         * Method that updates the Product Name {@code productName} to the View.
         *
         * @param productName The Product Name of the Product.
         */
        void updateProductName(String productName);

        /**
         * Method that updates the Product SKU {@code productSku} to the View.
         *
         * @param productSku The Product SKU of the Product.
         */
        void updateProductSku(String productSku);

        /**
         * Method that updates the Product Category {@code productCategory} to the View.
         *
         * @param productCategory The Product Category of the Product.
         */
        void updateProductCategory(String productCategory);

        /**
         * Method that updates the Product Description {@code description} to the View.
         *
         * @param description The description of the Product.
         */
        void updateProductDescription(String description);

        /**
         * Method that updates the List of Product Images {@code productImages} to the View
         *
         * @param productImages The List of {@link ProductImage} of a Product.
         */
        void updateProductImages(ArrayList<ProductImage> productImages);

        /**
         * Method that updates the List of Product Attributes {@code productAttributes} to the View.
         *
         * @param productAttributes The List of {@link ProductAttribute} of a Product
         */
        void updateProductAttributes(ArrayList<ProductAttribute> productAttributes);

        /**
         * Method that updates the Adapter of the RecyclerView List of Product's Suppliers with
         * Sales information.
         *
         * @param productSupplierSalesList List of {@link ProductSupplierSales} containing
         *                                 the Product's Suppliers with Sales information.
         */
        void loadProductSuppliersData(ArrayList<ProductSupplierSales> productSupplierSalesList);

        /**
         * Method invoked when the total available quantity of the Product has been recalculated.
         *
         * @param totalAvailableQuantity Integer value of the Total Available quantity of the Product.
         */
        void updateAvailability(int totalAvailableQuantity);

        /**
         * Method invoked to show the "Out Of Stock!" alert when the Total Available quantity
         * of the Product is 0.
         */
        void showOutOfStockAlert();

        /**
         * Method invoked when the user swipes left/right any Item View of the Product's Suppliers
         * in order to remove it from the list. This should show a Snackbar with Action UNDO.
         *
         * @param supplierCode The Supplier Code of the Supplier being swiped out/unlinked.
         */
        void showProductSupplierSwiped(String supplierCode);

        /**
         * Method that displays a message on Success of Updating a Product.
         *
         * @param productSku String containing the SKU of the Product that was updated successfully.
         */
        void showUpdateProductSuccess(String productSku);

        /**
         * Method that displays a message on Success of updating an Existing Supplier.
         *
         * @param supplierCode String containing the code of the Supplier that was updated successfully.
         */
        void showUpdateSupplierSuccess(String supplierCode);

        /**
         * Method that displays a message on Success of Deleting an Existing Supplier.
         *
         * @param supplierCode String containing the code of the Supplier that was deleted successfully.
         */
        void showDeleteSupplierSuccess(String supplierCode);

        /**
         * Method invoked before save operation or screen orientation change to persist
         * any data held by the view that had focus and its listener registered.
         * This clears the focus held by the view to trigger the listener, causing to persist any unsaved data.
         */
        void triggerFocusLost();

        /**
         * Method invoked by the Presenter to display the Discard dialog,
         * requesting the User whether to keep editing/discard the changes
         */
        void showDiscardDialog();

        /**
         * Method invoked when the user clicks on the Delete Menu Action to delete the Product.
         * This should launch a Dialog for the user to reconfirm the request before proceeding
         * with the Delete Action.
         */
        void showDeleteProductDialog();

    }

    /**
     * The Presenter Interface implemented by {@link SalesConfigPresenter}
     */
    interface Presenter extends BasePresenter {

        /**
         * Method that updates the state of "Product details restored", and keeps it in sync with the View.
         *
         * @param isProductRestored Boolean that indicates the state of Product data restored.
         *                          <b>TRUE</b> if it had been restored; <b>FALSE</b> otherwise.
         */
        void updateAndSyncProductState(boolean isProductRestored);

        /**
         * Method that updates the state of "Suppliers data restored", and keeps it in sync with the View.
         *
         * @param areSuppliersRestored Boolean that indicates the state of Suppliers data restored.
         *                             <b>TRUE</b> if it had been restored; <b>FALSE</b> otherwise.
         */
        void updateAndSyncSuppliersState(boolean areSuppliersRestored);

        /**
         * Method that updates the original Total available quantity of the Product, and keeps
         * it in sync with the View.
         *
         * @param oldTotalAvailableQuantity Integer value of the original Total available
         *                                  quantity of the Product.
         */
        void updateAndSyncOldTotalAvailability(int oldTotalAvailableQuantity);

        /**
         * Method that updates the Product Name {@code productName} to the View.
         *
         * @param productName The Product Name of the Product.
         */
        void updateProductName(String productName);

        /**
         * Method that updates the Product SKU {@code productSku} to the View.
         *
         * @param productSku The Product SKU of the Product.
         */
        void updateProductSku(String productSku);

        /**
         * Method that updates the Product Category {@code productCategory} to the View.
         *
         * @param productCategory The Product Category of the Product.
         */
        void updateProductCategory(String productCategory);

        /**
         * Method that updates the Product Description {@code description} to the View.
         *
         * @param description The description of the Product.
         */
        void updateProductDescription(String description);

        /**
         * Method that loads the Selected Product Image to be shown if present.
         *
         * @param productImages The List of {@link ProductImage} of a Product.
         */
        void updateProductImage(ArrayList<ProductImage> productImages);

        /**
         * Method that updates the List of Product Attributes {@code productAttributes} to the View.
         *
         * @param productAttributes The List of {@link ProductAttribute} of a Product
         */
        void updateProductAttributes(ArrayList<ProductAttribute> productAttributes);

        /**
         * Method that updates the List of Product's Suppliers with Sales information to the View.
         *
         * @param productSupplierSalesList List of {@link ProductSupplierSales} containing
         *                                 the Product's Suppliers with Sales information.
         */
        void updateProductSupplierSalesList(List<ProductSupplierSales> productSupplierSalesList);

        /**
         * Method invoked when the user clicks on Edit button of the Product Details. This should
         * launch the {@link com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigActivity}
         * for the Product to be edited.
         *
         * @param productId The Primary Key of the Product to be edited.
         */
        void editProduct(int productId);

        /**
         * Method invoked when the user clicks on "Edit" button. This should
         * launch the {@link com.example.kaushiknsanji.storeapp.ui.suppliers.config.SupplierConfigActivity}
         * for the Supplier to be edited.
         *
         * @param supplierId The Primary key of the Supplier to be edited.
         */
        void editSupplier(int supplierId);

        /**
         * Method invoked when the user swipes left/right any Item View of the Product's Suppliers
         * in order to remove it from the list. This should show a Snackbar with Action UNDO.
         *
         * @param supplierCode The Supplier Code of the Supplier being swiped out/unlinked.
         */
        void onProductSupplierSwiped(String supplierCode);

        /**
         * Method invoked when the user clicks on the "Procure" button of the Item View of any Product's Suppliers.
         * This should launch the {@link com.example.kaushiknsanji.storeapp.ui.inventory.procure.SalesProcurementActivity}
         * for the User to place procurement for the Product.
         *
         * @param productSupplierSales The {@link ProductSupplierSales} associated with the Item clicked.
         */
        void procureProduct(ProductSupplierSales productSupplierSales);

        /**
         * Method invoked when the total available quantity of the Product has been recalculated.
         *
         * @param totalAvailableQuantity Integer value of the Total Available quantity of the Product.
         */
        void updateAvailability(int totalAvailableQuantity);

        /**
         * Method invoked when there is a change to the total available quantity of the Product.
         *
         * @param changeInAvailableQuantity Integer value of the change in the Total Available
         *                                  quantity of the Product with respect to the last
         *                                  Updated Availability. Can be negative to indicate
         *                                  the decrease in Available Quantity.
         */
        void changeAvailability(int changeInAvailableQuantity);

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
         * Method invoked before save operation or screen orientation change to persist
         * any data held by the view that had focus and its listener registered.
         * This clears the focus held by the view to trigger the listener, causing to persist any unsaved data.
         */
        void triggerFocusLost();

        /**
         * Method invoked when the 'Save' Menu button is clicked to persist the Product Sales information.
         *
         * @param updatedProductSupplierSalesList The Updated list of Product's Suppliers with
         *                                        their Price and Inventory details.
         */
        void onSave(ArrayList<ProductSupplierSales> updatedProductSupplierSalesList);

        /**
         * Method invoked when the user clicks on the Delete Menu Action to delete the Product.
         * This should launch a Dialog for the user to reconfirm the request before proceeding
         * with the Delete Action.
         */
        void showDeleteProductDialog();

        /**
         * Method invoked when the user decides to delete the Product through the Delete Menu Action.
         * This deletes the Product from the database along with its relationship data.
         */
        void deleteProduct();

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
         * Method invoked when the user clicks on the android home/up button
         * or the back key is pressed
         */
        void onUpOrBackAction();

        /**
         * Method invoked when the user decides to exit without saving any data.
         */
        void finishActivity();

    }

}