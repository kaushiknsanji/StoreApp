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
