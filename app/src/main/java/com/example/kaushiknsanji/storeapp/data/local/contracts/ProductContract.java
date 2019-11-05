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
 * API Contract class for Product related tables
 *
 * @author Kaushik N Sanji
 */
public class ProductContract implements StoreContract {

    //Identifier for the table 'item' associated with the Base URI
    public static final String PATH_ITEM = "item";

    //Identifier for the table 'item_category' associated with the Base URI
    public static final String PATH_CATEGORY = "category";

    //Identifier for the table 'item_image' associated with the Base URI
    public static final String PATH_ITEM_IMAGE = "image";

    //Identifier for the table 'item_attr' associated with the Base URI
    public static final String PATH_ITEM_ATTR = "attr";

    /**
     * Private Constructor to avoid instantiating the {@link ProductContract}
     */
    private ProductContract() {
        //Suppressing with an error to enforce noninstantiability
        throw new AssertionError("No " + this.getClass().getCanonicalName() + " instances for you!");
    }

    /**
     * Inner class that defines the constants for the database 'item' Table.
     * This table contains the Item information.
     */
    public static final class Product implements BaseColumns {

        //The Content URI to access the 'item' Table data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEM);

        /**
         * The MIME Type of the {@link #CONTENT_URI} for the list of items
         * 'vnd.android.cursor.dir/com.example.kaushiknsanji.storeapp.provider.item
         */
        public static final String CONTENT_LIST_TYPE
                = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_ITEM;

        /**
         * The MIME Type of the {@link #CONTENT_URI} for a single item
         * 'vnd.android.cursor.item/com.example.kaushiknsanji.storeapp.provider.item
         */
        public static final String CONTENT_ITEM_TYPE
                = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_ITEM;


        //Identifier for Short information that retrieves the relationship data for the 'item'
        public static final String PATH_SHORT_INFO = "short";

        //The Content URI to access the short relationship data from 'item' Table in the provider
        public static final Uri CONTENT_URI_SHORT_INFO = Uri.withAppendedPath(CONTENT_URI, PATH_SHORT_INFO);

        /**
         * The MIME Type of the {@link #CONTENT_URI_SHORT_INFO} for the list of items with short relationship information
         * 'vnd.android.cursor.dir/com.example.kaushiknsanji.storeapp.provider.item.short
         */
        public static final String CONTENT_LIST_TYPE_SHORT_INFO
                = CONTENT_LIST_TYPE + "." + PATH_SHORT_INFO;

        //Identifier for retrieving an item from 'item' table based on the Item SKU
        public static final String PATH_ITEM_SKU = "sku";

        //The Content URI to access the 'item' Table data using the Item SKU in the provider
        public static final Uri CONTENT_URI_ITEM_SKU = Uri.withAppendedPath(CONTENT_URI, PATH_ITEM_SKU);

        /**
         * The MIME Type of the {@link #CONTENT_URI_ITEM_SKU} for a single item identified by the Item SKU
         * 'vnd.android.cursor.item/com.example.kaushiknsanji.storeapp.provider.item.sku
         */
        public static final String CONTENT_ITEM_TYPE_SKU
                = CONTENT_ITEM_TYPE + "." + PATH_ITEM_SKU;

        /**
         * Name of the Table
         */
        public static final String TABLE_NAME = "item";

        /**
         * The SKU of the Item
         * <P>Type: TEXT</P>
         * <P>Has Unique Constraint</P>
         */
        public static final String COLUMN_ITEM_SKU = "item_sku";

        /**
         * The name of the Item
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_ITEM_NAME = "item_name";

        /**
         * The Description of the Item
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_ITEM_DESCRIPTION = "item_description";

        /**
         * The Category ID of the Item
         * <P>Type: INTEGER</P>
         * <P>Foreign Key: item_category(_id)</P>
         */
        public static final String COLUMN_ITEM_CATEGORY_ID = "category_id";

        /**
         * Method that prepares and returns the URI for the 'item' Table
         * whose record is identified by the 'item_sku' value passed.
         *
         * @param itemSku The SKU configured for an Item
         * @return The {@link #CONTENT_URI_ITEM_SKU} with the {@code itemSku} value appended.
         */
        public static Uri buildItemSkuUri(String itemSku) {
            return CONTENT_URI_ITEM_SKU.buildUpon().appendPath(itemSku).build();
        }

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
     * Inner class that defines the constants for the database 'item_category' Table.
     * This table contains the Categories for items.
     */
    public static final class ProductCategory implements BaseColumns {

        //The Content URI to access the 'item_category' Table data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CATEGORY);

        /**
         * The MIME Type of the {@link #CONTENT_URI} for the list of categories
         * 'vnd.android.cursor.dir/com.example.kaushiknsanji.storeapp.provider.category
         */
        public static final String CONTENT_LIST_TYPE
                = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_CATEGORY;

        /**
         * The MIME Type of the {@link #CONTENT_URI} for a single category
         * 'vnd.android.cursor.item/com.example.kaushiknsanji.storeapp.provider.category
         */
        public static final String CONTENT_ITEM_TYPE
                = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_CATEGORY;

        /**
         * Name of the Table
         */
        public static final String TABLE_NAME = "item_category";

        /**
         * Category Name of the Item
         * <P>Type: TEXT</P>
         * <P>Has Unique Constraint</P>
         */
        public static final String COLUMN_ITEM_CATEGORY_NAME = "category_name";

        //Array of Categories that will be loaded on the initial launch of the app
        private static final String[] PRELOADED_CATEGORIES = new String[]{
                "Books",
                "Clothing",
                "Computers",
                "Electronics",
                "Furniture",
                "Office Products",
                "Software",
                "Sports",
                "Toys"
        };

        /**
         * Method that prepares and returns the URI for the 'item_category' Table
         * whose record is identified by the 'category_name' value passed.
         *
         * @param categoryName The Name of the Category configured for an item
         * @return The URI with the 'category_name' value appended.
         */
        public static Uri buildCategoryNameUri(String categoryName) {
            return CONTENT_URI.buildUpon().appendPath(categoryName).build();
        }

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

        /**
         * Method that returns an Array of Categories that will be loaded
         * on the initial launch of the app.
         *
         * @return Array of Categories to be preloaded.
         */
        public static String[] getPreloadedCategories() {
            return PRELOADED_CATEGORIES;
        }
    }

    /**
     * Inner class that defines the constants for the database 'item_image' Table.
     * This table contains the Image links of an Item.
     */
    public static final class ProductImage implements BaseColumns {

        //The Content URI to access the 'item_image' Table data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(Product.CONTENT_URI, PATH_ITEM_IMAGE);

        /**
         * The MIME Type of the {@link #CONTENT_URI} for the list of images
         * 'vnd.android.cursor.dir/com.example.kaushiknsanji.storeapp.provider.item.image
         */
        public static final String CONTENT_LIST_TYPE
                = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "." + PATH_ITEM + "." + PATH_ITEM_IMAGE;

        /**
         * The MIME Type of the {@link #CONTENT_URI} for a single image
         * 'vnd.android.cursor.item/com.example.kaushiknsanji.storeapp.provider.item.image
         */
        public static final String CONTENT_ITEM_TYPE
                = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "." + PATH_ITEM + "." + PATH_ITEM_IMAGE;

        /**
         * Name of the Table
         */
        public static final String TABLE_NAME = "item_image";

        /**
         * The Key of the Item
         * <P>Type: INTEGER</P>
         * <P>Foreign Key: item(_id)</P>
         */
        public static final String COLUMN_ITEM_ID = "item_id";

        /**
         * The URI to the Image of the Item
         * <P>Type: TEXT</P>
         * <P>Has Unique Constraint</P>
         */
        public static final String COLUMN_ITEM_IMAGE_URI = "image_uri";

        /**
         * The default Image of the Item selected to be shown always
         * <P>Type: INTEGER</P>
         */
        public static final String COLUMN_ITEM_IMAGE_DEFAULT = "is_default";

        /**
         * Constants for the possible values of 'is_default'
         */
        public static final int ITEM_IMAGE_DEFAULT = 1;
        public static final int ITEM_IMAGE_NON_DEFAULT = 0;

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
     * Inner class that defines the constants for the database 'item_attr' Table.
     * This table contains additional attributes that further defines an Item.
     */
    public static final class ProductAttribute implements BaseColumns {

        //The Content URI to access the 'item_attr' Table data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(Product.CONTENT_URI, PATH_ITEM_ATTR);

        /**
         * The MIME Type of the {@link #CONTENT_URI} for the list of Item Attributes
         * 'vnd.android.cursor.dir/com.example.kaushiknsanji.storeapp.provider.item.attr
         */
        public static final String CONTENT_LIST_TYPE
                = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "." + PATH_ITEM + "." + PATH_ITEM_ATTR;

        /**
         * The MIME Type of the {@link #CONTENT_URI} for a single Item Attribute
         * 'vnd.android.cursor.item/com.example.kaushiknsanji.storeapp.provider.item.attr
         */
        public static final String CONTENT_ITEM_TYPE
                = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "." + PATH_ITEM + "." + PATH_ITEM_ATTR;

        /**
         * Name of the Table
         */
        public static final String TABLE_NAME = "item_attr";

        /**
         * The Key of the Item
         * <P>Type: INTEGER</P>
         * <P>Foreign Key: item(_id)</P>
         */
        public static final String COLUMN_ITEM_ID = "item_id";

        /**
         * Name of the Item Attribute
         * <P>Type: TEXT</P>
         * <P>Has Unique Constraint</P>
         */
        public static final String COLUMN_ITEM_ATTR_NAME = "attr_name";

        /**
         * Value of the Item Attribute
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_ITEM_ATTR_VALUE = "attr_value";

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
