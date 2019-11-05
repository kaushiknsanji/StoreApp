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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.os.Build;
import android.util.Log;

import com.example.kaushiknsanji.storeapp.data.local.contracts.ProductContract.Product;
import com.example.kaushiknsanji.storeapp.data.local.contracts.ProductContract.ProductAttribute;
import com.example.kaushiknsanji.storeapp.data.local.contracts.ProductContract.ProductCategory;
import com.example.kaushiknsanji.storeapp.data.local.contracts.ProductContract.ProductImage;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SalesContract.ProductSupplierInfo;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SalesContract.ProductSupplierInventory;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SupplierContract.Supplier;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SupplierContract.SupplierContact;
import com.example.kaushiknsanji.storeapp.data.local.contracts.SupplierContract.SupplierContactType;
import com.example.kaushiknsanji.storeapp.utils.AppConstants;
import com.example.kaushiknsanji.storeapp.utils.AppExecutors;

import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.CLOSE_BRACE;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.COMMA;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.CONFLICT_FAIL;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.CONFLICT_REPLACE;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.CONSTRAINT;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.CREATE_INDEX;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.CREATE_TABLE;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.DEFAULT;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.DELETE_CASCADE;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.FOREIGN_KEY;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.INTEGER;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.NOT;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.NULL;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.ON;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.OPEN_BRACE;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.PRIMARY_KEY;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.PRIMARY_KEY_AUTOINCREMENT;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.REAL;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.REFERENCES;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.SPACE;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.TEXT;
import static com.example.kaushiknsanji.storeapp.data.local.utils.SqliteUtility.UNIQUE;

/**
 * Database Helper class that manages Database creation and Version management.
 *
 * @author Kaushik N Sanji
 */
public class StoreDbHelper extends SQLiteOpenHelper {
    //Constant used for logs
    private static final String LOG_TAG = StoreDbHelper.class.getSimpleName();

    //Constant for the Database Version
    private static final int DATABASE_VERSION = 1;
    //Constant for the Database Name
    private static final String DATABASE_NAME = "inventory.db";
    //Query that creates the Item Table
    private static final String CREATE_TABLE_ITEM
            = CREATE_TABLE + Product.TABLE_NAME
            + OPEN_BRACE
            + Product._ID + SPACE + INTEGER + SPACE + PRIMARY_KEY_AUTOINCREMENT + COMMA + SPACE
            + Product.COLUMN_ITEM_NAME + SPACE + TEXT + NOT + NULL + COMMA + SPACE
            + Product.COLUMN_ITEM_SKU + SPACE + TEXT + NOT + NULL + COMMA + SPACE
            + Product.COLUMN_ITEM_DESCRIPTION + SPACE + TEXT + NOT + NULL + COMMA + SPACE
            + Product.COLUMN_ITEM_CATEGORY_ID + SPACE + INTEGER + COMMA
            + CONSTRAINT + "unique_item_sku" + UNIQUE + OPEN_BRACE + Product.COLUMN_ITEM_SKU + CLOSE_BRACE + ON + CONFLICT_FAIL + COMMA
            + CONSTRAINT + "fk_category_id"
            + FOREIGN_KEY + OPEN_BRACE + Product.COLUMN_ITEM_CATEGORY_ID + CLOSE_BRACE
            + REFERENCES + ProductCategory.TABLE_NAME + OPEN_BRACE + ProductCategory._ID + CLOSE_BRACE
            + CLOSE_BRACE;
    //Query that creates the Item Category Table
    private static final String CREATE_TABLE_ITEM_CATEGORY
            = CREATE_TABLE + ProductCategory.TABLE_NAME
            + OPEN_BRACE
            + ProductCategory._ID + SPACE + INTEGER + SPACE + PRIMARY_KEY_AUTOINCREMENT + COMMA + SPACE
            + ProductCategory.COLUMN_ITEM_CATEGORY_NAME + SPACE + TEXT + NOT + NULL + COMMA
            + CONSTRAINT + "unique_category_name" + UNIQUE + OPEN_BRACE + ProductCategory.COLUMN_ITEM_CATEGORY_NAME + CLOSE_BRACE + ON + CONFLICT_FAIL
            + CLOSE_BRACE;
    //Query that creates the Item Image Table
    private static final String CREATE_TABLE_ITEM_IMAGE
            = CREATE_TABLE + ProductImage.TABLE_NAME
            + OPEN_BRACE
            + ProductImage.COLUMN_ITEM_ID + SPACE + INTEGER + COMMA + SPACE
            + ProductImage.COLUMN_ITEM_IMAGE_URI + SPACE + TEXT + COMMA + SPACE
            + ProductImage.COLUMN_ITEM_IMAGE_DEFAULT + SPACE + INTEGER + NOT + NULL + DEFAULT + ProductImage.ITEM_IMAGE_NON_DEFAULT + COMMA
            + CONSTRAINT + "unique_image_uri" + UNIQUE + OPEN_BRACE + ProductImage.COLUMN_ITEM_ID + COMMA + SPACE + ProductImage.COLUMN_ITEM_IMAGE_URI + CLOSE_BRACE + COMMA
            + CONSTRAINT + "fk_item_id"
            + FOREIGN_KEY + OPEN_BRACE + ProductImage.COLUMN_ITEM_ID + CLOSE_BRACE
            + REFERENCES + Product.TABLE_NAME + OPEN_BRACE + Product._ID + CLOSE_BRACE
            + ON + DELETE_CASCADE
            + CLOSE_BRACE;
    //Query that creates the Item Attribute Table
    private static final String CREATE_TABLE_ITEM_ATTR
            = CREATE_TABLE + ProductAttribute.TABLE_NAME
            + OPEN_BRACE
            + ProductAttribute.COLUMN_ITEM_ID + SPACE + INTEGER + COMMA + SPACE
            + ProductAttribute.COLUMN_ITEM_ATTR_NAME + SPACE + TEXT + NOT + NULL + COMMA + SPACE
            + ProductAttribute.COLUMN_ITEM_ATTR_VALUE + SPACE + TEXT + NOT + NULL + COMMA
            + CONSTRAINT + "unique_attr_name" + UNIQUE + OPEN_BRACE + ProductImage.COLUMN_ITEM_ID + COMMA + SPACE + ProductAttribute.COLUMN_ITEM_ATTR_NAME + CLOSE_BRACE + COMMA
            + CONSTRAINT + "fk_item_id"
            + FOREIGN_KEY + OPEN_BRACE + ProductAttribute.COLUMN_ITEM_ID + CLOSE_BRACE
            + REFERENCES + Product.TABLE_NAME + OPEN_BRACE + Product._ID + CLOSE_BRACE
            + ON + DELETE_CASCADE
            + CLOSE_BRACE;
    //Query that creates the Supplier Table
    private static final String CREATE_TABLE_SUPPLIER
            = CREATE_TABLE + Supplier.TABLE_NAME
            + OPEN_BRACE
            + Supplier._ID + SPACE + INTEGER + SPACE + PRIMARY_KEY_AUTOINCREMENT + COMMA + SPACE
            + Supplier.COLUMN_SUPPLIER_NAME + SPACE + TEXT + NOT + NULL + COMMA + SPACE
            + Supplier.COLUMN_SUPPLIER_CODE + SPACE + TEXT + NOT + NULL + COMMA
            + CONSTRAINT + "unique_supplier_code" + UNIQUE + OPEN_BRACE + Supplier.COLUMN_SUPPLIER_CODE + CLOSE_BRACE + ON + CONFLICT_FAIL
            + CLOSE_BRACE;
    //Query that creates the Contact Type Table of the Supplier
    private static final String CREATE_TABLE_SUPPLIER_CONTACT_TYPE
            = CREATE_TABLE + SupplierContactType.TABLE_NAME
            + OPEN_BRACE
            + SupplierContactType._ID + SPACE + INTEGER + SPACE + PRIMARY_KEY + COMMA + SPACE
            + SupplierContactType.COLUMN_CONTACT_TYPE_NAME + SPACE + TEXT + NOT + NULL + COMMA
            + CONSTRAINT + "unique_type_name" + UNIQUE + OPEN_BRACE + SupplierContactType.COLUMN_CONTACT_TYPE_NAME + CLOSE_BRACE
            + CLOSE_BRACE;
    //Query that creates the Supplier Contact Table
    private static final String CREATE_TABLE_SUPPLIER_CONTACT
            = CREATE_TABLE + SupplierContact.TABLE_NAME
            + OPEN_BRACE
            + SupplierContact.COLUMN_SUPPLIER_CONTACT_TYPE_ID + SPACE + INTEGER + COMMA + SPACE
            + SupplierContact.COLUMN_SUPPLIER_CONTACT_VALUE + SPACE + TEXT + NOT + NULL + COMMA + SPACE
            + SupplierContact.COLUMN_SUPPLIER_CONTACT_DEFAULT + SPACE + INTEGER + NOT + NULL + DEFAULT + SupplierContact.SUPPLIER_CONTACT_NON_DEFAULT + COMMA + SPACE
            + SupplierContact.COLUMN_SUPPLIER_ID + SPACE + INTEGER + COMMA
            + CONSTRAINT + "unique_record" + UNIQUE
            + OPEN_BRACE + SupplierContact.COLUMN_SUPPLIER_ID + COMMA + SPACE
            + SupplierContact.COLUMN_SUPPLIER_CONTACT_VALUE + CLOSE_BRACE + ON + CONFLICT_REPLACE + COMMA
            + CONSTRAINT + "fk_contact_type_id"
            + FOREIGN_KEY + OPEN_BRACE + SupplierContact.COLUMN_SUPPLIER_CONTACT_TYPE_ID + CLOSE_BRACE
            + REFERENCES + SupplierContactType.TABLE_NAME + OPEN_BRACE + SupplierContactType._ID + CLOSE_BRACE + COMMA
            + CONSTRAINT + "fk_supplier_id"
            + FOREIGN_KEY + OPEN_BRACE + SupplierContact.COLUMN_SUPPLIER_ID + CLOSE_BRACE
            + REFERENCES + Supplier.TABLE_NAME + OPEN_BRACE + Supplier._ID + CLOSE_BRACE
            + ON + DELETE_CASCADE
            + CLOSE_BRACE;
    //Query that creates the Item Supplier Info Table
    private static final String CREATE_TABLE_ITEM_SUPPLIER_INFO
            = CREATE_TABLE + ProductSupplierInfo.TABLE_NAME
            + OPEN_BRACE
            + ProductSupplierInfo.COLUMN_ITEM_ID + SPACE + INTEGER + COMMA + SPACE
            + ProductSupplierInfo.COLUMN_SUPPLIER_ID + SPACE + INTEGER + COMMA + SPACE
            + ProductSupplierInfo.COLUMN_ITEM_UNIT_PRICE + SPACE + REAL + NOT + NULL + DEFAULT + ProductSupplierInfo.DEFAULT_ITEM_UNIT_PRICE + COMMA
            + CONSTRAINT + "unique_record" + UNIQUE
            + OPEN_BRACE + ProductSupplierInfo.COLUMN_ITEM_ID + COMMA + SPACE
            + ProductSupplierInfo.COLUMN_SUPPLIER_ID + CLOSE_BRACE + ON + CONFLICT_REPLACE + COMMA
            + CONSTRAINT + "fk_item_id"
            + FOREIGN_KEY + OPEN_BRACE + ProductSupplierInfo.COLUMN_ITEM_ID + CLOSE_BRACE
            + REFERENCES + Product.TABLE_NAME + OPEN_BRACE + Product._ID + CLOSE_BRACE
            + ON + DELETE_CASCADE + COMMA
            + CONSTRAINT + "fk_supplier_id"
            + FOREIGN_KEY + OPEN_BRACE + ProductSupplierInfo.COLUMN_SUPPLIER_ID + CLOSE_BRACE
            + REFERENCES + Supplier.TABLE_NAME + OPEN_BRACE + Supplier._ID + CLOSE_BRACE
            + ON + DELETE_CASCADE
            + CLOSE_BRACE;
    //Query that creates the Item Supplier Inventory Table
    private static final String CREATE_TABLE_ITEM_SUPPLIER_INVENTORY
            = CREATE_TABLE + ProductSupplierInventory.TABLE_NAME
            + OPEN_BRACE
            + ProductSupplierInventory.COLUMN_ITEM_ID + SPACE + INTEGER + COMMA + SPACE
            + ProductSupplierInventory.COLUMN_SUPPLIER_ID + SPACE + INTEGER + COMMA + SPACE
            + ProductSupplierInventory.COLUMN_ITEM_AVAIL_QUANTITY + SPACE + INTEGER + NOT + NULL + DEFAULT + ProductSupplierInventory.DEFAULT_ITEM_AVAIL_QUANTITY + COMMA
            + CONSTRAINT + "unique_record" + UNIQUE
            + OPEN_BRACE + ProductSupplierInventory.COLUMN_ITEM_ID + COMMA + SPACE
            + ProductSupplierInventory.COLUMN_SUPPLIER_ID + CLOSE_BRACE + ON + CONFLICT_REPLACE + COMMA
            + CONSTRAINT + "fk_item_id"
            + FOREIGN_KEY + OPEN_BRACE + ProductSupplierInventory.COLUMN_ITEM_ID + CLOSE_BRACE
            + REFERENCES + Product.TABLE_NAME + OPEN_BRACE + Product._ID + CLOSE_BRACE
            + ON + DELETE_CASCADE + COMMA
            + CONSTRAINT + "fk_supplier_id"
            + FOREIGN_KEY + OPEN_BRACE + ProductSupplierInventory.COLUMN_SUPPLIER_ID + CLOSE_BRACE
            + REFERENCES + Supplier.TABLE_NAME + OPEN_BRACE + Supplier._ID + CLOSE_BRACE
            + ON + DELETE_CASCADE
            + CLOSE_BRACE;
    //Query that creates an Index on the "available_quantity" column
    //of "item_supplier_inventory" table
    private static final String CREATE_INDEX_SUPPLIER_QUANTITY
            = CREATE_INDEX + "quantity_idx" + ON + ProductSupplierInventory.TABLE_NAME
            + SPACE + OPEN_BRACE + ProductSupplierInventory.COLUMN_ITEM_AVAIL_QUANTITY + CLOSE_BRACE;
    //Stores the singleton instance of this class
    private static volatile StoreDbHelper INSTANCE;

    /**
     * Create a helper object to create, open, and/or manage a database.
     * This method always returns very quickly.  The database is not actually
     * created or opened until one of {@link #getWritableDatabase} or
     * {@link #getReadableDatabase} is called.
     *
     * @param context to use to open or create the database
     */
    private StoreDbHelper(Context context) {
        //Propagating the call to super, to initialize the database
        super(context,
                DATABASE_NAME,
                new AppCursorFactory(), //Custom CursorFactory to log the queries fired
                DATABASE_VERSION
        );
    }

    /**
     * Static Singleton Constructor that creates a single instance of {@link StoreDbHelper}.
     *
     * @param context is the {@link Context} of the Activity used to open/create the database
     * @return New or existing instance of {@link StoreDbHelper}
     */
    public static synchronized StoreDbHelper getInstance(Context context) {
        if (INSTANCE == null) {
            //When instance is not available
            synchronized (StoreDbHelper.class) {
                //Apply lock and check for the instance again
                if (INSTANCE == null) {
                    //When there is no instance, create a new one
                    //Using Application Context instead of Activity Context to prevent memory leaks
                    INSTANCE = new StoreDbHelper(context.getApplicationContext());
                }
            }
        }
        //Returning the instance of StoreDbHelper
        return INSTANCE;
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        //Executing the statements to create the tables in the database: START
        db.execSQL(CREATE_TABLE_ITEM);
        db.execSQL(CREATE_TABLE_ITEM_CATEGORY);
        db.execSQL(CREATE_TABLE_ITEM_IMAGE);
        db.execSQL(CREATE_TABLE_ITEM_ATTR);
        db.execSQL(CREATE_TABLE_SUPPLIER);
        db.execSQL(CREATE_TABLE_SUPPLIER_CONTACT_TYPE);
        db.execSQL(CREATE_TABLE_SUPPLIER_CONTACT);
        db.execSQL(CREATE_TABLE_ITEM_SUPPLIER_INFO);
        db.execSQL(CREATE_TABLE_ITEM_SUPPLIER_INVENTORY);
        //Executing the statements to create the tables in the database: END

        //Creating an Index on the Available Quantity column of the table "item_supplier_inventory"
        db.execSQL(CREATE_INDEX_SUPPLIER_QUANTITY);

        //Inserting predefined set of categories into the 'item_category' table
        insertPredefinedCategories();

        //Inserting predefined set of contact types into the 'contact_type' table
        insertPredefinedContactTypes();
    }

    /**
     * Method that loads a predefined set of categories into the 'item_category' table
     */
    private void insertPredefinedCategories() {
        //Executing in the background thread
        AppExecutors.getInstance().getDiskIO().execute(() -> {
            //Get the Categories to insert
            String[] preloadedCategories = ProductCategory.getPreloadedCategories();

            //Retrieving the database in write mode
            SQLiteDatabase writableDatabase = INSTANCE.getWritableDatabase();

            //Stores the count of records inserted
            int noOfRecordsInserted = 0;

            //Locking the database for insert
            writableDatabase.beginTransaction();
            try {
                //Iterate over the Categories, to insert them one by one
                for (String categoryName : preloadedCategories) {
                    //Preparing the ContentValue for inserting the category
                    ContentValues categoryContentValues = new ContentValues();
                    categoryContentValues.put(ProductCategory.COLUMN_ITEM_CATEGORY_NAME, categoryName);

                    //Executing insert
                    long recordId = writableDatabase.insert(
                            ProductCategory.TABLE_NAME,
                            null,
                            categoryContentValues
                    );

                    //Validating the operation
                    if (recordId == -1) {
                        //Bail out on failure
                        break;
                    } else {
                        //On success of inserting the record

                        //Increment the number of records inserted
                        noOfRecordsInserted++;
                    }
                }
            } finally {

                //Evaluating the number of records inserted to commit accordingly
                if (noOfRecordsInserted == preloadedCategories.length) {
                    //When all categories are inserted successfully, mark the transaction as successful
                    writableDatabase.setTransactionSuccessful();
                    Log.i(LOG_TAG, "insertPredefinedCategories: Predefined Categories inserted");
                } else {
                    //When NOT all categories are inserted, log the error
                    Log.e(LOG_TAG, "insertPredefinedCategories: Predefined Categories failed to insert.");
                }

                //Releasing the lock in the end
                writableDatabase.endTransaction();
            }

        });
    }

    /**
     * Method that loads a predefined set of contact types into the 'contact_type' table
     */
    private void insertPredefinedContactTypes() {
        //Executing in the background thread
        AppExecutors.getInstance().getDiskIO().execute(() -> {
            //Get the Contact types to insert
            String[] preloadedContactTypes = SupplierContactType.getPreloadedContactTypes();

            //Retrieving the database in write mode
            SQLiteDatabase writableDatabase = INSTANCE.getWritableDatabase();

            //Stores the count of records inserted
            int noOfRecordsInserted = 0;

            //Number of Contact types to preload
            int noOfContactTypes = preloadedContactTypes.length;

            //Locking the database for insert
            writableDatabase.beginTransaction();
            try {
                //Iterate over the contact types, to insert them one by one
                for (int index = 0; index < noOfContactTypes; index++) {
                    //Preparing the ContentValue for inserting the contact type
                    ContentValues contactTypeContentValues = new ContentValues();
                    contactTypeContentValues.put(SupplierContactType._ID, String.valueOf(index));
                    contactTypeContentValues.put(SupplierContactType.COLUMN_CONTACT_TYPE_NAME, preloadedContactTypes[index]);

                    //Executing insert
                    long recordId = writableDatabase.insert(
                            SupplierContactType.TABLE_NAME,
                            null,
                            contactTypeContentValues
                    );

                    //Validating the operation
                    if (recordId == -1) {
                        //Bail out on failure
                        break;
                    } else {
                        //On success of inserting the record

                        //Increment the number of records inserted
                        noOfRecordsInserted++;
                    }
                }
            } finally {

                //Evaluating the number of records inserted to commit accordingly
                if (noOfRecordsInserted == noOfContactTypes) {
                    //When all contact types are inserted successfully, mark the transaction as successful
                    writableDatabase.setTransactionSuccessful();
                    Log.i(LOG_TAG, "insertPredefinedContactTypes: Predefined Contact types inserted");
                } else {
                    //When NOT all contact types are inserted, log the error
                    Log.e(LOG_TAG, "insertPredefinedContactTypes: Predefined Contact types failed to insert.");
                }

                //Releasing the lock in the end
                writableDatabase.endTransaction();
            }

        });
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Dropping the Database tables and recreating it on Version Upgrade

        //Dropping the tables
        db.execSQL("DROP TABLE IF EXISTS " + Product.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProductCategory.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProductImage.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProductAttribute.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Supplier.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SupplierContactType.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SupplierContact.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProductSupplierInfo.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProductSupplierInventory.TABLE_NAME);

        //Dropping the Indexes manually created
        db.execSQL("DROP INDEX quantity_idx");

        //Recreating all the tables
        onCreate(db);
    }

    /**
     * Called when the database connection is being configured, to enable features such as
     * write-ahead logging or foreign key support.
     * <p>
     * This method is called before {@link #onCreate}, {@link #onUpgrade}, {@link #onDowngrade}, or
     * {@link #onOpen} are called. It should not modify the database except to configure the
     * database connection as required.
     * </p>
     * <p>
     * This method should only call methods that configure the parameters of the database
     * connection, such as {@link SQLiteDatabase#enableWriteAheadLogging}
     * {@link SQLiteDatabase#setForeignKeyConstraintsEnabled}, {@link SQLiteDatabase#setLocale},
     * {@link SQLiteDatabase#setMaximumSize}, or executing PRAGMA statements.
     * </p>
     *
     * @param db The database.
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        //Enabling the Foreign Key Constraints
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            //For API Level 16 and above
            db.setForeignKeyConstraintsEnabled(true);
        } else {
            //For API Level below 16
            //Using the PRAGMA command to enable the Foreign Key Constraints
            String foreignKeyPragmaStr = "PRAGMA foreign_keys = ON";
            db.execSQL(foreignKeyPragmaStr);
        }
    }

    /**
     * Implementation of {@link SQLiteDatabase.CursorFactory} to log the queries fired
     * when executed in debug mode.
     */
    private static class AppCursorFactory implements SQLiteDatabase.CursorFactory {

        /**
         * Execute a query and provide access to its result set through a Cursor
         * interface.
         *
         * @param db          a reference to a Database object that is already constructed
         *                    and opened.
         * @param masterQuery A driver for SQLiteCursors that is used to create them and gets notified
         *                    by the cursors it creates on significant events in their lifetimes.
         * @param editTable   The name of the table used for this query
         * @param query       The {@link SQLiteQuery} object associated with this cursor object.
         * @return Returns a {@link SQLiteCursor} instance to the query.
         */
        @Override
        public Cursor newCursor(SQLiteDatabase db,
                                SQLiteCursorDriver masterQuery,
                                String editTable, SQLiteQuery query) {

            //Log the Query when logging is enabled
            if (AppConstants.LOG_CURSOR_QUERIES) {
                Log.i(LOG_TAG, "Table: " + editTable);
                Log.i(LOG_TAG, "newCursor: " + query.toString());
            }

            //Returning the SQLiteCursor instance for the query fired
            return new SQLiteCursor(masterQuery, editTable, query);
        }
    }
}