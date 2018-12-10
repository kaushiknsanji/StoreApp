package com.example.kaushiknsanji.storeapp.data;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.List;

/**
 * Contract Interface for the App's File management. File communication is
 * implemented by {@link StoreRepository}
 * and {@link com.example.kaushiknsanji.storeapp.data.local.StoreFileRepository}
 *
 * @author Kaushik N Sanji
 */
public interface FileRepository {

    /**
     * Method that saves the Image pointed to by the Content URI {@code fileContentUri}
     * in a file located at the app's private external storage path.
     *
     * @param context            The Context of the Activity/Fragment
     * @param fileContentUri     The Content Uri of the Temporary Image File
     * @param operationsCallback The Callback to be implemented by the caller to receive the result.
     */
    void saveImageToFile(Context context, Uri fileContentUri, FileOperationsCallback<Uri> operationsCallback);

    /**
     * Method that persists the persistable URI permission grant that the system gives the app.
     * Applicable for devices with Android Kitkat (API level 19) and above.
     *
     * @param fileContentUri The Content Uri of a File returned by the Intent
     * @param intentFlags    The existing intent flags on the URI
     */
    void takePersistablePermissions(Uri fileContentUri, int intentFlags);

    /**
     * Method that deletes the Image files passed in {@code fileContentUriList}
     *
     * @param fileContentUriList List of String URIs (Content URIs) of the Image Files to be deleted.
     * @param operationsCallback The Callback to be implemented by the caller to receive the result.
     */
    void deleteImageFiles(List<String> fileContentUriList, FileOperationsCallback<Boolean> operationsCallback);

    /**
     * Method that deletes the Image files passed in {@code fileContentUriList} silently
     * without reporting success/failure back to the caller
     *
     * @param fileContentUriList List of String URIs (Content URIs) of the Image Files to be deleted.
     */
    void deleteImageFilesSilently(List<String> fileContentUriList);

    /**
     * Callback interface for file related operations.
     *
     * @param <T> The type of the results expected when the file operation was executed successfully.
     */
    interface FileOperationsCallback<T> {
        /**
         * Method invoked when the file operation was executed successfully.
         *
         * @param results The results of the operation in the generic type passed.
         */
        void onSuccess(T results);

        /**
         * Method invoked when the file operation failed to complete.
         *
         * @param messageId The String resource of the error message
         *                  for the file operation failure
         * @param args      Variable number of arguments to replace the format specifiers
         *                  in the String resource if any
         */
        default void onFailure(@StringRes int messageId, @Nullable Object... args) {
        }
    }

}
