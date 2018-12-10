package com.example.kaushiknsanji.storeapp.ui.inventory.procure;

import com.example.kaushiknsanji.storeapp.data.local.models.SupplierContact;

/**
 * Interface to be implemented by {@link SalesProcurementActivityFragment}
 * to receive callback events for User actions on RecyclerView list of Phone Contacts.
 *
 * @author Kaushik N Sanji
 */
public interface SupplierPhoneListUserActionsListener {

    /**
     * Callback Method of {@link SupplierPhoneListUserActionsListener} invoked when
     * the user clicks on any of the Phone Contacts shown. This should launch an Intent
     * to start the Phone Activity passing in the number {@link SupplierContact#mValue}
     *
     * @param itemPosition    The adapter position of the Item View clicked
     * @param supplierContact The {@link SupplierContact} associated with the Item View clicked.
     */
    void onPhoneClicked(final int itemPosition, SupplierContact supplierContact);
}
