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

