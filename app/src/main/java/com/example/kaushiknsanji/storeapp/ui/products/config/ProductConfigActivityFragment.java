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

package com.example.kaushiknsanji.storeapp.ui.products.config;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductAttribute;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.ui.common.ProgressDialogFragment;
import com.example.kaushiknsanji.storeapp.ui.products.image.ProductImageActivity;
import com.example.kaushiknsanji.storeapp.utils.OrientationUtility;
import com.example.kaushiknsanji.storeapp.utils.SnackbarUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * Content Fragment of {@link ProductConfigActivity} that inflates the layout 'R.layout.fragment_product_config'
 * to display/record the configuration details of the Product. This implements the
 * {@link ProductConfigContract.View} on the lines of Model-View-Presenter architecture.
 *
 * @author Kaushik N Sanji
 */
public class ProductConfigActivityFragment extends Fragment
        implements ProductConfigContract.View, View.OnClickListener, View.OnFocusChangeListener {

    //Constant used for logs
    private static final String LOG_TAG = ProductConfigActivityFragment.class.getSimpleName();

    //The Bundle argument constant of this Fragment
    private static final String ARGUMENT_INT_PRODUCT_ID = "argument.PRODUCT_ID";

    //Bundle constants for persisting the data throughout System config changes
    private static final String BUNDLE_PRODUCT_ID_INT_KEY = "ProductConfig.ProductId";
    private static final String BUNDLE_CATEGORY_SELECTED_STR_KEY = "ProductConfig.Category";
    private static final String BUNDLE_CATEGORY_OTHER_STR_KEY = "ProductConfig.CategoryOther";
    private static final String BUNDLE_PRODUCT_ATTRS_LIST_KEY = "ProductConfig.Attributes";
    private static final String BUNDLE_PRODUCT_IMAGES_LIST_KEY = "ProductConfig.Images";
    private static final String BUNDLE_EXISTING_PRODUCT_RESTORED_BOOL_KEY = "ProductConfig.IsExistingProductRestored";
    private static final String BUNDLE_PRODUCT_SKU_VALID_BOOL_KEY = "ProductConfig.IsProductSkuValid";
    private static final String BUNDLE_PRODUCT_NAME_ENTERED_BOOL_KEY = "ProductConfig.IsProductNameEntered";

    //The Presenter for this View
    private ProductConfigContract.Presenter mPresenter;

    //Stores the instance of the View components required
    private EditText mEditTextProductName;
    private TextInputLayout mTextInputProductSku;
    private TextInputEditText mEditTextProductSku;
    private TextInputEditText mEditTextProductDescription;
    private Spinner mSpinnerProductCategory;
    private EditText mEditTextProductCategoryOther;
    private RecyclerView mRecyclerViewProductAttrs;

    //Saves the Focus Change Listener registered view that had focus before save operation
    private View mLastRegisteredFocusChangeView;

    //Adapter for the Category Spinner
    private ArrayAdapter<String> mCategorySpinnerAdapter;

    //RecyclerView Adapter for the Additional Product Attributes
    private ProductAttributesAdapter mProductAttributesAdapter;

    //Stores the Product ID for an Edit request, retrieved from Bundle arguments passed
    private int mProductId;
    //Stores the Category selected
    private String mCategoryLastSelected;
    //Stores the Text for Category Other option
    private String mCategoryOtherText;
    //Stores the URI details of the Product Images
    private ArrayList<ProductImage> mProductImages;

    //Stores the state of Existing Product details restored,
    //to prevent updating the fields every time during System config change
    private boolean mIsExistingProductRestored;

    //Stores whether the Product SKU entered is valid or not
    private boolean mIsProductSkuValid;

    //Stores whether the Product Name was entered or not.
    //Used for monitoring unsaved progress
    private boolean mIsProductNameEntered;
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
                    //Start saving the Product Entry
                    saveProduct();
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
     * Mandatory Empty Constructor of {@link ProductConfigActivityFragment}.
     * This is required by the {@link android.support.v4.app.FragmentManager} to instantiate
     * the fragment (e.g. upon screen orientation changes).
     */
    public ProductConfigActivityFragment() {
    }

    /**
     * Static Factory Constructor that creates an instance of {@link ProductConfigActivityFragment}
     * using the provided {@code productId}
     *
     * @param productId The integer value of the Product Id of an existing Product;
     *                  or {@link ProductConfigContract#NEW_PRODUCT_INT} for a New Product Entry.
     * @return Instance of {@link ProductConfigActivityFragment}
     */
    public static ProductConfigActivityFragment newInstance(int productId) {
        //Saving the arguments passed, in a Bundle: START
        Bundle args = new Bundle(1);
        args.putInt(ARGUMENT_INT_PRODUCT_ID, productId);
        //Saving the arguments passed, in a Bundle: END

        //Instantiating the Fragment
        ProductConfigActivityFragment fragment = new ProductConfigActivityFragment();
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
     * @return Returns the View for the fragment's UI ('R.layout.fragment_product_config')
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout 'R.layout.fragment_product_config' for this fragment
        //Passing false as we are attaching the layout ourselves
        View rootView = inflater.inflate(R.layout.fragment_product_config, container, false);

        //Finding the views to initialize
        mEditTextProductName = rootView.findViewById(R.id.edittext_product_config_name);
        mTextInputProductSku = rootView.findViewById(R.id.textinput_product_config_sku);
        mEditTextProductSku = rootView.findViewById(R.id.edittext_product_config_sku);
        mEditTextProductDescription = rootView.findViewById(R.id.edittext_product_config_description);
        mSpinnerProductCategory = rootView.findViewById(R.id.spinner_product_config_category);
        mEditTextProductCategoryOther = rootView.findViewById(R.id.edittext_product_config_category_other);
        mRecyclerViewProductAttrs = rootView.findViewById(R.id.recyclerview_product_config_attrs);

        //Registering the Focus Change Listener on the Product Name field
        mEditTextProductName.setOnFocusChangeListener(this);
        //Registering the Focus Change Listener on Product SKU field
        mEditTextProductSku.setOnFocusChangeListener(this);
        //Registering the Focus Change Listener on Category Other Text Field
        mEditTextProductCategoryOther.setOnFocusChangeListener(this);

        //Attaching a TextWatcher for the Product SKU field
        mEditTextProductSku.addTextChangedListener(new ProductSkuTextWatcher());

        //Retrieving the Product Id from the Bundle
        Bundle arguments = getArguments();
        if (arguments != null) {
            mProductId = arguments.getInt(ARGUMENT_INT_PRODUCT_ID, ProductConfigContract.NEW_PRODUCT_INT);
        }

        //Setting Click Listeners for the Views
        rootView.findViewById(R.id.btn_product_config_add_attrs).setOnClickListener(this);

        //Initialize Category Spinner
        setupCategorySpinner();

        //Initialize RecyclerView for Product Attributes
        setupProductAttrsRecyclerView();

        //Returning the prepared view
        return rootView;
    }

    /**
     * Method that initializes the RecyclerView with its Adapter for the list
     * of Product's Additional Attributes {@link ProductAttribute}
     */
    private void setupProductAttrsRecyclerView() {
        //Creating a Vertical Linear Layout Manager with default layout order
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false);

        //Setting the Layout Manager to use
        mRecyclerViewProductAttrs.setLayoutManager(linearLayoutManager);

        //Initializing the Adapter
        mProductAttributesAdapter = new ProductAttributesAdapter();
        mPresenter.updateProductAttributes(null);

        //Setting the Adapter on RecyclerView
        mRecyclerViewProductAttrs.setAdapter(mProductAttributesAdapter);
    }

    /**
     * Method that initializes the Category Spinner with the Adapter and listener
     */
    private void setupCategorySpinner() {
        //Creating an Empty Category list
        List<String> categories = new ArrayList<>();

        //Creating Adapter for Spinner with a Spinner layout, passing in the empty list
        mCategorySpinnerAdapter = new ArrayAdapter<>(requireContext(),
                R.layout.item_product_config_category_spinner, categories);
        //Specifying the Drop down layout to use for choices
        mCategorySpinnerAdapter.setDropDownViewResource(R.layout.item_product_config_category_spinner_dropdown);

        //Attaching Adapter to Spinner
        mSpinnerProductCategory.setAdapter(mCategorySpinnerAdapter);

        //Setting the Item selected listener
        mSpinnerProductCategory.setOnItemSelectedListener(new CategorySpinnerClickListener());
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
        //Inflating the Menu options from 'R.menu.menu_fragment_product_config'
        inflater.inflate(R.menu.menu_fragment_product_config, menu);

        if (mProductId == ProductConfigContract.NEW_PRODUCT_INT) {
            //For a New Product Entry, "Delete" Action Menu needs to be hidden and disabled

            //Finding the Delete Action Menu Item
            MenuItem deleteMenuItem = menu.findItem(R.id.action_delete);
            //Hiding the Menu Item
            deleteMenuItem.setVisible(false);
            //Disabling the Menu Item
            deleteMenuItem.setEnabled(false);
        }
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
                //On Click of Delete Menu (applicable for an Existing Product entry only)

                //Delegating to the Presenter to show the re-confirmation dialog
                mPresenter.showDeleteProductDialog();
                return true;
            case R.id.action_save:
                //On Click of Save Menu

                //Saving the Product and its details
                saveProduct();
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
    private void saveProduct() {
        //Delegating to the Presenter to trigger focus loss on listener registered Views,
        //in order to persist their data
        mPresenter.triggerFocusLost();

        //Retrieving the data from the views and the adapter
        String productName = mEditTextProductName.getText().toString().trim();
        String productSku = mEditTextProductSku.getText().toString().trim();
        String productDescription = mEditTextProductDescription.getText().toString().trim();
        ArrayList<ProductAttribute> productAttributes = mProductAttributesAdapter.getProductAttributes();
        mCategoryOtherText = mEditTextProductCategoryOther.getText().toString().trim();

        //Delegating to the Presenter to initiate the Save process
        mPresenter.onSave(productName,
                productSku,
                productDescription,
                mCategoryLastSelected,
                mCategoryOtherText,
                productAttributes
        );
    }

    /**
     * Method invoked before save operation or screen orientation change to persist
     * any data held by the view that had focus and its listener registered.
     * This clears the focus held by the view to trigger the listener, causing to persist any unsaved data.
     */
    public void triggerFocusLost() {
        //Clearing focus on the last registered view
        if (mLastRegisteredFocusChangeView != null) {
            mLastRegisteredFocusChangeView.clearFocus();
            mLastRegisteredFocusChangeView = null;
        }

        //Clearing focus on the last registered view in Product Attributes RecyclerView
        if (mProductAttributesAdapter != null) {
            mProductAttributesAdapter.triggerFocusLost();
        }
    }

    /**
     * Method invoked to display a message on successful update/erase of the {@link ProductImage}s list.
     * This method will be invoked when the ProductImages are changed for an Existing Product,
     * immediately after returning from the {@link ProductImageActivity}
     */
    @Override
    public void showUpdateImagesSuccess() {
        if (getView() != null) {
            Snackbar.make(getView(),
                    getString(R.string.product_config_update_item_images_success, mEditTextProductSku.getText()),
                    Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Method that displays the EditText Field associated with the 'Other' Category
     * selected by the user.
     */
    @Override
    public void showCategoryOtherEditTextField() {
        mEditTextProductCategoryOther.setVisibility(View.VISIBLE);
    }

    /**
     * Method that hides the EditText Field of 'Other' Category when a predefined category
     * is selected by the user.
     */
    @Override
    public void hideCategoryOtherEditTextField() {
        mEditTextProductCategoryOther.setVisibility(View.INVISIBLE);
    }

    /**
     * Method that clears the EditText Field associated with the 'Other' Category
     */
    @Override
    public void clearCategoryOtherEditTextField() {
        mEditTextProductCategoryOther.setText("");
    }

    /**
     * Method invoked when required fields are missing data, on click of 'Save' Menu button.
     */
    @Override
    public void showEmptyFieldsValidationError() {
        showError(R.string.product_config_empty_fields_validation_error);
    }

    /**
     * Method invoked when either fields that make the {@link ProductAttribute} is missing.
     */
    @Override
    public void showAttributesPartialValidationError() {
        showError(R.string.product_config_attrs_partial_empty_fields_validation_error);
    }

    /**
     * Method invoked when the {@link ProductAttribute} identified by the Attribute Name {@code attributeName}
     * is already defined.
     *
     * @param attributeName The Attribute Name of the {@link ProductAttribute} that has been repeated.
     */
    @Override
    public void showAttributeNameConflictError(String attributeName) {
        showError(R.string.product_config_attrs_name_conflict_error, attributeName);
    }

    /**
     * Method that displays the Progress indicator
     *
     * @param statusTextId String resource for the status of the Progress to be shown.
     */
    @Override
    public void showProgressIndicator(@StringRes int statusTextId) {
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
     * Method invoked when an error is encountered during Product information
     * retrieval or save process.
     *
     * @param messageId String Resource of the error Message to be displayed
     * @param args      Variable number of arguments to replace the format specifiers
     *                  in the String resource if any
     */
    @Override
    public void showError(@StringRes int messageId, @Nullable Object... args) {
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
     * Method invoked when the Product SKU entered by the user is NOT Unique
     * causing the conflict.
     */
    @Override
    public void showProductSkuConflictError() {
        //Request the user to try a different SKU
        //Show error on the EditText
        mTextInputProductSku.setError(getString(R.string.product_config_sku_invalid_error));
    }

    /**
     * Method invoked when NO Product SKU was entered by the user.
     */
    @Override
    public void showProductSkuEmptyError() {
        //Show error on the EditText
        mTextInputProductSku.setError(getString(R.string.product_config_sku_empty_error));
    }

    /**
     * Method that updates the Product Name {@code name} to the View
     *
     * @param name The Name of the Product
     */
    @Override
    public void updateProductNameField(String name) {
        mEditTextProductName.setText(name);
    }

    /**
     * Method that updates the Product SKU {@code sku} to the View
     *
     * @param sku The SKU of the Product
     */
    @Override
    public void updateProductSkuField(String sku) {
        mEditTextProductSku.setText(sku);
    }

    /**
     * Method that locks the Product SKU field to prevent updates on this field
     */
    @Override
    public void lockProductSkuField() {
        mTextInputProductSku.setEnabled(false);
    }

    /**
     * Method that updates the Product Description {@code description} to the View
     *
     * @param description The description of the Product
     */
    @Override
    public void updateProductDescriptionField(String description) {
        mEditTextProductDescription.setText(description);
    }

    /**
     * Method that updates the Product Category to the View.
     *
     * @param selectedCategory  The selected Category of the Product
     * @param categoryOtherText The Category OTHER EditText field value, in the case where the Product Category
     *                          was not available from the Predefined list of Categories.
     */
    @Override
    public void updateCategorySelection(String selectedCategory, @Nullable String categoryOtherText) {
        //Saving the Category selected
        mCategoryLastSelected = selectedCategory;
        //Updating Category Spinner to show the Selected Product Category
        mSpinnerProductCategory.setSelection(mCategorySpinnerAdapter.getPosition(selectedCategory));
        if (!TextUtils.isEmpty(categoryOtherText)) {
            //If Manually entered Category is present,
            //then update the same to the corresponding EditText field
            mEditTextProductCategoryOther.setText(categoryOtherText);
        }
    }

    /**
     * Method invoked to keep the state of "Existing Product details restored", in sync with the Presenter.
     *
     * @param isExistingProductRestored Boolean that indicates the state of Existing Product data restored.
     *                                  <b>TRUE</b> if it had been restored; <b>FALSE</b> otherwise.
     */
    @Override
    public void syncExistingProductState(boolean isExistingProductRestored) {
        //Saving the state
        mIsExistingProductRestored = isExistingProductRestored;
    }

    /**
     * Method invoked to keep the state of "Product SKU Validity", in sync with the Presenter.
     *
     * @param isProductSkuValid Boolean that indicates whether the Product SKU entered was valid or not.
     *                          <b>TRUE</b> if the Product SKU is valid; <b>FALSE</b> otherwise.
     */
    @Override
    public void syncProductSkuValidity(boolean isProductSkuValid) {
        //Saving the state
        mIsProductSkuValid = isProductSkuValid;
    }

    /**
     * Method invoked to keep the state of "Product Name entered", in sync with the Presenter.
     * This is used for monitoring unsaved progress.
     *
     * @param isProductNameEntered Boolean that indicates whether the Product Name has been entered by the User or not.
     *                             <b>TRUE</b> if the Product Name is entered; <b>FALSE</b> otherwise.
     */
    @Override
    public void syncProductNameEnteredState(boolean isProductNameEntered) {
        //Saving the state
        mIsProductNameEntered = isProductNameEntered;
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
        builder.setMessage(R.string.product_config_unsaved_changes_dialog_message);
        //Set the Positive Button and its listener
        builder.setPositiveButton(R.string.product_config_unsaved_changes_dialog_positive_text, mUnsavedDialogOnClickListener);
        //Set the Negative Button and its listener
        builder.setNegativeButton(R.string.product_config_unsaved_changes_dialog_negative_text, mUnsavedDialogOnClickListener);
        //Set the Neutral Button and its listener
        builder.setNeutralButton(R.string.product_config_unsaved_changes_dialog_neutral_text, mUnsavedDialogOnClickListener);
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
        builder.setMessage(R.string.product_config_delete_product_confirm_dialog_message);
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
     * Method that updates the List of Product Attributes {@code productAttributes} to the View
     *
     * @param productAttributes The List of {@link ProductAttribute} of a Product
     */
    @Override
    public void updateProductAttributes(ArrayList<ProductAttribute> productAttributes) {
        mProductAttributesAdapter.replaceProductAttributes(productAttributes);
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
     * Called when the focus state of a view has changed.
     *
     * @param view     The view whose state has changed.
     * @param hasFocus The new focus state of {@code view}.
     */
    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) {
            //When a View has lost focus

            //Clear the View reference since the View has lost focus
            mLastRegisteredFocusChangeView = null;

            //Taking action based on the Id of the View that has lost focus
            switch (view.getId()) {
                case R.id.edittext_product_config_name:
                    //Update to the Presenter to notify that the Product Name has been entered
                    mPresenter.updateAndSyncProductNameEnteredState(!TextUtils.isEmpty(mEditTextProductName.getText().toString().trim()));
                    break;
                case R.id.edittext_product_config_sku:
                    //Validate the Product SKU entered, only for a New Product Entry
                    if (mProductId == ProductConfigContract.NEW_PRODUCT_INT) {
                        mPresenter.validateProductSku(mEditTextProductSku.getText().toString().trim());
                    }
                    break;
                case R.id.edittext_product_config_category_other:
                    //Validate and Update the Category fields
                    mPresenter.updateCategorySelection(mCategoryLastSelected, mEditTextProductCategoryOther.getText().toString());
                    break;
            }

        } else {
            //When a View had gained focus

            //Save the reference of the View in focus
            mLastRegisteredFocusChangeView = view;
        }
    }

    /**
     * Method that registers the Presenter {@code presenter} with the View implementing
     * {@link com.example.kaushiknsanji.storeapp.ui.BaseView}
     *
     * @param presenter Presenter instance implementing the {@link com.example.kaushiknsanji.storeapp.ui.BasePresenter}
     */
    @Override
    public void setPresenter(ProductConfigContract.Presenter presenter) {
        mPresenter = presenter;
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

            //Restoring Product Id which will be present if this is an Edit request
            mProductId = savedInstanceState.getInt(BUNDLE_PRODUCT_ID_INT_KEY);

            //Restoring Category related data
            //(Selection will be restored after Categories are downloaded)
            mCategoryLastSelected = savedInstanceState.getString(BUNDLE_CATEGORY_SELECTED_STR_KEY);
            mCategoryOtherText = savedInstanceState.getString(BUNDLE_CATEGORY_OTHER_STR_KEY);

            //Restoring Product Attributes
            mPresenter.updateProductAttributes(savedInstanceState.getParcelableArrayList(BUNDLE_PRODUCT_ATTRS_LIST_KEY));

            //Restoring Product Images
            mPresenter.updateProductImages(savedInstanceState.getParcelableArrayList(BUNDLE_PRODUCT_IMAGES_LIST_KEY));

            //Restoring the state of Product Name Entered
            mPresenter.updateAndSyncProductNameEnteredState(savedInstanceState.getBoolean(BUNDLE_PRODUCT_NAME_ENTERED_BOOL_KEY,
                    false));

            //Restoring the state of Existing Product data being last restored
            //if this was an Edit request
            mPresenter.updateAndSyncExistingProductState(savedInstanceState.getBoolean(BUNDLE_EXISTING_PRODUCT_RESTORED_BOOL_KEY,
                    false));

            //Restoring the state of Product SKU Validation
            mPresenter.updateAndSyncProductSkuValidity(savedInstanceState.getBoolean(BUNDLE_PRODUCT_SKU_VALID_BOOL_KEY,
                    false));

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
        outState.putInt(BUNDLE_PRODUCT_ID_INT_KEY, mProductId);
        outState.putString(BUNDLE_CATEGORY_SELECTED_STR_KEY, mCategoryLastSelected);
        outState.putString(BUNDLE_CATEGORY_OTHER_STR_KEY, mEditTextProductCategoryOther.getText().toString());
        outState.putParcelableArrayList(BUNDLE_PRODUCT_ATTRS_LIST_KEY, mProductAttributesAdapter.getProductAttributes());
        outState.putParcelableArrayList(BUNDLE_PRODUCT_IMAGES_LIST_KEY, mProductImages);
        outState.putBoolean(BUNDLE_PRODUCT_NAME_ENTERED_BOOL_KEY, mIsProductNameEntered);
        outState.putBoolean(BUNDLE_EXISTING_PRODUCT_RESTORED_BOOL_KEY, mIsExistingProductRestored);
        outState.putBoolean(BUNDLE_PRODUCT_SKU_VALID_BOOL_KEY, mIsProductSkuValid);
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

        //Start the work
        mPresenter.start();
    }

    /**
     * Method that updates the categories list to the spinner for configuration
     *
     * @param categories The list of categories available
     */
    @Override
    public void updateCategories(List<String> categories) {
        //Update the Category Spinner with the new data
        mCategorySpinnerAdapter.clear();
        //Add the Prompt as the first item
        categories.add(0, mSpinnerProductCategory.getPrompt().toString());
        mCategorySpinnerAdapter.addAll(categories);
        //Trigger data change event
        mCategorySpinnerAdapter.notifyDataSetChanged();

        if (!TextUtils.isEmpty(mCategoryLastSelected)) {
            //Validate and Update the Category selection if previously selected
            mPresenter.updateCategorySelection(mCategoryLastSelected, mCategoryOtherText);
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param view The view that was clicked.
     */
    @Override
    public void onClick(View view) {
        //Executing based on the View Id
        switch (view.getId()) {
            case R.id.btn_product_config_add_attrs:
                //For "Add More" button, present under Additional Attributes

                //Add an Empty row for capturing Additional Attribute
                mProductAttributesAdapter.addEmptyRecord();
                break;
        }
    }

    /**
     * RecyclerView {@link android.support.v7.widget.RecyclerView.Adapter} class to load the
     * list of Product Attributes {@link ProductAttribute} of the Product to be displayed/recorded.
     */
    private static class ProductAttributesAdapter extends RecyclerView.Adapter<ProductAttributesAdapter.ViewHolder> {

        //Stores the EditText View that had last acquired focus
        private View mLastFocusedView;

        //The Data of this Adapter
        private ArrayList<ProductAttribute> mProductAttributes;

        /**
         * Constructor of {@link ProductAttributesAdapter}
         */
        ProductAttributesAdapter() {
            //Initialize the Product Attribute List
            mProductAttributes = new ArrayList<>();
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
        public ProductAttributesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Inflating the item Layout view 'R.layout.item_product_config_attr'
            //Passing False as we are attaching the View ourselves
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_product_config_attr, parent, false);

            //Instantiating and returning the ViewHolder to cache reference to the view components in the item layout
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
        public void onBindViewHolder(@NonNull ProductAttributesAdapter.ViewHolder holder, int position) {
            //Binding the data at the position
            holder.bind(mProductAttributes.get(position));
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return mProductAttributes.size();
        }

        /**
         * Adds an empty {@link ProductAttribute} to the list of {@link ProductAttribute} shown
         * by the Adapter to capture new Additional Attribute Name and Value
         */
        void addEmptyRecord() {
            //Create an Empty ProductAttribute
            ProductAttribute productAttribute = new ProductAttribute.Builder()
                    .createProductAttribute();
            //Add it to adapter list
            mProductAttributes.add(productAttribute);
            //Notify the new item added to the end of the list, to show the empty record
            notifyItemInserted(mProductAttributes.size() - 1);
        }

        /**
         * Replaces the adapter data with the new list of Product Attributes {@code productAttributes}
         *
         * @param productAttributes New List of {@link ProductAttribute} to be displayed
         */
        void replaceProductAttributes(ArrayList<ProductAttribute> productAttributes) {
            if (productAttributes != null && productAttributes.size() >= 0) {
                //When there is data in the new list submitted

                //Clear the current list of Product Attributes
                mProductAttributes.clear();
                //Load the new list
                mProductAttributes.addAll(productAttributes);
                //Notify that there is a new list of Product Attributes to be shown
                notifyDataSetChanged();
            }

            if (mProductAttributes.size() == 0) {
                //Add an Empty Record to the list when there is no record present
                addEmptyRecord();
            }
        }

        /**
         * Getter Method for the data of this Adapter.
         *
         * @return ArrayList of {@link ProductAttribute} data
         */
        ArrayList<ProductAttribute> getProductAttributes() {
            return mProductAttributes;
        }

        /**
         * Method that clears focus on the registered view that last held it.
         * This causes to trigger the {@link android.view.View.OnFocusChangeListener#onFocusChange} event
         * on the view.
         */
        void triggerFocusLost() {
            if (mLastFocusedView != null) {
                mLastFocusedView.clearFocus();
                mLastFocusedView = null;
            }
        }

        /**
         * Method that deletes the {@link ProductAttribute} item at the {@code position} passed.
         *
         * @param position The position of the {@link ProductAttribute} item to be deleted.
         */
        void deleteRecord(int position) {
            //Validating the item position passed
            if (position > RecyclerView.NO_POSITION) {
                //When the item position passed is valid

                //Remove the ProductAttribute at the position specified
                mProductAttributes.remove(position);
                //Notify Item position removed
                notifyItemRemoved(position);
            }
        }

        /**
         * ViewHolder class for caching View components of the template item view 'R.layout.item_product_config_attr'
         */
        class ViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener, View.OnFocusChangeListener {
            //The EditText that records/displays the Product Attribute Name
            private TextInputEditText mEditTextAttrName;
            //The EditText that records/displays the Product Attribute Value
            private TextInputEditText mEditTextAttrValue;
            //The ImageButton for "Remove" Action
            private ImageButton mImageButtonRemoveAction;

            /**
             * Constructor of the ViewHolder
             *
             * @param itemView is the inflated item layout View passed
             *                 for caching its View components
             */
            ViewHolder(View itemView) {
                super(itemView);

                //Finding the Views needed
                mEditTextAttrName = itemView.findViewById(R.id.edittext_item_product_config_attr_name);
                mEditTextAttrValue = itemView.findViewById(R.id.edittext_item_product_config_attr_value);
                mImageButtonRemoveAction = itemView.findViewById(R.id.imgbtn_item_product_config_attr_remove);

                //Registering Focus Change Listeners on TextInput Fields to capture the updated value
                mEditTextAttrName.setOnFocusChangeListener(this);
                mEditTextAttrValue.setOnFocusChangeListener(this);

                //Setting Click Listener on ImageButton
                mImageButtonRemoveAction.setOnClickListener(this);
            }

            /**
             * Method that binds the data {@code productAttribute} to the item View.
             *
             * @param productAttribute {@link ProductAttribute} data at the position
             *                         to be bounded to the Item View.
             */
            void bind(ProductAttribute productAttribute) {
                //Set the Attribute Name
                mEditTextAttrName.setText(productAttribute.getAttributeName());
                //Set the Attribute Value
                mEditTextAttrValue.setText(productAttribute.getAttributeValue());
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

                    //Take action based on the id of the View being clicked
                    if (view.getId() == R.id.imgbtn_item_product_config_attr_remove) {
                        //For the Remove Action ImageButton

                        //Delete the record at the position clicked
                        deleteRecord(adapterPosition);
                    }
                }
            }

            /**
             * Called when the focus state of a view has changed.
             *
             * @param view     The view whose state has changed.
             * @param hasFocus The new focus state of {@code view}.
             */
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                //Get the current adapter position
                int adapterPosition = getAdapterPosition();
                if (adapterPosition > RecyclerView.NO_POSITION) {
                    //When the item position is valid

                    //Get the data at the position
                    ProductAttribute productAttribute = mProductAttributes.get(adapterPosition);

                    if (!hasFocus) {
                        //When the registered View has lost focus

                        //Clear the view reference
                        mLastFocusedView = null;

                        //Take action based on the id of the view which lost focus
                        switch (view.getId()) {
                            case R.id.edittext_item_product_config_attr_name:
                                //For the EditText that records/displays the Attribute Name

                                //Grab the Attribute Name from the EditText and update it to the current ProductAttribute data
                                productAttribute.setAttributeName(mEditTextAttrName.getText().toString().trim());
                                break;
                            case R.id.edittext_item_product_config_attr_value:
                                //For the EditText that records/displays the Attribute Value

                                //Grab the Attribute Value from the EditText and update it to the current ProductAttribute data
                                productAttribute.setAttributeValue(mEditTextAttrValue.getText().toString().trim());
                                break;
                        }
                    } else {
                        //When the registered view has gained focus

                        //Save the view reference
                        mLastFocusedView = view;
                    }
                }
            }

        }

    }

    /**
     * {@link TextWatcher} for the Product SKU Text field 'R.id.edittext_product_config_sku'
     */
    private class ProductSkuTextWatcher implements TextWatcher {
        /**
         * This method is called to notify you that, within <code>s</code>,
         * the <code>count</code> characters beginning at <code>start</code>
         * are about to be replaced by new text with length <code>after</code>.
         * It is an error to attempt to make changes to <code>s</code> from
         * this callback.
         */
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //Clear the error on EditText if any
            mTextInputProductSku.setError(null);
        }

        /**
         * This method is called to notify you that, within <code>s</code>,
         * the <code>count</code> characters beginning at <code>start</code>
         * have just replaced old text that had length <code>before</code>.
         * It is an error to attempt to make changes to <code>s</code> from
         * this callback.
         */
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //no-op
        }

        /**
         * This method is called to notify you that, somewhere within
         * <code>s</code>, the text has been changed.
         * It is legitimate to make further changes to <code>s</code> from
         * this callback, but be careful not to get yourself into an infinite
         * loop, because any changes you make will cause this method to be
         * called again recursively.
         */
        @Override
        public void afterTextChanged(Editable s) {
            //no-op
        }
    }

    /**
     * Class that implements the Spinner's item selected listener, used for Category selection
     */
    private class CategorySpinnerClickListener implements Spinner.OnItemSelectedListener {

        /**
         * <p>Callback method to be invoked when an item in this view has been
         * selected. This callback is invoked only when the newly selected
         * position is different from the previously selected position or if
         * there was no selected item.</p>
         * <p>
         * Implementers can call getItemAtPosition(position) if they need to access the
         * data associated with the selected item.
         *
         * @param parent   The AdapterView where the selection happened
         * @param view     The view within the AdapterView that was clicked
         * @param position The position of the view in the adapter
         * @param id       The row id of the item that is selected
         */
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //NOTE: Position 0 is reserved for the manually added Prompt
            if (position > 0) {
                //Retrieving the Category Name of the selection
                mCategoryLastSelected = parent.getItemAtPosition(position).toString();
                //Calling the Presenter method to take appropriate action
                //(For showing/hiding the EditText field of Category OTHER)
                mPresenter.onCategorySelected(mCategoryLastSelected);
            } else {
                //On other cases, reset the Category Name saved
                mCategoryLastSelected = "";
            }
        }

        /**
         * Callback method to be invoked when the selection disappears from this
         * view. The selection can disappear for instance when touch is activated
         * or when the adapter becomes empty.
         *
         * @param parent The AdapterView that now contains no selected item.
         */
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            //No-op
        }
    }
}
