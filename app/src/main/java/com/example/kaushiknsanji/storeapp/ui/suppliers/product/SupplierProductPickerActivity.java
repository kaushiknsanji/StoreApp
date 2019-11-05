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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductLite;
import com.example.kaushiknsanji.storeapp.utils.InjectorUtility;

import java.util.ArrayList;

/**
 * Activity that inflates the layout 'R.layout.activity_supplier_product_picker' which
 * displays a content fragment inflated by {@link SupplierProductPickerActivityFragment}.
 * This allows to search and pick the Products to be sold by the Supplier.
 *
 * @author Kaushik N Sanji
 */
public class SupplierProductPickerActivity extends AppCompatActivity
        implements SearchView.OnQueryTextListener,
        SupplierProductPickerNavigator, SupplierProductPickerMultiSelectListener, SupplierProductPickerSearchActionsListener {

    //Request codes used by the activity that calls this activity for result
    public static final int REQUEST_SUPPLIER_PRODUCTS = 50; //51 is reserved for the result of this request
    //Result code of the request
    public static final int RESULT_SUPPLIER_PRODUCTS = REQUEST_SUPPLIER_PRODUCTS + RESULT_FIRST_USER;
    //Intent Extra constant for retrieving the list of Supplier Products
    //from the Parent SupplierConfigActivityFragment
    public static final String EXTRA_SUPPLIER_PRODUCTS = SupplierProductPickerActivity.class.getPackage() + "extra.SUPPLIER_PRODUCTS";

    //Bundle constants for persisting the data through System config changes
    private static final String BUNDLE_SEARCH_QUERY_STR_KEY = "SupplierProductPicker.SearchQuery";

    //The Presenter for this View's Content Fragment
    private SupplierProductPickerContract.Presenter mPresenter;

    //The SearchView to search the Products
    private SearchView mSearchView;

    //Saves the ongoing and submitted Search Query
    private String mSearchQueryStr;
    //Saves the threshold value of minimum number of characters for triggering the search
    private int mSearchStartThreshold;

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Inflating the activity's layout
        setContentView(R.layout.activity_supplier_product_picker);

        //Get the Supplier's Products passed in the Intent Extra
        ArrayList<ProductLite> supplierProducts = getIntent().getParcelableArrayListExtra(EXTRA_SUPPLIER_PRODUCTS);
        if (supplierProducts == null) {
            //Ensuring the list is initialized when not
            supplierProducts = new ArrayList<>();
        }

        //Initialize Toolbar
        setupToolbar();

        //Initialize Content Fragment
        SupplierProductPickerActivityFragment contentFragment = obtainContentFragment(supplierProducts);

        //Initialize Presenter
        mPresenter = obtainPresenter(contentFragment);

        //Initialize SearchView
        setupSearchView();
    }

    /**
     * Called to retrieve per-instance state from an activity before being killed
     * so that the state can be restored in {@link #onCreate} or
     * {@link #onRestoreInstanceState} (the {@link Bundle} populated by this method
     * will be passed to both).
     * <p>
     * <p>If called, this method will occur before {@link #onStop}.  There are
     * no guarantees about whether it will occur before or after {@link #onPause}.
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Saving the Ongoing/Submitted Search Query
        outState.putString(BUNDLE_SEARCH_QUERY_STR_KEY, mSearchQueryStr);
    }

    /**
     * This method is called after {@link #onStart} when the activity is
     * being re-initialized from a previously saved state, given here in
     * <var>savedInstanceState</var>.  Most implementations will simply use {@link #onCreate}
     * to restore their state, but it is sometimes convenient to do it here
     * after all of the initialization has been done or to allow subclasses to
     * decide whether to use your default implementation.  The default
     * implementation of this method performs a restore of any view state that
     * had previously been frozen by {@link #onSaveInstanceState}.
     * <p>
     * <p>This method is called between {@link #onStart} and
     * {@link #onPostCreate}.
     *
     * @param savedInstanceState the data most recently supplied in {@link #onSaveInstanceState}.
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //Restoring the Ongoing/Submitted Search Query
        mSearchQueryStr = savedInstanceState.getString(BUNDLE_SEARCH_QUERY_STR_KEY);
    }

    /**
     * This is the fragment-orientated version of {@link #onResume()} that you
     * can override to perform operations in the Activity at the same point
     * where its fragments are resumed.  Be sure to always call through to
     * the super-class.
     */
    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        //Trigger the Search Query when previously entered and is greater than the search trigger threshold
        if (!TextUtils.isEmpty(mSearchQueryStr) && mSearchQueryStr.length() >= mSearchStartThreshold) {
            mSearchView.setQuery(mSearchQueryStr, true);
        }
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen.</p>
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handling based on the Menu item selected
        switch (item.getItemId()) {
            case android.R.id.home:
                //For android home/up button

                //Propagating the call to the Presenter to do the required action
                mPresenter.onUpOrBackAction();
                return true;
            default:
                //On other cases, do the default menu handling
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Method that creates and returns the instance of the Presenter for the View
     * {@link SupplierProductPickerActivityFragment}
     *
     * @param contentFragment {@link SupplierProductPickerActivityFragment} instance which is the
     *                        Content Fragment of this Activity participating in MVP.
     * @return Instance of the Presenter implementing {@link SupplierProductPickerContract.Presenter}
     */
    private SupplierProductPickerContract.Presenter obtainPresenter(SupplierProductPickerActivityFragment contentFragment) {
        return new SupplierProductPickerPresenter(
                InjectorUtility.provideStoreRepository(this),
                contentFragment,
                this,
                this,
                this
        );
    }

    /**
     * Method that initializes the Toolbar.
     */
    private void setupToolbar() {
        //Find the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_supplier_product_picker);

        //Set the custom toolbar as ActionBar
        setSupportActionBar(toolbar);

        //Enable the Up button navigation
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            //Enabling the home button for Up Action
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Method that creates/obtains the instance of {@link SupplierProductPickerActivityFragment}
     * which is the Content Fragment of this Activity.
     *
     * @param supplierProducts The Supplier's list of Products {@link ProductLite} passed via the
     *                         Intent by the Calling Activity.
     * @return Instance of {@link SupplierProductPickerActivityFragment}
     */
    private SupplierProductPickerActivityFragment obtainContentFragment(ArrayList<ProductLite> supplierProducts) {
        //Retrieving the FragmentManager instance
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        //Looking up for the Content Fragment instance if any
        SupplierProductPickerActivityFragment fragment
                = (SupplierProductPickerActivityFragment) supportFragmentManager.findFragmentById(R.id.content_supplier_product_picker);
        if (fragment == null) {
            //Create and add the Fragment at the Id when not present
            fragment = SupplierProductPickerActivityFragment.newInstance(supplierProducts);
            supportFragmentManager.beginTransaction()
                    .add(R.id.content_supplier_product_picker, fragment)
                    .commit();
        }
        //Returning the instance of Content Fragment
        return fragment;
    }

    /**
     * Method that initializes the {@link SearchView} and registers the listeners required
     */
    private void setupSearchView() {
        //Finding the SearchView
        mSearchView = findViewById(R.id.search_view_supplier_product_picker);
        //Registering the OnQueryTextListener
        mSearchView.setOnQueryTextListener(this);
        //Reading the threshold value of minimum number of characters for triggering the search
        mSearchStartThreshold = getResources().getInteger(R.integer.supplier_config_picker_search_start_threshold);
    }

    /**
     * Called when the user submits the query. This could be due to a key press on the
     * keyboard or due to pressing a submit button.
     * The listener can override the standard behavior by returning true
     * to indicate that it has handled the submit request. Otherwise return false to
     * let the SearchView handle the submission by launching any associated intent.
     *
     * @param query the query text that is to be submitted
     * @return true if the query has been handled by the listener, false to let the
     * SearchView perform the default action.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        if (mSearchView.isEnabled()) {
            //Performing search only when SearchView is enabled

            //Saving the Query submitted
            mSearchQueryStr = query;
            //Delegating to the Presenter to filter the results for the Search submitted
            mPresenter.filterResults(mSearchQueryStr);
            //Clearing the focus on SearchView
            mSearchView.clearFocus();
        }

        //Returning True since we have handled the event
        return true;
    }

    /**
     * Called when the query text is changed by the user.
     *
     * @param newText the new content of the query text field.
     * @return false if the SearchView should perform the default action of showing any
     * suggestions if available, true if the action was handled by the listener.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        if (mSearchView.isEnabled()) {
            //Performing search only when SearchView is enabled

            //Saving the Ongoing Query
            mSearchQueryStr = newText;

            if (mSearchQueryStr.length() >= mSearchStartThreshold) {
                //Delegating to the Presenter to start filtering the results when the
                //length is more than 3 characters
                mPresenter.filterResults(mSearchQueryStr);
            } else if (TextUtils.isEmpty(mSearchQueryStr)) {
                //Delegating to the Presenter to clear the filter when search is cleared or closed
                mPresenter.clearFilter();
            }
        }

        //Returning True since we have handled the event
        return true;
    }

    /**
     * Method that updates the result {@code productsToSell} to be sent back to the Calling activity.
     *
     * @param productsToSell List of Products {@link ProductLite} selected by the Supplier
     *                       for selling.
     */
    @Override
    public void doSetResult(ArrayList<ProductLite> productsToSell) {
        //Build the Result Intent and finish the Activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SUPPLIER_PRODUCTS, productsToSell);
        setResult(RESULT_SUPPLIER_PRODUCTS, resultIntent);

        //Finish the current activity
        finish();
    }

    /**
     * Method that updates the Calling Activity that the operation was aborted.
     */
    @Override
    public void doCancel() {
        //Pass Result for Cancel and finish the Activity
        setResult(RESULT_CANCELED);

        //Finish the current activity
        finish();
    }

    /**
     * Callback Method of {@link SupplierProductPickerMultiSelectListener} invoked to
     * display the number of Products {@code countOfProductsSelected} selected for the Supplier to sell.
     *
     * @param countOfProductsSelected The Number of Products selected/picked for the Supplier to sell.
     */
    @Override
    public void showSelectedCount(int countOfProductsSelected) {
        //Get the Support Action Bar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            //When we have the Action Bar

            //Set the Subtitle to display the count
            supportActionBar.setSubtitle(getString(R.string.supplier_product_picker_action_pick_live_count, countOfProductsSelected));
        }
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        //Propagating the call to the Presenter to do the required action
        mPresenter.onUpOrBackAction();
    }

    /**
     * Callback Method of {@link SupplierProductPickerSearchActionsListener} invoked when
     * all the Products available, are already picked for the Supplier. Hence the implementation
     * should disable the Search.
     */
    @Override
    public void disableSearch() {
        //Disabling the SearchView
        mSearchView.setEnabled(false);
    }
}
