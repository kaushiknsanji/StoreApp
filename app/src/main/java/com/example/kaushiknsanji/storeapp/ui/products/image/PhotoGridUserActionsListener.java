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

package com.example.kaushiknsanji.storeapp.ui.products.image;

import android.graphics.Bitmap;

import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;

/**
 * Interface to be implemented by the {@link ProductImageActivityFragment}
 * to receive callback events for User actions on RecyclerView Grid of Product photos.
 *
 * @author Kaushik N Sanji
 */
public interface PhotoGridUserActionsListener {
    /**
     * Callback Method of {@link PhotoGridUserActionsListener} invoked when
     * the user clicks on an item in the RecyclerView that displays a Grid of Photos,
     * to select from.
     *
     * @param itemPosition The adapter position of the Item clicked
     * @param productImage The {@link ProductImage} associated with the Item clicked
     * @param gridMode     The mode of the Action as defined by {@link com.example.kaushiknsanji.storeapp.ui.products.image.ProductImageContract.PhotoGridSelectModeDef}
     */
    void onItemClicked(int itemPosition, ProductImage productImage, @ProductImageContract.PhotoGridSelectModeDef String gridMode);

    /**
     * Callback Method of {@link PhotoGridUserActionsListener} invoked when
     * the user does a Long click on an item in the RecyclerView that displays a Grid of Photos,
     * to delete from. This should also trigger the contextual action mode for delete action.
     *
     * @param itemPosition The adapter position of the Item Long clicked
     * @param productImage The {@link ProductImage} associated with the Item Long clicked
     * @param gridMode     The mode of the Action as defined by {@link com.example.kaushiknsanji.storeapp.ui.products.image.ProductImageContract.PhotoGridSelectModeDef}
     */
    void onItemLongClicked(int itemPosition, ProductImage productImage, @ProductImageContract.PhotoGridSelectModeDef String gridMode);

    /**
     * Callback Method of {@link PhotoGridUserActionsListener} invoked when
     * the selected item's image is to be shown in the Main ImageView 'R.id.image_product_selected_item_photo'
     *
     * @param bitmap       The {@link Bitmap} of the Image to be shown.
     * @param productImage The {@link ProductImage} associated with the Item Image to be shown.
     */
    void showSelectedImage(Bitmap bitmap, ProductImage productImage);
}
