package com.example.kaushiknsanji.storeapp.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * Class that saves the Bitmaps downloaded, in a Memory Cache {@link LruCache}
 *
 * @author Kaushik N Sanji
 */
public class BitmapImageCache {
    //Constant for Cache size of the Memory Cache
    private static final int DEFAULT_CACHE_SIZE = 25 * 1024 * 1024; //25MB in bytes

    //For the Singleton instance of this
    private static volatile BitmapImageCache INSTANCE;

    //Memory Cache to save the Bitmaps downloaded
    private LruCache<String, Bitmap> mMemoryCache;

    /**
     * Private Constructor of {@link BitmapImageCache}
     */
    private BitmapImageCache() {
        //Retrieving the current Max Memory available (in bytes)
        final int maxMemory = (int) Runtime.getRuntime().maxMemory();
        //Calculating the safe usable Max Memory which is 1/8th of the current Max Memory available
        final int maxMemoryThreshold = maxMemory / 8;
        //Selecting the cache size based on the current availability
        final int cacheSizeSelected = DEFAULT_CACHE_SIZE > maxMemoryThreshold ? maxMemoryThreshold : DEFAULT_CACHE_SIZE;

        //Initializing the Memory Cache
        mMemoryCache = new LruCache<String, Bitmap>(cacheSizeSelected) {
            /**
             * Returns the size of the entry for {@code key} and {@code value} in
             * terms of bytes rather than the number of entries
             */
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                //Size of the cache now returned will be the size of the entries
                //measured in bytes rather than the number of entries
                return bitmap.getByteCount();
            }

        };
    }

    /**
     * Singleton Constructor of {@link BitmapImageCache}
     *
     * @return Instance of {@link BitmapImageCache}
     */
    private static BitmapImageCache getInstance() {
        if (INSTANCE == null) {
            synchronized (BitmapImageCache.class) {
                if (INSTANCE == null) {
                    //Creating the instance when not available
                    INSTANCE = new BitmapImageCache();
                }
            }
        }
        //Using the previously created instance
        return INSTANCE;
    }

    /**
     * Method that retrieves the Bitmap Image from Memory Cache for the given Image URL
     *
     * @param imageURLStr String containing the Image URL whose Bitmap needs to be retrieved from Memory Cache
     * @return Bitmap of the Image for the Image URL mentioned
     */
    public static Bitmap getBitmapFromCache(String imageURLStr) {
        return getInstance().mMemoryCache.get(imageURLStr);
    }

    /**
     * Method that adds the Bitmap Image to Memory Cache with the Image URL String as the Key
     *
     * @param imageURLStr String containing the Image URL used as the Key to store in Memory Cache
     * @param bitmap      Bitmap Image downloaded from the Image URL passed
     */
    public static void addBitmapToCache(String imageURLStr, Bitmap bitmap) {
        if (getBitmapFromCache(imageURLStr) == null
                && bitmap != null) {
            getInstance().mMemoryCache.put(imageURLStr, bitmap);
        }
    }

    /**
     * Method that clears the entire Memory Cache
     */
    public static void clearCache() {
        getInstance().mMemoryCache.evictAll();
    }
}
