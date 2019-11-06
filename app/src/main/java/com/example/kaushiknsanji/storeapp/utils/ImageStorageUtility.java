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

package com.example.kaushiknsanji.storeapp.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.media.ExifInterface;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class that manages the stuff related to images and its storage.
 *
 * @author Kaushik N Sanji
 */
public final class ImageStorageUtility {

    //Constant for the Image File Provider
    private static final String IMAGE_FILE_PROVIDER_AUTHORITY
            = AppConstants.APPLICATION_ID + ".fileprovider";

    //JPEG File extension constant
    private static final String JPEG_FILE_EXT = ".jpg";

    //Filename Prefix constant for a permanent image
    private static final String FILE_NAME_PREFIX = "IMG_";

    //Filename Prefix constant for a temporary image
    private static final String TEMP_FILE_NAME_PREFIX = "TMP_";

    //Filename Suffix constant which is a timestamp to make the filename unique
    private static final String FILE_TIMESTAMP_PATTERN = "yyyyMMdd_HHmmss";

    /**
     * Private Constructor to avoid direct instantiation of {@link ImageStorageUtility}
     */
    private ImageStorageUtility() {
        //Suppressing with an error to enforce noninstantiability
        throw new AssertionError("No " + this.getClass().getCanonicalName() + " instances for you!");
    }

    /**
     * Method that generates and returns the FILE_TIMESTAMP_PATTERN
     * for the current Datetime.
     *
     * @return Timestamp String for the current Datetime in FILE_TIMESTAMP_PATTERN
     */
    private static String getCurrentTimestampPattern() {
        return new SimpleDateFormat(FILE_TIMESTAMP_PATTERN, Locale.getDefault()).format(new Date());
    }

    /**
     * Method that creates and returns a Temporary Image File for writing an Image.
     *
     * @param context {@link Context} used for checking Content URI possibility
     *                and for retrieving the App's Cache Directories
     * @return A Temporary Image {@link File} in the App's Cache directory.
     * @throws IOException when the App's Cache directory could not be created or accessed.
     */
    @NonNull
    public static File createTempImageFile(Context context) throws IOException {
        return FileStorageUtility.createTempFile(context, IMAGE_FILE_PROVIDER_AUTHORITY,
                TEMP_FILE_NAME_PREFIX + getCurrentTimestampPattern(),
                JPEG_FILE_EXT,
                ContextCompat.getExternalCacheDirs(context)
        );
    }

    /**
     * Method that creates and returns an Image File for writing an Image.
     *
     * @param context {@link Context} used for checking Content URI possibility
     *                and for retrieving the App's External File Directories
     * @return A Permanent Image {@link File} in the App's External Files Directory.
     * @throws IOException when the App's External Files directory could not be created or accessed.
     */
    @NonNull
    public static File createImageFile(Context context) throws IOException {
        return FileStorageUtility.createFile(context, IMAGE_FILE_PROVIDER_AUTHORITY,
                FILE_NAME_PREFIX + getCurrentTimestampPattern(),
                JPEG_FILE_EXT,
                ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_PICTURES)
        );
    }

    /**
     * Method that decodes and returns the {@link Bitmap} from the file pointed to by
     * the Content URI {@code fileContentUri}.
     *
     * @param fileContentUri  The Content URI of the Image File to be decoded
     * @param contentResolver The {@link ContentResolver} instance to open a stream to the Image file.
     * @return The {@link Bitmap} decoded from the Image file {@code fileContentUri}
     * @throws IOException when opening and closing the stream to the Image file.
     */
    @Nullable
    public static Bitmap getBitmapFromContentUri(Uri fileContentUri, ContentResolver contentResolver) throws IOException {
        //Opening the stream to the Image file pointed by fileContentUri
        InputStream uriInputStream = contentResolver.openInputStream(fileContentUri);
        //Stores the decoded Bitmap
        Bitmap decodedBitmap = null;

        if (uriInputStream != null) {
            //When the stream is opened
            try {
                //Decoding into Bitmap
                decodedBitmap = BitmapFactory.decodeStream(uriInputStream);
            } finally {
                //Closing the stream when done
                uriInputStream.close();
            }
        }
        //Returning the decoded Bitmap of the Image
        return decodedBitmap;
    }

    /**
     * Method that decodes and returns an Optimized {@link Bitmap} from the file pointed to by
     * the Content URI {@code fileContentUri}.
     *
     * @param context        {@link Context} to get the {@link ContentResolver} and the Window dimensions.
     * @param fileContentUri The Content URI of the Image File to be decoded
     * @return An Optimized {@link Bitmap} decoded from the Image file {@code fileContentUri}
     * @throws IOException when opening and closing the stream to the Image file.
     */
    @Nullable
    public static Bitmap getOptimizedBitmapFromContentUri(Context context, Uri fileContentUri) throws IOException {
        //Retrieving the ContentResolver instance
        ContentResolver contentResolver = context.getContentResolver();

        //Get the device target dimensions (Normalizing to 50 percent of the value)
        int targetW = (int) (WindowDimensionsUtility.getDisplayWindowWidth(context) * 0.5);
        int targetH = (int) (WindowDimensionsUtility.getDisplayWindowHeight(context) * 0.5);

        //Creating an Instance of BitmapFactory Options to decode the dimensions of the original
        //Bitmap from the File Content URI
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inJustDecodeBounds = true; //Decoding for Bounds only

        //Retrieving the Stream to the file's Content URI
        try (InputStream decodeBoundsInputStream = contentResolver.openInputStream(fileContentUri)) {
            //Decoding the dimensions of the original bitmap from the stream
            BitmapFactory.decodeStream(decodeBoundsInputStream, null, bitmapOptions);
        }

        //Reading the bitmap's original dimensions
        int photoW = bitmapOptions.outWidth;
        int photoH = bitmapOptions.outHeight;

        //Calculating the amount to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        //Decode the image using the scaling factor determined
        bitmapOptions.inJustDecodeBounds = false; //Decoding the Image
        bitmapOptions.inSampleSize = scaleFactor;

        //Stores the optimized decoded Bitmap
        Bitmap optimizedBitmap = null;

        //Retrieving the Stream to the file's Content URI
        InputStream decodeBitmapInputStream = contentResolver.openInputStream(fileContentUri);
        if (decodeBitmapInputStream != null) {
            //When the stream is opened
            try {
                //Decoding into Bitmap with the scaled down dimensions
                optimizedBitmap = BitmapFactory.decodeStream(decodeBitmapInputStream, null, bitmapOptions);
            } finally {
                //Closing the stream when done
                decodeBitmapInputStream.close();
            }
        }

        //Returning the optimized decoded Bitmap of the Image
        return optimizedBitmap;
    }

    /**
     * Method that returns the Content URI to the {@link File} passed.
     *
     * @param context A {@link Context} required by the {@link android.support.v4.content.FileProvider}
     * @param file    The Image {@link File} instance
     * @return Content URI to the Image File {@code file}
     */
    public static Uri getContentUriForImageFile(Context context, File file) {
        return FileStorageUtility.getContentUriForFile(context, IMAGE_FILE_PROVIDER_AUTHORITY, file);
    }

    /**
     * Method that saves the Image captured in the temporary Image file pointed to by the Content URI {@code fileContentUri}
     * to a permanent file stored in the App's External Files directory determined.
     *
     * @param context        A {@link Context} to create the Output Image File, read the input
     *                       temporary Image and for preparing the Content URI of the output Image File.
     * @param fileContentUri Content URI of the Temporary Image File.
     * @return Content URI of the Image File if written successfully else {@code NULL}
     * @throws IOException when opening and closing the stream to the temporary Image file.
     */
    @Nullable
    public static Uri saveImage(Context context, Uri fileContentUri) throws IOException {
        //Creating the Output Image File
        File outputImageFile = createImageFile(context);

        //Rotating the Image based on the EXIF information captured in the temporary Image File
        Bitmap bitmap = rotateImageBasedOnExif(context, fileContentUri);

        //When we have the bitmap
        if (bitmap != null) {
            //Save the Image

            //Stores the success of the write operation
            boolean writeSuccess;

            //For writing the decoded Image to a File
            try (FileOutputStream fileOutputStream = new FileOutputStream(outputImageFile)) {
                //Opening the Output Stream to the Output Image File
                //Writing the Image to the file with 100% quality
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                //Marking the operation as successful
                writeSuccess = true;
            } catch (Exception e) {
                //Marking the operation as failure on Exception
                writeSuccess = false;
            }

            //When the Bitmap was written successfully to a File
            if (writeSuccess) {
                //Add the image to the system gallery
                addPhotoToGallery(context, outputImageFile);

                //Delete temporary image file
                deleteImageFile(fileContentUri, context.getContentResolver());

                //Returning the Uri of the Saved image
                return getContentUriForImageFile(context, outputImageFile);
            }
        }

        //Returning NULL when the process did not complete successfully
        return null;
    }

    /**
     * Method that returns the rotated/orientation-corrected version of the Bitmap Image pointed to by the Content URI
     * of the Image file {@code fileContentUri} based on its ExifInterface data.
     *
     * @param context        A {@link Context} to get the {@link ContentResolver} instance.
     * @param fileContentUri Content URI of the Input Image file.
     * @return New Bitmap which is the rotated/orientation-corrected version of the Input Image {@code fileContentUri}
     * @throws IOException when opening and closing the stream to the input Image file.
     */
    @Nullable
    private static Bitmap rotateImageBasedOnExif(Context context, Uri fileContentUri) throws IOException {
        //Retrieving the ContentResolver instance
        ContentResolver contentResolver = context.getContentResolver();
        //Decoding the Bitmap from the Temporary Image File URI
        Bitmap bitmap = getBitmapFromContentUri(fileContentUri, contentResolver);

        //Returning NULL when the decoded Bitmap is NULL
        if (bitmap == null) {
            return null;
        }

        //Opening the Stream to the Temporary Image File URI
        InputStream uriInputStream = contentResolver.openInputStream(fileContentUri);
        if (uriInputStream != null) {
            //When the stream is opened
            try {
                //Retrieve the ExifInterface of the Image
                ExifInterface exifInterface = new ExifInterface(uriInputStream);
                //Get the Orientation from the ExifInterface data
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                //Returning the rotated Image based on the Orientation degree determined
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        //For 90 degree clockwise rotated Image

                        //Returning 90 degree clockwise rotated Image
                        return rotateImage(bitmap, 90);
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        //For 180 degree clockwise rotated Image

                        //Returning 180 degree clockwise rotated Image
                        return rotateImage(bitmap, 180);
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        //For 270 degree clockwise rotated Image

                        //Returning 270 degree clockwise rotated Image
                        return rotateImage(bitmap, 270);
                    default:
                        //For no rotation, returning the temporary bitmap image AS-IS
                        return bitmap;
                }
            } finally {
                //Closing the stream when done
                uriInputStream.close();
            }
        }
        //On all else, returning the Bitmap of Temporary Image
        return bitmap;
    }

    /**
     * Method that rotates the Bitmap {@code bitmap} by the {@code degrees} mentioned
     * and returns the new Rotated version of the input bitmap.
     *
     * @param bitmap  The input {@link Bitmap} image to be rotated.
     * @param degrees Float value of the degrees to rotate the Image in clockwise direction.
     * @return New Rotated version of the input {@code bitmap} by the {@code degrees} mentioned
     */
    private static Bitmap rotateImage(Bitmap bitmap, float degrees) {
        //Creating a New Image Matrix
        Matrix matrix = new Matrix();
        //Rotating the Image about its center by the degrees mentioned
        matrix.setRotate(degrees, (float) bitmap.getWidth() / (float) 2, (float) bitmap.getHeight() / (float) 2);
        //Creating a new Rotated Bitmap Image using the Matrix created
        Bitmap bitmapRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        //Purging the temporary input bitmap image
        bitmap.recycle();
        //Returning the new Bitmap Image which is the rotated version of the input
        return bitmapRotated;
    }

    /**
     * Method that adds the Image File {@code savedImageFile} to the System's Gallery
     * by broadcasting the Intent. Works only for files stored in External Storage Public directory.
     *
     * @param context        {@link Context} to initiate the Broadcast
     * @param savedImageFile Image File to be added to the System's Gallery
     */
    private static void addPhotoToGallery(Context context, File savedImageFile) {
        //Creating a Broadcast Action Intent to Media scanner
        Intent mediaScanBroadcastIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        //Retrieving the File URI to the Image File
        Uri fileUri = Uri.fromFile(savedImageFile);
        //Passing the URI of the File in the intent
        mediaScanBroadcastIntent.setData(fileUri);
        //Sending the Broadcast to make the image show up in System's Gallery
        context.sendBroadcast(mediaScanBroadcastIntent);
    }

    /**
     * Method that deletes the Image file pointed to by the Content URI {@code fileContentUri}.
     *
     * @param fileContentUri  Content URI of the Image Files that needs to be deleted.
     * @param contentResolver Instance of {@link ContentResolver} to delete the Image File.
     * @return Boolean that indicates whether the Image file was deleted or not.
     * <br/><b>TRUE</b> if deleted; <b>FALSE</b> otherwise.
     */
    public static boolean deleteImageFile(Uri fileContentUri, ContentResolver contentResolver) {
        return FileStorageUtility.deleteFile(fileContentUri, contentResolver, IMAGE_FILE_PROVIDER_AUTHORITY);
    }
}
