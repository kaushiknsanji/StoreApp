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

package com.example.kaushiknsanji.storeapp.workers;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.widget.ImageView;

import com.example.kaushiknsanji.storeapp.R;
import com.example.kaushiknsanji.storeapp.cache.BitmapImageCache;

/**
 * Headless {@link Fragment} that manages downloading of Product Images for the
 * file Content URIs pointing to the Images stored in the external storage.
 * <p>
 * <p>Images are downloaded only when not present in the {@link BitmapImageCache}</p>
 * <p>Images retrieved are updated to the corresponding ImageView reference passed</p>
 *
 * @author Kaushik N Sanji
 */
public class ImageDownloaderFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Bitmap> {

    //Constant used for logs and Fragment Tag
    private static final String LOG_TAG = ImageDownloaderFragment.class.getSimpleName();

    //Stores the ImageView component that needs to be updated when the Image is downloaded
    private ImageView mImageView;

    //Stores the Image URL whose Image is to be downloaded
    private String mImageURLStr;

    //Stores the OnFailureListener instance
    private OnFailureListener mOnFailureListener;

    //Stores the OnSuccessListener instance
    private OnSuccessListener mOnSuccessListener;

    /**
     * Static Constructor of the Fragment
     *
     * @param fragmentManager FragmentManager that manages the Fragments
     * @param tagId           The integer position that identifies the view being inflated
     *                        in the RecyclerView's Adapter or any other unique id.
     * @return Instance of the Fragment {@link ImageDownloaderFragment}
     */
    public static ImageDownloaderFragment newInstance(FragmentManager fragmentManager, int tagId) {
        //Creating the Fragment Tag string using the tagId passed
        String fragmentTagStr = LOG_TAG + "_" + tagId;

        //Retrieving the Fragment from the FragmentManager if existing
        ImageDownloaderFragment imageDownloaderFragment
                = (ImageDownloaderFragment) fragmentManager.findFragmentByTag(fragmentTagStr);
        if (imageDownloaderFragment == null) {
            //When the Fragment is being added for the first time

            //Instantiating the Fragment
            imageDownloaderFragment = new ImageDownloaderFragment();
            //Adding the Fragment to Transaction and committing with state losses
            fragmentManager.beginTransaction().add(imageDownloaderFragment, fragmentTagStr).commitAllowingStateLoss();
        }

        //Returning the Fragment instance
        return imageDownloaderFragment;
    }

    /**
     * Method that loads the Image from Memory Cache or downloads the Image from the URL passed
     * if necessary.
     *
     * @param imageView   The ImageView Component to which the Image needs to be updated
     * @param imageURLStr String containing the Image URL whose Image needs to be downloaded.
     * @param loaderId    Integer identifier used while creating this Fragment or any other unique id.
     */
    public void executeAndUpdate(ImageView imageView, String imageURLStr, int loaderId) {
        //Delegating to other overloaded method with the derived instance for LoaderManager
        executeAndUpdate(imageView, imageURLStr, loaderId, obtainLoaderManager(imageView));
    }

    /**
     * Method that loads the Image from Memory Cache or downloads the Image from the URL passed
     * if necessary.
     *
     * @param imageView     The ImageView Component to which the Image needs to be updated
     * @param imageURLStr   String containing the Image URL whose Image needs to be downloaded.
     * @param loaderId      Integer identifier used while creating this Fragment or any other unique id.
     * @param loaderManager Instance of {@link LoaderManager} to use for downloading the image.
     */
    public void executeAndUpdate(ImageView imageView, String imageURLStr, int loaderId, LoaderManager loaderManager) {
        //Saving the parameters passed
        mImageView = imageView;
        mImageURLStr = imageURLStr;

        //Normalizing the loaderId to start from the ImageDownloader's base ID
        loaderId += ImageDownloader.IMAGE_LOADER;

        if (loaderManager == null) {
            //When we do not have the LoaderManager instance for downloading the Image
            //throw a Runtime Exception
            throw new IllegalStateException("LoaderManager is not attached.");
        }

        //Getting the loader at the loaderId if any
        ImageDownloader imageDownloader = getImageDownloader(loaderId, loaderManager);

        //Resetting the ImageView to the default Thumbnail Image for lazy loading
        mImageView.setImageResource(R.drawable.ic_all_product_default);

        //Boolean to check if we need to restart the loader
        boolean isNewImageURLStr = false;
        if (imageDownloader != null) {
            //When we have a previously registered loader

            //Set the Loader to be restarted when the Image URL passed is
            //empty/null or not the same as that of the loader
            isNewImageURLStr = TextUtils.isEmpty(mImageURLStr) || !mImageURLStr.equals(imageDownloader.getImageURLStr());
        }

        if (isNewImageURLStr) {
            //Restarting the Loader when the ImageURL is new
            loaderManager.restartLoader(loaderId, null, this);
        } else {
            //Invoking the Loader AS-IS if the ImageURL is the same
            //or if the Loader is not yet registered with the loaderId passed
            loaderManager.initLoader(loaderId, null, this);
        }
    }

    /**
     * Method that returns the instance of the {@link ImageDownloader} for the
     * Loader Id {@code loaderId} passed.
     *
     * @param loaderId      The Id of the Loader whose Loader instance needs to be looked up
     * @param loaderManager Instance of {@link LoaderManager}
     * @return Instance of {@link ImageDownloader} if found; else {@code null}
     */
    @Nullable
    private ImageDownloader getImageDownloader(int loaderId, LoaderManager loaderManager) {
        //Getting the loader at the loaderId
        Loader<Bitmap> loader = loaderManager.getLoader(loaderId);
        if (loader instanceof ImageDownloader) {
            //Returning the ImageDownloader instance
            return (ImageDownloader) loader;
        } else {
            //Returning NULL when not found
            return null;
        }
    }

    /**
     * Method that retrieves the {@link FragmentActivity} instance required
     * for obtaining the {@link LoaderManager} instance.
     *
     * @param context The {@link Context} to retrieve the Activity from.
     * @return Instance of the {@link FragmentActivity} is any; else {@code null}
     */
    @Nullable
    private FragmentActivity obtainActivity(@Nullable Context context) {
        if (context == null) {
            //Return Null when Null
            return null;
        } else if (context instanceof FragmentActivity) {
            //Return the FragmentActivity instance when Context is of type FragmentActivity
            return (FragmentActivity) context;
        } else if (context instanceof ContextWrapper) {
            //Recall with the Base Context when Context is of type ContextWrapper
            return obtainActivity(((ContextWrapper) context).getBaseContext());
        }
        //Returning Null when we could not derive the Activity instance from the given Context
        return null;
    }

    /**
     * Method that retrieves the {@link LoaderManager} instance for the given view {@code imageView}
     *
     * @param imageView The {@link ImageView} to retrieve the {@link LoaderManager} from.
     * @return Instance of {@link LoaderManager} when the {@link FragmentActivity} was derivable
     * from {@code imageView}; else {@code null}
     */
    @Nullable
    private LoaderManager obtainLoaderManager(ImageView imageView) {
        //Obtaining the Activity from the ImageView
        FragmentActivity activity = obtainActivity(imageView.getContext());
        if (activity != null) {
            //When we have the Activity, return the LoaderManager instance
            return activity.getSupportLoaderManager();
        }
        //Returning Null when Activity could not be derived from ImageView
        return null;
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @NonNull
    @Override
    public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
        //Returning an Instance of ImageDownloader to start the Image download
        return new ImageDownloader(mImageView.getContext(), mImageURLStr);
    }

    /**
     * Called when a previously created loader has finished its load.
     * This is where we display the Bitmap image generated by the loader
     *
     * @param loader      The Loader that has finished.
     * @param bitmapImage The Bitmap Image generated by the Loader.
     */
    @Override
    public void onLoadFinished(@NonNull Loader<Bitmap> loader, Bitmap bitmapImage) {
        if (bitmapImage != null && mImageView != null) {
            //When the bitmap was downloaded successfully and the ImageView is still attached
            onDownloadSuccess(bitmapImage);
        } else if (mImageView != null) {
            //When the bitmap failed to download and the ImageView is still attached
            onDownloadFailure();
        }
    }

    /**
     * Method invoked on failure of downloading the Image, or when the Image URL was invalid
     * This method sets the default image 'R.drawable.ic_all_product_default' to the ImageView
     * and triggers the Failure event if {@link OnFailureListener} is registered.
     */
    private void onDownloadFailure() {
        //Resetting the ImageView to the default Thumbnail Image when the Bitmap failed to download
        mImageView.setImageResource(R.drawable.ic_all_product_default);
        //When the OnFailureListener is registered, dispatch the failure event
        if (mOnFailureListener != null) {
            mOnFailureListener.onFailure();
        }
    }

    /**
     * Method invoked on Success of downloading the Image.
     * This method sets the downloaded Image {@code bitmapImage} to the ImageView
     * and triggers the Success event if {@link OnSuccessListener} is registered.
     *
     * @param bitmapImage The {@link Bitmap} of the Image downloaded
     */
    private void onDownloadSuccess(Bitmap bitmapImage) {
        //Updating the ImageView when the Bitmap is downloaded successfully
        mImageView.setImageBitmap(bitmapImage);
        //When the OnSuccessListener is registered, dispatch the success event
        if (mOnSuccessListener != null) {
            mOnSuccessListener.onSuccess(bitmapImage);
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(@NonNull Loader<Bitmap> loader) {
        //Resetting the ImageView to the default Thumbnail Image
        mImageView.setImageResource(R.drawable.ic_all_product_default);
    }

    /**
     * Method that registers the {@link OnFailureListener} to dispatch failure events.
     *
     * @param listener Instance of {@link OnFailureListener} that wishes to receive failure events.
     * @return Instance of the this Fragment {@link ImageDownloaderFragment} to chain method calls
     */
    public ImageDownloaderFragment setOnFailureListener(OnFailureListener listener) {
        mOnFailureListener = listener;
        return this;
    }

    /**
     * Method that registers the {@link OnSuccessListener} to dispatch success events.
     *
     * @param listener Instance of {@link OnSuccessListener} that wishes to receive success events.
     * @return Instance of the this Fragment {@link ImageDownloaderFragment} to chain method calls
     */
    public ImageDownloaderFragment setOnSuccessListener(OnSuccessListener listener) {
        mOnSuccessListener = listener;
        return this;
    }

    /**
     * Callback interface to be implemented by the Activity/Fragment
     * to receive failure events of Bitmap download operation
     */
    public interface OnFailureListener {
        /**
         * Callback method of {@link OnFailureListener} invoked when the Bitmap
         * fails to download
         */
        void onFailure();
    }

    /**
     * Callback interface to be implemented by the Activity/Fragment
     * to receive success events of Bitmap download operation
     */
    public interface OnSuccessListener {
        /**
         * Callback method of {@link OnSuccessListener} invoked when the Bitmap
         * was downloaded successfully.
         *
         * @param resultBitmap The Bitmap that was downloaded successfully.
         */
        void onSuccess(@NonNull Bitmap resultBitmap);
    }

}
