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

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.constraint.Group;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.local.models.SupplierLite;
import com.example.kaushiknsanji.storeapp.ui.common.ListItemSpacingDecoration;
import com.example.kaushiknsanji.storeapp.ui.suppliers.config.SupplierConfigActivity;
import com.example.kaushiknsanji.storeapp.utils.ColorUtility;
import com.example.kaushiknsanji.storeapp.utils.IntentUtility;
import com.example.kaushiknsanji.storeapp.utils.SnackbarUtility;

import java.util.ArrayList;
import java.util.Locale;

/**
 * {@link com.example.kaushiknsanji.storeapp.ui.MainActivity}'s ViewPager Fragment that inflates
 * the layout 'R.layout.layout_main_content_page' to display the list of Suppliers configured
 * in the database. This implements the {@link SupplierListContract.View} on the lines of
 * Model-View-Presenter architecture.
 *
 * @author Kaushik N Sanji
 */
public class SupplierListFragment extends Fragment
        implements SupplierListContract.View, SwipeRefreshLayout.OnRefreshListener {

    //Constant used for logs
    private static final String LOG_TAG = SupplierListFragment.class.getSimpleName();

    //The Presenter interface for this View
    private SupplierListContract.Presenter mPresenter;

    //References to the Views shown in this Fragment
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerViewContentList;
    private Group mGroupEmptyList;

    //Adapter of the RecyclerView
    private SupplierListAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SupplierListFragment() {
    }

    /**
     * Static Factory constructor that creates an instance of {@link SupplierListFragment}
     *
     * @return Instance of {@link SupplierListFragment}
     */
    public static SupplierListFragment newInstance() {
        return new SupplierListFragment();
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
     * @return Returns the View for the fragment's UI ('R.layout.layout_main_content_page')
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflating the layout 'R.layout.layout_main_content_page'
        //Passing false as we are attaching the layout ourselves
        View rootView = inflater.inflate(R.layout.layout_main_content_page, container, false);

        //Finding the Views
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_content_page);
        mRecyclerViewContentList = rootView.findViewById(R.id.recyclerview_content_page);
        TextView textViewEmptyList = rootView.findViewById(R.id.text_content_page_empty_list);
        ImageView imageViewStepNumber = rootView.findViewById(R.id.image_content_page_step_number);
        mGroupEmptyList = rootView.findViewById(R.id.group_content_page_empty);

        //Initialize the ImageView with the proper step number drawable
        imageViewStepNumber.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_main_supplier_page_number));

        //Initialize the Empty TextView with Text
        textViewEmptyList.setText(getString(R.string.supplier_list_empty_text));

        //Initialize SwipeRefreshLayout
        setupSwipeRefresh();

        //Initialize RecyclerView
        setupRecyclerView();

        //Returning the prepared layout
        return rootView;
    }

    /**
     * Method that initializes the SwipeRefreshLayout 'R.id.swipe_refresh_content_page'
     * and its listener
     */
    private void setupSwipeRefresh() {
        //Registering the refresh listener which triggers a new load on swipe to refresh
        mSwipeRefreshLayout.setOnRefreshListener(this);
        //Configuring the Colors for Swipe Refresh Progress Indicator
        mSwipeRefreshLayout.setColorSchemeColors(ColorUtility.obtainColorsFromTypedArray(requireContext(), R.array.swipe_refresh_colors, R.color.colorPrimary));
    }

    /**
     * Method that initializes a RecyclerView with its Adapter for loading and displaying the list of Suppliers.
     */
    private void setupRecyclerView() {
        //Creating a Vertical Linear Layout Manager with the default layout order
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false) {
            /**
             * Called when items have been added to the adapter. The LayoutManager may choose to
             * requestLayout if the inserted items would require refreshing the currently visible set
             * of child views. (e.g. currently empty space would be filled by appended items, etc.)
             *
             * @param recyclerView The {@link RecyclerView} this LayoutManager is bound to.
             * @param positionStart The Start position from where the Items were added to the {@link RecyclerView}
             * @param itemCount Number of Items added
             */
            @Override
            public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
                if (getChildCount() > 0 && itemCount == 1) {
                    //When there are some items visible and number of items added is 1

                    if (positionStart == getItemCount() - 1 && getItemCount() > 1) {
                        //When there are more than one Item View and the Item View
                        //added is in the last position

                        //Remove the previous Item View cache from RecyclerView to reload the Item View
                        //with proper item decoration height
                        removeView(findViewByPosition(positionStart - 1));
                    }
                }
            }

            /**
             * Called when items have been removed from the adapter.
             *
             * @param recyclerView The {@link RecyclerView} this LayoutManager is bound to.
             * @param positionStart The Start position from where the Items were removed from the {@link RecyclerView}
             * @param itemCount Number of Items removed
             */
            @Override
            public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
                if (getChildCount() > 0 && itemCount == 1) {
                    //When there are some items visible and number of items added is 1

                    if (positionStart == getItemCount() && getItemCount() > 1) {
                        //When there are more than one Item View and the Item View
                        //removed is from the last position

                        //Remove the previous Item View cache from RecyclerView to reload the Item View
                        //with proper item decoration height
                        removeView(findViewByPosition(positionStart - 1));
                    }
                }

            }
        };

        //Setting the Layout Manager to use
        mRecyclerViewContentList.setLayoutManager(linearLayoutManager);

        //Initializing the Adapter for the RecyclerView
        mAdapter = new SupplierListAdapter(new UserActionsListener());

        //Setting the Adapter on the RecyclerView
        mRecyclerViewContentList.setAdapter(mAdapter);

        //Retrieving the Item spacing to use
        int itemSpacing = getResources().getDimensionPixelSize(R.dimen.supplier_list_items_spacing);

        //Setting Item offsets using Item Decoration
        mRecyclerViewContentList.addItemDecoration(new ListItemSpacingDecoration(
                itemSpacing, itemSpacing, true
        ));

    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to {@link Activity#onResume() Activity.onResume} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onResume() {
        super.onResume();

        //Start loading the Suppliers
        mPresenter.start();
    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        //Dispatching the event to the Presenter to invalidate any critical resources
        mPresenter.releaseResources();
    }

    /**
     * Method that returns the registered Presenter for this View.
     *
     * @return The registered Presenter for this View. Can be {@code null}
     */
    @Nullable
    @Override
    public SupplierListContract.Presenter getPresenter() {
        return mPresenter;
    }

    /**
     * Method that registers the Presenter {@code presenter} with the View implementing {@link com.example.kaushiknsanji.storeapp.ui.BaseView}
     *
     * @param presenter Presenter instance implementing the {@link com.example.kaushiknsanji.storeapp.ui.BasePresenter}
     */
    @Override
    public void setPresenter(SupplierListContract.Presenter presenter) {
        mPresenter = presenter;
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        //Forcefully start a new load
        mPresenter.triggerSuppliersLoad(true);
    }

    /**
     * Method that displays the Progress indicator
     */
    @Override
    public void showProgressIndicator() {
        //Enabling the Swipe to Refresh if disabled prior to showing the Progress indicator
        if (!mSwipeRefreshLayout.isEnabled()) {
            mSwipeRefreshLayout.setEnabled(true);
        }
        //Displaying the Progress Indicator only when not already shown
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }

    /**
     * Method that hides the Progress indicator
     */
    @Override
    public void hideProgressIndicator() {
        //Hiding the Progress indicator
        mSwipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Method invoked when an error is encountered during Supplier information
     * retrieval or delete process.
     *
     * @param messageId String Resource of the error Message to be displayed
     * @param args      Variable number of arguments to replace the format specifiers
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
     * Method invoked when the Supplier List is empty. This should show a TextView with a
     * Text that suggests Users to add Suppliers into the database.
     */
    @Override
    public void showEmptyView() {
        //Hiding the RecyclerView
        mRecyclerViewContentList.setVisibility(View.INVISIBLE);
        //Displaying the Empty List TextView and Step Number Drawable
        mGroupEmptyList.setVisibility(View.VISIBLE);
        //Disabling the Swipe to Refresh
        mSwipeRefreshLayout.setEnabled(false);
    }

    /**
     * Method invoked when we have the Supplier List. This should show the Supplier List and
     * hide the Empty List TextView and Step Number Drawable.
     */
    @Override
    public void hideEmptyView() {
        //Displaying the RecyclerView
        mRecyclerViewContentList.setVisibility(View.VISIBLE);
        //Hiding the Empty List TextView and Step Number Drawable
        mGroupEmptyList.setVisibility(View.GONE);
    }

    /**
     * Method that updates the RecyclerView's Adapter with new {@code supplierList} data.
     *
     * @param supplierList List of Suppliers defined by {@link SupplierLite}
     *                     loaded from the database.
     */
    @Override
    public void loadSuppliers(ArrayList<SupplierLite> supplierList) {
        //Submitting the new updated list to the Adapter
        mAdapter.submitList(supplierList);
    }

    /**
     * Method invoked when the user clicks on the FAB button to add a New Supplier
     * into the database. This should
     * launch the {@link SupplierConfigActivity}
     * for configuring a New Supplier.
     */
    @Override
    public void launchAddNewSupplier() {
        //Creating the Intent to launch SupplierConfigActivity
        Intent supplierConfigIntent = new Intent(requireContext(), SupplierConfigActivity.class);
        //Starting the Activity with Result
        startActivityForResult(supplierConfigIntent, SupplierConfigActivity.REQUEST_ADD_SUPPLIER);
    }

    /**
     * Method invoked when the user clicks on "Edit" button or the Item View itself. This should
     * launch the {@link SupplierConfigActivity}
     * for the Supplier to be edited.
     *
     * @param supplierId The Primary key of the Supplier to be edited.
     */
    @Override
    public void launchEditSupplier(int supplierId) {
        //Creating the Intent to launch SupplierConfigActivity
        Intent supplierConfigIntent = new Intent(requireContext(), SupplierConfigActivity.class);
        //Passing in the Supplier ID of the Supplier to be edited
        supplierConfigIntent.putExtra(SupplierConfigActivity.EXTRA_SUPPLIER_ID, supplierId);
        //Starting the Activity with Result
        startActivityForResult(supplierConfigIntent, SupplierConfigActivity.REQUEST_EDIT_SUPPLIER);
    }

    /**
     * Method that displays a message on Success of adding a New Supplier.
     *
     * @param supplierCode String containing the code of the Supplier that was added successfully.
     */
    @Override
    public void showAddSuccess(String supplierCode) {
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.supplier_list_item_add_success, supplierCode), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Method that displays a message on Success of updating an Existing Supplier.
     *
     * @param supplierCode String containing the code of the Supplier that was updated successfully.
     */
    @Override
    public void showUpdateSuccess(String supplierCode) {
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
    public void showDeleteSuccess(String supplierCode) {
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.supplier_list_item_delete_success, supplierCode), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Method invoked when the user clicks on the Phone button. This should launch the Phone
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
     * Method invoked when the user clicks on the Email button. This should launch an Email
     * activity passing in the "TO" Address {@code toEmailAddress}
     *
     * @param toEmailAddress The "TO" Address to send an email to.
     */
    @Override
    public void composeEmail(String toEmailAddress) {
        //Launching an Email Activity passing in the Email Address
        IntentUtility.composeEmail(
                requireActivity(),
                new String[]{toEmailAddress},
                null,
                null,
                null
        );
    }

    /**
     * Receive the result from a previous call to
     * {@link #startActivityForResult(Intent, int)}.  This follows the
     * related Activity API as described there in
     * {@link android.support.v4.app.FragmentActivity#onActivityResult(int, int, Intent)}.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Delegating to the Presenter to handle
        mPresenter.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * {@link ListAdapter} class for RecyclerView to load the list of Suppliers to be displayed.
     */
    private static class SupplierListAdapter extends ListAdapter<SupplierLite, SupplierListAdapter.ViewHolder> {

        /**
         * {@link DiffUtil.ItemCallback} for calculating the difference between two {@link SupplierLite} objects
         */
        private static DiffUtil.ItemCallback<SupplierLite> DIFF_SUPPLIERS
                = new DiffUtil.ItemCallback<SupplierLite>() {
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
            public boolean areItemsTheSame(SupplierLite oldItem, SupplierLite newItem) {
                //Returning the comparison of the Supplier's Id
                return oldItem.getId() == newItem.getId();
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
             * This method is called only if {@link #areItemsTheSame(SupplierLite, SupplierLite)} returns {@code true} for
             * these items.
             *
             * @param oldItem The item in the old list.
             * @param newItem The item in the new list.
             * @return True if the contents of the items are the same or false if they are different.
             *
             * @see DiffUtil.Callback#areContentsTheSame(int, int)
             */
            @Override
            public boolean areContentsTheSame(SupplierLite oldItem, SupplierLite newItem) {
                //Returning the comparison of entire Supplier
                return oldItem.equals(newItem);
            }
        };

        //Listener for the User actions on the Supplier List Items
        private SupplierListUserActionsListener mActionsListener;

        /**
         * Constructor of {@link SupplierListAdapter}
         *
         * @param userActionsListener Instance of {@link SupplierListUserActionsListener}
         *                            to receive event callbacks for User Actions on Item Views
         */
        SupplierListAdapter(SupplierListUserActionsListener userActionsListener) {
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
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Inflating the item layout 'R.layout.item_supplier_list'
            //Passing False since we are attaching the layout ourselves
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_supplier_list, parent, false);
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
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            //Get the data at the position
            SupplierLite supplierLite = getItem(position);

            //Bind the Views with the data at the position
            holder.bind(supplierLite);
        }

        /**
         * ViewHolder class for caching View components of the template item view 'R.layout.item_supplier_list'
         */
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            //References to the Views required, in the Item View
            private TextView mTextViewSupplierName;
            private TextView mTextViewSupplierCode;
            private TextView mTextViewSupplierItemCount;
            private Button mButtonDefaultPhone;
            private Button mButtonDefaultEmail;
            private Button mButtonDelete;
            private Button mButtonEdit;

            /**
             * Constructor of {@link ViewHolder}
             *
             * @param itemView Inflated Instance of the Item View 'R.layout.item_supplier_list'
             */
            ViewHolder(View itemView) {
                super(itemView);

                //Finding the Views needed
                mTextViewSupplierName = itemView.findViewById(R.id.text_supplier_list_item_name);
                mTextViewSupplierCode = itemView.findViewById(R.id.text_supplier_list_item_code);
                mTextViewSupplierItemCount = itemView.findViewById(R.id.text_supplier_list_item_products_count);
                mButtonDefaultPhone = itemView.findViewById(R.id.btn_supplier_list_item_default_phone);
                mButtonDefaultEmail = itemView.findViewById(R.id.btn_supplier_list_item_default_email);
                mButtonDelete = itemView.findViewById(R.id.btn_supplier_list_item_delete);
                mButtonEdit = itemView.findViewById(R.id.btn_supplier_list_item_edit);

                //Registering the Click listeners on the required views
                mButtonDefaultPhone.setOnClickListener(this);
                mButtonDefaultEmail.setOnClickListener(this);
                mButtonDelete.setOnClickListener(this);
                mButtonEdit.setOnClickListener(this);
                itemView.setOnClickListener(this);
            }

            /**
             * Method that binds the item views with the data {@link SupplierLite} at the position.
             *
             * @param supplierLite The {@link SupplierLite} data at the item position
             */
            void bind(SupplierLite supplierLite) {
                //Get the Resources
                Resources resources = itemView.getContext().getResources();

                //Setting the Name
                mTextViewSupplierName.setText(supplierLite.getName());
                //Setting the Code
                mTextViewSupplierCode.setText(supplierLite.getCode());
                //Setting the Item Count
                mTextViewSupplierItemCount.setText(resources.getString(R.string.supplier_list_item_products_count_desc, supplierLite.getItemCount()));

                //Reading the default phone and email
                String defaultPhone = supplierLite.getDefaultPhone();
                String defaultEmail = supplierLite.getDefaultEmail();
                if (!TextUtils.isEmpty(defaultPhone)) {
                    //When default phone is present, set the Phone Number with its formatting
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mButtonDefaultPhone.setText(PhoneNumberUtils.formatNumber(defaultPhone, Locale.getDefault().getCountry()));
                    } else {
                        mButtonDefaultPhone.setText(PhoneNumberUtils.formatNumber(defaultPhone));
                    }
                    //Ensuring the button is visible
                    mButtonDefaultPhone.setVisibility(View.VISIBLE);
                } else {
                    //When the default phone is absent, hide the element
                    mButtonDefaultPhone.setVisibility(View.GONE);
                }

                if (!TextUtils.isEmpty(defaultEmail)) {
                    //When the default email is present, set the email.
                    mButtonDefaultEmail.setText(defaultEmail);
                    //Ensuring the button is visible
                    mButtonDefaultEmail.setVisibility(View.VISIBLE);
                } else {
                    //When the default email is absent, hide the element
                    mButtonDefaultEmail.setVisibility(View.GONE);
                }

            }

            /**
             * Called when a view has been clicked.
             *
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                //Checking if the adapter position is valid
                int adapterPosition = getAdapterPosition();
                if (adapterPosition > RecyclerView.NO_POSITION) {
                    //When the adapter position is valid

                    //Get the data at the position
                    SupplierLite supplierLite = getItem(adapterPosition);

                    //Get the View Id clicked
                    int clickedViewId = view.getId();

                    //Taking action based on the view clicked
                    if (clickedViewId == itemView.getId()
                            || clickedViewId == R.id.btn_supplier_list_item_edit) {
                        //When the entire Item View or the "Edit" button is clicked

                        //Dispatch the event to the action listener
                        mActionsListener.onEditSupplier(adapterPosition, supplierLite);

                    } else if (clickedViewId == R.id.btn_supplier_list_item_delete) {
                        //When the "Delete" button is clicked

                        //Dispatch the event to the action listener
                        mActionsListener.onDeleteSupplier(adapterPosition, supplierLite);

                    } else if (clickedViewId == R.id.btn_supplier_list_item_default_phone) {
                        //When the default phone button is clicked

                        //Dispatch the event to the action listener
                        mActionsListener.onDefaultPhoneClicked(adapterPosition, supplierLite);

                    } else if (clickedViewId == R.id.btn_supplier_list_item_default_email) {
                        //When the default email button is clicked

                        //Dispatch the event to the action listener
                        mActionsListener.onDefaultEmailClicked(adapterPosition, supplierLite);
                    }
                }
            }
        }
    }

    /**
     * Listener that implements {@link SupplierListUserActionsListener} to receive
     * event callbacks for User actions on RecyclerView list of Suppliers.
     */
    private class UserActionsListener implements SupplierListUserActionsListener {

        /**
         * Callback Method of {@link SupplierListUserActionsListener} invoked when
         * the user clicks on "Edit" button or the Item View itself. This should
         * launch the {@link SupplierConfigActivity}
         * for the Supplier to be edited.
         *
         * @param itemPosition The adapter position of the Item View clicked
         * @param supplier     The {@link SupplierLite} associated with the Item View clicked.
         */
        @Override
        public void onEditSupplier(int itemPosition, SupplierLite supplier) {
            //Delegating to the Presenter to handle the event
            mPresenter.editSupplier(supplier.getId());
        }

        /**
         * Callback Method of {@link SupplierListUserActionsListener} invoked when
         * the user clicks on "Delete" button. This should delete the Supplier
         * identified by {@link SupplierLite#mId}, from the database.
         *
         * @param itemPosition The adapter position of the Item View clicked
         * @param supplier     The {@link SupplierLite} associated with the Item View clicked.
         */
        @Override
        public void onDeleteSupplier(int itemPosition, SupplierLite supplier) {
            //Delegating to the Presenter to handle the event
            mPresenter.deleteSupplier(supplier);
        }

        /**
         * Callback Method of {@link SupplierListUserActionsListener} invoked when
         * the user clicks on the default Phone shown. This should launch an Intent
         * to start the Phone Activity passing in the number {@link SupplierLite#mDefaultPhone}.
         *
         * @param itemPosition The adapter position of the Item View clicked
         * @param supplier     The {@link SupplierLite} associated with the Item View clicked.
         */
        @Override
        public void onDefaultPhoneClicked(int itemPosition, SupplierLite supplier) {
            //Delegating to the Presenter to handle the event
            mPresenter.defaultPhoneClicked(supplier.getDefaultPhone());
        }

        /**
         * Callback Method of {@link SupplierListUserActionsListener} invoked when
         * the user clicks on the default Email shown. This should launch an Intent
         * to start an Email Activity passing in the To address {@link SupplierLite#mDefaultEmail}.
         *
         * @param itemPosition The adapter position of the Item View clicked
         * @param supplier     The {@link SupplierLite} associated with the Item View clicked.
         */
        @Override
        public void onDefaultEmailClicked(int itemPosition, SupplierLite supplier) {
            //Delegating to the Presenter to handle the event
            mPresenter.defaultEmailClicked(supplier.getDefaultEmail());
        }

    }

}
