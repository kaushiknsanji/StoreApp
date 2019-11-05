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

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.example.kaushiknsanji.storeapp.data.local.utils.QueryArgsUtility;

/**
 * Parcelable Model Class that is a lightweight version of {@link Supplier} model for displaying the
 * list of Suppliers available in the database.
 * <p>
 * This model class is a read-only model that is built using the data
 * read from the {@link android.database.Cursor}
 * </p>
 *
 * @author Kaushik N Sanji
 */
public class SupplierLite implements Parcelable {

    /**
     * Implementation of {@link android.os.Parcelable.Creator} interface
     * to generate instances of this Parcelable class {@link SupplierLite} from a {@link Parcel}
     */
    public static final Creator<SupplierLite> CREATOR = new Creator<SupplierLite>() {
        /**
         * Creates an instance of this Parcelable class {@link SupplierLite} from
         * a given Parcel whose data had been previously written by #writeToParcel() method
         *
         * @param in The Parcel to read the object's data from.
         * @return Returns a new instance of this Parcelable class {@link SupplierLite}
         */
        @Override
        public SupplierLite createFromParcel(Parcel in) {
            return new SupplierLite(in);
        }

        /**
         * Creates a new array of this Parcelable class {@link SupplierLite}
         *
         * @param size Size of the array
         * @return Returns an array of this Parcelable class {@link SupplierLite}, with every
         * entry initialized to null
         */
        @Override
        public SupplierLite[] newArray(int size) {
            return new SupplierLite[size];
        }
    };
    //The Primary Key/ID of the Supplier
    private final int mId;
    //The Name of the Supplier
    @NonNull
    private final String mName;
    //The Unique Code of the Supplier
    @NonNull
    private final String mCode;
    //The Default Phone Contact of the Supplier
    private final String mDefaultPhone;
    //The Default Email Contact of the Supplier
    private final String mDefaultEmail;
    //The Number of Products sold by the Supplier
    private final int mItemCount;

    /**
     * Private Constructor of {@link SupplierLite}
     *
     * @param id           The Integer Primary Key/ID of the Supplier
     * @param name         The Name of the Supplier
     * @param code         The Unique Code of the Supplier
     * @param defaultPhone The Default Phone Contact of the Supplier
     * @param defaultEmail The Default Email Contact of the Supplier
     * @param itemCount    The Number of Products sold by the Supplier
     */
    private SupplierLite(int id, @NonNull String name, @NonNull String code, String defaultPhone, String defaultEmail, int itemCount) {
        mId = id;
        mName = name;
        mCode = code;
        mDefaultPhone = defaultPhone;
        mDefaultEmail = defaultEmail;
        mItemCount = itemCount;
    }

    /**
     * Parcelable constructor that de-serializes the data from a Parcel {@code in} passed
     *
     * @param in The Instance of the Parcel class containing the serialized data
     */
    protected SupplierLite(Parcel in) {
        mId = in.readInt();
        mName = in.readString();
        mCode = in.readString();
        mDefaultPhone = in.readString();
        mDefaultEmail = in.readString();
        mItemCount = in.readInt();
    }

    /**
     * Static Factory constructor for {@link SupplierLite} that builds the instance
     * using the data read from the {@link Cursor}
     *
     * @param cursor The {@link Cursor} to this data
     * @return Instance of {@link SupplierLite}
     */
    public static SupplierLite from(Cursor cursor) {
        return new SupplierLite(
                cursor.getInt(QueryArgsUtility.SuppliersShortInfoQuery.COLUMN_SUPPLIER_ID_INDEX),
                cursor.getString(QueryArgsUtility.SuppliersShortInfoQuery.COLUMN_SUPPLIER_NAME_INDEX),
                cursor.getString(QueryArgsUtility.SuppliersShortInfoQuery.COLUMN_SUPPLIER_CODE_INDEX),
                cursor.getString(QueryArgsUtility.SuppliersShortInfoQuery.COLUMN_SUPPLIER_DEFAULT_PHONE_INDEX),
                cursor.getString(QueryArgsUtility.SuppliersShortInfoQuery.COLUMN_SUPPLIER_DEFAULT_EMAIL_INDEX),
                cursor.getInt(QueryArgsUtility.SuppliersShortInfoQuery.COLUMN_SUPPLIER_ITEM_COUNT_INDEX)
        );
    }

    /**
     * Flattens/Serializes the object of {@link SupplierLite} into a Parcel
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
        dest.writeString(mDefaultPhone);
        dest.writeString(mDefaultEmail);
        dest.writeInt(mItemCount);
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
     * Getter method for Primary Key/ID of the Supplier
     *
     * @return The Integer Primary Key/ID of the Supplier
     */
    public int getId() {
        return mId;
    }

    /**
     * Getter method for the Name of the Supplier
     *
     * @return The Name of the Supplier
     */
    @NonNull
    public String getName() {
        return mName;
    }

    /**
     * Getter Method for the Code of the Supplier
     *
     * @return The Code of the Supplier
     */
    @NonNull
    public String getCode() {
        return mCode;
    }

    /**
     * Getter Method for the Default Phone Contact of the Supplier
     *
     * @return The Default Phone Contact of the Supplier
     */
    public String getDefaultPhone() {
        return mDefaultPhone;
    }

    /**
     * Getter Method for the Default Email Contact of the Supplier
     *
     * @return The Default Email Contact of the Supplier
     */
    public String getDefaultEmail() {
        return mDefaultEmail;
    }

    /**
     * Getter Method for the Number of Products sold by the Supplier
     *
     * @return The Number of Products sold by the Supplier
     */
    public int getItemCount() {
        return mItemCount;
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

        SupplierLite that = (SupplierLite) o;

        if (mId != that.mId) return false;
        if (mItemCount != that.mItemCount) return false;
        if (!mName.equals(that.mName)) return false;
        if (!mCode.equals(that.mCode)) return false;
        if (mDefaultPhone != null ? !mDefaultPhone.equals(that.mDefaultPhone) : that.mDefaultPhone != null)
            return false;
        return mDefaultEmail != null ? mDefaultEmail.equals(that.mDefaultEmail) : that.mDefaultEmail == null;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        int result = mId;
        result = 31 * result + mName.hashCode();
        result = 31 * result + mCode.hashCode();
        result = 31 * result + (mDefaultPhone != null ? mDefaultPhone.hashCode() : 0);
        result = 31 * result + (mDefaultEmail != null ? mDefaultEmail.hashCode() : 0);
        result = 31 * result + mItemCount;
        return result;
    }
}
