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

package com.example.kaushiknsanji.storeapp.ui.products.image;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;
import com.example.kaushiknsanji.storeapp.utils.InjectorUtility;

import java.util.ArrayList;

/**
 * Activity that inflates the layout 'R.layout.activity_product_image' that
 * displays a content fragment inflated by {@link ProductImageActivityFragment}.
 * This allows to capture/store the Images of the Product in the Android Device
 * and their corresponding File Content URIs in the database. Also, one needs to select the
 * default Image for the Product, to be displayed.
 *
 * @author Kaushik N Sanji
 */
public class ProductImageActivity extends AppCompatActivity
        implements PhotoGridDeleteModeListener, ProductImageNavigator, SelectedPhotoActionsListener {

    //Request code used by the activity that calls this activity for result
    public static final int REQUEST_PRODUCT_IMAGE = 30;

    //Intent Extra constant for retrieving the Product Images from the Parent ProductConfigActivity
    public static final String EXTRA_PRODUCT_IMAGES = ProductImageActivity.class.getPackage() + "extra.PRODUCT_IMAGES";

    //The Presenter for this View's Content Fragment
    private ProductImageContract.Presenter mPresenter;

    //The Fab button to add product images
    private FloatingActionButton mFabAddImages;

    //The ImageView for the selected photo
    private ImageView mImageViewSelectedPhoto;

    //The App Bar to expand and collapse the Photo shown
    private AppBarLayout mAppBarLayout;

    //Tracks current Contextual ActionMode
    private ActionMode mActionMode;

    //Boolean to postpone/start the Shared Element enter transition
    private boolean mIsEnterTransitionPostponed;

    //The Callback for Contextual ActionMode
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        //Saves whether the delete action was handled
        private boolean mDeleteEventHandled = false;

        /**
         * Called when action mode is first created. The menu supplied will be used to
         * generate action buttons for the action mode.
         *
         * @param mode ActionMode being created
         * @param menu Menu used to populate action buttons
         * @return true if the action mode should be created, false if entering this
         *              mode should be aborted.
         */
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            //Set the title for the ActionMode
            mode.setTitle(getString(R.string.product_image_contextual_action_delete_title));
            //Inflate the Contextual Action Menu 'R.menu.cab_menu_fragment_product_image'
            mode.getMenuInflater().inflate(R.menu.cab_menu_fragment_product_image, menu);
            //Hide the Fab button to avoid confusion
            mFabAddImages.hide();
            //Returning true to create the action mode
            return true;
        }

        /**
         * Called to refresh an action mode's action menu whenever it is invalidated.
         *
         * @param mode ActionMode being prepared
         * @param menu Menu used to populate action buttons
         * @return true if the menu or action mode was updated, false otherwise.
         */
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        /**
         * Called to report a user click on an action button.
         *
         * @param mode The current ActionMode
         * @param item The item that was clicked
         * @return true if this callback handled the event, false if the standard MenuItem
         *         invocation should continue.
         */
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            //Taking action based on the ID of the Menu item clicked
            switch (item.getItemId()) {
                case R.id.action_delete:
                    //Trigger the deletion of the items selected
                    mPresenter.deleteSelection();
                    //Set the delete event boolean to True
                    mDeleteEventHandled = true;
                    //Close the contextual menu
                    mode.finish();
                    //Returning true as the event is handled
                    return mDeleteEventHandled;
                default:
                    //On all else, return false
                    return false;
            }
        }

        /**
         * Called when an action mode is about to be exited and destroyed.
         *
         * @param mode The current ActionMode being destroyed
         */
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            //Clear current ActionMode
            mActionMode = null;
            //Show the Fab button
            mFabAddImages.show();

            if (!mDeleteEventHandled) {
                //Reset and clear the selected state if the delete event was NOT handled
                mPresenter.onDeleteModeExit();
            } else {
                //Reset the event when handled
                mDeleteEventHandled = false;
            }
        }
    };

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
        setContentView(R.layout.activity_product_image);
        supportPostponeEnterTransition();

        //Find the AppBar
        mAppBarLayout = findViewById(R.id.app_bar_product_image);
        //Find the ImageView for Selected Photo
        mImageViewSelectedPhoto = findViewById(R.id.image_product_selected_item_photo);

        //Initialize Toolbar
        setupToolbar();

        //Initialize Fab
        setupFab();

        //Get the Product Images passed in the Intent Extra
        ArrayList<ProductImage> productImages = getIntent().getParcelableArrayListExtra(EXTRA_PRODUCT_IMAGES);
        if (productImages == null) {
            //Ensuring the list is initialized when not
            productImages = new ArrayList<>();
        }

        //Initialize Content Fragment
        ProductImageActivityFragment contentFragment = obtainContentFragment(productImages);

        //Initialize Presenter
        mPresenter = obtainPresenter(contentFragment);
    }

    /**
     * Method that creates and returns the instance of the Presenter that implements {@link ProductImageContract.Presenter}
     *
     * @param contentFragment The Content Fragment of this Activity that implements {@link ProductImageContract.View}
     * @return Instance of {@link ProductImagePresenter} that implements {@link ProductImageContract.Presenter}
     */
    @NonNull
    private ProductImageContract.Presenter obtainPresenter(ProductImageActivityFragment contentFragment) {
        return new ProductImagePresenter(
                InjectorUtility.provideStoreRepository(this),
                contentFragment,
                this,
                this,
                this
        );
    }

    /**
     * Method that initializes the Activity's Content Fragment {@link ProductImageActivityFragment}
     *
     * @param productImages ArrayList of Product Images {@link ProductImage}
     * @return Instance of the Content Fragment {@link ProductImageActivityFragment}
     */
    private ProductImageActivityFragment obtainContentFragment(ArrayList<ProductImage> productImages) {
        //Retrieving the Fragment Manager
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        //Finding if the Content Fragment is present at the Id
        ProductImageActivityFragment fragment
                = (ProductImageActivityFragment) supportFragmentManager.findFragmentById(R.id.content_product_image);
        if (fragment == null) {
            //Create and add the Fragment when not present
            fragment = ProductImageActivityFragment.newInstance(productImages);
            supportFragmentManager.beginTransaction()
                    .add(R.id.content_product_image, fragment)
                    .commit();
        }
        //Returning the Content Fragment instance
        return fragment;
    }

    /**
     * Method that initializes the FAB of the Activity with its listener.
     */
    private void setupFab() {
        //Finding the FAB
        mFabAddImages = findViewById(R.id.fab_product_image);
        //Setting the Click Listener to launch ProductImagePickerDialogFragment when clicked
        mFabAddImages.setOnClickListener(view -> mPresenter.openImagePickerDialog());
        //Setting the FAB Background Color
        mFabAddImages.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.productImageFabColor)));
    }

    /**
     * Method that initializes the Toolbar
     */
    private void setupToolbar() {
        //Find the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_product_image);
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
        //Discard the ActionMode if active
        if (mActionMode != null) {
            mActionMode.finish();
            return;
        }

        //On all else, propagating the call to the Presenter to do the required action
        mPresenter.onUpOrBackAction();
    }

    /**
     * Callback Method of {@link PhotoGridDeleteModeListener} invoked when
     * the user Long clicks on an item in the RecyclerView that displays a Grid of Photos.
     */
    @Override
    public void onGridItemDeleteMode() {
        //Start the Contextual Action Mode for delete when not yet started
        if (mActionMode == null) {
            mActionMode = startSupportActionMode(mActionModeCallback);
        }
    }

    /**
     * Callback Method of {@link PhotoGridDeleteModeListener} invoked when
     * the user clicks on items in DELETE Action Mode to select them for Delete.
     * <p>
     * This Method should show the count of Items {@code itemCount} currently selected for Delete.
     * </p>
     *
     * @param itemCount The Number of Image items currently selected for Delete Action.
     */
    @Override
    public void showSelectedItemCount(int itemCount) {
        //When DELETE Action is initialized
        if (mActionMode != null) {
            mActionMode.setSubtitle(getString(R.string.product_image_contextual_action_delete_live_count, itemCount));
        }
    }

    /**
     * Method that updates the result {@code productImages} to be sent back to the Calling activity.
     *
     * @param productImages List of {@link ProductImage}, each of which holds the URI information
     *                      of the Image File.
     */
    @Override
    public void doSetResult(ArrayList<ProductImage> productImages) {
        //Build the Result Intent passing in the current ProductImages
        Intent resultIntent = new Intent();
        resultIntent.putParcelableArrayListExtra(EXTRA_PRODUCT_IMAGES, productImages);
        setResult(RESULT_OK, resultIntent);

        //Finish the current activity
        finish();
    }

    /**
     * Callback Method of {@link SelectedPhotoActionsListener} invoked to display the
     * default image for the Product.
     */
    @Override
    public void showDefaultImage() {
        //Setting the Default Image
        mImageViewSelectedPhoto.setImageResource(R.drawable.ic_all_product_default);
        //Expanding the AppBar to reveal the Photo
        mAppBarLayout.setExpanded(true);
        //Setting the Transition Name on the ImageView for Shared Element Transition
        ViewCompat.setTransitionName(mImageViewSelectedPhoto, getString(R.string.transition_name_product_photo));
        if (mIsEnterTransitionPostponed) {
            //Start the Postponed transition if it was postponed
            supportStartPostponedEnterTransition();
        }
    }

    /**
     * Callback Method of {@link SelectedPhotoActionsListener} invoked to display the
     * selected Image {@code bitmap} for the Product.
     *
     * @param bitmap   The {@link Bitmap} of the Image to be shown.
     * @param imageUri The String Content URI of the Image to be shown.
     */
    @Override
    public void showSelectedImage(Bitmap bitmap, String imageUri) {
        //Setting the Transition Name on the ImageView for Shared Element Transition
        ViewCompat.setTransitionName(mImageViewSelectedPhoto, imageUri);
        //Updating the ImageView to show the Bitmap selected
        mImageViewSelectedPhoto.setImageBitmap(bitmap);
        //Expanding the AppBar to reveal the Photo
        mAppBarLayout.setExpanded(true);
        if (mIsEnterTransitionPostponed) {
            //Start the Postponed transition if it was postponed
            supportStartPostponedEnterTransition();
        }
    }
}
