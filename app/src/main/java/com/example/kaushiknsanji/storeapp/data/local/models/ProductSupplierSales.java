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
 * inventory of the Product at the Supplier.
 *
 * @author Kaushik N Sanji
 */
public class ProductSupplierSales implements Parcelable, Cloneable {

    /**
     * Implementation of {@link android.os.Parcelable.Creator} interface
     * to generate instances of this Parcelable class {@link ProductSupplierSales} from a {@link Parcel}
     */
    public static final Creator<ProductSupplierSales> CREATOR = new Creator<ProductSupplierSales>() {
        /**
         * Creates an instance of this Parcelable class {@link ProductSupplierSales} from
         * a given Parcel whose data had been previously written by #writeToParcel() method
         *
         * @param in The Parcel to read the object's data from.
         * @return Returns a new instance of this Parcelable class {@link ProductSupplierSales}
         */
        @Override
        public ProductSupplierSales createFromParcel(Parcel in) {
            return new ProductSupplierSales(in);
        }

        /**
         * Creates a new array of this Parcelable class {@link ProductSupplierSales}
         *
         * @param size Size of the array
         * @return Returns an array of this Parcelable class {@link ProductSupplierSales}, with every
         * entry initialized to null
         */
        @Override
        public ProductSupplierSales[] newArray(int size) {
            return new ProductSupplierSales[size];
        }
    };
    //The Primary Key/ID of the Product
    private final int mItemId;
    //The Primary Key/ID of the Supplier
    private final int mSupplierId;
    //The Name of the Supplier
    private final String mSupplierName;
    //The Unique Code of the Supplier
    private final String mSupplierCode;
    //The Selling Price of the Product by the Supplier
    private final float mUnitPrice;
    //The Available Quantity to Sell at the Supplier
    private int mAvailableQuantity;

    /**
     * Private Constructor of {@link ProductSupplierSales}
     *
     * @param itemId            The Integer Primary Key/ID of the Product
     * @param supplierId        The Integer Primary Key/ID of the Supplier
     * @param supplierName      The Name of the Supplier
     * @param supplierCode      The Unique Code of the Supplier
     * @param unitPrice         The Selling Price of the Product by the Supplier
     * @param availableQuantity The Available Quantity to Sell at the Supplier
     */
    private ProductSupplierSales(int itemId, int supplierId, String supplierName, String supplierCode, float unitPrice, int availableQuantity) {
        mItemId = itemId;
        mSupplierId = supplierId;
        mSupplierName = supplierName;
        mSupplierCode = supplierCode;
        mUnitPrice = unitPrice;
        mAvailableQuantity = availableQuantity;
    }

    /**
     * Parcelable constructor that de-serializes the data from a Parcel {@code in} passed
     *
     * @param in The Instance of the Parcel class containing the serialized data
     */
    protected ProductSupplierSales(Parcel in) {
        mItemId = in.readInt();
        mSupplierId = in.readInt();
        mSupplierName = in.readString();
        mSupplierCode = in.readString();
        mUnitPrice = in.readFloat();
        mAvailableQuantity = in.readInt();
    }

    /**
     * Flattens/Serializes the object of {@link ProductSupplierSales} into a Parcel
     *
     * @param dest  The Parcel in which the object should be written
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mItemId);
        dest.writeInt(mSupplierId);
        dest.writeString(mSupplierName);
        dest.writeString(mSupplierCode);
        dest.writeFloat(mUnitPrice);
        dest.writeInt(mAvailableQuantity);
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
     * Getter Method for Primary Key/ID of the Product
     *
     * @return The Integer Primary Key/ID of the Product
     */
    public int getItemId() {
        return mItemId;
    }

    /**
     * Getter Method for Primary Key/ID of the Supplier
     *
     * @return The Integer Primary Key/ID of the Supplier
     */
    public int getSupplierId() {
        return mSupplierId;
    }

    /**
     * Getter Method for the Name of the Supplier
     *
     * @return The Name of the Supplier
     */
    public String getSupplierName() {
        return mSupplierName;
    }

    /**
     * Getter Method for the Code of the Supplier
     *
     * @return The Code of the Supplier
     */
    public String getSupplierCode() {
        return mSupplierCode;
    }

    /**
     * Getter Method for the Selling Price of the Product by the Supplier
     *
     * @return The Float value of the Selling Price of the Product by the Supplier
     */
    public float getUnitPrice() {
        return mUnitPrice;
    }

    /**
     * Getter Method for the Available Quantity to Sell at the Supplier
     *
     * @return Integer value of the Available Quantity to Sell at the Supplier
     */
    public int getAvailableQuantity() {
        return mAvailableQuantity;
    }

    /**
     * Setter Method to set the Available Quantity to Sell at the Supplier
     *
     * @param availableQuantity Integer value of the Available Quantity to Sell at the Supplier
     */
    public void setAvailableQuantity(int availableQuantity) {
        mAvailableQuantity = availableQuantity;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param o The reference object with which to compare.
     * @return <b>TRUE</b> if this object is the same as the {@code o}
     * argument; <b>FALSE</b> otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductSupplierSales that = (ProductSupplierSales) o;

        if (mItemId != that.mItemId) return false;
        if (mSupplierId != that.mSupplierId) return false;
        if (Float.compare(that.mUnitPrice, mUnitPrice) != 0) return false;
        if (mAvailableQuantity != that.mAvailableQuantity) return false;
        if (!mSupplierName.equals(that.mSupplierName)) return false;
        return mSupplierCode.equals(that.mSupplierCode);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        int result = mItemId;
        result = 31 * result + mSupplierId;
        result = 31 * result + mSupplierName.hashCode();
        result = 31 * result + mSupplierCode.hashCode();
        result = 31 * result + (mUnitPrice != +0.0f ? Float.floatToIntBits(mUnitPrice) : 0);
        result = 31 * result + mAvailableQuantity;
        return result;
    }

    /**
     * Creates and returns a copy of this object.
     *
     * @return a clone of this instance.
     * @see Cloneable
     */
    @Override
    public final Object clone() {
        //Returning a new instance of ProductSupplierSales constructed using the Builder
        return new Builder()
                .setItemId(this.mItemId)
                .setSupplierId(this.mSupplierId)
                .setSupplierName(this.mSupplierName)
                .setSupplierCode(this.mSupplierCode)
                .setUnitPrice(this.mUnitPrice)
                .setAvailableQuantity(this.mAvailableQuantity)
                .createProductSupplierSales();
    }

    /**
     * Returns a string representation of the object.
     *
     * @return String representation of the {@link ProductSupplierSales}
     */
    @Override
    public String toString() {
        return "ProductSupplierSales{" +
                "mItemId=" + mItemId +
                ", mSupplierId=" + mSupplierId +
                ", mSupplierName='" + mSupplierName + '\'' +
                ", mSupplierCode='" + mSupplierCode + '\'' +
                ", mUnitPrice=" + mUnitPrice +
                ", mAvailableQuantity=" + mAvailableQuantity +
                '}';
    }

    /**
     * Static Builder class that constructs {@link ProductSupplierSales}
     */
    public static class Builder {

        private int mItemId;
        private int mSupplierId;
        private String mSupplierName;
        private String mSupplierCode;
        private float mUnitPrice = SalesContract.ProductSupplierInfo.DEFAULT_ITEM_UNIT_PRICE;
        private int mAvailableQuantity = SalesContract.ProductSupplierInventory.DEFAULT_ITEM_AVAIL_QUANTITY;

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
         * Setter for the Name of the Supplier
         *
         * @param supplierName The Name of the Supplier
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setSupplierName(String supplierName) {
            mSupplierName = supplierName;
            return this;
        }

        /**
         * Setter for the Code of the Supplier
         *
         * @param supplierCode The Unique Code of the Supplier
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setSupplierCode(String supplierCode) {
            mSupplierCode = supplierCode;
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
         * Setter for the Available Quantity to Sell at the Supplier
         *
         * @param availableQuantity Integer value of the Available Quantity to Sell at the Supplier
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setAvailableQuantity(int availableQuantity) {
            mAvailableQuantity = availableQuantity;
            return this;
        }

        /**
         * Terminal Method that constructs the {@link ProductSupplierSales}
         *
         * @return Instance of {@link ProductSupplierSales} built
         */
        public ProductSupplierSales createProductSupplierSales() {
            return new ProductSupplierSales(mItemId, mSupplierId, mSupplierName, mSupplierCode, mUnitPrice, mAvailableQuantity);
        }
    }
}
