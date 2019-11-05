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

import com.example.kaushiknsanji.storeapp.data.local.models.ProductImage;

import java.util.ArrayList;

/**
 * Defines Navigation actions that can be invoked from the {@link ProductImageActivity}
 *
 * @author Kaushik N Sanji
 */
public interface ProductImageNavigator {

    /**
     * Method that updates the result {@code productImages} to be sent back to the Calling activity.
     *
     * @param productImages List of {@link ProductImage}, each of which holds the URI information
     *                      of the Image File.
     */
    void doSetResult(ArrayList<ProductImage> productImages);

}
