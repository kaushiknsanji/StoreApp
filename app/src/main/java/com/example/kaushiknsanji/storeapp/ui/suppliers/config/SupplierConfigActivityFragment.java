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

package com.example.kaushiknsanji.storeapp.ui.suppliers.config;


import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SupplierContract;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductLite;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductSupplierInfo;
import com.example.kaushiknsanji.storeapp.data.local.models.SupplierContact;
import com.example.kaushiknsanji.storeapp.ui.common.ProgressDialogFragment;
import com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigActivity;
import com.example.kaushiknsanji.storeapp.utils.ContactUtility;
import com.example.kaushiknsanji.storeapp.utils.OrientationUtility;
import com.example.kaushiknsanji.storeapp.utils.SnackbarUtility;
import com.example.kaushiknsanji.storeapp.workers.ImageDownloaderFragment;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * Content Fragment of {@link SupplierConfigActivity} that inflates the layout 'R.layout.fragment_supplier_config'
 * to display/record the configuration details of the Supplier. This implements the
 * {@link SupplierConfigContract.View} on the lines of Model-View-Presenter architecture.
 *
 * @author Kaushik N Sanji
 */
public class SupplierConfigActivityFragment extends Fragment
        implements SupplierConfigContract.View, View.OnFocusChangeListener, View.OnClickListener {

    //Constant used for logs
    private static final String LOG_TAG = SupplierConfigActivityFragment.class.getSimpleName();

    //The Bundle argument constant of this Fragment
    private static final String ARGUMENT_INT_SUPPLIER_ID = "argument.SUPPLIER_ID";

    //Bundle constants for persisting the data throughout System config changes
    private static final String BUNDLE_SUPPLIER_CONTACTS_PHONE_KEY = "SupplierConfig.Contacts.Phone";
    private static final String BUNDLE_SUPPLIER_CONTACTS_EMAIL_KEY = "SupplierConfig.Contacts.Email";
    private static final String BUNDLE_SUPPLIER_PRODUCTS_DATA_SPARSE_ARRAY_KEY = "SupplierConfig.ProductLites";
    private static final String BUNDLE_SUPPLIER_PRODUCTS_PRICE_KEY = "SupplierConfig.ProductSupplierInfos";
    private static final String BUNDLE_EXISTING_SUPPLIER_RESTORED_BOOL_KEY = "SupplierConfig.IsExistingSupplierRestored";
    private static final String BUNDLE_SUPPLIER_CODE_VALID_BOOL_KEY = "SupplierConfig.IsSupplierCodeValid";
    private static final String BUNDLE_SUPPLIER_NAME_ENTERED_BOOL_KEY = "SupplierConfig.IsSupplierNameEntered";

    //The Presenter for this View
    private SupplierConfigContract.Presenter mPresenter;

    //Stores the instance of the View components required
    private EditText mEditTextSupplierName;
    private TextInputLayout mTextInputSupplierCode;
    private EditText mEditTextSupplierCode;
    private RecyclerView mRecyclerViewContactPhone;
    private RecyclerView mRecyclerViewContactEmail;
    private RecyclerView mRecyclerViewSupplierProduct;

    //The RecyclerView Adapter for Phone Contacts
    private SupplierContactAdapter mPhoneContactsAdapter;

    //The RecyclerView Adapter for Email Contacts
    private SupplierContactAdapter mEmailContactsAdapter;

    //The RecyclerView Adapter for Supplier Products
    private SupplierProductsAdapter mSupplierProductsAdapter;

    //Saves the Focus Change Listener registered view that had focus before save operation
    private View mLastRegisteredFocusChangeView;

    //Stores the Supplier ID for an Edit request, retrieved from the Bundle arguments passed
    private int mSupplierId;

    //Stores the state of Existing Supplier details restored,
    //to prevent updating the fields every time during System config change
    private boolean mIsExistingSupplierRestored;

    //Stores whether the Supplier Code entered was valid or not
    private boolean mIsSupplierCodeValid;

    //Stores whether the Supplier Name was entered or not.
    //Used for monitoring unsaved progress.
    private boolean mIsSupplierNameEntered;
    /**
     * The {@link AlertDialog} Click Listener for the Supplier Delete Menu.
     */
    private DialogInterface.OnClickListener mSupplierDeleteDialogOnClickListener = new DialogInterface.OnClickListener() {
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
                    //Dispatch to the Presenter to delete the Supplier
                    mPresenter.deleteSupplier();
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
                    //Start saving the Supplier Entry
                    saveSupplier();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //For "Discard" button

                    //Dismiss the dialog
                    dialog.dismiss();
                    //Unlock orientation
                    OrientationUtility.unlockScreenOrientation(requireActivity());
                    //Dispatch to the Presenter to finish the Activity
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
     * Mandatory Empty Constructor of {@link SupplierConfigActivityFragment}.
     * This is required by the {@link android.support.v4.app.FragmentManager} to instantiate
     * the fragment (e.g. upon screen orientation changes).
     */
    public SupplierConfigActivityFragment() {
    }

    /**
     * Static Factory Constructor that creates an instance of {@link SupplierConfigActivityFragment}
     * using the provided {@code supplierId}
     *
     * @param supplierId The Integer value of the Supplier Id of an existing Supplier;
     *                   or {@link SupplierConfigContract#NEW_SUPPLIER_INT} if it is
     *                   for a New Supplier Entry.
     * @return Instance of {@link SupplierConfigActivityFragment}
     */
    public static SupplierConfigActivityFragment newInstance(int supplierId) {
        //Saving the arguments passed, in a Bundle: START
        Bundle args = new Bundle(1);
        args.putInt(ARGUMENT_INT_SUPPLIER_ID, supplierId);
        //Saving the arguments passed, in a Bundle: END

        //Instantiating the Fragment
        SupplierConfigActivityFragment fragment = new SupplierConfigActivityFragment();
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
     * @return Returns the View for the fragment's UI ('R.layout.fragment_supplier_config')
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout 'R.layout.fragment_supplier_config' for this fragment
        //Passing false as we are attaching the layout ourselves
        View rootView = inflater.inflate(R.layout.fragment_supplier_config, container, false);

        //Finding the Views to initialize
        mEditTextSupplierName = rootView.findViewById(R.id.edittext_supplier_config_name);
        mTextInputSupplierCode = rootView.findViewById(R.id.textinput_supplier_config_code);
        mEditTextSupplierCode = rootView.findViewById(R.id.edittext_supplier_config_code);
        mRecyclerViewContactPhone = rootView.findViewById(R.id.recyclerview_supplier_config_phone);
        mRecyclerViewContactEmail = rootView.findViewById(R.id.recyclerview_supplier_config_email);
        mRecyclerViewSupplierProduct = rootView.findViewById(R.id.recyclerview_supplier_config_items);

        //Registering the Focus Change Listener on the Supplier Name field
        mEditTextSupplierName.setOnFocusChangeListener(this);
        //Registering the Focus Change Listener on the Supplier Code field
        mEditTextSupplierCode.setOnFocusChangeListener(this);

        //Attaching a Text Watcher for the Supplier Code field
        mEditTextSupplierCode.addTextChangedListener(new SupplierCodeTextWatcher());

        //Registering Click Listener on "Add Phone" button
        rootView.findViewById(R.id.btn_supplier_config_add_phone).setOnClickListener(this);
        //Registering Click Listener on "Add Email" button
        rootView.findViewById(R.id.btn_supplier_config_add_email).setOnClickListener(this);
        //Registering Click Listener on "Add Item" button
        rootView.findViewById(R.id.btn_supplier_config_add_item).setOnClickListener(this);

        //Retrieving the Supplier Id from the Bundle
        Bundle arguments = getArguments();
        if (arguments != null) {
            mSupplierId = arguments.getInt(ARGUMENT_INT_SUPPLIER_ID, SupplierConfigContract.NEW_SUPPLIER_INT);
        }

        //Initialize RecyclerView for Supplier Contact Phones
        setupContactPhonesRecyclerView();

        //Initialize RecyclerView for Supplier Contact Emails
        setupContactEmailsRecyclerView();

        //Initialize RecyclerView for Supplier Items
        setupSupplierItemsRecyclerView();

        //Returning the prepared layout
        return rootView;
    }

    /**
     * Method that registers the Presenter {@code presenter} with the View implementing
     * {@link com.example.kaushiknsanji.storeapp.ui.BaseView}
     *
     * @param presenter Presenter instance implementing the {@link com.example.kaushiknsanji.storeapp.ui.BasePresenter}
     */
    @Override
    public void setPresenter(SupplierConfigContract.Presenter presenter) {
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

            //Restoring Supplier Contacts
            ArrayList<SupplierContact> phoneContactList = savedInstanceState.getParcelableArrayList(BUNDLE_SUPPLIER_CONTACTS_PHONE_KEY);
            ArrayList<SupplierContact> emailContactList = savedInstanceState.getParcelableArrayList(BUNDLE_SUPPLIER_CONTACTS_EMAIL_KEY);
            ArrayList<SupplierContact> supplierContacts = new ArrayList<>();
            supplierContacts.addAll(phoneContactList);
            supplierContacts.addAll(emailContactList);
            mPresenter.updateSupplierContacts(supplierContacts);

            //Restoring Supplier Items and Items data
            SparseArray<ProductLite> productLiteSparseArray = savedInstanceState.getSparseParcelableArray(BUNDLE_SUPPLIER_PRODUCTS_DATA_SPARSE_ARRAY_KEY);
            ArrayList<ProductSupplierInfo> productSupplierInfoList = savedInstanceState.getParcelableArrayList(BUNDLE_SUPPLIER_PRODUCTS_PRICE_KEY);
            mPresenter.updateSupplierProducts(productSupplierInfoList, productLiteSparseArray);

            //Restoring the state of Supplier Name Entered
            mPresenter.updateAndSyncSupplierNameEnteredState(savedInstanceState.getBoolean(BUNDLE_SUPPLIER_NAME_ENTERED_BOOL_KEY, false));

            //Restoring the state of Existing Supplier data being last restored
            //if this was an Edit request
            mPresenter.updateAndSyncExistingSupplierState(savedInstanceState.getBoolean(BUNDLE_EXISTING_SUPPLIER_RESTORED_BOOL_KEY, false));

            //Restoring the state of Supplier Code Validation
            mPresenter.updateAndSyncSupplierCodeValidity(savedInstanceState.getBoolean(BUNDLE_SUPPLIER_CODE_VALID_BOOL_KEY, false));
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
        outState.putParcelableArrayList(BUNDLE_SUPPLIER_CONTACTS_PHONE_KEY, mPhoneContactsAdapter.getSupplierContacts());
        outState.putParcelableArrayList(BUNDLE_SUPPLIER_CONTACTS_EMAIL_KEY, mEmailContactsAdapter.getSupplierContacts());
        outState.putSparseParcelableArray(BUNDLE_SUPPLIER_PRODUCTS_DATA_SPARSE_ARRAY_KEY, mSupplierProductsAdapter.getProductLiteSparseArray());
        outState.putParcelableArrayList(BUNDLE_SUPPLIER_PRODUCTS_PRICE_KEY, mSupplierProductsAdapter.getProductSupplierInfoList());
        outState.putBoolean(BUNDLE_EXISTING_SUPPLIER_RESTORED_BOOL_KEY, mIsExistingSupplierRestored);
        outState.putBoolean(BUNDLE_SUPPLIER_CODE_VALID_BOOL_KEY, mIsSupplierCodeValid);
        outState.putBoolean(BUNDLE_SUPPLIER_NAME_ENTERED_BOOL_KEY, mIsSupplierNameEntered);
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
     * Initialize the contents of the Fragment host's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     *
     * @param menu     The options menu in which you place your items.
     * @param inflater The LayoutInflater object that can be used to inflate the Menu options
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //Inflating the Menu options from 'R.menu.menu_fragment_supplier_config'
        inflater.inflate(R.menu.menu_fragment_supplier_config, menu);

        if (mSupplierId == SupplierConfigContract.NEW_SUPPLIER_INT) {
            //For a New Supplier Entry, "Delete" Action Menu needs to be hidden and disabled

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
                //On Click of Delete Menu (applicable for an Existing Supplier entry only)

                //Delegating to the Presenter to show the re-confirmation dialog
                mPresenter.showDeleteSupplierDialog();
                return true;
            case R.id.action_save:
                //On Click of Save Menu

                //Saving the Supplier and its details
                saveSupplier();
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
    private void saveSupplier() {
        //Delegating to the Presenter to trigger focus loss on listener registered View,
        //in order to persist their data
        mPresenter.triggerFocusLost();

        //Retrieving the data from the views and the adapter
        String supplierName = mEditTextSupplierName.getText().toString().trim();
        String supplierCode = mEditTextSupplierCode.getText().toString().trim();
        ArrayList<SupplierContact> phoneContacts = mPhoneContactsAdapter.getSupplierContacts();
        ArrayList<SupplierContact> emailContacts = mEmailContactsAdapter.getSupplierContacts();
        ArrayList<ProductSupplierInfo> productSupplierInfoList = mSupplierProductsAdapter.getProductSupplierInfoList();

        //Delegating to the Presenter to initiate the Save process
        mPresenter.onSave(supplierName,
                supplierCode,
                phoneContacts,
                emailContacts,
                productSupplierInfoList);
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
                case R.id.edittext_supplier_config_name:
                    //For the EditText field of Supplier Name

                    //Update to the Presenter to notify that the Supplier Name has been entered
                    mPresenter.updateAndSyncSupplierNameEnteredState(!TextUtils.isEmpty(mEditTextSupplierName.getText().toString().trim()));
                    break;
                case R.id.edittext_supplier_config_code:
                    //For the EditText field of Supplier Code

                    //Validate the Supplier Code entered, only for a New Supplier Entry
                    if (mSupplierId == SupplierConfigContract.NEW_SUPPLIER_INT) {
                        mPresenter.validateSupplierCode(mEditTextSupplierCode.getText().toString().trim());
                    }
                    break;
            }

        } else {
            //When a View had gained focus

            //Save the reference of the View in focus
            mLastRegisteredFocusChangeView = view;
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
            case R.id.btn_supplier_config_add_phone:
                //For "Add Phone" button, present under "Contact Details : Phone"

                //Add an Empty row for capturing the Phone Contact Details
                mPhoneContactsAdapter.addEmptyRecord();
                break;
            case R.id.btn_supplier_config_add_email:
                //For "Add Email" button, present under "Contact Details : Email"

                //Add an Empty row for capturing the Email Contact Details
                mEmailContactsAdapter.addEmptyRecord();
                break;
            case R.id.btn_supplier_config_add_item:
                //For "Add Item" button, present under "Supplier Items"

                //Delegating to the Presenter to launch the Activity for picking Products
                mPresenter.pickProducts(mSupplierProductsAdapter.getProductLiteList());
                break;
        }
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
     * Method invoked when an error is encountered during Supplier information
     * retrieval or save process.
     *
     * @param messageId String Resource of the error Message to be displayed
     * @param args      Variable number of arguments to replace the format specifiers
     *                  in the String resource if any
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
     * Method that locks the Supplier Code field to prevent updates on this field.
     */
    @Override
    public void lockSupplierCodeField() {
        mTextInputSupplierCode.setEnabled(false);
    }

    /**
     * Method invoked to keep the state of "Existing Supplier details restored", in sync with the Presenter.
     *
     * @param isExistingSupplierRestored Boolean that indicates the state of Existing Supplier data restored.
     *                                   <b>TRUE</b> if it had been restored; <b>FALSE</b> otherwise.
     */
    @Override
    public void syncExistingSupplierState(boolean isExistingSupplierRestored) {
        //Saving the state
        mIsExistingSupplierRestored = isExistingSupplierRestored;
    }

    /**
     * Method invoked to keep the state of "Supplier Code Validity", in sync with the Presenter.
     *
     * @param isSupplierCodeValid Boolean that indicates whether the Supplier Code entered was valid or not.
     *                            <b>TRUE</b> if the Supplier Code is valid; <b>FALSE</b> otherwise.
     */
    @Override
    public void syncSupplierCodeValidity(boolean isSupplierCodeValid) {
        //Saving the state
        mIsSupplierCodeValid = isSupplierCodeValid;
    }

    /**
     * Method invoked to keep the state of "Supplier Name entered", in sync with the Presenter.
     * This is used for monitoring unsaved progress.
     *
     * @param isSupplierNameEntered Boolean that indicates whether the Supplier Name has been entered by the User or not.
     *                              <b>TRUE</b> if the Supplier Name is entered; <b>FALSE</b> otherwise.
     */
    @Override
    public void syncSupplierNameEnteredState(boolean isSupplierNameEntered) {
        //Saving the state
        mIsSupplierNameEntered = isSupplierNameEntered;
    }

    /**
     * Method invoked when the Supplier Code entered by the user is NOT Unique
     * causing the conflict.
     */
    @Override
    public void showSupplierCodeConflictError() {
        //Request the user to try a different Supplier Code
        //Show error on the EditText
        mTextInputSupplierCode.setError(getString(R.string.supplier_config_code_invalid_error));
    }

    /**
     * Method that updates the Supplier Name {@code supplierName} to the View
     *
     * @param supplierName The Name of the Supplier
     */
    @Override
    public void updateSupplierNameField(String supplierName) {
        mEditTextSupplierName.setText(supplierName);
    }

    /**
     * Method that updates the Supplier Code {@code supplierCode} to the View
     *
     * @param supplierCode The Code of the Supplier
     */
    @Override
    public void updateSupplierCodeField(String supplierCode) {
        mEditTextSupplierCode.setText(supplierCode);
    }

    /**
     * Method that updates the Supplier's Phone Contacts {@code phoneContacts} to the View
     *
     * @param phoneContacts List of {@link SupplierContact} of Phone Contact Type, of the Supplier
     */
    @Override
    public void updatePhoneContacts(ArrayList<SupplierContact> phoneContacts) {
        //Updating the data of the Supplier Contacts Adapter for Phone
        mPhoneContactsAdapter.replaceSupplierContactList(phoneContacts);
    }

    /**
     * Method that updates the Supplier's Email Contacts {@code emailContacts} to the View
     *
     * @param emailContacts List of {@link SupplierContact} of Email Contact Type, of the Supplier
     */
    @Override
    public void updateEmailContacts(ArrayList<SupplierContact> emailContacts) {
        //Updating the data of the Supplier Contacts Adapter for Email
        mEmailContactsAdapter.replaceSupplierContactList(emailContacts);
    }

    /**
     * Method that updates the List of Products {@link ProductLite} sold
     * by the Supplier with Price information {@link ProductSupplierInfo}, to the View.
     *
     * @param productSupplierInfoList The List of {@link ProductSupplierInfo} which contains the
     *                                Products with Price details that is sold by the Supplier.
     * @param productLiteSparseArray  {@link SparseArray} of {@link ProductLite} which contains the
     *                                data of the Products sold by the Supplier.
     */
    @Override
    public void updateSupplierProducts(ArrayList<ProductSupplierInfo> productSupplierInfoList,
                                       @Nullable SparseArray<ProductLite> productLiteSparseArray) {
        //Submitting to the Adapter
        mSupplierProductsAdapter.submitData(productSupplierInfoList, productLiteSparseArray);
    }

    /**
     * Method invoked when No Supplier Code was entered by the user.
     */
    @Override
    public void showSupplierCodeEmptyError() {
        //Show error on the EditText
        mTextInputSupplierCode.setError(getString(R.string.supplier_config_code_empty_error));
    }

    /**
     * Method invoked before save operation or screen orientation change to persist
     * any data held by the view that had focus and its listener registered.
     * This clears the focus held by the view to trigger the listener, causing to persist any unsaved data.
     */
    @Override
    public void triggerFocusLost() {
        //Clearing focus on the last registered view
        if (mLastRegisteredFocusChangeView != null) {
            mLastRegisteredFocusChangeView.clearFocus();
            mLastRegisteredFocusChangeView = null;
        }

        //Clearing focus on the last registered view in Phone Contacts Adapter
        if (mPhoneContactsAdapter != null) {
            mPhoneContactsAdapter.triggerFocusLost();
        }

        //Clearing focus on the last registered view in Email Contacts Adapter
        if (mEmailContactsAdapter != null) {
            mEmailContactsAdapter.triggerFocusLost();
        }

        //Clearing focus on the last registered view in Supplier Products Adapter
        if (mSupplierProductsAdapter != null) {
            mSupplierProductsAdapter.triggerFocusLost();
        }
    }

    /**
     * Method invoked when required fields are missing data, on click of 'Save' Menu button.
     */
    @Override
    public void showEmptyFieldsValidationError() {
        showError(R.string.supplier_config_empty_fields_validation_error);
    }

    /**
     * Method invoked when more than one {@link SupplierContact} is found to have
     * the same Contact value {@code contactValue}.
     *
     * @param conflictMessageResId The String resource of conflict error message to be shown.
     * @param contactValue         The Contact Value which is repeated causing the conflict.
     */
    @Override
    public void showSupplierContactConflictError(@StringRes int conflictMessageResId, String contactValue) {
        showError(conflictMessageResId, contactValue);
    }

    /**
     * Method invoked when there is no contact information configured for the Supplier.
     */
    @Override
    public void showEmptyContactsError() {
        showError(R.string.supplier_config_empty_contacts_error);
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
        builder.setMessage(R.string.supplier_config_unsaved_changes_dialog_message);
        //Set the Positive Button and its listener
        builder.setPositiveButton(R.string.supplier_config_unsaved_changes_dialog_positive_text, mUnsavedDialogOnClickListener);
        //Set the Negative Button and its listener
        builder.setNegativeButton(R.string.supplier_config_unsaved_changes_dialog_negative_text, mUnsavedDialogOnClickListener);
        //Set the Neutral Button and its listener
        builder.setNeutralButton(R.string.supplier_config_unsaved_changes_dialog_neutral_text, mUnsavedDialogOnClickListener);
        //Lock the Orientation
        OrientationUtility.lockCurrentScreenOrientation(requireActivity());
        //Create and display the AlertDialog
        builder.create().show();
    }

    /**
     * Method invoked when the user clicks on the Delete Menu Action to delete the Supplier.
     * This should launch a Dialog for the user to reconfirm the request before proceeding
     * with the Delete Action.
     */
    @Override
    public void showDeleteSupplierDialog() {
        //Creating an AlertDialog with a message, and listeners for the positive and negative buttons
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        //Set the Message
        builder.setMessage(R.string.supplier_config_delete_supplier_confirm_dialog_message);
        //Set the Positive Button and its listener
        builder.setPositiveButton(android.R.string.yes, mSupplierDeleteDialogOnClickListener);
        //Set the Negative Button and its listener
        builder.setNegativeButton(android.R.string.no, mSupplierDeleteDialogOnClickListener);
        //Lock the Orientation
        OrientationUtility.lockCurrentScreenOrientation(requireActivity());
        //Create and display the AlertDialog
        builder.create().show();
    }

    /**
     * Method invoked when the user swipes left/right any Item View of the Products sold by the Supplier
     * in order to remove it from the list. This should show a Snackbar with Action UNDO.
     *
     * @param productSku The Product SKU of the Product being swiped out.
     */
    @Override
    public void showSupplierProductSwiped(String productSku) {
        if (getView() != null) {
            new SnackbarUtility(Snackbar.make(getView(),
                    getString(R.string.supplier_config_product_swipe_action_success,
                            productSku), Snackbar.LENGTH_LONG))
                    .revealCompleteMessage()
                    .setAction(R.string.snackbar_action_undo, (view) -> {
                        //Try and Restore the Adapter data when UNDO is clicked
                        if (mSupplierProductsAdapter.restoreLastRemovedProduct()) {
                            //On Success, show a Snackbar message
                            Snackbar.make(getView(),
                                    getString(R.string.supplier_config_product_swipe_action_undo_success, productSku),
                                    Snackbar.LENGTH_LONG).show();
                        }
                    })
                    .showSnack();
        }
    }

    /**
     * Method that displays a message on Success of Updating an Existing Product.
     *
     * @param productSku String containing the SKU of the Product that was updated successfully.
     */
    @Override
    public void showUpdateSuccess(String productSku) {
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.product_list_item_update_success, productSku), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Method that displays a message on Success of Deleting an Existing Product
     *
     * @param productSku String containing the SKU of the Product that was deleted successfully.
     */
    @Override
    public void showDeleteSuccess(String productSku) {
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.product_list_item_delete_success, productSku), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Method invoked when one of the Supplier's Products were edited successfully and
     * returned through Activity result. This method should notify the Adapter to rebind
     * the data for the product with Id {@code productId}
     *
     * @param productId The Integer Id of the Product whose data needs to be rebound.
     */
    @Override
    public void notifyProductChanged(int productId) {
        //Delegating to the Adapter to rebind
        mSupplierProductsAdapter.notifyProductChanged(productId);
    }

    /**
     * Method invoked during save operation, when a {@link SupplierContact}
     * value is found to be still invalid.
     *
     * @param invalidMessageResId The String resource of the error message to be shown.
     */
    @Override
    public void showSupplierContactsInvalidError(@StringRes int invalidMessageResId) {
        showError(invalidMessageResId);
    }

    /**
     * Method that initializes the RecyclerView 'R.id.recyclerview_supplier_config_phone' and its Adapter.
     */
    private void setupContactPhonesRecyclerView() {
        //Creating a Vertical Linear Layout Manager with default layout order
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false);

        //Setting the Layout Manager to use
        mRecyclerViewContactPhone.setLayoutManager(linearLayoutManager);

        //Initializing the Adapter
        mPhoneContactsAdapter = new SupplierContactAdapter(SupplierContract.SupplierContactType.CONTACT_TYPE_PHONE);

        //Setting the Adapter on RecyclerView
        mRecyclerViewContactPhone.setAdapter(mPhoneContactsAdapter);
    }

    /**
     * Method that initializes the RecyclerView 'R.id.recyclerview_supplier_config_email' and its Adapter.
     */
    private void setupContactEmailsRecyclerView() {
        //Creating a Vertical Linear Layout Manager with default layout order
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false);

        //Setting the Layout Manager to use
        mRecyclerViewContactEmail.setLayoutManager(linearLayoutManager);

        //Initializing the Adapter
        mEmailContactsAdapter = new SupplierContactAdapter(SupplierContract.SupplierContactType.CONTACT_TYPE_EMAIL);

        //Setting the Adapter on RecyclerView
        mRecyclerViewContactEmail.setAdapter(mEmailContactsAdapter);
    }

    /**
     * Method that initializes the RecyclerView 'R.id.recyclerview_supplier_config_items' and its Adapter.
     */
    private void setupSupplierItemsRecyclerView() {
        //Creating a Vertical Linear Layout Manager with default layout order
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false);

        //Setting the Layout Manager to use
        mRecyclerViewSupplierProduct.setLayoutManager(linearLayoutManager);

        //Initializing the Adapter
        mSupplierProductsAdapter = new SupplierProductsAdapter(requireContext(), new SupplierProductItemUserActionsListener());

        //Setting the Adapter on RecyclerView
        mRecyclerViewSupplierProduct.setAdapter(mSupplierProductsAdapter);

        //Attaching the ItemTouchHelper for Swipe delete
        mSupplierProductsAdapter.getItemTouchHelper().attachToRecyclerView(mRecyclerViewSupplierProduct);

    }

    /**
     * RecyclerView {@link android.support.v7.widget.RecyclerView.Adapter} class to load the
     * list of {@link SupplierContact} to be displayed
     */
    private static class SupplierContactAdapter extends RecyclerView.Adapter<SupplierContactAdapter.ViewHolder> {

        //Payload constants used to rebind the state of list items for the position stored here
        private static final String PAYLOAD_NEW_DEFAULT_CONTACT = "Payload.NewDefaultContactPosition";
        private static final String PAYLOAD_OLD_DEFAULT_CONTACT = "Payload.OldDefaultContactPosition";
        //Stores the Contact Type of the Supplier Contacts supported by the Adapter
        @SupplierContact.SupplierContactTypeDef
        private final String mContactType;
        //The Data of this Adapter
        private ArrayList<SupplierContact> mSupplierContacts;
        //SparseArray of Contact Values tracked for preventing duplication
        private SparseArray<String> mTrackerContactValuesSparseArray;
        //Stores the current defaulted Supplier contact
        private SupplierContact mDefaultSupplierContact;
        //Stores the EditText View that had last acquired focus
        private View mLastFocusedView;

        /**
         * Constructor of {@link SupplierContactAdapter}
         *
         * @param contactType The Contact Type of the Supplier Contact to be supported by the Adapter.
         *                    Values accepted are as defined in
         *                    {@link com.example.kaushiknsanji.storeapp.data.local.models.SupplierContact.SupplierContactTypeDef}
         */
        SupplierContactAdapter(@SupplierContact.SupplierContactTypeDef String contactType) {
            //Save Contact Type
            mContactType = contactType;
            //Initialize the Supplier Contact List
            mSupplierContacts = new ArrayList<>();
            //Add an Empty Record to the list when there is no record present
            addEmptyRecord();
            //Initialize the duplicates tracker
            mTrackerContactValuesSparseArray = new SparseArray<>();
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
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Inflating the item layout 'R.layout.item_supplier_config_contact'
            //Passing False since we are attaching the layout ourselves
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_supplier_config_contact, parent, false);
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
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            //Get the data at the position
            SupplierContact supplierContact = mSupplierContacts.get(position);

            //Binding the data at the position
            holder.bind(supplierContact);

            //Binding the item state for the position based on the whether
            //the current contact is defaulted or not
            if (supplierContact.isDefault()) {
                //When the contact is defaulted

                //Save the Defaulted Contact data
                mDefaultSupplierContact = supplierContact;
                holder.showContactAsDefault(true, false);
            } else {
                //When the contact is not defaulted
                holder.showContactAsDefault(false, false);
            }
        }

        /**
         * Called by RecyclerView to display the data at the specified position. This method
         * should update the contents of the {@link ViewHolder#itemView} to reflect the item at
         * the given position.
         * <p>
         * Note that unlike {@link ListView}, RecyclerView will not call this method
         * again if the position of the item changes in the data set unless the item itself is
         * invalidated or the new position cannot be determined. For this reason, you should only
         * use the <code>position</code> parameter while acquiring the related data item inside
         * this method and should not keep a copy of it. If you need the position of an item later
         * on (e.g. in a click listener), use {@link ViewHolder#getAdapterPosition()} which will
         * have the updated adapter position.
         * <p>
         * Partial bind vs full bind:
         * <p>
         * The payloads parameter is a merge list from {@link #notifyItemChanged(int, Object)} or
         * {@link #notifyItemRangeChanged(int, int, Object)}.  If the payloads list is not empty,
         * the ViewHolder is currently bound to old data and Adapter may run an efficient partial
         * update using the payload info.  If the payload is empty,  Adapter must run a full bind.
         * Adapter should not assume that the payload passed in notify methods will be received by
         * onBindViewHolder().  For example when the view is not attached to the screen, the
         * payload in notifyItemChange() will be simply dropped.
         *
         * @param holder   The ViewHolder which should be updated to represent the contents of the
         *                 item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         * @param payloads A non-null list of merged payloads. Can be empty list if requires full
         */
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (payloads.isEmpty()) {
                //Propagating to super when there are no payloads
                super.onBindViewHolder(holder, position, payloads);
            } else {
                //When we have a payload for partial update

                //Get the Payload bundle
                Bundle bundle = (Bundle) payloads.get(0);
                //Iterate over the bundle keys
                for (String keyStr : bundle.keySet()) {
                    switch (keyStr) {
                        case PAYLOAD_NEW_DEFAULT_CONTACT:
                            //For the new defaulted contact item position

                            //Get the position from the bundle
                            int newDefaultContactItemPosition = bundle.getInt(keyStr, RecyclerView.NO_POSITION);
                            if (newDefaultContactItemPosition > RecyclerView.NO_POSITION
                                    && newDefaultContactItemPosition == position) {
                                //When the position is for the new defaulted item, show the contact as default
                                holder.showContactAsDefault(true, true);
                            }
                            break;
                        case PAYLOAD_OLD_DEFAULT_CONTACT:
                            //For the old defaulted contact item position

                            //Get the position from the bundle
                            int oldDefaultContactItemPosition = bundle.getInt(keyStr, RecyclerView.NO_POSITION);
                            if (oldDefaultContactItemPosition > RecyclerView.NO_POSITION
                                    && oldDefaultContactItemPosition == position) {
                                //When the position is for the old defaulted item, hide the Default state
                                holder.showContactAsDefault(false, false);
                            }
                            break;
                    }
                }
            }
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return mSupplierContacts.size();
        }

        /**
         * Method that finds the position of the {@code supplierContactToFind}
         * in the Adapter data and returns the same. Item Position here is retrieved only
         * by the comparison of SupplierContact's Value {@link SupplierContact#mValue}.
         *
         * @param supplierContactToFind The {@link SupplierContact} whose item position
         *                              is to retrieved.
         * @return The Item Position of {@link SupplierContact} if found; otherwise {@link RecyclerView#NO_POSITION}
         */
        int getItemPosition(SupplierContact supplierContactToFind) {
            //Initializing the Position value to -1
            int position = RecyclerView.NO_POSITION;

            //Returning -1 when the contact value is null or empty
            if (TextUtils.isEmpty(supplierContactToFind.getValue())) {
                return position;
            }

            if (mSupplierContacts != null && mSupplierContacts.size() > 0) {
                //When the Adapter has data

                //Get the number of items in the list
                int itemCount = getItemCount();

                //Iterating over the list to find #supplierContactToFind
                for (int index = 0; index < itemCount; index++) {
                    //Get the SupplierContact at the index
                    SupplierContact supplierContact = mSupplierContacts.get(index);
                    //Comparing the contact value of the current SupplierContact with that of #supplierContactToFind
                    if (supplierContactToFind.getValue().equals(supplierContact.getValue())) {
                        //When the Contact Value is same

                        //Capture the Item Position and bail out of the loop
                        position = index;
                        break;
                    }
                }
            }

            //Returning the Item Position obtained
            return position;
        }

        /**
         * Method that replaces the Adapter's {@link SupplierContact} list data.
         *
         * @param supplierContacts The new {@link SupplierContact} list data to be displayed.
         */
        void replaceSupplierContactList(ArrayList<SupplierContact> supplierContacts) {
            if (supplierContacts == null || supplierContacts.size() == 0) {
                //Clear the existing list when new list passed is null or empty
                mSupplierContacts.clear();
            } else {
                //When the new list passed is not null and has data

                //Clear the existing list
                mSupplierContacts.clear();
                //Copy all the data to the existing list
                mSupplierContacts.addAll(supplierContacts);
            }

            //Rebuild duplicates tracker SparseArray for the new list of contacts
            rebuildContactTrackers();

            //Clearing the defaulted Contact data saved
            mDefaultSupplierContact = null;

            //Notify that there is a new list of SupplierContacts to be shown
            notifyDataSetChanged();
        }

        /**
         * Method that rebuilds the SparseArray of Contact Values tracked for preventing duplication
         */
        private void rebuildContactTrackers() {
            //Clear the duplicates tracker SparseArray and rebuild for the new list of contacts
            mTrackerContactValuesSparseArray.clear();
            //Retrieving the current Number of Contacts
            int noOfContacts = mSupplierContacts.size();
            //Iterating the list of Contacts and building the duplicates tracker SparseArray
            for (int index = 0; index < noOfContacts; index++) {
                mTrackerContactValuesSparseArray.put(index, mSupplierContacts.get(index).getValue());
            }
        }

        /**
         * Method that appends an empty {@link SupplierContact} to the list of
         * {@link SupplierContact} shown by the Adapter to capture New Contact value
         */
        void addEmptyRecord() {
            //Get the current total number of items
            int currentItemCount = getItemCount();

            //Create an Empty SupplierContact for the Contact Type of the Adapter
            SupplierContact supplierContact = new SupplierContact.Builder()
                    .setType(mContactType)
                    .createSupplierContact();

            if (currentItemCount == 0) {
                //When total number of items is 0, set the Empty SupplierContact to be
                //the default Contact
                supplierContact.setDefault(true);
                //Save the New Defaulted Contact data
                mDefaultSupplierContact = supplierContact;
            }

            //Append the Empty SupplierContact to the list of SupplierContacts
            mSupplierContacts.add(supplierContact);
            //Notify the Item inserted at the position to show the empty record
            notifyItemInserted(currentItemCount);
        }

        /**
         * Method that deletes the {@link SupplierContact} item at the {@code position} passed
         *
         * @param position The Position of the {@link SupplierContact} item to be deleted.
         */
        void deleteRecord(int position) {
            //Validating the item position passed
            if (position > RecyclerView.NO_POSITION) {
                //When the item position passed is valid

                //Remove the SupplierContact at the position specified
                SupplierContact removedSupplierContact = mSupplierContacts.remove(position);
                //Get the Contact Value being removed
                String removedContactValue = removedSupplierContact.getValue();
                //Notify Item position removed
                notifyItemRemoved(position);

                if (removedSupplierContact.isDefault() && mDefaultSupplierContact != null &&
                        removedContactValue.equals(mDefaultSupplierContact.getValue())) {
                    //When the Removed SupplierContact item was the default contact

                    if (getItemCount() > 0) {
                        //When we have data, set the item at position 0 to be the default contact
                        changeDefaultContact(0);
                    }
                }

                //Rebuild duplicates tracker SparseArray if the contact value
                //removed is present in the current tracker list
                String trackerContactValue = mTrackerContactValuesSparseArray.get(position);
                if (!TextUtils.isEmpty(removedContactValue) && !TextUtils.isEmpty(trackerContactValue) && trackerContactValue.equals(removedContactValue)) {
                    rebuildContactTrackers();
                }
            }
        }

        /**
         * Method that updates/changes the contact to be set as default.
         *
         * @param newDefaultContactItemPosition The Position of the {@link SupplierContact} item
         *                                      to be set as the new default contact.
         */
        void changeDefaultContact(int newDefaultContactItemPosition) {
            //Validating the item position passed
            if (newDefaultContactItemPosition > RecyclerView.NO_POSITION) {
                //When the item position passed is valid

                //Get the SupplierContact at the item position passed
                SupplierContact newDefaultSupplierContact = mSupplierContacts.get(newDefaultContactItemPosition);

                if (!TextUtils.isEmpty(newDefaultSupplierContact.getValue())) {
                    //Saves whether this Contact was a previously defaulted contact
                    //(When true, we only need to remove it from default)
                    boolean previouslyDefaulted = newDefaultSupplierContact.isDefault();

                    //Set the Contact to be the new default contact
                    newDefaultSupplierContact.setDefault(true);

                    if (mDefaultSupplierContact != null) {
                        //When we have a saved defaulted contact

                        //Get the position of the old defaulted contact
                        int oldDefaultContactItemPosition = getItemPosition(mDefaultSupplierContact);
                        if (oldDefaultContactItemPosition > RecyclerView.NO_POSITION) {
                            //When the position of the old defaulted contact is valid (still present in adapter data)

                            //Get the SupplierContact of the old defaulted contact
                            SupplierContact oldDefaultSupplierContact = mSupplierContacts.get(oldDefaultContactItemPosition);
                            //Remove the Contact from Default
                            oldDefaultSupplierContact.setDefault(false);

                            //Creating a Bundle to do a partial update, to reset the old default state
                            Bundle payloadBundle = new Bundle(1);
                            //Put the position of the old defaulted contact into the bundle for update
                            payloadBundle.putInt(PAYLOAD_OLD_DEFAULT_CONTACT, oldDefaultContactItemPosition);
                            //Notify the change at the position of the old defaulted contact to reset its state
                            notifyItemChanged(oldDefaultContactItemPosition, payloadBundle);
                        }
                    }

                    if (!previouslyDefaulted) {
                        //When this contact was not previously defaulted, we need to set it as default

                        //Save the New Defaulted Contact data
                        mDefaultSupplierContact = newDefaultSupplierContact;

                        //Creating a Bundle to do a partial update, to show the default state
                        Bundle payloadBundle = new Bundle(1);
                        //Put the position of the new defaulted contact into the bundle for update
                        payloadBundle.putInt(PAYLOAD_NEW_DEFAULT_CONTACT, newDefaultContactItemPosition);
                        //Notify the change at the position of the new defaulted contact to show the default state
                        notifyItemChanged(newDefaultContactItemPosition, payloadBundle);
                    } else {
                        //When this contact was previously defaulted, clear the default contact saved
                        mDefaultSupplierContact = null;
                    }

                }

            }
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
         * @return ArrayList of {@link SupplierContact} data.
         */
        ArrayList<SupplierContact> getSupplierContacts() {
            return mSupplierContacts;
        }

        /**
         * ViewHolder class for caching View components of the template item view
         * 'R.layout.item_supplier_config_contact'
         */
        public class ViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener, View.OnFocusChangeListener {
            //The ImageButton for "Remove" Action
            private ImageButton mImageButtonRemoveAction;
            //The TextInput that displays the Contact Value
            private TextInputLayout mTextInputContact;
            //The EditText that stores/displays the Contact Value
            private TextInputEditText mEditTextContact;
            //The ImageView that shows/alters the state of Contact (defaulted/non-defaulted)
            private ImageView mImageViewContactDefault;
            //The TextView that shows the Text "Default" for the defaulted contact
            private TextView mTextViewContactDefaultLabel;

            /**
             * Constructor of the ViewHolder.
             *
             * @param itemView The inflated item layout View passed
             *                 for caching its View components
             */
            ViewHolder(View itemView) {
                super(itemView);

                //Finding the Views needed
                mImageButtonRemoveAction = itemView.findViewById(R.id.imgbtn_supplier_config_item_contact_remove);
                mTextInputContact = itemView.findViewById(R.id.textinput_supplier_config_item_contact);
                mEditTextContact = itemView.findViewById(R.id.edittext_supplier_config_item_contact);
                mImageViewContactDefault = itemView.findViewById(R.id.image_supplier_config_item_contact_default);
                mTextViewContactDefaultLabel = itemView.findViewById(R.id.text_supplier_config_item_contact_default_label);

                //Setting the Text InputType and Hint for the EditText field that captures the Contact value,
                //based on the kind of Contact (Phone/Email)
                if (mContactType.equals(SupplierContract.SupplierContactType.CONTACT_TYPE_PHONE)) {
                    //For "Phone" Contact Type
                    mEditTextContact.setInputType(InputType.TYPE_CLASS_PHONE);
                    //Setting the Hint Text to show
                    mTextInputContact.setHint(itemView.getContext().getString(R.string.supplier_config_item_contact_hint_phone));
                    //Adding Phone Number Formatting Text Watcher, for auto formatting number during input
                    mEditTextContact.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
                } else if (mContactType.equals(SupplierContract.SupplierContactType.CONTACT_TYPE_EMAIL)) {
                    //For "Email" Contact Type
                    mEditTextContact.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    //Setting the Hint Text to show
                    mTextInputContact.setHint(mContactType);
                    //Setting the Max Length to Email
                    int emailMaxLength = ContactUtility.getEmailMaxLength();
                    mEditTextContact.setFilters(new InputFilter[]{new InputFilter.LengthFilter(emailMaxLength)});
                }

                //Registering click listener on ImageButton (for Removing Contact) and the ImageView (for Defaulting Contact)
                mImageButtonRemoveAction.setOnClickListener(this);
                mImageViewContactDefault.setOnClickListener(this);

                //Registering Focus Change listener on TextInput Field
                mEditTextContact.setOnFocusChangeListener(this);
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
                    switch (view.getId()) {
                        case R.id.imgbtn_supplier_config_item_contact_remove:
                            //For the Remove Action ImageButton

                            //Delete the record at the position clicked
                            deleteRecord(adapterPosition);
                            break;
                        case R.id.image_supplier_config_item_contact_default:
                            //For the Default Action ImageView

                            //Change the Default contact to be the contact at the position clicked
                            changeDefaultContact(adapterPosition);
                            break;
                    }
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
                    SupplierContact supplierContact = mSupplierContacts.get(adapterPosition);

                    if (!hasFocus) {
                        //When the registered View has lost focus

                        //Clear the view reference
                        mLastFocusedView = null;

                        //Take action based on the id of the view which lost focus
                        switch (view.getId()) {
                            case R.id.edittext_supplier_config_item_contact:
                                //For EditText view that displays the contact value

                                //Grab the value from the EditText and update it to the current contact data
                                String contactValue = mEditTextContact.getText().toString().trim();

                                if (!TextUtils.isEmpty(contactValue)) {
                                    //When we have the contact value

                                    //Update it to the current contact data
                                    if (mContactType.equals(SupplierContract.SupplierContactType.CONTACT_TYPE_PHONE)) {
                                        //Retrieving the actual number for the Phone Contact Type
                                        contactValue = ContactUtility.convertAndStripPhoneNumber(contactValue);
                                    }
                                    supplierContact.setValue(contactValue);

                                    if (isContactDuplicate(adapterPosition, contactValue)) {
                                        //If the tracker already has this contact value, then it is a duplicate

                                        //Display the duplicate error on the TextInputLayout
                                        mTextInputContact.setErrorEnabled(true);
                                        mTextInputContact.setError(view.getContext().getString(R.string.supplier_config_item_contact_duplicate_error));
                                        //Invalidate to show the error
                                        mTextInputContact.invalidate();
                                    } else {
                                        //If the tracker does not have this contact value, then it is a new contact

                                        //Checking for contact validity
                                        if (isContactValid(contactValue)) {
                                            //Add to the list of trackers when the contact is valid
                                            mTrackerContactValuesSparseArray.put(adapterPosition, contactValue);
                                        }

                                    }

                                }
                                break;
                        }
                    } else {
                        //When the registered view has gained focus

                        //Save the view reference
                        mLastFocusedView = view;

                        if (mLastFocusedView.getId() == R.id.edittext_supplier_config_item_contact) {
                            //When the focused view is an EditText for Contact Value, clear the error shown if any
                            if (mTextInputContact.isErrorEnabled()) {
                                mTextInputContact.setError(null);
                                mTextInputContact.setErrorEnabled(false);
                            }
                        }

                    }
                }
            }

            /**
             * Method that checks whether the Contact Value entered {@code contactValue} is valid or not.
             *
             * @param contactValue The Contact Value of the Contact to be validated
             * @return Returns <b>TRUE</b> when the {@code contactValue} is valid; <b>FALSE</b> otherwise.
             */
            private boolean isContactValid(String contactValue) {
                //Returning False when the contact value is empty
                if (TextUtils.isEmpty(contactValue)) {
                    return false;
                }

                if (mContactType.equals(SupplierContract.SupplierContactType.CONTACT_TYPE_PHONE)) {
                    //For "Phone" Contact Type
                    if (!ContactUtility.isValidPhoneNumber(contactValue)) {
                        //When the Phone Number is invalid

                        //Display the invalid error on the TextInputLayout
                        mTextInputContact.setErrorEnabled(true);
                        mTextInputContact.setError(itemView.getContext().getString(R.string.supplier_config_phone_number_invalid_error, ContactUtility.getPhoneNumberMinLength(), ContactUtility.getPhoneNumberMaxLength()));
                        //Invalidate to show the error
                        mTextInputContact.invalidate();
                        //Returning False to indicate that the Contact value is invalid
                        return false;
                    }

                } else if (mContactType.equals(SupplierContract.SupplierContactType.CONTACT_TYPE_EMAIL)) {
                    //For "Email" Contact Type
                    if (!ContactUtility.isValidEmail(contactValue)) {
                        //When Email is invalid

                        //Display the invalid error on the TextInputLayout
                        mTextInputContact.setErrorEnabled(true);
                        mTextInputContact.setError(itemView.getContext().getString(R.string.supplier_config_email_invalid_error));
                        //Invalidate to show the error
                        mTextInputContact.invalidate();
                        //Returning False to indicate that the Contact value is invalid
                        return false;
                    }
                }
                //Returning True when the Contact value is valid
                return true;
            }

            /**
             * Method that checks if the contact value {@code contactValue} is duplicate or not.
             *
             * @param position     The integer position of the Item View being checked against.
             * @param contactValue The Contact Value to be validated for its uniqueness.
             * @return <b>TRUE</b> when the {@code contactValue} is duplicated; <b>FALSE</b> otherwise.
             */
            private boolean isContactDuplicate(int position, String contactValue) {
                //Retrieving the number of contact values in the tracker
                int noOfTrackedContacts = mTrackerContactValuesSparseArray.size();
                //Iterating over the SparseArray Tracker to check if the contact value is a duplicate
                for (int index = 0; index < noOfTrackedContacts; index++) {
                    if (mTrackerContactValuesSparseArray.valueAt(index).equals(contactValue)
                            && mTrackerContactValuesSparseArray.keyAt(index) != position) {
                        //Returning true when the Contact value is duplicated
                        //and is not for the same item view position
                        return true;
                    }
                }
                //Returning false when unique
                return false;
            }

            /**
             * Method that binds the data {@code supplierContact} to the Item View.
             *
             * @param supplierContact {@link SupplierContact} data at the position
             *                        to be bounded to the Item View.
             */
            void bind(SupplierContact supplierContact) {
                //Update the EditText to show the value of the Contact
                mEditTextContact.setText(supplierContact.getValue());

                //Ensure that the error on the TextInput is cleared and hidden
                mTextInputContact.setError(null);
                mTextInputContact.setErrorEnabled(false);

                //Check for contact validity to show error if any
                isContactValid(supplierContact.getValue());
            }

            /**
             * Method that updates the state of the record to show the current contact
             * as defaulted when {@code isDefault} is true.
             *
             * @param isDefault Boolean that updates the state of the current record.
             *                  <br/><b>TRUE</b> if the current contact is to be shown as defaulted.
             *                  <br/><b>FALSE</b> otherwise.
             * @param takeFocus Boolean that indicates whether the corresponding EditText field should take focus or not.
             *                  <b>TRUE</b> when the focus is to be given to the EditText; <b>FALSE</b> otherwise.
             */
            void showContactAsDefault(boolean isDefault, boolean takeFocus) {
                if (isDefault) {
                    //When the contact is to be shown as Defaulted

                    //Make the ImageView state as selected
                    mImageViewContactDefault.setSelected(true);
                    //Reveal the "Default" TextView
                    mTextViewContactDefaultLabel.setVisibility(View.VISIBLE);

                    if (takeFocus) {
                        //If focus is granted

                        //Show focus on this EditText so that the ScrollView does not slide away
                        //while changing defaults
                        mEditTextContact.requestFocus();
                    }

                } else {
                    //When the contact is removed from Default state

                    //Remove the ImageView state from selected
                    mImageViewContactDefault.setSelected(false);
                    //Hide the "Default" TextView
                    mTextViewContactDefaultLabel.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    /**
     * {@link ListAdapter} class for RecyclerView to load the list of Products {@link ProductLite}
     * and their Selling Price {@link ProductSupplierInfo} to be displayed.
     */
    private static class SupplierProductsAdapter extends ListAdapter<ProductSupplierInfo, SupplierProductsAdapter.ViewHolder> {
        /**
         * {@link DiffUtil.ItemCallback} for calculating the difference between two
         * {@link ProductSupplierInfo} objects.
         */
        private static DiffUtil.ItemCallback<ProductSupplierInfo> DIFF_PRODUCTS
                = new DiffUtil.ItemCallback<ProductSupplierInfo>() {
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
            public boolean areItemsTheSame(ProductSupplierInfo oldItem, ProductSupplierInfo newItem) {
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
             * This method is called only if {@link #areItemsTheSame(ProductSupplierInfo, ProductSupplierInfo)} returns {@code true} for
             * these items.
             *
             * @param oldItem The item in the old list.
             * @param newItem The item in the new list.
             * @return True if the contents of the items are the same or false if they are different.
             *
             * @see DiffUtil.Callback#areContentsTheSame(int, int)
             */
            @Override
            public boolean areContentsTheSame(ProductSupplierInfo oldItem, ProductSupplierInfo newItem) {
                //Returning the comparison of Unit Price of the Products
                return oldItem.getUnitPrice() == newItem.getUnitPrice();
            }
        };
        //Stores the Typeface used for Product SKU text
        private Typeface mProductSkuTypeface;
        //SparseArray of Products that stores the Product Details of Products
        private SparseArray<ProductLite> mProductLiteSparseArray;
        //The Data of this Adapter that stores the Price information of Products
        private ArrayList<ProductSupplierInfo> mProductSupplierInfoList;
        //Stores the EditText View that had last acquired focus
        private View mLastFocusedView;
        //Stores last removed data if needs to be undone
        private ProductSupplierInfo mLastRemovedProductSupplierInfo;
        private ProductLite mLastRemovedProductLite;
        //Listener for User Actions on Supplier List of Products
        private SupplierProductsUserActionsListener mActionsListener;

        /**
         * Constructor of {@link SupplierProductsAdapter}
         *
         * @param context             Context used for retrieving a Font
         * @param userActionsListener Instance of {@link SupplierProductsUserActionsListener}
         *                            to receive event callbacks for User Actions on Item Views
         */
        SupplierProductsAdapter(Context context, SupplierProductsUserActionsListener userActionsListener) {
            super(DIFF_PRODUCTS);
            //Registering the User Actions Listener
            mActionsListener = userActionsListener;
            //Reading the Typeface for Product SKU
            mProductSkuTypeface = ResourcesCompat.getFont(context, R.font.libre_barcode_128_text_regular);
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
        public SupplierProductsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Inflating the item layout 'R.layout.item_supplier_config_product'
            //Passing False since we are attaching the layout ourselves
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_supplier_config_product, parent, false);
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
        public void onBindViewHolder(@NonNull SupplierProductsAdapter.ViewHolder holder, int position) {
            //Get the data at the position
            ProductSupplierInfo productSupplierInfo = getItem(position);
            //Retrieving the Product Details of the Product at the position
            ProductLite productLite = mProductLiteSparseArray.get(productSupplierInfo.getItemId());
            //Binding the data at the position
            holder.bind(position, productSupplierInfo, productLite);
        }

        /**
         * Method that submits data to the Adapter.
         *
         * @param productSupplierInfoList List of {@link ProductSupplierInfo} objects that stores
         *                                the Price information of the Products, which is the data submitted to this {@link ListAdapter}.
         * @param productLiteSparseArray  {@link SparseArray} of {@link ProductLite} objects that stores the details of the Products.
         */
        void submitData(ArrayList<ProductSupplierInfo> productSupplierInfoList,
                        @Nullable SparseArray<ProductLite> productLiteSparseArray) {
            //Initializing the SparseArray of ProductLite objects
            mProductLiteSparseArray = (productLiteSparseArray == null) ? new SparseArray<>() : productLiteSparseArray;
            //Storing the List of ProductSupplierInfo objects
            mProductSupplierInfoList = productSupplierInfoList;
            //Submitting List of ProductSupplierInfo to the Adapter
            submitList(mProductSupplierInfoList);
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
        public void submitList(List<ProductSupplierInfo> submittedList) {
            //Creating a new list to hold the new data passed
            ArrayList<ProductSupplierInfo> productSupplierInfoList = new ArrayList<>(submittedList);
            //Propagating the new list to super
            super.submitList(productSupplierInfoList);
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
         * Getter Method for the SparseArray of Products sold by the Supplier.
         *
         * @return The SparseArray of Products {@link ProductLite} sold by the Supplier.
         */
        SparseArray<ProductLite> getProductLiteSparseArray() {
            return mProductLiteSparseArray;
        }

        /**
         * Method that returns a List of Products {@link ProductLite} sold by the Supplier.
         *
         * @return The List of Products {@link ProductLite} sold by the Supplier
         */
        ArrayList<ProductLite> getProductLiteList() {
            //Creating a new list to hold the list of Products sold
            ArrayList<ProductLite> productLiteList = new ArrayList<>();
            if (mProductLiteSparseArray != null && mProductLiteSparseArray.size() > 0) {
                //When the Supplier has Products

                //Retrieving the Number of Products sold by the Supplier
                int noOfProducts = mProductLiteSparseArray.size();
                //Iterating over the SparseArray to build the list of Products
                for (int index = 0; index < noOfProducts; index++) {
                    productLiteList.add(mProductLiteSparseArray.valueAt(index));
                }
            }
            //Returning the Products List
            return productLiteList;
        }

        /**
         * Getter Method for the data of this Adapter.
         *
         * @return The List of Products with their Selling Price information {@link ProductSupplierInfo}
         */
        ArrayList<ProductSupplierInfo> getProductSupplierInfoList() {
            return mProductSupplierInfoList;
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
                    ProductSupplierInfo productSupplierInfo = getItem(itemPosition);
                    //Storing the data being removed
                    mLastRemovedProductLite = mProductLiteSparseArray.get(productSupplierInfo.getItemId());
                    mLastRemovedProductSupplierInfo = productSupplierInfo;
                    //Removing the Product from the Product data list and adapter list
                    mProductLiteSparseArray.remove(productSupplierInfo.getItemId());
                    mProductSupplierInfoList.remove(productSupplierInfo);
                    //Submitting the updated List of ProductSupplierInfo to the Adapter
                    submitList(mProductSupplierInfoList);
                    //Propagating the event to the listener
                    mActionsListener.onSwiped(itemPosition, mLastRemovedProductLite);
                }
            });
        }

        /**
         * Method that restores the Last Removed Product from the Adapter data.
         *
         * @return <b>TRUE</b> when Last removed data was present and restored; <b>FALSE</b> otherwise.
         */
        boolean restoreLastRemovedProduct() {
            if (mLastRemovedProductSupplierInfo != null) {
                //When we have the last removed Product data

                //Add to the list of Supplier's Products with the Product data
                mProductSupplierInfoList.add(mLastRemovedProductSupplierInfo);
                mProductLiteSparseArray.put(mLastRemovedProductSupplierInfo.getItemId(), mLastRemovedProductLite);
                //Submitting the updated List of ProductSupplierInfo to the Adapter
                submitList(mProductSupplierInfoList);
                //Returning True when done
                return true;
            }
            //Returning False when we do not have the Last removed Product data
            return false;
        }

        /**
         * Method that triggers notifyItemChanged on the product with id {@code productId}
         * to rebind the product data.
         *
         * @param productId The Integer Id of the Product whose data needs to be rebound.
         */
        void notifyProductChanged(int productId) {
            //Retrieving the current number of Products
            int noOfProducts = mProductSupplierInfoList.size();
            //Iterating over the adapter data to find the position of the item that needs to be rebound
            for (int index = 0; index < noOfProducts; index++) {
                //Retrieving the current ProductSupplierInfo
                ProductSupplierInfo productSupplierInfo = mProductSupplierInfoList.get(index);
                //Checking for the Product Id
                if (productSupplierInfo.getItemId() == productId) {
                    //When the Product Id is same, trigger the notify for this position and bail out
                    notifyItemChanged(index);
                    break;
                }
            }
        }

        /**
         * ViewHolder class for caching View components of the template item view
         * 'R.layout.item_supplier_config_product'
         */
        public class ViewHolder extends RecyclerView.ViewHolder
                implements View.OnFocusChangeListener, View.OnClickListener {
            //The TextView that displays the Product Name
            private TextView mTextViewProductName;
            //The ImageView that displays the Product Photo
            private ImageView mImageViewProductPhoto;
            //The TextView that displays the Product SKU
            private TextView mTextViewProductSku;
            //The TextView that displays the Product Category
            private TextView mTextViewProductCategory;
            //The TextView that displays the Product Price Label
            private TextView mTextViewProductPriceLabel;
            //The EditText that captures the Product Price
            private EditText mEditTextProductPrice;

            /**
             * Constructor of the ViewHolder.
             *
             * @param itemView The inflated item layout View passed
             *                 for caching its View components
             */
            ViewHolder(View itemView) {
                super(itemView);

                //Finding the Views needed
                mTextViewProductName = itemView.findViewById(R.id.text_product_item_name);
                mImageViewProductPhoto = itemView.findViewById(R.id.image_product_item_photo);
                mTextViewProductSku = itemView.findViewById(R.id.text_product_item_sku);
                mTextViewProductCategory = itemView.findViewById(R.id.text_product_item_category);
                mTextViewProductPriceLabel = itemView.findViewById(R.id.text_supplier_config_item_product_price_label);
                mEditTextProductPrice = itemView.findViewById(R.id.edittext_supplier_config_item_product_price);

                //Get the Resources
                Resources resources = itemView.getContext().getResources();

                //Initialize the Product Price Label
                mTextViewProductPriceLabel.setText(resources.getString(R.string.supplier_config_item_product_label_price,
                        Currency.getInstance(Locale.getDefault()).getSymbol()));

                //Set Hint on Product Price EditText
                mEditTextProductPrice.setHint(Currency.getInstance(Locale.getDefault()).getCurrencyCode());

                //Registering Focus Listener on EditText
                mEditTextProductPrice.setOnFocusChangeListener(this);

                //Registering Click Listener on the Item View
                itemView.setOnClickListener(this);
            }

            /**
             * Method that binds the views with the data at the position {@code productLite} and {@code productSupplierInfo}.
             *
             * @param position            The position of the Item in the list
             * @param productSupplierInfo The {@link ProductSupplierInfo} data at the item position
             * @param productLite         The {@link ProductLite} data at the item position
             */
            void bind(int position, ProductSupplierInfo productSupplierInfo, @Nullable ProductLite productLite) {
                if (productLite != null) {
                    //When we have the Product details

                    //Bind the Product Name
                    mTextViewProductName.setText(productLite.getName());
                    //Bind the Product SKU
                    mTextViewProductSku.setText(productLite.getSku());
                    //Set Barcode typeface for the SKU
                    mTextViewProductSku.setTypeface(mProductSkuTypeface);
                    //Download and Bind the Product Photo at the position
                    ImageDownloaderFragment.newInstance(
                            ((FragmentActivity) mImageViewProductPhoto.getContext()).getSupportFragmentManager(), position)
                            .executeAndUpdate(mImageViewProductPhoto, productLite.getDefaultImageUri(), position);
                    //Bind the Product Category
                    mTextViewProductCategory.setText(productLite.getCategory());
                }
                //Bind the Product Price if supplied
                if (productSupplierInfo.getUnitPrice() > 0.0f) {
                    mEditTextProductPrice.setText(String.valueOf(productSupplierInfo.getUnitPrice()));
                } else {
                    //Clear when the Unit Price is 0
                    mEditTextProductPrice.setText("");
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
                    ProductSupplierInfo productSupplierInfo = getItem(adapterPosition);

                    if (!hasFocus) {
                        //When the registered View has lost focus

                        //Clear the view reference
                        mLastFocusedView = null;

                        //Take action based on the id of the view which lost focus
                        switch (view.getId()) {
                            case R.id.edittext_supplier_config_item_product_price:
                                //For EditText View that displays the Product Price

                                //Grab the value from EditText and update it to the current ProductSupplierInfo
                                String priceStr = mEditTextProductPrice.getText().toString().trim();

                                if (!TextUtils.isEmpty(priceStr)) {
                                    //When we have the Product Price value

                                    //Update it to the current ProductSupplierInfo
                                    productSupplierInfo.setUnitPrice(Float.parseFloat(priceStr));
                                }
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
                    ProductSupplierInfo productSupplierInfo = getItem(adapterPosition);
                    //Retrieving the Product Details of the Product at the position
                    ProductLite productLite = mProductLiteSparseArray.get(productSupplierInfo.getItemId());

                    //Get the View Id clicked
                    int clickedViewId = view.getId();

                    //Taking action based on the view clicked
                    if (clickedViewId == itemView.getId()) {
                        //When the entire Item View is clicked

                        //Dispatch the event to the action listener
                        mActionsListener.onEditProduct(adapterPosition, productLite, mImageViewProductPhoto);
                    }
                }
            }
        }

    }

    /**
     * {@link TextWatcher} for the Supplier Code Text field 'R.id.edittext_supplier_config_code'
     */
    private class SupplierCodeTextWatcher implements TextWatcher {
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
            mTextInputSupplierCode.setError(null);
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
     * Listener that implements {@link SupplierProductsUserActionsListener} to receive
     * event callbacks for User actions on RecyclerView list of Supplier Products.
     */
    private class SupplierProductItemUserActionsListener implements SupplierProductsUserActionsListener {

        /**
         * Callback Method of {@link SupplierProductsUserActionsListener} invoked when
         * the user clicks on "Edit" button or the Item View itself. This should
         * launch the {@link ProductConfigActivity}
         * for the Product to be edited.
         *
         * @param itemPosition          The adapter position of the Item clicked.
         * @param product               The {@link ProductLite} associated with the Item clicked.
         * @param imageViewProductPhoto The ImageView of the Adapter Item that displays the Image
         */
        @Override
        public void onEditProduct(int itemPosition, ProductLite product, ImageView imageViewProductPhoto) {
            //Creating ActivityOptions for Shared Element Transition
            //where the ImageView is the Shared Element
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    requireActivity(),
                    imageViewProductPhoto,
                    TextUtils.isEmpty(product.getDefaultImageUri()) ? getString(R.string.transition_name_product_photo) : product.getDefaultImageUri()
            );
            //Delegating to the Presenter to handle the event
            mPresenter.editProduct(product.getId(), activityOptionsCompat);
        }

        /**
         * Callback Method of {@link SupplierProductsUserActionsListener} invoked when
         * the user swipes the Item View to remove the Supplier-Product link.
         *
         * @param itemPosition The adapter position of the Item swiped.
         * @param product      The {@link ProductLite} associated with the Item swiped.
         */
        @Override
        public void onSwiped(int itemPosition, ProductLite product) {
            //Delegating to the Presenter to handle the event
            mPresenter.onSupplierProductSwiped(product.getSku());
        }

    }
}
