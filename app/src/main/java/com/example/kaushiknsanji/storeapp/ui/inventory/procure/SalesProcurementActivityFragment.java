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

package com.example.kaushiknsanji.storeapp.ui.inventory.procure;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.local.models.SupplierContact;
import com.example.kaushiknsanji.storeapp.ui.BasePresenter;
import com.example.kaushiknsanji.storeapp.ui.BaseView;
import com.example.kaushiknsanji.storeapp.ui.common.ListItemSpacingDecoration;
import com.example.kaushiknsanji.storeapp.ui.common.ProgressDialogFragment;
import com.example.kaushiknsanji.storeapp.utils.IntentUtility;
import com.example.kaushiknsanji.storeapp.utils.SnackbarUtility;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Content Fragment of {@link SalesProcurementActivity} that inflates the layout 'R.layout.fragment_sales_procurement'
 * to display the availability of the Product at the Supplier and Supplier's Contacts for placing procurement request.
 * This implements the {@link SalesProcurementContract.View} on the lines of Model-View-Presenter architecture.
 *
 * @author Kaushik N Sanji
 */
public class SalesProcurementActivityFragment extends Fragment implements SalesProcurementContract.View, View.OnClickListener {

    //Constant used for logs
    private static final String LOG_TAG = SalesProcurementActivityFragment.class.getSimpleName();

    //Bundle constants for persisting the data throughout System config changes
    private static final String BUNDLE_SUPPLIER_CONTACTS_LIST_KEY = "SalesProcurement.SupplierContacts";
    private static final String BUNDLE_CONTACTS_RESTORED_BOOL_KEY = "SalesProcurement.AreContactsRestored";

    //The Presenter for this View
    private SalesProcurementContract.Presenter mPresenter;

    //Stores the instance of the View components required
    private TextView mTextViewProductSupplierTitle;
    private TextView mTextViewProductSupplierAvailabilityTotal;
    private EditText mEditTextProductSupplierReqdQty;
    private RecyclerView mRecyclerViewPhoneContacts;
    private CardView mCardViewProcurementViaPhoneContacts;
    private CardView mCardViewProcurementViaEmailContacts;
    private CardView mCardViewProcurementNoContacts;

    //RecyclerView Adapter for Supplier's Phone Contacts
    private SupplierPhoneContactsAdapter mPhoneContactsAdapter;

    //Stores the List of Supplier Contacts of Phone Contact Type
    private ArrayList<SupplierContact> mPhoneContacts;
    //Stores the List of Supplier Contacts of Email Contact Type
    private ArrayList<SupplierContact> mEmailContacts;

    //Stores the state of Supplier Contacts restored,
    //to prevent updating the fields every time during System config change
    private boolean mAreSupplierContactsRestored;

    /**
     * Mandatory Empty Constructor of {@link SalesProcurementActivityFragment}.
     * This is required by the {@link android.support.v4.app.FragmentManager} to instantiate
     * the fragment (e.g. upon screen orientation changes).
     */
    public SalesProcurementActivityFragment() {
    }

    /**
     * Static Factory Constructor that creates an instance of {@link SalesProcurementActivityFragment}
     *
     * @return Instance of {@link SalesProcurementActivityFragment}
     */
    public static SalesProcurementActivityFragment newInstance() {
        //Instantiating and Returning the fragment instance
        return new SalesProcurementActivityFragment();
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
     * @return Returns the View for the fragment's UI ('R.layout.fragment_sales_procurement')
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout 'R.layout.fragment_sales_procurement' for this fragment
        //Passing false as we are attaching the layout ourselves
        View rootView = inflater.inflate(R.layout.fragment_sales_procurement, container, false);

        //Finding the views to initialize
        mTextViewProductSupplierTitle = rootView.findViewById(R.id.text_sales_procurement_product_supplier_title);
        mTextViewProductSupplierAvailabilityTotal = rootView.findViewById(R.id.text_sales_procurement_supplier_availability_total);
        mEditTextProductSupplierReqdQty = rootView.findViewById(R.id.edittext_sales_procurement_reqd_product_qty);
        mRecyclerViewPhoneContacts = rootView.findViewById(R.id.recyclerview_sales_procurement_phone_list);
        mCardViewProcurementNoContacts = rootView.findViewById(R.id.card_sales_procurement_no_contacts);
        mCardViewProcurementViaEmailContacts = rootView.findViewById(R.id.card_sales_procurement_email);
        mCardViewProcurementViaPhoneContacts = rootView.findViewById(R.id.card_sales_procurement_phone);

        //Registering Click listener on "Send Mail" Image Button
        rootView.findViewById(R.id.imgbtn_sales_procurement_send_mail).setOnClickListener(this);

        //Initialize RecyclerView for Supplier's Phone Contacts
        setupSupplierPhoneContactsRecyclerView();

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

        //Start the work
        mPresenter.start();
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

            //Restoring the Supplier's Contacts
            mPresenter.updateSupplierContacts(savedInstanceState.getParcelableArrayList(BUNDLE_SUPPLIER_CONTACTS_LIST_KEY));
            //Restoring the state of Supplier's Contacts restored
            mPresenter.updateAndSyncContactsState(savedInstanceState.getBoolean(BUNDLE_CONTACTS_RESTORED_BOOL_KEY, false));
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

        //Saving the state
        //Saving all the Contacts
        ArrayList<SupplierContact> supplierContactsList = new ArrayList<>();
        supplierContactsList.addAll(mPhoneContacts);
        supplierContactsList.addAll(mEmailContacts);
        outState.putParcelableArrayList(BUNDLE_SUPPLIER_CONTACTS_LIST_KEY, supplierContactsList);

        //Saving the Boolean state of Contacts download
        outState.putBoolean(BUNDLE_CONTACTS_RESTORED_BOOL_KEY, mAreSupplierContactsRestored);
    }

    /**
     * Method that registers the Presenter {@code presenter} with the View implementing {@link BaseView}
     *
     * @param presenter Presenter instance implementing the {@link BasePresenter}
     */
    @Override
    public void setPresenter(SalesProcurementContract.Presenter presenter) {
        mPresenter = presenter;
    }

    /**
     * Method that initializes the RecyclerView 'R.id.recyclerview_sales_procurement_phone_list' and its Adapter.
     */
    private void setupSupplierPhoneContactsRecyclerView() {
        //Creating a Vertical Linear Layout Manager with default layout order
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false);

        //Setting the Layout Manager to use
        mRecyclerViewPhoneContacts.setLayoutManager(linearLayoutManager);

        //Initializing the Adapter
        mPhoneContactsAdapter = new SupplierPhoneContactsAdapter(new SupplierPhoneListItemUserActionsListener());

        //Setting the Adapter on RecyclerView
        mRecyclerViewPhoneContacts.setAdapter(mPhoneContactsAdapter);

        //Retrieving the Item spacing to use
        int itemSpacing = getResources().getDimensionPixelSize(R.dimen.sales_procurement_item_phone_vertical_spacing);

        //Setting Item offsets using Item Decoration
        mRecyclerViewPhoneContacts.addItemDecoration(new ListItemSpacingDecoration(
                itemSpacing, itemSpacing
        ));
    }

    /**
     * Method invoked to keep the state of "Supplier's Contacts restored", in sync with the Presenter.
     *
     * @param areSupplierContactsRestored Boolean that indicates the state of Supplier's Contacts restored.
     *                                    <b>TRUE</b> if it had been restored; <b>FALSE</b> otherwise.
     */
    @Override
    public void syncContactsState(boolean areSupplierContactsRestored) {
        //Saving the state
        mAreSupplierContactsRestored = areSupplierContactsRestored;
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
     * Method that updates the Supplier's Phone Contacts {@code phoneContacts} to the View
     *
     * @param phoneContacts List of {@link SupplierContact} of Phone Contact Type, of the Supplier
     */
    @Override
    public void updatePhoneContacts(ArrayList<SupplierContact> phoneContacts) {
        //Saving the Phone Contacts
        mPhoneContacts = phoneContacts;
        //Submitting data to the Adapter
        mPhoneContactsAdapter.submitList(mPhoneContacts);
    }

    /**
     * Method invoked to hide the "Procure via Phone Call" View when there are No Phone Contacts to show
     */
    @Override
    public void hidePhoneContacts() {
        //Hiding the CardView that displays the "Procure via Phone Call"
        mCardViewProcurementViaPhoneContacts.setVisibility(View.GONE);
    }

    /**
     * Method that updates the Supplier's Email Contacts {@code emailContacts} to the View
     *
     * @param emailContacts List of {@link SupplierContact} of Email Contact Type, of the Supplier
     */
    @Override
    public void updateEmailContacts(ArrayList<SupplierContact> emailContacts) {
        //Saving the Email Contacts
        mEmailContacts = emailContacts;
    }

    /**
     * Method invoked to hide the "Procure via Email" View when there are No Email Contacts available
     */
    @Override
    public void hideEmailContacts() {
        //Hiding the CardView that displays the "Procure via Email"
        mCardViewProcurementViaEmailContacts.setVisibility(View.GONE);
    }

    /**
     * Method invoked to display a TextView with a message when there are No contacts available
     * for procuring the Product from the Supplier
     */
    @Override
    public void showEmptyContactsView() {
        //Displaying the CardView with a preset message for "No contacts available for Product Procurement!"
        mCardViewProcurementNoContacts.setVisibility(View.VISIBLE);
    }

    /**
     * Method that updates the Product Supplier Title to the View.
     *
     * @param titleResId   The String resource of the Title to be used
     * @param productName  The Name of the Product being procured
     * @param productSku   The SKU of the Product
     * @param supplierName The Name of the Supplier being procured from
     * @param supplierCode The Code of the Supplier
     */
    @Override
    public void updateProductSupplierTitle(int titleResId, String productName, String productSku, String supplierName, String supplierCode) {
        //Set the Title
        mTextViewProductSupplierTitle.setText(getString(titleResId, productName, productSku, supplierName, supplierCode));
    }

    /**
     * Method that updates the quantity available to Sell at the Supplier to the View
     *
     * @param availableQuantity Integer value of the available quantity of the Product at the Supplier
     */
    @Override
    public void updateProductSupplierAvailability(int availableQuantity) {
        //Set the available quantity value
        mTextViewProductSupplierAvailabilityTotal.setText(String.valueOf(availableQuantity));
        //Set the Text Color
        mTextViewProductSupplierAvailabilityTotal.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
    }

    /**
     * Method invoked to show the "Out Of Stock!" alert when the quantity available to Sell
     * for the Product at the Supplier is 0.
     */
    @Override
    public void showOutOfStockAlert() {
        //Set the Out of Stock message
        mTextViewProductSupplierAvailabilityTotal.setText(getString(R.string.sales_list_item_out_of_stock));
        //Set the Text Color
        mTextViewProductSupplierAvailabilityTotal.setTextColor(ContextCompat.getColor(requireContext(), R.color.salesListItemOutOfStockColor));
    }

    /**
     * Method invoked when the user clicks on any of the Phone Contacts shown. This should launch the Phone
     * dialer passing in the phone number {@code phoneNumber}
     *
     * @param phoneNumber The Phone Number to dial.
     */
    @Override
    public void dialPhoneNumber(String phoneNumber) {
        //Launching the Dialer passing in the Phone Number
        IntentUtility.dialPhoneNumber(requireActivity(), phoneNumber);
    }

    /**
     * Method invoked when the user clicks on the "Send Mail" Image Button. This should launch the Email
     * Activity passing in the data for Email address, subject and body of the Email.
     *
     * @param toEmailAddress The "TO" Address to send the email to.
     * @param ccAddresses    Array of "CC" Addresses to include.
     * @param subjectResId   The String resource for the Subject of the Email.
     * @param subjectArgs    The Arguments of the Subject String Resource.
     * @param bodyResId      The String resource for the Body of the Email.
     * @param bodyArgs       The Arguments of the Body String Resource.
     */
    @Override
    public void composeEmail(String toEmailAddress, String[] ccAddresses, int subjectResId,
                             Object[] subjectArgs, int bodyResId, Object[] bodyArgs) {
        //Launching the Email Activity with the details passed
        IntentUtility.composeEmail(
                requireActivity(),
                new String[]{toEmailAddress},
                ccAddresses,
                getString(subjectResId, subjectArgs),
                getString(bodyResId, bodyArgs)
        );
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
            case R.id.imgbtn_sales_procurement_send_mail:
                //For the "Send Mail" Image Button

                //Delegating to the Presenter, to dispatch an Email to the Supplier
                //for Procuring more quantity of the Product
                mPresenter.sendMailClicked(mEditTextProductSupplierReqdQty.getText().toString(), mEmailContacts);
                break;
        }
    }

    /**
     * {@link ListAdapter} class for the RecyclerView to load the list of Supplier's Phone Contacts
     * {@link SupplierContact} to be displayed.
     */
    private static class SupplierPhoneContactsAdapter extends ListAdapter<SupplierContact, SupplierPhoneContactsAdapter.ViewHolder> {

        /**
         * {@link DiffUtil.ItemCallback} for calculating the difference between two
         * {@link SupplierContact} objects.
         */
        private static DiffUtil.ItemCallback<SupplierContact> DIFF_CONTACTS
                = new DiffUtil.ItemCallback<SupplierContact>() {
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
            public boolean areItemsTheSame(SupplierContact oldItem, SupplierContact newItem) {
                //Returning the comparison of SupplierContact's isDefault
                return oldItem.isDefault() == newItem.isDefault();
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
             * This method is called only if {@link #areItemsTheSame(SupplierContact, SupplierContact)} returns {@code true} for
             * these items.
             *
             * @param oldItem The item in the old list.
             * @param newItem The item in the new list.
             * @return True if the contents of the items are the same or false if they are different.
             *
             * @see DiffUtil.Callback#areContentsTheSame(int, int)
             */
            @Override
            public boolean areContentsTheSame(SupplierContact oldItem, SupplierContact newItem) {
                //Returning the comparison of SupplierContact's value
                return oldItem.getValue().equals(newItem.getValue());
            }
        };
        //Listener for User Actions on Supplier's list of Phone Contacts
        private SupplierPhoneListUserActionsListener mActionsListener;

        /**
         * Constructor of {@link SupplierPhoneContactsAdapter}
         *
         * @param userActionsListener Instance of {@link SupplierPhoneListUserActionsListener}
         *                            to receive event callbacks for User Actions on Item Views
         */
        SupplierPhoneContactsAdapter(SupplierPhoneListUserActionsListener userActionsListener) {
            super(DIFF_CONTACTS);
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
        public SupplierPhoneContactsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Inflating the item layout 'R.layout.item_sales_procurement_phone_contact'
            //Passing False since we are attaching the layout ourselves
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sales_procurement_phone_contact, parent, false);
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
        public void onBindViewHolder(@NonNull SupplierPhoneContactsAdapter.ViewHolder holder, int position) {
            //Get the data at the position
            SupplierContact supplierContact = getItem(position);
            //Binding the data at the position
            holder.bind(supplierContact);
        }

        /**
         * ViewHolder class for caching View components of the template item view
         * 'R.layout.item_sales_procurement_phone_contact'
         */
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            //The TextView that displays the Phone Number
            private TextView mTextViewPhone;
            //The ImageView that shows whether the Phone Number
            //is the defaulted contact for the Supplier or not
            private ImageView mImageViewContactDefault;

            /**
             * Constructor of the ViewHolder.
             *
             * @param itemView The inflated item layout View passed
             *                 for caching its View components
             */
            ViewHolder(View itemView) {
                super(itemView);

                //Finding the Views needed
                mTextViewPhone = itemView.findViewById(R.id.text_sales_procurement_item_phone);
                mImageViewContactDefault = itemView.findViewById(R.id.image_sales_procurement_item_contact_default);

                //Registering Click Listener on the Item View
                itemView.setOnClickListener(this);
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
                    SupplierContact supplierContact = getItem(adapterPosition);

                    if (view.getId() == itemView.getId()) {
                        //When the Item View is clicked, pass the event to the listener
                        mActionsListener.onPhoneClicked(adapterPosition, supplierContact);
                    }
                }
            }

            /**
             * Method that binds the views with the data at the position {@link SupplierContact}
             *
             * @param supplierContact The data {@link SupplierContact} at the item position.
             */
            void bind(SupplierContact supplierContact) {
                if (supplierContact != null) {
                    //When we have the details

                    //Bind the Phone Number
                    String phoneNumber = supplierContact.getValue();
                    //Setting the Phone Number format based on the Build version
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        //For Lollipop and above
                        mTextViewPhone.setText(PhoneNumberUtils.formatNumber(phoneNumber, Locale.getDefault().getCountry()));
                    } else {
                        //For below Lollipop
                        mTextViewPhone.setText(PhoneNumberUtils.formatNumber(phoneNumber));
                    }

                    //Set the Visibility of Defaulted Contact ImageView based on whether
                    //the contact is a default contact or not
                    if (supplierContact.isDefault()) {
                        //ImageView is shown when the contact is defaulted
                        mImageViewContactDefault.setVisibility(View.VISIBLE);
                    } else {
                        //ImageView is invisible when the contact is not defaulted
                        mImageViewContactDefault.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }
    }

    /**
     * Listener that implements {@link SupplierPhoneListUserActionsListener} to receive
     * event callbacks for User actions RecyclerView List of Phone Contacts of the Supplier.
     */
    private class SupplierPhoneListItemUserActionsListener implements SupplierPhoneListUserActionsListener {

        /**
         * Callback Method of {@link SupplierPhoneListUserActionsListener} invoked when
         * the user clicks on any of the Phone Contacts shown. This should launch an Intent
         * to start the Phone Activity passing in the number {@link SupplierContact#mValue}
         *
         * @param itemPosition    The adapter position of the Item View clicked
         * @param supplierContact The {@link SupplierContact} associated with the Item View clicked.
         */
        @Override
        public void onPhoneClicked(int itemPosition, SupplierContact supplierContact) {
            //Delegating to the Presenter to handle the event
            mPresenter.phoneClicked(supplierContact);
        }
    }
}
