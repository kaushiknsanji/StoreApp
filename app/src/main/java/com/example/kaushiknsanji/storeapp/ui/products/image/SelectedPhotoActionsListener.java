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

/**
 * Interface to be implemented by {@link ProductImageActivity} to receive callback
 * events for User selection actions on RecyclerView Grid of Product photos.
 *
 * @author Kaushik N Sanji
 */
public interface SelectedPhotoActionsListener {

    /**
     * Callback Method of {@link SelectedPhotoActionsListener} invoked to display the
     * default image for the Product.
     */
    void showDefaultImage();

    /**
     * Callback Method of {@link SelectedPhotoActionsListener} invoked to display the
     * selected Image {@code bitmap} for the Product.
     *
     * @param bitmap   The {@link Bitmap} of the Image to be shown.
     * @param imageUri The String Content URI of the Image to be shown.
     */
    void showSelectedImage(Bitmap bitmap, String imageUri);

}
