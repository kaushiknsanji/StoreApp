package com.example.kaushiknsanji.storeapp.ui;

/**
 * Custom Presenter Interface that extends {@link BasePresenter} for use with the Fragments
 * shown in the ViewPager of the {@link MainActivity}.
 *
 * @author Kaushik N Sanji
 */
public interface PagerPresenter extends BasePresenter {
    /**
     * Method invoked by the {@link MainActivity} displaying the ViewPager.
     * This is called when the User clicks on the Fab "+" button shown by the {@link MainActivity}
     */
    void onFabAddClicked();

    /**
     * Method invoked by the {@link MainActivity} displaying the ViewPager.
     * This is called when the User clicks on the Refresh Menu icon shown by the {@link MainActivity}
     */
    void onRefreshMenuClicked();
}
