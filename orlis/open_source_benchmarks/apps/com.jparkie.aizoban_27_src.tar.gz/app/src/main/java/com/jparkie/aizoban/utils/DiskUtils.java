package com.jparkie.aizoban.utils;

import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.jparkie.aizoban.AizobanApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class DiskUtils {
    private static final Pattern DIR_SEPORATOR = Pattern.compile("/");

    private DiskUtils() {
        throw new AssertionError();
    }

    // http://stackoverflow.com/questions/13976982/removable-storage-external-sdcard-path-by-manufacturers
    // http://stackoverflow.com/questions/11281010/how-can-i-get-external-sd-card-path-for-android-4-0
    public static String[] getStorageDirectories() {
        final Set<String> storageDirectories = new HashSet<String>();

        storageDirectories.add(AizobanApplication.getInstance().getFilesDir().getAbsolutePath());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            File[] directories = AizobanApplication.getInstance().getExternalFilesDirs(null);
            if (directories != null) {
                for (File storage : directories) {
                    if (storage != null) {
                        storageDirectories.add(storage.getAbsolutePath());
                    }
                }
            }
        } else {
            final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
            final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
            final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");

            if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
                if (TextUtils.isEmpty(rawExternalStorage)) {
                    storageDirectories.add("/storage/sdcard0" + File.separator + AizobanApplication.getInstance().getPackageName());
                } else {
                    storageDirectories.add(rawExternalStorage + File.separator + AizobanApplication.getInstance().getPackageName());
                }
            } else {
                final String rawUserId;

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    rawUserId = "";
                } else {
                    final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                    final String[] folders = DIR_SEPORATOR.split(path);
                    final String lastFolder = folders[folders.length - 1];
                    boolean isDigit = false;

                    try {
                        Integer.valueOf(lastFolder);
                        isDigit = true;
                    } catch (NumberFormatException e) {
                        // Do Nothing.
                    }

                    rawUserId = isDigit ? lastFolder : "";
                }

                if (TextUtils.isEmpty(rawUserId)) {
                    storageDirectories.add(rawEmulatedStorageTarget + File.separator + AizobanApplication.getInstance().getPackageName());
                } else {
                    storageDirectories.add(rawEmulatedStorageTarget + File.separator + rawUserId + File.separator + AizobanApplication.getInstance().getPackageName());
                }
            }

            if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
                String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
                for (int index  = 0; index < rawSecondaryStorages.length; index++) {
                    storageDirectories.add(rawSecondaryStorages[index] + File.separator + AizobanApplication.getInstance().getPackageName());
                }
            }
        }

        return storageDirectories.toArray(new String[storageDirectories.size()]);
    }

    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static File saveInputStreamToDirectory(InputStream inputStream, String directory, String name) throws IOException {
        File fileDirectory = new File(directory);
        if (!fileDirectory.exists()) {
            if (!fileDirectory.mkdirs()) {
                throw new IOException("Failed Creating  Directory");
            }
        }

        File writeFile = new File(fileDirectory, name);
        if (writeFile.exists()) {
            if (writeFile.delete()) {
                writeFile = new File(fileDirectory, name);
            } else {
                throw new IOException("Failed Deleting Existing File for Overwrite");
            }
        }

        OutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(writeFile);

            byte[] fileBuffer= new byte[1024];
            for (int counter = 0; counter != -1; counter = inputStream.read(fileBuffer, 0, 1024)) {
                outputStream.write(fileBuffer, 0, counter);
            }

            outputStream.flush();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }

        return writeFile;
    }

    public static void deleteFiles(File inputFile) {
        if (inputFile.isDirectory()) {
            for (File childFile : inputFile.listFiles()) {
                deleteFiles(childFile);
            }
        }

        inputFile.delete();
    }
}
