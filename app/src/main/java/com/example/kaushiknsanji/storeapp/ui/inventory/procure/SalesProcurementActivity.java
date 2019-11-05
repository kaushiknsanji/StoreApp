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

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductSupplierSales;
import com.example.kaushiknsanji.storeapp.ui.products.config.DefaultPhotoChangeListener;
import com.example.kaushiknsanji.storeapp.utils.InjectorUtility;
import com.example.kaushiknsanji.storeapp.workers.ImageDownloaderFragment;

/**
 * Activity that inflates the layout 'R.layout.activity_sales_procurement' which
 * displays a content fragment inflated by {@link SalesProcurementActivityFragment}.
 * This allows to Dial/Email to the Supplier for placing product procurement request with the Supplier.
 *
 * @author Kaushik N Sanji
 */
public class SalesProcurementActivity extends AppCompatActivity implements SalesProcurementNavigator, DefaultPhotoChangeListener {

    //Intent Extra constant for retrieving the Product Name from the Parent SalesConfigActivity
    public static final String EXTRA_PRODUCT_NAME = SalesProcurementActivity.class.getPackage() + "extra.PRODUCT_NAME";
    //Intent Extra constant for retrieving the Product SKU from the Parent SalesConfigActivity
    public static final String EXTRA_PRODUCT_SKU = SalesProcurementActivity.class.getPackage() + "extra.PRODUCT_SKU";
    //Intent Extra constant for retrieving the Default Image set for the Product from the Parent SalesConfigActivity
    public static final String EXTRA_PRODUCT_IMAGE_DEFAULT = SalesProcurementActivity.class.getPackage() + "extra.PRODUCT_IMAGE_DEFAULT";
    //Intent Extra constant for retrieving the Product Supplier Sales data from the Parent SalesConfigActivity
    public static final String EXTRA_PRODUCT_SUPPLIER_SALES = SalesProcurementActivity.class.getPackage() + "extra.PRODUCT_SUPPLIER_SALES";

    //The Presenter for this View's Content Fragment
    private SalesProcurementContract.Presenter mPresenter;

    //The ImageView to show the default photo of the Product
    private ImageView mImageViewItemPhoto;

    //Boolean to postpone/start the Shared Element enter transition
    private boolean mIsEnterTransitionPostponed;

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
        setContentView(R.layout.activity_sales_procurement);

        //Finding the ImageView to load the Product Photo
        mImageViewItemPhoto = findViewById(R.id.image_sales_procurement_item_photo);

        //Retrieving the Product Name
        String productName = getIntent().getStringExtra(EXTRA_PRODUCT_NAME);
        //Retrieving the Product SKU
        String productSku = getIntent().getStringExtra(EXTRA_PRODUCT_SKU);
        //Retrieving the Default Product Image
        ProductImage productImage = getIntent().getParcelableExtra(EXTRA_PRODUCT_IMAGE_DEFAULT);
        //Retrieving the Product Sales data
        ProductSupplierSales productSupplierSales = getIntent().getParcelableExtra(EXTRA_PRODUCT_SUPPLIER_SALES);

        if (productSupplierSales == null) {
            //When the ProductSupplierSales is null, finish the Activity
            doFinish();
        }

        //Initialize Toolbar
        setupToolbar();

        //Initialize Content Fragment
        SalesProcurementActivityFragment contentFragment = obtainContentFragment();

        //Initialize Presenter
        mPresenter = obtainPresenter(productName, productSku, productImage, productSupplierSales, contentFragment);
    }

    /**
     * Method that creates and returns the instance of the Presenter that implements {@link SalesProcurementContract.Presenter}
     *
     * @param productName          The Name of the Product to procure.
     * @param productSku           The SKU of the Product to procure.
     * @param productImage         The defaulted {@link ProductImage} of the Product.
     * @param productSupplierSales The Sales details {@link ProductSupplierSales} of the Product.
     * @param contentFragment      The Content Fragment of this Activity that implements {@link SalesProcurementContract.View}
     * @return Instance of {@link SalesProcurementPresenter} that implements {@link SalesProcurementContract.Presenter}
     */
    @NonNull
    private SalesProcurementContract.Presenter obtainPresenter(String productName,
                                                               String productSku,
                                                               ProductImage productImage,
                                                               ProductSupplierSales productSupplierSales,
                                                               SalesProcurementActivityFragment contentFragment) {
        return new SalesProcurementPresenter(
                InjectorUtility.provideStoreRepository(this),
                contentFragment,
                productName, productSku, productImage, productSupplierSales,
                this,
                this
        );
    }

    /**
     * Method that initializes the Activity's Content Fragment {@link SalesProcurementActivityFragment}
     *
     * @return Instance of the Content Fragment {@link SalesProcurementActivityFragment}
     */
    private SalesProcurementActivityFragment obtainContentFragment() {
        //Retrieving the Fragment Manager
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        //Finding if the Content Fragment is present at the Id
        SalesProcurementActivityFragment fragment
                = (SalesProcurementActivityFragment) supportFragmentManager.findFragmentById(R.id.content_sales_procurement);
        if (fragment == null) {
            //Create and add the Fragment when not present
            fragment = SalesProcurementActivityFragment.newInstance();
            supportFragmentManager.beginTransaction()
                    .add(R.id.content_sales_procurement, fragment)
                    .commit();
        }
        //Returning the Content Fragment instance
        return fragment;
    }

    /**
     * Method that initializes the Toolbar and its Title
     */
    private void setupToolbar() {
        //Find the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_sales_procurement);
        //Set the Title
        toolbar.setTitle(R.string.sales_procurement_title);

        //Set the custom toolbar as ActionBar
        setSupportActionBar(toolbar);

        //Enable the Up button navigation
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            //Enabling the home button for Up Action
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            //Changing the Up button icon to close
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_all_close);
        }
    }

    /**
     * Support library version of {@link Activity#postponeEnterTransition()} that works
     * only on API 21 and later.
     */
    @Override
    public void supportPostponeEnterTransition() {
        super.supportPostponeEnterTransition();
        //Marking that the transition has been postponed
        mIsEnterTransitionPostponed = true;
    }

    /**
     * Support library version of {@link Activity#startPostponedEnterTransition()}
     * that only works with API 21 and later.
     */
    @Override
    public void supportStartPostponedEnterTransition() {
        super.supportStartPostponedEnterTransition();
        //Marking that the postponed transition has been started
        mIsEnterTransitionPostponed = false;
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
                mPresenter.doFinish();
                return true;
            default:
                //On other cases, do the default menu handling
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Method that finishes the current activity and returns back to the calling activity
     */
    @Override
    public void doFinish() {
        supportFinishAfterTransition();
    }

    /**
     * Callback Method of {@link DefaultPhotoChangeListener} invoked when there are
     * no selected Product Images for the Product.
     * <p>In this case, the View needs to show the default Image for the Product instead.</p>
     */
    @Override
    public void showDefaultImage() {
        //Set the Default Image
        mImageViewItemPhoto.setImageResource(R.drawable.ic_all_product_default);
        //Setting the Transition Name on the ImageView for Shared Element Transition
        ViewCompat.setTransitionName(mImageViewItemPhoto, getString(R.string.transition_name_product_photo));
        if (mIsEnterTransitionPostponed) {
            //Start the Postponed transition if it was postponed
            supportStartPostponedEnterTransition();
        }
    }

    /**
     * Callback Method of {@link DefaultPhotoChangeListener} invoked to display the selected
     * Image of the Product pointed to by the Image Content URI {@code imageUri}.
     *
     * @param imageUri The String Content URI of the Image to be shown.
     */
    @Override
    public void showSelectedProductImage(String imageUri) {
        //Setting the Transition Name on the ImageView for Shared Element Transition
        ViewCompat.setTransitionName(mImageViewItemPhoto, imageUri);
        //Load the Selected Image for the Product
        ImageDownloaderFragment.newInstance(getSupportFragmentManager(), mImageViewItemPhoto.getId())
                .setOnSuccessListener(bitmap -> {
                    if (mIsEnterTransitionPostponed) {
                        //Start the Postponed transition if it was postponed
                        supportStartPostponedEnterTransition();
                    }
                })
                .executeAndUpdate(mImageViewItemPhoto, imageUri, mImageViewItemPhoto.getId(), getSupportLoaderManager());
    }
}
