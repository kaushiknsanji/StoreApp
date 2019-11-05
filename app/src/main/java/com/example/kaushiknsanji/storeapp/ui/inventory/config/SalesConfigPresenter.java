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
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.DataRepository;
import com.example.kaushiknsanji.storeapp.data.StoreRepository;
import com.example.kaushiknsanji.storeapp.data.local.models.Product;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductAttribute;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductSupplierSales;
import com.example.kaushiknsanji.storeapp.ui.BaseView;
import com.example.kaushiknsanji.storeapp.ui.inventory.procure.SalesProcurementActivity;
import com.example.kaushiknsanji.storeapp.ui.products.config.DefaultPhotoChangeListener;
import com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigActivity;
import com.example.kaushiknsanji.storeapp.ui.suppliers.config.SupplierConfigActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * The Presenter class that implements {@link SalesConfigContract.Presenter} on the lines of
 * Model-View-Presenter architecture. This Presenter interfaces with the App repository {@link StoreRepository}
 * to modify the Product Sales Entry in the database and updates the same to
 * the View {@link SalesConfigActivityFragment} to load and display it.
 *
 * @author Kaushik N Sanji
 */
public class SalesConfigPresenter implements SalesConfigContract.Presenter {

    //Constant used for Logs
    private static final String LOG_TAG = SalesConfigPresenter.class.getSimpleName();

    //The Product ID of an Existing Product
    private final int mProductId;

    //The View Interface of this Presenter
    @NonNull
    private final SalesConfigContract.View mSalesConfigView;

    //Instance of the App Repository
    @NonNull
    private final StoreRepository mStoreRepository;

    //Navigator that receives callbacks when navigating away from the Current Activity
    private final SalesConfigNavigator mSalesConfigNavigator;

    //Listener that receives callbacks when there is a change in the default photo of the Product
    private final DefaultPhotoChangeListener mDefaultPhotoChangeListener;

    //Stores the SKU of the Product
    private String mProductSku;

    //Stores the Name of the Product
    private String mProductName;

    //Stores the state of Product details restored,
    //to prevent updating the fields every time during System config change
    private boolean mIsProductRestored;

    //Stores the state of Supplier details restored,
    //to prevent updating the fields every time during System config change
    private boolean mAreSuppliersRestored;

    //Stores the List of Product's Suppliers downloaded with their Sales information
    private List<ProductSupplierSales> mExistingProductSupplierSalesList;

    //Stores the URI details of the Product Images
    private ArrayList<ProductImage> mProductImages;

    //Stores the value of the Original Total Available Quantity of the Product
    private int mOldTotalAvailableQuantity;

    //Stores the Updated value of the Total Available Quantity of the Product
    private int mNewTotalAvailableQuantity;

    //Stores the ProductImage to be shown
    private ProductImage mProductImageToBeShown;

    /**
     * Constructor of {@link SalesConfigPresenter}
     *
     * @param productId                  The integer value of the Product Id of an existing Product
     * @param storeRepository            Instance of {@link StoreRepository} for accessing/manipulating the data
     * @param salesConfigView            The View instance {@link SalesConfigContract.View} of this Presenter
     * @param salesConfigNavigator       Instance of {@link SalesConfigNavigator} that receives callbacks
     *                                   when navigating away from the Current Activity
     * @param defaultPhotoChangeListener Instance of {@link DefaultPhotoChangeListener} that receives callbacks
     *                                   when there is a change in the default photo of the Product
     */
    SalesConfigPresenter(int productId,
                         @NonNull StoreRepository storeRepository,
                         @NonNull SalesConfigContract.View salesConfigView,
                         @NonNull SalesConfigNavigator salesConfigNavigator,
                         @NonNull DefaultPhotoChangeListener defaultPhotoChangeListener) {
        mProductId = productId;
        mStoreRepository = storeRepository;
        mSalesConfigView = salesConfigView;
        mSalesConfigNavigator = salesConfigNavigator;
        mDefaultPhotoChangeListener = defaultPhotoChangeListener;

        //Registering the View with the Presenter
        mSalesConfigView.setPresenter(this);
    }

    /**
     * Method that initiates the work of a Presenter which is invoked by the View
     * that implements the {@link BaseView}
     */
    @Override
    public void start() {

        //Download Product data for the Product Id
        loadProductDetails();

        //Download Suppliers of the Product with Price and Inventory
        loadProductSuppliers();

    }

    /**
     * Method that updates the state of "Product details restored", and keeps it in sync with the View.
     *
     * @param isProductRestored Boolean that indicates the state of Product data restored.
     *                          <b>TRUE</b> if it had been restored; <b>FALSE</b> otherwise.
     */
    @Override
    public void updateAndSyncProductState(boolean isProductRestored) {
        //Saving the state
        mIsProductRestored = isProductRestored;

        //Syncing the state to the View
        mSalesConfigView.syncProductState(mIsProductRestored);
    }

    /**
     * Method that updates the state of "Suppliers data restored", and keeps it in sync with the View.
     *
     * @param areSuppliersRestored Boolean that indicates the state of Suppliers data restored.
     *                             <b>TRUE</b> if it had been restored; <b>FALSE</b> otherwise.
     */
    @Override
    public void updateAndSyncSuppliersState(boolean areSuppliersRestored) {
        //Saving the state
        mAreSuppliersRestored = areSuppliersRestored;

        //Syncing the state to the View
        mSalesConfigView.syncSuppliersState(mAreSuppliersRestored);
    }

    /**
     * Method that updates the original Total available quantity of the Product, and keeps
     * it in sync with the View.
     *
     * @param oldTotalAvailableQuantity Integer value of the original Total available
     *                                  quantity of the Product.
     */
    @Override
    public void updateAndSyncOldTotalAvailability(int oldTotalAvailableQuantity) {
        //Saving to the Original Total Available Quantity member
        mOldTotalAvailableQuantity = oldTotalAvailableQuantity;
        //Publishing the value to the View to keep it in sync with the Presenter
        mSalesConfigView.syncOldTotalAvailability(mOldTotalAvailableQuantity);
    }

    /**
     * Method that downloads the Product details to update the View components with the
     * product data.
     */
    private void loadProductDetails() {
        if (!mIsProductRestored) {
            //When Product data was not previously downloaded

            //Display progress indicator
            mSalesConfigView.showProgressIndicator(R.string.product_config_status_loading_existing_product);

            //Retrieving the Product Details from the Repository
            mStoreRepository.getProductDetailsById(mProductId, new DataRepository.GetQueryCallback<Product>() {
                /**
                 * Method invoked when the results are obtained
                 * for the query executed.
                 *
                 * @param product The Product details loaded for the Product Id passed
                 */
                @Override
                public void onResults(Product product) {
                    //Update the Product Name field
                    updateProductName(product.getName());

                    //Update the Product SKU field
                    updateProductSku(product.getSku());

                    //Update the Product Category
                    updateProductCategory(product.getCategory());

                    //Update the Product Description
                    updateProductDescription(product.getDescription());

                    //Update the Product Image to be shown
                    updateProductImage(product.getProductImages());

                    //Update the Product Attributes
                    updateProductAttributes(product.getProductAttributes());

                    //Marking as downloaded/restored
                    updateAndSyncProductState(true);

                    //Hide progress indicator
                    mSalesConfigView.hideProgressIndicator();
                }

                /**
                 * Method invoked when the results could not be retrieved
                 * for the query due to some error.
                 *
                 * @param messageId The String resource of the error message
                 *                  for the query execution failure
                 * @param args Variable number of arguments to replace the format specifiers
                 *             in the String resource if any
                 */
                @Override
                public void onFailure(@StringRes int messageId, @Nullable Object... args) {
                    //Hide progress indicator
                    mSalesConfigView.hideProgressIndicator();

                    //Show the error message
                    mSalesConfigView.showError(messageId, args);
                }

                /**
                 * Method invoked when there are no results
                 * for the query executed.
                 */
                @Override
                public void onEmpty() {
                    //No-op, not called for this implementation
                }
            });
        }
    }

    /**
     * Method that updates the Product Name {@code productName} to the View.
     *
     * @param productName The Product Name of the Product.
     */
    @Override
    public void updateProductName(String productName) {
        //Saving the Product Name
        mProductName = productName;
        //Updating to the View to display the Product Name
        mSalesConfigView.updateProductName(productName);
    }

    /**
     * Method that updates the Product SKU {@code productSku} to the View.
     *
     * @param productSku The Product SKU of the Product.
     */
    @Override
    public void updateProductSku(String productSku) {
        //Saving the Product SKU
        mProductSku = productSku;
        //Updating to the View to display the Product SKU
        mSalesConfigView.updateProductSku(productSku);
    }

    /**
     * Method that updates the Product Category {@code productCategory} to the View.
     *
     * @param productCategory The Product Category of the Product.
     */
    public void updateProductCategory(String productCategory) {
        mSalesConfigView.updateProductCategory(productCategory);
    }

    /**
     * Method that updates the Product Description {@code description} to the View.
     *
     * @param description The description of the Product.
     */
    public void updateProductDescription(String description) {
        mSalesConfigView.updateProductDescription(description);
    }

    /**
     * Method that loads the Selected Product Image to be shown if present.
     *
     * @param productImages The List of {@link ProductImage} of a Product.
     */
    public void updateProductImage(ArrayList<ProductImage> productImages) {
        //Finding the Product Image to be shown if Present
        if (productImages != null && productImages.size() > 0) {
            //When we have Product Images

            //Save the Product Images list
            mProductImages = productImages;

            //Send the list to the view to save for subsequent reloads
            mSalesConfigView.updateProductImages(mProductImages);

            //Saves the ProductImage to be shown
            mProductImageToBeShown = null;

            //Iterating over the list of Images to find the selected Image
            for (ProductImage productImage : mProductImages) {
                if (productImage.isDefault()) {
                    //When selected, grab the ProductImage and bail out of the loop
                    mProductImageToBeShown = productImage;
                    break;
                }
            }

            if (mProductImageToBeShown != null) {
                //Delegating to the listener to show the selected Product Image by passing the String URI of the Image to the View
                mDefaultPhotoChangeListener.showSelectedProductImage(mProductImageToBeShown.getImageUri());
            } else {
                //This case should not occur, since at least one image needs to be selected.

                //Logging the error
                Log.e(LOG_TAG, "ERROR!!! updateProductImages: Product Images found but no default image");

                //Delegating to the listener to show the Default Image
                mDefaultPhotoChangeListener.showDefaultImage();
            }

        } else {
            //When there are no images, delegate to the listener to show the default image instead
            mDefaultPhotoChangeListener.showDefaultImage();

            //Initialize the ArrayList
            mProductImages = new ArrayList<>();
        }
    }

    /**
     * Method that updates the List of Product Attributes {@code productAttributes} to the View.
     *
     * @param productAttributes The List of {@link ProductAttribute} of a Product
     */
    public void updateProductAttributes(ArrayList<ProductAttribute> productAttributes) {
        mSalesConfigView.updateProductAttributes(productAttributes);
    }

    /**
     * Method that downloads the Suppliers of the Product with Sales information
     * to update the RecyclerView with the data.
     */
    private void loadProductSuppliers() {
        if (!mAreSuppliersRestored) {
            //When Suppliers data was not previously downloaded

            //Display progress indicator
            mSalesConfigView.showProgressIndicator(R.string.sales_config_status_loading_suppliers);

            //Retrieving the Suppliers with Sales information from the Repository
            mStoreRepository.getProductSuppliersSalesInfo(mProductId, new DataRepository.GetQueryCallback<List<ProductSupplierSales>>() {
                /**
                 * Method invoked when the results are obtained
                 * for the query executed.
                 *
                 * @param productSupplierSalesList The query results in the generic type passed
                 */
                @Override
                public void onResults(List<ProductSupplierSales> productSupplierSalesList) {
                    //Update the Product Suppliers data with the list downloaded
                    updateProductSupplierSalesList(productSupplierSalesList);

                    //Marking as downloaded/restored
                    updateAndSyncSuppliersState(true);

                    //Hide progress indicator
                    mSalesConfigView.hideProgressIndicator();
                }

                /**
                 * Method invoked when there are no results
                 * for the query executed.
                 */
                @Override
                public void onEmpty() {
                    //Update the Product Suppliers data with an Empty list
                    updateProductSupplierSalesList(new ArrayList<>());

                    //Just hide progress indicator
                    mSalesConfigView.hideProgressIndicator();
                }
            });
        }
    }

    /**
     * Method that updates the List of Product's Suppliers with Sales information to the View.
     *
     * @param productSupplierSalesList List of {@link ProductSupplierSales} containing
     *                                 the Product's Suppliers with Sales information.
     */
    public void updateProductSupplierSalesList(List<ProductSupplierSales> productSupplierSalesList) {
        //Saving the existing list
        mExistingProductSupplierSalesList = productSupplierSalesList;

        //Stores the Original Total Available Quantity of the Product
        int totalAvailableQuantity = 0;

        //Creating a new ArrayList to make a deep copy of the submitted list
        ArrayList<ProductSupplierSales> newProductSupplierSalesList = new ArrayList<>();
        //Iterating over the submitted list to add the deep copy of the ProductSupplierSales of the submitted list
        //and Calculating the Original Total Available Quantity
        for (ProductSupplierSales productSupplierSales : productSupplierSalesList) {
            newProductSupplierSalesList.add((ProductSupplierSales) productSupplierSales.clone());
            //Adding and Calculating the Original Total Available Quantity
            totalAvailableQuantity += productSupplierSales.getAvailableQuantity();
        }

        //Updating the Total Available value to the view
        //(Should be done before updating the ProductSupplierSales list to the View,
        // since new total will be calculated by the Adapter)
        updateAvailability(totalAvailableQuantity);

        //Update and Sync the Original Total Available Quantity to the View
        updateAndSyncOldTotalAvailability(totalAvailableQuantity);

        //Dispatching the deep copied list of ProductSupplierSales to the View
        mSalesConfigView.loadProductSuppliersData(newProductSupplierSalesList);
    }

    /**
     * Method invoked when the user clicks on Edit button of the Product Details. This should
     * launch the {@link ProductConfigActivity}
     * for the Product to be edited.
     *
     * @param productId The Primary Key of the Product to be edited.
     */
    @Override
    public void editProduct(int productId) {
        //Delegating to the Navigator to launch the Activity for editing the Product
        mSalesConfigNavigator.launchEditProduct(productId);
    }

    /**
     * Method invoked when the user clicks on "Edit" button. This should
     * launch the {@link SupplierConfigActivity}
     * for the Supplier to be edited.
     *
     * @param supplierId The Primary key of the Supplier to be edited.
     */
    @Override
    public void editSupplier(int supplierId) {
        //Delegating to the Navigator to launch the Activity for editing the Supplier
        mSalesConfigNavigator.launchEditSupplier(supplierId);
    }

    /**
     * Method invoked when the user swipes left/right any Item View of the Product's Suppliers
     * in order to remove it from the list. This should show a Snackbar with Action UNDO.
     *
     * @param supplierCode The Supplier Code of the Supplier being swiped out/unlinked.
     */
    @Override
    public void onProductSupplierSwiped(String supplierCode) {
        mSalesConfigView.showProductSupplierSwiped(supplierCode);
    }

    /**
     * Method invoked when the user clicks on the "Procure" button of the Item View of any Product's Suppliers.
     * This should launch the {@link SalesProcurementActivity}
     * for the User to place procurement for the Product.
     *
     * @param productSupplierSales The {@link ProductSupplierSales} associated with the Item clicked.
     */
    @Override
    public void procureProduct(ProductSupplierSales productSupplierSales) {
        //Delegating to the Navigator to launch the Activity for procuring the Product
        mSalesConfigNavigator.launchProcureProduct(productSupplierSales, mProductImageToBeShown, mProductName, mProductSku);
    }

    /**
     * Method invoked when the total available quantity of the Product has been recalculated.
     *
     * @param totalAvailableQuantity Integer value of the Total Available quantity of the Product.
     */
    @Override
    public void updateAvailability(int totalAvailableQuantity) {
        //Updating the value to the New Total Available Quantity member
        mNewTotalAvailableQuantity = totalAvailableQuantity;

        //Publishing to the View
        if (mNewTotalAvailableQuantity > 0) {
            //When we have quantity

            //Show the new total to the View
            mSalesConfigView.updateAvailability(mNewTotalAvailableQuantity);
        } else {
            //When we do not have quantity

            //Show the Out of Stock Alert to the View
            mSalesConfigView.showOutOfStockAlert();
        }
    }

    /**
     * Method invoked when there is a change to the total available quantity of the Product.
     *
     * @param changeInAvailableQuantity Integer value of the change in the Total Available
     *                                  quantity of the Product with respect to the last
     *                                  Updated Availability. Can be negative to indicate
     *                                  the decrease in Available Quantity.
     */
    @Override
    public void changeAvailability(int changeInAvailableQuantity) {
        //Calculating and Updating the Availability to the View
        updateAvailability(mNewTotalAvailableQuantity + changeInAvailableQuantity);
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

                    //Update as False so that the Product's Suppliers are redownloaded and updated on resume
                    updateAndSyncSuppliersState(false);

                    //Show the Update Success message
                    mSalesConfigView.showUpdateSupplierSuccess(data.getStringExtra(SupplierConfigActivity.EXTRA_RESULT_SUPPLIER_CODE));
                } else if (resultCode == SupplierConfigActivity.RESULT_DELETE_SUPPLIER) {
                    //When the result is for the Delete action

                    //Update as False so that the Product's Suppliers are redownloaded and updated on resume
                    updateAndSyncSuppliersState(false);

                    //Show the Delete Success message
                    mSalesConfigView.showDeleteSupplierSuccess(data.getStringExtra(SupplierConfigActivity.EXTRA_RESULT_SUPPLIER_CODE));
                }

            } else if (requestCode == ProductConfigActivity.REQUEST_EDIT_PRODUCT) {
                //For an Edit Product request

                if (resultCode == ProductConfigActivity.RESULT_EDIT_PRODUCT) {
                    //When the result is for the Edit action

                    //Update as False so that the Product's data is redownloaded and updated on resume
                    updateAndSyncProductState(false);

                    //Show the Update Success message
                    mSalesConfigView.showUpdateProductSuccess(data.getStringExtra(ProductConfigActivity.EXTRA_RESULT_PRODUCT_SKU));

                } else if (resultCode == ProductConfigActivity.RESULT_DELETE_PRODUCT) {
                    //When the result is for the Delete action

                    //Send the code to the calling activity to display the delete result
                    doSetResult(ProductConfigActivity.RESULT_DELETE_PRODUCT, mProductId, mProductSku);
                }

            }

        }
    }

    /**
     * Method invoked before save operation or screen orientation change to persist
     * any data held by the view that had focus and its listener registered.
     * This clears the focus held by the view to trigger the listener, causing to persist any unsaved data.
     */
    @Override
    public void triggerFocusLost() {
        mSalesConfigView.triggerFocusLost();
    }

    /**
     * Method invoked when the 'Save' Menu button is clicked to persist the Product Sales information.
     *
     * @param updatedProductSupplierSalesList The Updated list of Product's Suppliers with
     *                                        their Price and Inventory details.
     */
    @Override
    public void onSave(ArrayList<ProductSupplierSales> updatedProductSupplierSalesList) {
        //Display save progress indicator
        mSalesConfigView.showProgressIndicator(R.string.sales_config_status_saving);

        //Saving the Item's Inventory details via the Repository
        mStoreRepository.saveUpdatedProductSalesInfo(mProductId, mProductSku,
                mExistingProductSupplierSalesList,
                updatedProductSupplierSalesList, new DataRepository.DataOperationsCallback() {
                    /**
                     * Method invoked when the database operations like insert/update/delete
                     * was successful.
                     */
                    @Override
                    public void onSuccess() {
                        //Hide progress indicator
                        mSalesConfigView.hideProgressIndicator();

                        //Set the result and finish on successful insert/update
                        doSetResult(SalesConfigActivity.RESULT_EDIT_SALES, mProductId, mProductSku);
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
                        //Hide progress indicator
                        mSalesConfigView.hideProgressIndicator();

                        //Show message for Insert/Update Failure
                        mSalesConfigView.showError(messageId, args);
                    }
                });
    }

    /**
     * Method invoked when the user clicks on the Delete Menu Action to delete the Product.
     * This should launch a Dialog for the user to reconfirm the request before proceeding
     * with the Delete Action.
     */
    @Override
    public void showDeleteProductDialog() {
        //Delegating to the View to show the dialog
        mSalesConfigView.showDeleteProductDialog();
    }

    /**
     * Method invoked when the user decides to delete the Product through the Delete Menu Action.
     * This deletes the Product from the database along with its relationship data.
     */
    @Override
    public void deleteProduct() {
        //Display the Progress Indicator
        mSalesConfigView.showProgressIndicator(R.string.product_config_status_deleting);

        //For building the list of Image File URIs to be deleted
        ArrayList<String> fileContentUriList = new ArrayList<>();

        //Reading the Product Images list and adding it to the list of URIs to be deleted
        for (ProductImage productImage : mProductImages) {
            fileContentUriList.add(productImage.getImageUri());
        }

        //Executing Product Deletion via the Repository
        mStoreRepository.deleteProductById(mProductId, new DataRepository.DataOperationsCallback() {
            /**
             * Method invoked when the database operations like insert/update/delete
             * was successful.
             */
            @Override
            public void onSuccess() {
                //Hide Progress Indicator
                mSalesConfigView.hideProgressIndicator();

                //Deleting Image files if any
                if (fileContentUriList.size() > 0) {
                    //Executing the delete operation silently on the URIs via the repository
                    mStoreRepository.deleteImageFilesSilently(fileContentUriList);
                }

                //Set the result and finish on successful delete
                doSetResult(ProductConfigActivity.RESULT_DELETE_PRODUCT, mProductId, mProductSku);
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
                mSalesConfigView.hideProgressIndicator();

                //Show the error message
                mSalesConfigView.showError(messageId, args);
            }
        });
    }

    /**
     * Method that updates the result {@code resultCode} to be sent back to the Calling Activity
     *
     * @param resultCode The integer result code to be returned to the Calling Activity.
     * @param productId  Integer containing the Id of the Product involved.
     * @param productSku String containing the SKU information of the Product involved.
     */
    @Override
    public void doSetResult(int resultCode, int productId, @NonNull String productSku) {
        mSalesConfigNavigator.doSetResult(resultCode, productId, productSku);
    }

    /**
     * Method that updates the Calling Activity that the operation was aborted.
     */
    @Override
    public void doCancel() {
        mSalesConfigNavigator.doCancel();
    }

    /**
     * Method invoked when the user clicks on the android home/up button
     * or the back key is pressed
     */
    @Override
    public void onUpOrBackAction() {
        if (mNewTotalAvailableQuantity != mOldTotalAvailableQuantity) {
            //When there is a change in Total Available Quantity, then there are some unsaved changes

            //Show the discard dialog to see if the user wants to keep editing/discard the changes
            mSalesConfigView.showDiscardDialog();
        } else {
            //When there is NO change in Total Available Quantity, then silently close the Activity
            finishActivity();
        }
    }

    /**
     * Method invoked when the user decides to exit without saving any data.
     */
    @Override
    public void finishActivity() {
        //Return back to the calling activity
        doCancel();
    }
}
