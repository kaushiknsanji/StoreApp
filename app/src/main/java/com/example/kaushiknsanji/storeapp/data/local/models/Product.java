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
 * Parcelable Model class for storing and updating the details of the Product.
 *
 * @author Kaushik N Sanji
 */
public class Product implements Parcelable {

    /**
     * Implementation of {@link android.os.Parcelable.Creator} interface
     * to generate instances of this Parcelable class {@link Product} from a {@link Parcel}
     */
    public static final Creator<Product> CREATOR = new Creator<Product>() {
        /**
         * Creates an instance of this Parcelable class {@link Product} from
         * a given Parcel whose data had been previously written by #writeToParcel() method
         *
         * @param in The Parcel to read the object's data from.
         * @return Returns a new instance of this Parcelable class {@link Product}
         */
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        /**
         * Creates a new array of this Parcelable class {@link Product}
         *
         * @param size Size of the array
         * @return Returns an array of this Parcelable class {@link Product}, with every
         * entry initialized to null
         */
        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };
    //The Primary Key/ID of the Product
    private final int mId;
    //The Name of the Product
    private String mName;
    //The Unique SKU of the Product
    private String mSku;
    //The Description of the Product
    private String mDescription;
    //The Category of the Product
    private String mCategory;
    //List of Product Images
    private ArrayList<ProductImage> mProductImages;
    //List of Additional Attributes of the Product
    private ArrayList<ProductAttribute> mProductAttributes;

    /**
     * Private Constructor of {@link Product}
     *
     * @param id                The Integer Primary Key/ID of the Product
     * @param name              The Name of the Product
     * @param sku               The Unique SKU of the Product
     * @param description       The Description of the Product
     * @param category          The Category of the Product
     * @param productImages     List of Product Images {@link ProductImage}
     * @param productAttributes List of Product Attributes {@link ProductAttribute}
     */
    private Product(int id, String name, String sku, String description, String category,
                    ArrayList<ProductImage> productImages,
                    ArrayList<ProductAttribute> productAttributes) {
        mId = id;
        mName = name;
        mSku = sku;
        mDescription = description;
        mCategory = category;
        mProductImages = productImages;
        mProductAttributes = productAttributes;
    }

    /**
     * Parcelable constructor that de-serializes the data from a Parcel {@code in} passed
     *
     * @param in The Instance of the Parcel class containing the serialized data
     */
    protected Product(Parcel in) {
        mId = in.readInt();
        mName = in.readString();
        mSku = in.readString();
        mDescription = in.readString();
        mCategory = in.readString();
        mProductImages = in.createTypedArrayList(ProductImage.CREATOR);
        mProductAttributes = in.createTypedArrayList(ProductAttribute.CREATOR);
    }

    /**
     * Flattens/Serializes the object of {@link Product} into a Parcel
     *
     * @param dest  The Parcel in which the object should be written
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mName);
        dest.writeString(mSku);
        dest.writeString(mDescription);
        dest.writeString(mCategory);
        dest.writeTypedList(mProductImages);
        dest.writeTypedList(mProductAttributes);
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
    public int getId() {
        return mId;
    }

    /**
     * Getter method for the Name of the Product
     *
     * @return The Name of the Product
     */
    public String getName() {
        return mName;
    }

    /**
     * Setter method to set the Name of the Product
     *
     * @param name The Name of the Product
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * Getter Method for the SKU of the Product
     *
     * @return The SKU of the Product
     */
    public String getSku() {
        return mSku;
    }

    /**
     * Setter Method to set the SKU of the Product
     *
     * @param sku The Unique SKU of the Product
     */
    public void setSku(String sku) {
        mSku = sku;
    }

    /**
     * Getter Method for the Description of the Product
     *
     * @return The Description of the Product
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Setter Method to set the Description of the Product
     *
     * @param description The Description of the Product
     */
    public void setDescription(String description) {
        mDescription = description;
    }

    /**
     * Getter Method for the Category of the Product
     *
     * @return The Category of the Product
     */
    public String getCategory() {
        return mCategory;
    }

    /**
     * Setter Method to set the Category of the Product
     *
     * @param category The Category of the Product
     */
    public void setCategory(String category) {
        mCategory = category;
    }

    /**
     * Getter Method for the List of Product Images
     *
     * @return The {@link ArrayList} of {@link ProductImage}
     */
    public ArrayList<ProductImage> getProductImages() {
        return mProductImages;
    }

    /**
     * Setter Method to set the List of Product Images
     *
     * @param productImages The {@link ArrayList} of {@link ProductImage}
     */
    public void setProductImages(ArrayList<ProductImage> productImages) {
        mProductImages = productImages;
    }

    /**
     * Getter Method for the List of Product Attributes
     *
     * @return The {@link ArrayList} of {@link ProductAttribute}
     */
    public ArrayList<ProductAttribute> getProductAttributes() {
        return mProductAttributes;
    }

    /**
     * Setter Method to set the List of Product Attributes
     *
     * @param productAttributes The {@link ArrayList} of {@link ProductAttribute}
     */
    public void setProductAttributes(ArrayList<ProductAttribute> productAttributes) {
        mProductAttributes = productAttributes;
    }

    /**
     * Static Builder class that constructs {@link Product}
     */
    public static class Builder {
        private int mId;
        private String mName;
        private String mSku;
        private String mDescription;
        private String mCategory;
        private ArrayList<ProductImage> mProductImages;
        private ArrayList<ProductAttribute> mProductAttributes;

        /**
         * Setter for Integer Primary Key/ID of the Product
         *
         * @param id Integer Primary Key/ID of the Product
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setId(int id) {
            mId = id;
            return this;
        }

        /**
         * Setter for the Name of the Product
         *
         * @param name The Name of the Product
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setName(String name) {
            mName = name;
            return this;
        }

        /**
         * Setter for the SKU of the Product
         *
         * @param sku The Unique SKU of the Product
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setSku(String sku) {
            mSku = sku;
            return this;
        }

        /**
         * Setter for the Description of the Product
         *
         * @param description The Description of the Product
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setDescription(String description) {
            mDescription = description;
            return this;
        }

        /**
         * Setter for the Category of the Product
         *
         * @param category The Category of the Product
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setCategory(String category) {
            mCategory = category;
            return this;
        }

        /**
         * Setter for the List of Product Images
         *
         * @param productImages The {@link ArrayList} of {@link ProductImage}
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setProductImages(ArrayList<ProductImage> productImages) {
            mProductImages = productImages;
            return this;
        }

        /**
         * Setter for the List of Product Attributes
         *
         * @param productAttributes The {@link ArrayList} of {@link ProductAttribute}
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setProductAttributes(ArrayList<ProductAttribute> productAttributes) {
            mProductAttributes = productAttributes;
            return this;
        }

        /**
         * Terminal Method that constructs the {@link Product}
         *
         * @return Instance of {@link Product} built
         */
        public Product createProduct() {
            //Initializing Product Images List of not initialized
            if (mProductImages == null) mProductImages = new ArrayList<>();
            //Initializing Product Attributes List of not initialized
            if (mProductAttributes == null) mProductAttributes = new ArrayList<>();
            //Returning the instance built
            return new Product(mId, mName, mSku, mDescription, mCategory, mProductImages, mProductAttributes);
        }
    }
}
