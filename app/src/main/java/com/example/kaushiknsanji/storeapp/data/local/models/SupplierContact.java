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
import android.support.annotation.StringDef;

import com.example.kaushiknsanji.storeapp.data.local.contracts.SupplierContract;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Parcelable and Cloneable Model Class for storing and updating the
 * Contact of the Supplier.
 *
 * @author Kaushik N Sanji
 */
public class SupplierContact implements Parcelable, Cloneable {

    /**
     * Implementation of {@link android.os.Parcelable.Creator} interface
     * to generate instances of this Parcelable class {@link SupplierContact} from a {@link Parcel}
     */
    public static final Creator<SupplierContact> CREATOR = new Creator<SupplierContact>() {
        /**
         * Creates an instance of this Parcelable class {@link SupplierContact} from
         * a given Parcel whose data had been previously written by #writeToParcel() method
         *
         * @param in The Parcel to read the object's data from.
         * @return Returns a new instance of this Parcelable class {@link SupplierContact}
         */
        @Override
        public SupplierContact createFromParcel(Parcel in) {
            return new SupplierContact(in);
        }

        /**
         * Creates a new array of this Parcelable class {@link SupplierContact}
         *
         * @param size Size of the array
         * @return Returns an array of this Parcelable class {@link SupplierContact}, with every
         * entry initialized to null
         */
        @Override
        public SupplierContact[] newArray(int size) {
            return new SupplierContact[size];
        }
    };
    //The Type of the Supplier Contact
    private final String mType;
    //Value of the Contact
    private String mValue;
    //Denotes whether the Contact is the default contact for the Supplier or not
    private boolean mIsDefault;

    /**
     * Private Constructor of {@link SupplierContact}
     *
     * @param type      The type of the Supplier Contact as in {@link SupplierContactTypeDef}
     * @param value     The value of the Contact
     * @param isDefault A Boolean that denotes whether the contact is default or not
     */
    private SupplierContact(@SupplierContactTypeDef String type, String value, boolean isDefault) {
        mType = type;
        mValue = value;
        mIsDefault = isDefault;
    }

    /**
     * Parcelable constructor that de-serializes the data from a Parcel {@code in} passed
     *
     * @param in The Instance of the Parcel class containing the serialized data
     */
    protected SupplierContact(Parcel in) {
        mType = in.readString();
        mValue = in.readString();
        mIsDefault = in.readByte() != 0;
    }

    /**
     * Flattens/Serializes the object of {@link SupplierContact} into a Parcel
     *
     * @param dest  The Parcel in which the object should be written
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mType);
        dest.writeString(mValue);
        dest.writeByte((byte) (mIsDefault ? 1 : 0));
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
     * Getter Method for the type of the Supplier Contact
     *
     * @return The type of the Supplier Contact as in {@link SupplierContactTypeDef}
     */
    public String getType() {
        return mType;
    }

    /**
     * Getter Method for the value of the Supplier Contact
     *
     * @return The Value of the Supplier Contact
     */
    public String getValue() {
        return mValue;
    }

    /**
     * Setter Method to set the value of the Supplier Contact
     *
     * @param value The Value of the Supplier Contact
     */
    public void setValue(String value) {
        mValue = value;
    }

    /**
     * Getter Method for the Boolean that denotes whether Supplier contact is default or not
     *
     * @return Boolean that denotes whether Supplier contact is default or not.
     * <br/><b>TRUE</b> if the contact is the default contact for the Supplier; <b>FALSE</b> otherwise.
     */
    public boolean isDefault() {
        return mIsDefault;
    }

    /**
     * Setter Method to set the contact to be the default contact for the Supplier.
     *
     * @param isDefault <b>TRUE</b> if the contact should be the default contact for the Supplier; <b>FALSE</b> otherwise.
     */
    public void setDefault(boolean isDefault) {
        mIsDefault = isDefault;
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

        SupplierContact that = (SupplierContact) o;

        if (mIsDefault != that.mIsDefault) return false;
        if (!mType.equals(that.mType)) return false;
        return mValue != null ? mValue.equals(that.mValue) : that.mValue == null;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        int result = mType.hashCode();
        result = 31 * result + (mValue != null ? mValue.hashCode() : 0);
        result = 31 * result + (mIsDefault ? 1 : 0);
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
        //Returning a new instance of SupplierContact constructed using the Builder
        return new Builder()
                .setType(this.mType)
                .setIsDefault(this.mIsDefault)
                .setValue(this.mValue)
                .createSupplierContact();
    }

    /**
     * Returns a string representation of the object.
     *
     * @return String representation of the {@link SupplierContact}
     */
    @Override
    public String toString() {
        return "SupplierContact{" +
                "mType='" + mType + '\'' +
                ", mValue='" + mValue + '\'' +
                ", mIsDefault=" + mIsDefault +
                '}';
    }

    //Defining Annotation interface for valid Supplier Contact Types
    //Enumerating Annotation with valid Supplier Contact Types
    @StringDef({SupplierContract.SupplierContactType.CONTACT_TYPE_PHONE,
            SupplierContract.SupplierContactType.CONTACT_TYPE_EMAIL})
    //Retaining Annotation till Compile Time
    @Retention(RetentionPolicy.SOURCE)
    public @interface SupplierContactTypeDef {
    }

    /**
     * Static Builder class that constructs {@link SupplierContact}
     */
    public static class Builder {

        private String mType;
        private String mValue;
        private boolean mIsDefault = false;

        /**
         * Setter for the type of the Supplier Contact
         *
         * @param type The type of the Supplier Contact as in {@link SupplierContactTypeDef}
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setType(@SupplierContactTypeDef String type) {
            mType = type;
            return this;
        }

        /**
         * Setter for the value of the Supplier Contact
         *
         * @param value The Value of the Supplier Contact
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setValue(String value) {
            mValue = value;
            return this;
        }

        /**
         * Setter for the contact to be the default contact for the Supplier.
         *
         * @param isDefault <b>TRUE</b> if the contact should be the default contact for the Supplier; <b>FALSE</b> otherwise.
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setIsDefault(boolean isDefault) {
            mIsDefault = isDefault;
            return this;
        }

        /**
         * Terminal Method that constructs the {@link SupplierContact}
         *
         * @return Instance of {@link SupplierContact} built
         */
        public SupplierContact createSupplierContact() {
            return new SupplierContact(mType, mValue, mIsDefault);
        }
    }
}
