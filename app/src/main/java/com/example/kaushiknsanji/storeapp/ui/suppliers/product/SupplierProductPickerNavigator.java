package com.example.kaushiknsanji.storeapp.ui.suppliers.product;

import com.example.kaushiknsanji.storeapp.data.local.models.ProductLite;

import java.util.ArrayList;

/**
 * Defines Navigation Actions that can be invoked from {@link SupplierProductPickerActivity}.
 *
 * @author Kaushik N Sanji
 */
public interface SupplierProductPickerNavigator {

    /**
     * Method that updates the result {@code productsToSell} to be sent back to the Calling activity.
     *
     * @param productsToSell List of Products {@link ProductLite} selected by the Supplier
     *                       for selling.
     */
    void doSetResult(ArrayList<ProductLite> productsToSell);

    /**
     * Method that updates the Calling Activity that the operation was aborted.
     */
    void doCancel();
}
