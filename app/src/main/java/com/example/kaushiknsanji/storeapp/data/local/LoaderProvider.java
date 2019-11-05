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

package com.example.kaushiknsanji.storeapp.data.local;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.example.kaushiknsanji.storeapp.data.local.contracts.ProductContract;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SalesContract;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SupplierContract;
import com.example.kaushiknsanji.storeapp.data.local.utils.QueryArgsUtility;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

/**
 * Class that provides CursorLoader instance based in the Loader Type {@link LoadersTypeDef} requested.
 *
 * @author Kaushik N Sanji
 */
public class LoaderProvider {

    //Annotation constants for possible types of CursorLoaders
    public static final int PRODUCT_LIST_TYPE = 0;
    public static final int SUPPLIER_LIST_TYPE = 1;
    public static final int SALES_LIST_TYPE = 2;
    //Singleton instance of LoaderProvider
    private static volatile LoaderProvider INSTANCE;
    //Context Reference required for CursorLoaders
    private final WeakReference<Context> mContextWeakReference;

    /**
     * Private Constructor of {@link LoaderProvider}
     *
     * @param context A {@link Context} required for CursorLoaders
     */
    private LoaderProvider(@NonNull Context context) {
        mContextWeakReference = new WeakReference<>(context);
    }

    /**
     * Static Singleton Constructor that creates a single instance of {@link LoaderProvider}
     *
     * @param context A {@link Context} required for CursorLoaders
     * @return New or existing instance of {@link LoaderProvider}
     */
    public static LoaderProvider getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            //When instance is not available
            synchronized (LoaderProvider.class) {
                //Apply lock and check for the instance again
                if (INSTANCE == null) {
                    //When there is no instance, create a new one
                    INSTANCE = new LoaderProvider(context);
                }
            }
        }
        //Returning the instance of LoaderProvider
        return INSTANCE;
    }

    /**
     * Method that returns the {@link CursorLoader} to the {@code loaderType} requested
     *
     * @param loaderType Integer constant defined by {@link LoadersTypeDef} to
     *                   create a correct CursorLoader.
     * @return Returns a {@link CursorLoader} instance when the {@link Context} instance is present
     * and the {@code loaderType} requested is valid; otherwise {@code NULL}
     */
    public Loader<Cursor> createCursorLoader(@LoadersTypeDef int loaderType) {
        //Retrieving the Context from the reference
        Context context = mContextWeakReference.get();
        //Returning Null when the Context is Null
        if (context == null) {
            return null;
        }

        //Returning the CursorLoader instance based on the LoaderType
        switch (loaderType) {
            case PRODUCT_LIST_TYPE:
                //Returning the Cursor Loader to list of Products sorted by its SKU
                return new CursorLoader(
                        context,
                        ProductContract.Product.CONTENT_URI_SHORT_INFO,
                        QueryArgsUtility.ItemsShortInfoQuery.getProjection(),
                        null,
                        null,
                        ProductContract.Product.getQualifiedColumnName(ProductContract.Product.COLUMN_ITEM_SKU)
                );
            case SUPPLIER_LIST_TYPE:
                //Returning the Cursor Loader to list of Suppliers sorted by its Code
                return new CursorLoader(
                        context,
                        SupplierContract.Supplier.CONTENT_URI_SHORT_INFO,
                        QueryArgsUtility.SuppliersShortInfoQuery.getProjection(),
                        null,
                        null,
                        SupplierContract.Supplier.COLUMN_SUPPLIER_CODE
                );
            case SALES_LIST_TYPE:
                //Returning the Cursor Loader to the list of Products sold by the Suppliers, sorted by its SKU
                return new CursorLoader(
                        context,
                        SalesContract.ProductSupplierInventory.CONTENT_URI_SHORT_INFO,
                        QueryArgsUtility.SalesShortInfoQuery.getProjection(),
                        null,
                        null,
                        ProductContract.Product.getQualifiedColumnName(ProductContract.Product.COLUMN_ITEM_SKU)
                );
        }

        //Returning null when invalid Loader Type is passed
        //(This can never occur because of the annotated parameter in use)
        return null;
    }

    //Defining Annotation interface for valid types of CursorLoaders
    //Enumerating Annotation with the valid types of CursorLoaders
    //Retains annotation till Compile Time
    @IntDef({PRODUCT_LIST_TYPE, SUPPLIER_LIST_TYPE, SALES_LIST_TYPE})
    @Retention(RetentionPolicy.SOURCE)
    @interface LoadersTypeDef {
    }

}
