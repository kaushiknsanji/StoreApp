/*
 * Copyright 2018 Kaushik N. Sanji
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
