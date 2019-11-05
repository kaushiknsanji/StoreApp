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

package com.example.kaushiknsanji.storeapp.data.local.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Parcelable Model class for storing and updating the details of the Supplier.
 *
 * @author Kaushik N Sanji
 */
public class Supplier implements Parcelable {

    /**
     * Implementation of {@link android.os.Parcelable.Creator} interface
     * to generate instances of this Parcelable class {@link Supplier} from a {@link Parcel}
     */
    public static final Creator<Supplier> CREATOR = new Creator<Supplier>() {
        /**
         * Creates an instance of this Parcelable class {@link Supplier} from
         * a given Parcel whose data had been previously written by #writeToParcel() method
         *
         * @param in The Parcel to read the object's data from.
         * @return Returns a new instance of this Parcelable class {@link Supplier}
         */
        @Override
        public Supplier createFromParcel(Parcel in) {
            return new Supplier(in);
        }

        /**
         * Creates a new array of this Parcelable class {@link Supplier}
         *
         * @param size Size of the array
         * @return Returns an array of this Parcelable class {@link Supplier}, with every
         * entry initialized to null
         */
        @Override
        public Supplier[] newArray(int size) {
            return new Supplier[size];
        }
    };
    //The Primary Key/ID of the Supplier
    private final int mId;
    //The Name of the Supplier
    private String mName;
    //The Unique Code of the Supplier
    private String mCode;
    //List of Supplier's Contacts
    private ArrayList<SupplierContact> mContacts;
    //List of Products with their Selling Price info
    private ArrayList<ProductSupplierInfo> mProductSupplierInfoList;

    /**
     * Private Constructor of {@link Supplier}
     *
     * @param id                      The Integer Primary Key/ID of the Supplier
     * @param name                    The Name of the Supplier
     * @param code                    The Unique Code of the Supplier
     * @param contacts                {@link ArrayList} of Supplier's Contacts {@link SupplierContact}
     * @param productSupplierInfoList {@link ArrayList} of Products with their Selling Price info {@link ProductSupplierInfo}
     */
    private Supplier(int id,
                     String name,
                     String code,
                     ArrayList<SupplierContact> contacts,
                     ArrayList<ProductSupplierInfo> productSupplierInfoList) {
        mId = id;
        mName = name;
        mCode = code;
        mContacts = contacts;
        mProductSupplierInfoList = productSupplierInfoList;
    }

    /**
     * Parcelable constructor that de-serializes the data from a Parcel {@code in} passed
     *
     * @param in The Instance of the Parcel class containing the serialized data
     */
    protected Supplier(Parcel in) {
        mId = in.readInt();
        mName = in.readString();
        mCode = in.readString();
        mContacts = in.createTypedArrayList(SupplierContact.CREATOR);
        mProductSupplierInfoList = in.createTypedArrayList(ProductSupplierInfo.CREATOR);
    }

    /**
     * Flattens/Serializes the object of {@link Supplier} into a Parcel
     *
     * @param dest  The Parcel in which the object should be written
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mName);
        dest.writeString(mCode);
        dest.writeTypedList(mContacts);
        dest.writeTypedList(mProductSupplierInfoList);
    }

    /**
     * Describes the kinds of special objects contained in this Parcelable
     * instance's marshaled representation.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     */
    @Override
    public int describeContents() {
        return 0; //Indicating with no mask
    }

    /**
     * Getter Method for Primary Key/ID of the Supplier
     *
     * @return The Integer Primary Key/ID of the Supplier
     */
    public int getId() {
        return mId;
    }

    /**
     * Getter Method for the Name of the Supplier
     *
     * @return The Name of the Supplier
     */
    public String getName() {
        return mName;
    }

    /**
     * Setter Method to set the Name of the Supplier
     *
     * @param name The Name of the Supplier
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * Getter Method for the Code of the Supplier
     *
     * @return The Code of the Supplier
     */
    public String getCode() {
        return mCode;
    }

    /**
     * Setter Method to set the Code of the Supplier
     *
     * @param code The Unique Code of the Supplier
     */
    public void setCode(String code) {
        mCode = code;
    }

    /**
     * Getter Method for the List of Supplier's Contacts
     *
     * @return An {@link ArrayList} of Supplier's Contacts {@link SupplierContact}
     */
    public ArrayList<SupplierContact> getContacts() {
        return mContacts;
    }

    /**
     * Setter Method to set the List of Supplier's Contacts
     *
     * @param contacts {@link ArrayList} of Supplier's Contacts {@link SupplierContact}
     */
    public void setContacts(ArrayList<SupplierContact> contacts) {
        mContacts = contacts;
    }

    /**
     * Getter Method for the List of Products with their Selling Price info
     *
     * @return {@link ArrayList} of Products with their Selling Price info {@link ProductSupplierInfo}
     */
    public ArrayList<ProductSupplierInfo> getProductSupplierInfoList() {
        return mProductSupplierInfoList;
    }

    /**
     * Setter Method to set the List of Products with their Selling Price info
     *
     * @param productSupplierInfoList {@link ArrayList} of Products with their Selling Price info {@link ProductSupplierInfo}
     */
    public void setProductSupplierInfoList(ArrayList<ProductSupplierInfo> productSupplierInfoList) {
        mProductSupplierInfoList = productSupplierInfoList;
    }

    /**
     * Static Builder class that constructs {@link Supplier}
     */
    public static class Builder {

        private int mId;
        private String mName;
        private String mCode;
        private ArrayList<SupplierContact> mContacts;
        private ArrayList<ProductSupplierInfo> mProductSupplierInfoList;

        /**
         * Setter for the Integer Primary Key/ID of the Supplier
         *
         * @param id Integer Primary Key/ID of the Supplier
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setId(int id) {
            mId = id;
            return this;
        }

        /**
         * Setter for the Name of the Supplier
         *
         * @param name The Name of the Supplier
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setName(String name) {
            mName = name;
            return this;
        }

        /**
         * Setter for the Code of the Supplier
         *
         * @param code The Unique Code of the Supplier
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setCode(String code) {
            mCode = code;
            return this;
        }

        /**
         * Setter for the List of Supplier's Contacts
         *
         * @param contacts {@link ArrayList} of Supplier's Contacts {@link SupplierContact}
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setContacts(ArrayList<SupplierContact> contacts) {
            mContacts = contacts;
            return this;
        }

        /**
         * Setter for the List of Products with their Selling Price info
         *
         * @param productSupplierInfoList {@link ArrayList} of Products with their Selling Price info {@link ProductSupplierInfo}
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setProductSupplierInfoList(ArrayList<ProductSupplierInfo> productSupplierInfoList) {
            mProductSupplierInfoList = productSupplierInfoList;
            return this;
        }

        /**
         * Terminal Method that constructs the {@link Supplier}
         *
         * @return Instance of {@link Supplier} built
         */
        public Supplier createSupplier() {
            //Returning the instance built
            return new Supplier(mId, mName, mCode, mContacts, mProductSupplierInfoList);
        }
    }
}
