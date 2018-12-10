package com.example.kaushiknsanji.storeapp.ui.suppliers.config;

import android.widget.ImageView;

import com.example.kaushiknsanji.storeapp.data.local.models.ProductLite;

/**
 * Interface to be implemented by {@link SupplierConfigActivityFragment}
 * to receive callback events for User actions on RecyclerView list of Supplier Products
 *
 * @author Kaushik N Sanji
 */
public interface SupplierProductsUserActionsListener {

    /**
     * Callback Method of {@link SupplierProductsUserActionsListener} invoked when
     * the user clicks on "Edit" button or the Item View itself. This should
     * launch the {@link com.example.kaushiknsanji.storeapp.ui.products.config.ProductConfigActivity}
     * for the Product to be edited.
     *
     * @param itemPosition          The adapter position of the Item clicked.
     * @param product               The {@link ProductLite} associated with the Item clicked.
     * @param imageViewProductPhoto The ImageView of the Adapter Item that displays the Image
     */
    void onEditProduct(final int itemPosition, ProductLite product, ImageView imageViewProductPhoto);

    /**
     * Callback Method of {@link SupplierProductsUserActionsListener} invoked when
     * the user swipes the Item View to remove the Supplier-Product link.
     *
     * @param itemPosition The adapter position of the Item swiped.
     * @param product      The {@link ProductLite} associated with the Item swiped.
     */
    void onSwiped(final int itemPosition, ProductLite product);
}
