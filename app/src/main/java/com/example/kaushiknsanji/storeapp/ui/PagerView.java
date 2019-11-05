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

package com.example.kaushiknsanji.storeapp.ui;

import android.support.annotation.Nullable;

/**
 * Custom View Interface that extends {@link BaseView} for use with the Fragments
 * shown in the ViewPager of the {@link MainActivity}.
 *
 * @author Kaushik N Sanji
 */
public interface PagerView<T extends PagerPresenter> extends BaseView<T> {
    /**
     * Method that returns the registered Presenter for this View.
     *
     * @return The registered Presenter for this View. Can be {@code null}
     */
    @Nullable
    T getPresenter();
}
