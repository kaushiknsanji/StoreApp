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

/**
 * Base Presenter Interface as in Model-View-Presenter.
 *
 * @author Kaushik N Sanji
 */
public interface BasePresenter {

    /**
     * Method that initiates the work of a Presenter which is invoked by the View
     * that implements the {@link com.example.kaushiknsanji.storeapp.ui.BaseView}
     */
    void start();
}
