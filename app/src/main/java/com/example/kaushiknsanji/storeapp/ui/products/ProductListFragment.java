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

package com.example.kaushiknsanji.storeapp.ui.products;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.example.kaushiknsanji.storeapp.data.local.models.ProductLite;
import com.example.kaushiknsanji.storeapp.ui.common.ListItemSpacingDecoration;
import com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigActivity;
import com.example.kaushiknsanji.storeapp.utils.ColorUtility;
import com.example.kaushiknsanji.storeapp.utils.SnackbarUtility;
import com.example.kaushiknsanji.storeapp.workers.ImageDownloaderFragment;

import java.util.ArrayList;

/**
 * {@link com.example.kaushiknsanji.storeapp.ui.MainActivity}'s ViewPager Fragment that inflates
 * the layout 'R.layout.layout_main_content_page' to display the list of Products configured
 * in the database. This implements the {@link ProductListContract.View} on the lines of
 * Model-View-Presenter architecture.
 *
 * @author Kaushik N Sanji
 */
public class ProductListFragment extends Fragment
        implements ProductListContract.View, SwipeRefreshLayout.OnRefreshListener {

    //Constant used for logs
    private static final String LOG_TAG = ProductListFragment.class.getSimpleName();

    //The Presenter interface for this View
    private ProductListContract.Presenter mPresenter;

    //References to the Views shown in this Fragment
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerViewContentList;
    private Group mGroupEmptyList;

    //Adapter of the RecyclerView
    private ProductListAdapter mAdapter;

    /**
     * Mandatory Empty Constructor of {@link ProductListFragment}.
     * This is required by the {@link android.support.v4.app.FragmentManager} to instantiate
     * the fragment (e.g. upon screen orientation changes).
     */
    public ProductListFragment() {
    }

    /**
     * Static Factory constructor that creates an instance of {@link ProductListFragment}
     *
     * @return Instance of {@link ProductListFragment}
     */
    @NonNull
    public static ProductListFragment newInstance() {
        return new ProductListFragment();
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
        imageViewStepNumber.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_main_product_page_number));

        //Initialize the Empty TextView with Text
        textViewEmptyList.setText(getString(R.string.product_list_empty_text));

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
     * Method that initializes a RecyclerView with its Adapter for loading and displaying the list of Products.
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
        mAdapter = new ProductListAdapter(requireContext(), new UserActionsListener());

        //Setting the Adapter for RecyclerView
        mRecyclerViewContentList.setAdapter(mAdapter);

        //Retrieving the Item spacing to use
        int itemSpacing = getResources().getDimensionPixelSize(R.dimen.product_list_items_spacing);

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

        //Start loading the Products
        mPresenter.start();
    }

    /**
     * Method that returns the registered Presenter for this View.
     *
     * @return The registered Presenter for this View. Can be {@code null}
     */
    @Nullable
    @Override
    public ProductListContract.Presenter getPresenter() {
        return mPresenter;
    }

    /**
     * Method that registers the Presenter {@code presenter} with the View implementing {@link com.example.kaushiknsanji.storeapp.ui.BaseView}
     *
     * @param presenter Presenter instance implementing the {@link com.example.kaushiknsanji.storeapp.ui.BasePresenter}
     */
    @Override
    public void setPresenter(ProductListContract.Presenter presenter) {
        mPresenter = presenter;
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
     * Method that updates the RecyclerView's Adapter with new {@code productList} data.
     *
     * @param productList List of Products defined by {@link ProductLite},
     *                    loaded from the database
     */
    @Override
    public void loadProducts(ArrayList<ProductLite> productList) {
        //Submitting the new updated list to the Adapter
        mAdapter.submitList(productList);
    }

    /**
     * Method invoked when the user clicks on the "Edit" button on the Item View or the Item View itself
     * to edit the Product details. This should
     * launch the {@link ProductConfigActivity}
     * for the Product to be edited.
     *
     * @param productId             The Primary Key of the Product to be edited.
     * @param activityOptionsCompat Instance of {@link ActivityOptionsCompat} that has the
     *                              details for Shared Element Transition
     */
    @Override
    public void launchEditProduct(int productId, ActivityOptionsCompat activityOptionsCompat) {
        //Creating the Intent to launch ProductConfigActivity
        Intent productConfigIntent = new Intent(requireContext(), ProductConfigActivity.class);
        //Passing in the Product ID of the Product to be edited
        productConfigIntent.putExtra(ProductConfigActivity.EXTRA_PRODUCT_ID, productId);
        //Starting the Activity with Result
        startActivityForResult(productConfigIntent, ProductConfigActivity.REQUEST_EDIT_PRODUCT, activityOptionsCompat.toBundle());
    }

    /**
     * Method invoked when an error is encountered during Product information
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
     * Method that displays a message on Success of adding a New Product.
     *
     * @param productSku String containing the SKU of the Product that was added successfully.
     */
    @Override
    public void showAddSuccess(String productSku) {
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.product_list_item_add_success, productSku), Snackbar.LENGTH_LONG).show();
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
     * Method invoked when the Product List is empty. This should show a TextView with a
     * Text that suggests Users to add Products into the database.
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
     * Method invoked when we have the Product List. This should show the Product List and
     * hide the Empty List TextView and Step Number drawable.
     */
    @Override
    public void hideEmptyView() {
        //Displaying the RecyclerView
        mRecyclerViewContentList.setVisibility(View.VISIBLE);
        //Hiding the Empty List TextView and Step Number Drawable
        mGroupEmptyList.setVisibility(View.GONE);
    }

    /**
     * Method invoked when the user clicks on the FAB button to add a New Product
     * into the database. This should
     * launch the {@link ProductConfigActivity}
     * for configuring a New Product.
     */
    @Override
    public void launchAddNewProduct() {
        //Creating the Intent to launch ProductConfigActivity
        Intent productConfigIntent = new Intent(requireContext(), ProductConfigActivity.class);
        //Starting the Activity with Result
        startActivityForResult(productConfigIntent, ProductConfigActivity.REQUEST_ADD_PRODUCT);
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
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        //Forcefully start a new load
        mPresenter.triggerProductsLoad(true);
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
     * {@link ListAdapter} class for RecyclerView to load the list of Products to be displayed.
     */
    private static class ProductListAdapter extends ListAdapter<ProductLite, ProductListAdapter.ViewHolder> {

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
        private ProductListUserActionsListener mActionsListener;

        /**
         * Constructor of {@link ProductListAdapter}
         *
         * @param context             Context used for retrieving a Font
         * @param userActionsListener Instance of {@link ProductListUserActionsListener}
         */
        ProductListAdapter(Context context, ProductListUserActionsListener userActionsListener) {
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
        public ProductListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Inflating the item layout 'R.layout.item_product_list'
            //Passing False since we are attaching the layout ourselves
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_list, parent, false);
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
        public void onBindViewHolder(@NonNull ProductListAdapter.ViewHolder holder, int position) {
            //Get the data at the position
            ProductLite productLite = getItem(position);

            //Bind the Views with the data at the position
            holder.bind(position, productLite);
        }

        /**
         * ViewHolder class for caching View components of the template item view 'R.layout.item_product_list'
         */
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            //References to the Views required, in the Item View
            private TextView mTextViewProductName;
            private ImageView mImageViewProductPhoto;
            private TextView mTextViewProductSku;
            private TextView mTextViewProductCategory;
            private Button mButtonDelete;
            private Button mButtonEdit;

            /**
             * Constructor of {@link ViewHolder}
             *
             * @param itemView Inflated Instance of the Item View 'R.layout.item_product_list'
             */
            ViewHolder(View itemView) {
                super(itemView);

                //Finding the Views needed
                mTextViewProductName = itemView.findViewById(R.id.text_product_item_name);
                mImageViewProductPhoto = itemView.findViewById(R.id.image_product_item_photo);
                mTextViewProductSku = itemView.findViewById(R.id.text_product_item_sku);
                mTextViewProductCategory = itemView.findViewById(R.id.text_product_item_category);
                mButtonDelete = itemView.findViewById(R.id.btn_product_list_item_delete);
                mButtonEdit = itemView.findViewById(R.id.btn_product_list_item_edit);

                //Registering the Click listeners on the required views
                mButtonDelete.setOnClickListener(this);
                mButtonEdit.setOnClickListener(this);
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

                    //Taking action based on the view clicked
                    if (clickedViewId == itemView.getId()
                            || clickedViewId == R.id.btn_product_list_item_edit) {
                        //When the entire Item View or the "Edit" button is clicked

                        //Dispatch the event to the action listener
                        mActionsListener.onEditProduct(adapterPosition, productLite, mImageViewProductPhoto);

                    } else if (clickedViewId == R.id.btn_product_list_item_delete) {
                        //When the "Delete" button is clicked

                        //Dispatch the event to the action listener
                        mActionsListener.onDeleteProduct(adapterPosition, productLite);
                    }
                }
            }
        }
    }

    /**
     * Listener that implements {@link ProductListUserActionsListener} to receive
     * event callbacks for User actions on RecyclerView list of Products.
     */
    private class UserActionsListener implements ProductListUserActionsListener {

        /**
         * Callback Method of {@link ProductListUserActionsListener} invoked when
         * the user clicks on "Edit" button or the Item View itself. This should
         * launch the {@link ProductConfigActivity}
         * for the Product to be edited.
         *
         * @param itemPosition          The adapter position of the Item clicked
         * @param product               The {@link ProductLite} associated with the Item clicked
         * @param imageViewProductPhoto The ImageView of the Adapter Item that displays the Image
         */
        @Override
        public void onEditProduct(final int itemPosition, ProductLite product, ImageView imageViewProductPhoto) {
            //Creating ActivityOptions for Shared Element Transition
            //where the ImageView is the Shared Element
            ActivityOptionsCompat activityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(),
                            imageViewProductPhoto,
                            TextUtils.isEmpty(product.getDefaultImageUri()) ? getString(R.string.transition_name_product_photo) : product.getDefaultImageUri()
                    );
            //Delegating to the Presenter to handle the event
            mPresenter.editProduct(product.getId(), activityOptionsCompat);
        }

        /**
         * Callback Method of {@link ProductListUserActionsListener} invoked when
         * the user clicks on "Delete" button. This should delete the Product
         * identified by {@link ProductLite#mId}, from the database.
         *
         * @param itemPosition The adapter position of the Item clicked
         * @param product      The {@link ProductLite} associated with the Item clicked
         */
        @Override
        public void onDeleteProduct(final int itemPosition, ProductLite product) {
            //Delegating to the Presenter to handle the event
            mPresenter.deleteProduct(product);
        }
    }

}
