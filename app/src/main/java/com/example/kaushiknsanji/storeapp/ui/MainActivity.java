package com.example.kaushiknsanji.storeapp.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.cache.BitmapImageCache;
import com.example.kaushiknsanji.storeapp.ui.about.AboutActivity;
import com.example.kaushiknsanji.storeapp.ui.inventory.SalesListFragment;
import com.example.kaushiknsanji.storeapp.ui.products.ProductListFragment;
import com.example.kaushiknsanji.storeapp.ui.suppliers.SupplierListFragment;

/**
 * The Main Activity of the App that inflates the layout 'R.layout.activity_main'
 * containing the ViewPager that displays a list of Products, Suppliers and Sales information.
 *
 * @author Kaushik N Sanji
 */
public class MainActivity extends AppCompatActivity
        implements TabLayout.OnTabSelectedListener, View.OnClickListener {

    //For the ViewPager that displays the fragments for Products, Suppliers and Sales information
    private ViewPager mViewPager;
    //The Adapter for the ViewPager
    private MainPagerAdapter mPagerAdapter;
    //The TabLayout for the ViewPager
    private TabLayout mTabLayout;
    //The Common FAB Add button for "Products" and "Suppliers" Tab
    private FloatingActionButton mFabAdd;

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
        //Inflating the Activity's UI
        setContentView(R.layout.activity_main);

        //Initialize Toolbar
        setupToolbar();

        //Initialize ViewPager and its Adapter
        setupViewPager();

        //Find and Initialize the FAB
        setupFab();

        //Find TabLayout
        mTabLayout = findViewById(R.id.tablayout_main);
        //Setup TabLayout with ViewPager
        mTabLayout.setupWithViewPager(mViewPager);

        //Iterate over the Tabs to set the custom Tab view for each Tab
        int tabCount = mTabLayout.getTabCount();
        for (int tabIndex = 0; tabIndex < tabCount; tabIndex++) {
            //Get the current position Tab
            TabLayout.Tab tab = mTabLayout.getTabAt(tabIndex);
            if (tab != null) {
                //Inflate and get the Tab View for the current Tab
                View tabView = mPagerAdapter.getTabView(mViewPager, tabIndex);
                //Set the custom Tab View for the Tab
                tab.setCustomView(tabView);
                if (savedInstanceState == null && tabIndex == 0) {
                    //On Initial load, preselect the first page
                    mViewPager.setCurrentItem(tabIndex);
                    //Making the Title visible manually since the OnTabSelectedListener
                    //is not registered at this lifecycle state
                    mPagerAdapter.changeTabView(tab, true);
                    //Setting the Fab Color to use for the ProductListFragment
                    mFabAdd.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.mainProductListFabColor)));
                }
            }
        }

    }

    /**
     * This method is called after {@link #onStart} when the activity is
     * being re-initialized from a previously saved state, given here in
     * <var>savedInstanceState</var>.
     * <p>
     * <p>This method is called between {@link #onStart} and
     * {@link #onPostCreate}.
     *
     * @param savedInstanceState the data most recently supplied in {@link #onSaveInstanceState}.
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //Trigger the OnTabSelectedListener's selected event manually
        //to set the tab as selected
        onTabSelected(mTabLayout.getTabAt(mViewPager.getCurrentItem()));
    }

    /**
     * Method that initializes the Toolbar
     */
    private void setupToolbar() {
        //Finding the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        //Setting it as the Action Bar
        setSupportActionBar(toolbar);
    }

    /**
     * Method that initializes the ViewPager and its Adapter
     */
    private void setupViewPager() {
        //Finding Viewpager
        mViewPager = findViewById(R.id.viewpager_main);

        //Initializing Viewpager Adapter
        mPagerAdapter = new MainPagerAdapter(this, getSupportFragmentManager());

        //Attaching the Adapter to Viewpager
        mViewPager.setAdapter(mPagerAdapter);
    }

    /**
     * Method that initializes the FAB Add button
     */
    private void setupFab() {
        //Finding the FAB
        mFabAdd = findViewById(R.id.fab_main_add);

        //Registering the click listener on FAB
        mFabAdd.setOnClickListener(this);
    }

    /**
     * Called when a tab enters the selected state.
     *
     * @param tab The tab that was selected
     */
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        //Fix added to correct the Position pointed by the ViewPager: START
        //(Directly touching the tab instead of swiping can result in this)
        int newPosition = tab.getPosition();
        if (mViewPager.getCurrentItem() != newPosition) {
            //When position is incorrect, restore the position using the tab's position
            mViewPager.setCurrentItem(newPosition);
        }
        //Fix added to correct the Position pointed by the ViewPager: END

        //Making the title visible for the New Tab selected
        mPagerAdapter.changeTabView(tab, true);

        //Get the registered Fragment for the Position
        Fragment pagerFragment = mPagerAdapter.getRegisteredFragment(newPosition);

        if (pagerFragment instanceof SalesListFragment) {
            //When the Fragment is SalesListFragment, hide the FAB as there is no purpose for it
            mFabAdd.hide();
        } else {
            //When the Fragment is other than SalesListFragment, show the FAB since we need it to configure Products/Suppliers
            mFabAdd.show();

            //Setting the FAB Background Color based on the Fragment
            if (pagerFragment instanceof ProductListFragment) {
                mFabAdd.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.mainProductListFabColor)));
            } else if (pagerFragment instanceof SupplierListFragment) {
                mFabAdd.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.mainSupplierListFabColor)));
            }
        }
    }

    /**
     * Called when a tab exits the selected state.
     *
     * @param tab The tab that was unselected
     */
    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        //Hiding the Title for the Tab unselected
        mPagerAdapter.changeTabView(tab, false);
    }

    /**
     * Called when a tab that is already selected is chosen again by the user. Some applications
     * may use this action to return to the top level of a category.
     *
     * @param tab The tab that was reselected.
     */
    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        //no-op
    }

    /**
     * Called by the Activity when it is prepared to be shown
     */
    @Override
    protected void onResume() {
        super.onResume();

        //Registering the Listener on TabLayout
        mTabLayout.addOnTabSelectedListener(this);
    }

    /**
     * Called by the Activity when it loses focus
     */
    @Override
    protected void onPause() {
        super.onPause();

        //UnRegistering the Listener on TabLayout
        mTabLayout.removeOnTabSelectedListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param view The view that was clicked.
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_main_add:
                //For the FAB Add button
                fabAddButtonClicked();
                break;
        }
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflating the Menu options from 'R.menu.menu_main'
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //Returning true for the Menu to be displayed
        return true;
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
            case R.id.action_refresh:
                //On Click of Refresh Menu

                onRefreshMenuClicked();
                return true;
            case R.id.action_about:
                //On Click of About

                launchAboutActivity();
                return true;
            default:
                //On other cases, do the default menu handling
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Method that launches the {@link AboutActivity}. Invoked when "About" Menu is clicked.
     */
    private void launchAboutActivity() {
        //Creating an Intent to launch AboutActivity
        Intent aboutIntent = new Intent(this, AboutActivity.class);
        //Starting the Activity
        startActivity(aboutIntent);
    }

    /**
     * Method invoked when the FAB "+" button is clicked in the "Products"/"Suppliers" Tab.
     */
    private void fabAddButtonClicked() {
        //Retrieving the Fragment currently shown
        Fragment pagerFragment = getCurrentPagerFragment();

        if (pagerFragment != null) {
            //When we have a Fragment

            //Get the Presenter for the Pager Fragment
            Object presenter = ((PagerView) pagerFragment).getPresenter();

            if (presenter != null) {
                //When we have the Presenter, initiate the FAB click action via the interface method
                ((PagerPresenter) presenter).onFabAddClicked();
            }
        }
    }

    /**
     * Method invoked when the "Refresh" Menu is clicked.
     */
    private void onRefreshMenuClicked() {
        //Retrieving the Fragment currently shown
        Fragment pagerFragment = getCurrentPagerFragment();

        if (pagerFragment != null) {
            //When we have a Fragment

            //Get the Presenter for the Pager Fragment
            Object presenter = ((PagerView) pagerFragment).getPresenter();

            if (presenter != null) {
                //When we have the Presenter, initiate the Refresh Menu action via the interface method
                ((PagerPresenter) presenter).onRefreshMenuClicked();
            }
        }
    }

    /**
     * Method that retrieves the ViewPager Fragment associated with the
     * current ViewPager position.
     *
     * @return The Registered Fragment at the ViewPager position.
     */
    private Fragment getCurrentPagerFragment() {
        //Get the position of the ViewPager
        int position = mViewPager.getCurrentItem();

        //Get the registered Fragment for the Position
        return mPagerAdapter.getRegisteredFragment(position);
    }

    /**
     * Perform any final cleanup before an activity is destroyed.  This can
     * happen either because the activity is finishing (someone called
     * {@link #finish} on it, or because the system is temporarily destroying
     * this instance of the activity to save space.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Clearing the Bitmap Cache if any
        BitmapImageCache.clearCache();
    }
}
