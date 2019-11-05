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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.ui.products.image.ProductImageActivity;
import com.example.kaushiknsanji.storeapp.utils.InjectorUtility;
import com.example.kaushiknsanji.storeapp.workers.ImageDownloaderFragment;

import java.util.ArrayList;

/**
 * Activity that inflates the layout 'R.layout.activity_product_config' which
 * displays a content fragment inflated by {@link ProductConfigActivityFragment}.
 * This allows to configure a New Product in the database and also edit an existing Product.
 *
 * @author Kaushik N Sanji
 */
public class ProductConfigActivity extends AppCompatActivity
        implements ProductConfigNavigator, DefaultPhotoChangeListener, View.OnClickListener {

    //Request codes used by the activity that calls this activity for result
    public static final int REQUEST_ADD_PRODUCT = 20; //21 is reserved for the result of this request
    public static final int REQUEST_EDIT_PRODUCT = 22; //23, 24 are reserved for the result of this request

    //Custom Result Codes for Add operation
    public static final int RESULT_ADD_PRODUCT = REQUEST_ADD_PRODUCT + RESULT_FIRST_USER;
    //Custom Result Codes for Edit(23) and Delete(24) operations
    public static final int RESULT_EDIT_PRODUCT = REQUEST_EDIT_PRODUCT + RESULT_FIRST_USER;
    public static final int RESULT_DELETE_PRODUCT = RESULT_EDIT_PRODUCT + RESULT_FIRST_USER;

    //Intent Extra constant for retrieving the Product ID from the Parent ProductListFragment
    public static final String EXTRA_PRODUCT_ID = ProductConfigActivity.class.getPackage() + "extra.PRODUCT_ID";

    //Intent Extra constant for passing the Result information of Product Id to the Calling Activity
    public static final String EXTRA_RESULT_PRODUCT_ID = ProductConfigActivity.class.getPackage() + "extra.PRODUCT_ID";
    //Intent Extra constant for passing the Result information of Product SKU to the Calling Activity
    public static final String EXTRA_RESULT_PRODUCT_SKU = ProductConfigActivity.class.getPackage() + "extra.PRODUCT_SKU";

    //The Presenter for this View's Content Fragment
    private ProductConfigContract.Presenter mPresenter;

    //The ImageView to show the default photo of the Product
    private ImageView mImageViewItemPhoto;

    //The App Bar to expand and collapse the Photo shown
    private AppBarLayout mAppBarLayout;

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
        setContentView(R.layout.activity_product_config);
        //Delay the enter transition
        supportPostponeEnterTransition();

        //Find the AppBar
        mAppBarLayout = findViewById(R.id.app_bar_product_config);
        //Find the ImageView for the default photo of the Product
        mImageViewItemPhoto = findViewById(R.id.image_product_config_item_photo);

        //Get the Product ID if passed for editing
        int productId = getIntent().getIntExtra(EXTRA_PRODUCT_ID, ProductConfigContract.NEW_PRODUCT_INT);

        //Initialize Toolbar
        setupToolbar(productId);

        //Setting Click Listeners for the Views
        findViewById(R.id.image_product_config_add_photo).setOnClickListener(this);

        //Initialize Content Fragment
        ProductConfigActivityFragment contentFragment = obtainContentFragment(productId);

        //Initialize Presenter
        mPresenter = obtainPresenter(productId, contentFragment);
    }

    /**
     * Method that creates and returns the instance of the Presenter that implements {@link ProductConfigContract.Presenter}.
     *
     * @param productId       The integer value of the Product Id of an existing Product;
     *                        or {@link ProductConfigContract#NEW_PRODUCT_INT} for a New Product Entry.
     * @param contentFragment The Content Fragment of this Activity that implements {@link ProductConfigContract.View}
     * @return Instance of {@link ProductConfigPresenter} that implements {@link ProductConfigContract.Presenter}
     */
    @NonNull
    private ProductConfigContract.Presenter obtainPresenter(int productId, ProductConfigActivityFragment contentFragment) {
        return new ProductConfigPresenter(
                productId,
                InjectorUtility.provideStoreRepository(this),
                contentFragment,
                this,
                this
        );
    }

    /**
     * Method that initializes the Activity's Content Fragment {@link ProductConfigActivityFragment}.
     *
     * @param productId The integer value of the Product Id of an existing Product;
     *                  or {@link ProductConfigContract#NEW_PRODUCT_INT} for a New Product Entry.
     * @return Instance of the Content Fragment {@link ProductConfigActivityFragment}.
     */
    private ProductConfigActivityFragment obtainContentFragment(int productId) {
        //Retrieving the Fragment Manager
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        //Finding if the Content Fragment is present at the Id
        ProductConfigActivityFragment fragment
                = (ProductConfigActivityFragment) supportFragmentManager
                .findFragmentById(R.id.content_product_config);
        if (fragment == null) {
            //Create and add the Content Fragment when not present
            fragment = ProductConfigActivityFragment.newInstance(productId);
            supportFragmentManager.beginTransaction()
                    .add(R.id.content_product_config, fragment)
                    .commit();
        }
        //Returning the Content Fragment instance
        return fragment;
    }

    /**
     * Method that initializes the Toolbar and its title based
     * on the {@code productId} passed
     *
     * @param productId The integer value of the Product Id of an existing Product;
     *                  or {@link ProductConfigContract#NEW_PRODUCT_INT} for a New Product Entry.
     */
    private void setupToolbar(int productId) {
        //Find the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_product_config);
        //Set Title based on whether the request is for Add/Edit Product
        if (productId == ProductConfigContract.NEW_PRODUCT_INT) {
            //For New Product Entry
            toolbar.setTitle(R.string.product_config_title_add_product);
        } else {
            //For Editing Existing Product
            toolbar.setTitle(R.string.product_config_title_edit_product);
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
     * @param resultCode The integer result code to be returned to the Calling Activity.
     * @param productId  Integer containing the Id of the Product involved.
     * @param productSku String containing the SKU information of the Product involved.
     */
    @Override
    public void doSetResult(final int resultCode, final int productId, @NonNull final String productSku) {
        //Build the Result Intent and finish the Activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_RESULT_PRODUCT_ID, productId);
        resultIntent.putExtra(EXTRA_RESULT_PRODUCT_SKU, productSku);
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
     * Method that launches the {@link ProductImageActivity}
     * when the Add Photo button (R.id.image_product_config_add_photo) on the Product Image is clicked.
     *
     * @param productImages List of {@link ProductImage} that stores the URI details of the Images
     */
    @Override
    public void launchProductImagesView(ArrayList<ProductImage> productImages) {
        //Creating an Intent to launch ProductImageActivity
        Intent productImagesIntent = new Intent(this, ProductImageActivity.class);
        //Passing in the list of ProductImage data
        productImagesIntent.putParcelableArrayListExtra(ProductImageActivity.EXTRA_PRODUCT_IMAGES, productImages);
        //Creating ActivityOptions for Shared Element Transition where the ImageView is the Shared Element
        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                mImageViewItemPhoto,
                ViewCompat.getTransitionName(mImageViewItemPhoto)
        );
        //Starting the Activity with Result
        ActivityCompat.startActivityForResult(this, productImagesIntent,
                ProductImageActivity.REQUEST_PRODUCT_IMAGE, activityOptionsCompat.toBundle());
    }

    /**
     * Callback Method of {@link DefaultPhotoChangeListener} invoked when there are
     * no selected Product Images for the Product.
     * <p>In this case, the View needs to show the default Image for the Product instead.</p>
     */
    @Override
    public void showDefaultImage() {
        //Setting the Default Image
        mImageViewItemPhoto.setImageResource(R.drawable.ic_all_product_default);
        //Expanding the AppBar to reveal the Photo
        mAppBarLayout.setExpanded(true);
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
        //Expanding the AppBar to reveal the Photo
        mAppBarLayout.setExpanded(true);
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
            case R.id.image_product_config_add_photo:
                //For the Add Photo button on the Product Image

                //Dispatch to the Presenter to launch the ProductImageActivity with the ProductImages EXTRA
                mPresenter.openProductImages();
                break;
        }
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
