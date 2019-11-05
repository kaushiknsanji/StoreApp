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

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductLite;
import com.example.kaushiknsanji.storeapp.ui.common.ListItemSpacingDecoration;
import com.example.kaushiknsanji.storeapp.ui.common.ProgressDialogFragment;
import com.example.kaushiknsanji.storeapp.utils.OrientationUtility;
import com.example.kaushiknsanji.storeapp.utils.SnackbarUtility;
import com.example.kaushiknsanji.storeapp.workers.ImageDownloaderFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Content Fragment of {@link SupplierProductPickerActivity} that inflates the layout 'R.layout.fragment_supplier_product_picker'
 * to display a list of Products that can be picked to be sold by the Supplier. This implements the
 * {@link SupplierProductPickerContract.View} on the lines of Model-View-Presenter architecture.
 *
 * @author Kaushik N Sanji
 */
public class SupplierProductPickerActivityFragment extends Fragment implements SupplierProductPickerContract.View {

    //Constant used for logs
    private static final String LOG_TAG = SupplierProductPickerActivityFragment.class.getSimpleName();

    //The Bundle argument constant of this Fragment
    private static final String ARGUMENT_LIST_SUPPLIER_PRODUCTS = "argument.SUPPLIER_PRODUCTS";

    //Bundle constants for persisting the data through System config changes
    private static final String BUNDLE_REMAINING_PRODUCTS_LIST_KEY = "SupplierProductPicker.RemainingProducts";
    private static final String BUNDLE_SELECTED_PRODUCTS_LIST_KEY = "SupplierProductPicker.SelectedProducts";

    //The Presenter for this View
    private SupplierProductPickerContract.Presenter mPresenter;

    //Stores the instance of the View components required
    private RecyclerView mRecyclerViewProducts;
    private TextView mTextViewEmptyList;

    //The RecyclerView Adapter to display the Products
    private ProductListAdapter mProductListAdapter;

    //Stores the list of Products already registered by the Supplier for selling
    private ArrayList<ProductLite> mRegisteredProducts;
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
                    //Start saving the selected list of Products
                    saveSelectedProducts();
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
                    //For "Stay Here" button

                    //Just Dismiss the dialog
                    dialog.dismiss();
                    //Unlock orientation
                    OrientationUtility.unlockScreenOrientation(requireActivity());
                    break;
            }
        }
    };

    /**
     * Mandatory Empty Constructor of {@link SupplierProductPickerActivityFragment}
     * This is required by the {@link android.support.v4.app.FragmentManager} to instantiate
     * the fragment (e.g. upon screen orientation changes).
     */
    public SupplierProductPickerActivityFragment() {
    }

    /**
     * Static Factory Constructor that creates an instance of {@link SupplierProductPickerActivityFragment}
     * using the provided list of Supplier's Products {@code supplierProducts}.
     *
     * @param supplierProducts List of Products {@link ProductLite} being sold by the Supplier
     * @return Instance of {@link SupplierProductPickerActivityFragment}
     */
    public static SupplierProductPickerActivityFragment newInstance(ArrayList<ProductLite> supplierProducts) {
        //Saving the arguments passed, in a Bundle: START
        Bundle args = new Bundle(1);
        args.putParcelableArrayList(ARGUMENT_LIST_SUPPLIER_PRODUCTS, supplierProducts);
        //Saving the arguments passed, in a Bundle: END

        //Instantiating the Fragment
        SupplierProductPickerActivityFragment fragment = new SupplierProductPickerActivityFragment();
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
     * @return Returns the View for the fragment's UI ('R.layout.fragment_supplier_product_picker')
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout 'R.layout.fragment_supplier_product_picker' for this fragment
        //Passing false as we are attaching the layout ourselves
        View rootView = inflater.inflate(R.layout.fragment_supplier_product_picker, container, false);

        //Finding the Views to initialize
        mTextViewEmptyList = rootView.findViewById(R.id.text_supplier_product_picker_empty_list);
        mRecyclerViewProducts = rootView.findViewById(R.id.recyclerview_supplier_product_picker);

        //Reading the list of registered products from the Arguments Bundle
        Bundle arguments = getArguments();
        if (arguments != null) {
            mRegisteredProducts = arguments.getParcelableArrayList(ARGUMENT_LIST_SUPPLIER_PRODUCTS);
        }

        //Initialize RecyclerView for Product List
        setupProductListRecyclerView();

        //Returning the prepared layout
        return rootView;
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
     * <p>This corresponds to {@link FragmentActivity#onSaveInstanceState(Bundle)
     * Activity.onSaveInstanceState(Bundle)} and most of the discussion there
     * applies here as well.  Note however: <em>this method may be called
     * at any time before {@link #onDestroy()}</em>.  There are many situations
     * where a fragment may be mostly torn down (such as when placed on the
     * back stack with no UI showing), but its state will not be saved until
     * its owning activity actually needs to save its state.
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mProductListAdapter != null) {
            //Saving data when the Adapter is set

            //Save the Remaining list of Products data
            outState.putParcelableArrayList(BUNDLE_REMAINING_PRODUCTS_LIST_KEY, mProductListAdapter.getRemainingProducts());
            //Save the Selected list of Products data
            outState.putParcelableArrayList(BUNDLE_SELECTED_PRODUCTS_LIST_KEY, mProductListAdapter.getSelectedProducts());
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

            //Restore the list of Remaining Products
            ArrayList<ProductLite> remainingProducts = savedInstanceState.getParcelableArrayList(BUNDLE_REMAINING_PRODUCTS_LIST_KEY);
            //Restore the list of Selected Products
            ArrayList<ProductLite> selectedProducts = savedInstanceState.getParcelableArrayList(BUNDLE_SELECTED_PRODUCTS_LIST_KEY);
            //Delegating to the Presenter to load the RecyclerView Adapter data
            mPresenter.loadProductsToPick(mRegisteredProducts, remainingProducts, selectedProducts);
        }
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
        //Inflating the Menu options from 'R.menu.menu_fragment_supplier_product_picker'
        inflater.inflate(R.menu.menu_fragment_supplier_product_picker, menu);
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to {@link FragmentActivity#onResume() Activity.onResume} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onResume() {
        super.onResume();

        //Delegating to the Presenter to load the RecyclerView Adapter data
        mPresenter.loadProductsToPick(mRegisteredProducts, null, null);

    }

    /**
     * Method that registers the Presenter {@code presenter} with the View implementing
     * {@link com.example.kaushiknsanji.storeapp.ui.BaseView}
     *
     * @param presenter Presenter instance implementing the
     *                  {@link com.example.kaushiknsanji.storeapp.ui.BasePresenter}
     */
    @Override
    public void setPresenter(SupplierProductPickerContract.Presenter presenter) {
        mPresenter = presenter;
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
            case R.id.action_save:
                //On click of "Save" menu

                //Start saving the selected list of Products
                saveSelectedProducts();
                return true;
            default:
                //On other cases, do the default menu handling
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Method that saves the selected list of Products to the registered list.
     */
    private void saveSelectedProducts() {
        //Propagating the call to Presenter to begin the Save operation
        mPresenter.onSave(mRegisteredProducts, mProductListAdapter.getSelectedProducts());
    }

    /**
     * Method that initializes the RecyclerView 'R.id.recyclerview_supplier_product_picker' and its Adapter
     */
    private void setupProductListRecyclerView() {
        //Creating a Vertical Linear Layout Manager with default layout order
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false);

        //Setting the Layout Manager to use
        mRecyclerViewProducts.setLayoutManager(linearLayoutManager);

        //Initializing the Adapter
        mProductListAdapter = new ProductListAdapter(requireContext(), new UserActionsListener());

        //Setting the Adapter on RecyclerView
        mRecyclerViewProducts.setAdapter(mProductListAdapter);

        //Retrieving the Item spacing to use
        int itemSpacing = getResources().getDimensionPixelSize(R.dimen.supplier_product_list_items_spacing);

        //Setting Item offsets using Item Decoration
        mRecyclerViewProducts.addItemDecoration(new ListItemSpacingDecoration(itemSpacing, itemSpacing));

        //The size of Adapter items does not change the RecyclerView size
        mRecyclerViewProducts.setHasFixedSize(true);
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
     * Method invoked when an error is encountered during Supplier information
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
     * Method invoked when we have the Product List. This should show the Product List and
     * hide the Empty List TextView.
     */
    @Override
    public void hideEmptyView() {
        //Displaying the RecyclerView
        mRecyclerViewProducts.setVisibility(View.VISIBLE);
        //Hiding the Empty List TextView
        mTextViewEmptyList.setVisibility(View.GONE);
    }

    /**
     * Method invoked when the Product List is empty. This should show a TextView with a
     * Text {@code emptyTextResId} that tells the reason why the list is empty.
     *
     * @param emptyTextResId The String Resource of the empty message to be shown.
     * @param args           Variable number of arguments to replace the format specifiers
     */
    @Override
    public void showEmptyView(int emptyTextResId, @Nullable Object... args) {
        //Hiding the RecyclerView
        mRecyclerViewProducts.setVisibility(View.INVISIBLE);
        //Displaying the Empty List TextView
        mTextViewEmptyList.setVisibility(View.VISIBLE);

        //Evaluating the message to be shown
        String messageToBeShown;
        if (args != null && args.length > 0) {
            //For the String Resource with args
            messageToBeShown = getString(emptyTextResId, args);
        } else {
            //For the String Resource without args
            messageToBeShown = getString(emptyTextResId);
        }

        //Setting the Text to show
        mTextViewEmptyList.setText(messageToBeShown);
    }

    /**
     * Method invoked to update the list of Products {@link ProductLite} displayed by the Adapter
     * of the RecyclerView.
     *
     * @param remainingProducts List of remaining Products {@link ProductLite} that can
     *                          be picked, which is the data displayed by the Adapter.
     * @param selectedProducts  List of Products {@link ProductLite} that were currently selected if any.
     */
    @Override
    public void submitDataToAdapter(ArrayList<ProductLite> remainingProducts, @Nullable ArrayList<ProductLite> selectedProducts) {
        //Submitting data to the Adapter
        mProductListAdapter.submitData(remainingProducts, selectedProducts);
    }

    /**
     * Method invoked to filter the Product List shown by the Adapter, for the Product Name/SKU/Category
     * passed in the Search Query {@code searchQueryStr}
     *
     * @param searchQueryStr The Product Name/SKU/Category to filter in the Product List
     * @param filterListener A listener notified upon completion of the operation
     */
    @Override
    public void filterAdapterData(String searchQueryStr, Filter.FilterListener filterListener) {
        //Applying the filter on the Adapter to load results for "searchQueryStr"
        mProductListAdapter.getFilter().filter(searchQueryStr, filterListener);
    }

    /**
     * Method invoked to clear the filter applied on the Adapter of the RecyclerView
     *
     * @param filterListener A listener notified upon completion of the operation
     */
    @Override
    public void clearAdapterFilter(Filter.FilterListener filterListener) {
        //Clearing the filter applied on the Adapter
        mProductListAdapter.getFilter().filter(null, filterListener);
    }

    /**
     * Method invoked by the Presenter to display the Discard dialog,
     * requesting the User whether to stay here/discard the changes
     */
    @Override
    public void showDiscardDialog() {
        //Creating an AlertDialog with a message, and listeners for the positive, neutral and negative buttons
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        //Set the Message
        builder.setMessage(R.string.supplier_product_picker_unsaved_changes_dialog_message);
        //Set the Positive Button and its listener
        builder.setPositiveButton(R.string.supplier_product_picker_unsaved_changes_dialog_positive_text, mUnsavedDialogOnClickListener);
        //Set the Negative Button and its listener
        builder.setNegativeButton(R.string.supplier_product_picker_unsaved_changes_dialog_negative_text, mUnsavedDialogOnClickListener);
        //Set the Neutral Button and its listener
        builder.setNeutralButton(R.string.supplier_product_picker_unsaved_changes_dialog_neutral_text, mUnsavedDialogOnClickListener);
        //Lock the Orientation
        OrientationUtility.lockCurrentScreenOrientation(requireActivity());
        //Create and display the AlertDialog
        builder.create().show();
    }

    /**
     * {@link ListAdapter} class for RecyclerView to load the list of Products to be displayed.
     * Implements {@link Filterable} to provide filtering capability for the search query executed.
     */
    private static class ProductListAdapter extends ListAdapter<ProductLite, ProductListAdapter.ViewHolder>
            implements Filterable {

        //Payload constants used to rebind the state of list items for the position stored here
        private static final String PAYLOAD_SELECTED_PRODUCT = "Payload.SelectedProductPosition";
        private static final String PAYLOAD_UNSELECTED_PRODUCT = "Payload.UnselectedProductPosition";
        /**
         * {@link DiffUtil.ItemCallback} for calculating the difference between two {@link ProductLite} objects
         */
        private static DiffUtil.ItemCallback<ProductLite> DIFF_PRODUCTS
                = new DiffUtil.ItemCallback<ProductLite>() {
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
            public boolean areItemsTheSame(ProductLite oldItem, ProductLite newItem) {
                //Returning the comparison of the Product's Id
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
             * This method is called only if {@link #areItemsTheSame(ProductLite, ProductLite)} returns {@code true} for
             * these items.
             *
             * @param oldItem The item in the old list.
             * @param newItem The item in the new list.
             * @return True if the contents of the items are the same or false if they are different.
             *
             * @see DiffUtil.Callback#areContentsTheSame(int, int)
             */
            @Override
            public boolean areContentsTheSame(ProductLite oldItem, ProductLite newItem) {
                //Returning the comparison of entire Product
                return oldItem.equals(newItem);
            }
        };
        //Stores the Typeface used for Product SKU text
        private Typeface mProductSkuTypeface;
        //Listener for the User actions on the Product List Items
        private SupplierProductPickerListUserActionsListener mActionsListener;
        //The Data of this Adapter
        private ArrayList<ProductLite> mRemainingProducts;
        //The List of Products selected by the Supplier for selling
        private ArrayList<ProductLite> mSelectedProducts;

        /**
         * Constructor of {@link ProductListAdapter}
         *
         * @param context             Context used for retrieving a Font
         * @param userActionsListener Instance of {@link SupplierProductPickerListUserActionsListener}
         *                            to receive event callbacks for User Actions on Item Views
         */
        ProductListAdapter(Context context, SupplierProductPickerListUserActionsListener userActionsListener) {
            //Calling to Super with the DiffUtil to use
            super(DIFF_PRODUCTS);
            //Registering the User Actions Listener
            mActionsListener = userActionsListener;
            //Initializing the Selected Products List
            mSelectedProducts = new ArrayList<>();
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
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Inflating the item layout 'R.layout.item_supplier_product_picker'
            //Passing False since we are attaching the layout ourselves
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_supplier_product_picker, parent, false);
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
            ProductLite productLite = getItem(position);

            //Bind the Views with the data at the position
            holder.bind(position, productLite);

            //Bind the item selection state based on whether
            //this Product is part of the selected product list or not
            holder.setSelected(mSelectedProducts.contains(productLite));
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
                        case PAYLOAD_SELECTED_PRODUCT:
                            //For the selected product position

                            //Get the position from the bundle
                            int selectedProductPosition = bundle.getInt(keyStr, RecyclerView.NO_POSITION);
                            if (selectedProductPosition > RecyclerView.NO_POSITION
                                    && selectedProductPosition == position) {
                                //When the position is for the selected product, show the selected item state
                                holder.setSelected(true);
                            }
                            break;
                        case PAYLOAD_UNSELECTED_PRODUCT:
                            //For the unselected product position

                            //Get the position from the bundle
                            int unselectedProductPosition = bundle.getInt(keyStr, RecyclerView.NO_POSITION);
                            if (unselectedProductPosition > RecyclerView.NO_POSITION
                                    && unselectedProductPosition == position) {
                                //When the position is for the unselected product, reset the selected item state
                                holder.setSelected(false);
                            }
                            break;
                    }
                }
            }
        }

        /**
         * <p>Returns a filter that can be used to constrain data with a filtering
         * pattern.</p>
         * <p>
         * <p>This method is usually implemented by {@link Adapter}
         * classes.</p>
         *
         * @return a filter used to constrain data
         */
        @Override
        public Filter getFilter() {
            //Defining and Returning a New Custom Filter
            return new Filter() {

                /**
                 * <p>Invoked in a worker thread to filter the data according to the
                 * constraint. Subclasses must implement this method to perform the
                 * filtering operation. Results computed by the filtering operation
                 * must be returned as a {@link android.widget.Filter.FilterResults} that
                 * will then be published in the UI thread through
                 * {@link #publishResults(CharSequence,
                 * android.widget.Filter.FilterResults)}.</p>
                 *
                 * <p><strong>Contract:</strong> When the constraint is null, the original
                 * data must be restored.</p>
                 *
                 * @param constraint the constraint used to filter the data
                 * @return the results of the filtering operation
                 *
                 * @see #filter(CharSequence, android.widget.Filter.FilterListener)
                 * @see #publishResults(CharSequence, android.widget.Filter.FilterResults)
                 * @see android.widget.Filter.FilterResults
                 */
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    //Holds the results of the Filtering operation
                    FilterResults filterResults = new FilterResults();

                    if (TextUtils.isEmpty(constraint)) {
                        //When the constraint filter is absent, pass in the original unfiltered data
                        filterResults.values = mRemainingProducts;
                        filterResults.count = mRemainingProducts.size();
                    } else {
                        //When the constraint filter is present

                        //Trimming the constraint passed
                        constraint = constraint.toString().trim();

                        //Initializing an ArrayList of Products to store the Products matching the constraint
                        ArrayList<ProductLite> filteredProductList = new ArrayList<>();
                        //Iterating over the original unfiltered data to build the filtered list
                        for (ProductLite product : mRemainingProducts) {
                            //Get the Product Name
                            String productName = product.getName();
                            //Get the Product SKU
                            String productSku = product.getSku();
                            //Get the Category
                            String category = product.getCategory();

                            if (productName.contains(constraint) || productSku.contains(constraint)
                                    || category.contains(constraint)) {
                                //When the constraint is present in the Name/SKU/Category,
                                //add the Product to the filtered list
                                filteredProductList.add(product);
                            }
                        }
                        //Load the filtered list built
                        filterResults.values = filteredProductList;
                        filterResults.count = filteredProductList.size();
                    }
                    //Returning the results of the filtering operation
                    return filterResults;
                }

                /**
                 * <p>Invoked in the UI thread to publish the filtering results in the
                 * user interface. Subclasses must implement this method to display the
                 * results computed in {@link #performFiltering}.</p>
                 *
                 * @param constraint the constraint used to filter the data
                 * @param results the results of the filtering operation
                 *
                 * @see #filter(CharSequence, android.widget.Filter.FilterListener)
                 * @see #performFiltering(CharSequence)
                 * @see android.widget.Filter.FilterResults
                 */
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    //Casting the results into a List of Products
                    ArrayList<ProductLite> filteredProductList = (ArrayList<ProductLite>) results.values;
                    //Submitting the list to be loaded
                    submitList(filteredProductList);
                }
            };
        }

        /**
         * Method that submits the data to the Adapter.
         *
         * @param remainingProducts The List of Products {@link ProductLite} to be displayed. This is the data of the Adapter.
         * @param selectedProducts  The List of Products {@link ProductLite} selected by the Supplier if any. Can be {@code null}.
         */
        void submitData(ArrayList<ProductLite> remainingProducts,
                        @Nullable ArrayList<ProductLite> selectedProducts) {

            if (selectedProducts != null) {
                //When new selected products are passed

                //Clear the adapter list of selected products
                mSelectedProducts.clear();
                //Load the new list
                mSelectedProducts.addAll(selectedProducts);
            }

            //Load the list of products to be shown
            mRemainingProducts = remainingProducts;
            //Submitting the list to be loaded
            submitList(mRemainingProducts);
        }

        /**
         * Getter Method for the data of this Adapter.
         *
         * @return The List of Unfiltered Products {@link ProductLite} shown by the Adapter.
         */
        ArrayList<ProductLite> getRemainingProducts() {
            return mRemainingProducts;
        }

        /**
         * Getter Method for the list of Selected Products.
         *
         * @return The List of Products {@link ProductLite} selected/picked by the Supplier.
         */
        ArrayList<ProductLite> getSelectedProducts() {
            return mSelectedProducts;
        }

        /**
         * Method that updates the Selected list of Products for the Product being selected/unselected.
         *
         * @param position    The position of the item in the list being clicked to select/unselect.
         * @param productLite The Product data {@link ProductLite} at the position of the item being clicked.
         */
        private void updateSelectList(int position, ProductLite productLite) {
            if (mSelectedProducts.contains(productLite)) {
                //When the list already contains the Product, remove from the list
                mSelectedProducts.remove(productLite);

                //Creating a Bundle to do a partial update, to reset the selected state
                Bundle payloadBundle = new Bundle(1);
                //Put the position of the Product unselected, in the Bundle for update
                payloadBundle.putInt(PAYLOAD_UNSELECTED_PRODUCT, position);
                //Notify the change at the position to reset the selected state
                notifyItemChanged(position, payloadBundle);
            } else {
                //When the list does not contain the Product yet, add to the list
                mSelectedProducts.add(productLite);
                //Creating a Bundle to do a partial update, to show the selected state
                Bundle payloadBundle = new Bundle(1);
                //Put the position of the Product selected, in the Bundle for update
                payloadBundle.putInt(PAYLOAD_SELECTED_PRODUCT, position);
                //Notify the change at the position to show the selected state
                notifyItemChanged(position, payloadBundle);
            }
            //Invoking the User Actions Listener for updating the count of selected Products
            mActionsListener.onItemClicked(position);
        }

        /**
         * ViewHolder class for caching View components of the template item view
         * 'R.layout.item_supplier_product_picker'
         */
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            //The TextView that displays the Product Name
            private TextView mTextViewProductName;
            //The ImageView that displays the Product Photo
            private ImageView mImageViewProductPhoto;
            //The TextView that displays the Product SKU
            private TextView mTextViewProductSku;
            //The TextView that displays the Product Category
            private TextView mTextViewProductCategory;
            //The ImageView that displays the selected/unselected state of the View
            private ImageView mImageViewSelect;

            /**
             * Constructor of {@link ViewHolder}
             *
             * @param itemView Inflated Instance of the Item View 'R.layout.item_supplier_product_picker'
             */
            ViewHolder(View itemView) {
                super(itemView);

                //Finding the Views needed
                mTextViewProductName = itemView.findViewById(R.id.text_product_item_name);
                mImageViewProductPhoto = itemView.findViewById(R.id.image_product_item_photo);
                mTextViewProductSku = itemView.findViewById(R.id.text_product_item_sku);
                mTextViewProductCategory = itemView.findViewById(R.id.text_product_item_category);
                mImageViewSelect = itemView.findViewById(R.id.image_supplier_product_picker_item_select);

                //Registering the Click listener on the entire view
                itemView.setOnClickListener(this);
            }

            /**
             * Method that binds the views with the data at the position {@code productLite}
             *
             * @param position    The position of the Item in the list
             * @param productLite The {@link ProductLite} data at the item position
             */
            void bind(int position, ProductLite productLite) {
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
                    ProductLite productLite = getItem(adapterPosition);

                    //Get the View Id clicked
                    int clickedViewId = view.getId();

                    //Taking action based on the Id of the View clicked
                    if (clickedViewId == itemView.getId()) {
                        //When the entire item view is clicked
                        //Add/Remove the product to/from the select list
                        updateSelectList(adapterPosition, productLite);
                    }
                }
            }

            /**
             * Method that changes the selection state of the ImageView 'R.id.image_supplier_product_picker_item_select'
             * based on {@code selected} passed.
             *
             * @param selected true if the view must be selected, false otherwise
             */
            public void setSelected(boolean selected) {
                //Update the selection state on the ImageView
                mImageViewSelect.setSelected(selected);
            }
        }

    }

    /**
     * Listener that implements {@link SupplierProductPickerListUserActionsListener} to receive
     * event callbacks for User actions on RecyclerView list of Products.
     */
    private class UserActionsListener implements SupplierProductPickerListUserActionsListener {

        /**
         * Callback Method of {@link SupplierProductPickerListUserActionsListener} invoked when
         * the user clicks on an Item in the RecyclerView that displays a list of Products to pick/select
         *
         * @param itemPosition The adapter position of the Item clicked
         */
        @Override
        public void onItemClicked(int itemPosition) {
            //Delegate to the Presenter to update the count of Products selected
            mPresenter.updateSelectedProductCount(mProductListAdapter.getSelectedProducts().size());
        }
    }
}