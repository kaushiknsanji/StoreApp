package com.example.kaushiknsanji.storeapp.ui.suppliers.product;

/**
 * Interface to be implemented by {@link SupplierProductPickerActivity} to receive
 * callback events for User select/pick actions on the RecyclerView List of Products.
 *
 * @author Kaushik N Sanji
 */
public interface SupplierProductPickerMultiSelectListener {
    /**
     * Callback Method of {@link SupplierProductPickerMultiSelectListener} invoked to
     * display the number of Products {@code countOfProductsSelected} selected for the Supplier to sell.
     *
     * @param countOfProductsSelected The Number of Products selected/picked for the Supplier to sell.
     */
    void showSelectedCount(int countOfProductsSelected);
}
