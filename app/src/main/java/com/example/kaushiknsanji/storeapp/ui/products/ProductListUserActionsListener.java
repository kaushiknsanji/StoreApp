package com.example.kaushiknsanji.storeapp.ui.products;

import android.widget.ImageView;

import com.example.kaushiknsanji.storeapp.data.local.models.ProductLite;

/**
 * Interface to be implemented by {@link ProductListFragment}
 * to receive callback events for User actions on RecyclerView list of Products
 *
 * @author Kaushik N Sanji
 */
public interface ProductListUserActionsListener {

    /**
     * Callback Method of {@link ProductListUserActionsListener} invoked when
     * the user clicks on "Edit" button or the Item View itself. This should
     * launch the {@link com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigActivity}
     * for the Product to be edited.
     *
     * @param itemPosition          The adapter position of the Item clicked
     * @param product               The {@link ProductLite} associated with the Item clicked
     * @param imageViewProductPhoto The ImageView of the Adapter Item that displays the Image
     */
    void onEditProduct(final int itemPosition, ProductLite product, ImageView imageViewProductPhoto);

    /**
     * Callback Method of {@link ProductListUserActionsListener} invoked when
     * the user clicks on "Delete" button. This should delete the Product
     * identified by {@link ProductLite#mId}, from the database.
     *
     * @param itemPosition The adapter position of the Item clicked
     * @param product      The {@link ProductLite} associated with the Item clicked
     */
    void onDeleteProduct(final int itemPosition, ProductLite product);
}
