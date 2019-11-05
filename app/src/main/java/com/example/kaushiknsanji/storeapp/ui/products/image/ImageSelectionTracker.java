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

package com.example.kaushiknsanji.storeapp.ui.products.image;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Parcelable and Cloneable Model Class for storing/tracking/updating
 * the state of the Images selected in RecyclerView Grid of Product photos
 * shown by {@link ProductImageActivityFragment}
 *
 * @author Kaushik N Sanji
 */
public class ImageSelectionTracker implements Parcelable, Cloneable {

    /**
     * Implementation of {@link android.os.Parcelable.Creator} interface
     * to generate instances of this Parcelable class {@link ImageSelectionTracker} from a {@link Parcel}
     */
    public static final Creator<ImageSelectionTracker> CREATOR = new Creator<ImageSelectionTracker>() {

        /**
         * Creates an instance of this Parcelable class {@link ImageSelectionTracker} from
         * a given Parcel whose data had been previously written by #writeToParcel() method
         *
         * @param in The Parcel to read the object's data from.
         * @return Returns a new instance of this Parcelable class {@link ImageSelectionTracker}
         */
        @Override
        public ImageSelectionTracker createFromParcel(Parcel in) {
            return new ImageSelectionTracker(in);
        }

        /**
         * Creates a new array of this Parcelable class {@link ImageSelectionTracker}
         *
         * @param size Size of the array
         * @return Returns an array of this Parcelable class {@link ImageSelectionTracker}, with every
         * entry initialized to null
         */
        @Override
        public ImageSelectionTracker[] newArray(int size) {
            return new ImageSelectionTracker[size];
        }
    };
    //The Adapter Item Position
    private final int mPosition;
    //Grid Mode (SELECT/DELETE)
    @NonNull
    @ProductImageContract.PhotoGridSelectModeDef
    private final String mPhotoGridMode;
    //The Content URI of the Image
    private final String mImageContentUri;
    //Boolean that says whether the item was selected or unselected
    private boolean mSelected;

    /**
     * Private Constructor of {@link ImageSelectionTracker}
     *
     * @param position        The Integer position of the Adapter Item
     * @param photoGridMode   The Grid Mode as in {@link com.example.kaushiknsanji.storeapp.ui.products.image.ProductImageContract.PhotoGridSelectModeDef}
     * @param imageContentUri The Content URI of the Image
     * @param selected        The selected state of the Image/Adapter Item
     */
    private ImageSelectionTracker(int position,
                                  @NonNull @ProductImageContract.PhotoGridSelectModeDef String photoGridMode,
                                  String imageContentUri, boolean selected) {
        mPosition = position;
        mPhotoGridMode = photoGridMode;
        mImageContentUri = imageContentUri;
        mSelected = selected;
    }

    /**
     * Parcelable constructor that de-serializes the data from a Parcel {@code in} passed
     *
     * @param in The Instance of the Parcel class containing the serialized data
     */
    protected ImageSelectionTracker(Parcel in) {
        mPosition = in.readInt();
        mPhotoGridMode = in.readString();
        mImageContentUri = in.readString();
        mSelected = in.readByte() != 0;
    }

    /**
     * Flattens/Serializes the object of {@link ImageSelectionTracker} into a Parcel
     *
     * @param dest  The Parcel in which the object should be written
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mPosition);
        dest.writeString(mPhotoGridMode);
        dest.writeString(mImageContentUri);
        dest.writeByte((byte) (mSelected ? 1 : 0));
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
     * Getter for the selected state of the {@link ImageSelectionTracker}
     *
     * @return <b>TRUE</b> to indicate that the Image was selected for SELECT/DELETE action;
     * <b>FALSE</b> otherwise
     */
    public boolean isSelected() {
        return mSelected;
    }

    /**
     * Setter that updates the selected state of the {@link ImageSelectionTracker}
     *
     * @param selected <b>TRUE</b> to indicate that the Image was selected for SELECT/DELETE action;
     *                 <b>FALSE</b> otherwise
     */
    public void setSelected(boolean selected) {
        mSelected = selected;
    }

    /**
     * Getter for the Position of the adapter item tracked.
     *
     * @return Integer value of the Position of the adapter item tracked.
     */
    public int getPosition() {
        return mPosition;
    }

    /**
     * Getter for the Mode (SELECT/DELETE) of the {@link ImageSelectionTracker}
     *
     * @return String value as in {@link com.example.kaushiknsanji.storeapp.ui.products.image.ProductImageContract.PhotoGridSelectModeDef}
     */
    @NonNull
    public String getPhotoGridMode() {
        return mPhotoGridMode;
    }

    /**
     * Getter for the Content URI of the Image file selected for SELECT/DELETE action.
     *
     * @return The Content URI of the Image file selected
     */
    public String getImageContentUri() {
        return mImageContentUri;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return String representation of the {@link ImageSelectionTracker}
     */
    @Override
    public String toString() {
        return "ImageSelectionTracker{" +
                "mPosition=" + mPosition +
                ", mPhotoGridMode='" + mPhotoGridMode + '\'' +
                ", mSelected=" + mSelected +
                '}';
    }

    /**
     * Creates and returns a copy of this object.
     *
     * @return a clone of this instance.
     * @see Cloneable
     */
    @Override
    protected final Object clone() {
        //Returning a new instance of ImageSelectionTracker constructed using the Builder
        return new Builder()
                .setSelected(this.mSelected)
                .setPhotoGridMode(this.mPhotoGridMode)
                .setImageContentUri(this.mImageContentUri)
                .setPosition(this.mPosition)
                .createTracker();
    }

    /**
     * Static Builder class that constructs {@link ImageSelectionTracker}
     */
    public static class Builder {

        private int mPosition;
        private String mPhotoGridMode;
        private String mImageContentUri;
        private boolean mSelected = false; //Defaulting the selected state to FALSE(unselected)

        /**
         * Setter for the Position of the adapter item tracked.
         *
         * @param position Integer value of the Position of the adapter item tracked.
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setPosition(int position) {
            mPosition = position;
            return this;
        }

        /**
         * Setter for Mode (SELECT/DELETE) of the {@link ImageSelectionTracker}
         *
         * @param photoGridMode String value as in {@link com.example.kaushiknsanji.storeapp.ui.products.image.ProductImageContract.PhotoGridSelectModeDef}
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setPhotoGridMode(String photoGridMode) {
            mPhotoGridMode = photoGridMode;
            return this;
        }

        /**
         * Setter for the Content URI of the Image file selected for SELECT/DELETE action.
         *
         * @param imageContentUri The Content URI of the Image file selected
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setImageContentUri(String imageContentUri) {
            mImageContentUri = imageContentUri;
            return this;
        }

        /**
         * Setter for the selected state of the {@link ImageSelectionTracker}
         *
         * @param selected <b>TRUE</b> to indicate that the Image was selected for SELECT/DELETE action;
         *                 <b>FALSE</b> otherwise
         * @return Instance of {@link Builder} for chaining method calls.
         */
        public Builder setSelected(boolean selected) {
            mSelected = selected;
            return this;
        }

        /**
         * Terminal Method that constructs the {@link ImageSelectionTracker}
         *
         * @return Instance of {@link ImageSelectionTracker} built
         */
        public ImageSelectionTracker createTracker() {
            return new ImageSelectionTracker(mPosition, mPhotoGridMode, mImageContentUri, mSelected);
        }
    }
}
