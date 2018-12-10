package com.example.kaushiknsanji.storeapp.ui.products.config;

/**
 * Interface to be implemented by {@link ProductConfigActivity} to receive
 * callback events when there is a change in the default image of the Product.
 *
 * @author Kaushik N Sanji
 */
public interface DefaultPhotoChangeListener {

    /**
     * Callback Method of {@link DefaultPhotoChangeListener} invoked when there are
     * no selected Product Images for the Product.
     * <p>In this case, the View needs to show the default Image for the Product instead.</p>
     */
    void showDefaultImage();

    /**
     * Callback Method of {@link DefaultPhotoChangeListener} invoked to display the selected
     * Image of the Product pointed to by the Image Content URI {@code imageUri}.
     *
     * @param imageUri The String Content URI of the Image to be shown.
     */
    void showSelectedProductImage(String imageUri);

}
