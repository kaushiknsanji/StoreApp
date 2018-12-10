package com.example.kaushiknsanji.storeapp.ui.products.image;

import android.graphics.Bitmap;

/**
 * Interface to be implemented by {@link ProductImageActivity} to receive callback
 * events for User selection actions on RecyclerView Grid of Product photos.
 *
 * @author Kaushik N Sanji
 */
public interface SelectedPhotoActionsListener {

    /**
     * Callback Method of {@link SelectedPhotoActionsListener} invoked to display the
     * default image for the Product.
     */
    void showDefaultImage();

    /**
     * Callback Method of {@link SelectedPhotoActionsListener} invoked to display the
     * selected Image {@code bitmap} for the Product.
     *
     * @param bitmap   The {@link Bitmap} of the Image to be shown.
     * @param imageUri The String Content URI of the Image to be shown.
     */
    void showSelectedImage(Bitmap bitmap, String imageUri);

}
