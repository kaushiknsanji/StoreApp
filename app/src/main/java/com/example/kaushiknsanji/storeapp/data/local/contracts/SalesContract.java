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

package com.example.kaushiknsanji.storeapp.data.local.contracts;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * API Contract class common to Product and Supplier
 * that deals with the Sales related stuff.
 *
 * @author Kaushik N Sanji
 */
public class SalesContract implements StoreContract {

    //Identifier for the table 'item_supplier_info' associated with the Base URI
    public static final String PATH_ITEM_SUPPLIER_INFO = "salesinfo";

    //Identifier for the table 'item_supplier_inventory' associated with the Base URI
    public static final String PATH_ITEM_SUPPLIER_INVENTORY = "salesinventory";

    /**
     * Private Constructor to avoid instantiating the {@link SalesContract}
     */
    private SalesContract() {
        //Suppressing with an error to enforce noninstantiability
        throw new AssertionError("No " + this.getClass().getCanonicalName() + " instances for you!");
    }

    /**
     * Interface common to the tables in this contract
     */
    public interface ProductSupplierColumns extends BaseColumns {

        /**
         * The Key of the Item
         * <P>Type: INTEGER</P>
         * <P>Foreign Key: item(_id)</P>
         */
        String COLUMN_ITEM_ID = "item_id";

        /**
         * The Key of the Supplier
         * <P>Type: INTEGER</P>
         * <P>Foreign Key: supplier(_id)</P>
         */
        String COLUMN_SUPPLIER_ID = "supplier_id";
    }

    /**
     * Inner class that defines the constants for the database 'item_supplier_info' Table.
     * This table maintains the price information of the item supplied by the suppliers.
     */
    public static final class ProductSupplierInfo implements ProductSupplierColumns {

        //The Content URI to access the 'item_supplier_info' Table data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEM_SUPPLIER_INFO);

        /**
         * The MIME Type of the {@link #CONTENT_URI} for the list of items and its suppliers
         * 'vnd.android.cursor.dir/com.example.kaushiknsanji.storeapp.provider.salesinfo
         */
        public static final String CONTENT_LIST_TYPE
                = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_ITEM_SUPPLIER_INFO;

        /**
         * The MIME Type of the {@link #CONTENT_URI} for a single item and its suppliers
         * 'vnd.android.cursor.item/com.example.kaushiknsanji.storeapp.provider.salesinfo
         */
        public static final String CONTENT_ITEM_TYPE
                = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_ITEM_SUPPLIER_INFO;


        //The Content URI to access the 'item' relationship data from 'item_supplier_info' Table
        //for a given supplier in the provider
        public static final Uri CONTENT_URI_SUPPLIER_ITEMS = Uri.withAppendedPath(CONTENT_URI, SupplierContract.PATH_SUPPLIER);

        /**
         * The MIME Type of the {@link #CONTENT_URI_SUPPLIER_ITEMS} for the list of items of a given supplier
         * 'vnd.android.cursor.dir/com.example.kaushiknsanji.storeapp.provider.salesinfo.supplier
         */
        public static final String CONTENT_LIST_TYPE_SUPPLIER_ITEMS
                = CONTENT_LIST_TYPE + "." + SupplierContract.PATH_SUPPLIER;


        //The Content URI to access the 'supplier' relationship data from 'item_supplier_info' Table
        //for a given item in the provider
        public static final Uri CONTENT_URI_ITEM_SUPPLIERS = Uri.withAppendedPath(CONTENT_URI, ProductContract.PATH_ITEM);

        /**
         * The MIME Type of the {@link #CONTENT_URI_ITEM_SUPPLIERS} for the list of suppliers of a given item
         * 'vnd.android.cursor.dir/com.example.kaushiknsanji.storeapp.provider.salesinfo.item
         */
        public static final String CONTENT_LIST_TYPE_ITEM_SUPPLIERS
                = CONTENT_LIST_TYPE + "." + ProductContract.PATH_ITEM;

        /**
         * Name of the Table
         */
        public static final String TABLE_NAME = "item_supplier_info";

        /**
         * The Unit Selling Price of the Item
         * <P>Type: REAL</P>
         */
        public static final String COLUMN_ITEM_UNIT_PRICE = "unit_price";

        /**
         * The Default value of "Unit Selling Price of the Item"
         */
        public static final float DEFAULT_ITEM_UNIT_PRICE = 0.0f;

        /**
         * Method that prepares and returns a fully qualified Column Name
         * for the given Column with the current Table Name.
         *
         * @param columnNameStr The Name of the Column
         * @return String containing the Fully qualified Column Name with its Table Name
         * in the format TableName.ColumnName
         */
        @NonNull
        public static String getQualifiedColumnName(String columnNameStr) {
            return TextUtils.concat(TABLE_NAME, ".", columnNameStr).toString();
        }
    }

    /**
     * Inner class that defines the constants for the database 'item_supplier_inventory' Table.
     * This table maintains the available to sell quantity of the item supplied by the suppliers.
     */
    public static final class ProductSupplierInventory implements ProductSupplierColumns {

        //The Content URI to access the 'item_supplier_inventory' Table data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEM_SUPPLIER_INVENTORY);

        /**
         * The MIME Type of the {@link #CONTENT_URI} for the list of items and its suppliers
         * 'vnd.android.cursor.dir/com.example.kaushiknsanji.storeapp.provider.salesinventory
         */
        public static final String CONTENT_LIST_TYPE
                = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_ITEM_SUPPLIER_INVENTORY;

        /**
         * The MIME Type of the {@link #CONTENT_URI} for a single item and its suppliers
         * 'vnd.android.cursor.item/com.example.kaushiknsanji.storeapp.provider.salesinventory
         */
        public static final String CONTENT_ITEM_TYPE
                = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_ITEM_SUPPLIER_INVENTORY;

        //The Content URI to access the 'item' relationship data from 'item_supplier_inventory' Table
        //for a given supplier in the provider
        public static final Uri CONTENT_URI_INV_SUPPLIER = Uri.withAppendedPath(CONTENT_URI, SupplierContract.PATH_SUPPLIER);

        /**
         * The MIME Type of the {@link #CONTENT_URI_INV_SUPPLIER} for the list of items of a given supplier
         * 'vnd.android.cursor.dir/com.example.kaushiknsanji.storeapp.provider.salesinventory.supplier
         */
        public static final String CONTENT_LIST_TYPE_INV_SUPPLIER
                = CONTENT_LIST_TYPE + "." + SupplierContract.PATH_SUPPLIER;

        //The Content URI to access the 'supplier' relationship data from 'item_supplier_inventory' Table
        //for a given item in the provider
        public static final Uri CONTENT_URI_INV_ITEM = Uri.withAppendedPath(CONTENT_URI, ProductContract.PATH_ITEM);

        /**
         * The MIME Type of the {@link #CONTENT_URI_INV_ITEM} for the list of suppliers of a given item
         * 'vnd.android.cursor.dir/com.example.kaushiknsanji.storeapp.provider.salesinventory.item
         */
        public static final String CONTENT_LIST_TYPE_INV_ITEM
                = CONTENT_LIST_TYPE + "." + ProductContract.PATH_ITEM;

        //Identifier for Short information that retrieves the relationship data for the 'item_supplier_inventory'
        public static final String PATH_SHORT_INFO = "short";

        //The Content URI to access the short relationship data from 'item_supplier_inventory' Table in the provider
        public static final Uri CONTENT_URI_SHORT_INFO = Uri.withAppendedPath(CONTENT_URI, PATH_SHORT_INFO);

        /**
         * The MIME Type of the {@link #CONTENT_URI_SHORT_INFO} for the list of items + suppliers
         * with short relationship information from 'item_supplier_inventory' Table
         * 'vnd.android.cursor.dir/com.example.kaushiknsanji.storeapp.provider.salesinventory.short
         */
        public static final String CONTENT_LIST_TYPE_SHORT_INFO
                = CONTENT_LIST_TYPE + "." + PATH_SHORT_INFO;

        /**
         * Name of the Table
         */
        public static final String TABLE_NAME = "item_supplier_inventory";

        /**
         * The Quantity of the Item available to Sell
         * <P>Type: INTEGER</P>
         */
        public static final String COLUMN_ITEM_AVAIL_QUANTITY = "available_quantity";

        /**
         * Default value of "Quantity of the Item available to Sell"
         */
        public static final int DEFAULT_ITEM_AVAIL_QUANTITY = 0;

        /**
         * Method that prepares and returns a fully qualified Column Name
         * for the given Column with the current Table Name.
         *
         * @param columnNameStr The Name of the Column
         * @return String containing the Fully qualified Column Name with its Table Name
         * in the format TableName.ColumnName
         */
        @NonNull
        public static String getQualifiedColumnName(String columnNameStr) {
            return TextUtils.concat(TABLE_NAME, ".", columnNameStr).toString();
        }
    }
}
