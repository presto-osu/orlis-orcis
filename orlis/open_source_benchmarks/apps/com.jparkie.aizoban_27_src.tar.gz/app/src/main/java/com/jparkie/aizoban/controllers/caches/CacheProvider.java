package com.jparkie.aizoban.controllers.caches;

import com.jakewharton.disklrucache.DiskLruCache;
import com.jparkie.aizoban.AizobanApplication;
import com.jparkie.aizoban.utils.DiskUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class CacheProvider {
    private static final String PARAMETER_CACHE_DIRECTORY = "chapter_disk_cache";
    private static final int PARAMETER_APP_VERSION = 1;
    private static final int PARAMETER_VALUE_COUNT = 1;
    private static final long PARAMETER_CACHE_SIZE = 10 * 1024 * 1024;

    private static CacheProvider sInstance;

    private DiskLruCache mDiskCache;

    private CacheProvider() {
        try {
            mDiskCache = DiskLruCache.open(
                    new File(AizobanApplication.getInstance().getCacheDir(), PARAMETER_CACHE_DIRECTORY),
                    PARAMETER_APP_VERSION,
                    PARAMETER_VALUE_COUNT,
                    PARAMETER_CACHE_SIZE
            );
        } catch (IOException e) {
            // Do Nothing.
        }
    }

    public static CacheProvider getInstance() {
        if (sInstance == null) {
            sInstance = new CacheProvider();
        }

        return sInstance;
    }

    public synchronized String[] getImageUrlsFromDiskCache(String chapterUrl) throws IOException{
        DiskLruCache.Snapshot snapshot = null;

        try {
            String key = DiskUtils.hashKeyForDisk(chapterUrl);

            snapshot = mDiskCache.get(key);

            String joinedImageUrls = snapshot.getString(0);
            return joinedImageUrls.split(",");
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }
    }

    public synchronized void putImageUrlsToDiskCache(String chapterUrl, List<String> imageUrls) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int index = 0; index < imageUrls.size(); index++) {
            if (index == 0) {
                stringBuilder.append(imageUrls.get(index));
            } else {
                stringBuilder.append(",");
                stringBuilder.append(imageUrls.get(index));
            }
        }
        String cachedValue = stringBuilder.toString();

        DiskLruCache.Editor editor = null;
        OutputStream outputStream = null;
        try {
            String key = DiskUtils.hashKeyForDisk(chapterUrl);
            editor = mDiskCache.edit(key);

            if (editor == null) {
                return;
            }

            outputStream = new BufferedOutputStream(editor.newOutputStream(0));
            outputStream.write(cachedValue.getBytes());
            outputStream.flush();

            mDiskCache.flush();
            editor.commit();
        } catch (IOException e) {
            if (editor != null) {
                try {
                    editor.abort();
                } catch (IOException ignore) {
                    // Do Nothing.
                }
            }
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ignore) {
                    // Do Nothing.
                }
            }
        }
    }

    public synchronized File getCacheDir() {
        return mDiskCache.getDirectory();
    }
}
