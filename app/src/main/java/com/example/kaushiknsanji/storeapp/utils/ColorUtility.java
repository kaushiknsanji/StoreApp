package com.example.kaushiknsanji.storeapp.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ArrayRes;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;

/**
 * Utility Class that deals with reading and modifying Colors.
 *
 * @author Kaushik N Sanji
 */
public class ColorUtility {

    /**
     * Private Constructor to avoid direct instantiation of {@link ColorUtility}
     */
    private ColorUtility() {
    }

    /**
     * Method that reads the Typed Array {@code colorArrayRes} and returns an Integer Array of Colors read.
     *
     * @param context         The {@link Context} to use for reading the {@link TypedArray}
     *                        and retrieving the Colors from Color Resource @{@link ColorRes}
     * @param colorArrayRes   The {@link TypedArray} resource of Colors to be read
     * @param defaultColorRes The Integer value of the Color Resource @{@link ColorRes} to use as a default Color
     *                        in case when an attribute cannot be read from TypedArray or is not a defined resource.
     * @return Integer array of the Colors read from the {@link TypedArray}
     */
    public static int[] obtainColorsFromTypedArray(Context context, @ArrayRes int colorArrayRes, @ColorRes int defaultColorRes) {
        //Obtaining the Typed Array of Colors from the Resources
        TypedArray typedArrayColors = context.getResources().obtainTypedArray(colorArrayRes);
        //Get the number of Colors
        int noOfColors = typedArrayColors.length();
        //Creating an integer array for the size
        int[] colors = new int[noOfColors];
        //Retrieving the default color from the resources
        int defaultColorInt = ContextCompat.getColor(context, defaultColorRes);
        //Iterating over the TypedArray to get the colors
        for (int index = 0; index < noOfColors; index++) {
            colors[index] = typedArrayColors.getColor(index, defaultColorInt);
        }
        //Release the TypedArray resource
        typedArrayColors.recycle();
        //Returning the colors read from the TypedArray
        return colors;
    }
}
