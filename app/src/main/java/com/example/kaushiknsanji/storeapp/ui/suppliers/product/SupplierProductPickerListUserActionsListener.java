package com.example.kaushiknsanji.storeapp.ui.suppliers.product;

/**
 * Interface to be implemented by the {@link SupplierProductPickerActivityFragment}
 * to receive callback events for User actions on the RecyclerView list of Products.
 *
 * @author Kaushik N Sanji
 */
public interface SupplierProductPickerListUserActionsListener {
    /**
     * Callback Method of {@link SupplierProductPickerListUserActionsListener} invoked when
     * the user clicks on an Item in the RecyclerView that displays a list of Products to pick/select
     *
     * @param itemPosition The adapter position of the Item clicked
     */
    void onItemClicked(int itemPosition);
}
