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

/**
 * Parcelable and Cloneable Model Class for storing and updating the Product Image
 * details of the Product.
 *
 * @author Kaushik N Sanji
 */
public class ProductImage implements Parcelable, Cloneable {

    /**
     * Implementation of {@link android.os.Parcelable.Creator} interface
     * to generate instances of this Parcelable class {@link ProductImage} from a {@link Parcel}
     */
    public static final Creator<ProductImage> CREATOR = new Creator<ProductImage>() {
        /**
         * Creates an instance of this Parcelable class {@link ProductImage} from
         * a given Parcel whose data had been previously written by #writeToParcel() method
         *
         * @param in The Parcel to read the object's data from.
         * @return Returns a new instance of this Parcelable class {@link ProductImage}
         */
        @Override
        public ProductImage createFromParcel(Parcel in) {
            return new ProductImage(in);
        }

        /**
         * Creates a new array of this Parcelable class {@link ProductImage}
         *
         * @param size Size of the array
         * @return Returns an array of this Parcelable class {@link ProductImage}, with every
         * entry initialized to null
         */
        @Override
        public ProductImage[] newArray(int size) {
            return new ProductImage[size];
        }
    };
    //The Content Uri of the Product Image
    private final String mImageUri;
    //Denotes whether the Image is a Default image or not
    private boolean mIsDefault;

    /**
     * Private Constructor of {@link ProductImage}
     *
     * @param imageUri  The Content Uri of the Product Image
     * @param isDefault A Boolean that denotes whether the Image is a Default image or not
     */
    private ProductImage(String imageUri, boolean isDefault) {
        mImageUri = imageUri;
        mIsDefault = isDefault;
    }

    /**
     * Parcelable constructor that de-serializes the data from a Parcel {@code in} passed
     *
     * @param in The Instance of the Parcel class containing the serialized data
     */
    protected ProductImage(Parcel in) {
        mImageUri = in.readString();
        mIsDefault = in.readByte() != 0;
    }

    /**
     * Flattens/Serializes the object of {@link ProductImage} into a Parcel
     *
     * @param dest  The Parcel in which the object should be written
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mImageUri);
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
     * Getter Method for the Content URI of the Product Image
     *
     * @return The Content URI of the Product Image
     */
    public String getImageUri() {
        return mImageUri;
    }

    /**
     * Getter Method for the Boolean that denotes whether the Image is a default Image or not.
     *
     * @return Boolean that denotes whether the Image is a default Image or not.
     * <br/><b>TRUE</b> if the Image is the default Image for the Product; <b>FALSE</b> otherwise.
     */
    public boolean isDefault() {
        return mIsDefault;
    }

    /**
     * Setter Method for the Product Image to be set as default Image or not.
     *
     * @param isDefault <b>TRUE</b> if the Image should be the default Image for the Product; <b>FALSE</b> otherwise.
     */
    public void setDefault(boolean isDefault) {
        mIsDefault = isDefault;
    }

    /**
     * Creates and returns a copy of this object.
     *
     * @return a clone of this instance.
     * @see Cloneable
     */
    @Override
    public final Object clone() {
        //Returning a new instance of ProductImage constructed using the Builder
        return new Builder()
                .setImageUri(this.mImageUri)
                .setIsDefault(this.mIsDefault)
                .createProductImage();
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

        ProductImage that = (ProductImage) o;

        if (mIsDefault != that.mIsDefault) return false;
        return mImageUri.equals(that.mImageUri);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        int result = mImageUri.hashCode();
        result = 31 * result + (mIsDefault ? 1 : 0);
        return result;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return String representation of the {@link ProductImage}
     */
    @Override
    public String toString() {
        return "ProductImage{" +
                "mImageUri='" + mImageUri + '\'' +
                ", mIsDefault=" + mIsDefault +
                '}';
    }

    /**
     * Static Builder class that constructs {@link ProductImage}
     */
    public static class Builder {

        private String mImageUri;
        private boolean mIsDefault = false;

        /**
         * Setter for the Content URI of the Product Image
         *
         * @param imageUri The Content URI of the Product Image
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setImageUri(String imageUri) {
            mImageUri = imageUri;
            return this;
        }

        /**
         * Setter for the Product Image to be set as default Image or not.
         *
         * @param isDefault <b>TRUE</b> if the Image should be the default Image for the Product; <b>FALSE</b> otherwise.
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setIsDefault(boolean isDefault) {
            mIsDefault = isDefault;
            return this;
        }

        /**
         * Terminal Method that constructs the {@link ProductImage}
         *
         * @return Instance of {@link ProductImage} built
         */
        public ProductImage createProductImage() {
            return new ProductImage(mImageUri, mIsDefault);
        }
    }
}
