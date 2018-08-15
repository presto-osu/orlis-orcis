/*
 * Copyright (C) 2015 Matthias Gabriel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gabm.fancyplaces.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.gabm.fancyplaces.FancyPlacesApplication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.channels.FileChannel;

/**
 * Created by gabm on 23.12.14.
 */
public class ImageFile implements Parcelable, Serializable {
    public static final Parcelable.Creator<ImageFile> CREATOR
            = new Parcelable.Creator<ImageFile>() {
        public ImageFile createFromParcel(Parcel in) {
            return new ImageFile(in);
        }

        public ImageFile[] newArray(int size) {
            return new ImageFile[size];
        }
    };
    public static FancyPlacesApplication curAppContext = null;
    private String FileName;

    public ImageFile() {
        FileName = "";
    }

    public ImageFile(Parcel in) {
        FileName = in.readString();
    }

    public ImageFile(ImageFile rhs) {
        this(rhs.FileName);
    }

    public ImageFile(String fileName) {
        FileName = fileName;
    }

    public static ImageFile cloneObject(ImageFile rhs) {
        return new ImageFile(rhs.FileName);
    }

    public static ImageFile saveBitmap(Bitmap bmp, String fileName) {
        ImageFile result = new ImageFile();
        result.FileName = fileName;

        File file = new File(result.FileName);

        FileOutputStream fos = null;
        try {

            fos = new FileOutputStream(file);

            // Use the compress method on the BitMap object to write image to the OutputStream
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(FileName);
    }

    public ImageFile copy(String fileName) {
        ImageFile result = new ImageFile(fileName);

        File src = new File(FileName);
        File dest = new File(fileName);

        // copy file to our destination
        try {
            copy(src, dest);
        } catch (IOException e) {
            Log.d("IOException: ", "Couldn't copy image");
        }

        return result;
    }
/*
    public ImageSize getSize()
    {
        if (!exists())
            return null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(FileName, options);
        return new ImageSize(options.outWidth, options.outHeight);
    }*/

    public void move(String fileName) {
        ImageFile newFile = copy(fileName);
        this.delete();
        this.FileName = newFile.FileName;
    }

    public void delete() {
        File curFile = new File(FileName);
        if (curFile.exists())
            curFile.delete();
    }

    public void scaleDown(float maxImageSize) {

        Bitmap oldImage = loadFullSizeImage();

        float ratio = Math.min(
                maxImageSize / oldImage.getWidth(),
                maxImageSize / oldImage.getHeight());
        int width = Math.round(ratio * oldImage.getWidth());
        int height = Math.round(ratio * oldImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(oldImage, width, height, true);

        saveBitmap(newBitmap, getFileName());
    }

    public Bitmap loadFullSizeImage() {
        return loadBitmap(1);
    }

    public Bitmap loadThumbnail() {
        Bitmap thumbnail = loadBitmap(4);
        int minSize = 0;
        int offsetWidth = 0;
        int offsetHeight = 0;
        if (thumbnail.getHeight() < thumbnail.getWidth()) {
            minSize = thumbnail.getHeight();
            offsetWidth = (thumbnail.getWidth() - minSize) / 2;
        } else {
            minSize = thumbnail.getWidth();
            offsetHeight = (thumbnail.getHeight() - minSize) / 2;
        }

        Bitmap output = Bitmap.createBitmap(minSize, minSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect destRect = new Rect(0, 0, minSize, minSize);
        final Rect srcRect = new Rect(offsetWidth, offsetHeight, minSize, minSize);
        final RectF rectF = new RectF(destRect);
        final float roundPx = minSize;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(thumbnail, srcRect, destRect, paint);

        return output;
    }

    private Bitmap loadBitmap(int sampleSize) {
        if (!exists())
            return null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;

        Bitmap resultBitmap = BitmapFactory.decodeFile(FileName, options);


        return resultBitmap;
    }

    public Boolean exists() {
        return (new File(FileName)).exists();
    }

    public String getFileName() {
        return FileName;
    }

    private void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

    private void writeObject(ObjectOutputStream oos)
            throws IOException {

        SerializableImage serializableImage = new SerializableImage();
        serializableImage.setImage(loadFullSizeImage());

        // write the image
        oos.writeObject(serializableImage);

    }

    private void readObject(ObjectInputStream ois)
            throws ClassNotFoundException, IOException {
        // deserialize img
        SerializableImage img = (SerializableImage) ois.readObject();

        // save image to tmp dir
        ImageFile tmpImgFile = ImageFile.saveBitmap(img.getImage(), com.gabm.fancyplaces.FancyPlacesApplication.TMP_IMAGE_FULL_PATH);
        FileName = tmpImgFile.FileName;
    }

    protected class SerializableImage implements Serializable {

        private byte[] ImageData = null;

        public Bitmap getImage() {
            return BitmapFactory.decodeByteArray(ImageData, 0, ImageData.length);
        }

        public void setImage(Bitmap bmp) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            ImageData = stream.toByteArray();
        }
    }
}
