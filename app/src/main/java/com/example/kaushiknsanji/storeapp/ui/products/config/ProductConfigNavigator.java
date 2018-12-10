package com.example.kaushiknsanji.storeapp.ui.products.config;

import android.support.annotation.NonNull;

import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;

import java.util.ArrayList;

/**
 * Defines Navigation Actions that can be invoked from {@link ProductConfigActivity}
 *
 * @author Kaushik N Sanji
 */
public interface ProductConfigNavigator {

    /**
     * Method that updates the result {@code resultCode} to be sent back to the Calling Activity
     *
     * @param resultCode The integer result code to be returned to the Calling Activity.
     * @param productId  Integer containing the Id of the Product involved.
     * @param productSku String containing the SKU information of the Product involved.
     */
    void doSetResult(final int resultCode, final int productId, @NonNull final String productSku);

    /**
     * Method that updates the Calling Activity that the operation was aborted.
     */
    void doCancel();

    /**
     * Method that launches the {@link com.example.kaushiknsanji.storeapp.ui.products.image.ProductImageActivity}
     * when the Add Photo button (R.id.image_product_config_add_photo) on the Product Image is clicked.
     *
     * @param productImages List of {@link ProductImage} that stores the URI details of the Images
     */
    void launchProductImagesView(ArrayList<ProductImage> productImages);

}

