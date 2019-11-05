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

package com.example.kaushiknsanji.storeapp.data.local;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.data.FileRepository;
import com.example.kaushiknsanji.storeapp.utils.AppExecutors;
import com.example.kaushiknsanji.storeapp.utils.FileStorageUtility;
import com.example.kaushiknsanji.storeapp.utils.ImageStorageUtility;

import java.io.IOException;
import java.util.List;

/**
 * The File Repository class that implements {@link FileRepository} interface
 * to manage communication with the files maintained by the App.
 *
 * @author Kaushik N Sanji
 */
public class StoreFileRepository implements FileRepository {

    //Constant used for logs
    private static final String LOG_TAG = StoreFileRepository.class.getSimpleName();

    //Singleton instance of StoreFileRepository
    private static volatile StoreFileRepository INSTANCE;

    //The ContentResolver instance to take URI permissions and to delete the files
    private final ContentResolver mContentResolver;

    //AppExecutors instance for threading requests
    private final AppExecutors mAppExecutors;

    /**
     * Private Constructor of {@link StoreFileRepository}
     *
     * @param contentResolver The {@link ContentResolver} instance to take URI permissions and to delete the files
     * @param appExecutors    {@link AppExecutors} instance for threading requests
     */
    private StoreFileRepository(@NonNull ContentResolver contentResolver, @NonNull AppExecutors appExecutors) {
        mContentResolver = contentResolver;
        mAppExecutors = appExecutors;
    }

    /**
     * Singleton Constructor that creates a single instance of {@link StoreFileRepository}
     *
     * @param contentResolver The {@link ContentResolver} instance to take URI permissions and to delete the files
     * @param appExecutors    {@link AppExecutors} instance for threading requests
     * @return New or existing instance of {@link StoreFileRepository}
     */
    public static StoreFileRepository getInstance(@NonNull ContentResolver contentResolver, @NonNull AppExecutors appExecutors) {
        if (INSTANCE == null) {
            //When instance is not available
            synchronized (StoreFileRepository.class) {
                //Apply lock and check for the instance again
                if (INSTANCE == null) {
                    //When there is no instance, create a new one
                    INSTANCE = new StoreFileRepository(contentResolver, appExecutors);
                }
            }
        }
        //Returning the instance of StoreFileRepository
        return INSTANCE;
    }

    /**
     * Method that saves the Image pointed to by the Content URI {@code fileContentUri}
     * in a file located at the app's private external storage path.
     *
     * @param context            The Context of the Activity/Fragment
     * @param fileContentUri     The Content Uri of the Temporary Image File
     * @param operationsCallback The Callback to be implemented by the caller to receive the result.
     */
    @Override
    public void saveImageToFile(Context context, Uri fileContentUri, FileOperationsCallback<Uri> operationsCallback) {
        if (FileStorageUtility.isExternalStorageMounted()) {
            //When the external storage is mounted, begin the save process in the disk thread

            //Executing on the Disk Thread
            mAppExecutors.getDiskIO().execute(() -> {
                //Stores the URI of the saved image
                Uri savedImageFileUri = null;
                try {
                    //Saving the Image to a file and retrieving its Content URI
                    savedImageFileUri = ImageStorageUtility.saveImage(context, fileContentUri);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "saveImageToFile: Error occurred while saving the image " + fileContentUri, e);
                }

                //Saves the URI to pass the result to the callback
                final Uri finalSavedImageFileUri = savedImageFileUri;
                //Passing the results on the Main Thread
                mAppExecutors.getMainThread().execute(() -> {
                    if (finalSavedImageFileUri != null) {
                        //When the Image is saved successfully, URI will be present
                        operationsCallback.onSuccess(finalSavedImageFileUri);
                    } else {
                        //When the Image failed to save, URI will be absent. Dispatch an error to the callback
                        operationsCallback.onFailure(R.string.product_image_save_error);
                    }
                });
            });

        } else {
            //When the external storage is NOT mounted, dispatch a message to the callback
            operationsCallback.onFailure(R.string.product_image_disk_not_mounted_save_error);
        }
    }

    /**
     * Method that persists the persistable URI permission grant that the system gives the app.
     * Applicable for devices with Android Kitkat (API level 19) and above.
     *
     * @param fileContentUri The Content Uri of a File returned by the Intent
     * @param intentFlags    The existing intent flags on the URI
     */
    @Override
    public void takePersistablePermissions(Uri fileContentUri, int intentFlags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //Including Read and Write permissions
            final int takeFlags = intentFlags
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            mContentResolver.takePersistableUriPermission(fileContentUri, takeFlags);
        }
    }

    /**
     * Method that deletes the Image files passed in {@code fileContentUriList}
     *
     * @param fileContentUriList List of String URIs (Content URIs) of the Image Files to be deleted.
     * @param operationsCallback The Callback to be implemented by the caller to receive the result.
     */
    @Override
    public void deleteImageFiles(List<String> fileContentUriList, FileOperationsCallback<Boolean> operationsCallback) {
        if (FileStorageUtility.isExternalStorageMounted()) {
            //When the external storage is mounted, begin the delete process in the disk thread

            //Executing on the Disk Thread
            mAppExecutors.getDiskIO().execute(() -> {
                //Saves the final result of the operation
                int noOfFilesDeleted = 0;

                //Iterating over the list and deleting the files one by one
                for (String fileContentUriStr : fileContentUriList) {
                    boolean fileDeleted = ImageStorageUtility.deleteImageFile(
                            Uri.parse(fileContentUriStr),
                            mContentResolver
                    );
                    noOfFilesDeleted += fileDeleted ? 1 : 0;
                }

                //Saving to pass the result to the callback
                final int finalNoOfFilesDeleted = noOfFilesDeleted;

                //Passing the results on the Main Thread
                mAppExecutors.getMainThread().execute(() -> {
                    if (finalNoOfFilesDeleted == fileContentUriList.size()) {
                        //When all files were deleted successfully
                        operationsCallback.onSuccess(true);
                    } else {
                        //When not all files were deleted, dispatch the error to the callback
                        operationsCallback.onFailure(R.string.product_image_delete_error);
                    }
                });
            });
        }
    }

    /**
     * Method that deletes the Image files passed in {@code fileContentUriList} silently
     * without reporting success/failure back to the caller
     *
     * @param fileContentUriList List of String URIs (Content URIs) of the Image Files to be deleted.
     */
    @Override
    public void deleteImageFilesSilently(List<String> fileContentUriList) {
        //no-op
        //This is implemented by the StoreRepository
    }


}
