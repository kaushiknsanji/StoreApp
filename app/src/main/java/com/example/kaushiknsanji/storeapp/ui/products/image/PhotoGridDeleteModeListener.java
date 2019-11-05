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

/**
 * Interface to be implemented by the {@link ProductImageActivity}
 * to receive Long click/DELETE event on RecyclerView Grid items.
 *
 * @author Kaushik N Sanji
 */
public interface PhotoGridDeleteModeListener {
    /**
     * Callback Method of {@link PhotoGridDeleteModeListener} invoked when
     * the user Long clicks on an item in the RecyclerView that displays a Grid of Photos.
     */
    void onGridItemDeleteMode();

    /**
     * Callback Method of {@link PhotoGridDeleteModeListener} invoked when
     * the user clicks on items in DELETE Action Mode to select them for Delete.
     * <p>
     * This Method should show the count of Items {@code itemCount} currently selected for Delete.
     * </p>
     *
     * @param itemCount The Number of Image items currently selected for Delete Action.
     */
    void showSelectedItemCount(int itemCount);
}
