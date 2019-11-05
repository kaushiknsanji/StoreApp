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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductLite;
import com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigActivity;
import com.example.kaushiknsanji.storeapp.ui.suppliers.product.SupplierProductPickerActivity;
import com.example.kaushiknsanji.storeapp.utils.InjectorUtility;

import java.util.ArrayList;

/**
 * Activity that inflates the layout 'R.layout.activity_supplier_config' which
 * displays a content fragment inflated by {@link SupplierConfigActivityFragment}.
 * This allows to configure a New Supplier in the database and also edit an existing Supplier.
 *
 * @author Kaushik N Sanji
 */
public class SupplierConfigActivity extends AppCompatActivity implements SupplierConfigNavigator {

    //Request codes used by the activity that calls this activity for result
    public static final int REQUEST_ADD_SUPPLIER = 40; //41 is reserved for the result of this request
    public static final int REQUEST_EDIT_SUPPLIER = 42; //43, 44 are reserved for the results of this request

    //Custom Result Codes for Add operation
    public static final int RESULT_ADD_SUPPLIER = REQUEST_ADD_SUPPLIER + RESULT_FIRST_USER;
    //Custom Result Codes for Edit(43) and Delete(44) operations
    public static final int RESULT_EDIT_SUPPLIER = REQUEST_EDIT_SUPPLIER + RESULT_FIRST_USER;
    public static final int RESULT_DELETE_SUPPLIER = RESULT_EDIT_SUPPLIER + RESULT_FIRST_USER;

    //Intent Extra constant for retrieving the Supplier ID from the Parent SupplierListFragment
    public static final String EXTRA_SUPPLIER_ID = SupplierConfigActivity.class.getPackage() + "extra.SUPPLIER_ID";

    //Intent Extra constant for passing the Result information of Supplier ID to the Calling Activity
    public static final String EXTRA_RESULT_SUPPLIER_ID = SupplierConfigActivity.class.getPackage() + "extra.SUPPLIER_ID";
    //Intent Extra constant for passing the Result information of Supplier Code to the Calling Activity
    public static final String EXTRA_RESULT_SUPPLIER_CODE = SupplierConfigActivity.class.getPackage() + "extra.SUPPLIER_CODE";

    //The Presenter for this View's Content Fragment
    private SupplierConfigContract.Presenter mPresenter;

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
        setContentView(R.layout.activity_supplier_config);

        //Get the Supplier Id if passed for editing
        int supplierId = getIntent().getIntExtra(EXTRA_SUPPLIER_ID, SupplierConfigContract.NEW_SUPPLIER_INT);

        //Initialize Toolbar
        setupToolbar(supplierId);

        //Initialize Content Fragment
        SupplierConfigActivityFragment contentFragment = obtainContentFragment(supplierId);

        //Initialize Presenter
        mPresenter = obtainPresenter(supplierId, contentFragment);
    }

    /**
     * Method that creates and returns the instance of the Presenter that implements {@link SupplierConfigContract.Presenter}
     *
     * @param supplierId      The Integer value of the Supplier Id of an existing Supplier;
     *                        or {@link SupplierConfigContract#NEW_SUPPLIER_INT} if it is
     *                        for a New Supplier Entry.
     * @param contentFragment The Content Fragment of this Activity that implements {@link SupplierConfigContract.View}.
     * @return Instance of the {@link SupplierConfigPresenter} that implements {@link SupplierConfigContract.Presenter}
     */
    @NonNull
    private SupplierConfigContract.Presenter obtainPresenter(int supplierId, SupplierConfigActivityFragment contentFragment) {
        return new SupplierConfigPresenter(
                supplierId,
                InjectorUtility.provideStoreRepository(this),
                contentFragment,
                this
        );
    }

    /**
     * Method that initializes the Activity's Content Fragment {@link SupplierConfigActivityFragment}
     *
     * @param supplierId The Integer value of the Supplier Id of an existing Supplier;
     *                   or {@link SupplierConfigContract#NEW_SUPPLIER_INT} if it is
     *                   for a New Supplier Entry.
     * @return Instance of the Content Fragment {@link SupplierConfigActivityFragment}
     */
    private SupplierConfigActivityFragment obtainContentFragment(int supplierId) {
        //Retrieving the Fragment Manager
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        //Finding if the Content Fragment is present at the Id
        SupplierConfigActivityFragment fragment = (SupplierConfigActivityFragment) supportFragmentManager.findFragmentById(R.id.content_supplier_config);
        if (fragment == null) {
            //Create and add the Fragment at the Id when not present
            fragment = SupplierConfigActivityFragment.newInstance(supplierId);
            supportFragmentManager.beginTransaction()
                    .add(R.id.content_supplier_config, fragment)
                    .commit();
        }
        //Returning the Content Fragment instance
        return fragment;
    }

    /**
     * Method that initializes the Toolbar and its title based
     * on the {@code supplierId} passed
     *
     * @param supplierId The Integer value of the Supplier Id of an existing Supplier;
     *                   or {@link SupplierConfigContract#NEW_SUPPLIER_INT} if it is
     *                   for a New Supplier Entry.
     */
    public void setupToolbar(int supplierId) {
        //Find the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_supplier_config);
        //Set the Title based on whether the request is for Add/Edit Supplier
        if (supplierId == SupplierConfigContract.NEW_SUPPLIER_INT) {
            //For New Supplier Entry
            toolbar.setTitle(R.string.supplier_config_title_add_supplier);
        } else {
            //For Editing Existing Supplier
            toolbar.setTitle(R.string.supplier_config_title_edit_supplier);
        }

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
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        //Propagating the call to the Presenter to do the required action
        mPresenter.onUpOrBackAction();
    }

    /**
     * Method that updates the result {@code resultCode} to be sent back to the Calling Activity
     *
     * @param resultCode   The integer result code to be returned to the Calling Activity.
     * @param supplierId   Integer containing the Id of the Supplier involved.
     * @param supplierCode String containing the Supplier Code of the Supplier involved.
     */
    @Override
    public void doSetResult(int resultCode, int supplierId, @NonNull String supplierCode) {
        //Build the Result Intent and finish the Activity
        Intent resultIntent = new Intent();
        //Passing the Supplier ID
        resultIntent.putExtra(EXTRA_RESULT_SUPPLIER_ID, supplierId);
        //Passing the Supplier Code
        resultIntent.putExtra(EXTRA_RESULT_SUPPLIER_CODE, supplierCode);
        setResult(resultCode, resultIntent);

        //Finish the current activity
        supportFinishAfterTransition();
    }

    /**
     * Method that updates the Calling Activity that the operation was aborted.
     */
    @Override
    public void doCancel() {
        //Pass Result for Cancel and finish the Activity
        setResult(RESULT_CANCELED);

        //Finish the current activity
        supportFinishAfterTransition();
    }

    /**
     * Method invoked when the user clicks on any Item View of the Products sold by the Supplier. This should
     * launch the {@link ProductConfigActivity} for the Product to be edited.
     *
     * @param productId             The Primary Key of the Product to be edited.
     * @param activityOptionsCompat Instance of {@link ActivityOptionsCompat} that has the
     *                              details for Shared Element Transition
     */
    @Override
    public void launchEditProduct(int productId, ActivityOptionsCompat activityOptionsCompat) {
        //Creating the Intent to launch ProductConfigActivity
        Intent productConfigIntent = new Intent(this, ProductConfigActivity.class);
        //Passing in the Product ID of the Product to be edited
        productConfigIntent.putExtra(ProductConfigActivity.EXTRA_PRODUCT_ID, productId);
        //Starting the Activity with Result
        ActivityCompat.startActivityForResult(this, productConfigIntent, ProductConfigActivity.REQUEST_EDIT_PRODUCT, activityOptionsCompat.toBundle());
    }

    /**
     * Method invoked when the user clicks on the "Add Item" button, present under "Supplier Items"
     * to add/link items to the Supplier. This should launch the
     * {@link SupplierProductPickerActivity} to pick the Products for the Supplier to sell.
     *
     * @param productLiteList ArrayList of Products {@link ProductLite} already picked for the Supplier to sell.
     */
    @Override
    public void launchPickProducts(ArrayList<ProductLite> productLiteList) {
        //Creating an Intent to launch the SupplierProductPickerActivity
        Intent productPickerIntent = new Intent(this, SupplierProductPickerActivity.class);
        //Passing the Product list already registered for selling
        productPickerIntent.putExtra(SupplierProductPickerActivity.EXTRA_SUPPLIER_PRODUCTS, productLiteList);
        //Starting the activity with result
        startActivityForResult(productPickerIntent, SupplierProductPickerActivity.REQUEST_SUPPLIER_PRODUCTS);
    }

    /**
     * Receive the result from a previous call to
     * {@link #startActivityForResult(Intent, int)}.
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

}
