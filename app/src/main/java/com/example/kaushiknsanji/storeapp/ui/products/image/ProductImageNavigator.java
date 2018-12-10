package com.example.kaushiknsanji.storeapp.ui.products.image;

import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;

import java.util.ArrayList;

/**
 * Defines Navigation actions that can be invoked from the {@link ProductImageActivity}
 *
 * @author Kaushik N Sanji
 */
public interface ProductImageNavigator {

    /**
     * Method that updates the result {@code productImages} to be sent back to the Calling activity.
     *
     * @param productImages List of {@link ProductImage}, each of which holds the URI information
     *                      of the Image File.
     */
    void doSetResult(ArrayList<ProductImage> productImages);

}
