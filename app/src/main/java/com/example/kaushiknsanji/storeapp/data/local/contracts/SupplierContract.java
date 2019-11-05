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
 * API Contract class for Supplier related tables
 *
 * @author Kaushik N Sanji
 */
public class SupplierContract implements StoreContract {

    //Identifier for the table 'supplier' associated with the Base URI
    public static final String PATH_SUPPLIER = "supplier";

    //Identifier for the table 'contact_type' associated with the Base URI
    public static final String PATH_CONTACT_TYPE = "contacttype";

    //Identifier for the table 'supplier_contact' associated with the Base URI
    public static final String PATH_SUPPLIER_CONTACT = "contact";

    /**
     * Private Constructor to avoid instantiating the {@link SupplierContract}
     */
    private SupplierContract() {
        //Suppressing with an error to enforce noninstantiability
        throw new AssertionError("No " + this.getClass().getCanonicalName() + " instances for you!");
    }

    /**
     * Inner class that defines the constants for the database 'supplier' Table.
     * This table contains the Supplier information.
     */
    public static final class Supplier implements BaseColumns {

        //The Content URI to access the 'supplier' Table data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SUPPLIER);

        /**
         * The MIME Type of the {@link #CONTENT_URI} for the list of suppliers
         * 'vnd.android.cursor.dir/com.example.kaushiknsanji.storeapp.provider.supplier
         */
        public static final String CONTENT_LIST_TYPE
                = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_SUPPLIER;

        /**
         * The MIME Type of the {@link #CONTENT_URI} for a single supplier
         * 'vnd.android.cursor.item/com.example.kaushiknsanji.storeapp.provider.supplier
         */
        public static final String CONTENT_ITEM_TYPE
                = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_SUPPLIER;


        //Identifier for Short information that retrieves the relationship data for the 'supplier'
        public static final String PATH_SHORT_INFO = "short";

        //The Content URI to access the short relationship data from 'supplier' Table in the provider
        public static final Uri CONTENT_URI_SHORT_INFO = Uri.withAppendedPath(CONTENT_URI, PATH_SHORT_INFO);

        /**
         * The MIME Type of the {@link #CONTENT_URI_SHORT_INFO} for the list of suppliers with short relationship information
         * 'vnd.android.cursor.dir/com.example.kaushiknsanji.storeapp.provider.supplier.short
         */
        public static final String CONTENT_LIST_TYPE_SHORT_INFO
                = CONTENT_LIST_TYPE + "." + PATH_SHORT_INFO;

        //Identifier for retrieving a supplier from 'supplier' table based on the Supplier Code
        public static final String PATH_SUPPLIER_CODE = "code";

        //The Content URI to access the 'supplier' Table data using the Supplier Code in the provider
        public static final Uri CONTENT_URI_SUPPLIER_CODE = Uri.withAppendedPath(CONTENT_URI, PATH_SUPPLIER_CODE);

        /**
         * The MIME Type of the {@link #CONTENT_URI_SUPPLIER_CODE} for a single supplier identified by the Supplier Code
         * 'vnd.android.cursor.item/com.example.kaushiknsanji.storeapp.provider.supplier.code
         */
        public static final String CONTENT_ITEM_TYPE_SUPPLIER_CODE
                = CONTENT_ITEM_TYPE + "." + PATH_SUPPLIER_CODE;

        /**
         * Name of the Table
         */
        public static final String TABLE_NAME = "supplier";

        /**
         * Name of the Supplier
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_SUPPLIER_NAME = "supplier_name";

        /**
         * Code of the Supplier
         * <P>Type: TEXT</P>
         * <P>Has Unique Constraint</P>
         */
        public static final String COLUMN_SUPPLIER_CODE = "supplier_code";

        /**
         * Method that prepares and returns the URI for the 'supplier' Table
         * whose record is identified by the 'supplier_code' value passed.
         *
         * @param supplierCode The Supplier Code configured for a Supplier
         * @return The {@link #CONTENT_URI_SUPPLIER_CODE} with the {@code supplierCode} value appended
         */
        public static Uri buildSupplierCodeUri(String supplierCode) {
            return CONTENT_URI_SUPPLIER_CODE.buildUpon().appendPath(supplierCode).build();
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
     * Inner class that defines the constants for the database 'contact_type' Table.
     * This table contains the possible contact types of a Supplier.
     */
    public static final class SupplierContactType implements BaseColumns {

        //The Content URI to access the 'contact_type' Table data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CONTACT_TYPE);

        /**
         * The MIME Type of the {@link #CONTENT_URI} for the list of contact types
         * 'vnd.android.cursor.dir/com.example.kaushiknsanji.storeapp.provider.contacttype
         */
        public static final String CONTENT_LIST_TYPE
                = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_CONTACT_TYPE;

        /**
         * The MIME Type of the {@link #CONTENT_URI} for a single contact type
         * 'vnd.android.cursor.item/com.example.kaushiknsanji.storeapp.provider.contacttype
         */
        public static final String CONTENT_ITEM_TYPE
                = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_CONTACT_TYPE;

        /**
         * Name of the Table
         */
        public static final String TABLE_NAME = "contact_type";

        /**
         * Type of Contact
         * <P>Type: TEXT</P>
         * <P>Has Unique Constraint</P>
         */
        public static final String COLUMN_CONTACT_TYPE_NAME = "type_name";

        /**
         * Constants for the possible values of 'type_name'
         */
        public static final String CONTACT_TYPE_PHONE = "Phone";
        public static final int CONTACT_TYPE_ID_PHONE = 0;
        public static final String CONTACT_TYPE_EMAIL = "Email";
        public static final int CONTACT_TYPE_ID_EMAIL = 1;

        //Array of Contact Types that will be loaded on the initial launch of the app
        private static final String[] PRELOADED_CONTACT_TYPES = new String[]{
                CONTACT_TYPE_PHONE,
                CONTACT_TYPE_EMAIL
        };

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
         * Method that returns an Array of Contact Types that will be loaded
         * on the initial launch of the app.
         *
         * @return Array of Contact Types to be preloaded.
         */
        public static String[] getPreloadedContactTypes() {
            return PRELOADED_CONTACT_TYPES;
        }
    }

    /**
     * Inner class that defines the constants for the database 'supplier_contact' Table.
     * This table contains the contact information of a Supplier.
     */
    public static final class SupplierContact implements BaseColumns {

        //The Content URI to access the 'supplier_contact' Table data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(Supplier.CONTENT_URI, PATH_SUPPLIER_CONTACT);

        /**
         * The MIME Type of the {@link #CONTENT_URI} for the list of suppliers' contacts
         * 'vnd.android.cursor.dir/com.example.kaushiknsanji.storeapp.provider.supplier.contact
         */
        public static final String CONTENT_LIST_TYPE
                = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "." + PATH_SUPPLIER + "." + PATH_SUPPLIER_CONTACT;

        /**
         * The MIME Type of the {@link #CONTENT_URI} for the contacts of a single supplier
         * 'vnd.android.cursor.item/com.example.kaushiknsanji.storeapp.provider.supplier.contact
         */
        public static final String CONTENT_ITEM_TYPE
                = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "." + PATH_SUPPLIER + "." + PATH_SUPPLIER_CONTACT;

        /**
         * Name of the Table
         */
        public static final String TABLE_NAME = "supplier_contact";

        /**
         * The Key of the Supplier Contact Type
         * <P>Type: INTEGER</P>
         * <P>Foreign Key: contact_type(_id)</P>
         */
        public static final String COLUMN_SUPPLIER_CONTACT_TYPE_ID = "contact_type_id";

        /**
         * Value of the Supplier Contact for the chosen contact type
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_SUPPLIER_CONTACT_VALUE = "contact_value";

        /**
         * The default contact to be shown always
         * <P>Type: INTEGER</P>
         */
        public static final String COLUMN_SUPPLIER_CONTACT_DEFAULT = "is_default";

        /**
         * The Key of the Supplier
         * <P>Type: INTEGER</P>
         * <P>Foreign Key: supplier(_id)</P>
         */
        public static final String COLUMN_SUPPLIER_ID = "supplier_id";

        /**
         * Constants for the possible values of 'is_default'
         */
        public static final int SUPPLIER_CONTACT_DEFAULT = 1;
        public static final int SUPPLIER_CONTACT_NON_DEFAULT = 0;

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
