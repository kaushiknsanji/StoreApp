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

import com.example.kaushiknsanji.storeapp.data.local.contracts.SalesContract;

/**
 * Parcelable and Cloneable Model Class for storing and updating the
 * Selling Price details of the Product by the Supplier.
 *
 * @author Kaushik N Sanji
 */
public class ProductSupplierInfo implements Parcelable, Cloneable {

    /**
     * Implementation of {@link android.os.Parcelable.Creator} interface
     * to generate instances of this Parcelable class {@link ProductSupplierInfo} from a {@link Parcel}
     */
    public static final Creator<ProductSupplierInfo> CREATOR = new Creator<ProductSupplierInfo>() {
        /**
         * Creates an instance of this Parcelable class {@link ProductSupplierInfo} from
         * a given Parcel whose data had been previously written by #writeToParcel() method
         *
         * @param in The Parcel to read the object's data from.
         * @return Returns a new instance of this Parcelable class {@link ProductSupplierInfo}
         */
        @Override
        public ProductSupplierInfo createFromParcel(Parcel in) {
            return new ProductSupplierInfo(in);
        }

        /**
         * Creates a new array of this Parcelable class {@link ProductSupplierInfo}
         *
         * @param size Size of the array
         * @return Returns an array of this Parcelable class {@link ProductSupplierInfo}, with every
         * entry initialized to null
         */
        @Override
        public ProductSupplierInfo[] newArray(int size) {
            return new ProductSupplierInfo[size];
        }
    };
    //The Primary Key/ID of the Product
    private final int mItemId;
    //The Primary Key/ID of the Supplier
    private final int mSupplierId;
    //The Selling Price of the Product by the Supplier
    private float mUnitPrice;

    /**
     * Private Constructor of {@link ProductSupplierInfo}
     *
     * @param itemId     The Integer Primary Key/ID of the Product
     * @param supplierId The Integer Primary Key/ID of the Supplier
     * @param unitPrice  The Selling Price of the Product by the Supplier
     */
    private ProductSupplierInfo(final int itemId, final int supplierId, float unitPrice) {
        mItemId = itemId;
        mSupplierId = supplierId;
        mUnitPrice = unitPrice;
    }

    /**
     * Parcelable constructor that de-serializes the data from a Parcel {@code in} passed
     *
     * @param in The Instance of the Parcel class containing the serialized data
     */
    protected ProductSupplierInfo(Parcel in) {
        mItemId = in.readInt();
        mSupplierId = in.readInt();
        mUnitPrice = in.readFloat();
    }

    /**
     * Flattens/Serializes the object of {@link ProductSupplierInfo} into a Parcel
     *
     * @param dest  The Parcel in which the object should be written
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mItemId);
        dest.writeInt(mSupplierId);
        dest.writeFloat(mUnitPrice);
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
     * Getter method for Primary Key/ID of the Product
     *
     * @return The Integer Primary Key/ID of the Product
     */
    public int getItemId() {
        return mItemId;
    }

    /**
     * Getter method for Primary Key/ID of the Supplier
     *
     * @return The Integer Primary Key/ID of the Supplier
     */
    public int getSupplierId() {
        return mSupplierId;
    }

    /**
     * Getter method for the Selling Price of the Product by the Supplier
     *
     * @return The Float value of the Selling Price of the Product by the Supplier
     */
    public float getUnitPrice() {
        return mUnitPrice;
    }

    /**
     * Setter Method to set the Selling Price of the Product by the Supplier
     *
     * @param unitPrice The Float value of the Selling Price of the Product by the Supplier
     */
    public void setUnitPrice(float unitPrice) {
        mUnitPrice = unitPrice;
    }

    /**
     * Creates and returns a copy of this object.
     *
     * @return a clone of this instance.
     * @see Cloneable
     */
    @Override
    public final Object clone() {
        //Returning a new instance of ProductSupplierInfo constructed using the Builder
        return new Builder()
                .setItemId(this.mItemId)
                .setSupplierId(this.mSupplierId)
                .setUnitPrice(this.mUnitPrice)
                .createProductSupplierInfo();
    }

    /**
     * Returns a string representation of the object.
     *
     * @return String representation of the {@link ProductSupplierInfo}
     */
    @Override
    public String toString() {
        return "ProductSupplierInfo{" +
                "mItemId=" + mItemId +
                ", mSupplierId=" + mSupplierId +
                ", mUnitPrice=" + mUnitPrice +
                '}';
    }

    /**
     * Static Builder class that constructs {@link ProductSupplierInfo}
     */
    public static class Builder {

        private int mItemId;
        private int mSupplierId;
        private float mUnitPrice = SalesContract.ProductSupplierInfo.DEFAULT_ITEM_UNIT_PRICE;

        /**
         * Setter for Integer Primary Key/ID of the Product
         *
         * @param itemId Integer Primary Key/ID of the Product
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setItemId(int itemId) {
            mItemId = itemId;
            return this;
        }

        /**
         * Setter for Integer Primary Key/ID of the Supplier
         *
         * @param supplierId Integer Primary Key/ID of the Supplier
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setSupplierId(int supplierId) {
            mSupplierId = supplierId;
            return this;
        }

        /**
         * Setter for the float value of the Selling Price of the Product by the Supplier
         *
         * @param unitPrice The Float value of the Selling Price of the Product by the Supplier
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setUnitPrice(float unitPrice) {
            mUnitPrice = unitPrice;
            return this;
        }

        /**
         * Terminal Method that constructs the {@link ProductSupplierInfo}
         *
         * @return Instance of {@link ProductSupplierInfo} built
         */
        public ProductSupplierInfo createProductSupplierInfo() {
            return new ProductSupplierInfo(mItemId, mSupplierId, mUnitPrice);
        }
    }
}
