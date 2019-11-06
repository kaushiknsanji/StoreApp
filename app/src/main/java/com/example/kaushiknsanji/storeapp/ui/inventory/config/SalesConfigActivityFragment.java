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

import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductAttribute;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductSupplierSales;
import com.example.kaushiknsanji.storeapp.ui.BasePresenter;
import com.example.kaushiknsanji.storeapp.ui.BaseView;
import com.example.kaushiknsanji.storeapp.ui.common.ProgressDialogFragment;
import com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigContract;
import com.example.kaushiknsanji.storeapp.ui.suppliers.config.SupplierConfigActivity;
import com.example.kaushiknsanji.storeapp.utils.OrientationUtility;
import com.example.kaushiknsanji.storeapp.utils.SnackbarUtility;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * Content Fragment of {@link SalesConfigActivity} that inflates the layout 'R.layout.fragment_sales_config'
 * to display the configuration details of the Product and its Suppliers with their Sales inventory.
 * The Sales Inventory of the Product can be updated across its listed Suppliers and
 * procurement request can also be placed with any Supplier listed. This implements the
 * {@link SalesConfigContract.View} on the lines of Model-View-Presenter architecture.
 *
 * @author Kaushik N Sanji
 */
public class SalesConfigActivityFragment extends Fragment implements SalesConfigContract.View, View.OnClickListener {

    //Constant used for logs
    private static final String LOG_TAG = SalesConfigActivityFragment.class.getSimpleName();

    //The Bundle argument constant of this Fragment
    private static final String ARGUMENT_INT_PRODUCT_ID = "argument.PRODUCT_ID";

    //Bundle constants for persisting the data throughout System config changes
    private static final String BUNDLE_PRODUCT_NAME_KEY = "SalesConfig.ProductName";
    private static final String BUNDLE_PRODUCT_SKU_KEY = "SalesConfig.ProductSku";
    private static final String BUNDLE_PRODUCT_DESCRIPTION_KEY = "SalesConfig.ProductDescription";
    private static final String BUNDLE_PRODUCT_CATEGORY_KEY = "SalesConfig.ProductCategory";
    private static final String BUNDLE_PRODUCT_ORIGINAL_TOTAL_AVAIL_QTY_INT_KEY = "SalesConfig.OriginalTotalAvailableQuantity";
    private static final String BUNDLE_PRODUCT_IMAGES_LIST_KEY = "SalesConfig.ProductImages";
    private static final String BUNDLE_PRODUCT_ATTRS_LIST_KEY = "SalesConfig.ProductAttributes";
    private static final String BUNDLE_PRODUCT_SUPPLIERS_LIST_KEY = "SalesConfig.ProductSuppliers";
    private static final String BUNDLE_PRODUCT_RESTORED_BOOL_KEY = "SalesConfig.IsProductRestored";
    private static final String BUNDLE_SUPPLIERS_RESTORED_BOOL_KEY = "SalesConfig.AreSuppliersRestored";

    //The Presenter for this View
    private SalesConfigContract.Presenter mPresenter;

    //Stores the instance of the View components required
    private TextView mTextViewProductName;
    private TextView mTextViewProductSku;
    private TextView mTextViewProductDesc;
    private TextView mTextViewProductCategory;
    private TextView mTextViewProductAvailableQuantity;
    private TableLayout mTableLayoutProductAttrs;
    private RecyclerView mRecyclerViewProductSuppliers;

    //RecyclerView Adapter for Product's Suppliers
    private ProductSuppliersAdapter mProductSuppliersAdapter;

    //Stores the Product ID, retrieved from Bundle arguments passed
    private int mProductId;
    //Stores the Product Attributes list of the Product
    private ArrayList<ProductAttribute> mProductAttributes;
    //Stores the URI details of the Product Images
    private ArrayList<ProductImage> mProductImages;

    //Stores the state of Product details restored,
    //to prevent updating the fields every time during System config change
    private boolean mIsProductRestored;

    //Stores the state of Supplier details restored,
    //to prevent updating the fields every time during System config change
    private boolean mAreSuppliersRestored;

    //Stores the value of the Original Total Available Quantity of the Product
    private int mOldTotalAvailableQuantity;
    /**
     * The {@link AlertDialog} Click Listener for the Product Delete Menu.
     */
    private DialogInterface.OnClickListener mProductDeleteDialogOnClickListener = new DialogInterface.OnClickListener() {
        /**
         * This method will be invoked when a button in the dialog is clicked.
         *
         * @param dialog the dialog that received the click
         * @param which  the button that was clicked (ex.
         *               {@link DialogInterface#BUTTON_POSITIVE}) or the position
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //Taking action based on the button clicked
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //For "Yes" Button

                    //Dismiss the dialog
                    dialog.dismiss();
                    //Unlock orientation
                    OrientationUtility.unlockScreenOrientation(requireActivity());
                    //Dispatch to the Presenter to delete the Product
                    mPresenter.deleteProduct();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //For "No" Button

                    //Dismiss the dialog
                    dialog.dismiss();
                    //Unlock orientation
                    OrientationUtility.unlockScreenOrientation(requireActivity());
                    break;
            }
        }
    };
    /**
     * The {@link AlertDialog} Click Listener for the Unsaved changes dialog
     */
    private DialogInterface.OnClickListener mUnsavedDialogOnClickListener = new DialogInterface.OnClickListener() {
        /**
         * This method will be invoked when a button in the dialog is clicked.
         *
         * @param dialog the dialog that received the click
         * @param which  the button that was clicked (ex.
         *               {@link DialogInterface#BUTTON_POSITIVE}) or the position
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //Taking action based on the button clicked
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //For "Save" button

                    //Dismiss the dialog
                    dialog.dismiss();
                    //Unlock orientation
                    OrientationUtility.unlockScreenOrientation(requireActivity());
                    //Start saving the Product Sales
                    saveProductSales();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //For "Discard" button

                    //Dismiss the dialog
                    dialog.dismiss();
                    //Unlock orientation
                    OrientationUtility.unlockScreenOrientation(requireActivity());
                    //Dispatch to the Presenter to finish the Activity without saving changes
                    mPresenter.finishActivity();
                    break;
                case DialogInterface.BUTTON_NEUTRAL:
                    //For "Keep Editing" button

                    //Just Dismiss the dialog
                    dialog.dismiss();
                    //Unlock orientation
                    OrientationUtility.unlockScreenOrientation(requireActivity());
                    break;
            }
        }
    };

    /**
     * Mandatory Empty Constructor of {@link SalesConfigActivityFragment}
     * This is required by the {@link android.support.v4.app.FragmentManager} to instantiate
     * the fragment (e.g. upon screen orientation changes).
     */
    public SalesConfigActivityFragment() {
    }

    /**
     * Static Factory Constructor that creates an instance of {@link SalesConfigActivityFragment}
     * using the provided {@code productId}
     *
     * @param productId The integer value of the Product Id of an existing Product;
     * @return Instance of {@link SalesConfigActivityFragment}
     */
    public static SalesConfigActivityFragment newInstance(int productId) {
        //Saving the arguments passed, in a Bundle
        Bundle args = new Bundle(1);
        args.putInt(ARGUMENT_INT_PRODUCT_ID, productId);

        //Instantiating the Fragment
        SalesConfigActivityFragment fragment = new SalesConfigActivityFragment();
        //Passing the Bundle as Arguments to this Fragment
        fragment.setArguments(args);

        //Returning the fragment instance
        return fragment;
    }

    /**
     * Called to do initial creation of a fragment.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Indicating that this fragment has menu options to show
        setHasOptionsMenu(true);
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null (which
     * is the default implementation).  This will be called between
     * {@link #onCreate(Bundle)} and {@link #onActivityCreated(Bundle)}.
     * <p>
     * <p>If you return a View from here, you will later be called in
     * {@link #onDestroyView} when the view is being released.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Returns the View for the fragment's UI ('R.layout.fragment_sales_config')
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout 'R.layout.fragment_sales_config' for this fragment
        //Passing false as we are attaching the layout ourselves
        View rootView = inflater.inflate(R.layout.fragment_sales_config, container, false);

        //Finding the views to initialize
        mTextViewProductName = rootView.findViewById(R.id.text_sales_config_product_name);
        mTextViewProductSku = rootView.findViewById(R.id.text_sales_config_product_sku);
        mTextViewProductDesc = rootView.findViewById(R.id.text_sales_config_product_desc);
        mTextViewProductCategory = rootView.findViewById(R.id.text_sales_config_product_category);
        mTextViewProductAvailableQuantity = rootView.findViewById(R.id.text_sales_config_total_available_quantity);
        mTableLayoutProductAttrs = rootView.findViewById(R.id.tablelayout_sales_config_product_attrs);
        mRecyclerViewProductSuppliers = rootView.findViewById(R.id.recyclerview_sales_config_suppliers);

        //Registering Click listener on Product Edit Image Button
        rootView.findViewById(R.id.imgbtn_sales_config_product_edit).setOnClickListener(this);

        //Retrieving the Product Id and Supplier Id from the Bundle
        Bundle arguments = getArguments();
        if (arguments != null) {
            mProductId = arguments.getInt(ARGUMENT_INT_PRODUCT_ID, ProductConfigContract.NEW_PRODUCT_INT);
        }

        //Initialize RecyclerView for Product's Suppliers
        setupProductSuppliersRecyclerView();

        //Returning the prepared layout
        return rootView;
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to {@link android.support.v4.app.FragmentActivity#onResume() Activity.onResume} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onResume() {
        super.onResume();

        if (mProductId == ProductConfigContract.NEW_PRODUCT_INT) {
            //When the Product Id is not an existing Id, then finish the Activity
            mPresenter.doCancel();
        } else {
            //When the Product Id is valid, start downloading the required data
            mPresenter.start();
        }
    }

    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * {@link #setRetainInstance(boolean)} to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after {@link #onCreateView}
     * and before {@link #onViewStateRestored(Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            //On Subsequent launch

            //Restoring Product Name
            mPresenter.updateProductName(savedInstanceState.getString(BUNDLE_PRODUCT_NAME_KEY));

            //Restoring Product SKU
            mPresenter.updateProductSku(savedInstanceState.getString(BUNDLE_PRODUCT_SKU_KEY));

            //Restoring Product Description
            mPresenter.updateProductDescription(savedInstanceState.getString(BUNDLE_PRODUCT_DESCRIPTION_KEY));

            //Restoring Product Category
            mPresenter.updateProductCategory(savedInstanceState.getString(BUNDLE_PRODUCT_CATEGORY_KEY));

            //Restoring Product Images
            mPresenter.updateProductImage(savedInstanceState.getParcelableArrayList(BUNDLE_PRODUCT_IMAGES_LIST_KEY));

            //Restoring Product Attributes
            mPresenter.updateProductAttributes(savedInstanceState.getParcelableArrayList(BUNDLE_PRODUCT_ATTRS_LIST_KEY));

            //Restoring Product's Suppliers and Sales information
            mPresenter.updateProductSupplierSalesList(savedInstanceState.getParcelableArrayList(BUNDLE_PRODUCT_SUPPLIERS_LIST_KEY));

            //Restoring the Original Total Available Quantity after loading the Sales information
            //to overwrite with the corrected value
            mPresenter.updateAndSyncOldTotalAvailability(savedInstanceState.getInt(BUNDLE_PRODUCT_ORIGINAL_TOTAL_AVAIL_QTY_INT_KEY));

            //Restoring the state of Product data restored
            mPresenter.updateAndSyncProductState(savedInstanceState.getBoolean(BUNDLE_PRODUCT_RESTORED_BOOL_KEY, false));

            //Restoring the state of Suppliers data restored
            mPresenter.updateAndSyncSuppliersState(savedInstanceState.getBoolean(BUNDLE_SUPPLIERS_RESTORED_BOOL_KEY, false));
        }
    }

    /**
     * Called to ask the fragment to save its current dynamic state, so it
     * can later be reconstructed in a new instance of its process is
     * restarted.  If a new instance of the fragment later needs to be
     * created, the data you place in the Bundle here will be available
     * in the Bundle given to {@link #onCreate(Bundle)},
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}, and
     * {@link #onActivityCreated(Bundle)}.
     * <p>
     * <p>This corresponds to {@link android.support.v4.app.FragmentActivity#onSaveInstanceState(Bundle)
     * Activity.onSaveInstanceState(Bundle)} and most of the discussion there
     * applies here as well.
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //Trigger Focus Loss to capture any value partially entered
        mPresenter.triggerFocusLost();

        //Saving the state
        outState.putString(BUNDLE_PRODUCT_NAME_KEY, mTextViewProductName.getText().toString());
        outState.putString(BUNDLE_PRODUCT_SKU_KEY, mTextViewProductSku.getText().toString());
        outState.putString(BUNDLE_PRODUCT_DESCRIPTION_KEY, mTextViewProductDesc.getText().toString());
        outState.putString(BUNDLE_PRODUCT_CATEGORY_KEY, mTextViewProductCategory.getText().toString());
        outState.putInt(BUNDLE_PRODUCT_ORIGINAL_TOTAL_AVAIL_QTY_INT_KEY, mOldTotalAvailableQuantity);
        outState.putParcelableArrayList(BUNDLE_PRODUCT_IMAGES_LIST_KEY, mProductImages);
        outState.putParcelableArrayList(BUNDLE_PRODUCT_ATTRS_LIST_KEY, mProductAttributes);
        outState.putParcelableArrayList(BUNDLE_PRODUCT_SUPPLIERS_LIST_KEY, mProductSuppliersAdapter.getProductSupplierSalesList());
        outState.putBoolean(BUNDLE_PRODUCT_RESTORED_BOOL_KEY, mIsProductRestored);
        outState.putBoolean(BUNDLE_SUPPLIERS_RESTORED_BOOL_KEY, mAreSuppliersRestored);
    }

    /**
     * Method that registers the Presenter {@code presenter} with the View implementing {@link BaseView}
     *
     * @param presenter Presenter instance implementing the {@link BasePresenter}
     */
    @Override
    public void setPresenter(SalesConfigContract.Presenter presenter) {
        mPresenter = presenter;
    }

    /**
     * Initialize the contents of the Fragment host's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     *
     * @param menu     The options menu in which you place your items.
     * @param inflater The LayoutInflater object that can be used to inflate the Menu options
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //Inflating the Menu options from 'R.menu.menu_fragment_sales_config'
        inflater.inflate(R.menu.menu_fragment_sales_config, menu);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handling based on the Menu item selected
        switch (item.getItemId()) {
            case R.id.action_delete:
                //On Click of Delete Menu

                //Delegating to the Presenter to show the re-confirmation dialog
                mPresenter.showDeleteProductDialog();
                return true;
            case R.id.action_save:
                //On Click of Save Menu

                //Saving the Product Sales information
                saveProductSales();
                return true;
            default:
                //On other cases, do the default menu handling
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Method that grabs the data from the Views and dispatches to the Presenter
     * to initiate the Save process.
     */
    private void saveProductSales() {
        //Delegating to the Presenter to trigger focus loss on listener registered Views,
        //in order to persist their data
        mPresenter.triggerFocusLost();

        //Delegating to the Presenter to initiate the Save process
        mPresenter.onSave(mProductSuppliersAdapter.getProductSupplierSalesList());
    }

    /**
     * Method that initializes the RecyclerView 'R.id.recyclerview_sales_config_suppliers' and its Adapter.
     */
    private void setupProductSuppliersRecyclerView() {
        //Creating a Vertical Linear Layout Manager with default layout order
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false);

        //Setting the Layout Manager to use
        mRecyclerViewProductSuppliers.setLayoutManager(linearLayoutManager);

        //Initializing the Adapter
        mProductSuppliersAdapter = new ProductSuppliersAdapter(new ProductSupplierItemUserActionsListener());

        //Setting the Adapter on RecyclerView
        mRecyclerViewProductSuppliers.setAdapter(mProductSuppliersAdapter);

        //Attaching the ItemTouchHelper for Swipe delete
        mProductSuppliersAdapter.getItemTouchHelper().attachToRecyclerView(mRecyclerViewProductSuppliers);
    }

    /**
     * Method invoked to keep the state of "Product details restored", in sync with the Presenter.
     *
     * @param isProductRestored Boolean that indicates the state of Product data restored.
     *                          <b>TRUE</b> if it had been restored; <b>FALSE</b> otherwise.
     */
    @Override
    public void syncProductState(boolean isProductRestored) {
        //Saving the state
        mIsProductRestored = isProductRestored;
    }

    /**
     * Method invoked to keep the state of "Suppliers data restored", in sync with the Presenter.
     *
     * @param areSuppliersRestored Boolean that indicates the state of Suppliers data restored.
     *                             <b>TRUE</b> if it had been restored; <b>FALSE</b> otherwise.
     */
    @Override
    public void syncSuppliersState(boolean areSuppliersRestored) {
        //Saving the state
        mAreSuppliersRestored = areSuppliersRestored;
    }

    /**
     * Method invoked to keep the original Total available quantity of the Product,
     * in sync with the Presenter.
     *
     * @param oldTotalAvailableQuantity Integer value of the original Total available
     *                                  quantity of the Product.
     */
    @Override
    public void syncOldTotalAvailability(int oldTotalAvailableQuantity) {
        //Saving the original Total available quantity
        mOldTotalAvailableQuantity = oldTotalAvailableQuantity;
    }

    /**
     * Method that displays the Progress indicator
     *
     * @param statusTextId String resource for the status of the Progress to be shown.
     */
    @Override
    public void showProgressIndicator(int statusTextId) {
        ProgressDialogFragment.showDialog(getChildFragmentManager(), getString(statusTextId));
    }

    /**
     * Method that hides the Progress indicator
     */
    @Override
    public void hideProgressIndicator() {
        ProgressDialogFragment.dismissDialog(getChildFragmentManager());
    }

    /**
     * Method invoked when an error is encountered during Product/Suppliers information
     * retrieval or save process.
     *
     * @param messageId String Resource of the error Message to be displayed
     * @param args      Variable number of arguments to replace the format specifiers
     */
    @Override
    public void showError(int messageId, @Nullable Object... args) {
        if (getView() != null) {
            //When we have the root view

            //Evaluating the message to be shown
            String messageToBeShown;
            if (args != null && args.length > 0) {
                //For the String Resource with args
                messageToBeShown = getString(messageId, args);
            } else {
                //For the String Resource without args
                messageToBeShown = getString(messageId);
            }

            if (!TextUtils.isEmpty(messageToBeShown)) {
                //Displaying the Snackbar message of indefinite time length
                //when we have the error message to be shown

                new SnackbarUtility(Snackbar.make(getView(), messageToBeShown, Snackbar.LENGTH_INDEFINITE))
                        .revealCompleteMessage() //Removes the limit on max lines
                        .setDismissAction(R.string.snackbar_action_ok) //For the Dismiss "OK" action
                        .showSnack();
            }
        }
    }

    /**
     * Method that updates the Product Name {@code productName} to the View.
     *
     * @param productName The Product Name of the Product.
     */
    @Override
    public void updateProductName(String productName) {
        //Setting the Product Name
        mTextViewProductName.setText(productName);
    }

    /**
     * Method that updates the Product SKU {@code productSku} to the View.
     *
     * @param productSku The Product SKU of the Product.
     */
    @Override
    public void updateProductSku(String productSku) {
        //Setting the Product SKU
        mTextViewProductSku.setText(productSku);
        //Setting Barcode typeface for the SKU
        mTextViewProductSku.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.libre_barcode_128_text_regular));
    }

    /**
     * Method that updates the Product Category {@code productCategory} to the View.
     *
     * @param productCategory The Product Category of the Product.
     */
    @Override
    public void updateProductCategory(String productCategory) {
        mTextViewProductCategory.setText(productCategory);
    }

    /**
     * Method that updates the Product Description {@code description} to the View.
     *
     * @param description The description of the Product.
     */
    @Override
    public void updateProductDescription(String description) {
        mTextViewProductDesc.setText(description);
    }

    /**
     * Method that updates the List of Product Images {@code productImages} to the View
     *
     * @param productImages The List of {@link ProductImage} of a Product.
     */
    @Override
    public void updateProductImages(ArrayList<ProductImage> productImages) {
        //Saving the list of ProductImages
        mProductImages = productImages;
    }

    /**
     * Method that updates the List of Product Attributes {@code productAttributes} to the View.
     *
     * @param productAttributes The List of {@link ProductAttribute} of a Product.
     */
    @Override
    public void updateProductAttributes(ArrayList<ProductAttribute> productAttributes) {
        //Saving the Product Attributes data
        mProductAttributes = productAttributes;

        //Set Stretch all Table Columns
        mTableLayoutProductAttrs.setStretchAllColumns(true);

        //Removing all the Child Views if any
        mTableLayoutProductAttrs.removeAllViewsInLayout();

        //Retrieving the Number of Product Attributes
        int noOfProductAttrs = mProductAttributes.size();

        //Iterating over the list to build a Table for Product Attributes to be shown
        for (int index = 0; index < noOfProductAttrs; index++) {
            //Retrieving the current Product Attribute at the index
            ProductAttribute productAttribute = mProductAttributes.get(index);

            //Creating a new TableRow to show the entry
            TableRow tableRow = new TableRow(requireContext());
            //Setting TableRow LayoutParams
            TableLayout.LayoutParams tableRowLayoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
            //Adding the New Row to the TableLayout
            mTableLayoutProductAttrs.addView(tableRow, tableRowLayoutParams);

            //Setting background for the table row with different color shape for odd and even rows
            if (index % 2 == 0) {
                //When even
                tableRow.setBackgroundResource(R.drawable.shape_sales_config_product_attrs_table_bg_even_row);
            } else {
                //When odd
                tableRow.setBackgroundResource(R.drawable.shape_sales_config_product_attrs_table_bg_odd_row);
            }

            //Setting the LayoutParams for Table Cell TextView
            TableRow.LayoutParams textViewCellLayoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT);

            //Inflating a TextView with Style to hold the Attribute Name
            TextView textViewAttrName = (TextView) LayoutInflater.from(requireContext()).inflate(R.layout.layout_sales_config_product_attrs_table_cell_name, tableRow, false);
            //Setting the Attribute Name on TextView
            textViewAttrName.setText(productAttribute.getAttributeName());
            //Adding Attribute Name TextView to the TableRow
            tableRow.addView(textViewAttrName, textViewCellLayoutParams);

            //Inflating a TextView with Style to hold the Attribute Value
            TextView textViewAttrValue = (TextView) LayoutInflater.from(requireContext()).inflate(R.layout.layout_sales_config_product_attrs_table_cell_value, tableRow, false);
            //Setting the Attribute Value on TextView
            textViewAttrValue.setText(productAttribute.getAttributeValue());
            //Adding Attribute Value TextView to the TableRow
            tableRow.addView(textViewAttrValue, textViewCellLayoutParams);
        }
    }

    /**
     * Method that updates the Adapter of the RecyclerView List of Product's Suppliers with
     * Sales information.
     *
     * @param productSupplierSalesList List of {@link ProductSupplierSales} containing
     *                                 the Product's Suppliers with Sales information.
     */
    @Override
    public void loadProductSuppliersData(ArrayList<ProductSupplierSales> productSupplierSalesList) {
        mProductSuppliersAdapter.submitList(productSupplierSalesList);
    }

    /**
     * Method invoked when the total available quantity of the Product has been recalculated.
     *
     * @param totalAvailableQuantity Integer value of the Total Available quantity of the Product.
     */
    @Override
    public void updateAvailability(int totalAvailableQuantity) {
        //Set the total available quantity value
        mTextViewProductAvailableQuantity.setText(String.valueOf(totalAvailableQuantity));
        //Set the Text Color
        mTextViewProductAvailableQuantity.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
    }

    /**
     * Method invoked to show the "Out Of Stock!" alert when the Total Available quantity
     * of the Product is 0.
     */
    @Override
    public void showOutOfStockAlert() {
        //Set the Out of Stock message
        mTextViewProductAvailableQuantity.setText(getString(R.string.sales_list_item_out_of_stock));
        //Set the Text Color
        mTextViewProductAvailableQuantity.setTextColor(ContextCompat.getColor(requireContext(), R.color.salesListItemOutOfStockColor));
    }

    /**
     * Method invoked when the user swipes left/right any Item View of the Product's Suppliers
     * in order to remove it from the list. This should show a Snackbar with Action UNDO.
     *
     * @param supplierCode The Supplier Code of the Supplier being swiped out/unlinked.
     */
    @Override
    public void showProductSupplierSwiped(String supplierCode) {
        if (getView() != null) {
            new SnackbarUtility(Snackbar.make(getView(),
                    getString(R.string.sales_config_supplier_swipe_action_success,
                            supplierCode), Snackbar.LENGTH_LONG))
                    .revealCompleteMessage()
                    .setAction(R.string.snackbar_action_undo, (view) -> {
                        //Try and Restore the Adapter data when UNDO is clicked
                        if (mProductSuppliersAdapter.restoreLastRemovedProductSupplierSales()) {
                            //On Success, show a Snackbar message
                            Snackbar.make(getView(),
                                    getString(R.string.sales_config_supplier_swipe_action_undo_success, supplierCode),
                                    Snackbar.LENGTH_LONG).show();
                        }
                    })
                    .showSnack();
        }
    }

    /**
     * Method that displays a message on Success of Updating a Product.
     *
     * @param productSku String containing the SKU of the Product that was updated successfully.
     */
    @Override
    public void showUpdateProductSuccess(String productSku) {
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.product_list_item_update_success, productSku), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Method that displays a message on Success of updating an Existing Supplier.
     *
     * @param supplierCode String containing the code of the Supplier that was updated successfully.
     */
    @Override
    public void showUpdateSupplierSuccess(String supplierCode) {
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.supplier_list_item_update_success, supplierCode), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Method that displays a message on Success of Deleting an Existing Supplier.
     *
     * @param supplierCode String containing the code of the Supplier that was deleted successfully.
     */
    @Override
    public void showDeleteSupplierSuccess(String supplierCode) {
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.supplier_list_item_delete_success, supplierCode), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Method invoked before save operation or screen orientation change to persist
     * any data held by the view that had focus and its listener registered.
     * This clears the focus held by the view to trigger the listener, causing to persist any unsaved data.
     */
    @Override
    public void triggerFocusLost() {
        //Clearing focus in the last registered view in Product Suppliers Adapter
        if (mProductSuppliersAdapter != null) {
            mProductSuppliersAdapter.triggerFocusLost();
        }
    }

    /**
     * Method invoked by the Presenter to display the Discard dialog,
     * requesting the User whether to keep editing/discard the changes
     */
    @Override
    public void showDiscardDialog() {
        //Creating an AlertDialog with a message, and listeners for the positive, neutral and negative buttons
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        //Set the Message
        builder.setMessage(R.string.sales_config_unsaved_changes_dialog_message);
        //Set the Positive Button and its listener
        builder.setPositiveButton(R.string.sales_config_unsaved_changes_dialog_positive_text, mUnsavedDialogOnClickListener);
        //Set the Negative Button and its listener
        builder.setNegativeButton(R.string.sales_config_unsaved_changes_dialog_negative_text, mUnsavedDialogOnClickListener);
        //Set the Neutral Button and its listener
        builder.setNeutralButton(R.string.sales_config_unsaved_changes_dialog_neutral_text, mUnsavedDialogOnClickListener);
        //Lock the Orientation
        OrientationUtility.lockCurrentScreenOrientation(requireActivity());
        //Create and display the AlertDialog
        builder.create().show();
    }

    /**
     * Method invoked when the user clicks on the Delete Menu Action to delete the Product.
     * This should launch a Dialog for the user to reconfirm the request before proceeding
     * with the Delete Action.
     */
    @Override
    public void showDeleteProductDialog() {
        //Creating an AlertDialog with a message, and listeners for the positive and negative buttons
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        //Set the Message
        builder.setMessage(R.string.sales_config_delete_product_confirm_dialog_message);
        //Set the Positive Button and its listener
        builder.setPositiveButton(android.R.string.yes, mProductDeleteDialogOnClickListener);
        //Set the Negative Button and its listener
        builder.setNegativeButton(android.R.string.no, mProductDeleteDialogOnClickListener);
        //Lock the Orientation
        OrientationUtility.lockCurrentScreenOrientation(requireActivity());
        //Create and display the AlertDialog
        builder.create().show();
    }

    /**
     * Called when a view has been clicked.
     *
     * @param view The view that was clicked.
     */
    @Override
    public void onClick(View view) {
        //Taking action based on the Id of the View clicked
        switch (view.getId()) {
            case R.id.imgbtn_sales_config_product_edit:
                //For the Product Edit ImageButton

                //Delegating to the Presenter to launch the ProductConfigActivity to edit the Product
                mPresenter.editProduct(mProductId);
                break;
        }
    }

    /**
     * {@link ListAdapter} class for the RecyclerView to load the list of Suppliers with their
     * Selling Price and Product Availability {@link ProductSupplierSales} to be displayed.
     */
    private static class ProductSuppliersAdapter extends ListAdapter<ProductSupplierSales, ProductSuppliersAdapter.ViewHolder> {

        /**
         * {@link DiffUtil.ItemCallback} for calculating the difference between two
         * {@link ProductSupplierSales} objects.
         */
        private static DiffUtil.ItemCallback<ProductSupplierSales> DIFF_SUPPLIERS
                = new DiffUtil.ItemCallback<ProductSupplierSales>() {
            /**
             * Called to check whether two objects represent the same item.
             * <p>
             * For example, if your items have unique ids, this method should check their id equality.
             *
             * @param oldItem The item in the old list.
             * @param newItem The item in the new list.
             * @return True if the two items represent the same object or false if they are different.
             *
             * @see DiffUtil.Callback#areItemsTheSame(int, int)
             */
            @Override
            public boolean areItemsTheSame(ProductSupplierSales oldItem, ProductSupplierSales newItem) {
                //Returning the comparison of Product and Supplier's Id
                return (oldItem.getItemId() == newItem.getItemId()) && (oldItem.getSupplierId() == newItem.getSupplierId());
            }

            /**
             * Called to check whether two items have the same data.
             * <p>
             * This information is used to detect if the contents of an item have changed.
             * <p>
             * This method to check equality instead of {@link Object#equals(Object)} so that you can
             * change its behavior depending on your UI.
             * <p>
             * For example, if you are using DiffUtil with a
             * {@link android.support.v7.widget.RecyclerView.Adapter RecyclerView.Adapter}, you should
             * return whether the items' visual representations are the same.
             * <p>
             * This method is called only if {@link #areItemsTheSame(ProductSupplierSales, ProductSupplierSales)} returns {@code true} for
             * these items.
             *
             * @param oldItem The item in the old list.
             * @param newItem The item in the new list.
             * @return True if the contents of the items are the same or false if they are different.
             *
             * @see DiffUtil.Callback#areContentsTheSame(int, int)
             */
            @Override
            public boolean areContentsTheSame(ProductSupplierSales oldItem, ProductSupplierSales newItem) {
                //Returning the result of equals
                return oldItem.equals(newItem);
            }
        };
        //The Data of this Adapter that stores the Supplier details with Selling Price
        //and Product Availability
        private ArrayList<ProductSupplierSales> mProductSupplierSalesList;
        //Stores the EditText View that had last acquired focus
        private View mLastFocusedView;
        //Stores last removed data if needs to be undone
        private ProductSupplierSales mLastRemovedProductSupplierSales;
        //Listener for User Actions on Product's List of Suppliers
        private ProductSuppliersUserActionsListener mActionsListener;

        /**
         * Constructor of {@link ProductSuppliersAdapter}
         *
         * @param userActionsListener Instance of {@link ProductSuppliersUserActionsListener}
         *                            to receive event callbacks for User Actions on Item Views
         */
        ProductSuppliersAdapter(ProductSuppliersUserActionsListener userActionsListener) {
            super(DIFF_SUPPLIERS);
            //Registering the User Actions Listener
            mActionsListener = userActionsListener;
        }

        /**
         * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
         * an item.
         *
         * @param parent   The ViewGroup into which the new View will be added after it is bound to
         *                 an adapter position.
         * @param viewType The view type of the new View.
         * @return A new ViewHolder that holds a View of the given view type.
         */
        @NonNull
        @Override
        public ProductSuppliersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Inflating the item layout 'R.layout.item_sales_config_supplier'
            //Passing False since we are attaching the layout ourselves
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sales_config_supplier, parent, false);
            //Returning the Instance of ViewHolder for the inflated Item View
            return new ViewHolder(itemView);
        }

        /**
         * Called by RecyclerView to display the data at the specified position. This method should
         * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
         * position.
         *
         * @param holder   The ViewHolder which should be updated to represent the contents of the
         *                 item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        @Override
        public void onBindViewHolder(@NonNull ProductSuppliersAdapter.ViewHolder holder, int position) {
            //Get the data at the position
            ProductSupplierSales productSupplierSales = getItem(position);
            //Binding the data at the position
            holder.bind(productSupplierSales);
        }

        /**
         * Submits a new list to be diffed, and displayed.
         * <p>
         * If a list is already being displayed, a diff will be computed on a background thread, which
         * will dispatch Adapter.notifyItem events on the main thread.
         *
         * @param submittedList The new list to be displayed.
         */
        @Override
        public void submitList(List<ProductSupplierSales> submittedList) {
            //Restoring the quantities from the current list if present
            if (mProductSupplierSalesList != null && mProductSupplierSalesList.size() > 0) {
                //When we had some data

                //Creating a SparseArray of Supplier Id with their Quantities for lookup
                SparseIntArray suppliersQuantityArray = new SparseIntArray();
                for (ProductSupplierSales productSupplierSales : mProductSupplierSalesList) {
                    suppliersQuantityArray.put(productSupplierSales.getSupplierId(), productSupplierSales.getAvailableQuantity());
                }

                if (suppliersQuantityArray.size() > 0) {
                    //When we have lookup data

                    //Iterating over the submitted list to update them with the current quantities of Suppliers
                    for (ProductSupplierSales productSupplierSales : submittedList) {
                        productSupplierSales.setAvailableQuantity(suppliersQuantityArray.get(productSupplierSales.getSupplierId()));
                    }
                }
            }

            //Saving the updated list in the Adapter
            mProductSupplierSalesList = new ArrayList<>();
            mProductSupplierSalesList.addAll(submittedList);

            //Calculating the New Total Available Quantity of the Product
            int totalAvailableQuantity = 0;
            for (ProductSupplierSales productSupplierSales : mProductSupplierSalesList) {
                totalAvailableQuantity += productSupplierSales.getAvailableQuantity();
            }
            //Publishing the New Total Available Quantity of the Product to the Listener
            mActionsListener.onUpdatedAvailability(totalAvailableQuantity);

            //Creating a new list to publish the new data passed
            ArrayList<ProductSupplierSales> newProductSupplierSalesList = new ArrayList<>(submittedList);
            //Propagating the new list to super
            super.submitList(newProductSupplierSalesList);
        }

        /**
         * Method that clears focus on the registered view that last held it.
         * This causes to trigger the {@link android.view.View.OnFocusChangeListener#onFocusChange} event
         * on the view.
         */
        void triggerFocusLost() {
            if (mLastFocusedView != null) {
                mLastFocusedView.clearFocus();
            }
        }

        /**
         * Getter Method for the data of this Adapter.
         *
         * @return The List of Product's Suppliers with their Selling Price
         * and available quantity {@link ProductSupplierSales}
         */
        ArrayList<ProductSupplierSales> getProductSupplierSalesList() {
            return mProductSupplierSalesList;
        }

        /**
         * Getter Method for the {@link ItemTouchHelper} that removes the Item View
         * and its associated data, on Swipe, from the list.
         *
         * @return Instance of {@link ItemTouchHelper} enabled for Left and Right swipe actions
         */
        ItemTouchHelper getItemTouchHelper() {
            //Creating and returning an instance of ItemTouchHelper enabled for Left and Right swipe actions
            return new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                /**
                 * Called when ItemTouchHelper wants to move the dragged item from its old position to
                 * the new position.
                 * <p>
                 * If this method returns true, ItemTouchHelper assumes {@code viewHolder} has been moved
                 * to the adapter position of {@code target} ViewHolder
                 * ({@link ViewHolder#getAdapterPosition()
                 * ViewHolder#getAdapterPosition()}).
                 * <p>
                 * If you don't support drag & drop, this method will never be called.
                 *
                 * @param recyclerView The RecyclerView to which ItemTouchHelper is attached to.
                 * @param viewHolder   The ViewHolder which is being dragged by the user.
                 * @param target       The ViewHolder over which the currently active item is being
                 *                     dragged.
                 * @return True if the {@code viewHolder} has been moved to the adapter position of
                 * {@code target}.
                 */
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    //Not enabled for Dragging
                    return false;
                }

                /**
                 * Called when a ViewHolder is swiped by the user.
                 * <p>
                 * If you are returning relative directions ({@link ItemTouchHelper#START} , {@link ItemTouchHelper#END}) from the
                 * {@link #getMovementFlags(RecyclerView, RecyclerView.ViewHolder)} method, this method
                 * will also use relative directions. Otherwise, it will use absolute directions.
                 * <p>
                 * If you don't support swiping, this method will never be called.
                 * <p>
                 * ItemTouchHelper will keep a reference to the View until it is detached from
                 * RecyclerView.
                 * As soon as it is detached, ItemTouchHelper will call
                 * {@link #clearView(RecyclerView, RecyclerView.ViewHolder)}.
                 *
                 * @param viewHolder The ViewHolder which has been swiped by the user.
                 * @param direction  The direction to which the ViewHolder is swiped. It is one of
                 *                   {@link ItemTouchHelper#UP}, {@link ItemTouchHelper#DOWN},
                 *                   {@link ItemTouchHelper#LEFT} or {@link ItemTouchHelper#RIGHT}. If your
                 *                   {@link #getMovementFlags(RecyclerView, RecyclerView.ViewHolder)}
                 *                   method returned relative flags instead of {@link ItemTouchHelper#LEFT} / {@link ItemTouchHelper#RIGHT};
                 *                   `direction` will be relative as well. ({@link ItemTouchHelper#START} or {@link
                 *                   ItemTouchHelper#END}).
                 */
                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    //Retrieving the Adapter position
                    int itemPosition = viewHolder.getAdapterPosition();
                    //Get the data at the position
                    ProductSupplierSales productSupplierSales = getItem(itemPosition);
                    //Storing the data being removed
                    mLastRemovedProductSupplierSales = productSupplierSales;
                    //Removing the data from the Adapter list
                    mProductSupplierSalesList.remove(productSupplierSales);
                    //Submitting the updated list to the Adapter
                    submitList(mProductSupplierSalesList);
                    //Propagating the event to the listener
                    mActionsListener.onSwiped(itemPosition, mLastRemovedProductSupplierSales);
                }
            });
        }

        /**
         * Method that restores the last removed Supplier Sales data {@link ProductSupplierSales}
         * from the Adapter.
         *
         * @return <b>TRUE</b> when the Last removed Supplier Sales data was present and restored; <b>FALSE</b> otherwise.
         */
        boolean restoreLastRemovedProductSupplierSales() {
            if (mLastRemovedProductSupplierSales != null) {
                //When we have the last removed Supplier Sales data

                //Add it back to the Adapter data list
                mProductSupplierSalesList.add(mLastRemovedProductSupplierSales);
                //Submitting the updated list to the Adapter
                submitList(mProductSupplierSalesList);
                //Returning True when done
                return true;
            }
            //Returning False when we do not have the last removed Supplier Sales data
            return false;
        }

        /**
         * ViewHolder class for caching View components of the template item view
         * 'R.layout.item_sales_config_supplier'
         */
        public class ViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener, View.OnFocusChangeListener {
            //The TextView that displays the Supplier Name and Code
            private TextView mTextViewSupplierNameCode;
            //The TextView that displays the Supplier Selling Price
            private TextView mTextViewSupplierPrice;
            //The TextView that displays Out Of Stock Alert
            private TextView mTextViewOutOfStockAlert;
            //The EditText that captures the Available Quantity from the Supplier
            private EditText mEditTextAvailableQuantity;

            /**
             * Constructor of the ViewHolder.
             *
             * @param itemView The inflated item layout View passed
             *                 for caching its View components
             */
            ViewHolder(View itemView) {
                super(itemView);

                //Finding the Views needed
                mTextViewSupplierNameCode = itemView.findViewById(R.id.text_sales_config_item_supplier_name_code);
                mTextViewSupplierPrice = itemView.findViewById(R.id.text_sales_config_item_supplier_selling_price);
                mTextViewOutOfStockAlert = itemView.findViewById(R.id.text_sales_config_item_supplier_out_of_stock_alert);
                mEditTextAvailableQuantity = itemView.findViewById(R.id.edittext_sales_config_item_supplier_qty);

                //Registering Click Listeners on views
                itemView.findViewById(R.id.imgbtn_sales_config_item_supplier_increase_qty).setOnClickListener(this);
                itemView.findViewById(R.id.imgbtn_sales_config_item_supplier_decrease_qty).setOnClickListener(this);
                itemView.findViewById(R.id.btn_sales_config_item_supplier_edit).setOnClickListener(this);
                itemView.findViewById(R.id.btn_sales_config_item_supplier_procure).setOnClickListener(this);

                //Registering Focus Listener on EditText
                mEditTextAvailableQuantity.setOnFocusChangeListener(this);
            }

            /**
             * Called when a view has been clicked.
             *
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                //Get the current adapter position
                int adapterPosition = getAdapterPosition();
                if (adapterPosition > RecyclerView.NO_POSITION) {
                    //When the item position is valid

                    //Get the data at the position
                    ProductSupplierSales productSupplierSales = getItem(adapterPosition);

                    //Taking action based on the view clicked
                    switch (view.getId()) {
                        case R.id.imgbtn_sales_config_item_supplier_increase_qty:
                            //For the Increase Quantity ImageButton
                        {
                            //Trigger focus loss before proceeding
                            triggerFocusLost();

                            //Grab the availability value from the EditText
                            String availableQtyStr = mEditTextAvailableQuantity.getText().toString().trim();

                            if (!TextUtils.isEmpty(availableQtyStr)) {
                                //When we have the Supplier available quantity

                                //Parse the current available quantity value
                                int currentAvailableQuantity = Integer.parseInt(availableQtyStr);

                                //Increment the value
                                int updatedAvailableQuantity = currentAvailableQuantity + 1;

                                //Update it to the EditText and to the data at the position
                                mEditTextAvailableQuantity.setText(String.valueOf(updatedAvailableQuantity));
                                productSupplierSales.setAvailableQuantity(updatedAvailableQuantity);

                                //Update the change in Available Quantity to the Listener
                                mActionsListener.onChangeInAvailability(updatedAvailableQuantity - currentAvailableQuantity);

                                //Set the "Out of Stock!" visibility based on the updated Available Quantity
                                setOutOfStockAlertVisibility(updatedAvailableQuantity);
                            }
                        }
                        break;
                        case R.id.imgbtn_sales_config_item_supplier_decrease_qty:
                            //For the Decrease Quantity ImageButton
                        {
                            //Trigger focus loss before proceeding
                            triggerFocusLost();

                            //Grab the availability value from the EditText
                            String availableQtyStr = mEditTextAvailableQuantity.getText().toString().trim();

                            if (!TextUtils.isEmpty(availableQtyStr)) {
                                //When we have the Supplier available quantity

                                //Parse the current available quantity value
                                int currentAvailableQuantity = Integer.parseInt(availableQtyStr);

                                //Decrement the value
                                int updatedAvailableQuantity = currentAvailableQuantity - 1;

                                //Update it to the EditText and to the data at the position
                                //when the updated value is valid (equal to or greater than 0)
                                if (updatedAvailableQuantity >= 0) {
                                    mEditTextAvailableQuantity.setText(String.valueOf(updatedAvailableQuantity));
                                    productSupplierSales.setAvailableQuantity(updatedAvailableQuantity);

                                    //Update the change in Available Quantity to the Listener
                                    mActionsListener.onChangeInAvailability(updatedAvailableQuantity - currentAvailableQuantity);

                                    //Set the "Out of Stock!" visibility based on the updated Available Quantity
                                    setOutOfStockAlertVisibility(updatedAvailableQuantity);
                                }
                            }
                        }
                        break;
                        case R.id.btn_sales_config_item_supplier_edit:
                            //For the "Edit" button

                            //Dispatch the event to the action listener
                            mActionsListener.onEditSupplier(adapterPosition, productSupplierSales);
                            break;
                        case R.id.btn_sales_config_item_supplier_procure:
                            //For the "Procure" button

                            //Dispatch the event to the action listener
                            mActionsListener.onProcure(adapterPosition, productSupplierSales);
                            break;
                    }
                }
            }

            /**
             * Method that sets the Visibility of 'R.id.text_sales_config_item_supplier_out_of_stock_alert'
             * TextView that displays the "Out of Stock!" alert based on {@code availableQuantity} value.
             *
             * @param availableQuantity The Integer value of the Available Quantity of the
             *                          Product at the Supplier. When '0', the TextView will be
             *                          set to visible to show the alert; otherwise
             *                          hidden when the availableQuantity > 0
             */
            private void setOutOfStockAlertVisibility(int availableQuantity) {
                if (availableQuantity > 0) {
                    //When we have the Quantity, hide the "Out of Stock!" alert
                    mTextViewOutOfStockAlert.setVisibility(View.GONE);
                } else {
                    //When there is NO Quantity, show the "Out of Stock!" alert
                    mTextViewOutOfStockAlert.setVisibility(View.VISIBLE);
                }
            }

            /**
             * Called when the focus state of a view has changed.
             *
             * @param view     The view whose state has changed.
             * @param hasFocus The new focus state of view.
             */
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                //Get the current adapter position
                int adapterPosition = getAdapterPosition();
                if (adapterPosition > RecyclerView.NO_POSITION) {
                    //When the item position is valid

                    //Get the data at the position
                    ProductSupplierSales productSupplierSales = getItem(adapterPosition);

                    if (!hasFocus) {
                        //When the registered View has lost focus

                        //Clear the view reference
                        mLastFocusedView = null;

                        //Take action based on the id of the view which lost focus
                        switch (view.getId()) {
                            case R.id.edittext_sales_config_item_supplier_qty:
                                //For the EditText View that displays the Supplier Available Quantity

                                //Grab the value from EditText and update it to the current ProductSupplierSales
                                String availableQtyStr = mEditTextAvailableQuantity.getText().toString().trim();

                                //Get the previous Available Quantity value
                                int oldAvailableQuantity = productSupplierSales.getAvailableQuantity();

                                //Parse the updated Available Quantity value
                                int updatedAvailableQuantity = 0; //Initializing to 0
                                if (!TextUtils.isEmpty(availableQtyStr)) {
                                    //Read the value only when present
                                    updatedAvailableQuantity = Integer.parseInt(availableQtyStr);
                                } else {
                                    //Update the '0' Available Quantity value to EditText, when there was no value
                                    mEditTextAvailableQuantity.setText(String.valueOf(updatedAvailableQuantity));
                                }

                                //Resetting the updated Available Quantity value to 0 when negative
                                if (updatedAvailableQuantity < 0) {
                                    updatedAvailableQuantity = 0;
                                    mEditTextAvailableQuantity.setText(String.valueOf(updatedAvailableQuantity));
                                }

                                //Update it to the current ProductSupplierSales
                                productSupplierSales.setAvailableQuantity(updatedAvailableQuantity);

                                //Set the "Out of Stock!" visibility based on the updated Available Quantity
                                setOutOfStockAlertVisibility(updatedAvailableQuantity);

                                //Update the change in Available Quantity to the Listener
                                mActionsListener.onChangeInAvailability(updatedAvailableQuantity - oldAvailableQuantity);

                                break;
                        }

                    } else {
                        //When the registered view has gained focus

                        //Save the view reference
                        mLastFocusedView = view;
                    }
                }
            }

            /**
             * Method that binds the views with the data at the position {@link ProductSupplierSales}.
             *
             * @param productSupplierSales The data {@link ProductSupplierSales} at the item position.
             */
            void bind(ProductSupplierSales productSupplierSales) {
                if (productSupplierSales != null) {
                    //When we have the details

                    //Get the Resources
                    Resources resources = itemView.getContext().getResources();

                    //Bind the Supplier Name and Code
                    mTextViewSupplierNameCode.setText(resources.getString(R.string.sales_list_item_supplier_name_code_format,
                            productSupplierSales.getSupplierName(), productSupplierSales.getSupplierCode()));

                    //Bind the Supplier Selling Price
                    mTextViewSupplierPrice.setText(resources.getString(R.string.sales_config_item_supplier_selling_price,
                            productSupplierSales.getUnitPrice() + " " + Currency.getInstance(Locale.getDefault()).getCurrencyCode()));

                    //Bind the Available Quantity
                    int availableQuantity = productSupplierSales.getAvailableQuantity();
                    mEditTextAvailableQuantity.setText(String.valueOf(availableQuantity));

                    //Set the Visibility of "Out of Stock!" Alert based
                    //on the Available Quantity value
                    setOutOfStockAlertVisibility(availableQuantity);
                }
            }
        }
    }

    /**
     * Listener that implements {@link ProductSuppliersUserActionsListener} to receive
     * event callbacks for User actions on RecyclerView list of Product's Suppliers.
     */
    private class ProductSupplierItemUserActionsListener implements ProductSuppliersUserActionsListener {

        /**
         * Callback Method of {@link ProductSuppliersUserActionsListener} invoked when
         * the user clicks on the "Edit" button. This should launch the
         * {@link SupplierConfigActivity}
         * for the Supplier to be edited.
         *
         * @param itemPosition         The adapter position of the Item clicked.
         * @param productSupplierSales The {@link ProductSupplierSales} associated with the Item clicked.
         */
        @Override
        public void onEditSupplier(int itemPosition, ProductSupplierSales productSupplierSales) {
            //Delegating to the Presenter to handle the event
            mPresenter.editSupplier(productSupplierSales.getSupplierId());
        }

        /**
         * Callback Method of {@link ProductSuppliersUserActionsListener} invoked when
         * the user swipes the Item View to remove the Product-Supplier link.
         *
         * @param itemPosition         The adapter position of the Item clicked.
         * @param productSupplierSales The {@link ProductSupplierSales} associated with the Item swiped.
         */
        @Override
        public void onSwiped(int itemPosition, ProductSupplierSales productSupplierSales) {
            //Delegating to the Presenter to handle the event
            mPresenter.onProductSupplierSwiped(productSupplierSales.getSupplierCode());
        }

        /**
         * Callback Method of {@link ProductSuppliersUserActionsListener} invoked when
         * the user clicks on the "Procure" button. This should launch the
         * {@link com.example.kaushiknsanji.storeapp.ui.inventory.procure.SalesProcurementActivity}
         * for the User to place procurement for the Product.
         *
         * @param itemPosition         The adapter position of the Item clicked.
         * @param productSupplierSales The {@link ProductSupplierSales} associated with the Item clicked.
         */
        @Override
        public void onProcure(int itemPosition, ProductSupplierSales productSupplierSales) {
            //Delegating to the Presenter to handle the event
            mPresenter.procureProduct(productSupplierSales);
        }

        /**
         * Callback Method of {@link ProductSuppliersUserActionsListener} invoked when
         * the total available quantity of the Product has been recalculated.
         *
         * @param totalAvailableQuantity Integer value of the Total Available quantity of the Product.
         */
        @Override
        public void onUpdatedAvailability(int totalAvailableQuantity) {
            //Delegating to the Presenter to handle the event
            mPresenter.updateAvailability(totalAvailableQuantity);
        }

        /**
         * Callback Method of {@link ProductSuppliersUserActionsListener} invoked when
         * there is a change to the total available quantity of the Product.
         *
         * @param changeInAvailableQuantity Integer value of the change in the Total Available
         *                                  quantity of the Product with respect to the last
         *                                  Updated Availability. Can be negative to indicate
         *                                  the decrease in Available Quantity.
         */
        @Override
        public void onChangeInAvailability(int changeInAvailableQuantity) {
            //Delegating to the Presenter to handle the event
            mPresenter.changeAvailability(changeInAvailableQuantity);
        }
    }
}
