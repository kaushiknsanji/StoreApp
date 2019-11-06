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

package com.example.kaushiknsanji.storeapp.ui.inventory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.constraint.Group;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.local.models.SalesLite;
import com.example.kaushiknsanji.storeapp.ui.common.ListItemSpacingDecoration;
import com.example.kaushiknsanji.storeapp.ui.inventory.config.SalesConfigActivity;
import com.example.kaushiknsanji.storeapp.utils.ColorUtility;
import com.example.kaushiknsanji.storeapp.utils.SnackbarUtility;
import com.example.kaushiknsanji.storeapp.utils.TextAppearanceUtility;
import com.example.kaushiknsanji.storeapp.workers.ImageDownloaderFragment;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

/**
 * {@link com.example.kaushiknsanji.storeapp.ui.MainActivity}'s ViewPager Fragment that inflates
 * the layout 'R.layout.layout_main_content_page' to display the list of Products with their Sales information configured
 * in the database. This implements the {@link SalesListContract.View}
 * on the lines of Model-View-Presenter architecture.
 *
 * @author Kaushik N Sanji
 */
public class SalesListFragment extends Fragment
        implements SalesListContract.View, SwipeRefreshLayout.OnRefreshListener {

    //Constant used for logs
    private static final String LOG_TAG = SalesListFragment.class.getSimpleName();

    //The Presenter interface for this View
    private SalesListContract.Presenter mPresenter;

    //References to the Views shown in this Fragment
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerViewContentList;
    private Group mGroupEmptyList;

    //Adapter of the RecyclerView
    private SalesListAdapter mAdapter;

    /**
     * Mandatory Empty Constructor of {@link SalesListFragment}.
     * This is required by the {@link android.support.v4.app.FragmentManager} to instantiate
     * the fragment (e.g. upon screen orientation changes).
     */
    public SalesListFragment() {
    }

    /**
     * Static Factory constructor that creates an instance of {@link SalesListFragment}
     *
     * @return Instance of {@link SalesListFragment}
     */
    public static SalesListFragment newInstance() {
        return new SalesListFragment();
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
        imageViewStepNumber.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_main_sales_page_number));

        //Initialize the Empty TextView with Text
        textViewEmptyList.setText(getString(R.string.sales_list_empty_text));

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
     * Method that initializes a RecyclerView with its Adapter for loading and displaying the list of Products to Sell.
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

                    //Getting the last item position in the RecyclerView
                    int positionLast = getItemCount() - 1;
                    if (positionLast > positionStart) {
                        //When the last item position is more than the start position
                        for (int index = positionStart; index <= positionLast; index++) {
                            //Remove all the views from RecyclerView Cache till the last item position
                            //so that the RecyclerView grows in size properly to accommodate the items
                            //with proper item decoration height
                            removeView(findViewByPosition(index));
                        }
                        //Auto Scroll to the item position in the end
                        recyclerView.smoothScrollToPosition(positionStart);
                    }
                }

            }
        };

        //Setting the Layout Manager to use
        mRecyclerViewContentList.setLayoutManager(linearLayoutManager);

        //Initializing the Adapter for the RecyclerView
        mAdapter = new SalesListAdapter(requireContext(), new UserActionsListener());

        //Setting the Adapter on the RecyclerView
        mRecyclerViewContentList.setAdapter(mAdapter);

        //Retrieving the Item spacing to use
        int itemSpacing = getResources().getDimensionPixelSize(R.dimen.sales_list_items_spacing);

        //Setting Item offsets using Item Decoration
        mRecyclerViewContentList.addItemDecoration(new ListItemSpacingDecoration(
                itemSpacing, itemSpacing
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

        //Start loading the Products with Sales data
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
    public SalesListContract.Presenter getPresenter() {
        return mPresenter;
    }

    /**
     * Method that registers the Presenter {@code presenter} with the View implementing {@link com.example.kaushiknsanji.storeapp.ui.BaseView}
     *
     * @param presenter Presenter instance implementing the {@link com.example.kaushiknsanji.storeapp.ui.BasePresenter}
     */
    @Override
    public void setPresenter(SalesListContract.Presenter presenter) {
        mPresenter = presenter;
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        //Forcefully start a new load
        mPresenter.triggerProductSalesLoad(true);
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
     * Method invoked when an error is encountered during Sales information
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
     * Method invoked when the Sales List is empty. This should show a TextView with a
     * Text that suggests Users to first configure Products and its Suppliers into the database.
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
     * Method invoked when we have the Sales List. This should show the Sales List and
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
     * Method that updates the RecyclerView's Adapter with new {@code salesList} data.
     *
     * @param salesList List of Products with Sales data defined by {@link SalesLite},
     *                  loaded from the database.
     */
    @Override
    public void loadSalesList(ArrayList<SalesLite> salesList) {
        //Submitting the new updated list to the Adapter
        mAdapter.submitList(salesList);
    }

    /**
     * Method that displays a message on Success of Deleting an Existing Product.
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
     * Method that displays a message on Success of Selling a quantity of the Product
     * from the Top Supplier.
     *
     * @param productSku   The Product SKU of the Product sold.
     * @param supplierCode The Supplier Code of the Top Supplier for the Product sold.
     */
    @Override
    public void showSellQuantitySuccess(String productSku, String supplierCode) {
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.sales_list_item_sell_success, productSku, supplierCode), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Method that displays a message on Success of updating the Inventory of the Product.
     *
     * @param productSku String containing the SKU of the Product that was updated
     *                   with Inventory successfully.
     */
    @Override
    public void showUpdateInventorySuccess(String productSku) {
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.sales_list_item_update_inventory_success, productSku), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Method invoked when the user clicks on the Item View itself. This should launch the
     * {@link SalesConfigActivity}
     * for editing the Sales data of the Product.
     *
     * @param productId             The Primary Key of the Product to be edited.
     * @param activityOptionsCompat Instance of {@link ActivityOptionsCompat} that has the
     *                              details for Shared Element Transition
     */
    @Override
    public void launchEditProductSales(int productId, ActivityOptionsCompat activityOptionsCompat) {
        //Creating the Intent to launch SalesConfigActivity
        Intent salesConfigIntent = new Intent(requireContext(), SalesConfigActivity.class);
        //Passing in the Product ID of the Product to be edited
        salesConfigIntent.putExtra(SalesConfigActivity.EXTRA_PRODUCT_ID, productId);
        //Starting the Activity with Result
        startActivityForResult(salesConfigIntent, SalesConfigActivity.REQUEST_EDIT_SALES, activityOptionsCompat.toBundle());
    }

    /**
     * {@link ListAdapter} class for RecyclerView to load the list of Products for Selling.
     */
    private static class SalesListAdapter extends ListAdapter<SalesLite, SalesListAdapter.ViewHolder> {

        /**
         * {@link DiffUtil.ItemCallback} for calculating the difference between two {@link SalesLite} objects.
         */
        private static DiffUtil.ItemCallback<SalesLite> DIFF_SALES
                = new DiffUtil.ItemCallback<SalesLite>() {
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
            public boolean areItemsTheSame(SalesLite oldItem, SalesLite newItem) {
                //Returning the comparison of Item Id and Supplier Id
                return oldItem.getProductId() == newItem.getProductId()
                        && oldItem.getSupplierId() == newItem.getSupplierId();
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
             * This method is called only if {@link #areItemsTheSame(SalesLite, SalesLite)} returns {@code true} for
             * these items.
             *
             * @param oldItem The item in the old list.
             * @param newItem The item in the new list.
             * @return True if the contents of the items are the same or false if they are different.
             *
             * @see DiffUtil.Callback#areContentsTheSame(int, int)
             */
            @Override
            public boolean areContentsTheSame(SalesLite oldItem, SalesLite newItem) {
                //Returning the comparison of entire SalesLite
                return oldItem.equals(newItem);
            }
        };
        //Stores the Typeface used for Product SKU text
        private Typeface mProductSkuTypeface;
        //Listener for User Actions on Product List items
        private SalesListUserActionsListener mActionsListener;

        /**
         * Constructor of {@link SalesListAdapter}
         *
         * @param context             Context used for retrieving a Font
         * @param userActionsListener Instance of {@link SalesListUserActionsListener}
         *                            to receive event callbacks for User Actions on Item Views
         */
        SalesListAdapter(Context context, SalesListUserActionsListener userActionsListener) {
            super(DIFF_SALES);
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
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Inflating the item layout 'R.layout.item_sales_list'
            //Passing False since we are attaching the layout ourselves
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sales_list, parent, false);
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
            SalesLite salesLite = getItem(position);

            //Bind the Views with the data at the position
            holder.bind(position, salesLite);
        }

        /**
         * ViewHolder class for caching View components of the template item view 'R.layout.item_sales_list'
         */
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            //References to the Views required, in the Item View
            private TextView mTextViewProductName;
            private ImageView mImageViewProductPhoto;
            private TextView mTextViewProductSku;
            private TextView mTextViewProductCategory;
            private TextView mTextViewTotalAvailable;
            private TextView mTextViewSupplierNameCode;
            private TextView mTextViewSupplierPrice;
            private TextView mTextViewSupplierAvailability;
            private Button mButtonDeleteProduct;
            private Button mButtonSell;
            private Group mGroupTopSupplier;
            private Typeface mTotalAvailableTypeface;

            /**
             * Constructor of {@link ViewHolder}
             *
             * @param itemView Inflated Instance of the Item View 'R.layout.item_sales_list'
             */
            ViewHolder(View itemView) {
                super(itemView);

                //Finding the Views needed
                mTextViewProductName = itemView.findViewById(R.id.text_product_item_name);
                mImageViewProductPhoto = itemView.findViewById(R.id.image_product_item_photo);
                mTextViewProductSku = itemView.findViewById(R.id.text_product_item_sku);
                mTextViewProductCategory = itemView.findViewById(R.id.text_product_item_category);
                mTextViewTotalAvailable = itemView.findViewById(R.id.text_sales_list_item_total_available);
                mTextViewSupplierNameCode = itemView.findViewById(R.id.text_sales_list_item_supplier_name_code);
                mTextViewSupplierPrice = itemView.findViewById(R.id.text_sales_list_item_supplier_price);
                mTextViewSupplierAvailability = itemView.findViewById(R.id.text_sales_list_item_supplier_availability);
                mButtonDeleteProduct = itemView.findViewById(R.id.btn_sales_list_item_delete);
                mButtonSell = itemView.findViewById(R.id.btn_sales_list_item_sell);
                mGroupTopSupplier = itemView.findViewById(R.id.group_sales_list_item_top_supplier);

                //Reading the original typeface of "Total Available" TextView
                mTotalAvailableTypeface = mTextViewTotalAvailable.getTypeface();

                //Registering the Click listeners on the required views
                mButtonDeleteProduct.setOnClickListener(this);
                mButtonSell.setOnClickListener(this);
                itemView.setOnClickListener(this);
            }

            /**
             * Method that binds the views with the data at the position {@code salesLite}
             *
             * @param position  The position of the Item in the list
             * @param salesLite The {@link SalesLite} data at the item position
             */
            void bind(int position, SalesLite salesLite) {
                //Bind the Product Name
                mTextViewProductName.setText(salesLite.getProductName());
                //Bind the Product SKU
                mTextViewProductSku.setText(salesLite.getProductSku());
                //Set Barcode typeface for the SKU
                mTextViewProductSku.setTypeface(mProductSkuTypeface);
                //Download and Bind the Product Photo at the position
                ImageDownloaderFragment.newInstance(
                        ((FragmentActivity) mImageViewProductPhoto.getContext()).getSupportFragmentManager(), position)
                        .executeAndUpdate(mImageViewProductPhoto, salesLite.getDefaultImageUri(), position);
                //Bind the Product Category
                mTextViewProductCategory.setText(salesLite.getCategoryName());

                //Retrieving the Resources instance
                Resources resources = itemView.getContext().getResources();
                //Reading the Total Available Quantity
                int totalAvailableQuantity = salesLite.getTotalAvailableQuantity();

                //Checking the Total Available Quantity
                if (totalAvailableQuantity > 0) {
                    //When we have some quantity to sell

                    //Setting the Text, Color and Typeface to show the Total Available Quantity
                    TextAppearanceUtility.setHtmlText(mTextViewTotalAvailable, resources.getString(R.string.sales_list_item_total_available, totalAvailableQuantity));
                    mTextViewTotalAvailable.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.salesListItemTotalAvailableColor));
                    mTextViewTotalAvailable.setTypeface(mTotalAvailableTypeface);
                    mTextViewTotalAvailable.setAllCaps(false);
                    //Ensuring the Top Supplier details and the button to sell are visible
                    mGroupTopSupplier.setVisibility(View.VISIBLE);
                    //Setting the Supplier Name and its Code
                    mTextViewSupplierNameCode.setText(resources.getString(R.string.sales_list_item_supplier_name_code_format, salesLite.getTopSupplierName(), salesLite.getTopSupplierCode()));
                    //Setting the Selling Price of the Item
                    mTextViewSupplierPrice.setText(resources.getString(R.string.sales_list_item_supplier_selling_price,
                            Currency.getInstance(Locale.getDefault()).getSymbol() + " " + salesLite.getSupplierUnitPrice()));
                    //Setting the Availability at the Supplier
                    mTextViewSupplierAvailability.setText(String.valueOf(salesLite.getSupplierAvailableQuantity()));
                } else {
                    //When we have no quantity to sell

                    //Setting the Text, Color and Typeface to display "Out of Stock!"
                    mTextViewTotalAvailable.setText(resources.getString(R.string.sales_list_item_out_of_stock));
                    mTextViewTotalAvailable.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.salesListItemOutOfStockColor));
                    mTextViewTotalAvailable.setTypeface(mTotalAvailableTypeface, Typeface.BOLD);
                    mTextViewTotalAvailable.setAllCaps(true);

                    //Hiding the Top Supplier details and the button to sell
                    mGroupTopSupplier.setVisibility(View.GONE);
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
                    SalesLite salesLite = getItem(adapterPosition);

                    //Get the View Id clicked
                    int clickedViewId = view.getId();

                    //Taking action based on the view clicked
                    if (clickedViewId == itemView.getId()) {
                        //When the entire Item View is clicked

                        //Dispatch the event to the action listener
                        mActionsListener.onEditSales(adapterPosition, salesLite, mImageViewProductPhoto);

                    } else if (clickedViewId == mButtonDeleteProduct.getId()) {
                        //When the "Delete Product" button is clicked

                        //Dispatch the event to the action listener
                        mActionsListener.onDeleteProduct(adapterPosition, salesLite);

                    } else if (clickedViewId == mButtonSell.getId()) {
                        //When the "Sell 1" button is clicked

                        //Dispatch the event to the action listener
                        mActionsListener.onSellOneQuantity(adapterPosition, salesLite);

                    }

                }
            }
        }
    }

    /**
     * Listener that implements {@link SalesListUserActionsListener} to receive
     * event callbacks for User actions on RecyclerView list of Products for Selling.
     */
    private class UserActionsListener implements SalesListUserActionsListener {

        /**
         * Callback Method of {@link SalesListUserActionsListener} invoked when
         * the user clicks on the Item View itself. This should launch the
         * {@link SalesConfigActivity}
         * for editing the Sales data of the Product identified by {@link SalesLite#mProductId}.
         *
         * @param itemPosition          The adapter position of the Item View clicked.
         * @param salesLite             The {@link SalesLite} associated with the Item View clicked.
         * @param imageViewProductPhoto The ImageView of the Adapter Item that displays the Image
         */
        @Override
        public void onEditSales(int itemPosition, SalesLite salesLite, ImageView imageViewProductPhoto) {
            //Creating ActivityOptions for Shared Element Transition
            //where the ImageView is the Shared Element
            ActivityOptionsCompat activityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(),
                            imageViewProductPhoto,
                            TextUtils.isEmpty(salesLite.getDefaultImageUri()) ? getString(R.string.transition_name_product_photo) : salesLite.getDefaultImageUri()
                    );
            //Delegating to the Presenter to handle the event
            mPresenter.editProductSales(salesLite.getProductId(), activityOptionsCompat);
        }

        /**
         * Callback Method of {@link SalesListUserActionsListener} invoked when
         * the user clicks on the "Delete Product" button. This should delete the Product
         * identified by {@link SalesLite#mProductId} from the database along with its relationship
         * with other tables in database.
         *
         * @param itemPosition The adapter position of the Item View clicked.
         * @param salesLite    The {@link SalesLite} associated with the Item View clicked.
         */
        @Override
        public void onDeleteProduct(int itemPosition, SalesLite salesLite) {
            //Delegating to the Presenter to handle the event
            mPresenter.deleteProduct(salesLite.getProductId(), salesLite.getProductSku());
        }

        /**
         * Callback Method of {@link SalesListUserActionsListener} invoked when
         * the user clicks on the "Sell 1" button. This should decrease the Available Quantity
         * from the Top Supplier {@link SalesLite#mSupplierAvailableQuantity} by 1, indicating one Quantity
         * of the Product was sold/shipped.
         *
         * @param itemPosition The adapter position of the Item View clicked.
         * @param salesLite    The {@link SalesLite} associated with the Item View clicked.
         */
        @Override
        public void onSellOneQuantity(int itemPosition, SalesLite salesLite) {
            //Delegating to the Presenter to handle the event
            mPresenter.sellOneQuantity(salesLite);
        }
    }

}
